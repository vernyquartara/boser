package it.quartara.boser.model;

import java.util.Date;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.Table;

@Entity
@Inheritance
@DiscriminatorColumn(name="REQ_TYPE")
//@ForceDiscriminator
@Table(name="ASYNC_REQUESTS")
public abstract class AsyncRequest extends PersistentEntity {

	private static final long serialVersionUID = -6561109630548425347L;
	
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
}
