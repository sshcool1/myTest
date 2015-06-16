package net.ion.bleujin;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.FormParam;
import javax.ws.rs.Path;

import junit.framework.TestCase;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.Craken;
import net.ion.craken.node.crud.tree.impl.PropertyValue;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.nradon.stub.StubHttpResponse;
import net.ion.radon.client.StubServer;
import net.ion.radon.core.ContextParam;


public class TestBoard extends TestCase {

	public void testAdd() throws Exception {
		StubServer server = StubServer.create(Board.class) ;
		server.treeContext().putAttribute("craken", Craken.inmemoryCreateWithTest()) ;
		
		StubHttpResponse response = server.request("/board/add").postParam("subject", "first article").postParam("content", "this is first article").post() ;
		assertEquals("1 article added", response.contentsString());
		
		
		response = server.request("/board/view?no=1").get() ;
		JsonObject json = JsonObject.fromString(response.contentsString()) ;
		
		assertEquals("first article", json.asString("subject"));
		
	}
	
}


