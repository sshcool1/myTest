package net.ion.board;

import java.io.File;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import net.ion.board.config.builder.ConfigBuilder;
import net.ion.board.webapp.HTMLTemplateEngine;
import net.ion.board.webapp.REntry;
import net.ion.board.webapp.board.BoardWeb;
import net.ion.board.webapp.common.MyStaticFileHandler;
import net.ion.framework.db.ThreadFactoryBuilder;
import net.ion.framework.util.InfinityThread;
import net.ion.nradon.Radon;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.nradon.config.RadonConfigurationBuilder;
import net.ion.nradon.handler.SimpleStaticFileHandler;
import net.ion.radon.core.let.PathHandler;

public class TestServer extends TestCase {

	public void testFirst() throws Exception {
		Radon radon; 
		
		RadonConfigurationBuilder radonBuilder = RadonConfiguration.newBuilder(9001);
		
		radonBuilder.context(REntry.EntryName, REntry.create(ConfigBuilder.create("./resource/config/board-config.xml").build()));
		
		radon = radonBuilder.createRadon();
		radon.add("/resource/*", new SimpleStaticFileHandler(new File("./")))
		.add("/board/*", new SimpleStaticFileHandler(new File("./resource")))
		.add(new MyStaticFileHandler("./webapps/admin/", Executors.newCachedThreadPool(ThreadFactoryBuilder.createThreadFactory("static-io-thread-%d")), new HTMLTemplateEngine(radon.getConfig().getServiceContext())).welcomeFile("index.html"))
		.add("/admin/board/*", new PathHandler(BoardWeb.class).prefixURI("/admin") ) ;
		
		radonBuilder.start().get() ;
		new InfinityThread().startNJoin(); 
	}
}
