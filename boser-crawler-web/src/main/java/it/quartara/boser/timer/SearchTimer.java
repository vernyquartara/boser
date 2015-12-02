package it.quartara.boser.timer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Timer;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.model.ExecutionState;
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

	@Schedule(second="*/10", minute="*", hour="*", dayOfWeek="*", dayOfMonth="*", month="*", year="*", info="MyTimer", persistent=false)
    private void scheduledTimeout(final Timer t) {
		log.debug("Controllo search da avviare");
		/*
		 * legge la tabella delle request.
		 * se c'è almeno una request 'STARTED' termina.
		 * se trova una request da avviare:
		 * - ne cambia lo stato
		 * - mette un messaggio in coda passando l'id
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
		Map<String, Object> params = new HashMap<>();
		params.put("searchRequestId", requestToStart.getId());
		context.createProducer().send(queue, params);
		requestToStart.setState(ExecutionState.STARTED);
		em.merge(requestToStart);
    }
}