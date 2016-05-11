package it.quartara.boser.action.handlers;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.action.ActionException;
import it.quartara.boser.model.Search;
import it.quartara.boser.model.SearchKey;
import it.quartara.boser.model.SearchResult;
import it.quartara.boser.model.SearchResultState;
import it.quartara.boser.solr.SolrDocumentListWrapper;

/**
 * Scrive i risultati di ricerca in formato Excel.
 * Dato un elenco di documenti Solr, per ognuno (che rappresenta un link)
 * scrive una riga nel foglio Excel.
 * Le colonne che devono essere valorizzare sono "Testata" (pari al nome del dominio
 * punto estensione, senza terzo livello) e "Titolo" (pari al titolo del documento)
 * Il file viene prodotto nella cartella principale della ricerca
 * effettuata.
 * @author webny
 *
 */
public class XlsResultWriterHandler extends AbstractActionHandler {
	
	//static Pattern urlPattern = Pattern.compile("//[\\w|\\d]+\\.([\\w|\\d]+\\.[\\w|\\d]+)");
	static final String JAVA_AWT_HEADLESS = "java.awt.headless";
	
	private static final Logger log = LoggerFactory.getLogger(XlsResultWriterHandler.class);

	private static final int COL_NUM = 7;

	public XlsResultWriterHandler(EntityManager em, File searchRepo) {
		super(em, searchRepo);
	}

