package it.quartara.boser.service;

import it.quartara.boser.model.Search;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/search")
@Stateless
public class SearchService {

	private static final Logger log = LoggerFactory.getLogger(SearchService.class);
	
	@PersistenceContext(unitName = "BoserPU")
	EntityManager em;

	@GET
	@Produces("application/json")
	public List<Search> getList() {
		List<Search> elements = em.createQuery("from Search", Search.class).getResultList();
		return elements;
	}
	
	@GET
	@Path("/{id}")
	public Response getById(@PathParam("id") Long id) {
		String response = "ricerca id="+id;
		return Response.status(200).entity(response).build();
	}
	

	@GET
	@Path("/searchConfig/{id}")
	public Response getBySearchConfigId(@PathParam("id") Long id) {
		List<Search> elements = 
				em.createQuery("from Search where config.id = :ID", Search.class).setParameter("ID", id).getResultList();
		return Response.status(200).entity(elements).build();
	}
	

}
