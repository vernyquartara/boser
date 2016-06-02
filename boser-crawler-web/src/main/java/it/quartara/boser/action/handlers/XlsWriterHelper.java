package it.quartara.boser.action.handlers;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.action.ActionException;
import it.quartara.boser.model.IndexField;
import it.quartara.boser.model.Search;
import it.quartara.boser.model.SearchItemRequest;
import it.quartara.boser.model.SearchKey;
import it.quartara.boser.model.SearchRequest;
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
public class XlsWriterHelper {
	/*
	 * non può essere un EJB perché la chiamata arriva da un EJB bean managed, quindi esiste già una transazione,
	 * la chiamata a questa classe non deve avere gestione transazionale "propria" ma deve lasciare gestire
	 * tutto al chiamante. 
	 */
	static final String JAVA_AWT_HEADLESS = "java.awt.headless";
	
	private static final Logger log = LoggerFactory.getLogger(XlsWriterHelper.class);

	private static final int MAX_ROWS = 65535;

	public static void writeXlsResult(Search search, SearchKey key, SolrDocumentListWrapper documents, File searchRepo) throws ActionException {
		String headless = System.getProperty(JAVA_AWT_HEADLESS);
		System.setProperty(JAVA_AWT_HEADLESS, "true");
		File outputFile = new File(searchRepo.getAbsolutePath()+File.separator
									+"RES-"+getSearchResultFileNameSubstringByKey(key)
									+"-K"+key.getId()
									+".xls");
		log.debug("going to write xls file: {}", outputFile);
		
		
		Workbook wb = null;
		FileOutputStream fileOut = null;
		FileInputStream fileIn = null;
		
		try {
			if (outputFile.exists()) {
				/* se il file esiste si apre in modifica */
				fileIn = new FileInputStream(outputFile);
				wb = new HSSFWorkbook(fileIn, false);
			} else {
				/* se non esiste si crea nuovo */
				fileOut = new FileOutputStream(outputFile);
				wb = new HSSFWorkbook();
			}
		} catch (IOException e) {
			throw new ActionException("unable to open file: "+outputFile.getAbsolutePath());
		}
		
	    
	    CreationHelper createHelper = wb.getCreationHelper();
	    Font headerFont = createHeaderFont(wb);
	    CellStyle headerStyle = createHeaderStyle(wb);
	    headerStyle.setFont(headerFont);
	    
	    Font defaultFont = createDefaultFont(wb);
	    CellStyle defaultCellStyle = createDefaultStyle(wb);
	    defaultCellStyle.setFont(defaultFont);
	    
	    Font linkFont = createLinkFont(wb);
	    CellStyle linkStyle = createLinkStyle(wb);
	    linkStyle.setFont(linkFont);
	    
	    CellStyle dateCellStyle = createDateStyle(wb);
	    dateCellStyle.setFont(defaultFont);
	    dateCellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/MM/yy"));
	    
	    String[] heanders = new String[]{"Testata", "Tipo", "Data", "Titolo", "Argomento", "Modello", "Autore"};

	    
	    
	    int sheetCounter = wb.getNumberOfSheets();
	    int rowCounter = -1;
	    Sheet sheet = null;
	    if (sheetCounter == 0) {
	    	/* se ancora non esiste alcun foglio */
	    	rowCounter = 0;
	    } else {
	    	/* se esiste già almeno un foglio */
	    	sheet = wb.getSheetAt(wb.getNumberOfSheets()-1);
	    	rowCounter = getRowCounter(sheet);
	    }
	    for (SolrDocument doc : documents.getList()) {
	    	if (rowCounter % MAX_ROWS == 0) {
	    		/* si crea un nuovo foglio al raggiungimento dei MAX_ROWS risultati */
	    		sheet = wb.createSheet("Foglio"+ ++sheetCounter);
	    		createHeader(sheet, headerStyle, heanders);
	    		rowCounter = 1;
	    	}
			Row row = sheet.createRow(rowCounter++);
			row.setHeightInPoints(30);
			for (int i = 0; i < heanders.length; i++) {
		    	Cell cell = row.createCell(i, CELL_TYPE_STRING);
		    	cell.setCellStyle(defaultCellStyle);
		    	cell.setCellValue("");
		    }
			Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
			String url = (String)doc.getFieldValue(IndexField.URL.toString());
			link.setAddress(url);
			
		    Cell cell0 = row.getCell(0);
		    cell0.setHyperlink(link);
		    cell0.setCellValue(getLinkLabel(url));
		    cell0.setCellStyle(linkStyle);
		    
		    Cell cell3 = row.getCell(3);
		    cell3.setCellValue((String)doc.getFirstValue(IndexField.TITLE.toString()));
		    
		    Cell cell2 = row.getCell(2);
		    cell2.setCellStyle(dateCellStyle);
		    
		    sheet.autoSizeColumn(0);
    		sheet.setColumnWidth(1, 2007);
    		sheet.setColumnWidth(2, 2755);
    		sheet.autoSizeColumn(3);
    		sheet.setColumnWidth(4, 3651);
    		sheet.setColumnWidth(5, 3838);
    		sheet.setColumnWidth(6, 3191);
		}
		try {
			if (fileIn != null)   {
				FileOutputStream fos = new FileOutputStream(outputFile);
				wb.write(fos);
				fos.close();
			} else {
				wb.write(fileOut);
				fileOut.close();
			}
			wb.close();
			log.info("file written: {}", outputFile.getAbsolutePath());
		} catch (IOException e) {
			throw new ActionException("unable to write to file: "+outputFile.getAbsolutePath());
		} finally {
			if (headless != null) {
				System.setProperty(JAVA_AWT_HEADLESS, headless);
			}
		}
		
	}

