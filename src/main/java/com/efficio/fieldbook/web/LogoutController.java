package com.efficio.fieldbook.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/logout")
public class LogoutController {

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public String logout(HttpServletRequest request){

		HttpSession context = request.getSession(false);
		if(context != null) {
			context.invalidate();
		}

		SecurityContext context1 = SecurityContextHolder.getContext();
		context1.setAuthentication((Authentication)null);

		for(Cookie cookie : request.getCookies()) {
			cookie.setMaxAge(0);
		}

		SecurityContextHolder.clearContext();
		return "Fieodbook successfully logged out";
	}

}
