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
   private String currentUserId;
   
   /** The old fieldbook path. */
   private String oldFieldbookPath;

   public ExternalToolInfo(){
       
   }
   public ExternalToolInfo(Properties properties)
   {
      this.currentUserId = properties.get("workbench.currentUserId").toString();
      this.oldFieldbookPath = properties.get("old.fb.tool.path").toString();
   }
    /**
     * Gets the current user id.
     *
     * @return the current user id
     */
    public String getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * Sets the current user id.
     *
     * @param currentUserId the new current user id
     */
    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
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
