package net.ion.ssh;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import junit.framework.TestCase;
import net.ion.framework.util.Debug;
import net.ion.nradon.HttpControl;
import net.ion.nradon.HttpHandler;
import net.ion.nradon.HttpRequest;
import net.ion.nradon.HttpResponse;
import net.ion.nradon.Radon;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.nradon.handler.event.ServerEvent.EventType;
import net.ion.radon.aclient.NewClient;
import net.ion.radon.aclient.Response;
import net.ion.radon.core.let.PathHandler;

public class TestAradon extends TestCase {

	public void testRadonHandler() throws Exception {

		Radon radon = RadonConfiguration.newBuilder(9002)
				.add("/test/*", new PathHandler(DummyLet.class).prefixURI("test"))
				.add(new TestHandler("3")).startRadon();
		
		NewClient nc = NewClient.create();

		Response response = nc.prepareGet("http://localhost:9002/test/dummy").execute().get();
		Debug.line(response.getTextBody());

		nc.close();
		radon.stop().get();

		// new InfinityThread().startNJoin();
	}
}

@Path("/dummy")
class DummyLet {

	@GET
	@Path("")
	public String respond() {
		return "Hello World";
	}

}

class TestHandler implements HttpHandler {
	String name;

	public TestHandler(String name) {
		this.name = name;
	}

	@Override
	public void onEvent(EventType event, Radon radon) {
		System.out.println("test : " + name);

	}

	@Override
	public int order() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
		// TODO Auto-generated method stub

	}
}
