package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.TrialData;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.SampleListService;
import org.generationcp.middleware.util.FieldbookListUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(OpenTrialController.URL)
@SessionAttributes("isCategoricalDescriptionView")
@Transactional
public class OpenTrialController extends BaseTrialController {

	public static final String TRIAL_SETTINGS_DATA = "trialSettingsData";
	public static final String SELECTION_VARIABLE_DATA = "selectionVariableData";
	public static final String MEASUREMENTS_DATA = "measurementsData";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String TRIAL = "TRIAL";
	public static final String URL = "/TrialManager/openTrial";
	@Deprecated
	public static final String IS_EXP_DESIGN_PREVIEW = "isExpDesignPreview";
	public static final String MEASUREMENT_ROW_COUNT = "measurementRowCount";
	public static final String ENVIRONMENT_DATA_TAB = "environmentData";
	public static final String MEASUREMENT_DATA_EXISTING = "measurementDataExisting";
	private static final Logger LOG = LoggerFactory.getLogger(OpenTrialController.class);
	public static final String IS_EXP_DESIGN_PREVIEW_FALSE = "0";
	public static final String IS_DELETED_ENVIRONMENT = "0";
	private static final String IS_PREVIEW_EDITABLE = "0";
	private static final int NO_LIST_ID = -1;
	public static final String REDIRECT = "redirect:";

	@Resource
	private ErrorHandlerService errorHandlerService;

	/**
	 * The Inventory list manager.
	 */
	@Resource
	private InventoryDataManager inventoryDataManager;

	@Resource
	private SampleListService sampleListService;

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

	@ModelAttribute("contextInfo")
	public ContextInfo getContextInfo() {
		return this.contextUtil.getContextInfoFromSession();
	}

	@ModelAttribute("cropName")
	public String getCropName() {
		return this.contextUtil.getProjectInContext().getCropType().getCropName();
	}

	@ModelAttribute("currentProgramId")
	public String getCurrentProgramId() {
		return this.contextUtil.getProjectInContext().getUniqueID();
	}

	@ModelAttribute("programMethodURL")
	public String getProgramMethod() {
		return this.fieldbookProperties.getProgramBreedingMethodsUrl();
	}

	@ModelAttribute("trialEnvironmentHiddenFields")
	public List<Integer> getTrialEnvironmentHiddenFields() {
		return this.buildVariableIDList(AppConstants.HIDE_STUDY_ENVIRONMENT_FIELDS.getString());
	}

	@ModelAttribute("unspecifiedLocationId")
	public Integer unspecifiedLocationId() {
		return this.getUnspecifiedLocationId();
	}

	@ModelAttribute("isCategoricalDescriptionView")
	public Boolean initIsCategoricalDescriptionView() {
		return Boolean.FALSE;
	}

	@ModelAttribute("operationMode")
	public String getOperationMode() {
		return "OPEN";
	}

