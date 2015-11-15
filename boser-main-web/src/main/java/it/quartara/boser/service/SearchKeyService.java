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
		/*
		 * TODO gestire l'eventuale presenza delle chiavi
		 * (inserire solo l'associazione)
		 */
		
		SearchConfig searchConfig = em.find(SearchConfig.class, searchConfigId);
		Set<SearchKey> keys = searchConfig.getKeys();
		if (keys==null) {
			keys = new HashSet<>();
		}
		
		if (text.indexOf(";") == -1) {
			SearchKey newKey = new SearchKey();
			newKey.setText(text);
			em.persist(newKey);
			keys.add(newKey);
		} else {
			String[] keysArray = text.split(";");
			SearchKey newParentKey = new SearchKey();
			newParentKey.setText(keysArray[0]);
			em.persist(newParentKey);
			for (int i = 1; i < keysArray.length; i++) {
				String key = keysArray[i];
				SearchKey newKey = new SearchKey();
				newKey.setText(key);
				newKey.setParent(newParentKey);
				em.persist(newKey);
				keys.add(newKey);
			}
		}
		searchConfig.setLastUpdate(new Date());
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
