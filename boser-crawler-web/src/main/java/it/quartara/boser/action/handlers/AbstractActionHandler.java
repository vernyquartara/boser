package it.quartara.boser.action.handlers;

import java.io.File;

import javax.persistence.EntityManager;

import org.apache.solr.common.SolrDocumentList;

import it.quartara.boser.action.ActionException;
import it.quartara.boser.model.Search;
import it.quartara.boser.model.SearchKey;
import it.quartara.boser.solr.SolrDocumentListWrapper;

abstract public class AbstractActionHandler implements ActionHandler {
	
	protected static final String FILE_HEADER = "BOSER - Boring Search Engine\r\nRealizzato da Verny Quartara per CP Informatica\r\nwww.boring.it\r\nanno 2008\r\n\r\n";
	
	private ActionHandler nextHandler;
	
	protected EntityManager em;
	protected File searchRepo;

	public AbstractActionHandler(ActionHandler nextHandler, EntityManager em, File searchRepo) {
		super();
		this.nextHandler = nextHandler;
		this.em = em;
		this.searchRepo = searchRepo;
	}

	public AbstractActionHandler(EntityManager em, File searchRepo) {
		super();
		this.em = em;
		this.searchRepo = searchRepo;
	}
	
	@Override
	public void handle(Search search, SearchKey key, SolrDocumentListWrapper documents) throws ActionException {
		this.execute(search, key, documents);
		if (nextHandler != null) {
			nextHandler.handle(search, key, documents);
		}
	}
	
	abstract protected void execute(Search search, SearchKey key, SolrDocumentListWrapper documents) throws ActionException;

	@Override
	public void setNextHandler(ActionHandler nextHandler) {
		this.nextHandler = nextHandler;
	}
	
	protected String getSearchResultFileNameSubstringByKey(SearchKey key) {
		String result = key.getTerms().iterator().next();
		if (key.getTerms().size() > 1) {
			return result+"_etc";
		}
		return result;
	}
	
}
