package net.ion.board.webapp;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import javax.script.ScriptException;
import javax.ws.rs.core.MultivaluedMap;

import net.ion.board.config.NSConfig;
import net.ion.board.config.builder.ConfigBuilder;
import net.ion.board.webapp.common.Def;
import net.ion.board.webapp.common.Def.Script;
import net.ion.board.webapp.loaders.InstantJavaScript;
import net.ion.board.webapp.loaders.JScriptEngine;
import net.ion.board.webapp.loaders.ResultHandler;
import net.ion.board.webapp.misc.ScriptWeb;
import net.ion.craken.listener.CDDHandler;
import net.ion.craken.listener.CDDModifiedEvent;
import net.ion.craken.listener.CDDRemovedEvent;
import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.Craken;
import net.ion.craken.node.crud.tree.Fqn;
import net.ion.craken.node.crud.tree.impl.PropertyId;
import net.ion.craken.node.crud.tree.impl.PropertyValue;
import net.ion.framework.db.ThreadFactoryBuilder;
import net.ion.framework.parse.gson.stream.JsonWriter;
import net.ion.framework.schedule.AtTime;
import net.ion.framework.schedule.Job;
import net.ion.framework.schedule.ScheduledRunnable;
import net.ion.framework.schedule.Scheduler;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;

import org.apache.lucene.index.CorruptIndexException;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

public class REntry implements Closeable {

	public final static String EntryName = "rentry";

	private Craken r;
	private String wsName;
	private NSConfig nsconfig;

	private ReadSession rsession;
	private final Log log = LogFactory.getLog(REntry.class);
	private Scheduler scheduler = new Scheduler("scripter", Executors.newCachedThreadPool(ThreadFactoryBuilder.createThreadFactory("scripters-thread-%d")));

	public REntry(Craken r, String wsName, NSConfig nsconfig) throws IOException {
		this.r = r;
		this.wsName = wsName;
		this.nsconfig = nsconfig;

		this.rsession = login();
		initCDDListener(rsession);
	}

	// use test only (central not finished)
	public void reload() {
		initCDDListener(rsession);
	}

