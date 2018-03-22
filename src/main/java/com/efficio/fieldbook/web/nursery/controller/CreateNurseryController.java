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

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.util.JsonIoException;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class CreateNurseryController.
 */
@Controller
@RequestMapping(CreateNurseryController.URL)
public class CreateNurseryController extends SettingsController {

	/** The Constant URL. */
	public static final String URL = "/NurseryManager/createNursery";
	/** The Constant URL_SETTINGS. */
	public static final String URL_SETTINGS = "/NurseryManager/chooseSettings";
	/** The Constant URL_CHECKS. */
	private static final String URL_CHECKS = "/NurseryManager/includes/importGermplasmListCheckSection";
	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(CreateNurseryController.class);

	public static final String CREATE_NURSERY_FORM = "createNurseryForm";

	@Resource
	private OntologyService ontologyService;

	@Resource
	private ErrorHandlerService errorHandlerService;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return URL;
	}

	/**
	 * Use existing nursery.
	 *
	 * @param form the form
	 * @param nurseryId the nursery id
	 * @param model the model
	 * @param session the session
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@RequestMapping(value = "/nursery/{nurseryId}", method = RequestMethod.GET)
	public String useExistingNursery(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, @PathVariable final int nurseryId,
			final Model model, final HttpSession session, final HttpServletRequest request) {

		final ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);
		final String contextParams = ContextUtil.getContextParameterString(contextInfo);

		try {
			if (nurseryId != 0) {
				final Workbook workbook = this.fieldbookMiddlewareService.getStudyVariableSettings(nurseryId);
				this.userSelection.setConstantsWithLabels(workbook.getConstants());
				this.fieldbookService.createIdNameVariablePairs(workbook, new ArrayList<SettingDetail>(),
						AppConstants.ID_NAME_COMBINATION.getString(), false);

				final Dataset dataset = (Dataset) SettingsUtil.convertWorkbookToXmlDataset(workbook);
				SettingsUtil.convertXmlDatasetToPojo(this.fieldbookMiddlewareService, this.fieldbookService, dataset, this.userSelection,
						this.getCurrentProject().getUniqueID(), true, false);

				// nursery-level
				final List<SettingDetail> nurseryLevelConditions =
						this.updateRequiredFields(this.buildVariableIDList(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()),
								this.buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true),
								this.buildRequiredVariablesFlag(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()),
								this.userSelection.getStudyLevelConditions(), false,
								AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString(), VariableType.NURSERY_CONDITION.getRole().name());

				final String variableIds = AppConstants.FIXED_NURSERY_VARIABLES.getString()
						+ AppConstants.BREEDING_METHOD_ID_CODE_NAME_COMBINATION.getString();
				SettingsUtil.removeBasicDetailsVariables(nurseryLevelConditions, variableIds);

				// plot-level
				final List<SettingDetail> plotLevelConditions =
						this.updateRequiredFields(this.buildVariableIDList(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()),
								this.buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false),
								this.buildRequiredVariablesFlag(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()),
								this.userSelection.getPlotsLevelList(), false, "", VariableType.GERMPLASM_DESCRIPTOR.getRole().name());

				// remove variables not needed
				this.removeVariablesFromExistingNursery(plotLevelConditions, AppConstants.REMOVE_FACTORS_IN_USE_PREVIOUS_STUDY.getString());

				this.userSelection.setStudyLevelConditions(nurseryLevelConditions);
				this.userSelection.setPlotsLevelList(plotLevelConditions);

				form.setStudyLevelVariables(this.userSelection.getStudyLevelConditions());
				form.setBaselineTraitVariables(this.userSelection.getBaselineTraitsList());
				form.setSelectionVariatesVariables(this.userSelection.getSelectionVariates());
				form.setPlotLevelVariables(this.userSelection.getPlotsLevelList());
				form.setNurseryConditions(this.userSelection.getNurseryConditions());
				form.setLoadSettings("1");
				form.setProjectId(this.getCurrentProjectId());

				form.setMeasurementRowList(new ArrayList<MeasurementRow>());
				this.setFormStaticData(form, contextParams);
			}
		} catch (final MiddlewareQueryException e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
			this.addErrorMessageToResult(form, e);
		}

		this.addVariableSectionIdentifiers(model);
		model.addAttribute(
			CREATE_NURSERY_FORM, form);

		return super.showAjaxPage(model, CreateNurseryController.URL_SETTINGS);
	}

	@RequestMapping(value = "/nursery/getChecks/{nurseryId}", method = RequestMethod.GET)
	public String getChecksForUseExistingNursery(@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form,
			@PathVariable final int nurseryId, final Model model, final HttpSession session, final HttpServletRequest request) {
		this.setCheckVariablesInForm(form);
		model.addAttribute("importGermplasmListForm", form);

		return super.showAjaxPage(model, CreateNurseryController.URL_CHECKS);
	}

	protected void addErrorMessageToResult(final CreateNurseryForm form, final MiddlewareQueryException e) {
		final String param = AppConstants.NURSERY.getString();
		form.setHasError("1");
		form.setErrorMessage(this.errorHandlerService.getErrorMessagesAsString(e.getCode(),
				new Object[] {param, param.substring(0, 1).toUpperCase().concat(param.substring(1, param.length())), param}, "\n"));
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
	public String show(@ModelAttribute(CREATE_NURSERY_FORM) final CreateNurseryForm form,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form2, final Model model, final HttpSession session,
			final HttpServletRequest request) {

		final ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);
		final String contextParams = ContextUtil.getContextParameterString(contextInfo);
		SessionUtility.clearSessionData(session, new String[] {SessionUtility.USER_SELECTION_SESSION_NAME,
				SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
		form.setProjectId(this.getCurrentProjectId());
		this.setFormStaticData(form, contextParams);
		this.assignDefaultValues(form);
		form.setMeasurementRowList(new ArrayList<MeasurementRow>());

		this.addVariableSectionIdentifiers(model);

		// create check variables for specify checks
		this.setCheckVariablesInForm(form2);

		return super.show(model);
	}

	protected void setCheckVariablesInForm(final ImportGermplasmListForm form2) {
		List<SettingDetail> checkVariables = new ArrayList<>();
		checkVariables = this.buildDefaultVariables(checkVariables, AppConstants.CHECK_VARIABLES.getString(),
				this.buildRequiredVariablesLabel(AppConstants.CHECK_VARIABLES.getString(), false),
				VariableType.GERMPLASM_DESCRIPTOR.getRole().name());
		form2.setCheckVariables(checkVariables);
		this.userSelection.setRemovedConditions(checkVariables);
	}

	/**
	 * Assign default values.
	 *
	 * @param form the form
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	protected void assignDefaultValues(final CreateNurseryForm form) {
		List<SettingDetail> basicDetails = new ArrayList<>();
		final List<SettingDetail> nurseryDefaults = new ArrayList<>();
		List<SettingDetail> plotDefaults = new ArrayList<>();
		final List<SettingDetail> baselineTraitsList = new ArrayList<>();
		final List<SettingDetail> nurseryConditions = new ArrayList<>();

		basicDetails = this.buildDefaultVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString(),
				this.buildRequiredVariablesLabel(AppConstants.FIXED_NURSERY_VARIABLES.getString(), false),
				VariableType.STUDY_DETAIL.getRole().name());
		form.setBasicDetails(basicDetails);
		form.setStudyLevelVariables(nurseryDefaults);
		form.setPlotLevelVariables(plotDefaults);

		plotDefaults = this.buildDefaultVariables(plotDefaults, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(),
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
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public String submit(@ModelAttribute(CREATE_NURSERY_FORM) final CreateNurseryForm form, final Model model) {

		final String name = form.getStudyName().trim();

		final String description = form.getDescription();
		final String startDate = form.getStartDate();
		final String endDate = form.getEndDate();
		final String studyUpdate = form.getStudyUpdate();
		final String objective = form.getObjective();
		String createdBy = form.getCreatedBy();
		if (createdBy == null) {
			createdBy = this.contextUtil.getCurrentIbdbUserId().toString();
		}
		final List<SettingDetail> studyLevelVariables = SettingsUtil.combineStudyLevelVariablesInNurseryForm(form);

		this.addStudyLevelVariablesFromUserSelectionIfNecessary(studyLevelVariables, this.userSelection);

		this.addNurseryTypeFromDesignImport(studyLevelVariables);
		this.addExperimentalDesignTypeFromDesignImport(studyLevelVariables);

		final List<SettingDetail> studyLevelVariablesSession = this.userSelection.getBasicDetails();
		this.userSelection.getStudyLevelConditions().addAll(studyLevelVariablesSession);

		List<SettingDetail> baselineTraits = form.getBaselineTraitVariables();
		final List<SettingDetail> baselineTraitsSession = this.userSelection.getSelectionVariates();

		// Make sure that selection varieties are marked as selection methods so that this information can be used to save data correctly
		// into the projectprop table.
		if (form.getSelectionVariatesVariables() != null) {
			for (final SettingDetail settingDetail : form.getSelectionVariatesVariables()) {
				settingDetail.setVariableType(VariableType.SELECTION_METHOD);
				settingDetail.setRole(VariableType.SELECTION_METHOD.getRole());
			}
		}

		if (baselineTraits == null && form.getSelectionVariatesVariables() != null) {
			baselineTraits = form.getSelectionVariatesVariables();
			this.userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
		} else if (baselineTraits != null && form.getSelectionVariatesVariables() != null) {
			baselineTraits.addAll(form.getSelectionVariatesVariables());
			this.userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
		}
		// added code to set the role for the variables add
		SettingsUtil.setSettingDetailRoleAndVariableType(VariableType.STUDY_DETAIL.getId(), studyLevelVariables,
				this.fieldbookMiddlewareService, this.contextUtil.getCurrentProgramUUID());
		SettingsUtil.setSettingDetailRoleAndVariableType(VariableType.GERMPLASM_DESCRIPTOR.getId(), form.getPlotLevelVariables(),
				this.fieldbookMiddlewareService, this.contextUtil.getCurrentProgramUUID());
		SettingsUtil.setSettingDetailRoleAndVariableType(VariableType.TRAIT.getId(), form.getNurseryConditions(),
				this.fieldbookMiddlewareService, this.contextUtil.getCurrentProgramUUID());
		SettingsUtil.setSettingDetailRoleAndVariableType(VariableType.TRAIT.getId(), baselineTraits, this.fieldbookMiddlewareService,
				this.contextUtil.getCurrentProgramUUID());

		final Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, studyLevelVariables,
				form.getPlotLevelVariables(), baselineTraits, this.userSelection, form.getNurseryConditions(),
				this.contextUtil.getCurrentProgramUUID(), description, startDate, endDate, studyUpdate);
		SettingsUtil.setConstantLabels(dataset, this.userSelection.getConstantsWithLabels());
		final Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, this.contextUtil.getCurrentProgramUUID());
		this.userSelection.setWorkbook(workbook);

		this.createStudyDetails(workbook, form.getFolderId(), null, form.getDescription(), form.getStartDate(),
			form.getEndDate(), form.getStudyUpdate(), objective, name, createdBy);

		return "success";
	}

	private void addStudyLevelVariablesFromUserSelectionIfNecessary(final List<SettingDetail> studyLevelVariables,
			final UserSelection userSelection) {

		for (final SettingDetail settingDetailFromUserSelection : userSelection.getStudyLevelConditions()) {

			boolean settingDetailExists = false;

			for (final SettingDetail settingDetail : studyLevelVariables) {
				if (settingDetail.getVariable().getCvTermId().intValue() == settingDetailFromUserSelection.getVariable().getCvTermId()
						.intValue()) {
					settingDetailExists = true;
					break;
				}
			}

			if (!settingDetailExists) {
				studyLevelVariables.add(settingDetailFromUserSelection);
			}

		}

	}

	private void addNurseryTypeFromDesignImport(final List<SettingDetail> studyLevelVariables) {

		final SettingDetail nurseryTypeSettingDetail = new SettingDetail();
		final SettingVariable nurseryTypeSettingVariable = new SettingVariable();

		final Integer nurseryTypeValue = this.userSelection.getNurseryTypeForDesign();

		nurseryTypeSettingDetail.setValue(String.valueOf(nurseryTypeValue));
		nurseryTypeSettingVariable.setCvTermId(TermId.NURSERY_TYPE.getId());
		nurseryTypeSettingVariable.setName("NURSERY_TYPE");
		nurseryTypeSettingVariable.setOperation(Operation.ADD);
		nurseryTypeSettingDetail.setVariable(nurseryTypeSettingVariable);

		if (this.userSelection.getNurseryTypeForDesign() != null && nurseryTypeValue != null) {

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

	private void addExperimentalDesignTypeFromDesignImport(final List<SettingDetail> studyLevelVariables) {

		final SettingDetail nurseryTypeSettingDetail = new SettingDetail();
		final SettingVariable nurseryTypeSettingVariable = new SettingVariable();

		nurseryTypeSettingDetail.setValue(String.valueOf(TermId.OTHER_DESIGN.getId()));
		nurseryTypeSettingVariable.setCvTermId(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
		nurseryTypeSettingVariable.setName("EXPERIMENTAL_DESIGN");
		nurseryTypeSettingVariable.setOperation(Operation.ADD);
		nurseryTypeSettingDetail.setVariable(nurseryTypeSettingVariable);

		if (this.userSelection.getExpDesignVariables() != null && !this.userSelection.getExpDesignVariables().isEmpty()) {

			for (final SettingDetail settingDetail : studyLevelVariables) {
				if (settingDetail.getVariable().getCvTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId()) {
					settingDetail.setValue(String.valueOf(TermId.OTHER_DESIGN.getId()));
					settingDetail.getVariable().setName("EXPERIMENTAL_DESIGN");
					return;
				}
			}

			studyLevelVariables.add(nurseryTypeSettingDetail);
		}

	}

	/**
	 * Sets the form static data.
	 *
	 * @param form the new form static data
	 */
	private void setFormStaticData(final CreateNurseryForm form, final String contextParams) {

		// TODO move the translation of static data from form field into either the use of page model, or via Thymeleaf static evaluation

		form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
		form.setLocationId(AppConstants.LOCATION_ID.getString());
		form.setBreedingMethodUrl(this.fieldbookProperties.getProgramBreedingMethodsUrl());
		form.setOpenGermplasmUrl(this.fieldbookProperties.getGermplasmDetailsUrl());
		form.setBaselineTraitsSegment(VariableType.TRAIT.getId().toString());
		form.setSelectionVariatesSegment(VariableType.SELECTION_METHOD.getId().toString());
		form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
		form.setRequiredFields(AppConstants.FIXED_NURSERY_VARIABLES.getString());
		form.setBreedingMethodCode(AppConstants.BREEDING_METHOD_CODE.getString());

		try {
			form.setCreatedBy(this.fieldbookService.getPersonByUserId(this.contextUtil.getCurrentIbdbUserId()));
		} catch (final MiddlewareQueryException e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Gets the setting detail list.
	 *
	 * @param mode the mode
	 * @return the setting detail list
	 */
	private List<SettingDetail> getSettingDetailList(final int mode) {
		if (mode == VariableType.STUDY_DETAIL.getId()) {
			return this.userSelection.getStudyLevelConditions();
		} else if (mode == VariableType.GERMPLASM_DESCRIPTOR.getId() || mode == VariableType.EXPERIMENTAL_DESIGN.getId()) {
			return this.userSelection.getPlotsLevelList();
		} else if (mode == VariableType.TRAIT.getId() || mode == VariableType.NURSERY_CONDITION.getId()) {
			final List<SettingDetail> newList = new ArrayList<>();

			for (final SettingDetail setting : this.userSelection.getBaselineTraitsList()) {
				newList.add(setting);
			}

			for (final SettingDetail setting : this.userSelection.getNurseryConditions()) {
				newList.add(setting);
			}

			return newList;
		} else if (mode == VariableType.SELECTION_METHOD.getId()) {
			return this.userSelection.getSelectionVariates();
		}
		return new ArrayList<>();
	}

	// TODO : refactor out of this class and into the more general ManageSettingsController

	/**
	 * Displays the Add Setting popup.
	 *
	 * @param mode the mode
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "displayAddSetting/{mode}", method = RequestMethod.GET)
	public Map<String, Object> showAddSettingPopup(@PathVariable final int mode) {
		final Map<String, Object> result = new HashMap<>();
		try {

			final List<StandardVariableReference> standardVariableList =
					this.fieldbookService.filterStandardVariablesForSetting(mode, this.getSettingDetailList(mode));

			try {
				if (this.userSelection.getTraitRefList() == null) {
					final List<TraitClassReference> traitRefList = this.ontologyService.getAllTraitGroupsHierarchy(true);
					this.userSelection.setTraitRefList(traitRefList);
				}

				final List<TraitClassReference> traitRefList = this.userSelection.getTraitRefList();

				// we convert it to map so that it would be easier to check if there is a record or not
				final HashMap<String, StandardVariableReference> mapVariableRef = new HashMap<>();
				if (standardVariableList != null && !standardVariableList.isEmpty()) {
					for (final StandardVariableReference varRef : standardVariableList) {
						mapVariableRef.put(varRef.getId().toString(), varRef);
					}
				}

				final String treeData = TreeViewUtil.convertOntologyTraitsToJson(traitRefList, mapVariableRef);
				final String searchTreeData = TreeViewUtil.convertOntologyTraitsToSearchSingleLevelJson(traitRefList, mapVariableRef);
				result.put("treeData", treeData);
				result.put("searchTreeData", searchTreeData);
			} catch (final Exception e) {
				CreateNurseryController.LOG.error(e.getMessage());
			}
		} catch (final Exception e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Show variable details.
	 *
	 * @param id the id
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "showVariableDetails/{id}", method = RequestMethod.GET)
	public String showVariableDetails(@PathVariable final int id) {
		try {

			final SettingVariable svar = this.getSettingVariable(id);
			if (svar != null) {
				final ObjectMapper om = new ObjectMapper();
				return om.writeValueAsString(svar);
			}

		} catch (final Exception e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
		}
		return "[]";
	}

	/**
	 * Adds the settings.
	 *
	 * @param form the form
	 * @param model the model
	 * @param mode the mode
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/addSettings/{mode}", method = RequestMethod.POST, headers = {"Content-type=application/json"})
	public String addSettings(@RequestBody final CreateNurseryForm form, final Model model, @PathVariable final int mode) {
		final List<SettingDetail> newSettings = new ArrayList<>();
		try {
			final List<SettingVariable> selectedVariables = form.getSelectedVariables();
			if (selectedVariables != null && !selectedVariables.isEmpty()) {
				for (final SettingVariable var : selectedVariables) {
					final Operation operation = this.removeVarFromDeletedList(var, mode);

					var.setOperation(operation);
					this.populateSettingVariable(var);
					final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(var.getCvTermId());
					final SettingDetail newSetting = new SettingDetail(var, possibleValues, null, true);
					final List<ValueReference> possibleValuesFavorite = this.fieldbookService
							.getAllPossibleValuesFavorite(var.getCvTermId(), this.getCurrentProject().getUniqueID(), true);

					newSetting.setPossibleValuesFavorite(possibleValuesFavorite);

					newSetting.setPossibleValuesToJson(possibleValues);
					newSetting.setPossibleValuesFavoriteToJson(possibleValuesFavorite);

					newSettings.add(newSetting);
				}
			}

			if (!newSettings.isEmpty()) {
				return this.addNewSettingDetails(form, mode, newSettings);
			}

		} catch (final Exception e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
		}

		return "[]";
	}

	private Operation removeVarFromDeletedList(final SettingVariable var, final int mode) {
		List<SettingDetail> settingsList = new ArrayList<>();
		if (mode == VariableType.STUDY_DETAIL.getId()) {
			settingsList = this.userSelection.getDeletedStudyLevelConditions();
		} else if (mode == VariableType.GERMPLASM_DESCRIPTOR.getId() || mode == VariableType.EXPERIMENTAL_DESIGN.getId()) {
			settingsList = this.userSelection.getDeletedPlotLevelList();
		} else if (mode == VariableType.TRAIT.getId() || mode == VariableType.SELECTION_METHOD.getId()) {
			settingsList = this.userSelection.getDeletedBaselineTraitsList();
		} else if (mode == VariableType.NURSERY_CONDITION.getId()) {
			settingsList = this.userSelection.getDeletedNurseryConditions();
		}

		Operation operation = Operation.ADD;
		if (settingsList != null) {
			final Iterator<SettingDetail> iter = settingsList.iterator();
			while (iter.hasNext()) {
				final SettingVariable deletedVariable = iter.next().getVariable();
				if (deletedVariable.getCvTermId().equals(var.getCvTermId())) {
					operation = deletedVariable.getOperation();
					iter.remove();
				}
			}
		}
		return operation;
	}

	/**
	 * Clear settings.
	 *
	 * @param form the form
	 * @param model the model
	 * @param session the session
	 * @return the string
	 */
	@RequestMapping(value = "/clearSettings", method = RequestMethod.GET)
	public String clearSettings(@ModelAttribute(CREATE_NURSERY_FORM) final CreateNurseryForm form, final Model model,
			final HttpSession session, final HttpServletRequest request) {

		final String contextParams = ContextUtil.getContextParameterString(request);
		try {
			form.setProjectId(this.getCurrentProjectId());
			this.setFormStaticData(form, contextParams);
			this.assignDefaultValues(form);
			form.setMeasurementRowList(new ArrayList<MeasurementRow>());
		} catch (final Exception e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
		}

		model.addAttribute(CREATE_NURSERY_FORM, form);

		return super.showAjaxPage(model, CreateNurseryController.URL_SETTINGS);
	}

	/**
	 * Adds the new setting details.
	 *
	 * @param form the form
	 * @param mode the mode
	 * @param newDetails the new details
	 * @return the string
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws JsonIoException
	 * @throws Exception the exception
	 */
	private String addNewSettingDetails(final CreateNurseryForm form, final int mode, final List<SettingDetail> newDetails)
			throws JsonGenerationException, JsonMappingException, JsonIoException {
		if (mode == VariableType.STUDY_DETAIL.getId()) {
			if (form.getStudyLevelVariables() == null) {
				form.setStudyLevelVariables(newDetails);
			} else {
				form.getStudyLevelVariables().addAll(newDetails);
			}
			if (this.userSelection.getStudyLevelConditions() == null) {
				this.userSelection.setStudyLevelConditions(newDetails);
			} else {
				this.userSelection.getStudyLevelConditions().addAll(newDetails);
			}

		} else if (mode == VariableType.GERMPLASM_DESCRIPTOR.getId() || mode == VariableType.EXPERIMENTAL_DESIGN.getId()) {
			if (form.getPlotLevelVariables() == null) {
				form.setPlotLevelVariables(newDetails);
			} else {
				form.getPlotLevelVariables().addAll(newDetails);
			}
			if (this.userSelection.getPlotsLevelList() == null) {
				this.userSelection.setPlotsLevelList(newDetails);
			} else {
				this.userSelection.getPlotsLevelList().addAll(newDetails);
			}
		} else if (mode == VariableType.TRAIT.getId()) {
			if (form.getBaselineTraitVariables() == null) {
				form.setBaselineTraitVariables(newDetails);
			} else {
				form.getBaselineTraitVariables().addAll(newDetails);
			}
			if (this.userSelection.getBaselineTraitsList() == null) {
				this.userSelection.setBaselineTraitsList(newDetails);
			} else {
				this.userSelection.getBaselineTraitsList().addAll(newDetails);
			}
		} else if (mode == VariableType.SELECTION_METHOD.getId()) {
			if (form.getSelectionVariatesVariables() == null) {
				form.setSelectionVariatesVariables(newDetails);
			} else {
				form.getSelectionVariatesVariables().addAll(newDetails);
			}
			if (this.userSelection.getSelectionVariates() == null) {
				this.userSelection.setSelectionVariates(newDetails);
			} else {
				this.userSelection.getSelectionVariates().addAll(newDetails);
			}
		} else {
			if (form.getNurseryConditions() == null) {
				form.setNurseryConditions(newDetails);
			} else {
				form.getNurseryConditions().addAll(newDetails);
			}
			if (this.userSelection.getNurseryConditions() == null) {
				this.userSelection.setNurseryConditions(newDetails);
			} else {
				this.userSelection.getNurseryConditions().addAll(newDetails);
			}
		}
		final ObjectMapper om = new ObjectMapper();
		final String jsonData;
		try {
			jsonData = om.writeValueAsString(newDetails);
		} catch (final IOException e) {
			throw new JsonIoException(e.getMessage(), e);
		}
		return jsonData;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteVariable/{mode}/{variableIds}", method = RequestMethod.POST)
	public ResponseEntity<String> deleteVariable(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model,
			@PathVariable final int mode, @PathVariable final String variableIds) {
		try {
			final List<Integer> varIdList = SettingsUtil.parseVariableIds(variableIds);
			final Map<String, String> idNameRetrieveSaveMap = this.fieldbookService.getIdNamePairForRetrieveAndSave();
			for (final Integer variableId : varIdList) {
				if (mode == VariableType.STUDY_DETAIL.getId()) {

					this.addVariableInDeletedList(this.userSelection.getStudyLevelConditions(), mode, variableId, false);
					this.deleteVariableInSession(this.userSelection.getStudyLevelConditions(), variableId);
					if (idNameRetrieveSaveMap.get(variableId) != null) {
						// special case so we must delete it as well
						this.addVariableInDeletedList(this.userSelection.getStudyLevelConditions(), mode,
								Integer.parseInt(idNameRetrieveSaveMap.get(variableId)), false);
						this.deleteVariableInSession(this.userSelection.getStudyLevelConditions(),
								Integer.parseInt(idNameRetrieveSaveMap.get(variableId)));
					}
				} else if (mode == VariableType.GERMPLASM_DESCRIPTOR.getId() || mode == VariableType.EXPERIMENTAL_DESIGN.getId()) {
					this.addVariableInDeletedList(this.userSelection.getPlotsLevelList(), mode, variableId, false);
					this.deleteVariableInSession(this.userSelection.getPlotsLevelList(), variableId);
				} else if (mode == VariableType.TRAIT.getId()) {
					this.addVariableInDeletedList(this.userSelection.getBaselineTraitsList(), mode, variableId, false);
					this.deleteVariableInSession(this.userSelection.getBaselineTraitsList(), variableId);
				} else if (mode == VariableType.SELECTION_METHOD.getId()) {
					this.addVariableInDeletedList(this.userSelection.getSelectionVariates(), mode, variableId, false);
					this.deleteVariableInSession(this.userSelection.getSelectionVariates(), variableId);
				} else {
					this.addVariableInDeletedList(this.userSelection.getNurseryConditions(), mode, variableId, false);
					this.deleteVariableInSession(this.userSelection.getNurseryConditions(), variableId);
				}
			}
		} catch (final MiddlewareException e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
			return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("", HttpStatus.OK);
	}

	private void deleteVariableInSession(final List<SettingDetail> variableList, final int variableId) {
		final Iterator<SettingDetail> iter = variableList.iterator();
		while (iter.hasNext()) {
			if (iter.next().getVariable().getCvTermId().equals(Integer.valueOf(variableId))) {
				iter.remove();
			}
		}
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

	@RequestMapping(value = "/refresh/settings/tab", method = RequestMethod.GET)
	public String refreshSettingsTab(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model,
			final HttpSession session, final HttpServletRequest request) {

		final ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);
		final String contextParams = ContextUtil.getContextParameterString(contextInfo);

		final Workbook workbook = this.userSelection.getWorkbook();
		this.userSelection.setConstantsWithLabels(workbook.getConstants());

		if (this.userSelection.getStudyLevelConditions() != null) {
			for (final SettingDetail detail : this.userSelection.getStudyLevelConditions()) {
				final MeasurementVariable var =
						WorkbookUtil.getMeasurementVariable(workbook.getConditions(), detail.getVariable().getCvTermId());
				this.setSettingDetailsValueFromVariable(var, detail);
			}
		}

		if (this.userSelection.getNurseryConditions() != null) {
			for (final SettingDetail detail : this.userSelection.getNurseryConditions()) {
				final MeasurementVariable var =
						WorkbookUtil.getMeasurementVariable(workbook.getConstants(), detail.getVariable().getCvTermId());
				if (var != null) {
					detail.setValue(var.getValue());
				}
			}
		}

		form.setStudyLevelVariables(this.userSelection.getStudyLevelConditions());
		form.setBaselineTraitVariables(this.userSelection.getBaselineTraitsList());
		form.setSelectionVariatesVariables(this.userSelection.getSelectionVariates());
		form.setPlotLevelVariables(this.userSelection.getPlotsLevelList());
		form.setNurseryConditions(this.userSelection.getNurseryConditions());
		form.setLoadSettings("1");
		form.setProjectId(this.getCurrentProjectId());

		form.setMeasurementRowList(new ArrayList<MeasurementRow>());

		model.addAttribute(CREATE_NURSERY_FORM, form);
		this.setFormStaticData(form, contextParams);
		return super.showAjaxPage(model, CreateNurseryController.URL_SETTINGS);
	}

	protected void setSettingDetailsValueFromVariable(final MeasurementVariable var, final SettingDetail detail)
			{
		if (var.getTermId() == TermId.BREEDING_METHOD_CODE.getId() && var.getValue() != null && !var.getValue().isEmpty()) {
			// set the value of code to ID for it to be selected in the popup
			final Method method = this.fieldbookMiddlewareService.getMethodByCode(var.getValue(), this.contextUtil.getCurrentProgramUUID());
			if (method != null) {
				detail.setValue(String.valueOf(method.getMid()));
			} else {
				detail.setValue("");
			}
		} else if (var.getTermId() == TermId.LOCATION_ID.getId()) {
			this.setLocationVariableValue(detail, var);
		} else {
			final String currentVal = var.getValue();
			if (var.getTermId() != TermId.NURSERY_TYPE.getId()
					&& (detail.getPossibleValues() == null || detail.getPossibleValues().isEmpty())) {
				detail.setValue(currentVal);
			} else {
				// special case for nursery type
				if (var.getValue() != null && detail.getPossibleValues() != null) {

					for (final ValueReference possibleValue : detail.getPossibleValues()) {
						if (var.getValue().equalsIgnoreCase(possibleValue.getDescription())) {
							detail.setValue(possibleValue.getId().toString());
							break;
						}
					}
				}
			}
		}
	}

	protected void setLocationVariableValue(final SettingDetail detail, final MeasurementVariable var) {
		final int locationId = var.getValue() != null && !var.getValue().isEmpty() && NumberUtils.isNumber(var.getValue())
				? Integer.valueOf(var.getValue()) : 0;
		final Location location = this.fieldbookMiddlewareService.getLocationById(locationId);
		if (location != null) {
			detail.setValue(String.valueOf(location.getLocid()));
		} else {
			detail.setValue("");
		}
	}

	protected void setFieldbookMiddlewareService(
			final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	@Override
	protected void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}
}
