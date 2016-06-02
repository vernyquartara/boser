package it.quartara.boser.timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.action.ActionException;
import it.quartara.boser.action.handlers.XlsWriterHelper;
import it.quartara.boser.model.ExecutionState;
import it.quartara.boser.model.Parameter;
import it.quartara.boser.model.Search;
import it.quartara.boser.model.SearchItemRequest;
import it.quartara.boser.model.SearchRequest;

/**
 * Effettua il merge degli items di una richiesta di Search.
 * @author webny
 *
 */
@Singleton
public class SearchRequestTimer {
	
	private static final Logger log = LoggerFactory.getLogger(SearchRequestTimer.class);

	@Resource
    TimerService timerService;
	
	@PersistenceContext(name="BoserPU")
	private EntityManager em;
	
	/**
	 * Schedula l'avvio del timer.
	 * 
	 * @param searchRequestId
	 * @param initialDuration
	 * @param intervalDuration
	 */
	public void startTimer(long searchRequestId, long initialDuration, long intervalDuration) {
        log.info("Setting SearchRequestTimer for request id {}", searchRequestId);
        timerService.createIntervalTimer(initialDuration, initialDuration, 
        								 new TimerConfig(new Long(searchRequestId),true));
    }
	
	@SuppressWarnings("incomplete-switch")
	@Timeout
    public void updateSearchRequest(Timer timer) {
		/*
		 * se sono presenti items in elaborazione non si effettua alcuna operazione.
		 */
        Long searchRequestId = (Long) timer.getInfo();
        log.info("handling searchRequestId {}", searchRequestId);
        SearchRequest searchRequest = em.find(SearchRequest.class, searchRequestId);
        if (searchRequest == null) {
        	log.warn("SearchRequest NOT FOUND for id {} - are you debugging?", searchRequestId);
        	log.warn("no point in run again, timer canceled");
        	timer.cancel();
        	return;
        }
        for (SearchItemRequest item : searchRequest.getItems()) {
        	if (item.getState() == ExecutionState.READY) {
        		log.debug("item {}/{} ancora in elaborazione ({})", item.getId(), item.getLastUpdate(), item.getSearchKey().getQuery());
        		/*
        		if (item.getLastUpdate().before(DateUtils.addHours(new Date(), -2))) {
        			//expiration ... TBD
        		}
        		*/
        		return;
        	}
        }
        
        /*
         * tutti gli items in stato ERROR o COMPLETED. si completa la richiesta padre.
         */
		int countCompleted = 0, countFailed = 0;
		for (SearchItemRequest item : searchRequest.getItems()) {
			switch (item.getState()) {
			case COMPLETED 	: countCompleted++; break;
			case ERROR		: countFailed++; break;
			}
        }
		log.info("items completed: {} failed: {}", countCompleted, countFailed);
        searchRequest.setLastUpdate(new Date());
        searchRequest.setState(countCompleted > 0 ? ExecutionState.COMPLETED : ExecutionState.ERROR);
        em.merge(searchRequest);
        /*
         * creazione report
         */
        Parameter param = em.find(Parameter.class, "SEARCH_REPO");
		String repo = param.getValue();
		String searchPath = repo+File.separator+searchRequest.getSearchConfig().getId()+File.separator+searchRequest.getSearch().getId();
        try {
			XlsWriterHelper.writeXlsReport(searchRequest, new File(searchPath));
		} catch (ActionException e) {
			log.warn("errore durante la creazione del report", e);
			if (countCompleted == 0) {
				/*
				 * se non ci sono items completati e non si è riusciti nemmeno
				 * a creare il report, non ha senso creare lo zip,
				 * l'elaborazione si ferma.
				 */
				log.error("richiesta in errore, la creazione dello zip non sarà effettuata");
				searchRequest.setLastUpdate(new Date());
				searchRequest.setState(ExecutionState.ERROR);
				em.merge(searchRequest);
				return;
			}
		}
        
        /*
		 * creazione del file zip con i risultati.
		 */
		File zipFile = null;
		Date now = new Date();
		try {
			zipFile = createZipFile(searchPath, now);
			log.debug("created zip file {}", zipFile.getAbsolutePath());
		} catch (IOException e) {
			log.error("errore durante la creazione del file zip", e);
			searchRequest.setLastUpdate(new Date());
			searchRequest.setState(ExecutionState.ERROR);
			em.merge(searchRequest);
			return;
		}
		Search search = searchRequest.getSearch();
		search.setTimestamp(now);
		search.setZipFilePath(zipFile.getAbsolutePath());
		em.merge(search);
        
        
        /*
         * cancellazione timer
         */
        timer.cancel();
        log.info("timer canceled");
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
				return name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".xls");
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
