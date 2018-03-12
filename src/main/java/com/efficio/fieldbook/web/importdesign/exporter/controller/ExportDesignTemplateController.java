package com.efficio.fieldbook.web.importdesign.exporter.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.middleware.util.ResourceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.importdesign.controller.DesignImportController;

@Controller
@RequestMapping(ExportDesignTemplateController.URL)
public class ExportDesignTemplateController  extends AbstractBaseFieldbookController {
	
	public static final String URL = "/DesignTemplate";
	private static final String DESIGN_TEMPLATE = "DesignTemplate.csv";
	private static final String OUTPUT_FILENAME = "outputFilename";	
	private static final String FILENAME = "filename";
	private static final Logger LOG = LoggerFactory.getLogger(ExportDesignTemplateController.class);
	public static final String IS_SUCCESS = "isSuccess";
	private static final String ERROR_MESSAGE = "errorMessage";
	
	@Resource
	private MessageSource messageSource;
	
	@Resource
	private ResourceFinder resourceFinder;
	
	@ResponseBody
	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public Map<String, Object> exportDesignTemplate(final HttpServletResponse response, final HttpServletRequest req){		
		File designTemplateFile;
		final Map<String, Object> results = new HashMap<>();
		try {
			designTemplateFile = new File(resourceFinder.locate(ExportDesignTemplateController.DESIGN_TEMPLATE).getFile());
			results.put(OUTPUT_FILENAME, designTemplateFile.getAbsolutePath());
			results.put(FILENAME, designTemplateFile.getName());
			results.put(ExportDesignTemplateController.IS_SUCCESS, 1);
		} catch (FileNotFoundException e) {
			ExportDesignTemplateController.LOG.error(e.getMessage(), e);
			results.put(DesignImportController.IS_SUCCESS, 0);
			results.put(ERROR_MESSAGE, this.messageSource.getMessage("common.error.file.not.found", null, Locale.ENGLISH));
		}
		return results;
	}
	
	@Override
	public String getContentName() {	
		return null;
	}
}
