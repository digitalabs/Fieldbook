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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ui.Model;

import com.efficio.fieldbook.web.util.ExternalToolInfo;
import com.efficio.fieldbook.web.util.GitRepositoryState;
import com.efficio.fieldbook.web.util.ToolUtil;


/**
 * The Class AbstractBaseFieldbookController.
 */
public abstract class AbstractBaseFieldbookController implements ApplicationContextAware {

    /** The Constant BASE_TEMPLATE_NAME. */
    public static final String BASE_TEMPLATE_NAME = "/template/base-template";
    
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
    private void setupModelInfo(Model model){
        
        model.addAttribute(GIT_INFO_ATTRIBUTE, gitRepositoryState);
        model.addAttribute(EXTERNAL_INFO_ATTRIBUTE, externalToolInfo);
    }
        
    
   

    /**
     * Gets the current project id.
     *
     * @return the current project id
     */
    public String getCurrentProjectId(){
        if(externalToolInfo != null)
            return externalToolInfo.getCurrentProjectId();
        return "";
    }
    
    /**
     * Gets the old fieldbook path.
     *
     * @return the old fieldbook path
     */
    public String getOldFieldbookPath(){
        if(externalToolInfo != null)
            return externalToolInfo.getOldFieldbookPath();
        return "";
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
}
