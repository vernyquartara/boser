package it.quartara.boser.service;

import static it.quartara.boser.listener.EntityManagerListener.getEntityManager;
import it.quartara.boser.model.SearchConfig;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/searchConfig")
public class SearchConfigService {

	private static final Logger log = LoggerFactory.getLogger(SearchConfigService.class);

	@GET
	@Path("/crawler/{id}")
	public Response getByCrawler(@PathParam("id") Long id) {
		EntityManager em = getEntityManager();
		List<SearchConfig> elements = 
				em.createQuery("from SearchConfig where crawler.id = ?", SearchConfig.class).setParameter(1, id).getResultList();
		return Response.status(200).entity(elements).build();
	}

}
