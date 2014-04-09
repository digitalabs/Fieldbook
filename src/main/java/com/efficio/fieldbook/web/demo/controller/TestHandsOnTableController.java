/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.demo.controller;

import java.util.List;

import javax.annotation.Resource;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.form.HandsontableForm;

@Controller
@RequestMapping({"/handsontable"})
public class TestHandsOnTableController extends AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(TestHandsOnTableController.class);
    	
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Override
	public String getContentName() {
		return "demo/handsontable";
	}

	/**
	* Show.
	*
	* @param testForm the test form
	* @param model the model
	* @return the string
	*/
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("handsontableForm") HandsontableForm form,  Model model) {
	    Workbook workbook = null;

	    try { 
	        List<StudyDetails> nurseries = fieldbookMiddlewareService.getAllLocalNurseryDetails();
	        int nurseryId = 0;
	        if (nurseries != null) {
	            nurseryId = nurseries.get(0).getId();
	        }
	        workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
	    } catch (MiddlewareQueryException e) {
	        LOG.error(e.getMessage(), e);
	    }
	    
	    if (workbook != null) {
	        form.setMeasurementRowList(workbook.getObservations());
	        form.setMeasurementRowListToJson(workbook.getObservations());
	        form.setDataValuesJson("");
	    }
	    
	    return super.show(model);
	}
	
	/**
        * Show.
        *
        * @param testForm the test form
	* @param model the model
	* @return the string
	*/
	@RequestMapping(method = RequestMethod.POST)
	public String submitDetails(@ModelAttribute("handsontableForm") HandsontableForm form,  Model model) {
	    List<List<String>> dataValuesList;
	    try {
	        ObjectMapper om = new ObjectMapper();
	        dataValuesList = om.readValue(form.getDataValuesJson(), new TypeReference<List<List<String>>>(){});
	        for (List<String> dataValues : dataValuesList) {
	            for (String value : dataValues) {
	                
	            }
	        }
	    }
	    catch(Exception e) {
	        
	    }
	    return super.show(model);
	}
}
