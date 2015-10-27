package it.quartara.boser.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="CRAWL_REQUESTS")
public class CrawlRequest extends PersistentEntity {
	
	private static final long serialVersionUID = -7256088880635332385L;
	
	@ManyToOne
	private IndexConfig indexConfig;
	@Enumerated(EnumType.STRING)
	private ExecutionState state;
	private Date creationDate;
	private Date lastUpdate;
	
	public ExecutionState getState() {
		return state;
	}
	public void setState(ExecutionState state) {
		this.state = state;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public IndexConfig getIndexConfig() {
		return indexConfig;
	}
	public void setIndexConfig(IndexConfig indexConfig) {
		this.indexConfig = indexConfig;
	}
	
}
