package com.efficio.fieldbook.web;

import org.generationcp.commons.util.LogoutUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/logout")
public class LogoutController {

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public String logout(HttpServletRequest request, HttpServletResponse response){
		LogoutUtil.manuallyLogout(request, response);
		return "Fieodbook successfully logged out";
	}

}
