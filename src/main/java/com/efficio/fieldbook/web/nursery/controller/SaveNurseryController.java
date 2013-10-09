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

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.service.api.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.SaveNurseryForm;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;

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
    private MeasurementsGeneratorService measurementsGeneratorService;
    
    @Resource
    private ResourceBundleMessageSource messageSource;
    
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
    public @ResponseBody Map<String, String> saveNursery(@RequestParam String title, @RequestParam String objective,
            @RequestParam String nurseryBookName) {
    	
        Map<String, String> resultMap = new HashMap<String, String>();
        
        Workbook workbook = getWorkbook();
    	
        String errorMessages = validate(title, objective, nurseryBookName);
    	
        if (errorMessages != null) {
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", errorMessages);
            return resultMap;
        }

        try {
    	    setStudyDetails(title, objective, nurseryBookName, workbook);
    	    
    		dataImportService.saveDataset(workbook);
    		resultMap.put("status", "1");
    	
    	} catch(Exception e) {
    		LOG.error(e.getMessage());
    		resultMap.put("status", "-1");
    		resultMap.put("errorMessage", e.getMessage());
    	}
    	
    	return resultMap;
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
    
    private String validate(String title, String objective, String nurseryBookName) {
        StringBuilder errorMessages = null;
        StringBuilder requiredFields = null;
        if (StringUtils.isBlank(title)) {
            requiredFields = requiredFields == null ? new StringBuilder() : requiredFields.append(", ");
            requiredFields.append(messageSource.getMessage("nursery.savenursery.title", null, null));
        }
        if (StringUtils.isBlank(objective)) {
            requiredFields = requiredFields == null ? new StringBuilder() : requiredFields.append(", ");
            requiredFields.append(messageSource.getMessage("nursery.savenursery.objective", null, null));
        }
        if (StringUtils.isBlank(nurseryBookName)) {
            requiredFields = requiredFields == null ? new StringBuilder() : requiredFields.append(", ");
            requiredFields.append(messageSource.getMessage("nursery.savenursery.nurseryBookName", null, null));
        }
        if (requiredFields != null) {
            errorMessages = errorMessages == null ? new StringBuilder() : errorMessages.append("<br />");
            errorMessages.append(messageSource.getMessage("error.mandatory.field", new String[] {requiredFields.toString()}, null));
        }
        return errorMessages != null ? errorMessages.toString() : null;
    }

    public void setStudyDetails(String title, String objective, String nurseryBookName, Workbook workbook) {
        if (workbook.getStudyDetails() == null) {
            workbook.setStudyDetails(new StudyDetails());
        }
        StudyDetails studyDetails = workbook.getStudyDetails();
        studyDetails.setTitle(title);
        studyDetails.setObjective(objective);
        studyDetails.setStudyName(nurseryBookName);
        studyDetails.setStudyType(StudyType.N);
        
        //TODO: save parent id, currently not implemented yet in UI
        //studyDetails.setParentFolderId(form.getParentFolder());
        //for testing set to the default folder
        studyDetails.setParentFolderId(1);
    }

}