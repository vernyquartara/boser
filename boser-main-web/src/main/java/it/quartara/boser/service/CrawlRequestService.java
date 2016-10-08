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

import it.quartara.boser.model.CrawlRequest;
import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.IndexConfig;

@Stateless
@Path("/crawlRequest")
public class CrawlRequestService {
	
	private static final Logger log = LoggerFactory.getLogger(CrawlRequestService.class);
	
	@PersistenceContext(unitName = "BoserPU")
	EntityManager em;

	@GET
	public List<CrawlRequest> getAll() {
		List<CrawlRequest> elements = 
				em.createQuery("from CrawlRequest", CrawlRequest.class).getResultList();
		return elements;
	}
	
	/**
	 * Inserisce una nuova richiesta di indicizzazione.
	 * Accetta MediaType.APPLICATION_FORM_URLENCODED poiché i parametri
	 * sono relativi a diversi oggetti del modello (se fosse stato un unico
	 * oggetto sarebbe stato possibile accettare come parametro il tipo
	 * dell'oggetto stesso e ricevere JSON)
	 * @param indexConfigId
	 * @param depth
	 * @param topN
	 * @return 
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void insert(@FormParam("indexConfigId") Long indexConfigId,
					   @FormParam("depth") Integer depth,
					   @FormParam("topN") Integer topN) {
		log.info("insert new CrawlRequest for IndexConfig: {}", indexConfigId);
		IndexConfig indexConfig = em.find(IndexConfig.class, indexConfigId);
		indexConfig.setDepth(depth.shortValue());
		indexConfig.setTopN(topN);
		em.merge(indexConfig);
		CrawlRequest crawlRequest = new CrawlRequest();
		crawlRequest.setIndexConfig(indexConfig);
		Date now = new Date();
		crawlRequest.setCreationDate(now);
		crawlRequest.setLastUpdate(now);
		crawlRequest.setState(ExecutionState.READY);
		em.persist(crawlRequest);
	}
	
}