	private static int getRowCounter(Sheet sheet) {
		int rowCounter = 0;
		for (Row row : sheet) {
			if (row.getCell(row.getFirstCellNum()) == null) {
				return rowCounter;
			}
			rowCounter++;
		}
		return rowCounter;
	}

	private static CellStyle createDateStyle(Workbook wb) {
		CellStyle dateCellStyle = wb.createCellStyle();
	    dateCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
	    dateCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
	    dateCellStyle.setBorderTop(CellStyle.BORDER_THIN);
	    dateCellStyle.setBorderRight(CellStyle.BORDER_THIN);
	    dateCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    dateCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		return dateCellStyle;
	}

	private static CellStyle createLinkStyle(Workbook wb) {
		CellStyle linkStyle = wb.createCellStyle();
	    linkStyle.setBorderBottom(CellStyle.BORDER_THIN);
	    linkStyle.setBorderLeft(CellStyle.BORDER_THIN);
	    linkStyle.setBorderTop(CellStyle.BORDER_THIN);
	    linkStyle.setBorderRight(CellStyle.BORDER_THIN);
	    linkStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    linkStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		return linkStyle;
	}

	private static Font createLinkFont(Workbook wb) {
		Font linkFont = wb.createFont();
	    linkFont.setUnderline(Font.U_SINGLE);
	    linkFont.setColor(IndexedColors.BLUE.getIndex());
	    linkFont.setFontHeightInPoints((short)8);
	    linkFont.setFontName("Arial");
		return linkFont;
	}

	private static CellStyle createDefaultStyle(Workbook wb) {
		CellStyle defaultCellStyle = createLinkStyle(wb);
		return defaultCellStyle;
	}

	private static Font createDefaultFont(Workbook wb) {
		Font defaultFont = wb.createFont();
	    defaultFont.setFontHeightInPoints((short)8);
	    defaultFont.setFontName("Arial");
		return defaultFont;
	}

