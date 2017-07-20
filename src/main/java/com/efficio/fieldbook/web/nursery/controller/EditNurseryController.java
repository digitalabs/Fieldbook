/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

/**
 * The Class CreateNurseryController.
 */
@Controller
@RequestMapping(EditNurseryController.URL)
@SessionAttributes("isCategoricalDescriptionView")
public class EditNurseryController extends SettingsController {

	/**
	 * The Constant URL.
	 */
	public static final String URL = "/NurseryManager/editNursery";
	/**
	 * The Constant URL_SETTINGS.
	 */
	public static final String URL_SETTINGS = "/NurseryManager/addOrRemoveTraits";
	public static final String STATUS = "status";
	public static final String HAS_MEASUREMENT_DATA_STR = "hasMeasurementData";
	public static final String ERROR = "-1";
	public static final String NO_MEASUREMENT = "0";
	public static final String SUCCESS = "1";
	public static final int NO_LIST_ID = -1;

	/**
	 * The Constant LOG.
	 */
	public static final Logger LOG = LoggerFactory.getLogger(EditNurseryController.class);
	/**
	 * The ontology service.
	 */
	@Resource
	private OntologyService ontologyService;

	/**
	 * The fieldbook service.
	 */
	@Resource
	private FieldbookService fieldbookService;

	@Resource
	private DataImportService dataImportService;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName ()
	 */

	@Resource
	private ErrorHandlerService errorHandlerService;

	@Resource
	private StudyDataManager studyDataManagerImpl;

	@Override
	public String getContentName() {
		return "NurseryManager/editNursery";
	}

	@ModelAttribute("isCategoricalDescriptionView")
	public Boolean initIsCategoricalDescriptionView() {
		return Boolean.FALSE;
	}

