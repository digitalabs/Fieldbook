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
package com.efficio.fieldbook.web.common.service.impl;

import java.util.Date;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.service.ProjectActivityService;

/**
 * The Class ProjectActivityServiceImpl.
 */
@Service
public class ProjectActivityServiceImpl implements ProjectActivityService {

	/** The workbench data manager. */
	@Autowired
    private WorkbenchDataManager workbenchDataManager;

    /** The wb user id. */
    private Integer wbUserId;
    
    /** The project. */
    private Project project;
    
	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.common.service.ProjectActivityService#addWorkbenchProjectActivity(java.lang.String, java.lang.String)
	 */
	@Override
	public void addWorkbenchProjectActivity(String activityName,
			String activityDescription) {
		try {
			if(activityName != null && !activityName.equalsIgnoreCase("") && 
					activityDescription != null && !activityDescription.equalsIgnoreCase("")){
				retrieveIbdbUserId();
				User user = workbenchDataManager.getUserById(this.wbUserId);
		        ProjectActivity activity = new ProjectActivity(project.getProjectId().intValue(), project,
		        		activityName, activityDescription, user, new Date());
	
		        workbenchDataManager.addProjectActivity(activity);
			}
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Retrieve ibdb user id.
	 *
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	private void retrieveIbdbUserId() throws MiddlewareQueryException {
        this.wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
        this.project = workbenchDataManager.getLastOpenedProject(wbUserId);
    }
}
