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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/Fieldbook-servlet.xml" })
public class ManageNurseriesControllerTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private RequestMappingHandlerAdapter handleAdapter;

	@Autowired
	private RequestMappingHandlerMapping handlerMapping;

	@Test
	public void testGetReturnsCorrectModelAndView() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		
		request.setRequestURI(ManageNurseriesController.URL);
		request.setMethod("GET");

		Object handler = handlerMapping.getHandler(request).getHandler();
		ModelAndView mav = handleAdapter.handle(request, response, handler);

		ModelAndViewAssert.assertViewName(mav, ManageNurseriesController.BASE_TEMPLATE_NAME);
		ModelAndViewAssert.assertModelAttributeValue(mav, 
		        SaveNurseryController.TEMPLATE_NAME_ATTRIBUTE, "NurseryManager/manageNurseries");

	}

}