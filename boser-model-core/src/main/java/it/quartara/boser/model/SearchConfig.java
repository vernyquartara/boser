package it.quartara.boser.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="SEARCH_CONFIGS")
public class SearchConfig extends PersistentEntity {

	private static final long serialVersionUID = -2137681233423846088L;
	
	@ManyToOne
	private Crawler crawler;
	@OneToMany
	private Set<SearchAction> actions;
	@OneToMany
	private Set<SearchKey> keys;
	
	private String description;
	private Date creationDate;
	private Date lastUpdate;

	public Crawler getCrawler() {
		return crawler;
	}

	public void setCrawler(Crawler crawler) {
		this.crawler = crawler;
	}

	public Set<SearchAction> getActions() {
		return actions;
	}

	public void setActions(Set<SearchAction> actions) {
		this.actions = actions;
	}

	public Set<SearchKey> getKeys() {
		return keys;
	}

	public void setKeys(Set<SearchKey> keys) {
		this.keys = keys;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

}
