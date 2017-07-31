package net.ion.aradontest;

import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import junit.framework.TestCase;
import net.ion.framework.util.InfinityThread;
import net.ion.nradon.Radon;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.radon.core.let.PathHandler;

public class TestAradon extends TestCase{

	public void testRun() throws Exception {
		/*Radon radon = RadonConfiguration.newBuilder(9500)
				.add(new PathHandler(TestHello.class))
				.startRadon();*/
		
		
		
		
		
		
		new InfinityThread().startNJoin();
	}
	
}

