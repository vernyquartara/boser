package it.quartara.boser.worker;

import static it.quartara.boser.AppConstants.SEARCH_REPO_PATH;
import static it.quartara.boser.AppConstants.SEARCH_REQUEST_ITEM_ID;
import static it.quartara.boser.AppConstants.SOLR_MAX_RESULTS;
import static it.quartara.boser.AppConstants.SOLR_URL;

import java.io.File;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.action.ActionException;
import it.quartara.boser.action.handlers.SearchResultPersisterHandler;
import it.quartara.boser.action.handlers.XlsResultWriterHandler;
import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.Parameter;
import it.quartara.boser.model.SearchItemRequest;
import it.quartara.boser.solr.SolrDocumentListWrapper;

@MessageDriven(name = "SearchItemWorker", activationConfig = {
	    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/SearchRequestQueue"),
	    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@TransactionManagement( TransactionManagementType.BEAN )
public class SearchItemWorker implements MessageListener {
	
	private final static Logger log = LoggerFactory.getLogger(SearchItemWorker.class);
	
	@PersistenceContext(name="BoserPU")
	private EntityManager em;
	
	@Resource
	private EJBContext context;
	
	@EJB
	private SearchResultPersisterHandler srph;

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(Message message) {
		Long searchRequestItemId = null;
		File searchRepo = null;
		int solrMaxResults = 0;
		String solrUrl = null;
		try {
			if (message instanceof MapMessage) {
				Map<String, Object> params = message.getBody(Map.class);
				searchRequestItemId = (Long) params.get(SEARCH_REQUEST_ITEM_ID);
				searchRepo = new File( (String) params.get(SEARCH_REPO_PATH));
				solrMaxResults = (Integer)params.get(SOLR_MAX_RESULTS);
				solrUrl = (String) params.get(SOLR_URL);
				log.info("Received searchRequestId from queue: {}", searchRequestItemId);
			} else {
				log.warn("Message of wrong type: " + message.getClass().getName());
			}
		} catch (JMSException e) {
			throw new EJBException("error reading message from queue", e);
		}
		/*
		 * avvio transazione gestita a mano,
		 * per impostare il timeout ad un valore elevato visto che la ricerca
		 * potrebbe impiegare molto tempo al variare del numero dei risultati
		 */
		Parameter timeoutParam = em.find(Parameter.class, "TRANSACTION_TIMEOUT");
		int timeout = Integer.valueOf(timeoutParam.getValue());
		UserTransaction tx = context.getUserTransaction();
		try {
			tx.setTransactionTimeout(timeout); //http://stackoverflow.com/questions/28096898/usertransaction-settransactiontimeout-not-working
			tx.begin();
		} catch (NotSupportedException | SystemException e) {
			throw new EJBException("impossibile avviare la transazione", e);
		}
		SearchItemRequest item = em.find(SearchItemRequest.class, searchRequestItemId);
		log.debug("handling search request: {}", item);
		
		/*
		 * creazione servizi
		 */
		XlsResultWriterHandler xrwh = new XlsResultWriterHandler(em, searchRepo);
		/*
		 * si carica la chiave e si effettua una ricerca su solr
		 * per motivi di buffering si suddividono le ricerche su solr in lotti da "solrMaxResults"
		 * utile specialmente in caso di prima ricerca quando lo storico è vuoto
		 */
		HttpSolrServer solr = new HttpSolrServer(solrUrl);
		String queryText = item.getSearchKey().getQuery();
		Date now = new Date();
		for (int i = 0; i < Integer.MAX_VALUE; i += solrMaxResults) {
			SolrQuery query = new SolrQuery();
			query.setFields("url", "title", "digest");
			query.setQuery(queryText);
			query.setStart(i);
			query.setRows(solrMaxResults);
			QueryResponse queryResponse = null;
			try {
				log.debug("esecuzione ricerca su Sorl, query: {}, start: {}", queryText, i);
				queryResponse = solr.query(query);
			} catch (SolrServerException e) {
				log.error("errore durante l'esecuzione della ricerca su Solr",e);
				item.setLastUpdate(now);
				item.setState(ExecutionState.ERROR);
				em.merge(item);
				try {
					tx.commit();
					return;
				} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
						| HeuristicRollbackException | SystemException e1) {
					throw new EJBException("impossibile effettuare commit della transazione", e1);
				}
			}
			SolrDocumentList docList = queryResponse.getResults();
			log.debug("Solr restituisce {} risultati", docList.size());
			if (docList.size()==0) {
				break;
			}
			SolrDocumentListWrapper docListWrapper = new SolrDocumentListWrapper(docList);
			if (i==0) docListWrapper.setAppend(Boolean.FALSE);
			try {
				log.debug("avvio persistenza risultati");
				srph.persistSearchResults(item.getSearchRequest().getSearch(), item.getSearchKey(), docListWrapper);
			} catch (ActionException e) {
				StringBuilder buffer = new StringBuilder();
				buffer.append("Si è verificato un errore durante la persistenza dei risultati ");
				buffer.append("per la chiave di ricerca "+queryText);
				log.error(buffer.toString(), e);
				/*
				 * si ritenta l'esecuzione
				 */
				throw new EJBException(e);
			}
		}
		/*
		 * output su file
		 */
		try {
			xrwh.handle(item.getSearchRequest().getSearch(), item.getSearchKey(), null);
		} catch (ActionException e) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("Si è verificato un errore durante la scrittura del file xls ");
			buffer.append("per la chiave di ricerca "+queryText);
			log.error(buffer.toString(), e);
			item.setLastUpdate(now);
			item.setState(ExecutionState.ERROR);
			try {
				tx.commit();
				return;
			} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
					| HeuristicRollbackException | SystemException e1) {
				throw new EJBException("impossibile effettuare commit della transazione", e1);
			}
		}
		/*
		 * tutto è andato bene
		 */
		item.setLastUpdate(now);
		item.setState(ExecutionState.COMPLETED);
		em.merge(item);
		try {
			tx.commit();
			return;
		} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
				| HeuristicRollbackException | SystemException e1) {
			throw new EJBException("impossibile effettuare commit della transazione", e1);
		}
	}
	
}
