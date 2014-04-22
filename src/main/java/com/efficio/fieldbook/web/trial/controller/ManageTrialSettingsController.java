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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.TreatmentFactor;
import org.generationcp.middleware.pojos.workbench.settings.TrialDataset;
import org.generationcp.middleware.service.api.OntologyService;
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
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.trial.form.ManageSettingsForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.TreeViewUtil;

/**
 * The Class SaveAdvanceNurseryController.
 */
@Controller
@RequestMapping(ManageTrialSettingsController.URL)
public class ManageTrialSettingsController extends SettingsController{

    /** The Constant URL. */
    public static final String URL = "/TrialManager/manageTrialSettings";
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ManageTrialSettingsController.class);    
    
    
    @Resource
    private OntologyService ontologyService;
         
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "TrialManager/manageTrialSettings";
    }                    
    
    private void setFormStaticData(ManageSettingsForm form){
    	form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
    	form.setLocationId(AppConstants.LOCATION_ID.getString());
    	form.setBreedingMethodUrl(AppConstants.BREEDING_METHOD_URL.getString());
    	form.setLocationUrl(AppConstants.LOCATION_URL.getString());
    	form.setProjectId(this.getCurrentProjectId());
    	form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
    	form.setStartDateId(AppConstants.START_DATE_ID.getString());
    	form.setEndDateId(AppConstants.END_DATE_ID.getString());
    }
    
    /**
     * Shows the screen.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("manageSettingsForm") ManageSettingsForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{
    	session.invalidate();
    	
    	
        
    	//we need to get the default settings if there is
        //only has value for clear setting, the rest null            	
    	setupDefaultScreenValues(form, getDefaultTemplateSettingFilter());
    	form.setProjectId(this.getCurrentProjectId());
    	
    	//setupFormData(form);
    	return super.show(model);
    }
    
    /**
     * Sets the up default screen values.
     *
     * @param form the new up default screen values
     * @throws MiddlewareQueryException the middleware query exception
     */
    private void setupDefaultScreenValues(ManageSettingsForm form, TemplateSetting templateSettingFilter) throws MiddlewareQueryException{

    	setFormStaticData(form);
        List<TemplateSetting> templateSettingsList = new ArrayList<TemplateSetting>();
        //NULL when its an add new setting
        if(templateSettingFilter != null) 
        	templateSettingsList = workbenchService.getTemplateSettings(templateSettingFilter);
        
        if(templateSettingsList != null && !templateSettingsList.isEmpty()){
        	//we only get the 1st, cause its always gonna be 1 only per project and per tool
        	TemplateSetting templateSetting = templateSettingsList.get(0); //always 1
        	TrialDataset dataset = (TrialDataset) SettingsUtil.parseXmlToDatasetPojo(templateSetting.getConfiguration(), false);
        	SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
        	form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
        	form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
        	form.setPlotLevelVariables(userSelection.getPlotsLevelList());
        	form.setIsDefault(templateSetting.getIsDefault().intValue() == 1 ? true : false);
        	form.setSettingName(templateSetting.getName());
        	form.setSelectedSettingId(templateSetting.getTemplateSettingId());        	
	    	form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());
	    	form.setTreatmentFactors(userSelection.getTreatmentFactors());
        }
        else {
        	assignDefaultValues(form);
        }
    }
          
    /**
     * Post advance nursery.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the map
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/save", method = RequestMethod.POST)
    public String saveSettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{
		//will do the saving here
    	
    	TrialDataset dataset = (TrialDataset)SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, form.getSettingName(), 
    			form.getStudyLevelVariables(), form.getPlotLevelVariables(), form.getBaselineTraitVariables(), 
    			userSelection, form.getTrialLevelVariables(), form.getTreatmentFactors());
    	String xml = SettingsUtil.generateSettingsXml(dataset);
    	Integer tempateSettingId = form.getSelectedSettingId() > 0 ? Integer.valueOf(form.getSelectedSettingId()) : null;
    	TemplateSetting templateSetting = new TemplateSetting(tempateSettingId, Integer.valueOf(getCurrentProjectId()), dataset.getName(), getTrialTool(), xml, Boolean.valueOf(form.getIsDefault())) ;
    	int tempateSettingsId = templateSetting.getTemplateSettingId() != null ? templateSetting.getTemplateSettingId() : 0;
    	
    	if(templateSetting.getTemplateSettingId() == null){
    		
    		Integer newTemplateSettingsId = workbenchService.addTemplateSetting(templateSetting);
    		templateSetting.setTemplateSettingId(newTemplateSettingsId);
    		tempateSettingsId = newTemplateSettingsId;
    	}
    	else
    		workbenchService.updateTemplateSetting(templateSetting);
    	
    	
    	return viewSettings(form, tempateSettingsId, model, session);
    }
    
    /**
     * For deletion of nursery setting.
     *
     * @param form the form
     * @param templateSettingId the template setting id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/delete/{templateSettingId}", method = RequestMethod.POST)
    public String deleteSettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form, @PathVariable int templateSettingId
            , Model model, HttpSession session) throws MiddlewareQueryException{
		//will do the saving here
    	workbenchService.deleteTemplateSetting(Integer.valueOf(templateSettingId));
    	//need to add here the cleanup in the session and in the form
    	//we need to get the default settings if there is
        //only has value for clear setting, the rest null
        
    	    	
    	setupDefaultScreenValues(form, getDefaultTemplateSettingFilter());
    	model.addAttribute("settingsTrialList", getTrialSettingsList());
    	//setupFormData(form);
    	return super.showAjaxPage(model, getContentName() );
    }
    
    private TemplateSetting getDefaultTemplateSettingFilter(){
    	return new TemplateSetting(null, Integer.valueOf(getCurrentProjectId()), null, getTrialTool(), null, true);
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
    @RequestMapping(value="/view/{templateSettingId}", method = RequestMethod.GET)
    public String viewSettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form, @PathVariable int templateSettingId
            , Model model, HttpSession session) throws MiddlewareQueryException{
		//will do the saving here
    	setFormStaticData(form);
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
	    	form.setTreatmentFactors(userSelection.getTreatmentFactors());
	    	form.setIsDefault(templateSetting.getIsDefault().intValue() == 1 ? true : false);
	    	form.setSettingName(templateSetting.getName());
	    	form.setSelectedSettingId(templateSetting.getTemplateSettingId());
    	}else{
    		assignDefaultValues(form);
    	}
    	model.addAttribute("manageSettingsForm", form);
    	model.addAttribute("settingsTrialList", getTrialSettingsList());
    	//setupFormData(form);
        return super.showAjaxPage(model, getContentName() );
    }
    
    
    
    @RequestMapping(value="/trial/{trialId}", method = RequestMethod.GET)
    public String useExistingTrial(@ModelAttribute("manageSettingsForm") ManageSettingsForm form, @PathVariable int trialId
            , Model model, HttpSession session) throws MiddlewareQueryException{
        if(trialId != 0){     
            Workbook workbook = fieldbookMiddlewareService.getStudyVariableSettings(trialId, false);
            TrialDataset dataset = (TrialDataset)SettingsUtil.convertWorkbookToXmlDataset(workbook, false);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
            
            //study-level
            List<SettingDetail> studyLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString()), 
                    userSelection.getStudyLevelConditions(), true);
            
            //plot-level
            List<SettingDetail> plotLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString(), false), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString()), 
                    userSelection.getPlotsLevelList(), false);
            
            List<SettingDetail> trialLevelVariableList = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()), 
                    userSelection.getTrialLevelVariableList(), true);
            
            userSelection.setStudyLevelConditions(studyLevelConditions);
            userSelection.setPlotsLevelList(plotLevelConditions);
            userSelection.setTrialLevelVariableList(trialLevelVariableList);
            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
            form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());
            form.setIsDefault(false);
            form.setSettingName("");
        }
        
        model.addAttribute("manageSettingsForm", form);
        model.addAttribute("settingsTrialList", getTrialSettingsList());
        model.addAttribute("trialList", getTrialList());
        setFormStaticData(form);
        //setupFormData(form);
        return super.showAjaxPage(model, getContentName() );
    }
    
    @RequestMapping(value="/copy/{templateSettingId}", method = RequestMethod.GET)
    public String copySettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form, @PathVariable int templateSettingId
            , Model model, HttpSession session) throws MiddlewareQueryException{
		//will do the saving here
    	
    	if(templateSettingId != 0){    	
	    	TemplateSetting templateSettingFilter = new TemplateSetting(Integer.valueOf(templateSettingId), Integer.valueOf(getCurrentProjectId()), null, getTrialTool(), null, null);
	    	templateSettingFilter.setIsDefaultToNull();
	    	List<TemplateSetting> templateSettings = workbenchService.getTemplateSettings(templateSettingFilter);
	    	TemplateSetting templateSetting = templateSettings.get(0); //always 1
	    	
	    	boolean isNameUniqueAlready = false;
	    	String newSettingsName = "";
	    	int index = 1;
	    	do{
	    		newSettingsName = templateSetting.getName() + " ("+index+")";
	    		//this is to search for duplicatename
	    		templateSettingFilter.setTemplateSettingId(null);
	    		templateSettingFilter.setName(newSettingsName);
	    		templateSettingFilter.setIsDefaultToNull();
	    	templateSettings = workbenchService.getTemplateSettings(templateSettingFilter);
	    	index++;
	    	if(templateSettings == null || templateSettings.isEmpty())
	    		isNameUniqueAlready = true;
	    	
	    	}while(!isNameUniqueAlready);
	    	//we need to make sure name is unique
	    	
	    	//then we make a copy
	    	TemplateSetting newTemplateSetting = new TemplateSetting(null, Integer.valueOf(getCurrentProjectId()), newSettingsName, getTrialTool(), templateSetting.getConfiguration(), false);
	    	int copiedTemplateSettingId = workbenchService.addTemplateSetting(newTemplateSetting);
	    	
	    	return viewSettings(form, copiedTemplateSettingId, model, session);
    	}else{
    		assignDefaultValues(form);
    	}
    	model.addAttribute("manageSettingsForm", form);
    	model.addAttribute("settingsTrialList", getTrialSettingsList());
    	setFormStaticData(form);
    	//setupFormData(form);
        return super.showAjaxPage(model, getContentName() );
    }
    
    /**
     * Displays the Add Setting popup.
     *
     * @param mode the mode
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "displayAddSetting/{mode}", method = RequestMethod.GET)
    public Map<String, Object> showAddSettingPopup(@PathVariable int mode) {
		Map<String, Object> result = new HashMap<String, Object>();
    	try {

        	List<StandardVariableReference> standardVariableList = 
        			fieldbookService.filterStandardVariablesForTrialSetting(mode, getSettingDetailList(mode));
        	
        	try{
        		if(userSelection.getTraitRefList() == null){
        			List<TraitClassReference> traitRefList = (List<TraitClassReference>) 
        	                ontologyService.getAllTraitGroupsHierarchy(true);
        			userSelection.setTraitRefList(traitRefList);
        		}
        			List<TraitClassReference> traitRefList = userSelection.getTraitRefList();
        			//we convert it to map so that it would be easier to chekc if there is a record or not
        			HashMap<String, StandardVariableReference> mapVariableRef = new HashMap<String, StandardVariableReference>();
        			if(standardVariableList != null && !standardVariableList.isEmpty()){
        				for(StandardVariableReference varRef: standardVariableList){
        					mapVariableRef.put(varRef.getId().toString(), varRef);
        				}
        			}
        			
        			String treeData = TreeViewUtil.convertOntologyTraitsToJson(traitRefList, mapVariableRef);
        	        String searchTreeData = TreeViewUtil.convertOntologyTraitsToSearchSingleLevelJson(traitRefList, mapVariableRef);
        	        result.put("treeData", treeData);
        	        result.put("searchTreeData", searchTreeData);
        		
    	        //form.setTraitClassReferenceList(traitRefList);
    	        
        	}catch(Exception e){
        		LOG.error(e.getMessage());
        	}
        	/*
        	if (standardVariableList != null && !standardVariableList.isEmpty()) {
        		ObjectMapper om = new ObjectMapper();
        		return om.writeValueAsString(standardVariableList);
        	}
			*/
    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	
    	//return "[]";
    	return result;
    }
    
    /**
     * Show variable details.
     *
     * @param id the id
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value="showVariableDetails/{id}", method = RequestMethod.GET)
    public String showVariableDetails(@PathVariable int id) {
    	try {

    		SettingVariable svar = getSettingVariable(id);
    		if (svar != null) {
    			ObjectMapper om = new ObjectMapper();
    			return om.writeValueAsString(svar);
    		}
    		
    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	return "[]";
    }
    
    /**
     * Adds the settings.
     *
     * @param form the form
     * @param model the model
     * @param mode the mode
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/addSettings/{mode}", method = RequestMethod.POST)
    public String addSettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form, 
            Model model, @PathVariable int mode) {
    	List<SettingDetail> newSettings = new ArrayList<SettingDetail>();
    	try {
	    	List<SettingVariable> selectedVariables = form.getSelectedVariables();
	    	if (selectedVariables != null && !selectedVariables.isEmpty()) {
	    		int ctr = getLastGroupNumber() * 2;
	    		for (SettingVariable var : selectedVariables) {
	    			populateSettingVariable(var);
					List<ValueReference> possibleValues = 
					        fieldbookService.getAllPossibleValues(var.getCvTermId());
					SettingDetail newSetting = new SettingDetail(var, possibleValues, null, true);
					List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(var.getCvTermId(), this.getCurrentProjectId());
					newSetting.setPossibleValuesFavorite(possibleValuesFavorite);
					newSetting.setPossibleValuesToJson(possibleValues);
					newSetting.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
					newSetting.setGroup(ctr / 2 + 1);
					newSettings.add(newSetting);
					ctr++;
	    		}
	    	}
	    	
	    	if (newSettings != null && !newSettings.isEmpty()) {
	    		return addNewSettingDetails(form, mode, newSettings);
	    	}
	    	
    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	
    	return "[]";
    }
    
    @ResponseBody
    @RequestMapping(value = "/deleteVariable/{mode}/{variableId}", method = RequestMethod.POST)
    public String deleteVariable(@ModelAttribute("manageSettingsForm") ManageSettingsForm form, Model model, 
            @PathVariable int mode, @PathVariable int variableId) {
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            //form.getNurseryLevelVariables()
            deleteVariableInSession(userSelection.getStudyLevelConditions(), variableId);
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            deleteVariableInSession(userSelection.getPlotsLevelList(), variableId);
        } else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
            deleteVariableInSession(userSelection.getTrialLevelVariableList(), variableId);
        } else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
        	int group = 0;
        	for (SettingDetail treatmentFactor : userSelection.getTreatmentFactors()) {
        		if (treatmentFactor.getVariable().getCvTermId().equals(variableId)) {
        			group = treatmentFactor.getGroup();
        			break;
        		}
        	}
        	deleteVariablesInSessionByGroup(userSelection.getTreatmentFactors(), group);
        } else {
            deleteVariableInSession(userSelection.getBaselineTraitsList(), variableId);
        }
        return "";
    }
    
    private void deleteVariableInSession(List<SettingDetail> variableList, int variableId) {
        Iterator<SettingDetail> iter = variableList.iterator();
        while (iter.hasNext()) {
            if (iter.next().getVariable().getCvTermId().equals(Integer.valueOf(variableId))) {
                iter.remove();
            }
        }
    }
    
    private void deleteVariablesInSessionByGroup(List<SettingDetail> variableList, int groupId) {
    	List<SettingDetail> newDetails = new ArrayList<SettingDetail>();
        Iterator<SettingDetail> iter = variableList.iterator();
        while (iter.hasNext()) {
        	SettingDetail detail = iter.next();
            if (detail.getGroup().equals(groupId)) {
                //iter.remove();
            	
            }
            else {
            	newDetails.add(detail);
            }
        }
        userSelection.setTreatmentFactors(newDetails);
    }
    
    /**
     * Clear settings.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(value = "clearSettings/{templateSettingId}", method = RequestMethod.GET)
    public String clearSettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form,
    		@PathVariable int templateSettingId,
    		Model model, HttpSession session) {
    	
    	try {
	    	//session.invalidate();
	    	form.clear();
	    	form.setSelectedSettingId(templateSettingId);
	    	//we need to get the default settings if there is
	        //only has value for clear setting, the rest null
	        Integer templateSettingIdFilter = form.getSelectedSettingId() > 0 ? 
	                    Integer.valueOf(form.getSelectedSettingId()) : null;
	    	TemplateSetting templateSettingFilter = getDefaultTemplateSettingFilter();
	    	//if there is an id query, we need to set the isDefault filter to  null
	    	if(templateSettingIdFilter != null){
	    		templateSettingFilter.setIsDefaultToNull();
	    		templateSettingFilter.setTemplateSettingId(templateSettingIdFilter);
	    	}
	    	
	    	setupDefaultScreenValues(form, templateSettingFilter);
	    	//assignDefaultValues(form);
    	
    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	setFormStaticData(form);
    	
    	//return super.show(model);
    	return super.showAjaxPage(model, getContentName() );
    	//return "redirect: " + ManageNurseriesController.URL;
    }
    
    @RequestMapping(value = "addNewSettings", method = RequestMethod.GET)
    public String addNewSettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form, 
            Model model, HttpSession session) {
    	
    	try {
	    	//session.invalidate();
	    	form.clear();	    		    	
	    	setupDefaultScreenValues(form, null);
	    	//assignDefaultValues(form);
    	
    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	
    	//return super.show(model);
    	return super.showAjaxPage(model, getContentName() );
    	//return "redirect: " + ManageNurseriesController.URL;
    }
    
    
    /**
     * Assign default values.
     *
     * @param form the form
     * @throws MiddlewareQueryException the middleware query exception
     */
    private void assignDefaultValues(ManageSettingsForm form) throws MiddlewareQueryException {
    	List<SettingDetail> nurseryDefaults = new ArrayList<SettingDetail>();
    	List<SettingDetail> plotDefaults = new ArrayList<SettingDetail>();
    	List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
    	List<SettingDetail> trialLevelVariableList = new ArrayList<SettingDetail>();
    	form.setStudyLevelVariables(nurseryDefaults);
    	form.setPlotLevelVariables(plotDefaults);
    	form.setTrialLevelVariables(trialLevelVariableList);
    	form.setSettingName("");
    	form.setIsDefault(false);
    	nurseryDefaults = buildDefaultVariables(nurseryDefaults, AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString(), true));
    	this.userSelection.setStudyLevelConditions(nurseryDefaults);
    	plotDefaults = buildDefaultVariables(plotDefaults, AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString(), false));    	
    	this.userSelection.setPlotsLevelList(plotDefaults);
    	
    	this.userSelection.setBaselineTraitsList(baselineTraitsList);
    	
    	trialLevelVariableList = buildDefaultVariables(trialLevelVariableList, AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString(), true));
    	this.userSelection.setTrialLevelVariableList(trialLevelVariableList);
    	this.userSelection.setTreatmentFactors(null);
    }
    
    /**
     * Gets the setting detail list.
     *
     * @param mode the mode
     * @return the setting detail list
     */
    private List<SettingDetail> getSettingDetailList(int mode) {
    	if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            return userSelection.getStudyLevelConditions();
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt() || mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
        	List<SettingDetail> list = new ArrayList<SettingDetail>();
        	list.addAll(userSelection.getPlotsLevelList());
        	if (userSelection.getTreatmentFactors() != null && !userSelection.getTreatmentFactors().isEmpty()) {
        		list.addAll(userSelection.getTreatmentFactors());
        	}
            return list;
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt()) {
            return userSelection.getBaselineTraitsList();
        }  else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
            return userSelection.getTrialLevelVariableList();
        }
    	return null;
    }
    
    /**
     * Adds the new setting details.
     *
     * @param form the form
     * @param mode the mode
     * @param newDetails the new details
     * @return the string
     * @throws Exception the exception
     */
    private String addNewSettingDetails(ManageSettingsForm form, int mode
            , List<SettingDetail> newDetails) throws Exception {
    	if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            if (form.getStudyLevelVariables() == null) {
            	form.setStudyLevelVariables(newDetails);
            }
            else {
            	form.getStudyLevelVariables().addAll(newDetails);
            }
            if (userSelection.getStudyLevelConditions() == null) {
            	userSelection.setStudyLevelConditions(newDetails);
            }
            else {
            	userSelection.getStudyLevelConditions().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            if (form.getPlotLevelVariables() == null) {
            	form.setPlotLevelVariables(newDetails);
            }
            else {
            	form.getPlotLevelVariables().addAll(newDetails);
            }
            if (userSelection.getPlotsLevelList() == null) {
            	userSelection.setPlotsLevelList(newDetails);
            }
            else {
            	userSelection.getPlotsLevelList().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
            if (form.getTrialLevelVariables() == null) {
            	form.setTrialLevelVariables(newDetails);
            }
            else {
            	form.getTrialLevelVariables().addAll(newDetails);
            }
            if (userSelection.getTrialLevelVariableList() == null) {
            	userSelection.setTrialLevelVariableList(newDetails);
            }
            else {
            	userSelection.getTrialLevelVariableList().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
        	if (form.getTreatmentFactors() == null) {
        		form.setTreatmentFactors(newDetails);
        	}
        	else {
        		form.getTreatmentFactors().addAll(newDetails);
        	}
        	if (userSelection.getTreatmentFactors() == null) {
        		userSelection.setTreatmentFactors(newDetails);
        	}
        	else {
        		userSelection.getTreatmentFactors().addAll(newDetails);
        	}
        }else {
            if (form.getBaselineTraitVariables() == null) {
            	form.setBaselineTraitVariables(newDetails);
            }
            else {
            	form.getBaselineTraitVariables().addAll(newDetails);
            }
            if (userSelection.getBaselineTraitsList() == null) {
            	userSelection.setBaselineTraitsList(newDetails);
            }
            else {
            	userSelection.getBaselineTraitsList().addAll(newDetails);
            }
        }
    	ObjectMapper om = new ObjectMapper();
    	return om.writeValueAsString(newDetails);
    }
    
    private int getLastGroupNumber() {
    	int group = 0;
    	
    	if (userSelection.getTreatmentFactors() != null && !userSelection.getTreatmentFactors().isEmpty()) {
    		return userSelection.getTreatmentFactors().get(userSelection.getTreatmentFactors().size()-1).getGroup();
    	}
    	
    	return group;
    }
    
}