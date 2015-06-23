
package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.bean.TrialData;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Controller
@RequestMapping(OpenTrialController.URL)
public class OpenTrialController extends BaseTrialController {

	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String TRIAL = "TRIAL";
	public static final String URL = "/TrialManager/openTrial";
	public static final String IS_EXP_DESIGN_PREVIEW = "isExpDesignPreview";
	public static final String MEASUREMENT_ROW_COUNT = "measurementRowCount";
	public static final String ENVIRONMENT_DATA_TAB = "environmentData";
	public static final String MEASUREMENT_DATA_EXISTING = "measurementDataExisting";
	private static final Logger LOG = LoggerFactory.getLogger(OpenTrialController.class);

	@Resource
	private StudyDataManager studyDataManagerImpl;
	@Resource
	private OntologyService ontologyService;

	@Resource
	private ErrorHandlerService errorHandlerService;

	@Override
	public String getContentName() {
		return "TrialManager/createTrial";
	}

	@ModelAttribute("programLocationURL")
	public String getProgramLocation() {
		return this.fieldbookProperties.getProgramLocationsUrl();
	}

	@ModelAttribute("projectID")
	public String getProgramID() {
		return this.getCurrentProjectId();
	}

	@ModelAttribute("programMethodURL")
	public String getProgramMethod() {
		return this.fieldbookProperties.getProgramBreedingMethodsUrl();
	}

	@ModelAttribute("trialEnvironmentHiddenFields")
	public List<Integer> getTrialEnvironmentHiddenFields() {
		return this.buildVariableIDList(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString());
	}

	@ModelAttribute("operationMode")
	public String getOperationMode() {
		return "OPEN";
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
	public String showMeasurements(@ModelAttribute("createTrialForm") CreateTrialForm form, Model model) {

		Workbook workbook = this.userSelection.getWorkbook();
		Integer measurementDatasetId = null;
		if (workbook != null) {

			if (workbook.getMeasurementDatesetId() != null) {
				measurementDatasetId = workbook.getMeasurementDatesetId();
			}

			// this is so we can preview the exp design
			if (this.userSelection.getTemporaryWorkbook() != null) {
				workbook = this.userSelection.getTemporaryWorkbook();
				model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, "0");
			}

			try {
				this.userSelection.setMeasurementRowList(workbook.getObservations());
				if (measurementDatasetId != null) {
					form.setMeasurementDataExisting(this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(measurementDatasetId,
							SettingsUtil.buildVariates(workbook.getVariates())));
				} else {
					form.setMeasurementDataExisting(false);
				}

				form.setMeasurementVariables(workbook.getMeasurementDatasetVariablesView());

				model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT, workbook.getObservations() != null ? workbook
						.getObservations().size() : 0);
			} catch (MiddlewareQueryException e) {
				OpenTrialController.LOG.error(e.getMessage(), e);
			}
		}

