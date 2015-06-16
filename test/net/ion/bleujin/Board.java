package net.ion.bleujin;

import java.io.IOException;
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
import net.ion.craken.node.crud.Craken;
import net.ion.craken.node.crud.tree.impl.PropertyValue;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.radon.core.ContextParam;

public @Path("/board")
class Board {
	private ReadSession session;
	public Board(@ContextParam("craken") Craken craken) throws IOException{
		this.session = craken.login("test") ;
	}

	@Path("/add")
	@POST
	public String addArticle(@FormParam("subject") final String subject, @FormParam("content") final String content) throws IOException, InterruptedException, ExecutionException{
		return "" + session.tran(new TransactionJob<Integer>() {
			@Override
			public Integer handle(WriteSession wsession) throws Exception {
				PropertyValue count = wsession.pathBy("/article").increase("count") ;
				wsession.pathBy("/article", count.asInt()).property("subject", subject).property("content", content) ;
				return count.asInt();
			}
		}).get() + " article added" ;
	}
	
	@Path("/view")
	@GET
	public JsonObject viewArticle(@QueryParam("no") int no){
		ReadNode found = session.ghostBy("/article", no) ;
		
		Article article = found.toBean(Article.class) ;
		Debug.line(article, article.subject());
		
		return JsonObject.create().put("subject", found.property("subject").asString()).put("content", found.property("content").asString()) ;
	}
	
}

class Article {
	private String subject ;
	private String content ;
	
	public String subject() {
		return subject ;
	}
}



