package it.quartara.boser.listener;

import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.PdfConversion;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class EntityManagerListener implements ServletContextListener {

	private static final Logger log = LoggerFactory.getLogger(EntityManagerListener.class);
	
	private static EntityManagerFactory emf;
	
	/*
	 * Creates the EntityManagerFactory and puts it into the context
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
    public void contextInitialized(ServletContextEvent e) {
        emf = Persistence.createEntityManagerFactory("BoserPU");
        aggiornaStatoConversioniNonTerminate(emf);
    }
 
	/* Release the EntityManagerFactory
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
    public void contextDestroyed(ServletContextEvent e) {
        aggiornaStatoConversioniNonTerminate(emf);
        emf.close();
    }
	
	private void aggiornaStatoConversioniNonTerminate(EntityManagerFactory emf) {
		/*
         * le conversioni in stato STARTED vanno messe a ERROR
         */
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        log.debug("controllo conversioni pdf non terminate");
		TypedQuery<PdfConversion> query = em.createQuery("from PdfConversion where state='STARTED'", PdfConversion.class);
		List<PdfConversion> pdfConversions = query.getResultList();
		for (PdfConversion pdfConversion : pdfConversions) {
			log.debug("impostazione stato=ERROR per id={}", pdfConversion.getId());
			pdfConversion.setState(ExecutionState.ERROR);
			em.merge(pdfConversion);
		}
		em.getTransaction().commit();
		em.close();
	}
	
	public static EntityManager getEntityManager() {
		if (emf != null) {
			return emf.createEntityManager();
		}
		throw new IllegalStateException("EntityManagerFactory non inizializzato");
	}

}
