package com.efficio.fieldbook.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.commons.hibernate.DynamicManagerFactoryProviderConcurrency;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class FieldbookHandlerInterceptor implements HandlerInterceptor {

	@Autowired
	private WorkbenchDataManagerImpl workbenchDataManager; 
	
	@Autowired
	private DynamicManagerFactoryProviderConcurrency managerFactoryProvider; 
	
	
	public FieldbookHandlerInterceptor() {
		
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		
		
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setDateHeader("Expires", 0);
		
		Boolean lastOpenedProjectChanged = true;
    	try {
			lastOpenedProjectChanged = workbenchDataManager.isLastOpenedProjectChanged();
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
    	
    	if (lastOpenedProjectChanged){	
    		
    		try{
    			managerFactoryProvider.close();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		
			request.getSession().invalidate();
		}
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
		managerFactoryProvider.close();
		workbenchDataManager.close();
	
	}

}
