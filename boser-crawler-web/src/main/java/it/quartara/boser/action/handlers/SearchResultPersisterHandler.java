package it.quartara.boser.action.handlers;

import static it.quartara.boser.model.IndexField.DIGEST;
import static it.quartara.boser.model.IndexField.TITLE;
import static it.quartara.boser.model.IndexField.URL;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.action.ActionException;
import it.quartara.boser.model.SOLRSearchResult;
import it.quartara.boser.model.Search;
import it.quartara.boser.model.SearchKey;
import it.quartara.boser.model.SearchResult;
import it.quartara.boser.model.SearchResultState;
import it.quartara.boser.solr.SolrDocumentListWrapper;

/**
 * Gestisce la persistenza dei risultati di ricerca.
 * @author webny
 *
 */
public class SearchResultPersisterHandler extends AbstractActionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(SearchResultPersisterHandler.class);
	
	public SearchResultPersisterHandler(EntityManager em, File searchRepo) {
		super(em, searchRepo);
	}

	@Override
	protected void execute(Search search, SearchKey key, SolrDocumentListWrapper documents) throws ActionException {
		//Map<SearchResult, SolrDocument> duplicatedMap = new HashMap<SearchResult, SolrDocument>();
		for (SolrDocument doc : documents.getList()) {
			String digest = (String) doc.getFieldValue(DIGEST.toString());
			String url = (String) doc.getFieldValue(URL.toString());
			String title = (String) doc.getFieldValue(TITLE.toString());
			if (title==null) {
				title = "";
			}
			
			SOLRSearchResult solrSearchResult = em.find(SOLRSearchResult.class, digest);
			if (solrSearchResult==null) {
				/*
				 * se nullo, è la prima volta che viene restituito questo risultato.
				 * va aggiunto, relativamente alla ricerca corrente.
				 */
				solrSearchResult = new SOLRSearchResult();
				solrSearchResult.setDigest(digest);
				solrSearchResult.setUrl(url);
				solrSearchResult.setTitle(title);
				
				SearchResult searchResult = new SearchResult();
				searchResult.setSolrSearchResult(solrSearchResult);
				searchResult.setKey(key);
				searchResult.setSearch(search);
				searchResult.setState(SearchResultState.INSERTED);
				
				Set<SearchResult> foundResults = new HashSet<>();
				foundResults.add(searchResult);
				solrSearchResult.setFoundResults(foundResults);
				
				em.persist(solrSearchResult);
			} else if (solrSearchResult.getFoundResults().contains(key)) {
				/*
				 * se esiste il risultato e la chiave corrente figura tra
				 * le chiavi associate al risultato stesso, è un duplicato
				 */
				log.debug("DUPLICATO: {}", solrSearchResult);
				//duplicatedMap.put(searchResult, doc);
				em.detach(solrSearchResult);
			} else {
				/*
				 * altrimenti il risultato va restituito e la chiave va aggiunta
				 * tra le chiavi associate al risultato, per evitare che lo stesso
				 * venga restituito in futuro
				 */
				if (solrSearchResult.getFoundResults()==null) {
					solrSearchResult.setFoundResults(new HashSet<SearchResult>());
				}
				SearchResult searchResult = new SearchResult();
				searchResult.setSolrSearchResult(solrSearchResult);
				searchResult.setKey(key);
				searchResult.setSearch(search);
				searchResult.setState(SearchResultState.INSERTED);
				
				solrSearchResult.getFoundResults().add(searchResult);
				em.merge(solrSearchResult);
			}
		}
		em.flush();
		
		/*
		 * gestione dei duplicati da rivedere
		 * ogni volta scrive 100 risultati ma deve APPENDERE realmente non ricominciare ogni 100
		 * 
		if (!duplicatedMap.isEmpty()) {
			log.debug("rimozione duplicati e creazione file dei duplicati");
			documents.getList().removeAll(duplicatedMap.values());
			File duplicatedFile = new File(searchRepo.getAbsolutePath()+File.separator
											+"DUP-"+getSearchResultFileNameSubstringByKey(key)
											+"-K"+key.getId()
											+".txt");
			try {
				PrintWriter writer = new PrintWriter(new FileOutputStream(duplicatedFile, true));
				writer.println(FILE_HEADER);
				writer.println(FILE_TITLE);
				writer.println(duplicatedMap.size()+" duplicati per "+key.getQuery()+"\r\n");
				int i = 1;
				for (Entry<SearchResult, SolrDocument> entry : duplicatedMap.entrySet()) {
					SearchResult searchResult = entry.getKey();
					SolrDocument doc = entry.getValue();
					writer.append((i++ +")"+doc.getFieldValue(URL.toString())+"\r\n"));
					writer.append(doc.getFieldValue(TITLE.toString())+"\r\n");
					StringBuilder buffer = new StringBuilder();
					buffer.append("Archiviato il ");
					buffer.append(DateFormatUtils.format(searchResult.getSearch().getTimestamp(), "dd/MM/yy"));
					buffer.append(", configurazione di ricerca: ");
					buffer.append(searchResult.getSearch().getConfig().getDescription());
					buffer.append(", crawler: ");
					buffer.append(searchResult.getSearch().getConfig().getCrawler().getDescription());
					writer.append(buffer.toString()+"\r\n\r\n");
				}
				writer.close();
				log.info("wrote file: {}", duplicatedFile.getAbsolutePath());
			} catch (IOException e) {
				String msg = "problema di scrittura file dei duplicati";
				throw new ActionException(msg, e);
			}
		}
		*/
	}
	
}
