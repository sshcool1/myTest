package net.ion.board;

import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.ReadChildren;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.radon.core.ContextParam;

import org.jboss.resteasy.spi.HttpRequest;

import com.google.common.base.Function;

@Path("/board")
public class BoardWeb {
	
	private ReadSession rsession;
	private HttpRequest req;
	
	public BoardWeb(@ContextParam("session") ReadSession rsession ,@Context HttpRequest req){
		this.rsession = rsession;
		this.req = req;
	}
	
	@GET
	@Path("")
	public JsonObject boardMain() {
		ReadChildren children = rsession.ghostBy("/board/notice").children();
		
		return new JsonObject().put("list", children.transform(new Function<Iterator<ReadNode>, JsonArray>(){
			@Override
			public JsonArray apply(Iterator<ReadNode> iter) {
				JsonArray result = new JsonArray();
				while (iter.hasNext()) {
					ReadNode node = iter.next();
					result.add(node.toValueJson());
				}
				return result;
			}
		}));
	}
	
	@POST
	@Path("/insert")
	public JsonObject boardInsert(){
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				Object[] childNames = rsession.ghostBy("/board/notice").childrenNames().toArray();
				int name = 0;
				if(childNames.length > 0 )
					name = Integer.parseInt(childNames[childNames.length - 1].toString()) + 1;
					
				wsession.pathBy("/board/notice/" + name).property("title", req.getFormParameters().get("title")).property("content", req.getFormParameters().get("content")).property("writer", req.getFormParameters().get("writer")) ;
				return null;
			}
		});
		return new JsonObject().put("result", "success");
	}
	
	
	
}

