package net.ion.board;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.nradon.stub.StubHttpResponse;


public class TestBoardWeb extends TestBaseBoardWeb{

	public void testMain() throws Exception {
		StubHttpResponse response = ss.request("/board").get() ;
		assertTrue(JsonObject.fromString(response.contentsString()).has("list"));
	}
	
	public void testInsert() throws Exception {
		
		StubHttpResponse response = ss.request("/board/insert")
									.postParam("title", "title")
									.postParam("content", "This is Notice")
									.postParam("writer", "It's Me!!").post() ;
		
		System.out.println("request : " + JsonObject.fromString(response.contentsString()));
		
		response = ss.request("/board/insert")
				.postParam("title", "2title")
				.postParam("content","2This is Notice")
				.postParam("writer", "2It's Me!!").post() ;
		
		System.out.println("request : " + JsonObject.fromString(response.contentsString()));
		
		response = ss.request("/board").get() ;
		
		System.out.println("");
		assertTrue(JsonObject.fromString(response.contentsString()).has("list"));
	}
	
	public void testDelete() { 
		
		
		
	}
}

