package it.quartara.boser.service;

import java.util.Set;

import it.quartara.boser.model.SearchConfig;
import it.quartara.boser.model.SearchKey;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/searchKey")
public class SearchKeyService {
	
	private static final Logger log = LoggerFactory.getLogger(SearchService.class);
	
	@PersistenceContext(unitName = "BoserPU")
	EntityManager em;

	@PUT
	public Response insert(@FormParam("text") String param,
			@FormParam("searchConfigId") String searchConfigId) {
		log.debug(param);
		
		
		SearchConfig searchConfig = null;
		SearchKey key = new SearchKey();
		key.setText(param);
		searchConfig.getKeys().add(key);
		
		return Response.status(200).entity(param).build();
	}
	
	@DELETE
	public Response delete(@FormParam("searchConfigId") Long searchConfigId,
						   @FormParam("searchKeyId") Long searchKeyId) {
		
		SearchConfig searchConfig = null;
		Set<SearchKey> keys = searchConfig.getKeys();
		for (SearchKey searchKey : keys) {
			if (searchKey.getId() == searchKeyId) {
				//em.delete(searchKey);
			}
		}
		//em.merge(searchConfig);
		
		
		return Response.status(200).entity("OK").build();
	}
}
