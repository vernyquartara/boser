package it.quartara.boser.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("I")
public class SearchItemRequest extends AsyncRequest {
	
	private static final long serialVersionUID = -7256088880635332385L;
	
	@ManyToOne(optional=true)
	private SearchRequest searchRequest;
	@OneToOne(optional=true)
	private SearchKey searchKey;

	public SearchRequest getSearchRequest() {
		return searchRequest;
	}

	public void setSearchRequest(SearchRequest searchRequest) {
		this.searchRequest = searchRequest;
	}

	public SearchKey getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(SearchKey searchKey) {
		this.searchKey = searchKey;
	}

	
}
