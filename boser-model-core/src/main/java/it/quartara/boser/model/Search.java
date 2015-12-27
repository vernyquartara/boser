package it.quartara.boser.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="SEARCH")
public class Search extends PersistentEntity {

	private static final long serialVersionUID = -5701233996047114282L;
	
	private String zipFilePath;
	private Date timestamp;
	@ManyToOne @JoinColumn(name = "config_id", nullable = false)
	private SearchConfig config;
	
	public String getZipFilePath() {
		return zipFilePath;
	}
	public void setZipFilePath(String zipFilePath) {
		this.zipFilePath = zipFilePath;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public SearchConfig getConfig() {
		return config;
	}
	public void setConfig(SearchConfig config) {
		this.config = config;
	}
	public String getZipLabel() {
		if (zipFilePath != null) {
			if (zipFilePath.contains("/")) {
				return zipFilePath.substring(zipFilePath.lastIndexOf("/")+1);
			} else {
				return zipFilePath.substring(zipFilePath.lastIndexOf("\\")+1);
			}
		}
		return null;
	}
}
