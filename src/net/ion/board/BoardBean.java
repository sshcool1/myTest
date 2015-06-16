package net.ion.board;

public class BoardBean {
	
	int seq ;
	String title ;
	String content ;
	
	BoardBean(){
		
	}

	public BoardBean seq(int seq) {
		this.seq = seq;
		return this;
	}
	BoardBean(String title , String content){
		this.title = title;
		this.content = content;
	}
	BoardBean(int seq , String title , String content){
		this.seq = seq;
		this.title = title;
		this.content = content;
	}

	public BoardBean title(String title) {
		this.title = title;
		return this;
	}
	
	public String title() {
		return title;
	}
	
	public BoardBean content(String content){
		this.content = content;
		return this;
	}
	
	public String content(){
		return content;
	}

	@Override
	public String toString() {
		return "BoardBean [seq=" + seq + ", title=" + title + ", content="
				+ content + "]";
	}
	
	
}