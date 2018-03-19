package com.efficio.fieldbook.web.common.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.util.ResourceFinder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.web.importdesign.exporter.controller.ExportDesignTemplateController;

import junit.framework.Assert;

public class ExportDesignTemplateControllerTest {
	
	private static String DESIGN_TEMPLATE = "DesignTemplate.csv";

	private static final String OUTPUT_FILENAME = "outputFilename";

	private static final Object ERROR_MESSAGE = "errorMessage";	
	
	@Mock
	private HttpServletRequest req;

	@Mock
	private HttpServletResponse resp;
	
	@Mock
	private ResourceFinder resourceFinder;
	
	@Mock
	private MessageSource messageSource;
	
	@Mock
	private ContextUtil contextUtil;
	
	@InjectMocks
	ExportDesignTemplateController exportDesignTemplateController;
	
	@Before 
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();
	}
	
	@Test
	public void testExportDesignTemplateSuccess() throws FileNotFoundException, UnsupportedEncodingException{
		File designTemplateFile = new File(ResourceFinder.locateFile(ExportDesignTemplateControllerTest.DESIGN_TEMPLATE ).getFile());
		Mockito.when(resourceFinder.locate(Matchers.anyString())).thenReturn(ResourceFinder.locateFile(ExportDesignTemplateControllerTest.DESIGN_TEMPLATE ));
		Map<String, Object> results = this.exportDesignTemplateController.exportDesignTemplate(this.resp, this.req);
		
		Assert.assertEquals("The absolute path should be " + designTemplateFile.getAbsolutePath(), designTemplateFile.getAbsolutePath(), results.get(ExportDesignTemplateControllerTest.OUTPUT_FILENAME));
	}
	
	@Test
	public void testExportDesignTemplateFileNotFound() throws FileNotFoundException, UnsupportedEncodingException{
		Mockito.when(resourceFinder.locate(Matchers.anyString())).thenThrow(new FileNotFoundException());
		Mockito.when(this.messageSource.getMessage("common.error.file.not.found", null, Locale.ENGLISH)).thenReturn("File Not Found");
		Map<String, Object> results = this.exportDesignTemplateController.exportDesignTemplate(this.resp, this.req);
		
		Assert.assertNotNull("There should be an error message.", results.get(ExportDesignTemplateControllerTest.ERROR_MESSAGE));
	}
}
