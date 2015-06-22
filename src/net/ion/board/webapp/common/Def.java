package net.ion.board.webapp.common;


public class Def {
	
	public static class Loader {
		public static final String Time = "time" ;
		public static final String Status = "status" ;
		public static final String Created = "created";
		public static final String Content = "content" ;
	}

	
	public static class User {
		public static final String Name = "name" ;
		public static final String Password = "password" ;
	}
	
	public static class Script {
		public static final String Sid = "sid" ;
		public static final String Content = "content" ;
		public static final String Running = "running" ;
	}
	

	public static class Schedule {
		public static final String Sid = "sid" ;
		public static final String MINUTE = "minute" ;
		public static final String HOUR = "hour" ;
		public static final String DAY = "day" ;
		public static final String MONTH = "month" ;
		public static final String WEEK = "week" ;
		public static final String MATCHTIME = "matchtime" ;
		public static final String YEAR = "year" ;
		
		public static final String ENABLE = "enable" ;
		public static final String Parity = "parity";
	}
	
	public static class SLog {
		public static final String CIndex = "cindex" ;
		public static final String Sid = "sid" ;
		public static final String Runtime = "runtime" ;
		public static final String Status = "status" ;
		public static final String Success = "success";
		public static final String Fail = "fail";
		public static final String Result = "result";
		
		public static String path(String sid){
			return "/scripts/" + sid + "/slogs" ;
		}
	}
}
