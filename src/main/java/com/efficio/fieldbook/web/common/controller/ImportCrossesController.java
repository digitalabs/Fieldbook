package com.efficio.fieldbook.web.common.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ImportCrossesForm;

@Controller
@RequestMapping(ImportCrossesController.URL)
public class ImportCrossesController extends AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(ImportCrossesController.class);
    public static final String URL = "/import/crosses";

    @Resource
    private UserSelection studySelection;
    
    @Resource
    private FileService fileService;      
    
    /** The message source. */
    @Resource
    private ResourceBundleMessageSource messageSource;
    
    @Override
	public String getContentName() {
		return null;
	}
    
    @ResponseBody
    @RequestMapping(value="/germplasm", method = RequestMethod.POST)
    public String importFile(Model model, @ModelAttribute("importCrossesForm") ImportCrossesForm form) {
    	
    	Map<String, Object> resultsMap = new HashMap<String,Object>();
    	resultsMap.put("isSuccess", "1");
    	return super.convertObjectToJson(resultsMap);
    }
        
}
