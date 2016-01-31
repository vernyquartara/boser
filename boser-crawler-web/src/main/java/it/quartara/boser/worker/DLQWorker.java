package it.quartara.boser.worker;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.SearchRequest;

/**
 * Si occupa della gestione dei messaggi finiti nella dead letter queue.
 * In particolare imposta a ERROR lo stato della richiesta asincrona.
 * 
 * @author webny
 *
 */
@MessageDriven(name = "DLQWorker", activationConfig = {
	    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/DLQ"),
	    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@TransactionManagement( TransactionManagementType.CONTAINER )
public class DLQWorker implements MessageListener {
	
	private final static Logger log = LoggerFactory.getLogger(DLQWorker.class);
	
	@PersistenceContext(name="BoserPU")
	private EntityManager em;

	@Override
	public void onMessage(Message message) {
		log.debug("message {}", message.toString());
		try {
			if (message instanceof MapMessage) {
				if (((MapMessage)message).getObject("searchRequestId") instanceof Long) {
					Long searchRequestId = ((MapMessage)message).getLong("searchRequestId");
					SearchRequest request = em.find(SearchRequest.class, searchRequestId);
					if (request != null) {
						request.setState(ExecutionState.ERROR);
						em.merge(request);
					} else {
						log.warn("request null per searchRequestId {}", searchRequestId);
					}
				}
			}
		} catch (JMSException e) {
			log.error("errore di gestione dead letter queue", e);
		}
	}

}
