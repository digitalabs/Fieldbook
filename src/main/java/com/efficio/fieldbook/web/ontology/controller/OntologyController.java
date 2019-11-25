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

package com.efficio.fieldbook.web.ontology.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.ontology.form.OntologyBrowserForm;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This controller handles the ontology screen.
 *
 * @author Daniel Jao
 */
@Controller
@RequestMapping({OntologyController.URL, "/OntologyBrowser"})
public class OntologyController extends AbstractBaseFieldbookController {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(OntologyController.class);

	/** The Constant URL. */
	public static final String URL = "/OntologyBrowser/";

	/** The ontology service. */
	@Resource
	private OntologyService ontologyService;

	/** The message source. */
	@Autowired
	public MessageSource messageSource;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "OntologyBrowser/main";
	}

	/**
	 * Show the main import page.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form, Model model) {
		// this set the necessary info from the session variable
		return this.setupForm(form, false, model);
	}

	@RequestMapping(value = "popup", method = RequestMethod.GET)
	public String showPopup(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form, Model model) {
		// this set the necessary info from the session variable
		return this.setupForm(form, true, model);
	}

	private String setupForm(OntologyBrowserForm form, boolean showAsPopup, Model model) {
		try {
			List<TraitClassReference> traitRefList = this.ontologyService.getAllTraitGroupsHierarchy(true);
			form.setTraitClassReferenceList(traitRefList);
			form.setTreeData(TreeViewUtil.convertOntologyTraitsToJson(traitRefList, null));
			form.setSearchTreeData(TreeViewUtil.convertOntologyTraitsToSearchSingleLevelJson(traitRefList, null));

			model.addAttribute("isPopup", showAsPopup);
		} catch (Exception e) {
			OntologyController.LOG.error(e.getMessage(), e);
		}
		return super.show(model);
	}

	/**
	 * Checks if is integer.
	 *
	 * @param s the s
	 * @return true, if is integer
	 */
	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	/**
	 * Save new term.
	 *
	 * @param propertyId the property id
	 * @param local the local
	 * @return the map
	 */
	@ResponseBody
	@RequestMapping(value = "retrieve/trait/property/{propertyId}", method = RequestMethod.GET)
	public Map<String, String> retrieveTraitProperty(@PathVariable String propertyId, Locale local) {
		Map<String, String> resultMap = new HashMap<String, String>();

		try {
			Property property = this.ontologyService.getProperty(Integer.parseInt(propertyId));
			Term term = property.getIsA();
			String traitId = term == null ? "" : Integer.toString(term.getId());

			resultMap.put("status", "1");
			resultMap.put("traitId", traitId);

		} catch (MiddlewareQueryException e) {
			OntologyController.LOG.error(e.getMessage(), e);
			resultMap.put("status", "-1");
			resultMap.put("errorMessage", e.getMessage());
		}
		return resultMap;
	}

}
