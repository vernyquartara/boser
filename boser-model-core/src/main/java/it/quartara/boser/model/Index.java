package it.quartara.boser.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="INDEXES")
public class Index extends PersistentEntity {

	private static final long serialVersionUID = -2491066971995901349L;
	
	private Date creationDate;
	private String path;
	private short depth;
	private int topN;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public short getDepth() {
		return depth;
	}
	public void setDepth(short depth) {
		this.depth = depth;
	}
	public int getTopN() {
		return topN;
	}
	public void setTopN(int topN) {
		this.topN = topN;
	}
}
