package it.quartara.boser.timer;

import static it.quartara.boser.AppConstants.SEARCH_REPO_PATH;
import static it.quartara.boser.AppConstants.SEARCH_REQUEST_ITEM_ID;
import static it.quartara.boser.AppConstants.SOLR_MAX_RESULTS;
import static it.quartara.boser.AppConstants.SOLR_URL;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Timer;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.Parameter;
import it.quartara.boser.model.Search;
import it.quartara.boser.model.SearchConfig;
import it.quartara.boser.model.SearchItemRequest;
import it.quartara.boser.model.SearchKey;
import it.quartara.boser.model.SearchRequest;

@JMSDestinationDefinitions(
	    value = {
	        @JMSDestinationDefinition(
	            name = "java:/queue/SearchRequestQueue",
	            interfaceName = "javax.jms.Queue",
	            destinationName = "SearchRequestQueue"
	        )
	    })
@Singleton
public class SearchTimer {
	
	private static final Logger log = LoggerFactory.getLogger(SearchTimer.class);
	
	@PersistenceContext(name="BoserPU")
	private EntityManager em;
	
	@Inject
    private JMSContext context;

    @Resource(lookup = "java:/queue/SearchRequestQueue")
    private Queue queue;
    
    @EJB
    private SearchRequestTimer searchRequestTimer;
    
	@Schedule(second="*/10", minute="*", hour="*", dayOfWeek="*", dayOfMonth="*", month="*", year="*", info="MyTimer", persistent=false)
    private void scheduledTimeout(final Timer t) {
		log.debug("Controllo search da avviare");
		/*
		 * legge la tabella delle request.
		 * se c'è almeno una request 'STARTED' termina.
		 * se trova una request da avviare:
		 * - ne cambia lo stato
		 * - mette un messaggio in coda per ogni chiave da cercare
		 * - avvia un timer per il merge delle ricerche delle singole chiavi
		 */
		try {
			SearchRequest started = 
					em.createQuery("from SearchRequest where state = 'STARTED'", SearchRequest.class).getSingleResult();
			if (started!=null) {
				log.info("è già presente una ricerca in corso: {}", started.toString());
				return;
			}
		} catch (NonUniqueResultException e) {
			log.error("trovata più di una richiesta in stato STARTED", e);
			em.getTransaction().setRollbackOnly();
			return;
		} catch (NoResultException e) {
			log.debug("nessuna richiesta in stato STARTED");
		}
		
		List<SearchRequest> elements = 
				em.createQuery("from SearchRequest where state = 'READY' order by creationDate", SearchRequest.class).getResultList();
		if (elements==null || elements.isEmpty()) {
			log.info("nessuna richiesta di search da eseguire.");
			return;
		}
		
		SearchRequest requestToStart = elements.get(0);
		
		/*
		 * creazione nuova ricerca e relativa cartella su fs per i risultati
		 */
		Date now = new Date();
		SearchConfig searchConfig = requestToStart.getSearchConfig();
		Search search = new Search();
		search.setConfig(searchConfig);
		search.setTimestamp(now);
		em.persist(search);
		em.flush();
		Parameter param = em.find(Parameter.class, "SEARCH_REPO");
		String repo = param.getValue();
		String searchPath = repo+File.separator+searchConfig.getId()+File.separator+search.getId();
		File searchRepo = new File(searchPath);
		if (searchRepo.mkdirs()) {
			log.debug("creata cartella: {}", searchRepo.getAbsolutePath());
		}
		/*
		 * recupero parametri per i workers
		 */
		Parameter solrMaxResultsParam = em.find(Parameter.class, "SOLR_QUERY_MAX_RESULT_SIZE");
		int solrMaxResults = Integer.valueOf(solrMaxResultsParam.getValue());
		Parameter solrUrlParam = em.find(Parameter.class, "SOLR_URL");
		
		JMSProducer jmsProducer = context.createProducer();
		for (SearchKey key : requestToStart.getSearchConfig().getKeys()) {
			SearchItemRequest searchItemRequest = new SearchItemRequest();
			searchItemRequest.setCreationDate(requestToStart.getCreationDate());
			searchItemRequest.setLastUpdate(now);
			searchItemRequest.setSearchKey(key);
			searchItemRequest.setSearchRequest(requestToStart);
			searchItemRequest.setState(ExecutionState.READY);
			em.persist(searchItemRequest);
			Map<String, Object> params = new HashMap<>();
			params.put(SEARCH_REQUEST_ITEM_ID, searchItemRequest.getId());
			params.put(SEARCH_REPO_PATH, searchRepo.getAbsolutePath());
			params.put(SOLR_MAX_RESULTS, new Integer(solrMaxResults));
			params.put(SOLR_URL, solrUrlParam.getValue());
			jmsProducer.send(queue, params);
		}
		
		param = em.find(Parameter.class, "SEARCH_TIMER_INITIAL_DURATION");
		Long initialDuration = Long.valueOf(param.getValue());
		param = em.find(Parameter.class, "SEARCH_TIMER_INTERVAL_DURATION");
		Long intervalDuration = Long.valueOf(param.getValue());
		searchRequestTimer.startTimer(requestToStart.getId(), initialDuration, intervalDuration);
		
		requestToStart.setSearch(search);
		requestToStart.setState(ExecutionState.STARTED);
		em.merge(requestToStart);
    }
}