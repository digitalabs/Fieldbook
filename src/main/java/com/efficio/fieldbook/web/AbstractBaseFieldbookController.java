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

package com.efficio.fieldbook.web;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.security.AuthorizationUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;

/**
 * Base controller encapsulaitng common functionality between all the Fieldbook controllers.
 */
public abstract class AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractBaseFieldbookController.class);

	public static final String BASE_TEMPLATE_NAME = "/template/base-template";
	public static final String ANGULAR_BASE_TEMPLATE_NAME = "/template/ng-base-template";
	public static final String ERROR_TEMPLATE_NAME = "/template/error-template";
	public static final String TEMPLATE_NAME_ATTRIBUTE = "templateName";

	@Resource
	private WorkbenchService workbenchService;

	@Resource
	protected ContextUtil contextUtil;

	@Resource
	protected FieldbookProperties fieldbookProperties;

	private static Tool oldFbTool = null;

	@Resource
	private PaginationListSelection paginationListSelection;

	@Resource
	protected OntologyVariableDataManager variableDataManager;

	/**
	 * Implemented by the sub controllers to specify the html view that they render into the base template.
	 * 
	 */
	public abstract String getContentName();

	protected void setupModelInfo(final Model model) {

	}

	// TODO change the return type to Long.
	public String getCurrentProjectId() {
		try {
			final Project projectInContext = this.contextUtil.getProjectInContext();
			if (projectInContext != null) {
				return projectInContext.getProjectId().toString();
			}
		} catch (final MiddlewareQueryException e) {
			AbstractBaseFieldbookController.LOG.error(e.getMessage(), e);
		}
		// TODO Keeping this default return value of 0 from old logic. Needs review/cleanup.
		return "0";
	}

	public Project getCurrentProject() {
		return this.contextUtil.getProjectInContext();
	}

	public Integer getCurrentIbdbUserId() {
		return this.workbenchService.getCurrentIbdbUserId(Long.valueOf(this.getCurrentProjectId()),
				this.contextUtil.getCurrentWorkbenchUserId());

	}

	public String getOldFieldbookPath() {

		if (AbstractBaseFieldbookController.oldFbTool == null) {
			try {
				AbstractBaseFieldbookController.oldFbTool =
						this.workbenchService.getToolWithName(AppConstants.TOOL_NAME_OLD_FIELDBOOK.getString());
			} catch (final MiddlewareQueryException e) {
				AbstractBaseFieldbookController.LOG.error(e.getMessage(), e);
			}
		}
		if (AbstractBaseFieldbookController.oldFbTool != null) {
			return AbstractBaseFieldbookController.oldFbTool.getPath();
		}
		return "";
	}

	/**
	 * Base functionality for displaying the page.
	 * 
	 * @param model the model
	 * @return the string
	 */
	public String show(final Model model) {
		this.setupModelInfo(model);
		model.addAttribute(AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, this.getContentName());
		return AbstractBaseFieldbookController.BASE_TEMPLATE_NAME;
	}

	public String showCustom(final Model model, final String contentName) {
		this.setupModelInfo(model);
		model.addAttribute(AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, contentName);
		return AbstractBaseFieldbookController.BASE_TEMPLATE_NAME;
	}

	public String showAngularPage(final Model model) {
		this.setupModelInfo(model);
		model.addAttribute(AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, this.getContentName());
		return AbstractBaseFieldbookController.ANGULAR_BASE_TEMPLATE_NAME;
	}

	/**
	 * Base functionality for displaying the error page.
	 * 
	 * @param model the model
	 * @return the string
	 */
	public String showError(final Model model) {
		this.setupModelInfo(model);
		return AbstractBaseFieldbookController.ERROR_TEMPLATE_NAME;
	}

	/**
	 * Base functionality for displaying the page.
	 * 
	 * @param model the model
	 * @param ajaxPage the ajax page
	 * @return the string
	 */
	public String showAjaxPage(final Model model, final String ajaxPage) {
		this.setupModelInfo(model);
		return ajaxPage;
	}

	/**
	 * Convert favorite location to json.
	 * 
	 * @param objectList list of objects
	 * @return the string
	 */
	protected String convertObjectToJson(final Object objectList) {
		if (objectList != null) {
			try {
				final ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(objectList);
			} catch (final Exception e) {
				AbstractBaseFieldbookController.LOG.error(e.getMessage(), e);
			}
		}
		return "[]";
	}

	public PaginationListSelection getPaginationListSelection() {
		return this.paginationListSelection;
	}

	public void setPaginationListSelection(final PaginationListSelection paginationListSelection) {
		this.paginationListSelection = paginationListSelection;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	/**
	 * Remove variables with variable types 'Analysis' and 'Analysis Summary' in the workbook's conditions, constants, factors and variates
	 */
	protected void removeAnalysisAndAnalysisSummaryVariables(final Workbook workbook) {
		this.removeAnalysisVariables(workbook.getConditions());
		this.removeAnalysisVariables(workbook.getConstants());
		this.removeAnalysisVariables(workbook.getFactors());
		this.removeAnalysisVariables(workbook.getVariates());
	}

	/**
	 * Remove variables with variable types 'Analysis' and 'Analysis Summary' in the list of measurement variables
	 */
	private void removeAnalysisVariables(final List<MeasurementVariable> measurementVariables) {
		final Iterator<MeasurementVariable> measurementVariablesIterator = measurementVariables.iterator();
		while (measurementVariablesIterator.hasNext()) {
			final MeasurementVariable measurementVariable = measurementVariablesIterator.next();
			if (measurementVariable != null && VariableType.getReservedVariableTypes().contains(measurementVariable.getVariableType())) {
				measurementVariablesIterator.remove();
			}
		}
	}
	
	public void setIsSuperAdminAttribute(final Model model) {
		model.addAttribute("isSuperAdmin", AuthorizationUtil.isSuperAdminUser());
	}

	public void setVariableDataManager(final OntologyVariableDataManager variableDataManager) {
		this.variableDataManager = variableDataManager;
	}

}
