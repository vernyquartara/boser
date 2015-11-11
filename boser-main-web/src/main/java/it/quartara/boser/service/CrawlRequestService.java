package it.quartara.boser.service;

import java.util.Date;

import it.quartara.boser.model.CrawlRequest;
import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.IndexConfig;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Path("/crawlRequest")
public class CrawlRequestService {
	
	private static final Logger log = LoggerFactory.getLogger(CrawlRequestService.class);
	
	@PersistenceContext(unitName = "BoserPU")
	EntityManager em;

	/**
	 * Inserisce una nuova richiesta di indicizzazione.
	 * Accetta MediaType.APPLICATION_FORM_URLENCODED poiché i parametri
	 * sono relativi a diversi oggetti del modello (se fosse stato un unico
	 * oggetto sarebbe stato possibile accettare come parametro il tipo
	 * dell'oggetto stesso e ricevere JSON)
	 * @param indexConfigId
	 * @param depth
	 * @param topN
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void insert(@FormParam("indexConfigId") Long indexConfigId,
					   @FormParam("depth") Integer depth,
					   @FormParam("topN") Integer topN) {
		log.info("insert new CrawlRequest for IndexConfig: {}", indexConfigId);
		IndexConfig indexConfig = em.find(IndexConfig.class, indexConfigId);
		CrawlRequest crawlRequest = new CrawlRequest();
		crawlRequest.setIndexConfig(indexConfig);
		Date now = new Date();
		crawlRequest.setCreationDate(now);
		crawlRequest.setLastUpdate(now);
		crawlRequest.setState(ExecutionState.READY);
		//em.persist(crawlRequest);
	}
}
