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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.model.SearchConfig;
import it.quartara.boser.model.SearchKey;

@Path("/searchKey")
@Stateless
public class SearchKeyService {
	
	private static final Logger log = LoggerFactory.getLogger(SearchService.class);
	
	@PersistenceContext(unitName = "BoserPU")
	EntityManager em;
	
	

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
			terms.add(text);
		} else {
			String[] termsArray = text.split(",");
			for (String term : termsArray) {
				terms.add(term);
			}
		}
		Date now = new Date();
		newKey.setTerms(terms);
		newKey.setValidityStart(now);
		em.persist(newKey);
		keys.add(newKey);
		searchConfig.setKeys(keys);
		searchConfig.setLastUpdate(now);
		em.merge(searchConfig);
		return searchConfig;
	}
	
	@DELETE
	@Path("/{id}/searchConfig/{scId}")
	public SearchConfig delete(@PathParam("scId") Long searchConfigId,
						   @PathParam("id") Long searchKeyId) {
		
		SearchConfig searchConfig = em.find(SearchConfig.class, searchConfigId);
		Set<SearchKey> keys = searchConfig.getKeys();
		SearchKey toDelete = new SearchKey();
		toDelete.setId(searchKeyId);
		keys.remove(toDelete);
		em.merge(searchConfig);
		
		return searchConfig;
	}
}
