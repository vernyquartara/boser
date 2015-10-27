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

import it.quartara.boser.model.CrawlRequest;

@JMSDestinationDefinitions(
	    value = {
	        @JMSDestinationDefinition(
	            name = "java:/queue/CrawlerRequestQueue",
	            interfaceName = "javax.jms.Queue",
	            destinationName = "CrawlerRequestQueue"
	        )
	    })
@Singleton
public class CrawlerTimer {
	
	private static final Logger log = LoggerFactory.getLogger(CrawlerTimer.class);
	
	@PersistenceContext(name="BoserPU")
	private EntityManager em;
	
	@Inject
    private JMSContext context;

    @Resource(lookup = "java:/queue/CrawlerRequestQueue")
    private Queue queue;

	@Schedule(second="*/30", minute="*", hour="*", dayOfWeek="*", dayOfMonth="*", month="*", year="*", info="MyTimer", persistent=false)
    private void scheduledTimeout(final Timer t) {
		log.debug("Controllo crawl da avviare");
		/*
		 * legge la tabella delle request.
		 * se c'è almeno una request 'STARTED' termina.
		 * se trova una request da avviare:
		 * - ne cambia lo stato
		 * - mette un messaggio in coda passando l'id
		 */
		try {
			CrawlRequest started = 
					em.createQuery("from CrawlRequest where state = 'STARTED'", CrawlRequest.class).getSingleResult();
			if (started!=null) {
				log.info("è già presente un crawl in corso: {}", started.toString());
				return;
			}
		} catch (NonUniqueResultException e) {
			log.error("trovata più di una richiesta in stato STARTED", e);
			em.getTransaction().setRollbackOnly();
			return;
		} catch (NoResultException e) {
			log.debug("nessuna richiesta in stato STARTED");
		}
		
		List<CrawlRequest> elements = 
				em.createQuery("from CrawlRequest where state = 'READY' order by creationDate", CrawlRequest.class).getResultList();
		if (elements==null || elements.isEmpty()) {
			log.info("nessuna richiesta di crawl da eseguire.");
			return;
		}
		Map<String, Object> params = new HashMap<>();
		params.put("crawlRequestId", elements.get(0).getId());
		context.createProducer().send(queue, params);
    }
}