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
package com.efficio.fieldbook.web.common.controller;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import com.efficio.fieldbook.web.AbstractBaseControllerTest;
import com.efficio.fieldbook.web.common.bean.StudyDetails;

public class ReviewStudyDetailsControllerTest extends AbstractBaseControllerTest {

	@Resource
	private ReviewStudyDetailsController reviewStudyDetailsController;
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Test
	public void testShowReviewNurserySummaryWithError() throws Exception {
		fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		Mockito.when(fieldbookMiddlewareService.getStudyVariableSettings(1, true))
			.thenThrow(new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"));
		reviewStudyDetailsController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		ModelAndView mav = request(ReviewStudyDetailsController.URL + "/show/N/1", HttpMethod.GET.name());
		
		Assert.assertEquals("Expected HttpStatus OK but got " + response.getStatus() + " instead.", 
				HttpStatus.OK.value(), response.getStatus());
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "nurseryDetails");
	}
	
	@Test
	public void testShowReviewTrialSummaryWithError() throws Exception {
		fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		Mockito.when(fieldbookMiddlewareService.getStudyVariableSettings(1, false))
			.thenThrow(new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"));
		reviewStudyDetailsController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		ModelAndView mav = request(ReviewStudyDetailsController.URL + "/show/T/1", HttpMethod.GET.name());
		
		Assert.assertEquals("Expected HttpStatus OK but got " + response.getStatus() + " instead.", 
				HttpStatus.OK.value(), response.getStatus());
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "trialDetails");
	}
	
	@Test
	public void testAddErrorMessageToResultForNursery() throws Exception {
		StudyDetails details = new StudyDetails();

		reviewStudyDetailsController.addErrorMessageToResult(details,
				new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"),
				true, 1);
		
		Assert.assertEquals("Expecting error message for nursery but got " + details.getErrorMessage() + " instead.", 
				"This nursery is in a format that cannot be opened in the Nursery Manager. Please use the Study Browser if you" +
				" wish to see the details of this nursery."
				, details.getErrorMessage());
	}
	
	@Test
	public void testAddErrorMessageToResultForTrial() throws Exception {
		StudyDetails details = new StudyDetails();

		reviewStudyDetailsController.addErrorMessageToResult(details,
				new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"),
				false, 1);
		
		Assert.assertEquals("Expecting error message for nursery but got " + details.getErrorMessage() + " instead.", 
				"This trial is in a format that cannot be opened in the Trial Manager. Please use the Study Browser if you" +
				" wish to see the details of this trial."
				, details.getErrorMessage());
	}
}