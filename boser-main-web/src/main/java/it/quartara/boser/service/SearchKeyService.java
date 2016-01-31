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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

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
		
		SearchKey newKey = new SearchKey();
		Set<String> terms = new HashSet<>();
		if (text.indexOf(",") == -1) {
			terms.add(text.trim());
		} else {
			String[] termsArray = text.split(",");
			for (String term : termsArray) {
				terms.add(term.trim());
			}
		}
		Date now = new Date();
		newKey.setTerms(terms);
		newKey.setValidityStart(now);
		
		if (checkDuplicatedTerms(keys, newKey)) {
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
	private boolean checkDuplicatedTerms(Set<SearchKey> keys, SearchKey newKey) {
		for (SearchKey searchKey : keys) {
			Set<String> existingTerms = searchKey.getTerms();
			if (existingTerms.equals(newKey.getTerms())) {
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
}