	/**
	 * Use existing nursery.
	 *
	 * @param form the form
	 * @param form2 the form2
	 * @param nurseryId the nursery id
	 * @param model the model
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@RequestMapping(value = "/{nurseryId}", method = RequestMethod.GET)
	public String useExistingNursery(@ModelAttribute("createNurseryForm") final CreateNurseryForm form,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form2, @PathVariable final int nurseryId,
			@RequestParam(required = false) final String isAjax, final Model model, final HttpServletRequest request,
			final RedirectAttributes redirectAttributes,
			@RequestParam(value = "crosseslistid", required = false) final String crossesListId) throws MiddlewareQueryException {

		model.addAttribute("createdCrossesListId", crossesListId);

		final String contextParams = this.retrieveContextInfo(request);

		this.clearSessionData(request.getSession());

		//store the id of the created germplasm list with crosses to update it later in the flow when all data is updated applying naming
		// rules
		request.getSession().setAttribute("createdCrossesListId", crossesListId);

		try {
			Workbook workbook = null;
			if (nurseryId != 0) {
				final DmsProject dmsProject = this.studyDataManagerImpl.getProject(nurseryId);
				if (dmsProject.getProgramUUID() == null) {
					return "redirect:" + ManageNurseriesController.URL + "?summaryId=" + nurseryId + "&summaryName=" + dmsProject.getName();
				}

				// settings part
				workbook = this.setUpForWorkbook(form, nurseryId);
				// nursery-level
				this.setUpNurserylevelConditions(workbook, form, form2, nurseryId);

				// measurements part
				SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), false,
						this.ontologyService, contextUtil.getCurrentProgramUUID());
				this.setMeasurementsData(form, workbook);

				// make factors non-editable if experiments exist already
				if (form.isMeasurementDataExisting()) {
					for (final SettingDetail setting : this.userSelection.getPlotsLevelList()) {
						setting.setDeletable(false);
					}
				}

				form.setPlotLevelVariables(this.userSelection.getPlotsLevelList());

				this.setUpModelAttibutes(model, nurseryId);
			}

			this.setFormStaticData(form, contextParams, workbook);

			this.addVariableSectionIdentifiers(model);

			model.addAttribute("createNurseryForm", form);

			return this.getReturnValue(model, isAjax);

		} catch (final MiddlewareQueryException e) {
			EditNurseryController.LOG.debug(e.getMessage(), e);
			redirectAttributes.addFlashAttribute("redirectErrorMessage", this.errorHandlerService.getErrorMessagesAsString(e.getCode(),
					new String[] {AppConstants.NURSERY.getString(), StringUtils.capitalize(AppConstants.NURSERY.getString()),
							AppConstants.NURSERY.getString()}, "\n"));
			return "redirect:" + ManageNurseriesController.URL;
		} catch (final MiddlewareException e) {
			EditNurseryController.LOG.debug(e.getMessage(), e);
			redirectAttributes.addFlashAttribute("redirectErrorMessage", e.getMessage());
			return "redirect:" + ManageNurseriesController.URL;
		}

	}

	private Workbook setUpForWorkbook(final CreateNurseryForm form, final int nurseryId) {
		final Workbook workbook;
		workbook = this.fieldbookMiddlewareService.getNurseryDataSet(nurseryId);

		// workbook.observations collection is no longer loaded by default.
		// Load it so that Nursery manager functionality that relies on it continues to work.
		this.fieldbookMiddlewareService.loadAllObservations(workbook);

		this.userSelection.setConstantsWithLabels(workbook.getConstants());

		form.setMeasurementDataExisting(this.fieldbookMiddlewareService
				.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));

		this.convertToXmlDatasetPojo(workbook);
		return workbook;
	}

	private String getReturnValue(final Model model, final String isAjax) {
		if (isAjax != null && EditNurseryController.SUCCESS.equalsIgnoreCase(isAjax)) {
			return super.showAjaxPage(model, this.getContentName());
		}

		return super.show(model);
	}

	private void setUpModelAttibutes(final Model model, final int nurseryId) {
		final List<GermplasmList> germplasmList =
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(nurseryId), GermplasmListType.ADVANCED);
		final List<GermplasmList> germplasmCrossesList =
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(nurseryId), GermplasmListType.CROSSES);

		germplasmCrossesList
			.addAll(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(
				Integer.valueOf(nurseryId),
				GermplasmListType.CRT_CROSS));
		germplasmCrossesList
			.addAll(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(
				Integer.valueOf(nurseryId),
				GermplasmListType.IMP_CROSS));

		model.addAttribute("advancedList", germplasmList);
		model.addAttribute("crossesList", this.fieldbookMiddlewareService.appendTabLabelToList(germplasmCrossesList));
	}

	private void setUpNurserylevelConditions(final Workbook workbook, final CreateNurseryForm form, final ImportGermplasmListForm form2,
			final int nurseryId) {
		final List<SettingDetail> nurseryLevelConditions =
				this.updateRequiredFields(this.buildVariableIDList(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()),
						this.buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true),
						this.buildRequiredVariablesFlag(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()),
						this.userSelection.getStudyLevelConditions(), false, AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString(),
						VariableType.NURSERY_CONDITION.getRole().name());

		final List<SettingDetail> basicDetails =
				this.getSettingDetailsOfSection(nurseryLevelConditions, form, AppConstants.FIXED_NURSERY_VARIABLES.getString());

		this.setCheckVariables(this.userSelection.getRemovedConditions(), form2, form);
		
		final String variableIds =AppConstants.FIXED_NURSERY_VARIABLES.getString() + AppConstants.CHECK_VARIABLES.getString() + AppConstants.BREEDING_METHOD_ID_CODE_NAME_COMBINATION.getString();
		SettingsUtil.removeBasicDetailsVariables(nurseryLevelConditions, variableIds);

		this.userSelection.setBasicDetails(basicDetails);

		this.setUpFormAttributes(form, workbook, nurseryId);
	}

	private void setUpFormAttributes(final CreateNurseryForm form, final Workbook workbook, final int nurseryId) {
		form.setStudyId(nurseryId);
		form.setBasicDetails(this.userSelection.getBasicDetails());
		form.setStudyLevelVariables(this.userSelection.getStudyLevelConditions());
		form.setBaselineTraitVariables(this.userSelection.getBaselineTraitsList());
		form.setSelectionVariatesVariables(this.userSelection.getSelectionVariates());
		form.setGermplasmListId(this.getGermplasmListId(nurseryId));

		form.setNurseryConditions(this.userSelection.getNurseryConditions());
		form.setLoadSettings(EditNurseryController.SUCCESS);
		form.setFolderId(Integer.valueOf((int) workbook.getStudyDetails().getParentFolderId()));

		form.setFolderName(this.getNurseryFolderName(form.getFolderId()));
	}

	protected String getNurseryFolderName(final int folderId) throws MiddlewareQueryException {
		if (folderId == 1) {
			return AppConstants.NURSERIES.getString();
		}
		return this.fieldbookMiddlewareService.getFolderNameById(folderId);
	}

	protected void convertToXmlDatasetPojo(final Workbook workbook) throws MiddlewareQueryException {
		final Dataset dataset = (Dataset) SettingsUtil.convertWorkbookToXmlDataset(workbook);

		SettingsUtil.convertXmlDatasetToPojo(this.fieldbookMiddlewareService, this.fieldbookService, dataset, this.userSelection,
				this.getCurrentProject().getUniqueID(), false, false);
	}

	protected void clearSessionData(final HttpSession session) {
		SessionUtility.clearSessionData(session,
				new String[] {SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME,
						SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
	}

	protected void setCheckVariables(final List<SettingDetail> removedConditions, final ImportGermplasmListForm form2,
			final CreateNurseryForm form) {
		// set check variables
		final List<SettingDetail> checkVariables = this.getCheckVariables(removedConditions, form);
		form2.setCheckVariables(checkVariables);
	}

	protected String retrieveContextInfo(final HttpServletRequest request) {
		final ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);
		return ContextUtil.getContextParameterString(contextInfo);
	}

	/**
	 * Sets the measurements data.
	 *
	 * @param form the form
	 * @param workbook the workbook
	 */
	private void setMeasurementsData(final CreateNurseryForm form, final Workbook workbook) {
		this.userSelection.setMeasurementRowList(workbook.getObservations());
		form.setMeasurementRowList(this.userSelection.getMeasurementRowList());
		form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
		form.setStudyName(workbook.getStudyDetails().getStudyName());
		form.changePage(1);
		this.userSelection.setCurrentPage(form.getCurrentPage());
		this.userSelection.setWorkbook(workbook);
		this.userSelection.setTemporaryWorkbook(null);
	}

