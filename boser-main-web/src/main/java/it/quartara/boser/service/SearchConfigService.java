package it.quartara.boser.service;

import it.quartara.boser.model.SearchConfig;
import it.quartara.boser.model.SearchKey;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/searchConfig")
@Stateless
public class SearchConfigService {

	private static final Logger log = LoggerFactory.getLogger(SearchConfigService.class);
	
	@PersistenceContext(unitName = "BoserPU")
	EntityManager em;

	@GET
	@Path("/crawler/{id}")
	public Response getByCrawler(@PathParam("id") Long id) {
		List<SearchConfig> elements = 
				em.createQuery("from SearchConfig where crawler.id = ?", SearchConfig.class).setParameter(1, id).getResultList();
		return Response.status(200).entity(elements).build();
	}

	@GET
	@Path("/{id}")
	public SearchConfig getById(@PathParam("id") Long searchConfigId) {
		SearchConfig searchConfig = em.find(SearchConfig.class, searchConfigId);
		return searchConfig;
	}
}
