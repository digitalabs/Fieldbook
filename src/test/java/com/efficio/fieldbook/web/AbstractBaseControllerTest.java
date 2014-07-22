package com.efficio.fieldbook.web;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.SessionScope;
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
	
	@Before
	public void beforeEachTest() {
		// This is required for Spring to create and manage session scoped beans.
		((GenericApplicationContext) applicationContext).getBeanFactory().registerScope("session", new SessionScope());
	}
	
	protected ModelAndView request(String url, String method) throws Exception {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		
		// This is required for binding the request to current thread in Spring context.
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		
		request.setRequestURI(url);
		request.setMethod(method);

		Object handler = handlerMapping.getHandler(request).getHandler();
		return handleAdapter.handle(request, response, handler);	
	}

}
