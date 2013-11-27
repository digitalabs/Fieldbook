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
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public abstract class AbstractBaseFieldbookController implements ApplicationContextAware {

    public static final String BASE_TEMPLATE_NAME = "/template/base-template";
    public static final String TEMPLATE_NAME_ATTRIBUTE = "templateName";
    public static final String GIT_INFO_ATTRIBUTE = "gitInfo";
    public static final String EXTERNAL_INFO_ATTRIBUTE = "externalInfo";
    public static final String USER_SELECTION_ID_SUFFIX = "TestUserSelection";
    
    @Resource
    public GitRepositoryState gitRepositoryState;
    
    @Resource
    public ExternalToolInfo externalToolInfo;

    
    private ApplicationContext applicationContext;

    public abstract String getContentName();
    
    private void setupModelInfo(Model model){
        
        model.addAttribute(GIT_INFO_ATTRIBUTE, gitRepositoryState);
        model.addAttribute(EXTERNAL_INFO_ATTRIBUTE, externalToolInfo);
    }
        
    
   

    public String getCurrentProjectId(){
        if(externalToolInfo != null)
            return externalToolInfo.getCurrentProjectId();
        return "";
    }
    
    public String getOldFieldbookPath(){
        if(externalToolInfo != null)
            return externalToolInfo.getOldFieldbookPath();
        return "";
    }
    
    /**
     * Base functionality for displaying the page.
     *
     * @param model
     * @return
     */
    public String show(Model model) {
        setupModelInfo(model);
        model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName());
        return BASE_TEMPLATE_NAME;
    }
    
    /**
     * Base functionality for displaying the page.
     *
     * @param model
     * @return
     */
    public String showAjaxPage(Model model, String ajaxPage) {
        setupModelInfo(model);
        //model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName());        
        return ajaxPage;
    }
    

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
