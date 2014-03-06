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
package com.efficio.fieldbook.web.util;

import java.util.Properties;

import javax.annotation.Resource;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.fieldmap.controller.FieldmapController;

/**
 * The Class GitRepositoryState.
 *
 * @author Daniel Jao
 */
public class ExternalToolInfo {
    
   /** The current user id. */
   private String currentProjectId;
   
   /** The old fieldbook path. */
   private String oldFieldbookPath;

   @Resource
   private WorkbenchService workbenchService;
   
   private static final Logger LOG = LoggerFactory.getLogger(ExternalToolInfo.class);
   
   /**
    * Instantiates a new external tool info.
    */
   public ExternalToolInfo(){
       
   }
   
   /**
    * Instantiates a new external tool info.
    *
    * @param properties the properties
    */
   public ExternalToolInfo(Properties properties){
      this.currentProjectId = properties.get("workbench.currentProjectIdId").toString();
      this.oldFieldbookPath = properties.get("old.fb.tool.path").toString();
   }

    /**
     * Gets the current project id.
     * 
     * @return the current project id
     */
    public String getCurrentProjectId() {
        long projectId = 0;
        try {           
            projectId = workbenchService.getLastOpenedProject();
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        return String.valueOf(projectId);
    }

    /**
     * Sets the current project id.
     * 
     * @param currentProjectId
     *            the new current project id
     */
    public void setCurrentProjectId(String currentProjectId) {
        this.currentProjectId = currentProjectId;
    }

    /**
     * Gets the old fieldbook path.
     * 
     * @return the old fieldbook path
     */
    public String getOldFieldbookPath() {
        return oldFieldbookPath;
    }

    /**
     * Sets the old fieldbook path.
     * 
     * @param oldFieldbookPath
     *            the new old fieldbook path
     */
    public void setOldFieldbookPath(String oldFieldbookPath) {
        this.oldFieldbookPath = oldFieldbookPath;
    }
   
}
