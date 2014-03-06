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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.FieldbookService;
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

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ManageSettingsForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.TreeViewUtil;

/**
 * The Class SaveAdvanceNurseryController.
 */
@Controller
@RequestMapping(ManageNurserySettingsController.URL)
public class ManageNurserySettingsController extends AbstractBaseFieldbookController{

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/manageNurserySettings";
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ManageNurserySettingsController.class);
    
    /** The fieldbook middleware service. */
    @Resource
    private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;        
    
    /** The workbench data manager. */
    @Resource
    private WorkbenchService workbenchService;
    
    /** The fieldbook service. */
    @Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    
    @Resource
    private OntologyService ontologyService;
         
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/manageNurserySettings";
    }    
    
    /**
     * Gets the nursery tool.
     *
     * @return the nursery tool
     */
    private Tool getNurseryTool(){
    	Tool tool = null;
		try {
			tool = workbenchService.getToolWithName(
			        AppConstants.TOOL_NAME_NURSERY_MANAGER_WEB.getString());
		} catch (MiddlewareQueryException e) {
		    LOG.error(e.getMessage(), e);
		}
    	return tool;
    }
    
    /**
     * Gets the settings list.
     *
     * @return the settings list
     */
    @ModelAttribute("settingsList")
    public List<TemplateSetting> getSettingsList() {
        try {
        	//need to call the MW call passing the tool id and project id
        	//getCurrentProjectId()
        	
        	TemplateSetting templateSettingFilter = new TemplateSetting(null, Integer.valueOf(getCurrentProjectId()), null, getNurseryTool(), null, null);
        	templateSettingFilter.setIsDefaultToNull();
            List<TemplateSetting> templateSettingsList = workbenchService.getTemplateSettings(templateSettingFilter);
         // this is for the default
            templateSettingsList.add(0, new TemplateSetting(Integer.valueOf(0), Integer.valueOf(getCurrentProjectId()), "", null, "", false));
            return templateSettingsList;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
		
        return null;
    }
    
    /**
     * Gets the settings list.
     *
     * @return the settings list
     */
    @ModelAttribute("nurseryList")
    public List<StudyDetails> getNurseryList() {
        try {
            List<StudyDetails> nurseries = fieldbookMiddlewareService.getAllLocalNurseryDetails();
            return nurseries;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
                
        return null;
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

        List<TemplateSetting> templateSettingsList = new ArrayList<TemplateSetting>();
        //NULL when its an add new setting
        if(templateSettingFilter != null) 
        	templateSettingsList = workbenchService.getTemplateSettings(templateSettingFilter);
        
        if(templateSettingsList != null && !templateSettingsList.isEmpty()){
        	//we only get the 1st, cause its always gonna be 1 only per project and per tool
        	TemplateSetting templateSetting = templateSettingsList.get(0); //always 1
        	Dataset dataset = SettingsUtil.parseXmlToDatasetPojo(templateSetting.getConfiguration());
        	SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
        	form.setNurseryLevelVariables(userSelection.getNurseryLevelConditions());
        	form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
        	form.setPlotLevelVariables(userSelection.getPlotsLevelList());
        	form.setIsDefault(templateSetting.getIsDefault().intValue() == 1 ? true : false);
        	form.setSettingName(templateSetting.getName());
        	form.setSelectedSettingId(templateSetting.getTemplateSettingId());
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
    	
    	Dataset dataset = SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, form.getSettingName(), form.getNurseryLevelVariables(), form.getPlotLevelVariables(), form.getBaselineTraitVariables(), userSelection);
    	String xml = SettingsUtil.generateSettingsXml(dataset);
    	Integer tempateSettingId = form.getSelectedSettingId() > 0 ? Integer.valueOf(form.getSelectedSettingId()) : null;
    	TemplateSetting templateSetting = new TemplateSetting(tempateSettingId, Integer.valueOf(getCurrentProjectId()), dataset.getName(), getNurseryTool(), xml, Boolean.valueOf(form.getIsDefault())) ;
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
    	model.addAttribute("settingsList", getSettingsList());
    	//setupFormData(form);
    	return super.showAjaxPage(model, getContentName() );
    }
    
    private TemplateSetting getDefaultTemplateSettingFilter(){
    	return new TemplateSetting(null, Integer.valueOf(getCurrentProjectId()), null, getNurseryTool(), null, true);
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
    	
    	if(templateSettingId != 0){    	
	    	TemplateSetting templateSettingFilter = new TemplateSetting(Integer.valueOf(templateSettingId), Integer.valueOf(getCurrentProjectId()), null, getNurseryTool(), null, null);
	    	templateSettingFilter.setIsDefaultToNull();
	    	List<TemplateSetting> templateSettings = workbenchService.getTemplateSettings(templateSettingFilter);
	    	TemplateSetting templateSetting = templateSettings.get(0); //always 1
	    	Dataset dataset = SettingsUtil.parseXmlToDatasetPojo(templateSetting.getConfiguration());
	    	SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
	    	form.setNurseryLevelVariables(userSelection.getNurseryLevelConditions());
	    	form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
	    	form.setPlotLevelVariables(userSelection.getPlotsLevelList());
	    	form.setIsDefault(templateSetting.getIsDefault().intValue() == 1 ? true : false);
	    	form.setSettingName(templateSetting.getName());
	    	form.setSelectedSettingId(templateSetting.getTemplateSettingId());
    	}else{
    		assignDefaultValues(form);
    	}
    	model.addAttribute("manageSettingsForm", form);
    	model.addAttribute("settingsList", getSettingsList());
    	//setupFormData(form);
        return super.showAjaxPage(model, getContentName() );
    }
    
    @RequestMapping(value="/nursery/{nurseryId}", method = RequestMethod.GET)
    public String useExistingNursery(@ModelAttribute("manageSettingsForm") ManageSettingsForm form, @PathVariable int nurseryId
            , Model model, HttpSession session) throws MiddlewareQueryException{
        if(nurseryId != 0){     
            Workbook workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
            Dataset dataset = SettingsUtil.convertWorkbookToXmlDataset(workbook);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
            form.setNurseryLevelVariables(userSelection.getNurseryLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
            form.setIsDefault(false);
            form.setSettingName("");
        }
        
        model.addAttribute("manageSettingsForm", form);
        model.addAttribute("settingsList", getSettingsList());
        model.addAttribute("nurseryList", getNurseryList());
        //setupFormData(form);
        return super.showAjaxPage(model, getContentName() );
    }
    
    @RequestMapping(value="/copy/{templateSettingId}", method = RequestMethod.GET)
    public String copySettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form, @PathVariable int templateSettingId
            , Model model, HttpSession session) throws MiddlewareQueryException{
		//will do the saving here
    	
    	if(templateSettingId != 0){    	
	    	TemplateSetting templateSettingFilter = new TemplateSetting(Integer.valueOf(templateSettingId), Integer.valueOf(getCurrentProjectId()), null, getNurseryTool(), null, null);
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
	    	TemplateSetting newTemplateSetting = new TemplateSetting(null, Integer.valueOf(getCurrentProjectId()), newSettingsName, getNurseryTool(), templateSetting.getConfiguration(), false);
	    	int copiedTemplateSettingId = workbenchService.addTemplateSetting(newTemplateSetting);
	    	
	    	return viewSettings(form, copiedTemplateSettingId, model, session);
    	}else{
    		assignDefaultValues(form);
    	}
    	model.addAttribute("manageSettingsForm", form);
    	model.addAttribute("settingsList", getSettingsList());
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
        			fieldbookService.filterStandardVariablesForSetting(mode, getSettingDetailList(mode));
        	
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
	    		for (SettingVariable var : selectedVariables) {
	    			populateSettingVariable(var);
					List<ValueReference> possibleValues = 
					        fieldbookService.getAllPossibleValues(var.getCvTermId());
					SettingDetail newSetting = new SettingDetail(var, possibleValues, null, true);
					List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(var.getCvTermId(), this.getCurrentProjectId());
					newSetting.setPossibleValuesFavorite(possibleValuesFavorite);
					newSetting.setPossibleValuesToJson(possibleValues);
					newSetting.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
					newSettings.add(newSetting);
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
            deleteVariableInSession(userSelection.getNurseryLevelConditions(), variableId);
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            deleteVariableInSession(userSelection.getPlotsLevelList(), variableId);
        } else {
            deleteVariableInSession(userSelection.getBaselineTraitsList(), variableId);
        }
        return "";
    }
    
    private void deleteVariableInSession(List<SettingDetail> variableList, int variableId) {
        Iterator<SettingDetail> iter = variableList.iterator();
        while (iter.hasNext()) {
            if (iter.next().getVariable().getCvTermId().equals(new Integer(variableId))) {
                iter.remove();
            }
        }
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
    	form.setNurseryLevelVariables(nurseryDefaults);
    	form.setSettingName("");
    	form.setIsDefault(false);
    	this.userSelection.setNurseryLevelConditions(nurseryDefaults);
    	
    	nurseryDefaults.add(createSettingDetail(TermId.TRIAL_LOCATION.getId()
    	        , AppConstants.LOCATION.getString()));
        nurseryDefaults.add(createSettingDetail(TermId.PI_NAME.getId()
                , AppConstants.PRINCIPAL_INVESTIGATOR.getString()));
        nurseryDefaults.add(createSettingDetail(TermId.STUDY_NAME.getId()
                , AppConstants.STUDY_NAME.getString()));
        nurseryDefaults.add(createSettingDetail(TermId.STUDY_TITLE.getId()
                , AppConstants.STUDY_TITLE.getString()));
        nurseryDefaults.add(createSettingDetail(TermId.STUDY_OBJECTIVE.getId()
                , AppConstants.OBJECTIVE.getString()));
    }
    
    /**
     * Creates the setting detail.
     *
     * @param id the id
     * @return the setting detail
     * @throws MiddlewareQueryException the middleware query exception
     */
    private SettingDetail createSettingDetail(int id, String name) throws MiddlewareQueryException {
            String variableName = "";
            StandardVariable stdVar = getStandardVariable(id);
            if (name != null) {
                variableName = name;
            } else {
                variableName = stdVar.getName();
            }
            if (stdVar != null) {
            SettingVariable svar = new SettingVariable(
                    variableName, stdVar.getDescription(), stdVar.getProperty().getName(),
					stdVar.getScale().getName(), stdVar.getMethod().getName(), stdVar.getStoredIn().getName(), 
					stdVar.getDataType().getName(), stdVar.getDataType().getId(), 
					stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints().getMinValue() : null,
					stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints().getMaxValue() : null);
			svar.setCvTermId(stdVar.getId());
			svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
			svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");

			List<ValueReference> possibleValues = fieldbookService.getAllPossibleValues(id);
			SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
	                settingDetail.setPossibleValuesToJson(possibleValues);
	                List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProjectId());
	                settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
	                settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
	                return settingDetail;
		}
		return new SettingDetail();
    }
    
    /**
     * Gets the setting detail list.
     *
     * @param mode the mode
     * @return the setting detail list
     */
    private List<SettingDetail> getSettingDetailList(int mode) {
    	if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            return userSelection.getNurseryLevelConditions();
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            return userSelection.getPlotsLevelList();
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt()) {
            return userSelection.getBaselineTraitsList();
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
            if (form.getNurseryLevelVariables() == null) {
            	form.setNurseryLevelVariables(newDetails);
            }
            else {
            	form.getNurseryLevelVariables().addAll(newDetails);
            }
            if (userSelection.getNurseryLevelConditions() == null) {
            	userSelection.setNurseryLevelConditions(newDetails);
            }
            else {
            	userSelection.getNurseryLevelConditions().addAll(newDetails);
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
        } else {
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
    
    /**
     * Populates Setting Variable
     * @param var
     */
    private void populateSettingVariable(SettingVariable var) throws MiddlewareQueryException {
    	StandardVariable  stdvar = getStandardVariable(var.getCvTermId());
    	if (stdvar != null) {
			var.setDescription(stdvar.getDescription());
			var.setProperty(stdvar.getProperty().getName());
			var.setScale(stdvar.getScale().getName());
			var.setMethod(stdvar.getMethod().getName());
			var.setDataType(stdvar.getDataType().getName());
			var.setRole(stdvar.getStoredIn().getName());
			var.setCropOntologyId(stdvar.getCropOntologyId() != null ? stdvar.getCropOntologyId() : "");
			var.setTraitClass(stdvar.getIsA() != null ? stdvar.getIsA().getName() : "");
			var.setDataTypeId(stdvar.getDataType().getId());
			var.setMinRange(stdvar.getConstraints() != null && stdvar.getConstraints().getMinValue() != null ? stdvar.getConstraints().getMinValue() : null);
			var.setMaxRange(stdvar.getConstraints() != null && stdvar.getConstraints().getMaxValue() != null ? stdvar.getConstraints().getMaxValue() : null);
			var.setWidgetType();
    	}
    }

    /**
     * Get setting variable.
     * @param id
     * @return
     * @throws MiddlewareQueryException
     */
    private SettingVariable getSettingVariable(int id) throws MiddlewareQueryException {
		StandardVariable stdVar = getStandardVariable(id);
		if (stdVar != null) {
			SettingVariable svar = new SettingVariable(stdVar.getName(), 
			        stdVar.getDescription(), stdVar.getProperty().getName(),
					stdVar.getScale().getName(), stdVar.getMethod().getName(), 
					stdVar.getStoredIn().getName(), 
					stdVar.getDataType().getName(), stdVar.getDataType().getId(),
					stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints().getMinValue() : null,
					stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints().getMaxValue() : null);
			svar.setCvTermId(stdVar.getId());
			svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
			svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
			return svar;
		}
		return null;
    }
    /**
     * Get standard variable.
     * @param id
     * @return
     * @throws MiddlewareQueryException
     */
    private StandardVariable getStandardVariable(int id) throws MiddlewareQueryException {
    	StandardVariable variable = userSelection.getCacheStandardVariable(id);
    	if (variable == null) {
    		variable = fieldbookMiddlewareService.getStandardVariable(id);
    		if (variable != null) {
    			userSelection.putStandardVariableInCache(variable);
    		}
    	}
    	
    	return variable;
    }
}