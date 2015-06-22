package net.ion.board.webapp.board;

import net.ion.framework.parse.gson.JsonObject;
import net.ion.nradon.stub.StubHttpResponse;


public class TestBoardWeb extends TestBaseBoardWeb{
	
	StubHttpResponse response ;
	
	//list
	public void testMain() throws Exception {
		response = ss.request("/board").get() ;
		assertTrue(JsonObject.fromString(response.contentsString()).has("list"));
	}
	
	//select
	public void testSelect() throws Exception {
		
		ss.request("/board/insert").postParam("title", "title").postParam("content", "This is Notice").post() ;
		
		response = ss.request("/board/select?seq=1").get();
		
		JsonObject fromString = JsonObject.fromString(response.contentsString()).get("result").getAsJsonObject();
		assertEquals("title", fromString.get("title").getAsJsonObject().get("vals").getAsString());
	}
	
	//insert
	public void testInsert() throws Exception {
		response = ss.request("/board/insert").postParam("title", "title").postParam("content", "This is Notice").post() ;
		assertEquals("success" ,JsonObject.fromString(response.contentsString()).get("result").getAsString());
	}
	
	
	//update 
	public void testUpdate() throws Exception {
		ss.request("/board/insert").postParam("title", "title").postParam("content", "This is Notice").post() ;
		response = ss.request("/board/select?seq=1").get();
		JsonObject fromString = JsonObject.fromString(response.contentsString()).get("result").getAsJsonObject();
		assertEquals("title", fromString.get("title").getAsJsonObject().get("vals").getAsString() );
		
		ss.request("/board/update").postParam("seq", "1").postParam("title", "title2").post() ;
		
		response = ss.request("/board/select?seq=1").get();
		fromString = JsonObject.fromString(response.contentsString()).get("result").getAsJsonObject();
		assertEquals("title2", fromString.get("title").getAsJsonObject().get("vals").getAsString() );
	}
	
	//delete
	public void testDelete() throws Exception { 
		
		ss.request("/board/insert").postParam("title", "title").postParam("content", "This is Notice").post() ;
		
		response = ss.request("/board").get() ;
		assertEquals(1, JsonObject.fromString(response.contentsString()).get("list").getAsJsonArray().size());
		
		ss.request("/board/delete").postParam("seq", "1").post();
		
		response = ss.request("/board").get() ;
		assertEquals(0, JsonObject.fromString(response.contentsString()).get("list").getAsJsonArray().size());
	}
}

