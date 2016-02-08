package it.quartara.boser.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("S")
public class SearchRequest extends AsyncRequest {
	
	private static final long serialVersionUID = -7256088880635332385L;
	
	@ManyToOne(optional=true)
	private SearchConfig searchConfig;
	@OneToOne(optional=true)
	private Search search;

	public SearchConfig getSearchConfig() {
		return searchConfig;
	}

	public void setSearchConfig(SearchConfig searchConfig) {
		this.searchConfig = searchConfig;
	}

	public Search getSearch() {
		return search;
	}

	public void setSearch(Search search) {
		this.search = search;
	}
	
}
