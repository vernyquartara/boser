package it.quartara.boser.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name="SEARCH_KEYS")
public class SearchKey extends PersistentEntity {

	private static final long serialVersionUID = -1765773180660414187L;

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(
	        name="SEARCH_TERMS",
	        joinColumns=@JoinColumn(name="SEARCH_KEY_ID")
	)
	@Column(name="SEARCH_TERM")
	private Set<String> terms;
	
	private Date validityStart;
	private Date validityEnd;

	public Date getValidityStart() {
		return validityStart;
	}

	public void setValidityStart(Date validityStart) {
		this.validityStart = validityStart;
	}

	public Date getValidityEnd() {
		return validityEnd;
	}

	public void setValidityEnd(Date validityEnd) {
		this.validityEnd = validityEnd;
	}

	public Set<String> getTerms() {
		return terms;
	}

	public void setTerms(Set<String> terms) {
		this.terms = terms;
	}
	
	public String getQuery() {
		return "\""+StringUtils.join(terms, "\" \"")+"\"";
	}
	
	
}
