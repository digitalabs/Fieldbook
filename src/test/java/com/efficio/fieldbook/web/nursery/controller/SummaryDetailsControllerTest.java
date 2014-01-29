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

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.efficio.fieldbook.web.nursery.form.SummaryDetailsForm;

/**
 * The Class SummaryDetailsControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class SummaryDetailsControllerTest extends AbstractJUnit4SpringContextTests {
    
    private static final Logger LOG = LoggerFactory.getLogger(SummaryDetailsControllerTest.class);

	/** The controller. */
	@Autowired
	private SummaryDetailsController controller;
	
	/** The handler adapter. */
	@Autowired
	private RequestMappingHandlerAdapter handlerAdapter;
	
	/** The handler mapping. */
	@Autowired
	private RequestMappingHandlerMapping handlerMapping;
	
	/** The request. */
	private MockHttpServletRequest request;
	
	/** The response. */
	private MockHttpServletResponse response;
	
	/** The session. */
	private MockHttpSession session;
	
	/**
	 * Sets the up.
	 */
	@Before
	public void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		request.setRequestURI(SummaryDetailsController.URL);
		session = new MockHttpSession();
		request.setSession(session);
	}
	
	/**
	 * Test load initial page.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testLoadInitialPage() throws Exception {
		request.setMethod("GET");
		Object handler = handlerMapping.getHandler(request).getHandler();
		ModelAndView mav = handlerAdapter.handle(request, response, handler);
		
		ModelAndViewAssert.assertViewName(mav, SummaryDetailsController.BASE_TEMPLATE_NAME);
		ModelAndViewAssert.assertModelAttributeValue(mav, 
		        SummaryDetailsController.TEMPLATE_NAME_ATTRIBUTE, "NurseryManager/summary");
	}
	
	@Test
	public void testSummaryDetailsForm() throws Exception {
	    
	    SummaryDetailsForm form = controller.getForm();
		assertNotNull(form);
		assertNotNull(form.getNurseryName());
		
		LOG.debug(form.toString());
		
	}
}