	private static CellStyle createHeaderStyle(Workbook wb) {
		CellStyle headerStyle = wb.createCellStyle();
	    headerStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
	    headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
	    headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	    headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
	    headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
	    headerStyle.setBorderTop(CellStyle.BORDER_THIN);
	    headerStyle.setBorderRight(CellStyle.BORDER_THIN);
		return headerStyle;
	}

	private static Font createHeaderFont(Workbook wb) {
		Font headerFont = wb.createFont();
	    headerFont.setFontHeightInPoints((short)8);
	    headerFont.setFontName("Verdana");
	    headerFont.setBold(Boolean.TRUE);
		return headerFont;
	}

	private static String getLinkLabel(String url) throws ActionException {
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

	private static void createHeader(Sheet sheet, CellStyle style, String... headers) {
		Row row = sheet.createRow(0);
		row.setHeightInPoints(25);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellStyle(style);
			cell.setCellValue(headers[i]);
		}
	}
	
	private static String getSearchResultFileNameSubstringByKey(SearchKey key) {
		String result = key.getTerms().iterator().next();
		if (key.getTerms().size() > 1) {
			return result+"_etc";
		}
		return result;
	}
	
	public static void writeXlsReport(SearchRequest search, File searchRepo) throws ActionException {
		String headless = System.getProperty(JAVA_AWT_HEADLESS);
		System.setProperty(JAVA_AWT_HEADLESS, "true");
		File outputFile = new File(searchRepo.getAbsolutePath()+File.separator
									+"REPORT-"+search.getId()
									+".xls");
		log.debug("going to write xls file: {}", outputFile);
		
		Workbook wb = null;
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(outputFile);
			wb = new HSSFWorkbook();
		} catch (IOException e) {
			throw new ActionException("unable to open file: "+outputFile.getAbsolutePath());
		}
		
	    
	    Font headerFont = createHeaderFont(wb);
	    CellStyle headerStyle = createHeaderStyle(wb);
	    headerStyle.setFont(headerFont);
	    
	    Font defaultFont = createDefaultFont(wb);
	    CellStyle defaultCellStyle = createDefaultStyle(wb);
	    defaultCellStyle.setFont(defaultFont);
	    
	    CellStyle dateCellStyle = createDateStyle(wb);
	    dateCellStyle.setFont(defaultFont);
	    dateCellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/MM/yy HH:mm:ss"));
	    
	    
	    Sheet sheet = wb.createSheet("Report");
	    
	    int rowCounter = 1;
	    String[] headers = new String[]{"Id chiave", "Stato", "Timestamp", "Testo chiave"};
	    createHeader(sheet, headerStyle, headers);
		
		for (SearchItemRequest item : search.getItems()) {
			Row row = sheet.createRow(rowCounter++);
			row.setHeightInPoints(30);
			for (int i = 0; i < headers.length; i++) {
				/*
				 * creazione celle e applicazione stile
				 */
		    	Cell cell = row.createCell(i, CELL_TYPE_STRING);
		    	cell.setCellStyle(defaultCellStyle);
		    }
			
		    Cell cell0 = row.getCell(0);
		    cell0.setCellValue(item.getSearchKey().getId());
		    
		    Cell cell1 = row.getCell(1);
		    cell1.setCellValue(item.getState().toString());
		    
		    Cell cell2 = row.getCell(2);
		    cell2.setCellStyle(dateCellStyle);
		    cell2.setCellValue(item.getLastUpdate());
		    
		    Cell cell3 = row.getCell(3);
		    cell3.setCellValue(item.getSearchKey().getQuery());
		    
		    for (int i = 0; i < headers.length; i++) {
		    	sheet.autoSizeColumn(i);
		    }
		}
		
		try {
			wb.write(fileOut);
			fileOut.close();
			wb.close();
			log.info("report file written: {}", outputFile.getAbsolutePath());
		} catch (IOException e) {
			throw new ActionException("unable to write to file: "+outputFile.getAbsolutePath());
		} finally {
			if (headless != null) {
				System.setProperty(JAVA_AWT_HEADLESS, headless);
			}
		}
	}

}
