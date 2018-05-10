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

package com.efficio.fieldbook.web.fieldmap.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.label.printing.controller.LabelPrintingController;
import com.efficio.fieldbook.web.trial.controller.OpenTrialController;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

/**
 * The Class SaveFieldmapController.
 *
 * Controller is being use to save the fieldmap details in the database.
 */
@Controller
@RequestMapping({SaveFieldmapController.URL})
public class SaveFieldmapController extends AbstractBaseFieldbookController {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(SaveFieldmapController.class);

	/** The Constant URL. */
	public static final String URL = "/Fieldmap/saveFieldmap";

	/** The user fieldmap. */
	@Resource
	private UserFieldmap userFieldmap;

	/** The fieldbook middleware service. */
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return null;
	}

	/**
	 * Save field map.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String saveFieldMap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {

		try {
			if (this.userFieldmap != null && this.userFieldmap.getSelectedFieldMaps() != null && !this.userFieldmap.getSelectedFieldMaps()
				.isEmpty()) {

				this.updateSelectedFieldMapInfo();
				int userId = this.getCurrentIbdbUserId();
				this.fieldbookMiddlewareService
					.saveOrUpdateFieldmapProperties(this.userFieldmap.getSelectedFieldMaps(), userId, this.userFieldmap.isNew());
			}

		} catch (MiddlewareQueryException e) {
			SaveFieldmapController.LOG.error(e.getMessage(), e);
		}

		if ("1".equalsIgnoreCase(form.getSaveAndRedirectToCreateLabel())) {
			return "redirect:" + LabelPrintingController.URL + "/fieldmap";
		}

		return "redirect:" + OpenTrialController.URL + "/" + form.getUserFieldmap().getStudyId();

	}

	/**
	 * Update selected field map info.
	 */
	private void updateSelectedFieldMapInfo() {
		for (FieldMapInfo info : this.userFieldmap.getSelectedFieldMaps()) {
			for (FieldMapDatasetInfo datasetInfo : info.getDatasets()) {
				for (FieldMapTrialInstanceInfo trialInfo : datasetInfo.getTrialInstances()) {
					trialInfo.setLocationId(this.userFieldmap.getFieldLocationId());
					trialInfo.setFieldId(this.userFieldmap.getFieldId());
					trialInfo.setBlockId(this.userFieldmap.getBlockId());
					trialInfo.setRowsInBlock(this.userFieldmap.getNumberOfRowsInBlock());
					trialInfo.setRangesInBlock(this.userFieldmap.getNumberOfRangesInBlock());
					trialInfo.setPlantingOrder(this.userFieldmap.getPlantingOrder());
					trialInfo.setRowsPerPlot(this.userFieldmap.getNumberOfRowsPerPlot());
					trialInfo.setMachineRowCapacity(this.userFieldmap.getMachineRowCapacity());
					trialInfo.setDeletedPlots(this.userFieldmap.getDeletedPlots());

					// TODO: CLEAN UP, no longer needed
					trialInfo.setBlockName(this.userFieldmap.getBlockName());
					trialInfo.setFieldName(this.userFieldmap.getFieldName());
					trialInfo.setLocationName(this.userFieldmap.getLocationName());
				}
			}
		}
	}
}
