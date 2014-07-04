package com.efficio.fieldbook.web.nursery.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

@Controller
@RequestMapping(DeleteNurseryController.URL)
public class DeleteNurseryController extends AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteNurseryController.class);

    public static final String URL = "/NurseryManager/deleteNursery";
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
	@Override
	public String getContentName() {
		return null;
	}

    @ResponseBody
    @RequestMapping(value="/{studyId}", method = RequestMethod.POST)
    public Map<String, Object> submitDelete(@PathVariable int studyId) throws MiddlewareQueryException {
    	Map<String, Object> results = new HashMap<String, Object>();
    	
    	try {
    		fieldbookMiddlewareService.deleteStudy(studyId);
    		results.put("isSuccess", "1");
    		
    	} catch (Exception e) {
    		LOG.error(e.getMessage(), e);
    		results.put("isSuccess", "0");
    	}
    	
    	return results;
    }    	
}
