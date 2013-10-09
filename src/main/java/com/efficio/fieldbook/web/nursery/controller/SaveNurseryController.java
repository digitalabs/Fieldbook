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
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.service.api.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
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

/**
 * The Class SaveNurseryController.
 */
@Controller
@RequestMapping(SaveNurseryController.URL)
public class SaveNurseryController extends AbstractBaseFieldbookController{
	
	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(SaveNurseryController.class);

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/saveNursery";

    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    
    /** The data import service. */
    @Resource
    private DataImportService dataImportService;
    
    /** The measurements generator service. */
    @Resource
    private MeasurementsGeneratorService measurementsGeneratorService;
    
    /** The message source. */
    @Resource
    private ResourceBundleMessageSource messageSource;
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/saveNursery";
    }
    
    /**
     * Gets the user selection.
     *
     * @return the user selection
     */
    public UserSelection getUserSelection() {
        return this.userSelection;
    }

    /**
     * Shows the screen.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("saveNurseryForm") SaveNurseryForm form, Model model) {
    	
    	return super.show(model);
    }

    
    /**
     * Save nursery.
     *
     * @param title the title
     * @param objective the objective
     * @param nurseryBookName the nursery book name
     * @return the map
     */
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
    
    /**
     * Gets the workbook.
     *
     * @return the workbook
     */
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
    
    /**
     * Validate.
     *
     * @param title the title
     * @param objective the objective
     * @param nurseryBookName the nursery book name
     * @return the string
     */
    private String validate(String title, String objective, String nurseryBookName) {
        Locale locale = LocaleContextHolder.getLocale();
        StringBuilder errorMessages = null;
        
        //Required Field Checks
        StringBuilder requiredFields = null;
        if (StringUtils.isBlank(title)) {
            requiredFields = requiredFields == null ? new StringBuilder() : requiredFields.append(", ");
            requiredFields.append(messageSource.getMessage("nursery.savenursery.title", null, locale));
        }
        if (StringUtils.isBlank(objective)) {
            requiredFields = requiredFields == null ? new StringBuilder() : requiredFields.append(", ");
            requiredFields.append(messageSource.getMessage("nursery.savenursery.objective", null, locale));
        }
        if (StringUtils.isBlank(nurseryBookName)) {
            requiredFields = requiredFields == null ? new StringBuilder() : requiredFields.append(", ");
            requiredFields.append(messageSource.getMessage("nursery.savenursery.nurseryBookName", null, locale));
        }
        if (requiredFields != null) {
            errorMessages = errorMessages == null ? new StringBuilder() : errorMessages.append("<br />");
            errorMessages.append(messageSource.getMessage("error.mandatory.field", new String[] {requiredFields.toString()}, locale));
        }
        
        return errorMessages != null ? errorMessages.toString() : null;
    }

    /**
     * Sets the study details.
     *
     * @param title the title
     * @param objective the objective
     * @param nurseryBookName the nursery book name
     * @param workbook the workbook
     */
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