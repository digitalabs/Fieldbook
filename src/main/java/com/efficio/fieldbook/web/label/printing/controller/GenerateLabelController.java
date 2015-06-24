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

package com.efficio.fieldbook.web.label.printing.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.form.LabelPrintingForm;

/**
 * The Class GenerateLabelController.
 *
 * Code is not currently being use.
 */
@Controller
@RequestMapping({GenerateLabelController.URL})
public class GenerateLabelController extends AbstractBaseFieldbookController {

	/** The Constant URL. */
	public static final String URL = "/LabelPrinting/generateLabel";

	/** The user label printing. */
	@Resource
	private UserLabelPrinting userLabelPrinting;

	/**
	 * Show trial label details.
	 *
	 * @param form the form
	 * @param model the model
	 * @param session the session
	 * @param response the response
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showTrialLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, Model model, HttpSession session,
			HttpServletResponse response) {
		return super.show(model);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "LabelPrinting/generateLabel";
	}

	/**
	 * Gets the user label printing.
	 *
	 * @return the user label printing
	 */
	public UserLabelPrinting getUserLabelPrinting() {
		return this.userLabelPrinting;
	}

	/**
	 * Sets the user label printing.
	 *
	 * @param userLabelPrinting the new user label printing
	 */
	public void setUserLabelPrinting(UserLabelPrinting userLabelPrinting) {
		this.userLabelPrinting = userLabelPrinting;
	}

}
