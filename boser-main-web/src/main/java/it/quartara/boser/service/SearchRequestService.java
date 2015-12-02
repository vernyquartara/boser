package it.quartara.boser.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.SearchConfig;
import it.quartara.boser.model.SearchRequest;

@Stateless
@Path("/searchRequest")
public class SearchRequestService {
	
	private static final Logger log = LoggerFactory.getLogger(SearchRequestService.class);
	
	@PersistenceContext(unitName = "BoserPU")
	EntityManager em;

	@GET
	public List<SearchRequest> getAll() {
		List<SearchRequest> elements = 
				em.createQuery("from SearchRequest", SearchRequest.class).getResultList();
		return elements;
	}
	
	/**
	 * Inserisce una nuova richiesta di ricerca.
	 * Accetta MediaType.APPLICATION_FORM_URLENCODED poiché i parametri
	 * sono relativi a diversi oggetti del modello (se fosse stato un unico
	 * oggetto sarebbe stato possibile accettare come parametro il tipo
	 * dell'oggetto stesso e ricevere JSON)
	 * @param searchConfigId
	 * @return 
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void insert(@FormParam("searchConfigId") Long searchConfigId) {
		log.info("insert new SearchRequest for SearchConfig: {}", searchConfigId);
		Date now = new Date();
		SearchConfig searchConfig = em.find(SearchConfig.class, searchConfigId);
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setSearchConfig(searchConfig);
		searchRequest.setCreationDate(now);
		searchRequest.setLastUpdate(now);
		searchRequest.setState(ExecutionState.READY);
		em.persist(searchRequest);
	}
	
}
