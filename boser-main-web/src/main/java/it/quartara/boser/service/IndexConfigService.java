package it.quartara.boser.service;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.model.IndexConfig;
import it.quartara.boser.model.Site;

@Stateless
@Path("/indexConfig")
public class IndexConfigService {
	
	private static final Logger log = LoggerFactory.getLogger(IndexConfigService.class);
	
	@PersistenceContext(unitName = "BoserPU")
	EntityManager em;

	/**
	 * Restituisce una IndexConfig in base al suo ID.
	 * @param indexConfigId
	 * @return
	 */
	@GET
	@Path("/{id}")
	public IndexConfig getSites(@PathParam("id") Long indexConfigId) {
		IndexConfig indexConfig = em.find(IndexConfig.class, indexConfigId);
		return indexConfig;
	}
	
	/**
	 * Inserisce un nuovo sito per la IndexConfig specificata.
	 * @param indexConfigId
	 * @param url
	 * @return
	 */
	@POST
	@Path("/site")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public IndexConfig addSite(@FormParam("indexConfigId") Long indexConfigId,
							   @FormParam("url") String url) {
		log.info("adding new site {} to IndexConfig: {}", url, indexConfigId);
		IndexConfig indexConfig = em.find(IndexConfig.class, indexConfigId);
		/*
		 * TODO prima di inserire un sito bisogna verificare se è già presente,
		 * in questo caso si inserisce solo l'associazione.
		 */
		Set<Site> sites = indexConfig.getSites();
		if (sites==null) {
			sites = new HashSet<>();
		}
		Site site = new Site();
		site.setUrl(url);
		site.setRegexUrlFilter(url.replace("www.", "([a-z0-9]*\\.)*"));
		sites.add(site);
		em.persist(site);
		em.merge(indexConfig);
		return indexConfig;
	}
	
	/**
	 * Rimuove un sito dalla IndexConfig specificata.
	 * @param indexConfigId
	 * @param siteId
	 * @return
	 */
	@DELETE
	@Path("/{indexConfigId}/site/{siteId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public IndexConfig removeSite(@PathParam("indexConfigId") Long indexConfigId,
						   		  @PathParam("siteId") Long siteId) {
		log.info("removing site {} from IndexConfig: {}", siteId, indexConfigId);
		Site site = em.find(Site.class, siteId);
		IndexConfig indexConfig = em.find(IndexConfig.class, indexConfigId);
		Set<Site> sites = indexConfig.getSites();
		sites.remove(site);
		em.merge(indexConfig);
		return indexConfig;
	}
}