		return this.showAjaxPage(model, BaseTrialController.URL_MEASUREMENT);
	}

	@RequestMapping(value = "/{trialId}", method = RequestMethod.GET)
	public String openTrial(@ModelAttribute("createTrialForm") CreateTrialForm form, @PathVariable Integer trialId, Model model,
			HttpSession session, RedirectAttributes redirectAttributes) throws MiddlewareQueryException {
		this.clearSessionData(session);

		try {
			if (trialId != null && trialId != 0) {
				DmsProject dmsProject = this.studyDataManagerImpl.getProject(trialId);
				if (dmsProject.getProgramUUID() == null) {
					return "redirect:" + ManageTrialController.URL + "?summaryId=" + trialId + "&summaryName=" + dmsProject.getName();
				}
				final Workbook trialWorkbook = this.fieldbookMiddlewareService.getTrialDataSet(trialId);

				this.userSelection.setConstantsWithLabels(trialWorkbook.getConstants());
				this.userSelection.setWorkbook(trialWorkbook);
				this.userSelection
						.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(trialWorkbook.getConditions()));
				this.userSelection.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(this.userSelection
						.getExperimentalDesignVariables()));
				this.userSelection.setTemporaryWorkbook(null);
				this.userSelection.setMeasurementRowList(trialWorkbook.getObservations());

				this.fieldbookMiddlewareService.setTreatmentFactorValues(trialWorkbook.getTreatmentFactors(),
						trialWorkbook.getMeasurementDatesetId());

				form.setMeasurementDataExisting(this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(
						trialWorkbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(trialWorkbook.getVariates())));
				form.setStudyId(trialId);

				this.setModelAttributes(form, trialId, model, trialWorkbook);
				this.setUserSelectionImportedGermplasmMainInfo(trialId, model);
			}
			return this.showAngularPage(model);

		} catch (MiddlewareQueryException e) {
			OpenTrialController.LOG.debug(e.getMessage(), e);

			redirectAttributes.addFlashAttribute(
					"redirectErrorMessage",
					this.errorHandlerService.getErrorMessagesAsString(e.getCode(), new String[] {AppConstants.TRIAL.getString(),
							StringUtils.capitalize(AppConstants.TRIAL.getString()), AppConstants.TRIAL.getString()}, "\n"));
			return "redirect:" + ManageTrialController.URL;
		} catch (MiddlewareException e) {
			OpenTrialController.LOG.debug(e.getMessage(), e);

			redirectAttributes.addFlashAttribute(
					"redirectErrorMessage",
					e.getMessage());
			return "redirect:" + ManageTrialController.URL;
		}
	}

	protected void setUserSelectionImportedGermplasmMainInfo(Integer trialId, Model model) throws MiddlewareQueryException {
		List<GermplasmList> germplasmLists =
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(trialId), GermplasmListType.TRIAL);
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			GermplasmList germplasmList = germplasmLists.get(0);
			List<ListDataProject> data = this.fieldbookMiddlewareService.getListDataProject(germplasmList.getId());
			if (data != null && !data.isEmpty()) {
				model.addAttribute("germplasmListSize", data.size());
				List<ImportedGermplasm> list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(data);
				ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
				importedGermplasmList.setImportedGermplasms(list);
				ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
				mainInfo.setListId(germplasmList.getId());
				mainInfo.setAdvanceImportType(true);
				mainInfo.setImportedGermplasmList(importedGermplasmList);
				this.userSelection.setImportedGermplasmMainInfo(mainInfo);
				this.userSelection.setImportValid(true);
			}
		}
	}

	protected void setModelAttributes(CreateTrialForm form, Integer trialId, Model model, Workbook trialWorkbook)
			throws MiddlewareException {
		model.addAttribute("basicDetailsData", this.prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false, trialId));
		model.addAttribute("germplasmData", this.prepareGermplasmTabInfo(trialWorkbook.getFactors(), false));
		model.addAttribute(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		model.addAttribute("trialSettingsData", this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
		model.addAttribute("measurementsData", this.prepareMeasurementsTabInfo(trialWorkbook.getVariates(), false));
		model.addAttribute("experimentalDesignData",
				this.prepareExperimentalDesignTabInfo(trialWorkbook.getExperimentalDesignVariables(), false));
		model.addAttribute(
				OpenTrialController.MEASUREMENT_DATA_EXISTING,
				this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
						SettingsUtil.buildVariates(trialWorkbook.getVariates())));
		model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT, trialWorkbook.getObservations().size());
		model.addAttribute("treatmentFactorsData", this.prepareTreatmentFactorsInfo(trialWorkbook.getTreatmentFactors(), false));

		// so that we can reuse the same age being use for nursery
		model.addAttribute("createNurseryForm", form);
		model.addAttribute("experimentalDesignSpecialData", this.prepareExperimentalDesignSpecialData());
		model.addAttribute("studyName", trialWorkbook.getStudyDetails().getLabel());

		model.addAttribute("germplasmListSize", 0);
	}

	protected void clearSessionData(HttpSession session) {
		SessionUtility.clearSessionData(session, new String[] {SessionUtility.USER_SELECTION_SESSION_NAME,
				SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
	}

	/**
	 * @param data
	 * @return
	 * @throws MiddlewareQueryException
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, Object> submit(@RequestParam("replace") int replace, @RequestBody TrialData data) 
			throws MiddlewareException {

		this.processEnvironmentData(data.getEnvironments());
		List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
		// transfer over data from user input into the list of setting details stored in the session
		this.populateSettingData(basicDetails, data.getBasicDetails().getBasicDetails());

		List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
		combinedList.addAll(basicDetails);

		if (studyLevelConditions != null) {
			this.populateSettingData(studyLevelConditions, data.getTrialSettings().getUserInput());
			combinedList.addAll(studyLevelConditions);
		}

		if (this.userSelection.getPlotsLevelList() == null) {
			this.userSelection.setPlotsLevelList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getBaselineTraitsList() == null) {
			this.userSelection.setBaselineTraitsList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getNurseryConditions() == null) {
			this.userSelection.setNurseryConditions(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getTrialLevelVariableList() == null) {
			this.userSelection.setTrialLevelVariableList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getTreatmentFactors() == null) {
			this.userSelection.setTreatmentFactors(new ArrayList<SettingDetail>());
		}

		// include deleted list if measurements are available
		SettingsUtil.addDeletedSettingsList(combinedList, this.userSelection.getDeletedStudyLevelConditions(),
				this.userSelection.getStudyLevelConditions());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedPlotLevelList(), this.userSelection.getPlotsLevelList());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedBaselineTraitsList(),
				this.userSelection.getBaselineTraitsList());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedNurseryConditions(),
				this.userSelection.getNurseryConditions());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedTrialLevelVariables(),
				this.userSelection.getTrialLevelVariableList());
		SettingsUtil
				.addDeletedSettingsList(null, this.userSelection.getDeletedTreatmentFactors(), this.userSelection.getTreatmentFactors());

		String name = data.getBasicDetails().getBasicDetails().get(TermId.STUDY_NAME.getId());

		// retain measurement dataset id and trial dataset id
		int trialDatasetId = this.userSelection.getWorkbook().getTrialDatasetId();
		int measurementDatasetId = this.userSelection.getWorkbook().getMeasurementDatesetId();

		Dataset dataset =
				(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList, this.userSelection
						.getPlotsLevelList(), this.userSelection.getBaselineTraitsList(), this.userSelection, this.userSelection
						.getTrialLevelVariableList(), this.userSelection.getTreatmentFactors(),
						data.getTreatmentFactors().getCurrentData(), null, this.userSelection.getNurseryConditions(), 
						false,contextUtil.getCurrentProgramUUID());

		SettingsUtil.setConstantLabels(dataset, this.userSelection.getConstantsWithLabels());

		Workbook workbook =
				SettingsUtil.convertXmlDatasetToWorkbook(dataset, false, this.userSelection.getExpDesignParams(),
						this.userSelection.getExpDesignVariables(), this.fieldbookMiddlewareService,
						this.userSelection.getExperimentalDesignVariables(),
						contextUtil.getCurrentProgramUUID());

		if (this.userSelection.getTemporaryWorkbook() != null) {
			this.userSelection.setMeasurementRowList(null);
			this.userSelection.getWorkbook().setOriginalObservations(null);
			this.userSelection.getWorkbook().setObservations(null);
		}

		workbook.setOriginalObservations(this.userSelection.getWorkbook().getOriginalObservations());
		workbook.setTrialObservations(this.userSelection.getWorkbook().getTrialObservations());
		workbook.setTrialDatasetId(trialDatasetId);
		workbook.setMeasurementDatesetId(measurementDatasetId);

		List<MeasurementVariable> variablesForEnvironment = new ArrayList<MeasurementVariable>();
		variablesForEnvironment.addAll(workbook.getTrialVariables());

		List<MeasurementRow> trialEnvironmentValues =
				WorkbookUtil.createMeasurementRowsFromEnvironments(data.getEnvironments().getEnvironments(), variablesForEnvironment,
						this.userSelection.getExpDesignParams());
		workbook.setTrialObservations(trialEnvironmentValues);

		this.createStudyDetails(workbook, data.getBasicDetails());

		this.userSelection.setWorkbook(workbook);

		this.userSelection.setTrialEnvironmentValues(this.convertToValueReference(data.getEnvironments().getEnvironments()));

		Map<String, Object> returnVal = new HashMap<String, Object>();
		returnVal.put(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(workbook, false));
		returnVal.put(OpenTrialController.MEASUREMENT_DATA_EXISTING, false);
		returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT, 0);

		// saving of measurement rows
		if (this.userSelection.getMeasurementRowList() != null && !this.userSelection.getMeasurementRowList().isEmpty() && replace == 0) {
			try {
				WorkbookUtil.addMeasurementDataToRows(workbook.getFactors(), false, this.userSelection, 
						this.ontologyService,this.fieldbookService,contextUtil.getCurrentProgramUUID());
				WorkbookUtil.addMeasurementDataToRows(workbook.getVariates(), true, this.userSelection, 
						this.ontologyService,this.fieldbookService,contextUtil.getCurrentProgramUUID());

				workbook.setMeasurementDatasetVariables(null);
				workbook.setObservations(this.userSelection.getMeasurementRowList());

				this.userSelection.setWorkbook(workbook);

				this.fieldbookService.createIdNameVariablePairs(this.userSelection.getWorkbook(), new ArrayList<SettingDetail>(),
						AppConstants.ID_NAME_COMBINATION.getString(), true);

				this.fieldbookMiddlewareService.saveMeasurementRows(workbook,
						contextUtil.getCurrentProgramUUID());

				returnVal.put(
						OpenTrialController.MEASUREMENT_DATA_EXISTING,
						this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(),
								SettingsUtil.buildVariates(workbook.getVariates())));
				returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT, workbook.getObservations().size());

				this.fieldbookService.saveStudyColumnOrdering(workbook.getStudyDetails().getId(), workbook.getStudyName(),
						data.getColumnOrders(), workbook);

				return returnVal;
			} catch (MiddlewareQueryException e) {
				OpenTrialController.LOG.error(e.getMessage(), e);
				return new HashMap<String, Object>();
			}
		} else {
			return returnVal;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/updateSavedTrial", method = RequestMethod.GET)
	public Map<String, Object> updateSavedTrial(@RequestParam(value = "trialID") int id) throws MiddlewareException {
		Map<String, Object> returnVal = new HashMap<String, Object>();
		Workbook trialWorkbook = this.fieldbookMiddlewareService.getTrialDataSet(id);
		this.userSelection.setWorkbook(trialWorkbook);
		this.userSelection.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(trialWorkbook.getConditions()));
		this.userSelection.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(this.userSelection.getExperimentalDesignVariables()));
		returnVal.put(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		returnVal.put(
				OpenTrialController.MEASUREMENT_DATA_EXISTING,
				this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
						SettingsUtil.buildVariates(trialWorkbook.getVariates())));
		returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT, trialWorkbook.getObservations().size());
		returnVal.put("measurementsData", this.prepareMeasurementsTabInfo(trialWorkbook.getVariates(), false));
		this.prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false, id);
		this.prepareGermplasmTabInfo(trialWorkbook.getFactors(), false);
		this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false);

		return returnVal;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/retrieveVariablePairs/{id}", method = RequestMethod.GET)
	public List<SettingDetail> retrieveVariablePairs(@PathVariable int id) {
		return super.retrieveVariablePairs(id);
	}

	@ModelAttribute("nameTypes")
	public List<UserDefinedField> getNameTypes() {
		try {
			return this.fieldbookMiddlewareService.getGermplasmNameTypes();
		} catch (MiddlewareQueryException e) {
			OpenTrialController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<UserDefinedField>();
	}

	/**
	 * Reset session variables after save.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@RequestMapping(value = "/recreate/session/variables", method = RequestMethod.GET)
	public String resetSessionVariablesAfterSave(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model)
			throws MiddlewareException {
		Workbook workbook = this.userSelection.getWorkbook();
		form.setMeasurementDataExisting(this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(),
				SettingsUtil.buildVariates(workbook.getVariates())));

		this.resetSessionVariablesAfterSave(workbook, false);
		return this.loadMeasurementDataPage(false, form, workbook, workbook.getMeasurementDatasetVariablesView(), model, "");
	}

	/**
	 * Reset session variables after save.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@RequestMapping(value = "/load/measurement", method = RequestMethod.GET)
	public String loadMeasurement(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
		Workbook workbook = this.userSelection.getWorkbook();
		List<MeasurementVariable> measurementDatasetVariables = workbook.getMeasurementDatasetVariablesView();
		form.setMeasurementDataExisting(this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(),
				SettingsUtil.buildVariates(workbook.getVariates())));
		return this.loadMeasurementDataPage(false, form, workbook, measurementDatasetVariables, model, "");
	}

	@RequestMapping(value = "/load/preview/measurement", method = RequestMethod.GET)
	public String loadPreviewMeasurement(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model)
			throws MiddlewareQueryException {
		Workbook workbook = this.userSelection.getTemporaryWorkbook();
		Workbook originalWorkbook = this.userSelection.getWorkbook();
		this.userSelection.setMeasurementRowList(workbook.getObservations());
		model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, this.isPreviewEditable(originalWorkbook));
		return this.loadMeasurementDataPage(true, form, workbook, workbook.getMeasurementDatasetVariables(), model, "");
	}

	protected String isPreviewEditable(Workbook originalWorkbook) {
		String isPreviewEditable = "0";
		if (originalWorkbook == null || originalWorkbook.getStudyDetails() == null || originalWorkbook.getStudyDetails().getId() == null) {
			isPreviewEditable = "1";
		}
		return isPreviewEditable;
	}

	@RequestMapping(value = "/load/dynamic/change/measurement", method = RequestMethod.POST)
	public String loadDynamicChangeMeasurement(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model,
			HttpServletRequest request) throws MiddlewareException {
		boolean isInPreviewMode = false;
		Workbook workbook = this.userSelection.getWorkbook();
		if (this.userSelection.getTemporaryWorkbook() != null) {
			isInPreviewMode = true;
			workbook = this.userSelection.getTemporaryWorkbook();
		}

		List<MeasurementVariable> measurementDatasetVariables = new ArrayList<MeasurementVariable>();
		measurementDatasetVariables.addAll(workbook.getMeasurementDatasetVariablesView());
		// we show only traits that are being passed by the frontend
		String traitsListCsv = request.getParameter("traitsList");

		List<MeasurementVariable> newMeasurementDatasetVariables = new ArrayList<MeasurementVariable>();

		List<SettingDetail> traitList = this.userSelection.getBaselineTraitsList();

		if (!measurementDatasetVariables.isEmpty()) {
			for (MeasurementVariable var : measurementDatasetVariables) {
				if (var.isFactor()) {
					newMeasurementDatasetVariables.add(var);
				}
			}
			if (traitsListCsv != null && !"".equalsIgnoreCase(traitsListCsv)) {
				StringTokenizer token = new StringTokenizer(traitsListCsv, ",");
				while (token.hasMoreTokens()) {
					int id = Integer.valueOf(token.nextToken());
					MeasurementVariable currentVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, id);
					if (currentVar == null) {
						StandardVariable var = this.fieldbookMiddlewareService.getStandardVariable(id,
								contextUtil.getCurrentProgramUUID());
						MeasurementVariable newVar =
								ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, this.fieldbookService);
						newVar.setFactor(false);
						newMeasurementDatasetVariables.add(newVar);
						SettingsUtil.findAndUpdateVariableName(traitList, newVar);
					} else {
						newMeasurementDatasetVariables.add(currentVar);
						SettingsUtil.findAndUpdateVariableName(traitList, currentVar);
					}
				}
			}
			measurementDatasetVariables = newMeasurementDatasetVariables;
		}

		// we do a cleanup here
		if (isInPreviewMode) {
			model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, "0");
		}
		FieldbookUtil.setColumnOrderingOnWorkbook(workbook, form.getColumnOrders());
		measurementDatasetVariables = workbook.arrangeMeasurementVariables(measurementDatasetVariables);
		return this.loadMeasurementDataPage(true, form, workbook, measurementDatasetVariables, model,
				request.getParameter("deletedEnvironment"));
	}

	private String loadMeasurementDataPage(boolean isTemporary, CreateNurseryForm form, Workbook workbook,
			List<MeasurementVariable> measurementDatasetVariables, Model model, String deletedEnvironments) throws MiddlewareQueryException {

		List<MeasurementRow> observations = workbook.getObservations();
		Integer measurementDatasetId = workbook.getMeasurementDatesetId();
		List<MeasurementVariable> variates = workbook.getVariates();

		// set measurements data
		this.userSelection.setMeasurementRowList(observations);
		if (!isTemporary) {
			this.userSelection.setWorkbook(workbook);
		}
		if (measurementDatasetId != null) {
			form.setMeasurementDataExisting(this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(measurementDatasetId,
					SettingsUtil.buildVariates(variates)));
		} else {
			form.setMeasurementDataExisting(false);
		}
		// we do a matching of the name here so there won't be a problem in the data table
		if (observations != null && !observations.isEmpty()) {
			List<MeasurementData> dataList = observations.get(0).getDataList();
			for (MeasurementData data : dataList) {
				this.processMeasurementVariable(measurementDatasetVariables, data);
			}
			this.userSelection.setMeasurementRowList(observations);
		}
		// remove deleted environment from existing observation
		if (deletedEnvironments.length() > 0 && !"0".equals(deletedEnvironments)) {
			Workbook tempWorkbook = this.processDeletedEnvironments(deletedEnvironments, measurementDatasetVariables, workbook);
			form.setMeasurementRowList(tempWorkbook.getObservations());
			model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT, tempWorkbook.getObservations() != null ? tempWorkbook
					.getObservations().size() : 0);
		}

		form.setMeasurementVariables(measurementDatasetVariables);
		this.userSelection.setMeasurementDatasetVariable(measurementDatasetVariables);
		model.addAttribute("createNurseryForm", form);
		return super.showAjaxPage(model, BaseTrialController.URL_DATATABLE);
	}

	private void processMeasurementVariable(List<MeasurementVariable> measurementDatasetVariables, MeasurementData data) {
		if (data.getMeasurementVariable() != null) {
			MeasurementVariable var =
					WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, data.getMeasurementVariable().getTermId());
			if (var != null && data.getMeasurementVariable().getName() != null) {
				var.setName(data.getMeasurementVariable().getName());
			}
		}
	}

	private Workbook processDeletedEnvironments(String deletedEnvironment, List<MeasurementVariable> measurementDatasetVariables,
			Workbook workbook) {

		Workbook tempWorkbook = this.userSelection.getTemporaryWorkbook();
		if (tempWorkbook == null) {
			tempWorkbook = this.generateTemporaryWorkbook();
		}

		List<MeasurementRow> filteredObservations =
				this.getFilteredObservations(this.userSelection.getMeasurementRowList(), deletedEnvironment);
		List<MeasurementRow> filteredTrialObservations =
				this.getFilteredTrialObservations(workbook.getTrialObservations(), deletedEnvironment);

		tempWorkbook.setTrialObservations(filteredTrialObservations);
		tempWorkbook.setObservations(filteredObservations);
		tempWorkbook.setMeasurementDatasetVariables(measurementDatasetVariables);

		this.userSelection.setTemporaryWorkbook(tempWorkbook);
		this.userSelection.setMeasurementRowList(filteredObservations);
		this.userSelection.getWorkbook().setTrialObservations(filteredTrialObservations);
		this.userSelection.getWorkbook().setObservations(filteredObservations);
		this.userSelection.getWorkbook().setMeasurementDatasetVariables(measurementDatasetVariables);

		return tempWorkbook;
	}

	private Workbook generateTemporaryWorkbook() {
		List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
		// transfer over data from user input into the list of setting details stored in the session
		List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
		combinedList.addAll(basicDetails);

		if (studyLevelConditions != null) {
			combinedList.addAll(studyLevelConditions);
		}

		String name = "";

		Dataset dataset =
				(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList,
						this.userSelection.getPlotsLevelList(), this.userSelection.getBaselineTraitsList(), this.userSelection,
						this.userSelection.getTrialLevelVariableList(), this.userSelection.getTreatmentFactors(), null, null,
						this.userSelection.getNurseryConditions(), false,
						contextUtil.getCurrentProgramUUID());

		Workbook tempWorkbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false,
				contextUtil.getCurrentProgramUUID());
		StudyDetails details = new StudyDetails();
		details.setStudyType(StudyType.T);
		tempWorkbook.setStudyDetails(details);

		return tempWorkbook;
	}

	protected List<MeasurementRow> getFilteredTrialObservations(List<MeasurementRow> trialObservations, String deletedEnvironment) {

		if ("0".equalsIgnoreCase(deletedEnvironment) || "".equalsIgnoreCase(deletedEnvironment) || trialObservations == null) {
			return trialObservations;
		}

		List<MeasurementRow> filteredTrialObservations = new ArrayList<MeasurementRow>();
		filteredTrialObservations.addAll(trialObservations);

		// remove the deleted trial instance
		for (MeasurementRow row : trialObservations) {
			List<MeasurementData> dataList = row.getDataList();
			for (MeasurementData data : dataList) {
				if (this.isATrialInstanceMeasurementVariable(data) && deletedEnvironment.equalsIgnoreCase(data.getValue())) {
					filteredTrialObservations.remove(row);
					break;
				}
			}
		}

		filteredTrialObservations = this.updateTrialInstanceNoAfterDelete(deletedEnvironment, filteredTrialObservations);

		return filteredTrialObservations;
	}

	private boolean isATrialInstanceMeasurementVariable(MeasurementData data) {
		if (data.getMeasurementVariable() != null) {
			MeasurementVariable var = data.getMeasurementVariable();
			if (var != null
					&& data.getMeasurementVariable().getName() != null
					&& (OpenTrialController.TRIAL_INSTANCE.equalsIgnoreCase(var.getName()) || OpenTrialController.TRIAL
							.equalsIgnoreCase(var.getName()))) {
				return true;
			}
		}
		return false;
	}

	protected List<MeasurementRow> updateTrialInstanceNoAfterDelete(String deletedEnvironment,
			List<MeasurementRow> filteredMeasurementRowList) {

		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		measurementRowList.addAll(filteredMeasurementRowList);

		for (MeasurementRow row : measurementRowList) {
			List<MeasurementData> dataList = row.getDataList();
			for (MeasurementData data : dataList) {
				if (this.isATrialInstanceMeasurementVariable(data)) {
					this.updateEnvironmentThatIsGreaterThanDeletedEnvironment(deletedEnvironment, data);
					break;
				}
			}
		}

		return measurementRowList;
	}

	private void updateEnvironmentThatIsGreaterThanDeletedEnvironment(String deletedEnvironment, MeasurementData data) {
		Integer deletedInstanceNo = Integer.valueOf(deletedEnvironment);
		Integer currentInstanceNo = Integer.valueOf(data.getValue());

		if (deletedInstanceNo < currentInstanceNo) {
			data.setValue(String.valueOf(--currentInstanceNo));
		}
	}

	protected List<MeasurementRow> getFilteredObservations(List<MeasurementRow> observations, String deletedEnvironment) {

		if ("0".equalsIgnoreCase(deletedEnvironment) || "".equalsIgnoreCase(deletedEnvironment)) {
			return observations;
		}

		List<MeasurementRow> filteredObservations = new ArrayList<MeasurementRow>();
		for (MeasurementRow row : observations) {
			List<MeasurementData> dataList = row.getDataList();
			for (MeasurementData data : dataList) {
				if (this.isATrialInstanceMeasurementVariable(data) && !deletedEnvironment.equalsIgnoreCase(data.getValue())
						&& !"0".equalsIgnoreCase(data.getValue())) {
					filteredObservations.add(row);
					break;
				}
			}
		}

		filteredObservations = this.updateTrialInstanceNoAfterDelete(deletedEnvironment, filteredObservations);

		return filteredObservations;
	}
}
