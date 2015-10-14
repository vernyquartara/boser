package it.quartara.boser.listener;

import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.PdfConversion;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class EntityManagerListener implements ServletContextListener {

	private static final Logger log = LoggerFactory.getLogger(EntityManagerListener.class);
	
	@PersistenceContext(unitName = "BoserPU")
	EntityManager em;
	
	@Override
	//@TransactionAttribute(TransactionAttributeType.)
	public void contextInitialized(ServletContextEvent e) {
		/*
         * le conversioni in stato STARTED vanno messe a ERROR
         */
        log.debug("controllo conversioni pdf non terminate");
		TypedQuery<PdfConversion> query = em.createQuery("from PdfConversion where state='STARTED'", PdfConversion.class);
		List<PdfConversion> pdfConversions = query.getResultList();
		for (PdfConversion pdfConversion : pdfConversions) {
			log.debug("impostazione stato=ERROR per id={}", pdfConversion.getId());
			pdfConversion.setState(ExecutionState.ERROR);
			em.merge(pdfConversion);
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
	
}
