package it.quartara.boser.action.handlers;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.*;

import org.junit.Test;

@RunWith(PowerMockRunner.class)
@PrepareForTest({XlsWriterHelper.class})
public class XlsWriterHelperTest {

	@Test
	public void getLinkLabelTest() throws Exception {
		
		String url = "http://auto.motormag.it/";
		String result = Whitebox.invokeMethod(XlsWriterHelper.class, "getLinkLabel", url);
		assertEquals("auto.motormag.it", result);
		
		url = "http://www.ultimogiro.com/";
		result = Whitebox.invokeMethod(XlsWriterHelper.class, "getLinkLabel", url);
		assertEquals("ultimogiro.com", result);
		
		url = "http://motoriblog.net/";
		result = Whitebox.invokeMethod(XlsWriterHelper.class, "getLinkLabel", url);
		assertEquals("motoriblog.net", result);
		
		url = "http://ecomotorinews.it/";
		result = Whitebox.invokeMethod(XlsWriterHelper.class, "getLinkLabel", url);
		assertEquals("ecomotorinews.it", result);
		
		url = "http://auto.postificio.com/";
		result = Whitebox.invokeMethod(XlsWriterHelper.class, "getLinkLabel", url);
		assertEquals("auto.postificio.com", result);
	}
}
