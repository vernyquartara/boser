package it.quartara.boser.model;

/**
 * Indicates the state of a search result.
 * 
 * @author webny
 *
 */
public enum SearchResultState {
	
	/**
	 * red from solr and inserted into history table (search_results)
	 */
	INSERTED,
	/**
	 * retrieved from history table to be written to xls
	 */
	RETRIEVED;

}
