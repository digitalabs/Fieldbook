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

package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Controller
@RequestMapping(ReviewStudyDetailsController.URL)
public class ReviewStudyDetailsController extends AbstractBaseFieldbookController {

	public static final String URL = "/StudyManager/reviewStudyDetails";

	private static final Logger LOG = LoggerFactory.getLogger(ReviewStudyDetailsController.class);

	private static final int COLS = 3;

	public static final String TRIAL_MANAGER_REVIEW_TRIAL_DETAILS = "TrialManager/reviewTrialDetails";

	@Resource
	private UserSelection userSelection;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private ErrorHandlerService errorHandlerService;

	@Resource
	private Properties appConstantsProperties;

	/** The workbench service. */
	@Resource
	protected WorkbenchService workbenchService;

	@Override
	public String getContentName() {
		return TRIAL_MANAGER_REVIEW_TRIAL_DETAILS;
	}

	@RequestMapping(value = "/show/{id}", method = RequestMethod.GET)
	public String show(@PathVariable final int id, @ModelAttribute("createTrialForm") final CreateTrialForm form,
		final Model model) {

		final Workbook workbook;
		StudyDetails details;
		try {
			workbook = this.fieldbookMiddlewareService.getStudyVariableSettings(id);
			workbook.getStudyDetails().setId(id);
			this.removeAnalysisAndAnalysisSummaryVariables(workbook);
			final String createdBy = this.fieldbookService.getPersonByUserId(NumberUtils.toInt(workbook.getStudyDetails().getCreatedBy()));
			details = SettingsUtil.convertWorkbookToStudyDetails(workbook, this.fieldbookMiddlewareService, this.fieldbookService,
					this.userSelection, this.contextUtil.getCurrentProgramUUID(), this.appConstantsProperties, createdBy);
			this.rearrangeDetails(details);
			this.getPaginationListSelection().addReviewWorkbook(Integer.toString(id), workbook);
			if (workbook.getMeasurementDatesetId() != null) {
				details.setHasMeasurements(this.fieldbookMiddlewareService.countObservations(workbook.getMeasurementDatesetId()) > 0);
			} else {
				details.setHasMeasurements(false);
			}

			this.userSelection.setWorkbook(workbook);
		} catch (final MiddlewareException e) {
			ReviewStudyDetailsController.LOG.error(e.getMessage(), e);
			details = new StudyDetails();
			this.addErrorMessageToResult(details, e, id);
		}

		model.addAttribute("trialDetails", details);
		return this.showAjaxPage(model, this.getContentName());
	}

	protected void addErrorMessageToResult(final StudyDetails details, final MiddlewareException e, final int id) {
		final String param = AppConstants.STUDY.getString();
		details.setId(id);
		String errorMessage = e.getMessage();
		if (e instanceof MiddlewareQueryException) {
			errorMessage = this.errorHandlerService.getErrorMessagesAsString(((MiddlewareQueryException) e).getCode(),
				new Object[] {param, param.substring(0, 1).toUpperCase().concat(param.substring(1, param.length())), param}, "\n");
		}
		details.setErrorMessage(errorMessage);
	}

	@ResponseBody
	@RequestMapping(value = "/datasets/{nurseryId}")
	public List<DatasetReference> loadDatasets(@PathVariable final int nurseryId) {
		return this.fieldbookMiddlewareService.getDatasetReferences(nurseryId);
	}

	private void rearrangeDetails(final StudyDetails details) {
		details.setBasicStudyDetails(this.rearrangeSettingDetails(details.getBasicStudyDetails()));
		details.setManagementDetails(this.rearrangeSettingDetails(details.getManagementDetails()));
	}

	private List<SettingDetail> rearrangeSettingDetails(final List<SettingDetail> list) {
		final List<SettingDetail> newList = new ArrayList<>();

		if (list != null && !list.isEmpty()) {
			final int rows = Double.valueOf(Math.ceil(list.size() / (double) ReviewStudyDetailsController.COLS)).intValue();
			final int extra = list.size() % ReviewStudyDetailsController.COLS;
			for (int i = 0; i < list.size(); i++) {
				int delta = 0;
				final int currentColumn = i % ReviewStudyDetailsController.COLS;
				if (currentColumn > extra && extra > 0) {
					delta = currentColumn - extra;
				}
				final int computedIndex = currentColumn * rows + i / ReviewStudyDetailsController.COLS - delta;
				if (computedIndex < list.size()) {
					newList.add(list.get(computedIndex));
				} else {
					newList.add(list.get(computedIndex - 1));
				}
			}
		}
		return newList;
	}

	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected void setFieldbookService(final com.efficio.fieldbook.service.api.FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}
}
