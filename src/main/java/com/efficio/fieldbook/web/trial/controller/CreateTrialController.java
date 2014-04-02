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
package com.efficio.fieldbook.web.trial.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.TrialDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class CreateTrialController.
 */
@Controller
@RequestMapping(CreateTrialController.URL)
public class CreateTrialController extends SettingsController {
	
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(CreateTrialController.class);

    /** The Constant URL. */
    public static final String URL = "/TrialManager/createTrial";
    
    /** The Constant URL_SETTINGS. */
    public static final String URL_SETTINGS = "/TrialManager/chooseSettings";
	
   
	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "TrialManager/createTrial";
	}

    

    /**
     * Use existing Trial.
     *
     * @param form the form
     * @param TrialId the Trial id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/Trial/{TrialId}", method = RequestMethod.GET)
    public String useExistingTrial(@ModelAttribute("manageSettingsForm") CreateTrialForm form, @PathVariable int TrialId
            , Model model, HttpSession session) throws MiddlewareQueryException{
        if(TrialId != 0){
            /*
            Workbook workbook = fieldbookMiddlewareService.getTrialVariableSettings(TrialId);
            Dataset dataset = SettingsUtil.convertWorkbookToXmlDataset(workbook);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
            
            //Trial-level
            List<SettingDetail> TrialLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_Trial_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_Trial_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_Trial_REQUIRED_FIELDS.getString()), 
                    userSelection.getStudyLevelConditions(), true);
            
            //plot-level
            List<SettingDetail> plotLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
                    userSelection.getPlotsLevelList(), false);
            
            
            
            userSelection.setStudyLevelConditions(TrialLevelConditions);
            userSelection.setPlotsLevelList(plotLevelConditions);
            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
            //form.setSelectedSettingId(1);
            form.setLoadSettings("1");
            form.setRequiredFields(AppConstants.CREATE_Trial_REQUIRED_FIELDS.getString());
            */
        }
        setFormStaticData(form);
        model.addAttribute("createTrialForm", form);
        model.addAttribute("settingsList", getTrialSettingsList());
        model.addAttribute("TrialList", getTrialList());
        //setupFormData(form);
        return super.showAjaxPage(model, URL_SETTINGS);
    }
    
    /**
     * Show.
     *
     * @param form the form
     * @param form2 the form2
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("createTrialForm") CreateTrialForm form, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, Model model, HttpSession session) throws MiddlewareQueryException{
    	session.invalidate();
    	form.setProjectId(this.getCurrentProjectId());
    	//form.setRequiredFields(AppConstants.CREATE_Trial_REQUIRED_FIELDS.getString());
    	setFormStaticData(form);
    	return super.show(model);
    }

    /**
     * View settings.
     *
     * @param form the form
     * @param templateSettingId the template setting id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/view/{templateSettingId}", method = RequestMethod.POST)
    public String viewSettings(@ModelAttribute("createTrialForm") CreateTrialForm form, @PathVariable int templateSettingId, 
    	Model model, HttpSession session) throws MiddlewareQueryException{
    	if(templateSettingId != 0){    	
            TemplateSetting templateSettingFilter = new TemplateSetting(Integer.valueOf(templateSettingId), Integer.valueOf(getCurrentProjectId()), null, getTrialTool(), null, null);
            templateSettingFilter.setIsDefaultToNull();
            List<TemplateSetting> templateSettings = workbenchService.getTemplateSettings(templateSettingFilter);
            TemplateSetting templateSetting = templateSettings.get(0); //always 1
            TrialDataset dataset = (TrialDataset)SettingsUtil.parseXmlToDatasetPojo(templateSetting.getConfiguration(), false);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
            form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());
            form.setTrialEnvironmentValues(userSelection.getTrialEnvironmentValues());
            form.setTrialInstances(1);
            form.setSelectedSettingId(templateSetting.getTemplateSettingId());
    	}
    	form.setLoadSettings("1");
    	setFormStaticData(form);
        return super.showAjaxPage(model, URL_SETTINGS );
    }

    /**
     * Submit.
     *
     * @param form the form
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String submit(@ModelAttribute("createTrialForm") CreateTrialForm form, Model model) throws MiddlewareQueryException {
    	
    	String name = null;
    	for (SettingDetail nvar : form.getStudyLevelVariables()) {
    		if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
    			name = nvar.getValue();
    			break;
    		}
    	}

    	Dataset dataset = (Dataset)SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, form.getStudyLevelVariables(), form.getPlotLevelVariables(), form.getBaselineTraitVariables(), userSelection);
    	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset);
    	userSelection.setWorkbook(workbook);
    	
    	createStudyDetails(workbook, form.getStudyLevelVariables(), form.getFolderId());
 
    	return "success";
    }
    
    /**
     * Creates the study details.
     *
     * @param workbook the workbook
     * @param conditions the conditions
     * @param folderId the folder id
     */
    private void createStudyDetails(Workbook workbook, List<SettingDetail> conditions, Integer folderId) {
        if (workbook.getStudyDetails() == null) {
            workbook.setStudyDetails(new StudyDetails());
        }
        StudyDetails studyDetails = workbook.getStudyDetails();

        if (conditions != null && !conditions.isEmpty()) {
	        studyDetails.setTitle(getSettingDetailValue(conditions, TermId.STUDY_TITLE.getId()));
	        studyDetails.setObjective(getSettingDetailValue(conditions, TermId.STUDY_OBJECTIVE.getId()));
	        studyDetails.setStudyName(getSettingDetailValue(conditions, TermId.STUDY_NAME.getId()));
	        studyDetails.setStudyType(StudyType.N);
	        
	        if (folderId != null) {
	        	studyDetails.setParentFolderId(folderId);
	        }
    	}
        studyDetails.print(1);
    }
    
    /**
     * Gets the setting detail value.
     *
     * @param details the details
     * @param termId the term id
     * @return the setting detail value
     */
    private String getSettingDetailValue(List<SettingDetail> details, int termId) {
    	String value = null;
    	
    	for (SettingDetail detail : details) {
    		if (detail.getVariable().getCvTermId().equals(termId)) {
    			value = detail.getValue();
    			break;
    		}
    	}
    	
    	return value;
    }
    
    /**
     * Sets the form static data.
     *
     * @param form the new form static data
     */
    private void setFormStaticData(CreateTrialForm form){
        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setBreedingMethodUrl(AppConstants.BREEDING_METHOD_URL.getString());
        form.setLocationUrl(AppConstants.LOCATION_URL.getString());
        form.setProjectId(this.getCurrentProjectId());
        form.setImportLocationUrl(AppConstants.IMPORT_GERMPLASM_URL.getString());
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
        form.setStartDateId(AppConstants.START_DATE_ID.getString());
    	form.setEndDateId(AppConstants.END_DATE_ID.getString());
    }
}
