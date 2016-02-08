package it.quartara.boser.action.handlers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;

import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.action.ActionException;
import it.quartara.boser.model.IndexField;
import it.quartara.boser.model.Search;
import it.quartara.boser.model.SearchKey;
import it.quartara.boser.solr.SolrDocumentListWrapper;

/**
 * Scrive su file i risultati di ricerca.
 * Crea un file con il nome della chiave.
 * Il percorso del file viene letto da un Parameter con chiave SEARCH_REPO.
 * 
 * @author webny
 *
 */
public class TxtResultWriterHandler extends AbstractActionHandler {
	
	private static final String TITLE = "RISULTATI DELLA RICERCA\r\n";
	
	private static final Logger log = LoggerFactory.getLogger(TxtResultWriterHandler.class);

	public TxtResultWriterHandler(EntityManager em, File searchRepo) {
		super(em, searchRepo);
	}

	@Override
	protected void execute(Search search, SearchKey key, SolrDocumentListWrapper documents) throws ActionException {
		File outputFile = new File(searchRepo.getAbsolutePath()+File.separator
									+"RES-"+getSearchResultFileNameSubstringByKey(key)
									+"-K"+key.getId()
									+".txt");
		try {
			PrintWriter writer = new PrintWriter(outputFile);
			writer.println(FILE_HEADER);
			writer.println(TITLE);
			writer.println(documents.getList().size()+" risultati per "+key.getQuery()+"\r\n");
			for (int i = 0; i < documents.getList().size(); i++) {
				SolrDocument doc = documents.getList().get(i);
				writer.println(i+1+")"+doc.getFieldValue(IndexField.URL.toString()));
				writer.println(doc.getFieldValue(IndexField.TITLE.toString())+"\r\n");
			}
			writer.close();
			log.info("wrote file: {}", outputFile.getAbsolutePath());
		} catch (IOException e) {
			String msg = "problema di scrittura file dei risultati";
			throw new ActionException(msg, e);
		}
	}

}
