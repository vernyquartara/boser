package it.quartara.boser.worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.omg.CORBA.TCKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.action.ActionException;
import it.quartara.boser.action.handlers.ActionHandler;
import it.quartara.boser.action.handlers.SearchResultPersisterHandler;
import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.Index;
import it.quartara.boser.model.Parameter;
import it.quartara.boser.model.Search;
import it.quartara.boser.model.SearchAction;
import it.quartara.boser.model.SearchConfig;
import it.quartara.boser.model.SearchKey;
import it.quartara.boser.model.SearchRequest;

@MessageDriven(name = "SearchRequestQueue", activationConfig = {
	    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/SearchRequestQueue"),
	    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@TransactionManagement( TransactionManagementType.BEAN )
public class SearchWorker implements MessageListener {
	
	private final static Logger log = LoggerFactory.getLogger(SearchWorker.class);
	
	@PersistenceContext(name="BoserPU")
	private EntityManager em;
	
	@Resource
	private EJBContext context;

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(Message message) {
		Long searchRequestId = null;
		try {
			if (message instanceof MapMessage) {
				Map<String, Object> params = message.getBody(Map.class);
				searchRequestId = (Long) params.get("searchRequestId");
				log.info("Received searchRequestId from queue: {}", searchRequestId);
			} else {
				log.warn("Message of wrong type: " + message.getClass().getName());
			}
		} catch (JMSException e) {
			throw new EJBException("error reading message from queue", e);
		}
		
		UserTransaction tx = context.getUserTransaction();
		try {
			tx.begin();
		} catch (NotSupportedException | SystemException e) {
			throw new EJBException("impossibile avviare la transazione", e);
		}
		SearchRequest request = em.find(SearchRequest.class, searchRequestId);
		log.debug("handling search request: {}", request);
		
		
		Date now = new Date();
		SearchConfig searchConfig = request.getSearchConfig();
		Index currentIndex = getCurrentIndex(em);
		Search search = new Search();
		search.setConfig(searchConfig);
		search.setIndex(currentIndex);
		search.setTimestamp(now);
		em.persist(search);
		
		Parameter solrUrlParam = em.find(Parameter.class, "SOLR_URL");
		HttpSolrServer solr = new HttpSolrServer(solrUrlParam.getValue());
		ActionHandler handlers = null;
		try {
			handlers = createHandlerChain(searchConfig.getActions(), em);
		} catch (ClassNotFoundException | NoSuchMethodException
				| SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			log.error("errore durante la creazione della catena di handlers", e);
			request.setLastUpdate(now);
			request.setState(ExecutionState.ERROR);
			em.merge(request);
			try {
				tx.commit();
				return;
			} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
					| HeuristicRollbackException | SystemException e1) {
				throw new EJBException("impossibile effettuare commit della transazione", e1);
			}
		}
		for (SearchKey key : searchConfig.getKeys()) {
			String queryText = key.getQuery();
			SolrQuery query = new SolrQuery();
			query.setQuery(queryText);
			QueryResponse queryResponse = null;
			try {
				log.debug("query di ricerca: "+queryText);
				queryResponse = solr.query(query);
			} catch (SolrServerException e) {
				log.error("errore durante l'esecuzione della ricerca su Solr",e);
				request.setLastUpdate(now);
				request.setState(ExecutionState.ERROR);
				em.merge(request);
				try {
					tx.commit();
					return;
				} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
						| HeuristicRollbackException | SystemException e1) {
					throw new EJBException("impossibile effettuare commit della transazione", e1);
				}
			}
			SolrDocumentList docList = queryResponse.getResults();
			
			if (!docList.isEmpty()) {
				try {
					log.debug("avvio gestione actions");
					handlers.handle(search, key, docList);
				} catch (ActionException e) {
					StringBuilder buffer = new StringBuilder();
					buffer.append("Si è verificato un errore durante l'esecuzione delle action ");
					buffer.append("per la chiave di ricerca "+queryText);
					log.error(buffer.toString(), e);
					//continue; // or not continue???
					/*
					 * TODO impostare stato ERRORE per la ricerca??
					 */
				}
			}
		}
		/*
		 * TODO creazione del file zip e creazione oggetto Search
		 */
		Parameter param = em.find(Parameter.class, "SEARCH_REPO");
		String repo = param.getValue();
		String searchPath = repo+File.separator+searchConfig.getId()+File.separator+search.getId();
		File zipFile = null;
		try {
			zipFile = createZipFile(searchPath, now);
		} catch (IOException e) {
			log.error("errore durante la creazione del file zip", e);
			request.setLastUpdate(now);
			request.setState(ExecutionState.ERROR);
			em.merge(request);
			try {
				tx.commit();
				return;
			} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
					| HeuristicRollbackException | SystemException e1) {
				throw new EJBException("impossibile effettuare commit della transazione", e1);
			}
		}
		search.setZipFilePath(zipFile.getAbsolutePath());
		em.merge(search);
		request.setLastUpdate(now);
		request.setState(ExecutionState.COMPLETED);
		em.merge(request);
		try {
			tx.commit();
		} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
				| HeuristicRollbackException | SystemException e) {
			throw new EJBException("impossibile effettuare commit della transazione", e);
		}
	}
	
	
	public static void main(String[] args) throws IOException {
	}
	

	private Index getCurrentIndex(EntityManager em) {
		/*
		 * TODO rivedere e spostare la query
		 */
		String query = "from Index i where i.whenTerminated = "
				+ "(select max(whenTerminated) from Index)";
		TypedQuery<Index> index = em.createQuery(query, Index.class);
		return index.getSingleResult();
	}

	private ActionHandler createHandlerChain(Set<SearchAction> actions,	EntityManager em) 
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
				   IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ActionHandler firstHandler = null, currentHandler = null;
		for (SearchAction action : actions) {
			Class<?> handlerClass = Class.forName(action.getHandlerClass());
			Constructor<?> handlerConstructor = handlerClass.getConstructor(EntityManager.class);
			ActionHandler handler = (ActionHandler) handlerConstructor.newInstance(em);
			if (firstHandler == null) {
				firstHandler = handler;
			}
			if (currentHandler == null) {
				currentHandler = handler;
			} else {
				currentHandler.setNextHandler(handler);
				currentHandler = handler;
			}
		}
		currentHandler.setNextHandler(new SearchResultPersisterHandler(em));
		return firstHandler;
	}
	
	private File createZipFile(String searchPath, Date timestamp) throws IOException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		File zipFile = new File(searchPath+File.separator+format.format(timestamp)+".zip");
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		
		byte[] buffer = new byte[1024];
		File[] files = new File(searchPath).listFiles(new FilenameFilter(){
			
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".pdf")
						|| name.toLowerCase().endsWith(".xls");
			}
		});
		for (File file : files) {
			ZipEntry ze = new ZipEntry(file.getName());
			zos.putNextEntry(ze);
			FileInputStream inputFile = new FileInputStream(file);
			int len;
			while ((len = inputFile.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			inputFile.close();
			zos.closeEntry();
		}
		zos.close();
		return zipFile;
	}

}
