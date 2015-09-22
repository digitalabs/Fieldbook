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

package com.efficio.fieldbook.web.common.controller;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.common.bean.StudyDetails;

public class ReviewStudyDetailsControllerTest extends AbstractBaseIntegrationTest {

	@Resource
	private ReviewStudyDetailsController reviewStudyDetailsController;

	@Test
	public void testShowReviewNurserySummaryWithError() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get(ReviewStudyDetailsController.URL + "/show/N/1"))
		.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.model().attributeExists("nurseryDetails"));
	}

	@Test
	public void testShowReviewTrialSummaryWithError() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get(ReviewStudyDetailsController.URL + "/show/T/1"))
		.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.model().attributeExists("trialDetails"));
	}

	@Test
	public void testAddErrorMessageToResultForNursery() throws Exception {
		final StudyDetails details = new StudyDetails();

		this.reviewStudyDetailsController.addErrorMessageToResult(details,
				new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"), true, 1);

		Assert.assertEquals("Expecting error message for nursery but got " + details.getErrorMessage() + " instead.",
				"This nursery is in a format that cannot be opened in the Nursery Manager. Please use the Study Browser if you"
						+ " wish to see the details of this nursery.", details.getErrorMessage());
	}

	@Test
	public void testAddErrorMessageToResultForTrial() throws Exception {
		final StudyDetails details = new StudyDetails();

		this.reviewStudyDetailsController.addErrorMessageToResult(details,
				new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"), false, 1);

		Assert.assertEquals("Expecting error message for nursery but got " + details.getErrorMessage() + " instead.",
				"This trial is in a format that cannot be opened in the Trial Manager. Please use the Study Browser if you"
						+ " wish to see the details of this trial.", details.getErrorMessage());
	}
}
