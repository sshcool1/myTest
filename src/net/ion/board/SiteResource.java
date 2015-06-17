package net.ion.board;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

@Path("")
public class SiteResource {
	
	private File siteHome = new File("./resource/admin") ;
	
	@GET
	@Path("/{remain : .*}")
	public File viewResource(@PathParam("remain") String remain){
		File file = new File(siteHome, remain) ;
		
		if (! file.exists()) throw new WebApplicationException(404) ;
		
		return file ;
	}
}
