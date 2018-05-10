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

package com.efficio.fieldbook.web.trial.controller;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.middleware.domain.oms.StudyType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.trial.form.ManageTrialForm;

/**
 * The Class ManageTrialController.
 */
@Controller
@RequestMapping({"/TrialManager", ManageTrialController.URL})
public class ManageTrialController extends AbstractBaseFieldbookController {

	/** The Constant URL. */
	public static final String URL = "/TrialManager/manageTrial";

	/**
	 *
	 * @param form
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("manageTrialForm") ManageTrialForm form, Model model, HttpServletRequest request) {
		model.addAttribute("preloadSummaryId", request.getParameter("summaryId") != null ? request.getParameter("summaryId") : "");
		model.addAttribute("preloadSummaryName", request.getParameter("summaryName") != null ? request.getParameter("summaryName") : "");
		return super.show(model);
	}

	@Override
	public String getContentName() {
		return "Common/manageStudy";
	}

}
