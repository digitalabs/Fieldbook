/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web;

import java.util.Enumeration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.service.ProjectActivityService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;

/**
 * Base controller encapsulaitng common functionality between all the Fieldbook controllers.
 */
public abstract class AbstractBaseFieldbookController {

	public static final String BASE_TEMPLATE_NAME = "/template/base-template";
	public static final String ERROR_TEMPLATE_NAME = "/template/error-template";
	public static final String TEMPLATE_NAME_ATTRIBUTE = "templateName";
		
	@Resource
	private WorkbenchService workbenchService;
	
	@Resource
	private ProjectActivityService projectActivityService;
	
	@Resource
	protected FieldbookProperties fieldbookProperties;

	private static final Logger LOG = LoggerFactory.getLogger(AbstractBaseFieldbookController.class);

	private static Tool oldFbTool = null;

	@Resource
	private PaginationListSelection paginationListSelection;

	/**
	 * Implemented by the sub controllers to specify the html view that they render into the base template.
	 * 
	 */
	public abstract String getContentName();

	protected void setupModelInfo(Model model) {
		
	}

	public String getCurrentProjectId() {		
		long projectId = 0;
        try {           
            projectId = workbenchService.getLastOpenedProject();
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        return String.valueOf(projectId);
	}

	public String getOldFieldbookPath() {

		if (oldFbTool == null) {
			try {
				oldFbTool = workbenchService.getToolWithName(AppConstants.TOOL_NAME_OLD_FIELDBOOK.getString());
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		if (oldFbTool != null) {
			return oldFbTool.getPath();
		}
		return "";
	}

	public Tool getNurseryTool() {
		Tool tool = null;
		try {
			tool = workbenchService.getToolWithName(AppConstants.TOOL_NAME_NURSERY_MANAGER_WEB.getString());
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		return tool;
	}

	public Tool getTrialTool() {
		Tool tool = null;
		try {
			tool = workbenchService.getToolWithName(AppConstants.TOOL_NAME_TRIAL_MANAGER_WEB.getString());
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		return tool;
	}

	/**
	 * Base functionality for displaying the page.
	 * 
	 * @param model
	 *            the model
	 * @return the string
	 */
	public String show(Model model) {
		setupModelInfo(model);
		model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName());
		return BASE_TEMPLATE_NAME;
	}

	public String showCustom(Model model, String contentName) {
		setupModelInfo(model);
		model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, contentName);
		return BASE_TEMPLATE_NAME;
	}

	/**
	 * Base functionality for displaying the error page.
	 * 
	 * @param model
	 *            the model
	 * @return the string
	 */
	public String showError(Model model) {
		setupModelInfo(model);
		return ERROR_TEMPLATE_NAME;
	}

	/**
	 * Base functionality for displaying the page.
	 * 
	 * @param model
	 *            the model
	 * @param ajaxPage
	 *            the ajax page
	 * @return the string
	 */
	public String showAjaxPage(Model model, String ajaxPage) {
		setupModelInfo(model);
		return ajaxPage;
	}

	/**
	 * Convert favorite location to json.
	 * 
	 * @param locations
	 *            the locations
	 * @return the string
	 */
	protected String convertObjectToJson(Object objectList) {
		if (objectList != null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(objectList);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return "[]";
	}

	public PaginationListSelection getPaginationListSelection() {
		return paginationListSelection;
	}

	public void setPaginationListSelection(PaginationListSelection paginationListSelection) {
		this.paginationListSelection = paginationListSelection;
	}

	public ProjectActivityService getProjectActivityService() {
		return projectActivityService;
	}	
}
