package it.quartara.boser.service;

import it.quartara.boser.model.Crawler;
import it.quartara.boser.model.Index;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/index")
@Stateless
public class IndexService {

	private static final Logger log = LoggerFactory.getLogger(IndexService.class);
	
	@PersistenceContext(unitName = "BoserPU")
	EntityManager em;

	@GET
	@Produces("application/json")
	public Response getList() {
		List<Index> elements = em.createQuery("from Index where config.crawler.id = 1", Index.class).getResultList();
		return Response.status(200).entity(elements).build();
	}

}
