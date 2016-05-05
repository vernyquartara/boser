package it.quartara.boser.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class SearchResultPK implements Serializable {
	
	/** */
	private static final long serialVersionUID = -1691492284034411L;
	
	private Long key;
	private String solrSearchResult;
	
	public SearchResultPK() {
		super();
	}
	
	public SearchResultPK(Long key, String solrSearchResult) {
		super();
		this.key = key;
		this.solrSearchResult = solrSearchResult;
	}
	
	public Long getKey() {
		return key;
	}
	public void setKey(Long key) {
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
		SearchResultPK other = (SearchResultPK) obj;
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

	public String getSolrSearchResult() {
		return solrSearchResult;
	}

	public void setSolrSearchResult(String solrSearchResult) {
		this.solrSearchResult = solrSearchResult;
	}

}
