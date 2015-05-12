package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(ImportDesignController.URL)
public class ImportDesignController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(ImportDesignController.class);
	
	public static final String URL = "/design";

	@Resource
	private DesignImportParser parser;
	
	@Resource
	private UserSelection studySelection;
	
	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/import", method = RequestMethod.POST, produces = "application/json")
	public Map<String, Object> importFile(Model model,
			@ModelAttribute("importDesignForm") ImportDesignForm form) {

		Map<String, Object> resultsMap = new HashMap<>();
		
		try {
			
			DesignImportData designImportData = parser.parseFile(form.getFile());
			
			studySelection.setDesignImportData(designImportData);
			
			resultsMap.put("isSuccess", 1);
			
		} catch (Exception e) {
			
			LOG.error(e.getMessage(), e);
			
			resultsMap.put("isSuccess", 0);
			// error messages is still in .prop format,
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		return resultsMap;
	}


}