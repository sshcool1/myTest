package net.ion.board;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import net.ion.board.config.NSConfig;
import net.ion.board.config.builder.ConfigBuilder;
import net.ion.board.webapp.AppLogSink;
import net.ion.board.webapp.HTMLTemplateEngine;
import net.ion.board.webapp.REntry;
import net.ion.board.webapp.board.BoardWeb;
import net.ion.board.webapp.common.MyAuthenticationHandler;
import net.ion.board.webapp.common.MyEventLog;
import net.ion.board.webapp.common.MyStaticFileHandler;
import net.ion.board.webapp.common.MyVerifier;
import net.ion.board.webapp.common.TraceHandler;
import net.ion.board.webapp.misc.MenuWeb;
import net.ion.board.webapp.misc.MiscWeb;
import net.ion.board.webapp.misc.TraceWeb;
import net.ion.framework.db.ThreadFactoryBuilder;
import net.ion.nradon.EventSourceConnection;
import net.ion.nradon.EventSourceHandler;
import net.ion.nradon.HttpControl;
import net.ion.nradon.HttpHandler;
import net.ion.nradon.HttpRequest;
import net.ion.nradon.HttpResponse;
import net.ion.nradon.Radon;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.nradon.config.RadonConfigurationBuilder;
import net.ion.nradon.handler.SimpleStaticFileHandler;
import net.ion.nradon.handler.event.ServerEvent.EventType;
import net.ion.nradon.handler.logging.LoggingHandler;
import net.ion.radon.core.let.PathHandler;

public class BoardServer {

	private RadonConfigurationBuilder builder;
	private Radon radon;
	private NSConfig nsconfig;

	private enum Status {
		STOPED, INITED, STARTED;
	}

	private AtomicReference<Status> status = new AtomicReference<BoardServer.Status>(Status.STOPED);
	private REntry rentry;

	BoardServer(NSConfig nsconfig) {
		this.nsconfig = nsconfig;
	}

	public static BoardServer create(NSConfig nsconfig) throws Exception {
		BoardServer server = new BoardServer(nsconfig);
		server.init();
		return server;
	}

	public static BoardServer create(int portNum) throws Exception {
		return create(ConfigBuilder.createDefault(portNum).build());
	}

	public void init() throws Exception {
		this.builder = RadonConfiguration.newBuilder(nsconfig.serverConfig().port());
		this.rentry = this.builder.context(REntry.EntryName, REntry.create(nsconfig));

		final EventSourceEntry esentry = builder.context(EventSourceEntry.EntryName, EventSourceEntry.create());

		this.radon = builder.createRadon();
		
		final MyEventLog elogger = MyEventLog.create(System.out);

		radon.add(new MyAuthenticationHandler(MyVerifier.test(rentry.login())))
				.add("/admin/*", new TraceHandler(rentry))
				.add("/resource/*", new SimpleStaticFileHandler(new File("./")))
				.add("/board/*", new SimpleStaticFileHandler(new File("./resource")))
				.add(new LoggingHandler(new AppLogSink(elogger)))
				.add(new MyStaticFileHandler("./webapps/admin/", Executors.newCachedThreadPool(ThreadFactoryBuilder.createThreadFactory("static-io-thread-%d")), new HTMLTemplateEngine(radon
						.getConfig().getServiceContext())).welcomeFile("index.html"))
				.add("/admin/*", new PathHandler(BoardWeb.class, MiscWeb.class , MenuWeb.class , TraceWeb.class ).prefixURI("/admin"))
				.add("/logging/event/*", new EventSourceHandler() {
					@Override
					public void onOpen(EventSourceConnection econn) throws Exception {
						elogger.onOpen(econn);
					}

					@Override
					public void onClose(EventSourceConnection econn) throws Exception {
						elogger.onClose(econn);
					}
				}).add("/event/{id}", new EventSourceHandler() {
					@Override
					public void onOpen(EventSourceConnection conn) throws Exception {
						esentry.onOpen(conn);
					}

					@Override
					public void onClose(EventSourceConnection conn) throws Exception {
						esentry.onClose(conn);
					}
				}).add(new HttpHandler() {

					@Override
					public void onEvent(EventType eventtype, Radon radon) {
					}

					@Override
					public int order() {
						return 1000;
					}

					@Override
					public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
						response.status(404).content("not found path : " + request.uri()).end();
					}
				});
		// .add("/api/board/*", new PathHandler(BoardWeb.class)) ;

		radon.getConfig().getServiceContext().putAttribute(BoardServer.class.getCanonicalName(), this);
		status.set(Status.INITED);
		;
	}

	public NSConfig config() {
		return nsconfig;
	}

	@Deprecated
	// only test
	public REntry rentry() {
		return rentry;
	}

	public BoardServer start() throws Exception {
		if (this.builder == null)
			init();

		this.radon.start().get();
		status.set(Status.STARTED);
		return this;
	}

	public BoardServer shutdown() throws InterruptedException, ExecutionException {
		if (status.get() == Status.STOPED)
			return this;

		radon.stop().get();
		status.set(Status.STOPED);
		return this;
	}

}
