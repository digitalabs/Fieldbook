/*******************************************************************************
 * Copyright (c) 2014, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.service.api;

import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.Tool;

/**
 * The Interface WorkbenchService.
 * 
 * @author Joyce Avestro
 * 
 */
public interface WorkbenchService {
	
    /**
     * Gets the favorite project location ids.
     *
     * @param projectId the project id
     * @return the favorite project location ids
     * @throws MiddlewareQueryException the middleware query exception
     */
    List<Long> getFavoriteProjectLocationIds(String projectId) throws MiddlewareQueryException;
    
    /**
     * Gets the favorite project methods.
     *
     * @param projectId the project id
     * @return the favorite project methods
     * @throws MiddlewareQueryException the middleware query exception
     */
    List<Integer> getFavoriteProjectMethods(String projectId) throws MiddlewareQueryException;

    /**
     * Gets the tool with name.
     *
     * @param toolName the tool name
     * @return the tool with name
     * @throws MiddlewareQueryException the middleware query exception
     */
    Tool getToolWithName(String toolName) throws MiddlewareQueryException;
    
    /**
     * Gets the template settings.
     *
     * @param templateSettingFilter the template setting filter
     * @return the template settings
     * @throws MiddlewareQueryException the middleware query exception
     */
    List<TemplateSetting> getTemplateSettings(TemplateSetting templateSettingFilter) 
            throws MiddlewareQueryException;

    /**
     * Adds the template setting.
     *
     * @param templateSetting the template setting
     * @throws MiddlewareQueryException the middleware query exception
     */
    Integer addTemplateSetting(TemplateSetting templateSetting) throws MiddlewareQueryException;
    
    /**
     * Update template setting.
     *
     * @param templateSetting the template setting
     * @throws MiddlewareQueryException the middleware query exception
     */
    void updateTemplateSetting(TemplateSetting templateSetting) throws MiddlewareQueryException;
    
    /**
     * Delete template setting.
     *
     * @param templateSettingId the template setting id
     * @throws MiddlewareQueryException the middleware query exception
     */
    void deleteTemplateSetting(Integer templateSettingId) throws MiddlewareQueryException;
       
    /**
     * Gets the current ibdb user id.
     *
     * @param projectId the project id
     * @param workbenchUserId the current workbench user id
     * 
     * @return the current ibdb user id
     * @throws MiddlewareQueryException the middleware query exception
     */
    Integer getCurrentIbdbUserId(Long projectId, Integer workbenchUserId) throws MiddlewareQueryException;
    
    /**
     * Gets the project by id.
     *
     * @param projectId the project id
     * @return the project by id
     * @throws MiddlewareQueryException the middleware query exception
     */
    Project getProjectById(Long projectId) throws MiddlewareQueryException;
}
