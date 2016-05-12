package it.quartara.boser.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="SOLR_SEARCH_RESULTS")
public class SOLRSearchResult implements Serializable {
	
	/** */
	private static final long serialVersionUID = -8253431950026112226L;

	@Id
	@Column(columnDefinition = "CHAR(32)")
	private String digest;
	
	@OneToMany(mappedBy="solrSearchResult", cascade=CascadeType.ALL)
	private Set<SearchResult> foundResults;
	
	@Column(columnDefinition = "VARCHAR(492)")
	private String url;
	
	@Column(columnDefinition = "VARCHAR(255)")
	private String title;
	
	@Version
	private long version;
	
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
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((digest == null) ? 0 : digest.hashCode());
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
		SOLRSearchResult other = (SOLRSearchResult) obj;
		if (digest == null) {
			if (other.digest != null) {
				return false;
			}
		} else if (!digest.equals(other.digest)) {
			return false;
		}
		return true;
	}
	@Override
	public String toString() {
		return "SORLSearchResult [digest=" + digest + ", url=" + url + "]";
	}
	public Set<SearchResult> getFoundResults() {
		return foundResults;
	}
	public void setFoundResults(Set<SearchResult> foundResults) {
		this.foundResults = foundResults;
	}
	
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}

}
