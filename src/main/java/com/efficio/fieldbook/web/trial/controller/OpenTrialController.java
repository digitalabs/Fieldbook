package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.AdvanceList;
import com.efficio.fieldbook.web.trial.bean.CrossesList;
import com.efficio.fieldbook.web.trial.bean.TrialData;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.ValueReference;
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
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.SampleListService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping(OpenTrialController.URL)
@SessionAttributes("isCategoricalDescriptionView")
@Transactional
public class OpenTrialController extends BaseTrialController {

	static final String TRIAL_SETTINGS_DATA = "trialSettingsData";
	// TODO: MARK FOR DELETE IBP-2789
	//static final String SELECTION_VARIABLE_DATA = "selectionVariableData";
	//static final String MEASUREMENTS_DATA = "measurementsData";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String TRIAL = "TRIAL";
	public static final String URL = "/TrialManager/openTrial";
	@Deprecated
	public static final String IS_EXP_DESIGN_PREVIEW = "isExpDesignPreview";
	private static final String CONTAINS_OUT_OF_SYNC_VALUES = "containsOutOfSyncValues";
	static final String HAS_GENERATED_DESIGN = "hasGeneratedDesign";
	static final String ENVIRONMENT_DATA_TAB = "environmentData";
	// TODO: MARK FOR DELETE IBP-2789
	//static final String MEASUREMENT_DATA_EXISTING = "measurementDataExisting";
	static final String HAS_LISTS_OR_SUB_OBS = "hasListsOrSubObs";
	private static final Logger LOG = LoggerFactory.getLogger(OpenTrialController.class);
	private static final String IS_EXP_DESIGN_PREVIEW_FALSE = "0";
	private static final String IS_DELETED_ENVIRONMENT = "0";
	private static final String IS_PREVIEW_EDITABLE = "0";
	private static final int NO_LIST_ID = -1;
	private static final String REDIRECT = "redirect:";

	@Resource
	private ErrorHandlerService errorHandlerService;

	@Resource
	private DatasetService datasetService;

	@Resource
	DatasetTypeService datasetTypeService;

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

	@ModelAttribute("currentCropUserId")
	public Integer getCurrentCropUserId() {
		return this.contextUtil.getCurrentIbdbUserId();
	}

	@ModelAttribute("maxNumOfSubObsSets")
	public Integer getMaxNumOfSubObsSets() {
		return this.fieldbookProperties.getMaxNumOfSubObsSetsPerStudy();
	}

	@ModelAttribute("maxNumOfSubObsSetsPerParentUnit")
	public Integer getMaxNumOfSubObsSetsPerParentunit() {
		return this.fieldbookProperties.getMaxNumOfSubObsPerParentUnit();
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

	// TODO: MARK FOR DELETE IBP-2789
	@Deprecated
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
			// TODO: MARK FOR DELETE IBP-2789
			//model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT, this.studyDataManager.countExperiments(measurementDatasetId));
		}

