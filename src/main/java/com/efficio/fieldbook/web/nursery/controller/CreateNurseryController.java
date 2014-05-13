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

import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
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
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * The Class CreateNurseryController.
 */
@Controller
@RequestMapping(CreateNurseryController.URL)
public class CreateNurseryController extends SettingsController {
	
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(CreateNurseryController.class);

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/createNursery";
    
    /** The Constant URL_SETTINGS. */
    public static final String URL_SETTINGS = "/NurseryManager/chooseSettings";
	
   
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
	return "NurseryManager/ver2.0/createNursery";
    }

    /**
     * Use existing nursery.
     *
     * @param form the form
     * @param nurseryId the nursery id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/nursery/{nurseryId}", method = RequestMethod.GET)
    public String useExistingNursery(@ModelAttribute("manageSettingsForm") CreateNurseryForm form, @PathVariable int nurseryId
            , Model model, HttpSession session) throws MiddlewareQueryException{
        if(nurseryId != 0){     
            Workbook workbook = fieldbookMiddlewareService.getStudyVariableSettings(nurseryId, true);
            Dataset dataset = (Dataset)SettingsUtil.convertWorkbookToXmlDataset(workbook);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
            
            //nursery-level
            List<SettingDetail> nurseryLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()), 
                    userSelection.getStudyLevelConditions(), true);
            
            //plot-level
            List<SettingDetail> plotLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
                    userSelection.getPlotsLevelList(), false);
            
            
            
            userSelection.setStudyLevelConditions(nurseryLevelConditions);
            userSelection.setPlotsLevelList(plotLevelConditions);
            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
            //form.setSelectedSettingId(1);
            form.setLoadSettings("1");
            form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString());
        }
        setFormStaticData(form);
        model.addAttribute("createNurseryForm", form);
        model.addAttribute("settingsList", getNurserySettingsList());
        model.addAttribute("nurseryList", getNurseryList());
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
    public String show(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, Model model, HttpSession session) throws MiddlewareQueryException{
    	session.invalidate();
    	form.setProjectId(this.getCurrentProjectId());
    	form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString());
    	form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
    	setFormStaticData(form);
    	assignDefaultValues(form);
    	return super.show(model);
    }
    
    /**
     * Assign default values.
     *
     * @param form the form
     * @throws MiddlewareQueryException the middleware query exception
     */
    private void assignDefaultValues(CreateNurseryForm form) throws MiddlewareQueryException {
        List<SettingDetail> nurseryDefaults = new ArrayList<SettingDetail>();
        List<SettingDetail> plotDefaults = new ArrayList<SettingDetail>();
        List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
        form.setStudyLevelVariables(nurseryDefaults);
        form.setPlotLevelVariables(plotDefaults);
        nurseryDefaults = buildDefaultVariables(nurseryDefaults, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true));
        this.userSelection.setStudyLevelConditions(nurseryDefaults);
        plotDefaults = buildDefaultVariables(plotDefaults, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false));
        this.userSelection.setPlotsLevelList(plotDefaults);
        this.userSelection.setBaselineTraitsList(baselineTraitsList);
        
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
    public String viewSettings(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @PathVariable int templateSettingId, 
    	Model model, HttpSession session) throws MiddlewareQueryException{
    	
    	if(templateSettingId != 0){    	
	    	TemplateSetting templateSettingFilter = new TemplateSetting(Integer.valueOf(templateSettingId), Integer.valueOf(getCurrentProjectId()), null, getNurseryTool(), null, null);
	    	templateSettingFilter.setIsDefaultToNull();
	    	List<TemplateSetting> templateSettings = workbenchService.getTemplateSettings(templateSettingFilter);
	    	TemplateSetting templateSetting = templateSettings.get(0); //always 1
	    	Dataset dataset = SettingsUtil.parseXmlToDatasetPojo(templateSetting.getConfiguration());
	    	SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
	    	form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
	    	form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
	    	form.setPlotLevelVariables(userSelection.getPlotsLevelList());
//	    	form.setIsDefault(templateSetting.getIsDefault().intValue() == 1 ? true : false);
//	    	form.setSettingName(templateSetting.getName());
	    	form.setSelectedSettingId(templateSetting.getTemplateSettingId());
	    	form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString());
//    	}else{
//    		assignDefaultValues(form);
    	}
//    	model.addAttribute("createNurseryForm", form);
//    	model.addAttribute("settingsList", getSettingsList());
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
    public String submit(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
    	
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
    private void setFormStaticData(CreateNurseryForm form){
        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setBreedingMethodUrl(AppConstants.BREEDING_METHOD_URL.getString());
        form.setLocationUrl(AppConstants.LOCATION_URL.getString());
        form.setProjectId(this.getCurrentProjectId());
        form.setImportLocationUrl(AppConstants.IMPORT_GERMPLASM_URL.getString());
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
        form.setStartDateId(AppConstants.START_DATE_ID.getString());
    	form.setEndDateId(AppConstants.END_DATE_ID.getString());
    	form.setOpenGermplasmUrl(AppConstants.GERMPLASM_DETAILS_URL.getString());
    }
}
