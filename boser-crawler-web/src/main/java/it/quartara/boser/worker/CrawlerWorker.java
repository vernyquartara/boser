package it.quartara.boser.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.model.CrawlRequest;
import it.quartara.boser.model.Site;

@MessageDriven(name = "CrawlerRequestQueue", activationConfig = {
	    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/CrawlerRequestQueue"),
	    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class CrawlerWorker implements MessageListener {
	
	private final static Logger log = LoggerFactory.getLogger(CrawlerWorker.class);
	
	@PersistenceContext(name="BoserPU")
	private EntityManager em;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CrawlRequest request = em.find(CrawlRequest.class, crawlRequestId);
		/*
		 * scrittura files dei parametri per il crawl
		 */
		String seedFileName = "/home/webny/work/apache-nutch-1.10/input/seed.txt";
		File filtersFileTemplate = new File("/home/webny/work/apache-nutch-1.10/conf/regex-urlfilter.template");
		File filtersFile = new File("/home/webny/work/apache-nutch-1.10/conf/regex-urlfilter.txt");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        ProcessBuilder pb = new ProcessBuilder("/home/webny/work/apache-nutch-1.10/bin/crawl",
        									   "input",
        									   "crawl20151027",
        									   "1");
        pb.directory(new File("/home/webny/work/apache-nutch-1.10"));
		log.info("Run crawl command");
		Process process;
		try {
			process = pb.start();
			int errCode = process.waitFor();
			log.info("crawl command executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
			log.info("crawl Output:\n" + output(process.getInputStream()));
			log.info("exit code: " + process.exitValue());
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	public static void main(String[] args) throws IOException {
		ProcessBuilder pb = new ProcessBuilder("C:\\Users\\verny.quartara\\test.bat", "This is ProcessBuilder Example from JCG");
		pb.directory(new File("C:\\Users\\verny.quartara\\"));
		System.out.println("Run echo command");
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
