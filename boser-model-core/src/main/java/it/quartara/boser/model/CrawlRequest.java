package it.quartara.boser.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("C")
public class CrawlRequest extends AsyncRequest {
	
	private static final long serialVersionUID = -7256088880635332385L;
	
	@ManyToOne
	private IndexConfig indexConfig;

	public IndexConfig getIndexConfig() {
		return indexConfig;
	}

	public void setIndexConfig(IndexConfig indexConfig) {
		this.indexConfig = indexConfig;
	}
	
}
