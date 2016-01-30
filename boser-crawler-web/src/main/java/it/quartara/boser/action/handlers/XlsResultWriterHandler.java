package it.quartara.boser.action.handlers;

import static it.quartara.boser.model.IndexField.TITLE;
import static it.quartara.boser.model.IndexField.URL;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.persistence.EntityManager;

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
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.action.ActionException;
import it.quartara.boser.model.Search;
import it.quartara.boser.model.SearchKey;

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

	public XlsResultWriterHandler(EntityManager em, File searchRepo) {
		super(em, searchRepo);
	}

	@Override
	protected void execute(Search search, SearchKey key, SolrDocumentList documents) throws ActionException {
		String headless = System.getProperty(JAVA_AWT_HEADLESS);
		System.setProperty(JAVA_AWT_HEADLESS, "true");
		File outputFile = new File(searchRepo.getAbsolutePath()+File.separator
									+"RES-"+getSearchResultFileNameSubstringByKey(key)
									+"-K"+key.getId()
									+".xls");
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
	    CellStyle linkStyle = wb.createCellStyle();
	    linkStyle.setFont(linkFont);
	    
	    Sheet sheet = wb.createSheet("Foglio1");
	    createHeader(sheet, headerStyle);
	    int rowCounter = 1;
		for (SolrDocument doc : documents) {
			Row row = sheet.createRow(rowCounter++);
			row.setHeightInPoints(30);
			for (int i = 0; i < 8; i++) {
		    	Cell cell = row.createCell(i, CELL_TYPE_STRING);
		    	cell.setCellStyle(defaultCellStyle);
		    	cell.setCellValue("");
		    }
			Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
			String url = (String)doc.getFieldValue(URL.toString());
			link.setAddress(url);
			
		    Cell cell0 = row.getCell(0);
		    cell0.setHyperlink(link);
		    cell0.setCellValue(getLinkLabel(url));
		    
		    Cell cell3 = row.getCell(3);
		    cell3.setCellValue((String)doc.getFieldValue(TITLE.toString()));
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		sheet.autoSizeColumn(5);
		sheet.autoSizeColumn(6);
		sheet.autoSizeColumn(7);
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
		String[] headers = {"Testata", "Tipo", "Data", "Titolo", "Argomento", "Modello", "Autore", "Foto col" };
		for (int i = 0; i < headers.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellStyle(style);
			cell.setCellValue(headers[i]);
		}
	}

}
