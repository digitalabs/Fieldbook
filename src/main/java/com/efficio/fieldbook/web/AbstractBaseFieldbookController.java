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
package com.efficio.fieldbook.web;

import javax.annotation.Resource;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExternalToolInfo;
import com.efficio.fieldbook.web.util.GitRepositoryState;

/**
 * The Class AbstractBaseFieldbookController.
 */
public abstract class AbstractBaseFieldbookController implements ApplicationContextAware {

    /** The Constant BASE_TEMPLATE_NAME. */
    public static final String BASE_TEMPLATE_NAME = "/template/base-template";
    public static final String ERROR_TEMPLATE_NAME = "/template/error-template";
    
    /** The Constant TEMPLATE_NAME_ATTRIBUTE. */
    public static final String TEMPLATE_NAME_ATTRIBUTE = "templateName";
    
    /** The Constant GIT_INFO_ATTRIBUTE. */
    public static final String GIT_INFO_ATTRIBUTE = "gitInfo";
    
    /** The Constant EXTERNAL_INFO_ATTRIBUTE. */
    public static final String EXTERNAL_INFO_ATTRIBUTE = "externalInfo";
    
    /** The Constant USER_SELECTION_ID_SUFFIX. */
    public static final String USER_SELECTION_ID_SUFFIX = "TestUserSelection";
    
    /** The git repository state. */
    @Resource
    public GitRepositoryState gitRepositoryState;
    
    /** The external tool info. */
    @Resource
    public ExternalToolInfo externalToolInfo;

    
    /** The application context. */
    private ApplicationContext applicationContext;
    
    /** The workbench data manager. */
    @Resource
    private WorkbenchService workbenchService;
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBaseFieldbookController.class);

    private static Tool oldFbTool = null;
    /**
     * Gets the content name.
     *
     * @return the content name
     */
    public abstract String getContentName();
    
    /**
     * Sets the up model info.
     *
     * @param model the new up model info
     */
    protected void setupModelInfo(Model model){
        
        model.addAttribute(GIT_INFO_ATTRIBUTE, gitRepositoryState);
        model.addAttribute(EXTERNAL_INFO_ATTRIBUTE, externalToolInfo);
    }
        
    
   

    /**
     * Gets the current project id.
     *
     * @return the current project id
     */
    public String getCurrentProjectId(){
        if(externalToolInfo != null){
            return externalToolInfo.getCurrentProjectId();
        }
        return "";
    }
    
    /**
     * Gets the old fieldbook path.
     *
     * @return the old fieldbook path
     */
    public String getOldFieldbookPath(){
    	
    	if(oldFbTool == null){
			try {
				oldFbTool = workbenchService.getToolWithName(
				        AppConstants.TOOL_NAME_OLD_FIELDBOOK.getString());
			} catch (MiddlewareQueryException e) {
			    LOG.error(e.getMessage(), e);
			}
    	}
		if(oldFbTool != null)
			return oldFbTool.getPath();
		return "";
    	/*
        if(externalToolInfo != null){
            return externalToolInfo.getOldFieldbookPath();
        }
        return "";
        */
    }
    
    public Tool getNurseryTool(){
    	Tool tool = null;
		try {
			tool = workbenchService.getToolWithName(
			        AppConstants.TOOL_NAME_NURSERY_MANAGER_WEB.getString());
		} catch (MiddlewareQueryException e) {
		    LOG.error(e.getMessage(), e);
		}
    	return tool;
    }
    public Tool getTrialTool(){
    	Tool tool = null;
		try {
			tool = workbenchService.getToolWithName(
			        AppConstants.TOOL_NAME_TRIAL_MANAGER_WEB.getString());
		} catch (MiddlewareQueryException e) {
		    LOG.error(e.getMessage(), e);
		}
    	return tool;
    }
    
    /**
     * Base functionality for displaying the page.
     *
     * @param model the model
     * @return the string
     */
    public String show(Model model) {
        setupModelInfo(model);
        model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName());
        return BASE_TEMPLATE_NAME;
    }
    
    public String showCustom(Model model,String contentName) {
        setupModelInfo(model);
        model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, contentName);
        return BASE_TEMPLATE_NAME;
    }
    
    /**
     * Base functionality for displaying the page.
     *
     * @param model the model
     * @return the string
     */
    public String showError(Model model) {
        setupModelInfo(model);
        //model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName());
        return ERROR_TEMPLATE_NAME;
    }
    
    /**
     * Base functionality for displaying the page.
     *
     * @param model the model
     * @param ajaxPage the ajax page
     * @return the string
     */
    public String showAjaxPage(Model model, String ajaxPage) {
        setupModelInfo(model);
        //model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName());        
        return ajaxPage;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    /**
     * Convert favorite location to json.
     *
     * @param locations the locations
     * @return the string
     */
    protected String convertObjectToJson(Object objectList) {
        if (objectList!= null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(objectList);
            } catch(Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return "[]";
    }
}
