package it.quartara.boser.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class SearchResultPK implements Serializable {
	
	/** */
	private static final long serialVersionUID = -1691492284034411L;
	
	private Long key;
	private String digest;
	
	public SearchResultPK() {
		super();
	}
	
	public SearchResultPK(Long key, String digest) {
		super();
		this.key = key;
		this.digest = digest;
	}
	
	public Long getKey() {
		return key;
	}
	public void setKey(Long key) {
		this.key = key;
	}
	public String getTitle() {
		return digest;
	}
	public void setTitle(String title) {
		this.digest = title;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		SearchResultPK other = (SearchResultPK) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
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
		return "SearchResultPK [digest=" + digest + ", key=" + key +"]";
	}
	
}
