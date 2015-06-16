package net.ion.board;

import java.util.ArrayList;
import java.util.List;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;


class Board {
	
	ReadSession rsession ; 
	BoardBean bean ;
	
	public Board bean(BoardBean bean) {
		this.bean = bean;
		return this;
	}
	
	public BoardBean getBean(){
		return this.bean;
	}

	public List<BoardBean> list(int i, int j) throws Exception {
		List<BoardBean> result = new ArrayList<BoardBean>();
		List<ReadNode> list = rsession.pathBy("/board").childQuery("").sort("seq").where("(id > "+ i +" && id <= " + j+")").find().toList();
		rsession.pathBy("/board").childQuery("").sort("seq=desc").find().debugPrint();
		for(ReadNode rNode : list ){
			result.add(new BoardBean().seq(rNode.property("seq").asInt()).title(rNode.property("title").toString()).content(rNode.property("content").toString()));
		}
		return result;
	}

	Board(ReadSession rsession){
		this.rsession = rsession;
		bean = new BoardBean();
	}
	
	public boolean removeSelf() {
		rsession.tran(new TransactionJob<Boolean>() {
			@Override
			public Boolean handle(WriteSession wsession) throws Exception {
				wsession.pathBy("/board/" + bean.seq).removeSelf();
				return true;
			}
		});
		return true;
	}

	public Board newArticle(BoardBean bean) {
		this.bean = bean; 
		
		return this;
	}
	
	private boolean exists(int seq) {
		ReadNode rNode = rsession.ghostBy("/board/" + seq);
		return !rNode.isGhost() ;
	}
	
	public boolean update() {
		if(!exists(bean.seq)) {
			throw new IllegalArgumentException();
		}
		
		return put();
	}

	public boolean insert(){
		return put();
	}
	
	private boolean put() {
		rsession.tran(new TransactionJob<Boolean>() {
			@Override
			public Boolean handle(WriteSession wsession) throws Exception {
				int seq = wsession.pathBy("/board").increase("seq").asInt();
				wsession.pathBy("/board/" + seq).property("title",bean.title).property("content", bean.content).property("seq" , seq);
				return true;
			}
		});
		return true;
	}
	
	public Board get(int seq) {
		if(!exists(seq)) {
			throw new IllegalArgumentException();
		}

		ReadNode rNode = rsession.pathBy("/board/");
		bean = new BoardBean(rNode.property("seq").asInt(), rNode.property("title").asString() , rNode.property("content").asString());
		return this;
	}
	
	
}