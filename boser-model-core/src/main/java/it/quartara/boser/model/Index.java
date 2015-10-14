package it.quartara.boser.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="INDEXES")
public class Index extends PersistentEntity {

	private static final long serialVersionUID = -2491066971995901349L;
	
	private Date whenStarted;
	private Date whenTerminated;
	private String path;
	@ManyToOne(fetch=FetchType.EAGER)
	private IndexConfig config;
	@Enumerated(EnumType.STRING)
	private ExecutionState state;
	
	public Date getWhenStarted() {
		return whenStarted;
	}
	public void setWhenStarted(Date whenStarted) {
		this.whenStarted = whenStarted;
	}
	public Date getWhenTerminated() {
		return whenTerminated;
	}
	public void setWhenTerminated(Date whenTerminated) {
		this.whenTerminated = whenTerminated;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public IndexConfig getConfig() {
		return config;
	}
	public void setConfig(IndexConfig config) {
		this.config = config;
	}
	public ExecutionState getState() {
		return state;
	}
	public void setState(ExecutionState state) {
		this.state = state;
	}
}
