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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
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

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Controller
@RequestMapping(ReviewStudyDetailsController.URL)
public class ReviewStudyDetailsController extends AbstractBaseFieldbookController {

	public static final String URL = "/StudyManager/reviewStudyDetails";

	private static final Logger LOG = LoggerFactory.getLogger(ReviewStudyDetailsController.class);

	@Resource
	private UserSelection userSelection;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private ErrorHandlerService errorHandlerService;

	@Override
	public String getContentName() {
		return this.getContentName(this.userSelection.isTrial());
	}

	private String getContentName(boolean isTrial) {
		if (isTrial) {
			return "TrialManager/reviewTrialDetails";
		} else {
			return "NurseryManager/reviewNurseryDetails";
		}
	}

	@RequestMapping(value = "/show/{studyType}/{id}", method = RequestMethod.GET)
	public String show(@PathVariable String studyType, @PathVariable int id,
			@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, Model model) throws MiddlewareQueryException {

		boolean isNursery = studyType != null && StudyType.N.getName().equalsIgnoreCase(studyType) ? true : false;
		Workbook workbook;
		StudyDetails details;
		try {
			workbook = this.fieldbookMiddlewareService.getStudyVariableSettings(id, isNursery);
			workbook.setStudyId(id);
			details =
					SettingsUtil.convertWorkbookToStudyDetails(workbook, this.fieldbookMiddlewareService, this.fieldbookService,
							this.userSelection);
			this.rearrangeDetails(details);
			this.getPaginationListSelection().addReviewWorkbook(Integer.toString(id), workbook);
			if (workbook.getMeasurementDatesetId() != null) {
				details.setHasMeasurements(this.fieldbookMiddlewareService.countObservations(workbook.getMeasurementDatesetId()) > 0);
			} else {
				details.setHasMeasurements(false);
			}

		} catch (MiddlewareQueryException e) {
			ReviewStudyDetailsController.LOG.error(e.getMessage(), e);
			details = new StudyDetails();
			this.addErrorMessageToResult(details, e, isNursery, id);
		}

		if (isNursery) {
			model.addAttribute("nurseryDetails", details);
		} else {
			model.addAttribute("trialDetails", details);
		}

		return this.showAjaxPage(model, this.getContentName(!isNursery));
	}

	protected void addErrorMessageToResult(StudyDetails details, MiddlewareQueryException e, boolean isNursery, int id) {
		String param;
		if (isNursery) {
			param = AppConstants.NURSERY.getString();
		} else {
			param = AppConstants.TRIAL.getString();
		}
		details.setId(id);
		details.setErrorMessage(this.errorHandlerService.getErrorMessagesAsString(e.getCode(), new Object[] {param,
				param.substring(0, 1).toUpperCase().concat(param.substring(1, param.length())), param}, "\n"));
	}

	@ResponseBody
	@RequestMapping(value = "/datasets/{nurseryId}")
	public List<DatasetReference> loadDatasets(@PathVariable int nurseryId) throws MiddlewareQueryException {
		return this.fieldbookMiddlewareService.getDatasetReferences(nurseryId);
	}

	private void rearrangeDetails(StudyDetails details) {
		details.setBasicStudyDetails(this.rearrangeSettingDetails(details.getBasicStudyDetails()));
		details.setManagementDetails(this.rearrangeSettingDetails(details.getManagementDetails()));
	}

	private List<SettingDetail> rearrangeSettingDetails(List<SettingDetail> list) {
		List<SettingDetail> newList = new ArrayList<SettingDetail>();
		final int COLS = 3;

		if (list != null && !list.isEmpty()) {
			int rows = Double.valueOf(Math.ceil(list.size() / (double) COLS)).intValue();
			int extra = list.size() % COLS;
			for (int i = 0; i < list.size(); i++) {
				int delta = 0;
				int currentColumn = i % COLS;
				if (currentColumn > extra && extra > 0) {
					delta = currentColumn - extra;
				}
				int computedIndex = currentColumn * rows + i / COLS - delta;
				if (computedIndex < list.size()) {
					newList.add(list.get(computedIndex));
				} else {
					newList.add(list.get(computedIndex - 1));
				}
			}
		}
		return newList;
	}

	protected void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