	private void initCDDListener(final ReadSession session) {

		final JScriptEngine jsengine = JScriptEngine.create();

		session.workspace().cddm().add(new CDDHandler() {
			@Override
			public String pathPattern() {
				return "/scripts/{sid}/schedule";
			}

			@Override
			public TransactionJob<Void> deleted(Map<String, String> rmap, CDDRemovedEvent cevent) {
				String sid = rmap.get("sid");
				scheduler.removeJob(sid);

				return null;
			}

			@Override
			public TransactionJob<Void> modified(Map<String, String> rmap, CDDModifiedEvent cevent) {
				EventPropertyReadable rnode = new EventPropertyReadable(cevent);
				String jobId = rmap.get("sid");
				if (rnode.property(Def.Schedule.ENABLE).asBoolean()) {
					ReadNode sinfo = rsession.ghostBy("/scripts/" + jobId + "/schedule");
					scheduler.removeJob(jobId);
					AtTime at = makeAtTime(sinfo);
					scheduler.addJob(new Job(jobId, makeCallable(jobId), at));
				} else {
					scheduler.removeJob(jobId);
				}

				return null;
			}

			private ScheduledRunnable makeCallable(final String scriptId) {
				return new ScheduledRunnable() {
					@Override
					public void run() {
						final ReadNode scriptNode = rsession.ghostBy("/scripts/" + scriptId);

						// should check running(in distribute mode)
						if (scriptNode.property(Script.Running).asBoolean())
							return;
						rsession.tran(new TransactionJob<Void>() {
							@Override
							public Void handle(WriteSession wsession) throws Exception {
								wsession.pathBy(scriptNode.fqn()).property(Script.Running, true);
								return null;
							}
						});
						//

						String scriptContent = scriptNode.property(Def.Script.Content).asString();
						StringWriter result = new StringWriter();
						final JsonWriter jwriter = new JsonWriter(result);

						try {
							StringWriter writer = new StringWriter();
							MultivaluedMap<String, String> params = new MultivaluedMapImpl<String, String>();
							InstantJavaScript script = jsengine.createScript(IdString.create(scriptId), "", new StringReader(scriptContent));

							String[] execResult = script.exec(new ResultHandler<String[]>() {
								@Override
								public String[] onSuccess(Object result, Object... args) {
									try {
										jwriter.beginObject().name("return").value(ObjectUtil.toString(result));
									} catch (IOException ignore) {
									} finally {
										rsession.tran(new TransactionJob<Void>() {
											@Override
											public Void handle(WriteSession wsession) throws Exception {
												wsession.pathBy(scriptNode.fqn()).property(Script.Running, false);
												return null;
											}
										});
									}
									return new String[] { "schedule success", ObjectUtil.toString(result) };
								}

								@Override
								public String[] onFail(Exception ex, Object... args) {
									try {
										jwriter.beginObject().name("return").value("").name("exception").value(ex.getMessage());
									} catch (IOException e) {
									} finally {
										rsession.tran(new TransactionJob<Void>() {
											@Override
											public Void handle(WriteSession wsession) throws Exception {
												wsession.pathBy(scriptNode.fqn()).property(Script.Running, false);
												return null;
											}
										});
									}
									return new String[] { "schedule fail", ex.getMessage() };
								}
							}, writer, rsession, params, REntry.this, jsengine);

							jwriter.name("writer").value(writer.toString());

							jwriter.name("params");
							jwriter.beginArray();
							for (Entry<String, List<String>> entry : params.entrySet()) {
								jwriter.beginObject().name(entry.getKey()).beginArray();
								for (String val : entry.getValue()) {
									jwriter.value(val);
								}
								jwriter.endArray().endObject();
							}
							jwriter.endArray();
							jwriter.endObject();
							jwriter.close();
							rsession.tran(ScriptWeb.end(scriptId, execResult[0], execResult[1]));
						} catch (IOException ex) {
							rsession.tran(ScriptWeb.end(scriptId, "schedule fail", ex.getMessage()));
						} catch (ScriptException ex) {
							rsession.tran(ScriptWeb.end(scriptId, "schedule fail", ex.getMessage()));
						} finally {
							IOUtil.close(jwriter);
						}
						// write log

					}
				};
			}

			private AtTime makeAtTime(ReadNode sinfo) {
				String expr = StringUtil.coalesce(sinfo.property("minute").asString(), "*") + " " + StringUtil.coalesce(sinfo.property("hour").asString(), "*") + " "
						+ StringUtil.coalesce(sinfo.property("day").asString(), "*") + " " + StringUtil.coalesce(sinfo.property("month").asString(), "*") + " "
						+ StringUtil.coalesce(sinfo.property("week").asString(), "*") + " " + StringUtil.coalesce(sinfo.property("matchtime").asString(), "*") + " "
						+ StringUtil.coalesce(sinfo.property("year").asString(), "*");

				return new AtTime(expr);
			}

		});

		scheduler.start();

		// register schedule job
		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				IteratorList<WriteNode> scripts = wsession.pathBy("/scripts").children().iterator();
				while (scripts.hasNext()) {
					WriteNode wnode = scripts.next();
					wnode.property(Script.Running, false);
					if (wnode.hasChild("schedule")) {
						WriteNode scheduleNode = wnode.child("schedule");
						if (scheduleNode.property(Def.Schedule.ENABLE).asBoolean()) {
							scheduleNode.property(Def.Schedule.ENABLE, true);
						}
					}
				}
				return null;
			}
		});

	}

	// test
	public final static REntry create() throws CorruptIndexException, IOException {
		NSConfig nsconfig = ConfigBuilder.createDefault(9000).build();
		return create(nsconfig);
	}

	public final static REntry create(NSConfig nsconfig) throws CorruptIndexException, IOException {
		return nsconfig.createREntry();
	}

	public final static REntry test() throws CorruptIndexException, IOException {
		NSConfig nsconfig = ConfigBuilder.createDefault(9000).build();
		return nsconfig.testREntry();
	}

	public ReadSession login() throws IOException {
		return r.login(wsName);
	}

	public Craken repository() {
		return r;
	}

	@Override
	public void close() throws IOException {
		r.shutdown();
	}

}

interface PropertyReadable {
	public PropertyValue property(String propId);

	public PropertyValue property(PropertyId propId);

	public Fqn fqn();
}

class EventPropertyReadable implements PropertyReadable {

	private CDDModifiedEvent event;

	public EventPropertyReadable(CDDModifiedEvent event) {
		this.event = event;
	}

	@Override
	public PropertyValue property(String propId) {
		return event.property(propId);
	}

	@Override
	public PropertyValue property(PropertyId propId) {
		return event.property(propId);
	}

	public Fqn fqn() {
		return event.getKey().getFqn();
	}
}

class RNodePropertyReadable implements PropertyReadable {

	private ReadNode rnode;

	public RNodePropertyReadable(ReadNode node) {
		this.rnode = node;
	}

	@Override
	public PropertyValue property(String propId) {
		return rnode.property(propId);
	}

	@Override
	public PropertyValue property(PropertyId propId) {
		return rnode.propertyId(propId);
	}

	public Fqn fqn() {
		return rnode.fqn();
	}
}
