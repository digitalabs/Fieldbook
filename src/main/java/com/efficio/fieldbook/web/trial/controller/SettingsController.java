/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.service.ValidationService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * The Class SettingsController.
 */
public abstract class SettingsController extends AbstractBaseFieldbookController {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);

	/** The workbench service. */
	@Resource
	protected WorkbenchService workbenchService;

	/** The fieldbook service. */
	@Resource
	protected FieldbookService fieldbookService;

	/** The fieldbook middleware service. */
	@Resource
	protected org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	/** The user selection. */
	@Resource
	protected UserSelection userSelection;

	/** The validation service. */
	@Resource
	protected ValidationService validationService;

	/** The data import service. */
	@Resource
	protected DataImportService dataImportService;

	@Resource
	protected OntologyService ontologyService;

	
	/**
	 * Builds the required factors.
	 *
	 * @param requiredFields the required fields
	 * @return the list
	 */
	protected List<Integer> buildVariableIDList(final String requiredFields) {
		return FieldbookUtil.getInstance().buildVariableIDList(requiredFields);
	}

	/**
	 * Creates the setting detail.
	 *
	 * @param id the id
	 * @param name the name
	 * @return the setting detail
	 */
	protected SettingDetail createSettingDetail(final int id, final String name, final String role) {
		final String variableName;
		final StandardVariable stdVar = this.getStandardVariable(id);
		if (name != null && !name.isEmpty()) {
			variableName = name;
		} else {
			variableName = stdVar.getName();
		}

		if (stdVar != null && stdVar.getName() != null) {
			final SettingVariable svar =
					new SettingVariable(variableName, stdVar.getDescription(), stdVar.getProperty().getName(), stdVar.getScale().getName(),
							stdVar.getMethod().getName(), role, stdVar.getDataType().getName(), stdVar.getDataType().getId(),
							stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null
									? stdVar.getConstraints().getMinValue() : null,
							stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null
									? stdVar.getConstraints().getMaxValue() : null);
			svar.setCvTermId(stdVar.getId());
			svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
			svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
			svar.setOperation(Operation.ADD);
			final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(id, true);
			final SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
			final PhenotypicType type = StringUtils.isEmpty(role) ? null : PhenotypicType.getPhenotypicTypeByName(role);
			settingDetail.setRole(type);
			stdVar.setPhenotypicType(type);
			if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
				settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
			}
			settingDetail.setPossibleValuesToJson(possibleValues);
			final List<ValueReference> possibleValuesFavorite =
					this.fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProject().getUniqueID(), true);
			settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
			settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);

			final List<ValueReference> allValues = this.fieldbookService.getAllPossibleValuesWithFilter(svar.getCvTermId(), false);
			settingDetail.setAllValues(allValues);
			settingDetail.setAllValuesToJson(allValues);

			final List<ValueReference> allFavoriteValues =
					this.fieldbookService.getAllPossibleValuesFavorite(svar.getCvTermId(), this.getCurrentProject().getUniqueID(), null);

			final List<ValueReference> intersection = SettingsUtil.intersection(allValues, allFavoriteValues);

			settingDetail.setAllFavoriteValues(intersection);
			settingDetail.setAllFavoriteValuesToJson(intersection);

			return settingDetail;
		} else {
			final SettingVariable svar = new SettingVariable();
			svar.setCvTermId(stdVar != null ? stdVar.getId() : 0);
			return new SettingDetail(svar, null, null, false);
		}
	}

	/**
	 * Creates the setting detail of given variable type
	 *
	 * @param id the variable id
	 * @param alias the variable alias
	 * @param variableType the variable type
	 * @return the setting detail
	 */// TODO TRIAL
	protected SettingDetail createSettingDetailWithVariableType(final int id, final String alias, final VariableType variableType) {
		final Variable variable = this.variableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), id, false, false);

		String variableName = variable.getName();
		if (alias != null && !alias.isEmpty()) {
			variableName = alias;
		}

		final Property property = variable.getProperty();
		final Scale scale = variable.getScale();
		final org.generationcp.middleware.domain.ontology.Method method = variable.getMethod();

		final Double minValue = variable.getMinValue() == null ? null : Double.parseDouble(variable.getMinValue());
		final Double maxValue = variable.getMaxValue() == null ? null : Double.parseDouble(variable.getMaxValue());

		final SettingVariable settingVariable = new SettingVariable(variableName, variable.getDefinition(),
				variable.getProperty().getName(), scale.getName(), method.getName(), variableType.getRole().name(),
				scale.getDataType().getName(), scale.getDataType().getId(), minValue, maxValue);

		// NOTE: Using variable type which is used in project properties
		settingVariable.setVariableTypes(Collections.singleton(variableType));

		settingVariable.setCvTermId(variable.getId());
		settingVariable.setCropOntologyId(property.getCropOntologyId());

		if (variable.getFormula() != null) {
			settingVariable.setFormula(variable.getFormula());
		}

		if (!property.getClasses().isEmpty()) {
			settingVariable.setTraitClass(property.getClasses().iterator().next());
		}

		settingVariable.setOperation(Operation.ADD);
		final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(id);

		final SettingDetail settingDetail = new SettingDetail(settingVariable, possibleValues, null, false);
		settingDetail.setRole(variableType.getRole());
		settingDetail.setVariableType(variableType);

		if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
			settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
		}
		settingDetail.setPossibleValuesToJson(possibleValues);
		final List<ValueReference> possibleValuesFavorite =
				this.fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProject().getUniqueID(), false);
		settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
		settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
		return settingDetail;
	}

	/**
	 * Get standard variable.
	 *
	 * @param id the id
	 * @return the standard variable
	 */
	protected StandardVariable getStandardVariable(final int id) {
		return this.fieldbookMiddlewareService.getStandardVariable(id, this.contextUtil.getCurrentProgramUUID());
	}

	/**
	 * Checks if the measurement table has user input data for a particular variable id
	 *
	 * @param variableId
	 * @return
	 */
	// TODO TRIAL
	public boolean hasMeasurementDataEntered(final int variableId) {
		for (final MeasurementRow row : this.userSelection.getMeasurementRowList()) {
			for (final MeasurementData data : row.getDataList()) {
				if (data.getMeasurementVariable().getTermId() == variableId && data.getValue() != null && !data.getValue().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	// TODO TRIAL
	protected void removeVariablesFromExistingNursery(final List<SettingDetail> settingList, final String variables) {
		final Iterator<SettingDetail> variableList = settingList.iterator();
		while (variableList.hasNext()) {
			if (SettingsUtil.inHideVariableFields(variableList.next().getVariable().getCvTermId(), variables)) {
				variableList.remove();
			}
		}
	}

	//TODO TRIAL
	protected void resetSessionVariablesAfterSave(final Workbook workbook) {

		// update variables in measurement rows
		if (this.userSelection.getMeasurementRowList() != null && !this.userSelection.getMeasurementRowList().isEmpty()) {
			final MeasurementRow row = this.userSelection.getMeasurementRowList().get(0);
			for (final MeasurementVariable mvar : workbook.getMeasurementDatasetVariables()) {
				if (mvar.getOperation() == Operation.UPDATE) {
					for (final MeasurementVariable rvar : row.getMeasurementVariables()) {
						if (mvar.getTermId() == rvar.getTermId()) {
							if (mvar.getName() != null && !"".equals(mvar.getName())) {
								rvar.setName(mvar.getName());
							}
							break;
						}
					}
				}
			}
		}

		// remove deleted variables in measurement rows & header for variates
		this.removeDeletedVariablesInMeasurements(this.userSelection.getDeletedPlotLevelList(), workbook);
		this.removeDeletedVariablesInMeasurements(this.userSelection.getDeletedBaselineTraitsList(), workbook);

		// remove deleted variables in the original lists
		// and change add operation to update
		this.removeDeletedSetUpdate(this.userSelection.getStudyLevelConditions(), workbook.getConditions());
		this.removeDeletedSetUpdate(this.userSelection.getPlotsLevelList(), workbook.getFactors());
		this.removeDeletedSetUpdate(this.userSelection.getBaselineTraitsList(), workbook.getVariates());
		this.removeDeletedSetUpdate(this.userSelection.getStudyConditions(), workbook.getConstants());
		this.removeDeletedSetUpdate(this.userSelection.getTrialLevelVariableList(), null);
		this.removeDeletedSetUpdate(this.userSelection.getSelectionVariates(), null);
		workbook.reset();

		// reorder variates based on measurementrow order
		int index = 0;
		final List<MeasurementVariable> newVariatesList = new ArrayList<>();
		if (this.userSelection.getMeasurementRowList() != null) {
			for (final MeasurementRow row : this.userSelection.getMeasurementRowList()) {
				if (index == 0) {
					for (final MeasurementData var : row.getDataList()) {
						for (final MeasurementVariable varToArrange : workbook.getVariates()) {
							if (var.getMeasurementVariable().getTermId() == varToArrange.getTermId()) {
								newVariatesList.add(varToArrange);
							}
						}
					}
				}
				index++;
				break;
			}
		}
		workbook.setVariates(newVariatesList);

		// remove deleted variables in the deleted lists
		this.resetDeletedLists(this.userSelection);

		// add name variables
		if (this.userSelection.getRemovedConditions() == null) {
			this.userSelection.setRemovedConditions(new ArrayList<SettingDetail>());
		}
		// remove basic details & hidden variables from study level variables
		final String variableIds = AppConstants.FIXED_STUDY_VARIABLES.getString() + AppConstants.CHECK_VARIABLES.getString();
		SettingsUtil.removeBasicDetailsVariables(this.userSelection.getStudyLevelConditions(), variableIds);

		this.removeHiddenVariables(this.userSelection.getStudyLevelConditions(), AppConstants.HIDE_STUDY_FIELDS.getString());
		this.removeRemovedVariablesFromSession(this.userSelection.getStudyLevelConditions(), this.userSelection.getRemovedConditions());
		this.removeHiddenVariables(this.userSelection.getPlotsLevelList(), AppConstants.HIDE_PLOT_FIELDS.getString());
		this.removeRemovedVariablesFromSession(this.userSelection.getPlotsLevelList(), this.userSelection.getRemovedFactors());
		this.addNameVariables(this.userSelection.getRemovedConditions(), workbook, AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
		this.removeCodeVariablesIfNeeded(this.userSelection.getStudyLevelConditions(),
			AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
		// set value of breeding method code back to code after saving
		SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), false, this.ontologyService,
			this.contextUtil.getCurrentProgramUUID());
		// remove selection variates from traits list
		this.removeSelectionVariatesFromTraits(this.userSelection.getBaselineTraitsList());

	}

	//TODO TRIAL
	private void removeRemovedVariablesFromSession(final List<SettingDetail> variableList, final List<SettingDetail> removedVariableList) {
		if (removedVariableList == null || variableList == null) {
			return;
		}
		for (final SettingDetail setting : removedVariableList) {
			final Iterator<SettingDetail> iter = variableList.iterator();
			while (iter.hasNext()) {
				if (iter.next().getVariable().getCvTermId().equals(setting.getVariable().getCvTermId())) {
					iter.remove();
				}
			}
		}
	}

	//TODO TRIAL
	private void removeDeletedVariablesInMeasurements(final List<SettingDetail> deletedList, final Workbook workbook) {
		if (deletedList != null) {
			for (final SettingDetail setting : deletedList) {
				// remove from header
				if (workbook.getMeasurementDatasetVariables() != null) {
					final Iterator<MeasurementVariable> iter = workbook.getMeasurementDatasetVariables().iterator();
					while (iter.hasNext()) {
						if (iter.next().getTermId() == setting.getVariable().getCvTermId()) {
							iter.remove();
						}
					}
				}
			}
		}
	}

	/**
	 * Removes the deleted set update.
	 *
	 * @param settingList the setting list
	 * @param variableList the variable list
	 */
	//TODO TRIAL
	private void removeDeletedSetUpdate(final List<SettingDetail> settingList, final List<MeasurementVariable> variableList) {
		if (settingList != null) {
			// remove all variables having delete and add operation
			final Iterator<SettingDetail> iter = settingList.iterator();
			while (iter.hasNext()) {
				final SettingDetail setting = iter.next();
				if (setting.getVariable().getOperation() != null && setting.getVariable().getOperation().equals(Operation.DELETE)) {
					iter.remove();
				} else if (setting.getVariable().getOperation() != null && setting.getVariable().getOperation().equals(Operation.ADD)) {
					setting.getVariable().setOperation(Operation.UPDATE);
				}
			}
		}

		if (variableList != null) {
			// remove all variables having delete and add operation
			final Iterator<MeasurementVariable> iter2 = variableList.iterator();
			while (iter2.hasNext()) {
				final MeasurementVariable var = iter2.next();
				if (var.getOperation() != null && var.getOperation().equals(Operation.DELETE)) {
					iter2.remove();
				} else if (var.getOperation() != null && var.getOperation().equals(Operation.ADD)) {
					var.setOperation(Operation.UPDATE);
				}
			}
		}
	}

	/**
	 * Reset deleted lists.
	 */
	//TODO TRIAL
	private void resetDeletedLists(final UserSelection userSelection) {
		userSelection.setDeletedStudyLevelConditions(new ArrayList<SettingDetail>());
		userSelection.setDeletedPlotLevelList(new ArrayList<SettingDetail>());
		userSelection.setDeletedBaselineTraitsList(new ArrayList<SettingDetail>());
		userSelection.setDeletedStudyConditions(new ArrayList<SettingDetail>());
		userSelection.setDeletedTrialLevelVariables(new ArrayList<SettingDetail>());
	}

	/**
	 * Removes the selection variates from traits.
	 *
	 * @param traits the traits
	 */
	//TODO TRIAL
	void removeSelectionVariatesFromTraits(final List<SettingDetail> traits) {
		if (traits != null) {
			final Iterator<SettingDetail> iter = traits.iterator();
			while (iter.hasNext()) {
				final SettingDetail var = iter.next();
				final String property = HtmlUtils.htmlUnescape(var.getVariable().getProperty());
				if (SettingsUtil.inPropertyList(this.ontologyService.getProperty(property).getId())) {
					iter.remove();
				}
			}
		}
	}

	/**
	 * Removes the hidden variables.
	 *
	 * @param settingList
	 * @param hiddenVarList
	 */
	//TODO TRIAL
	private void removeHiddenVariables(final List<SettingDetail> settingList, final String hiddenVarList) {
		if (settingList != null) {

			final Iterator<SettingDetail> iter = settingList.iterator();
			while (iter.hasNext()) {
				if (SettingsUtil.inHideVariableFields(iter.next().getVariable().getCvTermId(), hiddenVarList)) {
					iter.remove();
				}
			}
		}
	}

	//TODO TRIAL
	private void addNameVariables(final List<SettingDetail> removedConditions, final Workbook workbook, final String idCodeNamePairs) {
		final Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		final Map<String, SettingDetail> removedConditionsMap = new HashMap<>();
		if (workbook != null && idCodeNamePairs != null && !"".equalsIgnoreCase(idCodeNamePairs)) {
			// we get a map so we can check easily instead of traversing it again
			for (final MeasurementVariable var : workbook.getConditions()) {
				if (var != null) {
					studyConditionMap.put(Integer.toString(var.getTermId()), var);
				}
			}

			if (removedConditions != null) {
				for (final SettingDetail setting : removedConditions) {
					if (setting != null) {
						removedConditionsMap.put(Integer.toString(setting.getVariable().getCvTermId()), setting);
					}
				}
			}
			final String programUUID = this.contextUtil.getCurrentProgramUUID();
			final StringTokenizer tokenizer = new StringTokenizer(idCodeNamePairs, ",");
			if (tokenizer.hasMoreTokens()) {
				// we iterate it
				while (tokenizer.hasMoreTokens()) {
					final String pair = tokenizer.nextToken();
					final StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
					final String idTermId = tokenizerPair.nextToken();
					final String codeTermId = tokenizerPair.nextToken();
					final String nameTermId = tokenizerPair.nextToken();

					final Method method = this.getMethod(studyConditionMap, idTermId, codeTermId, programUUID);

					// add code to the removed conditions if code is not yet in the list
					if (studyConditionMap.get(idTermId) != null && studyConditionMap.get(codeTermId) != null
							&& removedConditionsMap.get(codeTermId) == null) {
						this.addSettingDetail(removedConditions, removedConditionsMap, studyConditionMap, codeTermId,
								method == null ? "" : method.getMcode());
					}

					// add name to the removed conditions if name is not yet in the list
					if (studyConditionMap.get(nameTermId) != null && removedConditionsMap.get(nameTermId) == null) {
						this.addSettingDetail(removedConditions, removedConditionsMap, studyConditionMap, nameTermId,
								method == null ? "" : method.getMname());

					}
				}
			}
		}
	}

	//TODO TRIAL
	protected Method getMethod(final Map<String, MeasurementVariable> studyConditionMap, final String idTermId, final String codeTermId,
			final String programUUID) {
		Method method = null;
		if (studyConditionMap.get(idTermId) != null) {
			method = studyConditionMap.get(idTermId).getValue().isEmpty() ? null
					: this.fieldbookMiddlewareService.getMethodById(Double.valueOf(studyConditionMap.get(idTermId).getValue()).intValue());
		} else if (studyConditionMap.get(codeTermId) != null) {
			method = studyConditionMap.get(codeTermId).getValue().isEmpty() ? null
					: this.fieldbookMiddlewareService.getMethodByCode(studyConditionMap.get(codeTermId).getValue(), programUUID);
		}
		return method;
	}

	//TODO TRIAL
	private void addSettingDetail(final List<SettingDetail> removedConditions, final Map<String, SettingDetail> removedConditionsMap,
			final Map<String, MeasurementVariable> studyConditionMap, final String id, final String value) {
		if (removedConditionsMap.get(id) == null) {
			removedConditions.add(this.createSettingDetail(Integer.parseInt(id), studyConditionMap.get(id).getName(), null));
		}
		if (removedConditions != null) {
			for (final SettingDetail setting : removedConditions) {
				if (setting.getVariable().getCvTermId() == Integer.parseInt(id)) {
					setting.setValue(value);
					setting.getVariable().setOperation(Operation.UPDATE);
				}
			}
		}
	}

	//TODO TRIAL
	private void removeCodeVariablesIfNeeded(final List<SettingDetail> variableList, final String idCodeNamePairs) {
		final Map<String, SettingDetail> variableListMap = new HashMap<>();
		if (variableList != null) {
			for (final SettingDetail setting : variableList) {
				if (setting != null) {
					variableListMap.put(Integer.toString(setting.getVariable().getCvTermId()), setting);
				}
			}
		}

		final StringTokenizer tokenizer = new StringTokenizer(idCodeNamePairs, ",");
		if (tokenizer.hasMoreTokens()) {
			// we iterate it
			while (tokenizer.hasMoreTokens()) {
				final String pair = tokenizer.nextToken();
				final StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
				final String idTermId = tokenizerPair.nextToken();
				final String codeTermId = tokenizerPair.nextToken();

				final Iterator<SettingDetail> iter = variableList != null ? variableList.iterator() : null;
				while (iter.hasNext()) {
					final Integer cvTermId = iter.next().getVariable().getCvTermId();
					if (cvTermId.equals(Integer.parseInt(codeTermId)) && variableListMap.get(idTermId) != null) {
						iter.remove();
					}
				}
			}
		}
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	//TODO TRIAL
	protected void addVariableInDeletedList(final List<SettingDetail> currentList, final int mode, final int variableId,
			final boolean createNewSettingIfNull) {
		SettingDetail newSetting = null;
		for (final SettingDetail setting : currentList) {
			if (setting.getVariable().getCvTermId().equals(Integer.valueOf(variableId))) {
				newSetting = setting;
			}
		}

		if (newSetting == null && createNewSettingIfNull) {
			try {
				newSetting = this.createSettingDetail(variableId, "", "");
				newSetting.getVariable().setOperation(Operation.UPDATE);
			} catch (final MiddlewareQueryException e) {
				SettingsController.LOG.error(e.getMessage(), e);
			}
		} else if (newSetting == null) {
			return;
		}

		if (mode == VariableType.STUDY_DETAIL.getId()) {
			if (this.userSelection.getDeletedStudyLevelConditions() == null) {
				this.userSelection.setDeletedStudyLevelConditions(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedStudyLevelConditions().add(newSetting);
		} else if (mode == VariableType.EXPERIMENTAL_DESIGN.getId() || mode == VariableType.GERMPLASM_DESCRIPTOR.getId()) {
			if (this.userSelection.getDeletedPlotLevelList() == null) {
				this.userSelection.setDeletedPlotLevelList(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedPlotLevelList().add(newSetting);
		} else if (mode == VariableType.TRAIT.getId() || mode == VariableType.SELECTION_METHOD.getId()) {
			this.addNewSettingToDeletedBaselineTraits(newSetting);
		} else if (mode == VariableType.STUDY_CONDITION.getId()) {
			if (this.userSelection.getDeletedStudyConditions() == null) {
				this.userSelection.setDeletedStudyConditions(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedStudyConditions().add(newSetting);
		} else if (mode == VariableType.ENVIRONMENT_DETAIL.getId()) {
			if (this.userSelection.getDeletedTrialLevelVariables() == null) {
				this.userSelection.setDeletedTrialLevelVariables(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedTrialLevelVariables().add(newSetting);
		} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
			if (this.userSelection.getDeletedTreatmentFactors() == null) {
				this.userSelection.setDeletedTreatmentFactors(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedTreatmentFactors().add(newSetting);
		}
	}

	//TODO TRIAL
	private void addNewSettingToDeletedBaselineTraits(final SettingDetail newSetting) {
		if (this.userSelection.getDeletedBaselineTraitsList() == null) {
			this.userSelection.setDeletedBaselineTraitsList(new ArrayList<SettingDetail>());
		}
		this.userSelection.getDeletedBaselineTraitsList().add(newSetting);
	}

	public void setFieldbookService(final FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}

	
	public void setOntologyService(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

}