		return this.showAjaxPage(model, BaseTrialController.URL_MEASUREMENT);
	}

	// TODO: MARK FOR DELETE IBP-2689
	@Deprecated
	@ResponseBody
	@RequestMapping(value = "/columns", method = RequestMethod.POST)
	public List<MeasurementVariable> getColumns(
		@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model,
		final HttpServletRequest request) {
		return this.getLatestMeasurements(form, request);
	}

	@RequestMapping(value = "/subObservationTab", method = RequestMethod.GET)
	public String showSubObservationTab(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_SUB_OBSERVATION_TAB);
	}

	@RequestMapping(value = "/subObservationSet", method = RequestMethod.GET)
	public String showSubObservationSet(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_SUB_OBSERVATION_SET);
	}

	@RequestMapping(value = "/{trialId}", method = RequestMethod.GET)
	public String openTrial(
		@ModelAttribute("createTrialForm") final CreateTrialForm form, @PathVariable final Integer trialId,
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
					.checkIfStudyHasMeasurementData(
						workbook.getMeasurementDatesetId(),
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
				new String[] {
					AppConstants.STUDY.getString(), StringUtils.capitalize(AppConstants.STUDY.getString()),
					AppConstants.STUDY.getString()}, "\n"));
			return REDIRECT + ManageTrialController.URL;
		} catch (final ParseException e) {
			redirectAttributes.addFlashAttribute("redirectErrorMessage", this.errorHandlerService
				.getErrorMessagesAsString("study.error.parser.format.date.basic.details",
					new String[] {
						AppConstants.STUDY.getString(), StringUtils.capitalize(AppConstants.STUDY.getString()),
						AppConstants.STUDY.getString()}, "\n"));
			return REDIRECT + ManageTrialController.URL;
		}
	}

	private Integer getGermplasmListId(final int studyId) {
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

	void setUserSelectionImportedGermplasmMainInfo(final UserSelection userSelection, final Integer trialId, final Model model) {
		final List<GermplasmList> germplasmLists =
			this.fieldbookMiddlewareService.getGermplasmListsByProjectId(trialId, GermplasmListType.STUDY);
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			final GermplasmList germplasmList = germplasmLists.get(0);

			final List<ListDataProject> listDataProjects = this.fieldbookMiddlewareService.getListDataProject(germplasmList.getId());

			long germplasmListChecksSize = 0;
			if (this.userSelection.getExpDesignParams().getDesignType() == ExperimentDesignType.P_REP.getId()) {

				germplasmListChecksSize = this.getGermplasmListChecksSize(germplasmList.getId());
			} else {
				germplasmListChecksSize = this.fieldbookMiddlewareService
					.countListDataProjectByListIdAndEntryTypeIds(
						germplasmList.getId(), Arrays.asList(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()));
			}

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

	long getGermplasmListChecksSize(final int germplasmListId) {
		final List<ValueReference> entryTypes = this.fieldbookService.getAllPossibleValues(TermId.ENTRY_TYPE.getId(), true);
		final List<Integer> checkEntryTypeIds = new ArrayList<>();
		for (final ValueReference entryType : entryTypes) {
			if (SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() != entryType.getId()) {
				checkEntryTypeIds.add(entryType.getId());
			}
		}
		return this.fieldbookMiddlewareService.countListDataProjectByListIdAndEntryTypeIds(germplasmListId, checkEntryTypeIds);
	}

	protected void setModelAttributes(final CreateTrialForm form, final Integer trialId, final Model model, final Workbook trialWorkbook)
		throws ParseException {
		final List<AdvanceList> advanceLists = this.getAdvancedList(trialId);
		final List<CrossesList> crossesLists = this.getCrossesList(trialId);
		final List<SampleListDTO> sampleListDTOS = this.getSampleList(trialWorkbook.getStudyDetails().getId());

		final List<Integer> datasetTypeIds = this.datasetTypeService.getSubObservationDatasetTypeIds();
		final List<DatasetDTO> datasetDTOS = this.datasetService.getDatasets(trialId, new HashSet<>(datasetTypeIds));

		final boolean hasListOrSubObs =
			!advanceLists.isEmpty() || !crossesLists.isEmpty() || !sampleListDTOS.isEmpty() || !datasetDTOS.isEmpty();
		model.addAttribute("basicDetailsData", this.prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false, trialId));
		model.addAttribute("germplasmData", this.prepareGermplasmTabInfo(trialWorkbook.getFactors(), false));
		model.addAttribute(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		model.addAttribute(
			OpenTrialController.TRIAL_SETTINGS_DATA,
			this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
		// TODO: MARK FOR DELETE IBP-2789
/*		model.addAttribute(
			OpenTrialController.MEASUREMENTS_DATA,
			this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.TRAIT, false));*/
		// TODO: MARK FOR DELETE IBP-2789
		/*model.addAttribute(
			OpenTrialController.SELECTION_VARIABLE_DATA,
			this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.SELECTION_METHOD, false));*/
		model.addAttribute("experimentalDesignData", this.prepareExperimentalDesignTabInfo(trialWorkbook, false));
		// TODO: MARK FOR DELETE IBP-2789
		/*model.addAttribute(OpenTrialController.MEASUREMENT_DATA_EXISTING, this.fieldbookMiddlewareService
			.checkIfStudyHasMeasurementData(
				trialWorkbook.getMeasurementDatesetId(),
				SettingsUtil.buildVariates(trialWorkbook.getVariates())));*/
		model.addAttribute(
			OpenTrialController.HAS_LISTS_OR_SUB_OBS, hasListOrSubObs);

		// TODO: MARK FOR DELETE IBP-2789
		/*model.addAttribute(
			OpenTrialController.MEASUREMENT_ROW_COUNT,
			this.studyDataManager.countExperiments(trialWorkbook.getMeasurementDatesetId()));*/
		model.addAttribute(OpenTrialController.HAS_GENERATED_DESIGN, this.studyDataManager.countExperiments(trialWorkbook.getMeasurementDatesetId()) > 0);
		model.addAttribute("treatmentFactorsData", this.prepareTreatmentFactorsInfo(trialWorkbook.getTreatmentFactors(), false));
		model.addAttribute("studyTypes", this.studyDataManager.getAllVisibleStudyTypes());

		// so that we can reuse the same page being use for nursery
		model.addAttribute("createTrialForm", form);
		model.addAttribute("experimentalDesignSpecialData", this.prepareExperimentalDesignSpecialData());
		model.addAttribute("studyName", trialWorkbook.getStudyDetails().getLabel());
		model.addAttribute("description", trialWorkbook.getStudyDetails().getDescription());
		model.addAttribute("advancedList", advanceLists);
		model.addAttribute("sampleList", sampleListDTOS);
		model.addAttribute("crossesList", crossesLists);

		model.addAttribute("germplasmListSize", 0);
		model.addAttribute("studyId", trialWorkbook.getStudyDetails().getId());
		model.addAttribute("measurementDatasetId", trialWorkbook.getMeasurementDatesetId());

		this.setIsSuperAdminAttribute(model);
	}

	private void clearSessionData(final HttpSession session) {
		SessionUtility.clearSessionData(
			session,
			new String[] {
				SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME,
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
		// transfer over data from user input into the list of setting details
		// stored in the session
		this.populateSettingData(this.userSelection.getBasicDetails(), data.getBasicDetails().getBasicDetails());
		this.populateSettingData(this.userSelection.getStudyLevelConditions(), data.getTrialSettings().getUserInput());

		this.initializeBasicUserSelectionLists();
		this.addDeletedSettingsList();
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
		// TODO: MARK FOR DELETE IBP-2789
		/*if (this.userSelection.isDesignGenerated()) {

			this.userSelection.setMeasurementRowList(null);
			this.userSelection.getWorkbook().setOriginalObservations(null);
			this.userSelection.getWorkbook().setObservations(null);

			this.addMeasurementVariablesToTrialObservationIfNecessary(data.getEnvironments().getEnvironments(), workbook,
				this.userSelection.getTemporaryWorkbook().getTrialObservations());
		}*/

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
		//returnVal.put(OpenTrialController.MEASUREMENT_DATA_EXISTING, false);
		// TODO: MARK FOR DELETE IBP-2789
		//returnVal.put(OpenTrialController.HAS_ADVANCED_OR_CROSSES_LIST, false);
		// TODO: MARK FOR DELETE IBP-2789
		//returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT, 0);

		// saving variables with generated design
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
				// TODO VER ESTO PARA CAMBIAR SI SE PUEDE: MARK FOR DELETE IBP-2789
				this.fieldbookMiddlewareService.saveWorkbookVariablesAndObservations(workbook, this.contextUtil.getCurrentProgramUUID());
				//this.fieldbookMiddlewareService.updatePhenotypeStatus(workbook.getObservations());
				// TODO: MARK FOR DELETE IBP-2789
				/*returnVal.put(OpenTrialController.MEASUREMENT_DATA_EXISTING, this.fieldbookMiddlewareService
					.checkIfStudyHasMeasurementData(
						workbook.getMeasurementDatesetId(),
						SettingsUtil.buildVariates(workbook.getVariates())));*/
				/*returnVal.put(
					OpenTrialController.HAS_ADVANCED_OR_CROSSES_LIST,
					this.fieldbookMiddlewareService.hasAdvancedOrCrossesList(workbook.getStudyDetails().getId()));*/
				// TODO: MARK FOR DELETE IBP-2789
				//returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT, this.studyDataManager.countExperiments(measurementDatasetId));

				this.fieldbookService
					.saveStudyColumnOrdering(workbook.getStudyDetails().getId(), workbook.getStudyName(), data.getColumnOrders(),
						workbook);
				final Boolean hasOutOfSyncObservations =
					this.fieldbookMiddlewareService.hasOutOfSyncObservations(workbook.getMeasurementDatesetId());
				returnVal.put(OpenTrialController.CONTAINS_OUT_OF_SYNC_VALUES, hasOutOfSyncObservations);
				return returnVal;
			} catch (final MiddlewareQueryException e) {
				OpenTrialController.LOG.error(e.getMessage(), e);
				return new HashMap<>();
			}
		} else {
			return returnVal;
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
		// TODO: MARK FOR DELETE IBP-2789
		/*returnVal.put(OpenTrialController.MEASUREMENT_DATA_EXISTING, this.fieldbookMiddlewareService
			.checkIfStudyHasMeasurementData(
				trialWorkbook.getMeasurementDatesetId(),
				SettingsUtil.buildVariates(trialWorkbook.getVariates())));*/
		//returnVal.put(OpenTrialController.HAS_ADVANCED_OR_CROSSES_LIST, this.fieldbookMiddlewareService.hasAdvancedOrCrossesList(id));
		// TODO: MARK FOR DELETE IBP-2789
		/*returnVal.put(
			OpenTrialController.MEASUREMENT_ROW_COUNT,
			this.studyDataManager.countExperiments(trialWorkbook.getMeasurementDatesetId()));*/
/*		returnVal.put(
			OpenTrialController.MEASUREMENTS_DATA,
			this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.TRAIT, false));*/
		// TODO: MARK FOR DELETE IBP-2789
		/*returnVal.put(
			OpenTrialController.SELECTION_VARIABLE_DATA,
			this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.SELECTION_METHOD, false));*/
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
	public Map<String, Object> resetSessionVariablesAfterSave(
		@ModelAttribute("createTrialForm") final CreateTrialForm form,
		final Model model) {
		final Workbook workbook = this.userSelection.getWorkbook();
		form.setMeasurementDataExisting(this.fieldbookMiddlewareService
			.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));

		this.resetSessionVariablesAfterSave(workbook);
		final Map<String, Object> result = new HashMap<>();
		result.put("success", "1");
		return result;
	}

	// TODO: MARK FOR DELETE IBP-2789
	@Deprecated
	@RequestMapping(value = "/load/preview/measurement", method = RequestMethod.GET)
	public String loadPreviewMeasurement(@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model) {
		final Workbook workbook = this.userSelection.getTemporaryWorkbook();
		final Workbook originalWorkbook = this.userSelection.getWorkbook();
		this.userSelection.setMeasurementRowList(workbook.getObservations());
		model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, this.isPreviewEditable(originalWorkbook));
		return super.showAjaxPage(model, BaseTrialController.URL_DATATABLE);
	}

	String isPreviewEditable(final Workbook originalWorkbook) {
		String isPreviewEditable = IS_PREVIEW_EDITABLE;
		if (originalWorkbook == null || originalWorkbook.getStudyDetails() == null || originalWorkbook.getStudyDetails().getId() == null) {
			isPreviewEditable = "1";
		}
		return isPreviewEditable;
	}

	// TODO: MARK FOR DELETE IBP-2789
	@Deprecated
	@ResponseBody
	@RequestMapping(value = "/load/dynamic/change/measurement", method = RequestMethod.POST)
	public Map<String, Object> loadDynamicChangeMeasurement(
		@ModelAttribute("createTrialForm") final CreateTrialForm form,
		final Model model, final HttpServletRequest request) {
		List<MeasurementVariable> removedTraits = new ArrayList<>();
		Map<Integer, List<Integer>> usages = new HashMap<>();
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
			removedTraits =
				(List<MeasurementVariable>) CollectionUtils.subtract(measurementDatasetVariables, newMeasurementDatasetVariables);
			usages = WorkbookUtil.getVariatesUsedInFormulas(measurementDatasetVariables);
			measurementDatasetVariables = newMeasurementDatasetVariables;
		}

		this.setOutOfSyncVariables(removedTraits, usages);
		FieldbookUtil.setColumnOrderingOnWorkbook(workbook, form.getColumnOrders());
		measurementDatasetVariables = workbook.arrangeMeasurementVariables(measurementDatasetVariables);
		this.processPreLoadingMeasurementDataPage(true, form, workbook, measurementDatasetVariables, model,
			request.getParameter("deletedEnvironment"));
		final Map<String, Object> result = new HashMap<>();
		result.put("success", "1");
		return result;
	}

	private void setOutOfSyncVariables(
		final List<MeasurementVariable> removedTraits,
		final Map<Integer, List<Integer>> usages) {

		for (final MeasurementVariable variable : removedTraits) {
			final Integer termId = variable.getTermId();
			if (usages.containsKey(termId)) {
				for (final Integer targetTermId : usages.get(termId)) {
					final List<MeasurementRow> rows = this.userSelection.getMeasurementRowList();
					for (final MeasurementRow row : rows) {
						final MeasurementData value = row.getMeasurementData(targetTermId);
						if (value != null && value.getPhenotypeId() != null) {
							value.setValueStatus(Phenotype.ValueStatus.OUT_OF_SYNC);
							value.setChanged(true);
						}
					}

				}
			}
		}
	}

	private void processPreLoadingMeasurementDataPage(
		final boolean isTemporary, final CreateTrialForm form, final Workbook workbook,
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
			// TODO: MARK FOR DELETE IBP-2789
			//model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT, this.studyDataManager.countExperiments(measurementDatasetId));
		}

		form.setMeasurementVariables(measurementDatasetVariables);
		this.userSelection.setMeasurementDatasetVariable(measurementDatasetVariables);
		model.addAttribute("createTrialForm", form);
		model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, this.isPreviewEditable(workbook));
	}

	private Workbook processDeletedEnvironments(
		final String deletedEnvironment,
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

	List<MeasurementRow> getFilteredTrialObservations(
		final List<MeasurementRow> trialObservations,
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

	private List<MeasurementRow> updateTrialInstanceNoAfterDelete(
		final String deletedEnvironment,
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

	List<MeasurementRow> getFilteredObservations(final List<MeasurementRow> observations, final String deletedEnvironment) {

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

	List<SampleListDTO> getSampleList(final Integer studyId) {
		final List<Integer> datasetTypeIds = this.datasetTypeService.getObservationDatasetTypeIds();
		final List<Integer> datasetIds = new ArrayList<>();

		final List<DatasetDTO> datasetDTOs = this.datasetService.getDatasets(studyId, new HashSet<>(datasetTypeIds));
		for (final DatasetDTO dataset : datasetDTOs) {
			datasetIds.add(dataset.getDatasetId());
		}
		if (!datasetIds.isEmpty()) {
			return this.sampleListService.getSampleLists(datasetIds);
		}
		return null;
	}

}
