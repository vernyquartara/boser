package it.quartara.boser.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.model.SearchConfig;
import it.quartara.boser.model.SearchKey;

@Path("/searchKey")
@Stateless
public class SearchKeyService {
	
	private static final Logger log = LoggerFactory.getLogger(SearchKeyService.class);
	
	@PersistenceContext(unitName = "BoserPU")
	private EntityManager em;

	@POST
	public SearchConfig insert(@FormParam("text") String text,
							   @FormParam("searchConfigId") Long searchConfigId) {
		log.debug("inserting new key: {}", text);
		
		SearchConfig searchConfig = em.find(SearchConfig.class, searchConfigId);
		Set<SearchKey> keys = searchConfig.getKeys();
		if (keys==null) {
			keys = new HashSet<>();
		}
		/*
		 * bonifica stringa di input
		 */
		Set<String> terms = new HashSet<>();
		if (text.indexOf(",") == -1) {
			terms.add(text.trim());
		} else {
			String[] termsArray = StringUtils.removeEnd(StringUtils.removeStart(text, ","), ",")
								 .trim()
								 .split(",");
			for (String term : termsArray) {
				terms.add(term.trim());
			}
		}
		/*
		 * controllo duplicati
		 */
		if (checkDuplicatedTerms(keys, terms)) {
			SearchKey newKey = new SearchKey();
			Date now = new Date();
			newKey.setTerms(terms);
			newKey.setValidityStart(now);
			em.persist(newKey);
			keys.add(newKey);
			searchConfig.setKeys(keys);
			searchConfig.setLastUpdate(now);
			em.merge(searchConfig);
		} else {
			/*
			 * TODO raffinare gestione?
			 */
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		return searchConfig;
	}

	/*
	 * controlla la presenza di duplicati.
	 * per ogni chiave di ricerca, se i termini sono uguali a quelli
	 * della chiave che si sta cercando di inserire, restituisce false.
	 */
	private boolean checkDuplicatedTerms(Set<SearchKey> keys, Set<String> newKeyTerms) {
		for (SearchKey searchKey : keys) {
			Set<String> existingTerms = searchKey.getTerms();
			if (existingTerms.equals(newKeyTerms)) {
				return false;
			}
		}						
		return true;
	}

	@DELETE
	@Path("/{id}/searchConfig/{scId}")
	public SearchConfig delete(@PathParam("scId") Long searchConfigId,
						   @PathParam("id") Long searchKeyId) {
		Date now = new Date();
		SearchKey keyToDelete = em.find(SearchKey.class, searchKeyId);
		keyToDelete.setValidityEnd(now);
		
		SearchConfig searchConfig = em.find(SearchConfig.class, searchConfigId);
		Set<SearchKey> keys = searchConfig.getKeys();
		keys.remove(keyToDelete);
		searchConfig.setLastUpdate(now);
		em.merge(searchConfig);
		
		return searchConfig;
	}
	
	@PUT
	@Path("/{id}/searchConfig/{scId}")
	public SearchConfig update(@PathParam("scId") Long searchConfigId,
			   				   @PathParam("id") Long searchKeyId,
			   				   @FormParam("newValue") String newValue) {
		if (StringUtils.isEmpty(newValue)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		/*
		 * prima controllo se la modifica genera una chiave di ricerca duplicata
		 */
		Set<String> terms = new HashSet<>();
		if (newValue.indexOf(",") == -1) {
			terms.add(newValue.trim());
		} else {
			String[] termsArray = StringUtils.removeEnd(StringUtils.removeStart(newValue, ","), ",")
								 .trim()
								 .split(",");
			for (String term : termsArray) {
				terms.add(term.trim());
			}
		}
		SearchConfig searchConfig = em.find(SearchConfig.class, searchConfigId);
		Set<SearchKey> keys = searchConfig.getKeys();
		SearchKey searchKey = em.find(SearchKey.class, searchKeyId);
		if (checkDuplicatedTerms(keys, terms)) {
			searchKey.setTerms(terms);
			em.merge(searchKey);
		} else {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		
		em.refresh(searchConfig);
		return searchConfig;
	}
}
