/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.util;

import javax.servlet.http.HttpSession;

/**
 * The Class SessionUtility would be used for clearing session attributes and container for session relation attributes
 */
public class SessionUtility {

	/*
	 * this is being use for caching possible values, mainly use when creating Nursery (like breeding method, other categorical variates),
	 * we would want to clear this session every time we enter the create screen so we are sure it would get new value in the db
	 */
	public static String POSSIBLE_VALUES_SESSION_NAME = "scopedTarget.possibleValuesCache";
	/*
	 * This is the session object being use for advancing a study
	 */
	public static String ADVANCING_NURSERY_SESSION_NAME = "scopedTarget.advancingNursery";
	/*
	 * This is the session object being use for create/editing a nursery
	 */
	public static String USER_SELECTION_SESSION_NAME = "scopedTarget.userSelection";
	/*
	 * This is the session object being use for seed inventory
	 */
	public static String SEED_SELECTION_SESSION_NAME = "scopedTarget.seedSelection";
	/*
	 * This is the session object being use for fieldmap process
	 */
	public static String FIELDMAP_SESSION_NAME = "scopedTarget.userFieldmap";

	/*
	 * This is the session object being use to store paginated pages like the one being use in the review page
	 */
	public static String PAGINATION_LIST_SELECTION_SESSION_NAME = "scopedTarget.paginationListSelection";

	// this would be use in place for the session.invalidate
	public static void clearSessionData(HttpSession session, String[] attributeNames) {
		if (session != null && attributeNames != null) {
			for (int index = 0; index < attributeNames.length; index++) {
				session.removeAttribute(attributeNames[index]);
			}
		}
	}
}
