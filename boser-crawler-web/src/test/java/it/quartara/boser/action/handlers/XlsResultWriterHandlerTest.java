package it.quartara.boser.action.handlers;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.*;

import org.junit.Test;

@RunWith(PowerMockRunner.class)
@PrepareForTest({XlsResultWriterHandler.class})
public class XlsResultWriterHandlerTest {

	@Test
	public void getLinkLabelTest() throws Exception {
		XlsResultWriterHandler instance = new XlsResultWriterHandler(null, null);
		
		String url = "http://auto.motormag.it/";
		String result = Whitebox.invokeMethod(instance, "getLinkLabel", url);
		assertEquals("auto.motormag.it", result);
		
		url = "http://www.ultimogiro.com/";
		result = Whitebox.invokeMethod(instance, "getLinkLabel", url);
		assertEquals("ultimogiro.com", result);
		
		url = "http://motoriblog.net/";
		result = Whitebox.invokeMethod(instance, "getLinkLabel", url);
		assertEquals("motoriblog.net", result);
		
		url = "http://ecomotorinews.it/";
		result = Whitebox.invokeMethod(instance, "getLinkLabel", url);
		assertEquals("ecomotorinews.it", result);
		
		url = "http://auto.postificio.com/";
		result = Whitebox.invokeMethod(instance, "getLinkLabel", url);
		assertEquals("auto.postificio.com", result);
	}
}
