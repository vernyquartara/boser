package it.quartara.boser.solr;

import org.apache.solr.common.SolrDocumentList;

/**
 * SolrDocumentList wrapper, useful to implement
 * "append" feature to search result report XLS document.
 */
public class SolrDocumentListWrapper {

	/**
	 * 
	 */
	public static final long serialVersionUID = -6615916573415687745L;
	
	private boolean append = Boolean.TRUE;
	private SolrDocumentList list;
	
	public SolrDocumentListWrapper(){
	}
	
	public SolrDocumentListWrapper(SolrDocumentList list) {
		this.list = list;
	}

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

	public SolrDocumentList getList() {
		return list;
	}

	public void setList(SolrDocumentList list) {
		this.list = list;
	}

}
