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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.impl.ImportGermplasmFileServiceImpl;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

/**
 * The Class AddOrRemoveTraitsController.
 */
@Controller
@RequestMapping(AddOrRemoveTraitsController.URL)
public class AddOrRemoveTraitsController extends AbstractBaseFieldbookController{

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/addOrRemoveTraits";
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    @Resource
    private FieldbookService fieldbookMiddlewareService;

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
    	
    	int index = 0;
    	List<MeasurementRow> measurementRows = new ArrayList();
    	for(ImportedGermplasm germplasm : getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()){
    		MeasurementRow measurementRow = new MeasurementRow();
    		List<MeasurementData> dataList = new ArrayList();
    		index++;
    		int newGid = fieldbookMiddlewareService.getNextGermplasmId();
    		for(MeasurementVariable var : getUserSelection().getWorkbook().getMeasurementDatasetVariables()){
    			MeasurementData measurementData =null;
    			var.setFactor(true);    			
    			if (var.getName().equalsIgnoreCase(ImportGermplasmFileServiceImpl.FACTOR_ENTRY)) {
    				measurementData = new MeasurementData(var.getName(), Integer.toString(index));
                } else if (var.getName().equalsIgnoreCase(ImportGermplasmFileServiceImpl.FACTOR_DESIGNATION)
                		|| var.getName().equalsIgnoreCase(ImportGermplasmFileServiceImpl.FACTOR_DESIG)) {
                	measurementData = new MeasurementData(var.getName(), germplasm.getDesig());                	
                } else if (var.getName().equalsIgnoreCase(ImportGermplasmFileServiceImpl.FACTOR_GID)) {
                	measurementData = new MeasurementData(var.getName(), Integer.toString(newGid));                	
                } else if (var.getName().equalsIgnoreCase(ImportGermplasmFileServiceImpl.FACTOR_CROSS)) {
                	measurementData = new MeasurementData(var.getName(), "");                	
                } else if (var.getName().equalsIgnoreCase(ImportGermplasmFileServiceImpl.FACTOR_SOURCE)) {
                	measurementData = new MeasurementData(var.getName(), "");
                } else if (var.getName().equalsIgnoreCase(ImportGermplasmFileServiceImpl.FACTOR_ENTRY_CODE)) {
                	measurementData = new MeasurementData(var.getName(), Integer.toString(index));
                } else if (var.getName().equalsIgnoreCase(ImportGermplasmFileServiceImpl.FACTOR_PLOT)) {
                	measurementData = new MeasurementData(var.getName(), Integer.toString(index));
                } else if (var.getName().equalsIgnoreCase(ImportGermplasmFileServiceImpl.FACTOR_CHECK)) {
                	measurementData = new MeasurementData(var.getName(), germplasm.getCheck());
                }
                else{
                	//meaning non factor
                	measurementData = new MeasurementData(var.getName(), "");
                	measurementData.setEditable(true);
                	var.setFactor(false);
                	//measurementData.set
                }
    			dataList.add(measurementData);
    		}
    		measurementRow.setDataList(dataList);
    		measurementRows.add(measurementRow);
    	}
    	form.setMeasurementRowList(measurementRows);
    	form.setMeasurementVariables(getUserSelection().getWorkbook().getMeasurementDatasetVariables());
    	/*
    	getUserSelection().getWorkbook().getFactors();
    	getUserSelection().getWorkbook().getConstants();
    	getUserSelection().getWorkbook().getVariates();
    	*/
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
    	//need to save the 
    	/*
    	 * for
    	 */
    	form.getMeasurementRowList(); //this contains all the data
    	form.getMeasurementVariables(); //no value, just get it in the userSelection.getWorkbook 
        return "redirect:" + SaveNurseryController.URL;
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