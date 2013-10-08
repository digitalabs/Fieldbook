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

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.SaveNurseryForm;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.validation.SaveNurseryValidator;

@Controller
@RequestMapping(SaveNurseryController.URL)
public class SaveNurseryController extends AbstractBaseFieldbookController{
	
	private static final Logger LOG = LoggerFactory.getLogger(SaveNurseryController.class);

    public static final String URL = "/NurseryManager/saveNursery";

    @Resource
    private UserSelection userSelection;
    
    @Resource
    private DataImportService dataImportService;
    
    @Resource
    private SaveNurseryValidator saveNurseryValidator;
    
    @Resource
    private MeasurementsGeneratorService measurementsGeneratorService;
    
    @Override
    public String getContentName() {
        return "NurseryManager/saveNursery";
    }
    
    public UserSelection getUserSelection() {
        return this.userSelection;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("saveNurseryForm") SaveNurseryForm form, Model model) {
    	
    	return super.show(model);
    }

    @RequestMapping(method = RequestMethod.POST)
    public String saveNursery(@ModelAttribute("saveNurseryForm") SaveNurseryForm form, BindingResult result, Model model) {
    	
        Workbook workbook = getWorkbook();
    	
    	saveNurseryValidator.validate(form, result);
    	
    	if (result.hasErrors()) {
    		return super.show(model);
    	}
    	
    	try {
    	    setStudyDetails(form, workbook);
    	    
    		dataImportService.saveDataset(workbook);
    	
    	} catch(MiddlewareQueryException e) {
    		LOG.error(e.getMessage());
    		result.reject("error.savenursery.failed");
    		return super.show(model);
    	}
    	
        return "redirect:" + SuccessfulController.URL;
    }
    
    private Workbook getWorkbook() {
        UserSelection userSelection = getUserSelection();
        Workbook workbook = userSelection.getWorkbook();

        //TODO: CLEAN THIS SECTION UP, THIS IS FOR TESTING ONLY.
        //test, for testing step 4 stand-alone
        if (workbook == null) {
            workbook = new Workbook();
            //workbook = TestNurseryWorkbookUtil.getTestWorkbook();
            userSelection.setWorkbook(workbook);
            //TODO: workbook can't be null, throw an exception
        }

        //Simulate Step 3 - generating / saving measurements data (TO BE REMOVED)
        if (workbook.getObservations() == null || workbook.getObservations().size() == 0) {
            workbook.setObservations(measurementsGeneratorService.generateMeasurementRows(userSelection));
        }
        
        return workbook;
    }
    
    public void setStudyDetails(SaveNurseryForm form, Workbook workbook) {
        if (workbook.getStudyDetails() == null) {
            workbook.setStudyDetails(new StudyDetails());
        }
        StudyDetails studyDetails = workbook.getStudyDetails();
        studyDetails.setTitle(form.getTitle());
        studyDetails.setObjective(form.getObjective());
        studyDetails.setStudyName(form.getNurseryBookName());
        studyDetails.setStudyType(StudyType.N);
        
        //TODO: save parent id, currently not implemented yet in UI
        //studyDetails.setParentFolderId(form.getParentFolder());
        //for testing set to the default folder
        studyDetails.setParentFolderId(1);
    }

}