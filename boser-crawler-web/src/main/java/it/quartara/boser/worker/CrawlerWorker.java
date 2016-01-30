package it.quartara.boser.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.model.CrawlRequest;
import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.Index;
import it.quartara.boser.model.Parameter;
import it.quartara.boser.model.Site;

@MessageDriven(name = "CrawlerWorker", activationConfig = {
	    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/CrawlerRequestQueue"),
	    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@TransactionManagement( TransactionManagementType.BEAN )
public class CrawlerWorker implements MessageListener {
	
	private final static Logger log = LoggerFactory.getLogger(CrawlerWorker.class);
	
	@PersistenceContext(name="BoserPU")
	private EntityManager em;
	
	@Resource
	private EJBContext context;

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(Message message) {
		Long crawlRequestId = null;
		try {
			if (message instanceof MapMessage) {
				Map<String, Object> params = message.getBody(Map.class);
				crawlRequestId = (Long) params.get("crawlRequestId");
				log.info("Received crawlRequestId from queue: {}", crawlRequestId);
			} else {
				log.warn("Message of wrong type: " + message.getClass().getName());
			}
		} catch (JMSException e) {
			throw new EJBException("error reading message from queue", e);
		}
		
		CrawlRequest request = em.find(CrawlRequest.class, crawlRequestId);
		/*
		 * scrittura files dei parametri per il crawl
		 */
		Parameter nutchHomeParameter = em.find(Parameter.class, "NUTCH_HOME");
		String nutchHome = nutchHomeParameter.getValue();
		String seedFileName = nutchHome+"/input/seed.txt";
		File filtersFileTemplate = new File(nutchHome+"/conf/regex-urlfilter.template");
		File filtersFile = new File(nutchHome+"/conf/regex-urlfilter.txt");
		File seedFile = new File(seedFileName);
		Set<Site> sites = request.getIndexConfig().getSites();
		List<String> urls = new ArrayList<>();
		List<String> filters = new ArrayList<>();
		for (Site site : sites) {
			urls.add(site.getUrl());
			filters.add(site.getRegexUrlFilter());
		}
		try {
			FileUtils.writeLines(seedFile, urls);
			FileUtils.copyFile(filtersFileTemplate, filtersFile);
			FileUtils.writeLines(filtersFile, filters, true);
		} catch (IOException e) {
			throw new EJBException("unable to write crawler configuration", e);
		}
        
		Parameter solrUrlParam = em.find(Parameter.class, "SOLR_URL");
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		String crawlId = "CRAWL"+df.format(new Date());
        ProcessBuilder pb = new ProcessBuilder(nutchHome+"/bin/crawl",
							        		   "-i",
											   "-D",
											   "solr.server.url="+solrUrlParam.getValue(),
        									   "input",
        									   crawlId,
        									   Short.valueOf(request.getIndexConfig().getDepth()).toString());
        pb.directory(new File(nutchHome));
        String logFilePath = nutchHome+File.separator+"logs"+File.separator+crawlId+".log";
        pb.redirectOutput(new File(logFilePath));
		log.info("Running crawler, check log file {} and wait...", logFilePath);
		Process process;
		UserTransaction utx = context.getUserTransaction();
		try {
			utx.setTransactionTimeout(Integer.MAX_VALUE);
			/*
			 * prima transazione, cancellazione crawl precedente
			 * e avvio del processo
			 */
			utx.begin();
			try {
				Index currentIndex = getCurrentIndex();
				if (currentIndex!=null) {
					FileUtils.deleteDirectory(new File(currentIndex.getPath()));
				}
				request.setState(ExecutionState.STARTED);
				request.setLastUpdate(new Date());
				em.merge(request);
				process = pb.start();
				utx.commit();
			} catch (IOException e) {
				request.setState(ExecutionState.ERROR);
				request.setLastUpdate(new Date());
				em.merge(request);
				utx.commit();
				throw new EJBException("unable to start the cralwer process", e);
			}
			/*
			 * seconda transazione, esecuzione del processo
			 */
			utx.begin();
			int errCode;
			try {
				errCode = process.waitFor();
			} catch (InterruptedException e) {
				request.setState(ExecutionState.ERROR);
				request.setLastUpdate(new Date());
				em.merge(request);
				utx.commit();
				throw new EJBException("error executing the cralwer process", e);
			}
			log.info("crawl command executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
			try {
				log.info("crawl Output:\n" + output(process.getInputStream()));
			} catch (IOException e) {
				log.error("error to log process output");
			}
			Date now = new Date();
			log.info("exit code: " + process.exitValue());
			Index newIndex = new Index();
			//newIndex.setConfig(request.getIndexConfig());
			newIndex.setCreationDate(now);
			newIndex.setPath(nutchHome+File.separator+crawlId);
			newIndex.setDepth(request.getIndexConfig().getDepth());
			newIndex.setTopN(request.getIndexConfig().getTopN());
			em.persist(newIndex);
			request.setState(errCode==0?ExecutionState.COMPLETED:ExecutionState.ERROR);
			request.setLastUpdate(now);
			request.setIndex(newIndex);
			em.merge(request);
			utx.commit();
			log.info("done.");
		} catch (NotSupportedException | SystemException | SecurityException | IllegalStateException | 
				RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
			throw new EJBException("unable to handle transaction", e);
		}
	}
	
	private static String output(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + System.getProperty("line.separator"));
			}
		} finally {
			br.close();
		}
		return sb.toString();
	}
	
	private Index getCurrentIndex() {
		String query = "from Index i where i.creationDate = "
				+ "(select max(creationDate) from Index)";
		TypedQuery<Index> index = em.createQuery(query, Index.class);
		try {
			return index.getSingleResult();
		} catch (NoResultException e) {
			log.warn("nessun indice presente su DB (è il primo crawl?)");
			return null;
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		//ProcessBuilder pb = new ProcessBuilder("C:\\Users\\verny.quartara\\test.bat", "This is ProcessBuilder Example from JCG");
		//pb.directory(new File("C:\\Users\\verny.quartara\\"));
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		String nutchHome = "/home/webny/work/apache-nutch-1.10";
		ProcessBuilder pb = new ProcessBuilder(nutchHome+"/bin/crawl",
				"-i",
				"-D",
				"solr.server.url=http://localhost:8983/solr/boser",
				"input",
				"CRAWL"+df.format(new Date()),
				"1");
		pb.directory(new File(nutchHome));
		pb.redirectOutput(new File(nutchHome+"/out.log"));
		Process process;
		try {
			process = pb.start();
			int errCode = process.waitFor();
			System.out.println("Echo command executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
			System.out.println("Echo Output:\n" + output(process.getInputStream()));	
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
