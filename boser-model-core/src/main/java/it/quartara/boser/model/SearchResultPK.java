package it.quartara.boser.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class SearchResultPK implements Serializable {
	
	/** */
	private static final long serialVersionUID = -1691492284034411L;
	
	private String url;
	private Long key;
	private String title;
	
	public SearchResultPK() {
		super();
	}
	
	public SearchResultPK(String url, Long key, String title) {
		super();
		this.url = url;
		this.key = key;
		this.title = title;
	}
	
	public Long getKey() {
		return key;
	}
	public void setKey(Long key) {
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
		SearchResultPK other = (SearchResultPK) obj;
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
		return "SearchResultPK [url=" + url + ", title=" + title + ", key=" + key +"]";
	}
	
}