	@Override
	protected void execute(Search search, SearchKey key, SolrDocumentListWrapper documents) throws ActionException {
		String headless = System.getProperty(JAVA_AWT_HEADLESS);
		System.setProperty(JAVA_AWT_HEADLESS, "true");
		File outputFile = new File(searchRepo.getAbsolutePath()+File.separator
									+"RES-"+getSearchResultFileNameSubstringByKey(key)
									+"-K"+key.getId()
									+".xls");
		log.debug("will write xls file: {}", outputFile);
		FileOutputStream fileOut = null;
	    try {
			fileOut = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			throw new ActionException("unable to open file: "+outputFile.getAbsolutePath());
		}
	    
	    Workbook wb = new HSSFWorkbook();
	    CreationHelper createHelper = wb.getCreationHelper();
	    Font headerFont = wb.createFont();
	    headerFont.setFontHeightInPoints((short)8);
	    headerFont.setFontName("Verdana");
	    headerFont.setBold(Boolean.TRUE);
	    CellStyle headerStyle = wb.createCellStyle();
	    headerStyle.setFont(headerFont);
	    headerStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
	    headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
	    headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	    headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
	    headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
	    headerStyle.setBorderTop(CellStyle.BORDER_THIN);
	    headerStyle.setBorderRight(CellStyle.BORDER_THIN);
	    
	    Font defaultFont = wb.createFont();
	    defaultFont.setFontHeightInPoints((short)8);
	    defaultFont.setFontName("Arial");
	    CellStyle defaultCellStyle = wb.createCellStyle();
	    defaultCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
	    defaultCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
	    defaultCellStyle.setBorderTop(CellStyle.BORDER_THIN);
	    defaultCellStyle.setBorderRight(CellStyle.BORDER_THIN);
	    defaultCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    defaultCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	    defaultCellStyle.setFont(defaultFont);
	    
	    Font linkFont = wb.createFont();
	    linkFont.setUnderline(Font.U_SINGLE);
	    linkFont.setColor(IndexedColors.BLUE.getIndex());
	    linkFont.setFontHeightInPoints((short)8);
	    linkFont.setFontName("Arial");
	    CellStyle linkStyle = wb.createCellStyle();
	    linkStyle.setFont(linkFont);
	    linkStyle.setBorderBottom(CellStyle.BORDER_THIN);
	    linkStyle.setBorderLeft(CellStyle.BORDER_THIN);
	    linkStyle.setBorderTop(CellStyle.BORDER_THIN);
	    linkStyle.setBorderRight(CellStyle.BORDER_THIN);
	    linkStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    linkStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	    
	    CellStyle dateCellStyle = wb.createCellStyle();
	    dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yy"));
	    dateCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
	    dateCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
	    dateCellStyle.setBorderTop(CellStyle.BORDER_THIN);
	    dateCellStyle.setBorderRight(CellStyle.BORDER_THIN);
	    dateCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    dateCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	    dateCellStyle.setFont(defaultFont);
	    
	    log.debug("executing: from SearchResult where search.id={} and state={}",search.getId(), SearchResultState.INSERTED);
	    List<SearchResult> docList = null;
	    try {
	    	TypedQuery<SearchResult> query = em.createQuery("from SearchResult sr where sr.search.id=:searchId and sr.state=:stateId",
	    												SearchResult.class);
	    	query.setParameter("searchId", search.getId());
	    	query.setParameter("stateId", SearchResultState.INSERTED);
	    	docList = query.getResultList();
	    	log.debug("tot. results: {}", docList.size());
	    } catch (Exception e) {
	    	log.error("eccezione imprevista", e);
	    }
	    
	    int sheetCounter = 1;
	    Sheet sheet = wb.createSheet("Foglio"+sheetCounter);
	    int rowCounter = 1;
	    createHeader(sheet, headerStyle);
	    for (SearchResult doc : docList) {
	    	if (rowCounter % 65535 == 0) {
	    		sheet.autoSizeColumn(0);
	    		sheet.setColumnWidth(1, 2007);
	    		sheet.setColumnWidth(2, 2755);
	    		sheet.autoSizeColumn(3);
	    		sheet.setColumnWidth(4, 3651);
	    		sheet.setColumnWidth(5, 3838);
	    		sheet.setColumnWidth(6, 3191);
	    		/*
	    		 * si crea un nuovo foglio al raggiungimento dei 65536 risultati
	    		 * non si prevede che si necessario un terzo foglio
	    		 */
	    		sheet = wb.createSheet("Foglio"+ ++sheetCounter);
	    		createHeader(sheet, headerStyle);
	    		rowCounter = 1;
	    	}
			Row row = sheet.createRow(rowCounter++);
			row.setHeightInPoints(30);
			for (int i = 0; i < COL_NUM; i++) {
		    	Cell cell = row.createCell(i, CELL_TYPE_STRING);
		    	cell.setCellStyle(defaultCellStyle);
		    	cell.setCellValue("");
		    }
			Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
			String url = (String)doc.getSolrSearchResult().getUrl();
			link.setAddress(url);
			
		    Cell cell0 = row.getCell(0);
		    cell0.setHyperlink(link);
		    cell0.setCellValue(getLinkLabel(url));
		    cell0.setCellStyle(linkStyle);
		    
		    Cell cell3 = row.getCell(3);
		    cell3.setCellValue((String)doc.getSolrSearchResult().getTitle());
		    doc.setState(SearchResultState.RETRIEVED);
		    
		    Cell cell2 = row.getCell(2);
		    cell2.setCellStyle(dateCellStyle);
		}
		sheet.autoSizeColumn(0);
		sheet.setColumnWidth(1, 2007);
		sheet.setColumnWidth(2, 2755);
		sheet.autoSizeColumn(3);
		sheet.setColumnWidth(4, 3651);
		sheet.setColumnWidth(5, 3838);
		sheet.setColumnWidth(6, 3191);
		try {
			wb.write(fileOut);
			fileOut.close();
			wb.close();
			log.info("wrote file: {}", outputFile.getAbsolutePath());
		} catch (IOException e) {
			throw new ActionException("unable to write to file: "+outputFile.getAbsolutePath());
		} finally {
			if (headless != null) {
				System.setProperty(JAVA_AWT_HEADLESS, headless);
			}
		}
		
	}

	private String getLinkLabel(String url) throws ActionException {
		try {
			URL parsed = new URL(url);
			String host = parsed.getHost();
			if (StringUtils.countMatches(host, ".") > 1) {
				return host.replace("www.", "");
			}
			return host;
		} catch (MalformedURLException e) {
			throw new ActionException("unable to match link url in string: " + url, e);
		}
	}

	private void createHeader(Sheet sheet, CellStyle style) {
		Row row = sheet.createRow(0);
		row.setHeightInPoints(25);
		String[] headers = {"Testata", "Tipo", "Data", "Titolo", "Argomento", "Modello", "Autore"};
		for (int i = 0; i < headers.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellStyle(style);
			cell.setCellValue(headers[i]);
		}
	}

}
