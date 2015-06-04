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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.bean.BasicDetails;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.trial.bean.TabInfo;
import com.efficio.fieldbook.web.trial.bean.TrialData;
import com.efficio.fieldbook.web.trial.bean.TrialSettingsBean;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

/**
 * The Class CreateTrialController.
 */
@Controller
@RequestMapping(CreateTrialController.URL)
public class CreateTrialController extends BaseTrialController {

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CreateTrialController.class);

	/**
	 * The Constant URL.
	 */
	public static final String URL = "/TrialManager/createTrial";
	public static final String ENVIRONMENT_DATA_TAB = "environmentData";
	public static final String TRIAL_SETTINGS_DATA_TAB = "trialSettingsData";

	/**
	 * The Constant URL_SETTINGS.
	 */

	@Resource
	private ErrorHandlerService errorHandlerService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "TrialManager/createTrial";
	}

	@ModelAttribute("operationMode")
	public String getOperationMode() {
		return "CREATE";
	}

	@ModelAttribute("measurementDataExisting")
	public Boolean getMeasurementDataExisting() throws MiddlewareQueryException {
		return false;
	}

	/**
	 * Show.
	 *
	 * @param model the model
	 * @param session the session
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("createTrialForm") CreateTrialForm form, Model model, HttpSession session)
			throws MiddlewareQueryException {

		SessionUtility.clearSessionData(session, new String[] {SessionUtility.USER_SELECTION_SESSION_NAME,
				SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});

		model.addAttribute("basicDetailsData", this.prepareBasicDetailsTabInfo());
		model.addAttribute("germplasmData", this.prepareGermplasmTabInfo(false));
		model.addAttribute(CreateTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(false));
		model.addAttribute(CreateTrialController.TRIAL_SETTINGS_DATA_TAB, this.prepareTrialSettingsTabInfo());
		model.addAttribute("experimentalDesignSpecialData", this.prepareExperimentalDesignSpecialData());
		model.addAttribute("measurementRowCount", 0);

		// so that we can reuse the same page being use for nursery
		model.addAttribute("createNurseryForm", form);
		return this.showAngularPage(model);
	}

	@ResponseBody
	@RequestMapping(value = "/useExistingTrial", method = RequestMethod.GET)
	public Map<String, Object> getExistingTrialDetails(@RequestParam(value = "trialID") Integer trialID) throws MiddlewareQueryException {
		Map<String, Object> tabDetails = new HashMap<String, Object>();
		CreateTrialForm form = new CreateTrialForm();
		try {
			if (trialID != null && trialID != 0) {
				Workbook trialWorkbook = this.fieldbookMiddlewareService.getTrialDataSet(trialID);
				this.userSelection.setConstantsWithLabels(trialWorkbook.getConstants());

				tabDetails.put("germplasmData", this.prepareGermplasmTabInfo(trialWorkbook.getFactors(), true));
				tabDetails.put(CreateTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, true));
				tabDetails.put(CreateTrialController.TRIAL_SETTINGS_DATA_TAB,
						this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), true));
				tabDetails.put("measurementsData", this.prepareMeasurementsTabInfo(trialWorkbook.getVariates(), true));
				this.fieldbookMiddlewareService.setTreatmentFactorValues(trialWorkbook.getTreatmentFactors(),
						trialWorkbook.getMeasurementDatesetId());
				tabDetails.put("treatmentFactorsData", this.prepareTreatmentFactorsInfo(trialWorkbook.getTreatmentFactors(), true));
			}
		} catch (MiddlewareQueryException e) {
			CreateTrialController.LOG.error(e.getMessage(), e);
			form = this.addErrorMessageToResult(e);
		}

		tabDetails.put("createTrialForm", form);
		return tabDetails;
	}

	private CreateTrialForm addErrorMessageToResult(MiddlewareQueryException e) {
		String param = AppConstants.TRIAL.getString();
		CreateTrialForm form = new CreateTrialForm();
		form.setHasError(true);
		form.setErrorMessage(this.errorHandlerService.getErrorMessagesAsString(e.getCode(), new Object[] {param,
				param.substring(0, 1).toUpperCase().concat(param.substring(1, param.length())), param}, "\n"));
		return form;
	}

	@ModelAttribute("programLocationURL")
	public String getProgramLocation() {
		return this.fieldbookProperties.getProgramLocationsUrl();
	}

	@ModelAttribute("projectID")
	public String getProgramID() {
		return this.getCurrentProjectId();
	}

	@ModelAttribute("trialEnvironmentHiddenFields")
	public List<Integer> getTrialEnvironmentHiddenFields() {
		return this.buildVariableIDList(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString());
	}

	@RequestMapping(value = "/trialSettings", method = RequestMethod.GET)
	public String showCreateTrial(Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_SETTINGS);
	}

	@RequestMapping(value = "/environment", method = RequestMethod.GET)
	public String showEnvironments(Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_ENVIRONMENTS);
	}

	@RequestMapping(value = "/germplasm", method = RequestMethod.GET)
	public String showGermplasm(Model model, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form) {
		return this.showAjaxPage(model, BaseTrialController.URL_GERMPLASM);
	}

	@RequestMapping(value = "/treatment", method = RequestMethod.GET)
	public String showTreatmentFactors(Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_TREATMENT);
	}

	@RequestMapping(value = "/experimentalDesign", method = RequestMethod.GET)
	public String showExperimentalDesign(Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_EXPERIMENTAL_DESIGN);
	}

	@RequestMapping(value = "/measurements", method = RequestMethod.GET)
	public String showMeasurements(Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_MEASUREMENT);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/retrieveVariablePairs/{id}", method = RequestMethod.GET)
	public List<SettingDetail> retrieveVariablePairs(@PathVariable int id) {
		return super.retrieveVariablePairs(id);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public String submit(@RequestBody TrialData data) throws MiddlewareQueryException {
		this.processEnvironmentData(data.getEnvironments());
		List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
		basicDetails = this.addUserIdIfNecessary(basicDetails);
		// transfer over data from user input into the list of setting details stored in the session
		this.populateSettingData(basicDetails, data.getBasicDetails().getBasicDetails());

		List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
		combinedList.addAll(basicDetails);

		if (studyLevelConditions != null) {
			this.populateSettingData(studyLevelConditions, data.getTrialSettings().getUserInput());
			combinedList.addAll(studyLevelConditions);
		}

		String name = data.getBasicDetails().getBasicDetails().get(Integer.toString(TermId.STUDY_NAME.getId()));

		Dataset dataset =
				(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList, this.userSelection
						.getPlotsLevelList(), this.userSelection.getBaselineTraitsList(), this.userSelection, this.userSelection
						.getTrialLevelVariableList(), this.userSelection.getTreatmentFactors(),
						data.getTreatmentFactors().getCurrentData(), null, this.userSelection.getNurseryConditions(), false);

		SettingsUtil.setConstantLabels(dataset, this.userSelection.getConstantsWithLabels());
		Workbook workbook =
				SettingsUtil.convertXmlDatasetToWorkbook(dataset, false, this.userSelection.getExpDesignParams(),
						this.userSelection.getExpDesignVariables(), this.fieldbookMiddlewareService,
						this.userSelection.getExperimentalDesignVariables());

		List<MeasurementVariable> variablesForEnvironment = new ArrayList<MeasurementVariable>();
		variablesForEnvironment.addAll(workbook.getTrialVariables());

		List<MeasurementRow> trialEnvironmentValues =
				WorkbookUtil.createMeasurementRowsFromEnvironments(data.getEnvironments().getEnvironments(), variablesForEnvironment,
						this.userSelection.getExpDesignParams());
		workbook.setTrialObservations(trialEnvironmentValues);

		this.createStudyDetails(workbook, data.getBasicDetails());

		this.userSelection.setWorkbook(workbook);

		this.userSelection.setTrialEnvironmentValues(this.convertToValueReference(data.getEnvironments().getEnvironments()));

		this.fieldbookService.saveStudyColumnOrdering(workbook.getStudyId(), name, data.getColumnOrders(), workbook);

		return "success";
	}

	protected TabInfo prepareGermplasmTabInfo(boolean isClearSettings) {
		List<SettingDetail> initialDetailList = new ArrayList<SettingDetail>();
		List<Integer> initialSettingIDs = this.buildVariableIDList(AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString());

		for (Integer initialSettingID : initialSettingIDs) {
			try {
				SettingDetail detail = this.createSettingDetail(initialSettingID, null);
				initialDetailList.add(detail);
			} catch (MiddlewareQueryException e) {
				CreateTrialController.LOG.error(e.getMessage(), e);
			}

		}

		TabInfo info = new TabInfo();
		info.setSettings(initialDetailList);

		if (isClearSettings || this.userSelection.getPlotsLevelList() == null) {
			this.userSelection.setPlotsLevelList(initialDetailList);
		}

		return info;
	}

	protected TabInfo prepareEnvironmentsTabInfo(boolean isClearSettings) throws MiddlewareQueryException {
		TabInfo info = new TabInfo();
		EnvironmentData data = new EnvironmentData();
		int noOfEnvironments = Integer.parseInt(AppConstants.DEFAULT_NO_OF_ENVIRONMENT_COUNT.getString());
		data.setNoOfEnvironments(noOfEnvironments);
		info.setData(data);

		for (int i = 0; i < noOfEnvironments; i++) {
			data.getEnvironments().add(new Environment());
		}

		Map<String, Object> settingMap = new HashMap<String, Object>();
		List<SettingDetail> managementDetailList = new ArrayList<SettingDetail>();
		List<Integer> hiddenFields = this.buildVariableIDList(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString());

		for (Integer id : this.buildVariableIDList(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString())) {
			SettingDetail detail = this.createSettingDetail(id, null);
			for (Integer hiddenField : hiddenFields) {
				if (id.equals(hiddenField)) {
					detail.setHidden(true);
				}
			}

			managementDetailList.add(detail);
		}

		settingMap.put("managementDetails", managementDetailList);
		settingMap.put("trialConditionDetails", new ArrayList<SettingDetail>());

		if (isClearSettings || this.userSelection.getTrialLevelVariableList() == null || this.userSelection.getBasicDetails().isEmpty()) {
			this.userSelection.setTrialLevelVariableList(managementDetailList);
		}

		info.setSettingMap(settingMap);
		return info;
	}

	protected TabInfo prepareBasicDetailsTabInfo() throws MiddlewareQueryException {
		Map<String, String> basicDetails = new HashMap<String, String>();
		List<SettingDetail> initialDetailList = new ArrayList<SettingDetail>();
		List<Integer> initialSettingIDs = this.buildVariableIDList(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());

		for (Integer initialSettingID : initialSettingIDs) {
			try {
				basicDetails.put(initialSettingID.toString(), "");
				SettingDetail detail = this.createSettingDetail(initialSettingID, null);
				initialDetailList.add(detail);
			} catch (MiddlewareQueryException e) {
				CreateTrialController.LOG.error(e.getMessage(), e);
			}
		}

		BasicDetails basic = new BasicDetails();
		basic.setBasicDetails(basicDetails);

		basic.setUserID(this.getCurrentIbdbUserId());
		basic.setUserName(this.fieldbookService.getPersonByUserId(basic.getUserID()));

		TabInfo tab = new TabInfo();
		tab.setData(basic);

		if (this.userSelection.getBasicDetails() == null || this.userSelection.getBasicDetails().isEmpty()) {
			this.userSelection.setBasicDetails(initialDetailList);
		}

		return tab;
	}

	protected TabInfo prepareTrialSettingsTabInfo() {
		TabInfo info = new TabInfo();
		info.setSettings(new ArrayList<SettingDetail>());
		info.setData(new TrialSettingsBean());
		return info;
	}

	private List<SettingDetail> addUserIdIfNecessary(List<SettingDetail> basicDetails) throws MiddlewareQueryException {
		boolean found = false;
		List<SettingDetail> detailList = basicDetails;
		if (basicDetails == null) {
			detailList = new ArrayList<SettingDetail>();
		}
		for (SettingDetail detail : detailList) {
			if (detail.getVariable().getCvTermId() == TermId.STUDY_UID.getId()) {
				found = true;
				break;
			}
		}
		if (!found) {
			detailList.add(this.createSettingDetail(TermId.STUDY_UID.getId(), "STUDY_UID"));
		}
		return detailList;
	}

	@ResponseBody
	@RequestMapping(value = "/clearSettings", method = RequestMethod.GET)
	public String clearSettings() {
		try {
			this.prepareGermplasmTabInfo(true);
			this.prepareEnvironmentsTabInfo(true);

			this.prepareTrialSettingsTabInfo();
			this.prepareExperimentalDesignSpecialData();
			List<SettingDetail> detailList = new ArrayList<SettingDetail>();
			this.userSelection.setBaselineTraitsList(detailList);
			this.userSelection.setStudyLevelConditions(new ArrayList<SettingDetail>());
			this.userSelection.setNurseryConditions(new ArrayList<SettingDetail>());
			detailList = new ArrayList<SettingDetail>();
			this.userSelection.setTreatmentFactors(detailList);
			if (this.userSelection.getTemporaryWorkbook() != null) {
				this.userSelection.setTemporaryWorkbook(null);
			}
			if (this.userSelection.getImportedGermplasmMainInfo() != null) {
				this.userSelection.setImportedGermplasmMainInfo(null);
			}
		} catch (MiddlewareQueryException e) {
			CreateTrialController.LOG.error(e.getMessage(), e);
		}
		return "success";
	}

	@ResponseBody
	@RequestMapping(value = "/refresh/settings/tab", method = RequestMethod.GET)
	public Map<String, TabInfo> refreshSettingsTab() throws MiddlewareQueryException {
		Map<String, TabInfo> tabDetails = new HashMap<String, TabInfo>();

		Workbook trialWorkbook = this.userSelection.getWorkbook();
		this.userSelection.setConstantsWithLabels(trialWorkbook.getConstants());

		tabDetails.put(CreateTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		tabDetails.put(CreateTrialController.TRIAL_SETTINGS_DATA_TAB,
				this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));

		return tabDetails;
	}

	protected void setFieldbookMiddlewareService(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}
}
