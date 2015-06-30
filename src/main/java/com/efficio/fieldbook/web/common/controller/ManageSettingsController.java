package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.PropertyTreeSummary;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.ontology.OntologyVariableSummary;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.OntologyVariableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 * <p/>
 * This controller class handles back end functionality that are common to the operations that require management of settings (Create/Edit
 * Nursery/Trial)
 */

@Controller
@RequestMapping(value = ManageSettingsController.URL)
public class ManageSettingsController extends SettingsController {

	public static final String URL = "/manageSettings";

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ManageSettingsController.class);

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Resource
	private ContextUtil contextUtil;

	@ResponseBody
	@RequestMapping(value = "/settings/role/{roleId}", method = RequestMethod.GET,
					produces = "application/json; charset=utf-8")
	public List<PropertyTreeSummary> getOntologyPropertiesByRole(@PathVariable Integer roleId) {
		assert !Objects.equals(roleId, null);

		PhenotypicType phenotypicTypeById = PhenotypicType.getPhenotypicTypeById(roleId);

		assert !Objects.equals(phenotypicTypeById, null);

		Set<Integer> variableTypes = VariableType.getVariableTypesIdsByPhenotype(phenotypicTypeById);

		return getOntologyPropertiesByVariableType(variableTypes.toArray(new Integer[variableTypes.size()]), null, false, true);
	}

	@ResponseBody
	@RequestMapping(value = "/settings/properties", method = RequestMethod.GET,
					produces = "application/json; charset=utf-8")
	public List<PropertyTreeSummary> getOntologyPropertiesByVariableType(
			@RequestParam(value = "type", required = true) Integer[] variableTypes,
			@RequestParam(value = "classes", required = false) String[] classes, @RequestParam(required = false) boolean isTrial,
			@RequestParam(required = false) boolean showHiddenVariables) {
		
		
		// HACK! Workaround if callie is from design import
		List<Integer> correctedVarTypes = new ArrayList<>();
		for (Integer varType : variableTypes) {
			// this is not a varType but a phenotype
			if (!varType.toString().startsWith("18")) {
				PhenotypicType phenotypicTypeById = PhenotypicType.getPhenotypicTypeById(varType);
				correctedVarTypes.addAll(VariableType
						.getVariableTypesIdsByPhenotype(phenotypicTypeById));
			} else {
				correctedVarTypes.add(varType);
			}
		}
		
		List<PropertyTreeSummary> propertyTreeList = new ArrayList<>();

		try {
			Set<VariableType> selectedVariableTypes = new HashSet<>();
			List<String> varTypeValues = new ArrayList<>();
			for (Integer varType : correctedVarTypes) {
				selectedVariableTypes.add(VariableType.getById(varType));
				varTypeValues.add(VariableType.getById(varType).getName());
			}

			List<Property> properties;

			properties = ontologyPropertyDataManager
					.getAllPropertiesWithClassAndVariableType(classes, varTypeValues.toArray(new String[varTypeValues.size()]));

			// fetch all standard variables given property
			for (Property property : properties) {
				OntologyVariableInfo variableFilterOptions = new OntologyVariableInfo();
				variableFilterOptions.setProgramUuid(contextUtil.getCurrentProgramUUID());
				variableFilterOptions.setPropertyId(property.getId());

				variableFilterOptions.getVariableTypes().addAll(selectedVariableTypes);

				HashSet<Integer> filteredVariables = new HashSet<>();
				if (!showHiddenVariables) {
					filteredVariables.addAll(filterOutVariablesByVariableType(selectedVariableTypes, isTrial));
				}

				List<OntologyVariableSummary> ontologyList =
						ontologyVariableDataManager.getWithFilter(variableFilterOptions, filteredVariables);

				if (ontologyList.isEmpty()) {
					continue;
				}

				if (selectedVariableTypes.contains(VariableType.TREATMENT_FACTOR)) {
					ontologyVariableDataManager.processTreatmentFactorHasPairValue(ontologyList,
							AppConstants.CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());
				}

				PropertyTreeSummary propertyTree = new PropertyTreeSummary(property, ontologyList);
				propertyTreeList.add(propertyTree);

			}

			// Todo: what to make of this.fieldbookMiddlewareService.filterStandardVariablesByIsAIds(...)

		} catch (MiddlewareException e) {
			LOG.error(e.getMessage(), e);
		}

		return propertyTreeList;
	}

	private List<Integer> filterOutVariablesByVariableType(Set<VariableType> selectedVariableTypes, boolean isTrial) {
		List<Integer> cvTermIDs = new ArrayList<>();

		for (VariableType varType : selectedVariableTypes) {
			switch (varType) {
				case STUDY_DETAIL:
					cvTermIDs.addAll(AppConstants.HIDE_STUDY_DETAIL_VARIABLES.getIntegerList());

				case SELECTION_METHOD:
					cvTermIDs.addAll(AppConstants.HIDE_ID_VARIABLES.getIntegerList());
				case ENVIRONMENT_DETAIL:
					cvTermIDs.addAll(AppConstants.HIDE_TRIAL_VARIABLES.getIntegerList());

					if (isTrial) {
						cvTermIDs.addAll(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getIntegerList());
						cvTermIDs.addAll(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS_FROM_POPUP.getIntegerList());
					}

				case TREATMENT_FACTOR:
					cvTermIDs.addAll(AppConstants.CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());
				default:
					cvTermIDs.addAll(AppConstants.HIDE_PLOT_FIELDS.getIntegerList());
			}
		}

		return cvTermIDs;
	}

	/**
	 * Adds the settings.
	 *
	 * @param form the form
	 * @param mode the mode
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/addSettings/{mode}", method = RequestMethod.POST)
	public List<SettingDetail> addSettings(@RequestBody CreateNurseryForm form, @PathVariable int mode) {
		List<SettingDetail> newSettings = new ArrayList<SettingDetail>();
		try {
			List<SettingVariable> selectedVariables = form.getSelectedVariables();
			if (selectedVariables != null && !selectedVariables.isEmpty()) {
				for (SettingVariable var : selectedVariables) {
					Operation operation = this.removeVarFromDeletedList(var, mode);

					var.setOperation(operation);
					this.populateSettingVariable(var);
					List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(var.getCvTermId());
					SettingDetail newSetting = new SettingDetail(var, possibleValues, null, true);
					List<ValueReference> possibleValuesFavorite =
							this.fieldbookService.getAllPossibleValuesFavorite(var.getCvTermId(), this.getCurrentProject().getUniqueID());
					newSetting.setPossibleValuesFavorite(possibleValuesFavorite);
					newSettings.add(newSetting);
				}
			}

			if (newSettings != null && !newSettings.isEmpty()) {
				this.addNewSettingDetails(mode, newSettings);
				return newSettings;
			}

		} catch (Exception e) {
			ManageSettingsController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<SettingDetail>();
	}

	/**
	 * Adds the new setting details.
	 *
	 * @param mode       the mode
	 * @param newDetails the new details
	 * @return the string
	 * @throws Exception the exception
	 */
	private void addNewSettingDetails(int mode, List<SettingDetail> newDetails) throws Exception {
		SettingsUtil.addNewSettingDetails(mode, newDetails, userSelection);
	}

	private Operation removeVarFromDeletedList(SettingVariable var, int mode) {
		List<SettingDetail> settingsList = new ArrayList<SettingDetail>();
		if (mode == VariableType.STUDY_DETAIL.getId()) {
			settingsList = this.userSelection.getDeletedStudyLevelConditions();
		} else if (mode == VariableType.EXPERIMENTAL_DESIGN.getId() || mode == VariableType.GERMPLASM_DESCRIPTOR.getId()) {
			settingsList = this.userSelection.getDeletedPlotLevelList();
		} else if (mode == VariableType.TRAIT.getId() || mode == VariableType.SELECTION_METHOD.getId()) {
			settingsList = this.userSelection.getDeletedBaselineTraitsList();
		} else if (mode == VariableType.NURSERY_CONDITION.getId()) {
			settingsList = this.userSelection.getDeletedNurseryConditions();
		} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
			settingsList = this.userSelection.getDeletedTreatmentFactors();
		} else if (mode == VariableType.ENVIRONMENT_DETAIL.getId()) {
			settingsList = this.userSelection.getDeletedTrialLevelVariables();
		}

		Operation operation = Operation.ADD;
		if (settingsList != null) {
			Iterator<SettingDetail> iter = settingsList.iterator();
			while (iter.hasNext()) {
				SettingVariable deletedVariable = iter.next().getVariable();
				if (deletedVariable.getCvTermId().equals(Integer.valueOf(var.getCvTermId()))) {
					operation = deletedVariable.getOperation();
					iter.remove();
				}
			}
		}
		return operation;
	}

	/**
	 * Gets the setting detail list.
	 *
	 * @param mode the mode
	 * @return the setting detail list
	 */
	private List<SettingDetail> getSettingDetailList(int mode) {
		if (mode == VariableType.STUDY_DETAIL.getId()) {
			return this.userSelection.getStudyLevelConditions();
		} else if (mode == VariableType.GERMPLASM_DESCRIPTOR.getId() || mode == VariableType.EXPERIMENTAL_DESIGN.getId()) {
			return this.userSelection.getPlotsLevelList();
		} else if (mode == VariableType.TRAIT.getId() || mode == VariableType.NURSERY_CONDITION.getId()) {
			List<SettingDetail> newList = new ArrayList<SettingDetail>();

			if (this.userSelection.getBaselineTraitsList() != null) {
				for (SettingDetail setting : this.userSelection.getBaselineTraitsList()) {
					newList.add(setting);
				}
			}
			if (this.userSelection.getNurseryConditions() != null) {
				for (SettingDetail setting : this.userSelection.getNurseryConditions()) {
					newList.add(setting);
				}
			}
			return newList;
		} else if (mode == VariableType.SELECTION_METHOD.getId()) {
			return this.userSelection.getSelectionVariates();
		} else if (mode == VariableType.ENVIRONMENT_DETAIL.getId()) {
			return this.userSelection.getTrialLevelVariableList();
		} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
			return this.userSelection.getTreatmentFactors();
		}
		return new ArrayList<SettingDetail>();
	}

	@ResponseBody
	@RequestMapping(value = "/deleteVariable/{mode}", method = RequestMethod.POST)
	public boolean deleteVariable(@PathVariable int mode, @RequestBody List<Integer> ids) {

		for (Integer id : ids) {
			this.deleteVariable(mode, id);
		}

		return true;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteVariable/{mode}/{variableId}", method = RequestMethod.POST)
	public ResponseEntity<String> deleteVariable(@PathVariable int mode, @PathVariable int variableId) {
		try {
			Map<String, String> idNameRetrieveSaveMap = this.fieldbookService.getIdNamePairForRetrieveAndSave();
			if (mode == VariableType.STUDY_DETAIL.getId()) {

				this.addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(userSelection.getStudyLevelConditions(), variableId);
				if (idNameRetrieveSaveMap.get(variableId) != null) {
					//special case so we must delete it as well
					this.addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode,
							Integer.parseInt(idNameRetrieveSaveMap.get(variableId)), true);
					SettingsUtil.deleteVariableInSession(this.userSelection.getStudyLevelConditions(),
							Integer.parseInt(idNameRetrieveSaveMap.get(variableId)));
				}
			} else if (mode == VariableType.EXPERIMENTAL_DESIGN.getId() || mode == VariableType.GERMPLASM_DESCRIPTOR.getId()) {
				this.addVariableInDeletedList(this.userSelection.getPlotsLevelList(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getPlotsLevelList(), variableId);
			} else if (mode == VariableType.TRAIT.getId()) {
				this.addVariableInDeletedList(this.userSelection.getBaselineTraitsList(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getBaselineTraitsList(), variableId);
			} else if (mode == VariableType.SELECTION_METHOD.getId()) {
				this.addVariableInDeletedList(this.userSelection.getSelectionVariates(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getSelectionVariates(), variableId);
			} else if (mode == VariableType.NURSERY_CONDITION.getId()) {
				this.addVariableInDeletedList(this.userSelection.getNurseryConditions(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getNurseryConditions(), variableId);
			} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
				this.addVariableInDeletedList(this.userSelection.getTreatmentFactors(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getTreatmentFactors(), variableId);
			} else {
				this.addVariableInDeletedList(this.userSelection.getTrialLevelVariableList(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getTrialLevelVariableList(), variableId);
			}
		} catch (MiddlewareException e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>("", HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/deleteTreatmentFactorVariable", method = RequestMethod.POST)
	public String deleteTreatmentFactorVariable(@RequestBody Map<String, Integer> ids) {
		Integer levelID = ids.get("levelID");
		Integer valueID = ids.get("valueID");
		if (levelID != null && levelID != 0) {
			this.deleteVariable(VariableType.TREATMENT_FACTOR.getId(), levelID);
		}

		if (valueID != null && valueID != 0) {
			this.deleteVariable(VariableType.TREATMENT_FACTOR.getId(), valueID);
		}

		return "";
	}

	@ResponseBody
	@RequestMapping(value = "/hasMeasurementData/{mode}", method = RequestMethod.POST)
	public boolean hasMeasurementData(@RequestBody List<Integer> ids, @PathVariable int mode) {
		for (Integer id : ids) {
			if (this.checkModeAndHasMeasurementData(mode, id)) {
				return true;
			}
		}
		return false;
	}

	@ResponseBody
	@RequestMapping(value = "/hasMeasurementData/environmentNo/{environmentNo}", method = RequestMethod.POST)
	public boolean hasMeasurementDataOnEnvironment(@RequestBody List<Integer> ids, @PathVariable int environmentNo) {
		Workbook workbook = this.userSelection.getWorkbook();
		List<MeasurementRow> observationsOnEnvironment = this.getObservationsOnEnvironment(workbook, environmentNo);

		for (Integer variableId : ids) {
			if (SettingsController.hasMeasurementDataEntered(variableId, observationsOnEnvironment)) {
				return true;
			}
		}

		return false;
	}

	protected List<MeasurementRow> getObservationsOnEnvironment(Workbook workbook, int environmentNo) {
		List<MeasurementRow> observations = workbook.getObservations();
		List<MeasurementRow> filteredObservations = new ArrayList<MeasurementRow>();

		// we do a matching of the name here so there won't be a problem in the data table
		for (MeasurementRow row : observations) {
			List<MeasurementData> dataList = row.getDataList();
			for (MeasurementData data : dataList) {
				if (this.isEnvironmentNotDeleted(data, environmentNo)) {
					filteredObservations.add(row);
					break;
				}

			}
		}
		return filteredObservations;
	}

	private boolean isEnvironmentNotDeleted(MeasurementData data, int environmentNo) {
		if (data.getMeasurementVariable() != null) {
			MeasurementVariable var = data.getMeasurementVariable();
			if (var != null && var.getName() != null && ("TRIAL_INSTANCE".equalsIgnoreCase(var.getName()) || "TRIAL"
					.equalsIgnoreCase(var.getName())) && data.getValue().equals(String.valueOf(environmentNo))) {
				return true;
			}
		}
		return false;
	}

	protected boolean checkModeAndHasMeasurementData(int mode, int variableId) {
		return mode == VariableType.TRAIT.getId() && this.userSelection.getMeasurementRowList() != null && !this.userSelection
				.getMeasurementRowList().isEmpty() && this.hasMeasurementDataEntered(variableId);
	}

	@Override
	public String getContentName() {
		return null;
	}
}
