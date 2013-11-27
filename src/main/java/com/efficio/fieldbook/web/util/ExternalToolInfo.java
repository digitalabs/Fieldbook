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

// TODO: Auto-generated Javadoc
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
   public ExternalToolInfo(Properties properties)
   {
      this.currentProjectId = properties.get("workbench.currentProjectIdId").toString();
      this.oldFieldbookPath = properties.get("old.fb.tool.path").toString();
   }

/**
 * Gets the current project id.
 *
 * @return the current project id
 */
public String getCurrentProjectId() {
    return currentProjectId;
}

/**
 * Sets the current project id.
 *
 * @param currentProjectId the new current project id
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
 * @param oldFieldbookPath the new old fieldbook path
 */
public void setOldFieldbookPath(String oldFieldbookPath) {
    this.oldFieldbookPath = oldFieldbookPath;
}
   
       
}
