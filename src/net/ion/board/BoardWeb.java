package net.ion.board;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.ReadChildren;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.radon.core.ContextParam;

import com.google.common.base.Function;

@Path("/board")
public class BoardWeb {
	
	private ReadSession rsession;
	
	public BoardWeb(@ContextParam("session") ReadSession rsession){
		this.rsession = rsession;
	}
	//list
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
	//insert
	@POST
	@Path("/insert")
	public JsonObject boardInsert(@FormParam("content") final String content , @FormParam("seq") final String seq , @FormParam("title")final String title) throws InterruptedException, ExecutionException{
		return new JsonObject().put("result", rsession.tran(new TransactionJob<String>() {
			@Override
			public String handle(WriteSession wsession) throws Exception {
				int seq = wsession.pathBy("/board/notice").increase("seq").asInt();
				wsession.pathBy("/board/notice", seq).property("title", title).property("content", content).property("seq",seq);
				return "success";
			}
		}).get());
		
	}
	
	//update 
	@POST
	@Path("/update")
	public JsonObject boardUpdate( @FormParam("seq") final String seq, @FormParam("content") final String content  , @FormParam("title")final String title ) throws InterruptedException, ExecutionException {
		if(!exists(seq)) {
			throw new IllegalArgumentException();
		}
		return new JsonObject().put("result" , rsession.tran(new TransactionJob<String>() {
			@Override
			public String handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/board/notice" , seq ).property("content" , content).property("title", title);
				return "success";
			}
		}).get());
	}
	
	//select 
	@GET
	@Path("/select")
	public JsonObject boardSelect(@QueryParam("seq") String seq){
		ReadNode rNode = rsession.pathBy("/board/notice", seq);
		return new JsonObject().put("result" , rNode.toValueJson() );
	}
	
	//delete 
	@POST
	@Path("/delete") 
	public JsonObject boardDelete(@FormParam("seq")final String seq ) throws InterruptedException, ExecutionException  {
		return new JsonObject().put("result" , rsession.tran(new TransactionJob<String>() {
			@Override
			public String handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/board/notice" , seq).removeSelf();
				return "success";
			}
		}).get());
	}
	
	private boolean exists(String seq) {
		ReadNode rNode = rsession.ghostBy("/board/notice" , seq);
		return !rNode.isGhost() ;
	}
}

