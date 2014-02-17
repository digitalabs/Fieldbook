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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ManageSettingsForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

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
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    @Resource
    private WorkbenchDataManager workbenchDataManager;
    
    @Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
    
    @Resource
    private UserSelection userSelection;
         
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/manageNurserySettings";
    }    
    
    @ModelAttribute("settingsList")
    public List<TemplateSetting> getSettingsList() {
        try {
        	//need to call the MW call passing the tool id and project id
        	//getCurrentProjectId()
        	Tool tool = workbenchDataManager.getToolWithName(AppConstants.TOOL_NAME_NURSERY_MANAGER_WEB);
        	
        	TemplateSetting templateSettingFilter = new TemplateSetting(null, Integer.valueOf(getCurrentProjectId()), null, tool, null, null);
        	templateSettingFilter.setIsDefaultToNull();
            List<TemplateSetting> templateSettingsList = workbenchDataManager.getTemplateSettings(templateSettingFilter);
           
            return templateSettingsList;
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
    	return super.show(model);
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
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String saveSettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{
		//will do the saving here
    	Dataset dataset = SettingsUtil.convertPojoToXmlDataset(form.getSettingName(), form.getNurseryLevelVariables(), form.getPlotLevelVariables(), form.getBaselineTraitVariables());
    	String xml = SettingsUtil.generateSettingsXml(dataset);
    	Tool tool = workbenchDataManager.getToolWithName(AppConstants.TOOL_NAME_NURSERY_MANAGER_WEB);
    	Integer tempateSettingId = form.getSelectedSettingId() > 0 ? Integer.valueOf(form.getSelectedSettingId()) : null;
    	TemplateSetting templateSetting = new TemplateSetting(tempateSettingId, Integer.valueOf(getCurrentProjectId()), dataset.getName(), tool, xml, Boolean.valueOf(form.getIsDefault())) ;
    	if(templateSetting.getTemplateSettingId() != null)
    		workbenchDataManager.addTemplateSetting(templateSetting);
    	else
    		workbenchDataManager.updateTemplateSetting(templateSetting);
        return super.show(model);
    }
    /**
     * For deletion of nursery setting
     * @param templateSettingId
     * @param model
     * @param session
     * @return
     * @throws MiddlewareQueryException
     */
    @ResponseBody
    @RequestMapping(value="/delete/{templateSettingId}", method = RequestMethod.POST)
    public String deleteSettings(@PathVariable int templateSettingId
            , Model model, HttpSession session) throws MiddlewareQueryException{
		//will do the saving here
    	workbenchDataManager.deleteTemplateSetting(Integer.valueOf(templateSettingId));
    	//need to add here the cleanup in the session and in the form
    	// need to reset here
        return super.show(model);
    }
    
    @ResponseBody
    @RequestMapping(value="/view/{templateSettingId}", method = RequestMethod.GET)
    public String viewSettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form, @PathVariable int templateSettingId
            , Model model, HttpSession session) throws MiddlewareQueryException{
		//will do the saving here
    	Tool tool = workbenchDataManager.getToolWithName(AppConstants.TOOL_NAME_NURSERY_MANAGER_WEB);
    	
    	TemplateSetting templateSettingFilter = new TemplateSetting(Integer.valueOf(templateSettingId), Integer.valueOf(getCurrentProjectId()), null, tool, null, null);
    	templateSettingFilter.setIsDefaultToNull();
    	List<TemplateSetting> templateSettings = workbenchDataManager.getTemplateSettings(templateSettingFilter);
    	TemplateSetting templateSetting = templateSettings.get(0); //always 1
    	Dataset dataset = SettingsUtil.parseXmlToDatasetPojo(templateSetting.getConfiguration());
    	SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, dataset, userSelection);
    	form.setNurseryLevelVariables(userSelection.getNurseryLevelConditions());
    	form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
    	form.setPlotLevelVariables(userSelection.getPlotsLevelList());
    	form.setIsDefault(templateSetting.getIsDefault().intValue() == 1 ? true : false);
    	form.setSettingName(templateSetting.getName());
    	// we now need to return json for the display
    	//need to add here the cleanup in the session and in the form
        return super.show(model);
    }
    
    /**
     * Displays the Add Setting popup.
     * 
     * @param model
     * @param mode
     * @return
     * @throws MiddlewareQueryException
     */
    @ResponseBody
    @RequestMapping(value = "displayAddSetting/{mode}", method = RequestMethod.GET)
    public String showAddSettingPopup(@PathVariable int mode) {
    	try {
    		Set<StandardVariable> stdVars = userSelection.getAllStandardVariables();
    		if (stdVars == null || stdVars.isEmpty()) {
    			stdVars = fieldbookMiddlewareService.getAllStandardVariables();
    		}
        	List<StandardVariableReference> standardVariableList = fieldbookService.filterStandardVariablesForSetting(stdVars, mode, getSettingDetailList(mode));
        	if (standardVariableList != null && !standardVariableList.isEmpty()) {
        		ObjectMapper om = new ObjectMapper();
        		return om.writeValueAsString(standardVariableList);
        	}

    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	
    	return "[]";
    }
    
    @ResponseBody
    @RequestMapping(value="showVariableDetails/{id}", method = RequestMethod.GET)
    public String showVariableDetails(@PathVariable int id) {
    	try {
    		
    		StandardVariable stdVar = fieldbookMiddlewareService.getStandardVariable(id);
    		if (stdVar != null) {
    			SettingVariable svar = new SettingVariable(stdVar.getName(), stdVar.getDescription(), stdVar.getProperty().getName(),
    					stdVar.getScale().getName(), stdVar.getMethod().getName(), stdVar.getStoredIn().getName(), 
    					stdVar.getDataType().getName());
    			svar.setCvTermId(stdVar.getId());
    			ObjectMapper om = new ObjectMapper();
    			return om.writeValueAsString(svar);
    		}
    		
    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	return "[]";
    }
    
    @RequestMapping(value = "addSettings", method = RequestMethod.POST)
    public String addSettings(@ModelAttribute("manageSettingsForm") ManageSettingsForm form) {
    	try {
	    	List<SettingVariable> selectedVariables = form.getSelectedVariables();
	    	List<SettingDetail> newSettings = new ArrayList<SettingDetail>();
	    	
	    	if (selectedVariables != null && !selectedVariables.isEmpty()) {
	    		for (SettingVariable var : selectedVariables) {
					List<ValueReference> possibleValues = fieldbookService.getAllPossibleValues(var.getCvTermId());
					newSettings.add(new SettingDetail(var, possibleValues, null, false));
	    		}
	    	}
	    	
	    	if (!newSettings.isEmpty()) {
	    		ObjectMapper om = new ObjectMapper();
	    		return om.writeValueAsString(newSettings);
	    	}
	    	
    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	
    	return "[]";
    }
    
    
    private List<SettingDetail> getSettingDetailList(int mode) {
    	switch (mode) {
	    	case AppConstants.SEGMENT_STUDY : return userSelection.getNurseryLevelConditions(); 
	    	case AppConstants.SEGMENT_PLOT : return userSelection.getPlotsLevelList();
	    	case AppConstants.SEGMENT_TRAITS : return userSelection.getBaselineTraitsList(); 
    	}
    	return null;
    }
}