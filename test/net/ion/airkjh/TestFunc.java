package net.ion.airkjh;

import net.ion.framework.util.Debug;
import net.ion.nradon.HttpControl;
import net.ion.nradon.HttpHandler;
import net.ion.nradon.HttpRequest;
import net.ion.nradon.HttpResponse;
import net.ion.nradon.Radon;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.nradon.config.RadonConfigurationBuilder;
import net.ion.nradon.handler.event.ServerEvent.EventType;
import net.ion.nradon.netty.NettyWebServer;
import net.ion.radon.Options;
import net.ion.radon.aclient.NewClient;
import junit.framework.TestCase;

public class TestFunc extends TestCase {

	public void testDefaultValue() throws Exception {
		Options opt = new Options(new String[0]) ;
		String value = opt.getString("config", "./resource/config/niss-config.xml");
		
		assertEquals("./resource/config/niss-config.xml", value);
	}
	
	public void testWithOption() throws Exception {
		Options opt = new Options(new String[] {"-config:text.xml"}) ;
		String value = opt.getString("config", "./resource/config/niss-config.xml");
		
		assertEquals("text.xml", value);
	}
	
	public void testRadon() throws Exception {
		NettyWebServer radon = RadonConfiguration.newBuilder(8999).createRadon();
		radon.start().get();
		
		radon.add(new HttpHandler() {
			@Override
			public int order() {
				return 1;
			}
			
			@Override
			public void onEvent(EventType event, Radon radon) {
			}
			
			@Override
			public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
				System.out.println("Handler1");
				response.status(404).end();
			}
		}).add(new HttpHandler() {
			
			@Override
			public int order() {
				return 2;
			}
			
			@Override
			public void onEvent(EventType event, Radon radon) {
				
			}
			
			@Override
			public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
				System.out.println("Handler2");
				control.nextHandler(request, response);
			}
		});
		
		NewClient nc = NewClient.create();
		nc.prepareGet("http://localhost:8999").execute().get();
		
		nc.close();
		radon.stop();
	}
	
}
