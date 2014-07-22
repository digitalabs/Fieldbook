package com.efficio.fieldbook.web;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public abstract class AbstractBaseControllerTest extends AbstractJUnit4SpringContextTests {
	
	@Autowired
	protected RequestMappingHandlerAdapter handleAdapter;

	@Autowired
	protected RequestMappingHandlerMapping handlerMapping;
	
	protected ModelAndView request(String url, String method) throws Exception {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		
		request.setRequestURI(url);
		request.setMethod(method);

		Object handler = handlerMapping.getHandler(request).getHandler();
		return handleAdapter.handle(request, response, handler);	
	}

}
