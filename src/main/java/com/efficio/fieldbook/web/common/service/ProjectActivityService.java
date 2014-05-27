/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package com.efficio.fieldbook.web.common.service;

/**
 * The Interface ProjectActivityService.
 */
public interface ProjectActivityService {

	/**
	 * Adds the workbench project activity.
	 *
	 * @param activityName the activity name
	 * @param activityDescription the activity description
	 */
	void addWorkbenchProjectActivity(String activityName, String activityDescription);
}
