package it.quartara.boser.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("C")
public class CrawlRequest extends AsyncRequest {
	
	private static final long serialVersionUID = -7256088880635332385L;
	
	@ManyToOne(optional=true)
	private IndexConfig indexConfig;
	@ManyToOne(optional=true)
	private Index index;

	public IndexConfig getIndexConfig() {
		return indexConfig;
	}

	public void setIndexConfig(IndexConfig indexConfig) {
		this.indexConfig = indexConfig;
	}

	public Index getIndex() {
		return index;
	}

	public void setIndex(Index index) {
		this.index = index;
	}
	
}
