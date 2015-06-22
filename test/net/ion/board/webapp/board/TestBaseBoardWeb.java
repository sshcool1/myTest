package net.ion.board.webapp.board;

import junit.framework.TestCase;
import net.ion.board.webapp.board.BoardWeb;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.Craken;
import net.ion.radon.client.StubServer;

public class TestBaseBoardWeb extends TestCase{

	protected StubServer ss;
	protected Craken craken;
	private ReadSession rsession;
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.ss = StubServer.create(BoardWeb.class);
		this.craken = Craken.inmemoryCreateWithTest();
		this.rsession = craken.login("test");
		
		this.ss.treeContext().putAttribute("session", rsession);
	}

	@Override
	protected void tearDown() throws Exception {
		ss.shutdown();
		super.tearDown();
	}
}