	/**
	 * Show.
	 *
	 * @param form the form
	 * @param form2 the form2
	 * @param model the model
	 * @param session the session
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("createNurseryForm") final CreateNurseryForm form,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form2, final Model model, final HttpServletRequest req,
			final HttpSession session, final HttpServletRequest request) throws MiddlewareException {

		final String contextParams = this.retrieveContextInfo(request);
		this.clearSessionData(session);
		this.setFormStaticData(form, contextParams, new Workbook());
		this.assignDefaultValues(form);
		return super.show(model);
	}

	/**
	 * Assign default values.
	 *
	 * @param form the form
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	void assignDefaultValues(final CreateNurseryForm form) throws MiddlewareException {
		List<SettingDetail> basicDetails = new ArrayList<>();
		List<SettingDetail> nurseryDefaults = new ArrayList<>();
		List<SettingDetail> plotDefaults = new ArrayList<>();
		final List<SettingDetail> baselineTraitsList = new ArrayList<>();
		final List<SettingDetail> nurseryConditions = new ArrayList<>();

		basicDetails = this.buildDefaultVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString(),
				this.buildRequiredVariablesLabel(AppConstants.FIXED_NURSERY_VARIABLES.getString(), false),
				VariableType.STUDY_DETAIL.getRole().name());
		form.setBasicDetails(basicDetails);
		form.setStudyLevelVariables(nurseryDefaults);
		form.setPlotLevelVariables(plotDefaults);

		plotDefaults =
				this.buildDefaultVariables(plotDefaults, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(),
						this.buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false),
						VariableType.GERMPLASM_DESCRIPTOR.getRole().name());
		
		this.userSelection.setBasicDetails(basicDetails);
		this.userSelection.setStudyLevelConditions(nurseryDefaults);
		this.userSelection.setPlotsLevelList(plotDefaults);
		this.userSelection.setBaselineTraitsList(baselineTraitsList);
		this.userSelection.setNurseryConditions(nurseryConditions);
	}

	/**
	 * Submit.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, String> submit(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model)
			throws MiddlewareQueryException {
		// get the name of the nursery

		String name = null;
		for (final SettingDetail nvar : form.getBasicDetails()) {
			if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId()
					.equals(TermId.STUDY_NAME.getId())) {
				name = nvar.getValue();
				break;
			}
		}
		// combine all study conditions (basic details and management details
		// and hidden variables)
		final List<SettingDetail> studyLevelVariables = this.combineStudyConditions(form);

		// combine all variates (traits and selection variates)
		final List<SettingDetail> baselineTraits = this.combineVariates(form);

		// include deleted list if measurements are available
		this.includeDeletedList(form, studyLevelVariables, baselineTraits);

		final int trialDatasetId = this.userSelection.getWorkbook().getTrialDatasetId();
		// retain measurement dataset id
		final int measurementDatasetId = this.userSelection.getWorkbook().getMeasurementDatesetId();

		SettingsUtil.setSettingDetailRoleAndVariableType(VariableType.STUDY_DETAIL.getId(), studyLevelVariables,
				this.fieldbookMiddlewareService, this.contextUtil.getCurrentProgramUUID());
		SettingsUtil.setSettingDetailRoleAndVariableType(VariableType.GERMPLASM_DESCRIPTOR.getId(), form.getPlotLevelVariables(),
				this.fieldbookMiddlewareService, this.contextUtil.getCurrentProgramUUID());
		SettingsUtil.setSettingDetailRoleAndVariableType(VariableType.TRAIT.getId(), form.getNurseryConditions(),
				this.fieldbookMiddlewareService, this.contextUtil.getCurrentProgramUUID());

		final Dataset dataset = (Dataset) SettingsUtil
				.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, studyLevelVariables, form.getPlotLevelVariables(),
						baselineTraits, this.userSelection, form.getNurseryConditions(), this.contextUtil.getCurrentProgramUUID());

		SettingsUtil.setConstantLabels(dataset, this.userSelection.getConstantsWithLabels());

		final Workbook workbook = this.prepareNewWorkbookForSaving(trialDatasetId, measurementDatasetId, dataset);

		this.createStudyDetails(workbook, form.getBasicDetails(), form.getFolderId(), form.getStudyId());
		this.userSelection.setWorkbook(workbook);

		final Map<String, String> resultMap = new HashMap<>();
		// saving of measurement rows
		if (this.userSelection.getMeasurementRowList() != null && !this.userSelection.getMeasurementRowList().isEmpty()) {
			this.saveMeasurementRows(form, trialDatasetId, workbook, resultMap);
			return resultMap;
		} else {
			resultMap.put(EditNurseryController.STATUS, EditNurseryController.SUCCESS);
			return resultMap;
		}

	}

	public void saveMeasurementRows(final CreateNurseryForm form, final int trialDatasetId, final Workbook workbook,
			final Map<String, String> resultMap) {
		try {
			WorkbookUtil
					.addMeasurementDataToRows(workbook.getFactors(), false, this.userSelection, this.ontologyService, this.fieldbookService,
							this.contextUtil.getCurrentProgramUUID());
			WorkbookUtil
					.addMeasurementDataToRows(workbook.getVariates(), true, this.userSelection, this.ontologyService, this.fieldbookService,
							this.contextUtil.getCurrentProgramUUID());

			workbook.setMeasurementDatasetVariables(null);
			form.setMeasurementRowList(this.userSelection.getMeasurementRowList());
			form.setMeasurementVariables(this.userSelection.getWorkbook().getMeasurementDatasetVariables());
			workbook.setObservations(form.getMeasurementRowList());

			this.userSelection.setWorkbook(workbook);

			this.fieldbookService.createIdCodeNameVariablePairs(this.userSelection.getWorkbook(),
					AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
			this.fieldbookService.createIdNameVariablePairs(this.userSelection.getWorkbook(), this.userSelection.getRemovedConditions(),
					AppConstants.ID_NAME_COMBINATION.getString(), true);
			this.fieldbookMiddlewareService.saveMeasurementRows(workbook, this.contextUtil.getCurrentProgramUUID(), true);
			workbook.setTrialObservations(this.fieldbookMiddlewareService
					.buildTrialObservations(trialDatasetId, workbook.getTrialConditions(), workbook.getTrialConstants()));
			workbook.setOriginalObservations(workbook.getObservations());

			this.fieldbookService.saveStudyImportedCrosses(this.userSelection.getImportedCrossesId(), form.getStudyId());
			resultMap.put(EditNurseryController.STATUS, EditNurseryController.SUCCESS);
			resultMap.put(EditNurseryController.HAS_MEASUREMENT_DATA_STR, String.valueOf(this.fieldbookMiddlewareService
					.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(),
							SettingsUtil.buildVariates(workbook.getVariates()))));

			this.fieldbookService.saveStudyColumnOrdering(form.getStudyId(), workbook.getStudyName(), form.getColumnOrders(), workbook);
		} catch (final MiddlewareException e) {
			resultMap.put(EditNurseryController.STATUS, EditNurseryController.ERROR);
			resultMap.put("errorMessage", e.getMessage());

			EditNurseryController.LOG.error(e.getMessage(), e);
		}
	}

	Workbook prepareNewWorkbookForSaving(final int trialDatasetId, final int measurementDatasetId, final Dataset dataset) {

		String programUUID = this.contextUtil.getCurrentProgramUUID();

		final Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true, programUUID);

		workbook.setOriginalObservations(this.userSelection.getWorkbook().getOriginalObservations());
		workbook.setTrialObservations(this.userSelection.getWorkbook().getTrialObservations());

		workbook.setTrialDatasetId(trialDatasetId);
		workbook.setMeasurementDatesetId(measurementDatasetId);

		// A nursery only has one trial observation. so we get the first measurement row from workbook.getTrialObservations()
		MeasurementRow trialObservation = !workbook.getTrialObservations().isEmpty() ? workbook.getTrialObservations().get(0) : null;

		this.populateMeasurementDataUsingValuesFromVariables(workbook.getTrialConditions(), trialObservation);
		this.populateMeasurementDataUsingValuesFromVariables(workbook.getTrialConstants(), trialObservation);

		this.dataImportService.populatePossibleValuesForCategoricalVariates(workbook.getConditions(), programUUID);

		return workbook;
	}

	private void includeDeletedList(final CreateNurseryForm form, final List<SettingDetail> studyLevelVariables,
			final List<SettingDetail> baselineTraits) {
		SettingsUtil.addDeletedSettingsList(studyLevelVariables, this.userSelection.getDeletedStudyLevelConditions(),
				this.userSelection.getStudyLevelConditions());
		SettingsUtil.addDeletedSettingsList(form.getPlotLevelVariables(), this.userSelection.getDeletedPlotLevelList(),
				this.userSelection.getPlotsLevelList());
		SettingsUtil.addDeletedSettingsList(baselineTraits, this.userSelection.getDeletedBaselineTraitsList(),
				this.userSelection.getBaselineTraitsList());
		SettingsUtil.addDeletedSettingsList(form.getNurseryConditions(), this.userSelection.getDeletedNurseryConditions(),
				this.userSelection.getNurseryConditions());
	}

	protected List<SettingDetail> combineVariates(final CreateNurseryForm form) {

		this.setVariableTypeForTrait(form);
		this.setVariableTypeForSelectionMethod(form);

		List<SettingDetail> baselineTraits = form.getBaselineTraitVariables();
		final List<SettingDetail> baselineTraitsSession = this.userSelection.getSelectionVariates();
		if (baselineTraits == null) {
			baselineTraits = form.getSelectionVariatesVariables();
			this.userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
		} else if (form.getSelectionVariatesVariables() != null) {
			baselineTraits.addAll(form.getSelectionVariatesVariables());
			this.userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
		}

		if (form.getPlotLevelVariables() == null) {
			form.setPlotLevelVariables(new ArrayList<SettingDetail>());
		}
		if (baselineTraits == null) {
			baselineTraits = new ArrayList<>();
		}
		if (form.getNurseryConditions() == null) {
			form.setNurseryConditions(new ArrayList<SettingDetail>());
		}
		return baselineTraits;
	}

	private void setVariableTypeForTrait(final CreateNurseryForm form) {
		if (form.getBaselineTraitVariables() != null) {
			//NOTE: Setting variable type as TRAIT for Trait Variable List
			for (SettingDetail selectionDetail : form.getBaselineTraitVariables()) {
				selectionDetail.setVariableType(VariableType.TRAIT);
				selectionDetail.setRole(VariableType.TRAIT.getRole());
			}
		}
	}

	private void setVariableTypeForSelectionMethod(final CreateNurseryForm form) {
		if (form.getSelectionVariatesVariables() != null) {
			//NOTE: Setting variable type as SELECTION_METHOD for Trait Variable List
			for (SettingDetail selectionDetail : form.getSelectionVariatesVariables()) {
				selectionDetail.setVariableType(VariableType.SELECTION_METHOD);
				selectionDetail.setRole(VariableType.SELECTION_METHOD.getRole());
			}
		}
	}

	private List<SettingDetail> combineStudyConditions(final CreateNurseryForm form) {
		final List<SettingDetail> studyLevelVariables = new ArrayList<>();
		if (form.getStudyLevelVariables() != null && !form.getStudyLevelVariables().isEmpty()) {
			studyLevelVariables.addAll(form.getStudyLevelVariables());
		}
		studyLevelVariables.addAll(form.getBasicDetails());

		final List<SettingDetail> studyLevelVariablesSession = this.userSelection.getBasicDetails();
		this.userSelection.getStudyLevelConditions().addAll(studyLevelVariablesSession);
		if (this.userSelection.getRemovedConditions() != null && !this.userSelection.isDesignGenerated()) {
			studyLevelVariables.addAll(this.userSelection.getRemovedConditions());
			this.userSelection.getStudyLevelConditions().addAll(this.userSelection.getRemovedConditions());
		}

		this.addStudyLevelVariablesFromUserSelectionIfNecessary(studyLevelVariables, this.userSelection);

		this.addNurseryTypeFromDesignImport(studyLevelVariables);
		this.addExperimentalDesignTypeFromDesignImport(studyLevelVariables);

		// add hidden variables like OCC in factors list
		if (this.userSelection.getRemovedFactors() != null) {
			form.getPlotLevelVariables().addAll(this.userSelection.getRemovedFactors());
			this.userSelection.getPlotsLevelList().addAll(this.userSelection.getRemovedFactors());
		}
		return studyLevelVariables;
	}

	void addNurseryTypeFromDesignImport(final List<SettingDetail> studyLevelVariables) {

		final SettingDetail nurseryTypeSettingDetail = new SettingDetail();
		final SettingVariable nurseryTypeSettingVariable = new SettingVariable();

		final Integer nurseryTypeValue = this.userSelection.getNurseryTypeForDesign();

		this.setUpForDesignImport(nurseryTypeSettingDetail, nurseryTypeSettingVariable, String.valueOf(nurseryTypeValue),
				TermId.NURSERY_TYPE.getId(), "NURSERY_TYPE");

		if (nurseryTypeValue != null) {

			for (final SettingDetail settingDetail : studyLevelVariables) {
				if (settingDetail.getVariable().getCvTermId() == TermId.NURSERY_TYPE.getId()) {
					settingDetail.setValue(String.valueOf(nurseryTypeValue));
					settingDetail.getVariable().setName("NURSERY_TYPE");
					this.userSelection.setNurseryTypeForDesign(null);
					return;
				}
			}

			studyLevelVariables.add(nurseryTypeSettingDetail);
		}

		this.userSelection.setNurseryTypeForDesign(null);

	}

	void addExperimentalDesignTypeFromDesignImport(final List<SettingDetail> studyLevelVariables) {

		final SettingDetail nurseryTypeSettingDetail = new SettingDetail();
		final SettingVariable nurseryTypeSettingVariable = new SettingVariable();

		this.setUpForDesignImport(nurseryTypeSettingDetail, nurseryTypeSettingVariable, String.valueOf(TermId.OTHER_DESIGN.getId()),
				TermId.EXPERIMENT_DESIGN_FACTOR.getId(), "EXPERIMENT_DESIGN");

		if (this.userSelection.getExpDesignVariables() != null && !this.userSelection.getExpDesignVariables().isEmpty()) {

			for (final SettingDetail settingDetail : studyLevelVariables) {
				if (settingDetail.getVariable().getCvTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId()) {
					settingDetail.setValue(String.valueOf(TermId.OTHER_DESIGN.getId()));
					settingDetail.getVariable().setName("EXPERIMENT_DESIGN");
					return;
				}
			}

			studyLevelVariables.add(nurseryTypeSettingDetail);
		}

	}

	void populateMeasurementDataUsingValuesFromVariables(List<MeasurementVariable> variables, MeasurementRow measurementRow) {

		if (measurementRow != null && variables != null && !variables.isEmpty()) {

			for (final MeasurementVariable measurementVariable : variables) {
				MeasurementData measurementData = measurementRow.getMeasurementData(measurementVariable.getTermId());
				if (measurementData != null) {
					measurementData.setValue(measurementVariable.getValue());
					if (measurementData.isCategorical() && StringUtils.isNumeric(measurementVariable.getValue())) {
						measurementData.setcValueId(measurementVariable.getValue());
					}
				}

			}

		}

	}

	/**
	 * Sets the form static data.
	 *
	 * @param form the new form static data
	 */
	protected void setFormStaticData(final CreateNurseryForm form, final String contextParams, final Workbook workbook) {

		final ExperimentalDesignVariable expDesignVar = workbook.getExperimentalDesignVariables();
		if (expDesignVar != null && expDesignVar.getExperimentalDesign() != null) {
			form.setExperimentTypeId(expDesignVar.getExperimentalDesign().getValue());
		}

		form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
		form.setLocationId(AppConstants.LOCATION_ID.getString());
		form.setBreedingMethodUrl(this.fieldbookProperties.getProgramBreedingMethodsUrl());
		form.setLocationUrl(this.fieldbookProperties.getProgramLocationsUrl());
		form.setProjectId(this.getCurrentProjectId());
		form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
		form.setStartDateId(AppConstants.START_DATE_ID.getString());
		form.setEndDateId(AppConstants.END_DATE_ID.getString());
		form.setOpenGermplasmUrl(this.fieldbookProperties.getGermplasmDetailsUrl());
		form.setBaselineTraitsSegment(VariableType.TRAIT.getId().toString());
		form.setSelectionVariatesSegment(VariableType.SELECTION_METHOD.getId().toString());
		form.setCharLimit(Integer.parseInt(AppConstants.CHAR_LIMIT.getString()));
		form.setRequiredFields(
				AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString() + "," + AppConstants.FIXED_NURSERY_VARIABLES.getString());
		form.setProjectId(this.getCurrentProjectId());
		form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
		form.setBreedingMethodCode(AppConstants.BREEDING_METHOD_CODE.getString());
		Integer datasetId = workbook.getMeasurementDatesetId();
		try {
			if (datasetId == null) {
				datasetId = this.fieldbookMiddlewareService
						.getMeasurementDatasetId(workbook.getStudyDetails().getId(), workbook.getStudyName());
			}
			form.setHasFieldmap(this.fieldbookMiddlewareService.hasFieldMap(datasetId));
		} catch (final MiddlewareException e) {
			EditNurseryController.LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Check measurement data.
	 *
	 * @param form the form
	 * @param model the model
	 * @param mode the mode
	 * @return the map
	 */
	@ResponseBody
	@RequestMapping(value = "/checkMeasurementData/{mode}/{variableIds}", method = RequestMethod.GET)
	public Map<String, String> checkMeasurementData(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model,
			@PathVariable final int mode, @PathVariable final String variableIds) {
		final Map<String, String> resultMap = new HashMap<>();

		// if there are measurement rows, check if values are already entered
		if (this.userSelection.getMeasurementRowList() != null && !this.userSelection.getMeasurementRowList().isEmpty() && this
				.hasMeasurementDataEnteredForVariables(SettingsUtil.parseVariableIds(variableIds), this.userSelection)) {
			resultMap.put(EditNurseryController.HAS_MEASUREMENT_DATA_STR, EditNurseryController.SUCCESS);
		} else {
			resultMap.put(EditNurseryController.HAS_MEASUREMENT_DATA_STR, EditNurseryController.NO_MEASUREMENT);
		}

		return resultMap;
	}

	/**
	 * Reset session variables after save.
	 *
	 * @param form the form
	 * @param model the model
	 * @param session the session
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@RequestMapping(value = "/recreate/session/variables", method = RequestMethod.GET)
	public String resetSessionVariablesAfterSave(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model,
			final HttpSession session, final HttpServletRequest request) throws MiddlewareException {

		final String contextParams = this.retrieveContextInfo(request);

		final Workbook workbook = this.userSelection.getWorkbook();
		form.setMeasurementDataExisting(this.fieldbookMiddlewareService
				.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));
		this.fieldbookMiddlewareService.setOrderVariableByRank(workbook);
		this.resetSessionVariablesAfterSave(workbook, true);

		// set measurement session variables to form
		this.setMeasurementsData(form, workbook);
		this.setFormStaticData(form, contextParams, workbook);
		model.addAttribute("createNurseryForm", form);

		return super.showAjaxPage(model, EditNurseryController.URL_SETTINGS);
	}

	/**
	 * Show variable details.
	 *
	 * @param id the id
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/showVariableDetails/{id}", method = RequestMethod.GET)
	public String showVariableDetails(@PathVariable final int id) {
		try {
			final SettingVariable svar = this.getSettingVariable(id);
			if (svar != null) {
				final ObjectMapper om = new ObjectMapper();
				return om.writeValueAsString(svar);
			}

		} catch (final Exception e) {
			EditNurseryController.LOG.error(e.getMessage(), e);
		}
		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/deleteMeasurementRows", method = RequestMethod.POST)
	public Map<String, String> deleteMeasurementRows() {
		final Map<String, String> resultMap = new HashMap<>();

		try {
			this.fieldbookMiddlewareService.deleteObservationsOfStudy(this.userSelection.getWorkbook().getMeasurementDatesetId());
			resultMap.put(EditNurseryController.STATUS, EditNurseryController.SUCCESS);
		} catch (final MiddlewareQueryException e) {
			EditNurseryController.LOG.error(e.getMessage(), e);
			resultMap.put(EditNurseryController.STATUS, EditNurseryController.ERROR);
			resultMap.put("errorMessage", e.getMessage());
		}

		this.userSelection.setMeasurementRowList(null);
		this.userSelection.getWorkbook().setOriginalObservations(null);
		this.userSelection.getWorkbook().setObservations(null);
		return resultMap;
	}

	@ModelAttribute("nameTypes")
	public List<UserDefinedField> getNameTypes() {
		try {
			return this.fieldbookMiddlewareService.getGermplasmNameTypes();
		} catch (final MiddlewareQueryException e) {
			EditNurseryController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
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

	@ModelAttribute("programLocationURL")
	public String getProgramLocation() {
		return this.fieldbookProperties.getProgramLocationsUrl();
	}

	@ModelAttribute("programMethodURL")
	public String getProgramMethod() {
		return this.fieldbookProperties.getProgramBreedingMethodsUrl();
	}

	@ModelAttribute("projectID")
	public String getProgramID() {
		return this.getCurrentProjectId();
	}

	public Integer getGermplasmListId(final int studyId) {
		if (this.userSelection.getImportedAdvancedGermplasmList() == null) {
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();

			final List<GermplasmList> germplasmLists =
					this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.NURSERY);

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
				NO_LIST_ID;
	}

	private void addStudyLevelVariablesFromUserSelectionIfNecessary(final List<SettingDetail> studyLevelVariables,
			final UserSelection userSelection) {

		for (final SettingDetail settingDetailFromUserSelection : userSelection.getStudyLevelConditions()) {

			boolean settingDetailExists = false;

			for (final SettingDetail settingDetail : studyLevelVariables) {
				if (settingDetail.getVariable().getCvTermId().intValue() == settingDetailFromUserSelection.getVariable().getCvTermId()
						.intValue()) {
					settingDetail.setRole(settingDetailFromUserSelection.getRole());
					settingDetail.setVariableType(settingDetailFromUserSelection.getVariableType());
					settingDetailExists = true;
					break;
				}
			}

			if (!settingDetailExists) {
				studyLevelVariables.add(settingDetailFromUserSelection);
			}

		}

	}

	private void setUpForDesignImport(final SettingDetail nurseryTypeSettingDetail, final SettingVariable nurseryTypeSettingVariable,
			final String value, final Integer cvTermId, final String name) {
		nurseryTypeSettingDetail.setValue(value);
		nurseryTypeSettingVariable.setCvTermId(cvTermId);
		nurseryTypeSettingVariable.setName(name);
		nurseryTypeSettingVariable.setOperation(Operation.ADD);
		nurseryTypeSettingDetail.setVariable(nurseryTypeSettingVariable);
	}

	/**
	 * Checks if the measurement data is existing
	 *
	 * @return the map
	 */
	@ResponseBody
	@RequestMapping(value = "/isMeasurementDataExisting", method = RequestMethod.GET)
	public Map<String, Object> isMeasurementDataExisting() {
		final Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(EditNurseryController.HAS_MEASUREMENT_DATA_STR, this.fieldbookMiddlewareService
				.checkIfStudyHasMeasurementData(userSelection.getWorkbook().getMeasurementDatesetId(),
						SettingsUtil.buildVariates(userSelection.getWorkbook().getVariates())));
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/columns", method = RequestMethod.GET)
	public List<MeasurementVariable> getColumns() {
		return this.userSelection.getWorkbook().getMeasurementDatasetVariables();
	}
	
	public void setFieldbookService(FieldbookService fieldbookService){
		this.fieldbookService = fieldbookService;
		super.setFieldbookService(fieldbookService);
	}

}
