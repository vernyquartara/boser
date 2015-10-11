package it.quartara.boser.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="USERS")
public class User extends PersistentEntity {

	private static final long serialVersionUID = 5245239781066415088L;
	
	private String username;
	private String email;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
