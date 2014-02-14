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
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
    private Map generateDummySettings(int i){
    	Map map = new HashMap();
    	map.put("id", Integer.valueOf(i));
    	map.put("name", i + " Test Name");
    	return map;
    }
    @ModelAttribute("settingsList")
    public List<Map> getSettingsList() {
        //try {
        	//need to call the MW call passing the tool id and project id
        	//getCurrentProjectId()
            //List<Location> dataTypesOrig = fieldbookMiddlewareService.getAllLocations();
            //List<Location> dataTypes = dataTypesOrig;
            List<Map> mapList = new ArrayList();
            for(int i = 0 ; i < 10 ; i++){
            	mapList.add(generateDummySettings(i));
            }
            
            return mapList;
        /*    
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
		*/
        //return null;
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
    public String postAdvanceNursery(@ModelAttribute("manageSettingsForm") ManageSettingsForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{

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
    		
        	Set<StandardVariable> stdVars = fieldbookMiddlewareService.getAllStandardVariables();
        	List<StandardVariableReference> standardVariableList = fieldbookService.filterStandardVariablesForSetting(stdVars, mode, getSettingDetailList(mode));
        	if (standardVariableList != null && !standardVariableList.isEmpty()) {
        		ObjectMapper om = new ObjectMapper();
        		om.writeValueAsString(standardVariableList);
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
    			ObjectMapper om = new ObjectMapper();
    			return om.writeValueAsString(svar);
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