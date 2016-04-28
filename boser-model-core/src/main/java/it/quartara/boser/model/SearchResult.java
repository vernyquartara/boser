package it.quartara.boser.model;

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
public class SearchResult {
	@Id
	@Column(columnDefinition = "CHAR(32)")
	private String digest;
	
	@Id
	@ManyToOne
	private SearchKey key;
	
	@Column(columnDefinition = "VARCHAR(492)")
	private String url;
	
	@Column(columnDefinition = "VARCHAR(255)")
	private String title;
	
	@Column(columnDefinition = "CHAR(1)")
	@Convert(converter = SearchResultStateConverter.class)
	private SearchResultState state;
	
	@ManyToOne
	private Search search;
	
	public SearchKey getKey() {
		return key;
	}
	public void setKey(SearchKey key) {
		this.key = key;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Search getSearch() {
		return search;
	}
	public void setSearch(Search search) {
		this.search = search;
	}
	public SearchResultState getState() {
		return state;
	}
	public void setState(SearchResultState state) {
		this.state = state;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "SearchResult [url=" + url + ", title=" + title + ", key=" + key +"]";
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
}
