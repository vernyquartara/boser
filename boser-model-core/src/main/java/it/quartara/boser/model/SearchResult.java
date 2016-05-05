package it.quartara.boser.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import it.quartara.boser.model.converter.SearchResultStateConverter;

@Entity
@Table(name="SEARCH_RESULTS")
@IdClass(SearchResultPK.class)
public class SearchResult implements Serializable {
	
	/** */
	private static final long serialVersionUID = 2269885353366595747L;

	@Id
	@ManyToOne
	private SOLRSearchResult solrSearchResult;
	
	@Id
	@ManyToOne
	private SearchKey key;
	
	@Column(columnDefinition = "CHAR(1)")
	@Convert(converter = SearchResultStateConverter.class)
	private SearchResultState state;
	
	@ManyToOne
	private Search search;


	public SOLRSearchResult getSolrSearchResult() {
		return solrSearchResult;
	}

	public void setSolrSearchResult(SOLRSearchResult solrSearchResult) {
		this.solrSearchResult = solrSearchResult;
	}

	public SearchResultState getState() {
		return state;
	}

	public void setState(SearchResultState state) {
		this.state = state;
	}

	public Search getSearch() {
		return search;
	}

	public void setSearch(Search search) {
		this.search = search;
	}

	public SearchKey getKey() {
		return key;
	}

	public void setKey(SearchKey key) {
		this.key = key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((solrSearchResult == null) ? 0 : solrSearchResult.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SearchResult other = (SearchResult) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		if (solrSearchResult == null) {
			if (other.solrSearchResult != null) {
				return false;
			}
		} else if (!solrSearchResult.equals(other.solrSearchResult)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SearchResult [solrSearchResult=" + solrSearchResult + ", key=" + key + "]";
	}

}
