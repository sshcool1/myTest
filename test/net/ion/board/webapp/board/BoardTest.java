package net.ion.board.webapp.board;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import net.ion.board.webapp.board.Board;
import net.ion.board.webapp.board.BoardBean;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.Craken;

import org.apache.lucene.index.CorruptIndexException;

public class BoardTest extends TestCase {
	Craken craken;
	ReadSession rsession;
	Board service ;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		craken = Craken.inmemoryCreateWithTest();	
		rsession = craken.login("test");
		service = new Board(rsession);
	}
	
	@Override
	protected void tearDown() throws Exception {
		craken.shutdown();
		super.tearDown();
	}
	
	public void testFirst() throws CorruptIndexException, IOException {
		boolean result = service.bean(new BoardBean("a","aa")).insert();
		assertEquals(true, result);
	}
	
	public void testAutoIncreate() throws Exception {
		
		for (int i=0; i< 20; i++) {
			service.bean(new BoardBean("a" + i,"aa"  + i)).insert();	
		}
	
		List<BoardBean> list = service.list(0, 10);
		for(BoardBean bean : list)
			System.out.println("outPut : " + bean.toString());
	}
	
	public void testSelect() throws Exception {
		
		boolean result = service.bean(new BoardBean().seq(0).title("a").content("aa")).insert();
		
		Board board = service.get(0);
		
		assertEquals("0", board.bean.seq);
		assertEquals("a", board.bean.title);
		assertEquals("aa", board.bean.content);
		
	}
	
	public void testSelectNotExists() {
		try{
			service.get(0);
			fail();
		}catch(IllegalArgumentException e){
			
		}
	}
	
	public void testUpdate(){
		
		service.bean(new BoardBean(0, "a", "aa"));
		service.update();
		
		service.bean( new BoardBean(0, "b", "bb") );
		boolean result = service.update();
		
		assertEquals(true, result);
	}
	
	public void testInit() throws Exception {
		
		service.newArticle(new BoardBean().content("world")).insert();
		
		//service.get(1).updateArticle(new BoardBean().title("test").content("world2")).update();
		
		service.get(1).removeSelf();
		
		service.list(0, 10);//, new Sort(title, Order.DESC));
	}
}

