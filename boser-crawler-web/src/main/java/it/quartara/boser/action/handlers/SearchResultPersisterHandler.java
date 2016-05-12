package it.quartara.boser.action.handlers;

import static it.quartara.boser.model.IndexField.DIGEST;
import static it.quartara.boser.model.IndexField.TITLE;
import static it.quartara.boser.model.IndexField.URL;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import javax.transaction.Transactional.TxType;

import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.action.ActionException;
import it.quartara.boser.model.SOLRSearchResult;
import it.quartara.boser.model.Search;
import it.quartara.boser.model.SearchKey;
import it.quartara.boser.model.SearchResult;
import it.quartara.boser.model.SearchResultPK;
import it.quartara.boser.model.SearchResultState;
import it.quartara.boser.solr.SolrDocumentListWrapper;

/**
 * Gestisce la persistenza dei risultati di ricerca.
 * @author webny
 *
 */
@Stateless
public class SearchResultPersisterHandler {
	
	private static final Logger log = LoggerFactory.getLogger(SearchResultPersisterHandler.class);
	
	@PersistenceContext(name="BoserPU")
	private EntityManager em;
	
	@Resource
	private EJBContext context;

	
	public void persistSearchResults(Search search, SearchKey key, SolrDocumentListWrapper documents) throws ActionException {
		for (SolrDocument doc : documents.getList()) {
			String digest = (String) doc.getFieldValue(DIGEST.toString());
			String url = (String) doc.getFieldValue(URL.toString());
			String title = (String) doc.getFieldValue(TITLE.toString());
			if (title==null) {
				title = "";
			}
			
			SearchResultPK srpk = new SearchResultPK(key.getId(), digest);
			SearchResult searchResult = em.find(SearchResult.class, srpk);
			if (searchResult != null) {
				/*
				 * se esiste è un duplicato, stop.
				 */
				log.debug("DUPLICATO: {}", searchResult);
				em.detach(searchResult);
			} else {
				/*
				 * controllo prima se esiste il SOLRSearchResult,
				 * se non esiste lo inserisco ma devo lockare per evitare
				 * che un altro thread faccia la stessa cosa.
				 * Il lock va rilasciato subito dopo l'inserimento.
				 * 
				 * il problema è che il lock viene rilasciato solo al commit,
				 * questo vuol dire che deve essere aperta una nuova transazione
				 * specificamente per l'inserimento del SOLRSearchResult.
				 * Per questo si può usare un metodo a parte ma la classe deve
				 * essere trasformata in un EJB.
				 */
				SOLRSearchResult solrSearchResult = insertSOLRSearchResultIfNotExists(digest, url, title);
				
				SearchResult newSearchResult = new SearchResult();
				newSearchResult.setSolrSearchResult(solrSearchResult);
				newSearchResult.setKey(key);
				newSearchResult.setSearch(search);
				newSearchResult.setState(SearchResultState.INSERTED);
				
				em.persist(newSearchResult);
			}
		}
		//em.flush();
	}

	@Transactional(TxType.REQUIRES_NEW)
	public SOLRSearchResult insertSOLRSearchResultIfNotExists(String digest, String url, String title) {
		SOLRSearchResult solrSearchResult = em.find(SOLRSearchResult.class, digest, LockModeType.PESSIMISTIC_WRITE);
		if (solrSearchResult == null) {
			solrSearchResult = new SOLRSearchResult();
			solrSearchResult.setDigest(digest);
			solrSearchResult.setUrl(url);
			solrSearchResult.setTitle(title);
			em.persist(solrSearchResult);
		}
		return solrSearchResult;
	}
	
	
	
	/*
	 * 
	
	SearchResultPK srpk = new SearchResultPK(key.getId(), digest);
			SearchResult searchResult = em.find(SearchResult.class, srpk);
			if (searchResult != null) {
				/*
				 * se esiste è un duplicato, stop.
				 *
				log.debug("DUPLICATO: {}", searchResult);
				em.detach(searchResult);
			} else {
				/*
				 * se non esiste va inserito, lasciando gestire il cascade
				 * di inserimento a jpa.
				 *
				SOLRSearchResult solrSearchResult = new SOLRSearchResult();
				solrSearchResult.setDigest(digest);
				solrSearchResult.setUrl(url);
				solrSearchResult.setTitle(title);
				
				SearchResult newSearchResult = new SearchResult();
				newSearchResult.setSolrSearchResult(solrSearchResult);
				newSearchResult.setKey(key);
				newSearchResult.setSearch(search);
				newSearchResult.setState(SearchResultState.INSERTED);
				
				em.persist(newSearchResult);
			}
	
	
	 * 
	 * 
	 */
	
}
