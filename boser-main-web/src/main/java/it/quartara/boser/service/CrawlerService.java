package it.quartara.boser.service;

import static it.quartara.boser.listener.EntityManagerListener.getEntityManager;
import it.quartara.boser.model.Crawler;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/crawler")
public class CrawlerService {

	private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);

	@GET
	@Produces("application/json")
	public Response getList() {
		EntityManager em = getEntityManager();
		List<Crawler> elements = em.createQuery("from Crawler", Crawler.class).getResultList();
		return Response.status(200).entity(elements).build();
	}
	
	@GET
	@Path("/{id}")
	public Response getElement(@PathParam("id") Long id) {
		EntityManager em = getEntityManager();
		Crawler crawler = em.find(Crawler.class, id);
		return Response.status(200).entity(crawler).build();
	}

}
