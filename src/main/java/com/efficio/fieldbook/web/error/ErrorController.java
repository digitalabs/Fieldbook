/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.error;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

@Controller
@RequestMapping({ErrorController.URL})
public class ErrorController extends AbstractBaseFieldbookController {

	public static final String URL = "/error";

	@RequestMapping(method = RequestMethod.GET)
	public String showGeneralError(Model model, HttpSession session, Locale locale) {
		return super.showError(model);
	}

	@Override
	public String getContentName() {
		return null;
	}

}
