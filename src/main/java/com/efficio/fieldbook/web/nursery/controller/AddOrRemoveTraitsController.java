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

package com.efficio.fieldbook.web.nursery.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * The Class AddOrRemoveTraitsController.
 */
@Controller
@RequestMapping(AddOrRemoveTraitsController.URL)
public class AddOrRemoveTraitsController extends AbstractBaseFieldbookController {

	/** The Constant URL. */
	public static final String URL = "/NurseryManager/addOrRemoveTraits";

	public static final String OBSERVATIONS_HTML = "NurseryManager/observations";

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(AddOrRemoveTraitsController.class);

	/** The user selection. */
	@Resource
	private UserSelection userSelection;
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private MeasurementsGeneratorService measurementsGeneratorService;

	@Resource
	private PaginationListSelection paginationListSelection;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "NurseryManager/addOrRemoveTraits";
	}

	/**
	 * Shows the screen
	 *
	 * @param form the form
	 * @param model the model
	 * @param session the session
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	@Deprecated
	public String show(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, HttpSession session)
			throws MiddlewareQueryException {

		this.getUserSelection().setMeasurementRowList(
				this.measurementsGeneratorService.generateRealMeasurementRows(this.getUserSelection()));
		form.setMeasurementRowList(this.getUserSelection().getMeasurementRowList());
		form.setMeasurementVariables(this.getUserSelection().getWorkbook().getMeasurementDatasetVariables());
		form.changePage(1);
		this.userSelection.setCurrentPage(form.getCurrentPage());

		return super.show(model);
	}

	@RequestMapping(value = "/viewNursery/{nurseryId}", method = RequestMethod.GET)
	public String viewNursery(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, @PathVariable int nurseryId) {
		Workbook workbook = null;

		try {
			workbook = this.fieldbookMiddlewareService.getStudyDataSet(nurseryId);
		} catch (MiddlewareException e) {
			AddOrRemoveTraitsController.LOG.error(e.getMessage(), e);
		}

		if (workbook != null) {
			this.getUserSelection().setMeasurementRowList(workbook.getObservations());
			form.setMeasurementRowList(this.getUserSelection().getMeasurementRowList());
			form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
			form.setStudyName(workbook.getStudyDetails().getStudyName());
			form.changePage(1);
			this.userSelection.setCurrentPage(form.getCurrentPage());
			this.userSelection.setWorkbook(workbook);
		}

		return super.show(model);
	}

	/**
	 * Gets the user selection.
	 *
	 * @return the user selection
	 */
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	@RequestMapping(value = "/viewStudyAjax/{studyType}/{datasetId}", method = RequestMethod.GET)
	public String viewNurseryAjax(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, @PathVariable String studyType,
			@PathVariable int datasetId) {

		Workbook workbook = null;
		try {
			boolean isTrial = studyType != null && StudyType.T.getName().equalsIgnoreCase(studyType);
			workbook = this.fieldbookMiddlewareService.getCompleteDataset(datasetId, isTrial);
			this.fieldbookService.setAllPossibleValuesInWorkbook(workbook);
			SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), false,
					this.ontologyService, contextUtil.getCurrentProgramUUID());
		} catch (MiddlewareException e) {
			AddOrRemoveTraitsController.LOG.error(e.getMessage(), e);
		}
		this.getUserSelection().setMeasurementRowList(workbook.arrangeMeasurementObservation(workbook.getObservations()));
		form.setMeasurementRowList(this.getUserSelection().getMeasurementRowList());
		form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
		this.paginationListSelection.addReviewDetailsList(String.valueOf(datasetId), form.getMeasurementRowList());
		this.paginationListSelection.addReviewVariableList(String.valueOf(datasetId), form.getMeasurementVariables());
		form.changePage(1);
		this.userSelection.setCurrentPage(form.getCurrentPage());

		return super.showAjaxPage(model, AddOrRemoveTraitsController.OBSERVATIONS_HTML);
	}
}
