
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
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;

public abstract class AbstractBaseControllerIntegrationTest extends AbstractBaseIntegrationTest {

	@Autowired
	protected RequestMappingHandlerAdapter handleAdapter;

	@Autowired
	protected RequestMappingHandlerMapping handlerMapping;

	protected MockHttpServletRequest request;
	protected MockHttpServletResponse response;
	protected MockHttpSession session;

	@Before
	public void beforeEachTest() {
		// This is required for Spring to create and manage session scoped beans.
		((GenericApplicationContext) this.applicationContext).getBeanFactory().registerScope("session", new SessionScope());

		this.request = new MockHttpServletRequest();
		this.response = new MockHttpServletResponse();
		this.session = new MockHttpSession();
		this.request.setSession(this.session);

		// This is required for binding the request to current thread in Spring context.
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(this.request));
	}

	protected ModelAndView request(String url, String method) throws Exception {

		this.request.setRequestURI(url);
		this.request.setMethod(method);

		Object handler = this.handlerMapping.getHandler(this.request).getHandler();
		return this.handleAdapter.handle(this.request, this.response, handler);
	}

	protected boolean verifyHandler(String url, String method, Class handlerClass, String methodName) throws Exception {
		this.request.setRequestURI(url);
		this.request.setMethod(method);

		Object handler = this.handlerMapping.getHandler(this.request).getHandler();

		if (handler instanceof HandlerMethod) {
			HandlerMethod methodCalled = (HandlerMethod) handler;
			boolean verified = handlerClass.isInstance(methodCalled.getBean());
			verified &= methodCalled.getMethod().getName().equals(methodName);
			return verified;
		}

		throw new UnsupportedOperationException();
	}

}