	@RequestMapping(value = "/trialSettings", method = RequestMethod.GET)
	public String showCreateTrial(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_SETTINGS);
	}

	@RequestMapping(value = "/environment", method = RequestMethod.GET)
	public String showEnvironments(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_ENVIRONMENTS);
	}

	@RequestMapping(value = "/germplasm", method = RequestMethod.GET)
	public String showGermplasm(final Model model, @ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form) {
		return this.showAjaxPage(model, BaseTrialController.URL_GERMPLASM);
	}

	@RequestMapping(value = "/treatment", method = RequestMethod.GET)
	public String showTreatmentFactors(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_TREATMENT);
	}

	@RequestMapping(value = "/experimentalDesign", method = RequestMethod.GET)
	public String showExperimentalDesign(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_EXPERIMENTAL_DESIGN);
	}

	@RequestMapping(value = "/measurements", method = RequestMethod.GET)
	public String showMeasurements(@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model) {

		Workbook workbook = this.userSelection.getWorkbook();
		Integer measurementDatasetId = null;
		if (workbook != null) {

			if (workbook.getMeasurementDatesetId() != null) {
				measurementDatasetId = workbook.getMeasurementDatesetId();
			}

			// this is so we can preview the exp design
			if (this.userSelection.getTemporaryWorkbook() != null) {
				workbook = this.userSelection.getTemporaryWorkbook();
				// TODO Remove this flag it is no longer used on the front-end
				model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, OpenTrialController.IS_EXP_DESIGN_PREVIEW_FALSE);
			}

			this.userSelection.setMeasurementRowList(workbook.getObservations());
			if (measurementDatasetId != null) {
				form.setMeasurementDataExisting(this.fieldbookMiddlewareService
						.checkIfStudyHasMeasurementData(measurementDatasetId, SettingsUtil.buildVariates(workbook.getVariates())));
			} else {
				form.setMeasurementDataExisting(false);
			}

			form.setMeasurementVariables(workbook.getMeasurementDatasetVariablesView());
			model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT, this.studyDataManager.countExperiments(measurementDatasetId));
		}

		return this.showAjaxPage(model, BaseTrialController.URL_MEASUREMENT);
	}

	@ResponseBody
	@RequestMapping(value = "/columns", method = RequestMethod.POST)
	public List<MeasurementVariable> getColumns(@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model,
			final HttpServletRequest request) {
		return this.getLatestMeasurements(form, request);
	}

	@RequestMapping(value = "/{trialId}", method = RequestMethod.GET)
	public String openTrial(@ModelAttribute("createTrialForm") final CreateTrialForm form, @PathVariable final Integer trialId,
			final Model model, final HttpSession session, final RedirectAttributes redirectAttributes,
			@RequestParam(value = "crosseslistid", required = false) final String crossesListId) {

		model.addAttribute("createdCrossesListId", crossesListId);

		this.clearSessionData(session);
		session.setAttribute("createdCrossesListId", crossesListId);
		try {
			if (trialId != null && trialId != 0) {
				final DmsProject dmsProject = this.studyDataManager.getProject(trialId);
				if (dmsProject.getProgramUUID() == null) {
					return REDIRECT + ManageTrialController.URL + "?summaryId=" + trialId + "&summaryName=" + dmsProject.getName();
				}

				final Workbook workbook = this.fieldbookMiddlewareService.getStudyDataSet(trialId);

				// FIXME
				// See setStartingEntryNoAndPlotNoFromObservations() in
				// prepareExperimentalDesignTabInfo
				this.fieldbookMiddlewareService.loadAllObservations(workbook);

				this.removeAnalysisAndAnalysisSummaryVariables(workbook);

				this.userSelection.setConstantsWithLabels(workbook.getConstants());
				this.userSelection.setWorkbook(workbook);
				this.userSelection.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(workbook.getConditions()));
				this.userSelection
						.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(this.userSelection.getExperimentalDesignVariables()));
				this.userSelection.setTemporaryWorkbook(null);
				this.userSelection.setMeasurementRowList(workbook.getObservations());

				this.fieldbookMiddlewareService
						.setTreatmentFactorValues(workbook.getTreatmentFactors(), workbook.getMeasurementDatesetId());

				form.setMeasurementDataExisting(this.fieldbookMiddlewareService
						.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(),
								SettingsUtil.buildVariates(workbook.getVariates())));
				form.setStudyId(trialId);
				form.setGermplasmListId(this.getGermplasmListId(trialId));
				form.setStudyTypeName(dmsProject.getStudyType().getName());
				this.setModelAttributes(form, trialId, model, workbook);
				this.setUserSelectionImportedGermplasmMainInfo(this.userSelection, trialId, model);
			}
			return this.showAngularPage(model);

		} catch (final MiddlewareQueryException e) {
			OpenTrialController.LOG.debug(e.getMessage(), e);

			redirectAttributes.addFlashAttribute("redirectErrorMessage", this.errorHandlerService.getErrorMessagesAsString(e.getCode(),
					new String[] {AppConstants.STUDY.getString(), StringUtils.capitalize(AppConstants.STUDY.getString()),
							AppConstants.STUDY.getString()}, "\n"));
			return REDIRECT + ManageTrialController.URL;
		} catch (final ParseException e) {
			redirectAttributes.addFlashAttribute("redirectErrorMessage", this.errorHandlerService
					.getErrorMessagesAsString("study.error.parser.format.date.basic.details",
							new String[] {AppConstants.STUDY.getString(), StringUtils.capitalize(AppConstants.STUDY.getString()),
									AppConstants.STUDY.getString()}, "\n"));
			return REDIRECT + ManageTrialController.URL;
		}
	}

	protected Integer getGermplasmListId(final int studyId) {
		if (this.userSelection.getImportedAdvancedGermplasmList() == null) {
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			final GermplasmListType listType = GermplasmListType.STUDY;
			final List<GermplasmList> germplasmLists = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, listType);

			if (germplasmLists != null && !germplasmLists.isEmpty()) {
				final GermplasmList germplasmList = germplasmLists.get(0);

				if (germplasmList != null) {
					// BMS-1419, set the id to the original list's id
					mainInfo.setListId(germplasmList.getListRef() != null ? germplasmList.getListRef() : germplasmList.getId());
				}
			}
			this.userSelection.setImportedGermplasmMainInfo(mainInfo);
		}

		return this.userSelection.getImportedGermplasmMainInfo() != null
				&& this.userSelection.getImportedGermplasmMainInfo().getListId() != null ?
				this.userSelection.getImportedGermplasmMainInfo().getListId() :
				OpenTrialController.NO_LIST_ID;
	}

	protected void setUserSelectionImportedGermplasmMainInfo(final UserSelection userSelection, final Integer trialId, final Model model) {
		final List<GermplasmList> germplasmLists =
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(trialId, GermplasmListType.STUDY);
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			final GermplasmList germplasmList = germplasmLists.get(0);

			final List<ListDataProject> listDataProjects = this.fieldbookMiddlewareService.getListDataProject(germplasmList.getId());
			final long germplasmListChecksSize = this.fieldbookMiddlewareService
					.countListDataProjectByListIdAndEntryType(germplasmList.getId(), SystemDefinedEntryType.CHECK_ENTRY);

			if (listDataProjects != null && !listDataProjects.isEmpty()) {

				model.addAttribute("germplasmListSize", listDataProjects.size());
				model.addAttribute("germplasmChecksSize", germplasmListChecksSize);
				FieldbookListUtil.populateStockIdInListDataProject(listDataProjects, this.inventoryDataManager);
				final List<ImportedGermplasm> list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(listDataProjects);
				final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
				importedGermplasmList.setImportedGermplasms(list);
				final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
				// BMS-1419, set the id to the original list's id
				mainInfo.setListId(germplasmList.getListRef());
				mainInfo.setAdvanceImportType(true);
				mainInfo.setImportedGermplasmList(importedGermplasmList);

				userSelection.setImportedGermplasmMainInfo(mainInfo);
				userSelection.setImportValid(true);
			}
		}
	}

	protected void setModelAttributes(final CreateTrialForm form, final Integer trialId, final Model model, final Workbook trialWorkbook)
			throws ParseException {
		model.addAttribute("basicDetailsData", this.prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false, trialId));
		model.addAttribute("germplasmData", this.prepareGermplasmTabInfo(trialWorkbook.getFactors(), false));
		model.addAttribute(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		model.addAttribute(OpenTrialController.TRIAL_SETTINGS_DATA,
				this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
		model.addAttribute(OpenTrialController.MEASUREMENTS_DATA,
				this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.TRAIT, false));
		model.addAttribute(OpenTrialController.SELECTION_VARIABLE_DATA,
				this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.SELECTION_METHOD, false));
		model.addAttribute("experimentalDesignData", this.prepareExperimentalDesignTabInfo(trialWorkbook, false));

		model.addAttribute(OpenTrialController.MEASUREMENT_DATA_EXISTING, this.fieldbookMiddlewareService
				.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
						SettingsUtil.buildVariates(trialWorkbook.getVariates())));

		model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT,
				this.studyDataManager.countExperiments(trialWorkbook.getMeasurementDatesetId()));
		model.addAttribute("treatmentFactorsData", this.prepareTreatmentFactorsInfo(trialWorkbook.getTreatmentFactors(), false));
        model.addAttribute("studyTypes", this.studyDataManager.getAllVisibleStudyTypes());
		
		// so that we can reuse the same page being use for nursery
		model.addAttribute("createTrialForm", form);
		model.addAttribute("experimentalDesignSpecialData", this.prepareExperimentalDesignSpecialData());
		model.addAttribute("studyName", trialWorkbook.getStudyDetails().getLabel());
		model.addAttribute("description", trialWorkbook.getStudyDetails().getDescription());
		model.addAttribute("advancedList", this.getAdvancedList(trialId));
		model.addAttribute("sampleList", this.getSampleList(trialId));
		model.addAttribute("crossesList", this.getCrossesList(trialId));

		model.addAttribute("germplasmListSize", 0);
	}

	protected void clearSessionData(final HttpSession session) {
		SessionUtility.clearSessionData(session,
				new String[] {SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME,
						SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
	}

	/**
	 * @param data
	 * @return
	 * @throws MiddlewareQueryException
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@Transactional
	public Map<String, Object> submit(@RequestParam("replace") final int replace, @RequestBody final TrialData data) {

		this.processEnvironmentData(data.getEnvironments());

		final List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		final List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();

		final List<SettingDetail> combinedList = new ArrayList<>();
		combinedList.addAll(basicDetails);
		combinedList.addAll(studyLevelConditions);

		// transfer over data from user input into the list of setting details
		// stored in the session
		this.populateSettingData(this.userSelection.getBasicDetails(), data.getBasicDetails().getBasicDetails());
		this.populateSettingData(this.userSelection.getStudyLevelConditions(), data.getTrialSettings().getUserInput());

		if (this.userSelection.getPlotsLevelList() == null) {
			this.userSelection.setPlotsLevelList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getBaselineTraitsList() == null) {
			this.userSelection.setBaselineTraitsList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getStudyConditions() == null) {
			this.userSelection.setStudyConditions(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getTrialLevelVariableList() == null) {
			this.userSelection.setTrialLevelVariableList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getTreatmentFactors() == null) {
			this.userSelection.setTreatmentFactors(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getSelectionVariates() == null) {
			this.userSelection.setSelectionVariates(new ArrayList<SettingDetail>());
		}

		// TODO: add deleted selection variates
		// include deleted list if measurements are available
		SettingsUtil.addDeletedSettingsList(combinedList, this.userSelection.getDeletedStudyLevelConditions(),
				this.userSelection.getStudyLevelConditions());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedPlotLevelList(), this.userSelection.getPlotsLevelList());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedBaselineTraitsList(),
				this.userSelection.getBaselineTraitsList());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedStudyConditions(), this.userSelection.getStudyConditions());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedTrialLevelVariables(),
				this.userSelection.getTrialLevelVariableList());
		SettingsUtil
				.addDeletedSettingsList(null, this.userSelection.getDeletedTreatmentFactors(), this.userSelection.getTreatmentFactors());

		final String name = data.getBasicDetails().getStudyName();
		// retain measurement dataset id and trial dataset id
		final int trialDatasetId = this.userSelection.getWorkbook().getTrialDatasetId();
		final int measurementDatasetId = this.userSelection.getWorkbook().getMeasurementDatesetId();

		// Combining variates to baseline traits.
		this.userSelection.getBaselineTraitsList().addAll(this.userSelection.getSelectionVariates());

		final Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataSet(this.fieldbookMiddlewareService, name, this.userSelection,
				data.getTreatmentFactors().getCurrentData(), this.contextUtil.getCurrentProgramUUID());

		SettingsUtil.setConstantLabels(dataset, this.userSelection.getConstantsWithLabels());

		final Workbook workbook = SettingsUtil
				.convertXmlDatasetToWorkbook(dataset, this.userSelection.getExpDesignParams(), this.userSelection.getExpDesignVariables(),
						this.fieldbookMiddlewareService, this.userSelection.getExperimentalDesignVariables(),
						this.contextUtil.getCurrentProgramUUID());

		if (this.userSelection.isDesignGenerated()) {

			this.userSelection.setMeasurementRowList(null);
			this.userSelection.getWorkbook().setOriginalObservations(null);
			this.userSelection.getWorkbook().setObservations(null);

			this.addMeasurementVariablesToTrialObservationIfNecessary(data.getEnvironments(), workbook,
					this.userSelection.getTemporaryWorkbook().getTrialObservations());
		}

		this.assignOperationOnExpDesignVariables(workbook.getConditions());

		workbook.setOriginalObservations(this.userSelection.getWorkbook().getOriginalObservations());
		workbook.setTrialObservations(this.userSelection.getWorkbook().getTrialObservations());
		workbook.setTrialDatasetId(trialDatasetId);
		workbook.setMeasurementDatesetId(measurementDatasetId);

		final List<MeasurementVariable> variablesForEnvironment = new ArrayList<>();
		variablesForEnvironment.addAll(workbook.getTrialVariables());

		final List<MeasurementRow> trialEnvironmentValues = WorkbookUtil
				.createMeasurementRowsFromEnvironments(data.getEnvironments().getEnvironments(), variablesForEnvironment,
						this.userSelection.getExpDesignParams());
		workbook.setTrialObservations(trialEnvironmentValues);

		this.createStudyDetails(workbook, data.getBasicDetails());

		this.userSelection.setWorkbook(workbook);

		this.userSelection.setTrialEnvironmentValues(this.convertToValueReference(data.getEnvironments().getEnvironments()));

		final Map<String, Object> returnVal = new HashMap<>();
		returnVal.put(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(workbook, false));
		returnVal.put(OpenTrialController.MEASUREMENT_DATA_EXISTING, false);
		returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT, 0);

		// saving of measurement rows
		if (replace == 0) {
			try {
				WorkbookUtil.addMeasurementDataToRows(workbook.getFactors(), false, this.userSelection, this.ontologyService,
						this.fieldbookService, this.contextUtil.getCurrentProgramUUID());
				WorkbookUtil.addMeasurementDataToRows(workbook.getVariates(), true, this.userSelection, this.ontologyService,
						this.fieldbookService, this.contextUtil.getCurrentProgramUUID());

				workbook.setMeasurementDatasetVariables(null);
				workbook.setObservations(this.userSelection.getMeasurementRowList());

				this.userSelection.setWorkbook(workbook);

				this.fieldbookService.createIdNameVariablePairs(this.userSelection.getWorkbook(), new ArrayList<SettingDetail>(),
						AppConstants.ID_NAME_COMBINATION.getString(), true);

				// Set the flag that indicates whether the variates will be save
				// or not to false since it's already save after inline edit
				this.fieldbookMiddlewareService.saveMeasurementRows(workbook, this.contextUtil.getCurrentProgramUUID(), false);
				returnVal.put(OpenTrialController.MEASUREMENT_DATA_EXISTING, this.fieldbookMiddlewareService
						.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(),
								SettingsUtil.buildVariates(workbook.getVariates())));
				returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT, this.studyDataManager.countExperiments(measurementDatasetId));

				this.fieldbookService
						.saveStudyColumnOrdering(workbook.getStudyDetails().getId(), workbook.getStudyName(), data.getColumnOrders(),
								workbook);

				return returnVal;
			} catch (final MiddlewareQueryException e) {
				OpenTrialController.LOG.error(e.getMessage(), e);
				return new HashMap<>();
			}
		} else {
			return returnVal;
		}
	}

	/**
	 * assign UPDATE operation for existing experimental design variables
	 *
	 * @param conditions
	 */
	void assignOperationOnExpDesignVariables(final List<MeasurementVariable> conditions) {
		final VariableTypeList factors =
				this.studyDataManager.getAllStudyFactors(this.userSelection.getWorkbook().getStudyDetails().getId());

		for (final MeasurementVariable mvar : conditions) {
			// update the operation for experiment design variables :
			// EXP_DESIGN, EXP_DESIGN_SOURCE, NREP
			// only if these variables already exists in the existing trial
			if ((mvar.getTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId() || mvar.getTermId() == TermId.NUMBER_OF_REPLICATES.getId()
					|| mvar.getTermId() == TermId.EXPT_DESIGN_SOURCE.getId()) && factors.findById(mvar.getTermId()) != null) {
				mvar.setOperation(Operation.UPDATE);
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "/updateSavedTrial", method = RequestMethod.GET)
	public Map<String, Object> updateSavedTrial(@RequestParam(value = "trialID") final int id) throws ParseException {
		final Map<String, Object> returnVal = new HashMap<>();
		final Workbook trialWorkbook = this.fieldbookMiddlewareService.getStudyDataSet(id);
		this.fieldbookMiddlewareService.loadAllObservations(trialWorkbook);

		this.removeAnalysisAndAnalysisSummaryVariables(trialWorkbook);

		this.userSelection.setWorkbook(trialWorkbook);
		this.userSelection.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(trialWorkbook.getConditions()));
		this.userSelection.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(this.userSelection.getExperimentalDesignVariables()));
		returnVal.put(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		returnVal.put(OpenTrialController.MEASUREMENT_DATA_EXISTING, this.fieldbookMiddlewareService
				.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
						SettingsUtil.buildVariates(trialWorkbook.getVariates())));
		returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT,
				this.studyDataManager.countExperiments(trialWorkbook.getMeasurementDatesetId()));
		returnVal.put(OpenTrialController.MEASUREMENTS_DATA,
				this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.TRAIT, false));
		returnVal.put(OpenTrialController.SELECTION_VARIABLE_DATA,
				this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.SELECTION_METHOD, false));
		returnVal.put(OpenTrialController.TRIAL_SETTINGS_DATA, this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
		this.prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false, id);
		this.prepareGermplasmTabInfo(trialWorkbook.getFactors(), false);
		this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false);

		return returnVal;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/retrieveVariablePairs/{id}", method = RequestMethod.GET)
	public List<SettingDetail> retrieveVariablePairs(@PathVariable final int id) {
		return super.retrieveVariablePairs(id);
	}

	@ModelAttribute("nameTypes")
	public List<UserDefinedField> getNameTypes() {
		try {
			return this.fieldbookMiddlewareService.getGermplasmNameTypes();
		} catch (final MiddlewareQueryException e) {
			OpenTrialController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
	}

	/**
	 * Reset session variables after save.
	 */
	@ResponseBody
	@RequestMapping(value = "/recreate/session/variables", method = RequestMethod.GET)
	public Map<String, Object> resetSessionVariablesAfterSave(@ModelAttribute("createTrialForm") final CreateTrialForm form,
			final Model model) {
		final Workbook workbook = this.userSelection.getWorkbook();
		form.setMeasurementDataExisting(this.fieldbookMiddlewareService
				.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));

		this.resetSessionVariablesAfterSave(workbook);
		final Map<String, Object> result = new HashMap<>();
		result.put("success", "1");
		return result;
	}

	@RequestMapping(value = "/load/preview/measurement", method = RequestMethod.GET)
	public String loadPreviewMeasurement(@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model) {
		final Workbook workbook = this.userSelection.getTemporaryWorkbook();
		final Workbook originalWorkbook = this.userSelection.getWorkbook();
		this.userSelection.setMeasurementRowList(workbook.getObservations());
		model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, this.isPreviewEditable(originalWorkbook));
		return super.showAjaxPage(model, BaseTrialController.URL_DATATABLE);
	}

	protected String isPreviewEditable(final Workbook originalWorkbook) {
		String isPreviewEditable = IS_PREVIEW_EDITABLE;
		if (originalWorkbook == null || originalWorkbook.getStudyDetails() == null || originalWorkbook.getStudyDetails().getId() == null) {
			isPreviewEditable = "1";
		}
		return isPreviewEditable;
	}

	@ResponseBody
	@RequestMapping(value = "/load/dynamic/change/measurement", method = RequestMethod.POST)
	public Map<String, Object> loadDynamicChangeMeasurement(@ModelAttribute("createTrialForm") final CreateTrialForm form,
			final Model model, final HttpServletRequest request) {
		Workbook workbook = this.userSelection.getWorkbook();
		if (this.userSelection.getTemporaryWorkbook() != null) {
			workbook = this.userSelection.getTemporaryWorkbook();
		}

		List<MeasurementVariable> measurementDatasetVariables = new ArrayList<>();
		measurementDatasetVariables.addAll(workbook.getMeasurementDatasetVariablesView());

		final String listCsv = request.getParameter("variableList");

		if (!measurementDatasetVariables.isEmpty()) {
			final List<MeasurementVariable> newMeasurementDatasetVariables = this.getMeasurementVariableFactor(measurementDatasetVariables);
			this.getTraitsAndSelectionVariates(measurementDatasetVariables, newMeasurementDatasetVariables, listCsv);
			measurementDatasetVariables = newMeasurementDatasetVariables;
		}

		FieldbookUtil.setColumnOrderingOnWorkbook(workbook, form.getColumnOrders());
		measurementDatasetVariables = workbook.arrangeMeasurementVariables(measurementDatasetVariables);
		this.processPreLoadingMeasurementDataPage(true, form, workbook, measurementDatasetVariables, model,
				request.getParameter("deletedEnvironment"));
		final Map<String, Object> result = new HashMap<>();
		result.put("success", "1");
		return result;
	}

	private void processPreLoadingMeasurementDataPage(final boolean isTemporary, final CreateTrialForm form, final Workbook workbook,
			final List<MeasurementVariable> measurementDatasetVariables, final Model model, final String deletedEnvironments) {

		final Integer measurementDatasetId = workbook.getMeasurementDatesetId();
		final List<MeasurementVariable> variates = workbook.getVariates();

		if (!isTemporary) {
			this.userSelection.setWorkbook(workbook);
		}
		if (measurementDatasetId != null) {
			form.setMeasurementDataExisting(this.fieldbookMiddlewareService
					.checkIfStudyHasMeasurementData(measurementDatasetId, SettingsUtil.buildVariates(variates)));
		} else {
			form.setMeasurementDataExisting(false);
		}

		// remove deleted environment from existing observation
		if (deletedEnvironments.length() > 0 && !IS_DELETED_ENVIRONMENT.equals(deletedEnvironments)) {
			final Workbook tempWorkbook = this.processDeletedEnvironments(deletedEnvironments, measurementDatasetVariables, workbook);
			form.setMeasurementRowList(tempWorkbook.getObservations());
			model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT, this.studyDataManager.countExperiments(measurementDatasetId));
		}

		form.setMeasurementVariables(measurementDatasetVariables);
		this.userSelection.setMeasurementDatasetVariable(measurementDatasetVariables);
		model.addAttribute("createTrialForm", form);
		model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, this.isPreviewEditable(workbook));
	}

	private Workbook processDeletedEnvironments(final String deletedEnvironment,
			final List<MeasurementVariable> measurementDatasetVariables, final Workbook workbook) {

		Workbook tempWorkbook = this.userSelection.getTemporaryWorkbook();
		if (tempWorkbook == null) {
			tempWorkbook = this.generateTemporaryWorkbook();
		}

		// workbook.observations() collection is no longer pre-loaded into user
		// session when trial is opened. Load now as we need it to
		// keep environment deletion functionality working as before (all plots
		// assumed loaded).
		this.fieldbookMiddlewareService.loadAllObservations(workbook);

		final List<MeasurementRow> filteredObservations = this.getFilteredObservations(workbook.getObservations(), deletedEnvironment);
		final List<MeasurementRow> filteredTrialObservations =
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
		final List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		final List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
		// transfer over data from user input into the list of setting details
		// stored in the session
		final List<SettingDetail> combinedList = new ArrayList<>();
		combinedList.addAll(basicDetails);

		if (studyLevelConditions != null) {
			combinedList.addAll(studyLevelConditions);
		}

		final String name = StringUtils.EMPTY;

		final String description = StringUtils.EMPTY;
		final String startDate = StringUtils.EMPTY;
		final String endDate = StringUtils.EMPTY;
		final String studyUpdate = StringUtils.EMPTY;

		final Dataset dataset = (Dataset) SettingsUtil
				.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList, this.userSelection.getPlotsLevelList(),
						this.userSelection.getBaselineTraitsList(), this.userSelection, this.userSelection.getTrialLevelVariableList(),
						this.userSelection.getTreatmentFactors(), null, null, this.userSelection.getStudyConditions(),
						this.contextUtil.getCurrentProgramUUID(), description, startDate, endDate, studyUpdate);

		return SettingsUtil.convertXmlDatasetToWorkbook(dataset, this.contextUtil.getCurrentProgramUUID());
	}

	protected List<MeasurementRow> getFilteredTrialObservations(final List<MeasurementRow> trialObservations,
			final String deletedEnvironment) {

		if (IS_DELETED_ENVIRONMENT.equalsIgnoreCase(deletedEnvironment) || StringUtils.EMPTY.equalsIgnoreCase(deletedEnvironment)
				|| trialObservations == null) {
			return trialObservations;
		}

		List<MeasurementRow> filteredTrialObservations = new ArrayList<>();
		filteredTrialObservations.addAll(trialObservations);

		// remove the deleted trial instance
		for (final MeasurementRow row : trialObservations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (this.isATrialInstanceMeasurementVariable(data) && deletedEnvironment.equalsIgnoreCase(data.getValue())) {
					filteredTrialObservations.remove(row);
					break;
				}
			}
		}

		filteredTrialObservations = this.updateTrialInstanceNoAfterDelete(deletedEnvironment, filteredTrialObservations);

		return filteredTrialObservations;
	}

	private boolean isATrialInstanceMeasurementVariable(final MeasurementData data) {
		if (data.getMeasurementVariable() != null) {
			final MeasurementVariable var = data.getMeasurementVariable();
			if (var != null && data.getMeasurementVariable().getName() != null && (
					OpenTrialController.TRIAL_INSTANCE.equalsIgnoreCase(var.getName()) || OpenTrialController.TRIAL
							.equalsIgnoreCase(var.getName()))) {
				return true;
			}
		}
		return false;
	}

	protected List<MeasurementRow> updateTrialInstanceNoAfterDelete(final String deletedEnvironment,
			final List<MeasurementRow> filteredMeasurementRowList) {

		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		measurementRowList.addAll(filteredMeasurementRowList);

		for (final MeasurementRow row : measurementRowList) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (this.isATrialInstanceMeasurementVariable(data)) {
					this.updateEnvironmentThatIsGreaterThanDeletedEnvironment(deletedEnvironment, data);
					break;
				}
			}
		}

		return measurementRowList;
	}

	private void updateEnvironmentThatIsGreaterThanDeletedEnvironment(final String deletedEnvironment, final MeasurementData data) {
		final Integer deletedInstanceNo = Integer.valueOf(deletedEnvironment);
		Integer currentInstanceNo = Integer.valueOf(data.getValue());

		if (deletedInstanceNo < currentInstanceNo) {
			data.setValue(String.valueOf(--currentInstanceNo));
		}
	}

	protected List<MeasurementRow> getFilteredObservations(final List<MeasurementRow> observations, final String deletedEnvironment) {

		if (IS_DELETED_ENVIRONMENT.equalsIgnoreCase(deletedEnvironment) || StringUtils.EMPTY.equalsIgnoreCase(deletedEnvironment)) {
			return observations;
		}

		List<MeasurementRow> filteredObservations = new ArrayList<>();
		for (final MeasurementRow row : observations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (this.isATrialInstanceMeasurementVariable(data) && !deletedEnvironment.equalsIgnoreCase(data.getValue())
						&& !IS_DELETED_ENVIRONMENT.equalsIgnoreCase(data.getValue())) {
					filteredObservations.add(row);
					break;
				}
			}
		}

		filteredObservations = this.updateTrialInstanceNoAfterDelete(deletedEnvironment, filteredObservations);

		return filteredObservations;
	}

	protected List<SampleListDTO> getSampleList(final Integer trialId) {
		return this.sampleListService.getSampleLists(trialId);
	}

}
