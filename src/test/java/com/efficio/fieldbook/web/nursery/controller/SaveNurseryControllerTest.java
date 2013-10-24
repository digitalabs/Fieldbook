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
package com.efficio.fieldbook.web.nursery.controller;

import junit.framework.Assert;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.efficio.fieldbook.web.nursery.form.SaveNurseryForm;

//http://www.finalconcept.com.au/article/view/spring-unit-testing-controllers
//http://stackoverflow.com/questions/12607140/how-to-test-post-spring-mvc
	
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class SaveNurseryControllerTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private SaveNurseryController controller;
	
	@Autowired
	private RequestMappingHandlerAdapter handlerAdapter;
	
	@Autowired
	private RequestMappingHandlerMapping handlerMapping;
	
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private MockHttpSession session;
	
	@Before
	public void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		request.setRequestURI(SaveNurseryController.URL);
		session = new MockHttpSession();
		request.setSession(session);
	}
	
	@Test
	public void testLoadInitialPage() throws Exception {
		request.setMethod("GET");
		Object handler = handlerMapping.getHandler(request).getHandler();
		ModelAndView mav = handlerAdapter.handle(request, response, handler);
		
		ModelAndViewAssert.assertViewName(mav, SaveNurseryController.BASE_TEMPLATE_NAME);
		ModelAndViewAssert.assertModelAttributeValue(mav, SaveNurseryController.TEMPLATE_NAME_ATTRIBUTE, "NurseryManager/saveNursery");
	}
	
	@Test
	public void testSetStudyDetails() throws Exception {
		SaveNurseryForm form = new SaveNurseryForm();
		
		String title = "Study title";
		String objective = "Study Objective";
		String name = "Nursery name";
		
		form.setTitle(title);
		form.setObjective(objective);
		form.setNurseryBookName(name);
		
		Workbook workbook = new Workbook();
		
		controller.setStudyDetails(title, objective, name, workbook);
		
		Assert.assertNotNull(workbook.getStudyDetails());
		StudyDetails studyDetails = workbook.getStudyDetails();
		Assert.assertEquals(title, studyDetails.getTitle());
		Assert.assertEquals(objective, studyDetails.getObjective());
		Assert.assertEquals(name, studyDetails.getStudyName());
		Assert.assertEquals(StudyType.N, studyDetails.getStudyType());
	}
}
