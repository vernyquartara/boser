package it.quartara.boser.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="INDEX_CONFIGS")
public class IndexConfig extends PersistentEntity {

	private static final long serialVersionUID = -8628166413138666941L;
	
	@ManyToOne
	private Crawler crawler;
	private short depth;
	private short topN;
	@OneToMany(fetch=FetchType.EAGER)
	private Set<Site> sites;
	
	public short getDepth() {
		return depth;
	}
	public void setDepth(short depth) {
		this.depth = depth;
	}
	public short getTopN() {
		return topN;
	}
	public void setTopN(short topN) {
		this.topN = topN;
	}
	public Set<Site> getSites() {
		return sites;
	}
	public void setSites(Set<Site> sites) {
		this.sites = sites;
	}
	public Crawler getCrawler() {
		return crawler;
	}
	public void setCrawler(Crawler crawler) {
		this.crawler = crawler;
	}
}
