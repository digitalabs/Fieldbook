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
package com.efficio.fieldbook.web.nursery.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

/**
 * The Class AddOrRemoveTraitsController.
 */
@Controller
@RequestMapping(AddOrRemoveTraitsController.URL)
public class AddOrRemoveTraitsController extends AbstractBaseFieldbookController{

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/addOrRemoveTraits";
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AddOrRemoveTraitsController.class);
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    @Resource
    private MeasurementsGeneratorService measurementsGeneratorService;

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/addOrRemoveTraits";
    }
    
    /**
     * Shows the screen
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{
    	
    	//getUserSelection().getWorkbook().getMeasurementDatasetVariables();
    	
    	form.setMeasurementRowList(measurementsGeneratorService.generateRealMeasurementRows(getUserSelection()));
    	form.setMeasurementVariables(getUserSelection().getWorkbook().getMeasurementDatasetVariables());
    	/*
    	getUserSelection().getWorkbook().getFactors();
    	getUserSelection().getWorkbook().getConstants();
    	getUserSelection().getWorkbook().getVariates();
    	*/
    	return super.show(model);
    }
    
    @RequestMapping(value="/viewNursery/{nurseryId}", method = RequestMethod.GET)
    public String viewNursery(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, Model model, 
            @PathVariable int nurseryId) {
        Workbook workbook = null;
        
        try { 
            workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        if (workbook != null) {
            form.setMeasurementRowList(workbook.getObservations());
            form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
            userSelection.setWorkbook(workbook);
        }
        
        return super.show(model);
    }
    
    /**
     * Show details.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String showDetails(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form,          
            BindingResult result, Model model) {
        // If operation = add new nursery
        Workbook workbook = userSelection.getWorkbook();
        if (workbook == null) {
            workbook = new Workbook();
        }
        workbook.setObservations(form.getMeasurementRowList());
        userSelection.setWorkbook(workbook);
        
        return "redirect:" + SaveNurseryController.URL;
    }
    

    @ResponseBody
    @RequestMapping(value="/updateTraits", method = RequestMethod.POST)
    public  Map<String, String> updateTraits(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form,          
            BindingResult result, Model model){
        Map<String, String> resultMap = new HashMap<String, String>();
        
        Workbook workbook = userSelection.getWorkbook();
        
        int ctr = 0;
        for (MeasurementRow observation : workbook.getObservations()) {
            form.getMeasurementRowList().get(ctr).setExperimentId(observation.getExperimentId());
            ctr++;
        }

        workbook.setObservations(form.getMeasurementRowList());

        try { 
            fieldbookMiddlewareService.saveMeasurementRows(workbook);
            resultMap.put("status", "1");
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());
        }
        
        return resultMap;
    }
    
    /**
     * Gets the user selection.
     *
     * @return the user selection
     */
    public UserSelection getUserSelection() {
        return this.userSelection;
    }

}