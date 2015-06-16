package net.ion.aradontest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.ChildQueryResponse;
import net.ion.craken.node.crud.Craken;
import net.ion.craken.node.crud.ReadChildren;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class TestCrakenFunc extends TestCase {

	private Craken craken;
	private ReadSession rsession;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.craken = Craken.inmemoryCreateWithTest();
		this.rsession = craken.login("test");
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testFirst() throws Exception {
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/board/notice/1").property("title", "Notice!!").property("content", "This is notice").property("writer", new String[] {"airkjh", "bleujin"}) ;
				String parent = "/board/notice" ;
				
				
				
				
				
//				wsession.pathBy("/board/notice/1").unset("writer");
//				wsession.pathBy("/board/notice/1").clear();
				return null;
			}
		});
		
		
		ReadNode node = rsession.pathBy("/board/notice/1");
		node.debugPrint();
		
		String title = node.property("title").asString();
		String content = node.property("content").asString();
		Set<String> writers = node.property("writer").asSet();
		
		Debug.line(title, content, writers);
	}
	
	public void testBlob() throws Exception {
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				FileInputStream input = new FileInputStream("./resource/log4j.properties") ;
				wsession.pathBy("/blob_test").blob("pblob", input);
				input.close();
				return null;
			}
		});
		
		InputStream inputStream = rsession.pathBy("/blob_test").property("pblob").asBlob().toInputStream();
		Debug.line(IOUtil.toStringWithClose(inputStream));
	}
	
	public void testChildren() throws Exception {
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/parent/child1").property("name", "child1");
				wsession.pathBy("/parent/child2").property("name", "child2");
				wsession.pathBy("/parent/child3").property("name", "child3");
				return null;
			}
		});
		
		ReadChildren children = rsession.pathBy("/parent").children();

		for(ReadNode node : children) {
			node.debugPrint();
		}
	}
	
	public void testQuery() throws Exception {
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/parent/child1").property("name", "child1");
				wsession.pathBy("/parent/child2").property("name", "child2");
				wsession.pathBy("/parent/child3").property("name", "child3");
				return null;
			}
		});
		
		ChildQueryResponse response = rsession.pathBy("/parent").childQuery("name:child1", true).find() ;
		response.debugPrint();
	}
	
	public void testTransform() {
		rsession.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/parent/child1").property("name", "child1");
				wsession.pathBy("/parent/child2").property("name", "child2");
				wsession.pathBy("/parent/child3").property("name", "child3");
				
//				wsession.pathBy("/parent/child3").removeSelf();
				
				return null;
			}
		});
		
		List<Child> transform = rsession.pathBy("/parent").children().transform(new Function<Iterator<ReadNode>, List<Child>>() {
			@Override
			public List<Child> apply(Iterator<ReadNode> iter) {
				List<Child> children = Lists.newArrayList();
				while(iter.hasNext()) {
					Child c = new Child();
					c.name = iter.next().property("name").asString();
					children.add(c) ;
				}
				return null;
			}
		});
	}
}

class Child {
	String name ;
}

