package net.ion.board;

import java.io.File;

import junit.framework.TestCase;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.Craken;
import net.ion.framework.util.InfinityThread;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.nradon.config.RadonConfigurationBuilder;
import net.ion.nradon.handler.SimpleStaticFileHandler;
import net.ion.radon.core.let.PathHandler;

public class TestServer extends TestCase {

	public void testFirst() throws Exception {
		RadonConfigurationBuilder radonBuilder = RadonConfiguration.newBuilder(9000)
			 //resources/common/js/jquery.min_1.9.1.js
		.add("/resource/*", new SimpleStaticFileHandler(new File("./")))
		.add("/board/*", new SimpleStaticFileHandler(new File("./resource")))
		.add("/api/board/*", new PathHandler(BoardWeb.class)) ;
		
		Craken craken = Craken.inmemoryCreateWithTest() ;
		ReadSession rsession = craken.login("test") ;
		radonBuilder.context("session", rsession) ;
		
		radonBuilder.start().get() ;
		new InfinityThread().startNJoin(); 
	}
}
