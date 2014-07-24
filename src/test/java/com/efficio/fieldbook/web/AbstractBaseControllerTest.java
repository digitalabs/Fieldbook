package com.efficio.fieldbook.web;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.SessionScope;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;

public abstract class AbstractBaseControllerTest extends AbstractBaseIntegrationTest {
	
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
