package net.ion.board;

import net.ion.framework.util.Debug;
import net.ion.nradon.stub.StubHttpResponse;
import net.ion.radon.client.StubServer;
import junit.framework.TestCase;

public class TestSiteResource extends TestCase{

	
	public void testResource() throws Exception{
		StubServer ss = StubServer.create(SiteResource.class) ;
		
		StubHttpResponse response = ss.request("/hello.htm").get() ;
		assertEquals(200, response.status());
		Debug.line(response.contentsString());

		response = ss.request("/hello.html").get() ;
		assertEquals(404, response.status());

	}
}
