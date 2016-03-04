
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;

public class WorkbookUtil {

	public static Integer getMeasurementVariableId(final List<MeasurementVariable> variables, final String name) {
		if (variables != null && !variables.isEmpty()) {
			for (final MeasurementVariable variable : variables) {
				if (variable.getName().equalsIgnoreCase(name)) {
					return variable.getTermId();
				}
			}
		}
		return null;
	}

	public static String getMeasurementVariableName(final List<MeasurementVariable> variables, final int id) {
		if (variables != null && !variables.isEmpty()) {
			for (final MeasurementVariable variable : variables) {
				if (variable != null && variable.getTermId() == id) {
					return variable.getName();
				}
			}
		}
		return null;
	}

	public static String getValueByIdInRow(final List<MeasurementVariable> variables, final int termId, final MeasurementRow row) {
		final String label = WorkbookUtil.getMeasurementVariableName(variables, termId);
		if (label != null) {
			return row.getMeasurementDataValue(label);
		}
		return null;
	}

	public static String getCodeValueByIdInRow(final List<MeasurementVariable> variables, final int termId, final MeasurementRow row) {
		final String label = WorkbookUtil.getMeasurementVariableName(variables, termId);
		if (label != null) {
			return row.getMeasurementData(label).getValue();
		}
		return null;
	}

	public static List<MeasurementRow> filterObservationsByTrialInstance(final List<MeasurementRow> observations, final String trialInstance) {
		final List<MeasurementRow> list = new ArrayList<>();
		if (observations != null && !observations.isEmpty()) {
			final List<MeasurementVariable> variables = observations.get(0).getMeasurementVariables();
			for (final MeasurementRow row : observations) {
				final String value = WorkbookUtil.getValueByIdInRow(variables, TermId.TRIAL_INSTANCE_FACTOR.getId(), row);
				if (value == null || value.equals(trialInstance)) {
					list.add(row);
				}
			}
		}
		return list;
	}

	public static MeasurementVariable getMeasurementVariable(final List<MeasurementVariable> variables, final int id) {
		if (variables != null && !variables.isEmpty()) {
			for (final MeasurementVariable variable : variables) {
				if (variable != null && variable.getTermId() == id) {
					return variable;
				}
			}
		}
		return null;
	}

	public static List<MeasurementRow> createMeasurementRows(final List<List<ValueReference>> list,
			final List<MeasurementVariable> variables) {
		final List<MeasurementRow> observations = new ArrayList<>();

		if (list != null && !list.isEmpty()) {
			for (final List<ValueReference> row : list) {
				final List<MeasurementData> dataList = new ArrayList<>();
				for (final ValueReference ref : row) {
					final MeasurementVariable var = WorkbookUtil.getMeasurementVariable(variables, ref.getId());
					if (var != null) {
						final boolean isEditable = !ref.getId().equals(TermId.TRIAL_INSTANCE_FACTOR.getId());
						final MeasurementData data = new MeasurementData(var.getName(), ref.getName(), isEditable, var.getDataType(), var);
						dataList.add(data);
					}
				}
				observations.add(new MeasurementRow(dataList));
			}
		}

		return observations;
	}

	public static List<MeasurementRow> createMeasurementRowsFromEnvironments(final List<Environment> environments,
			final List<MeasurementVariable> variables) {
		return WorkbookUtil.createMeasurementRowsFromEnvironments(environments, variables, null);
	}

	public static List<MeasurementRow> createMeasurementRowsFromEnvironments(final List<Environment> environments,
			final List<MeasurementVariable> variables, final ExpDesignParameterUi params) {

		final List<MeasurementRow> observations = new ArrayList<>();

		if (environments != null) {
			for (final Environment environment : environments) {
				final List<MeasurementData> dataList = new ArrayList<>();
				for (final MeasurementVariable var : variables) {
					String value = environment.getManagementDetailValues().get(Integer.toString(var.getTermId()));
					Integer phenotypeId = null;
					if (value == null) {
						value = environment.getTrialDetailValues().get(Integer.toString(var.getTermId()));
						phenotypeId = environment.getPhenotypeIDMap().get(Integer.toString(var.getTermId()));
					}
					if (params != null && value == null) {
						final TermId termId = TermId.getById(var.getTermId());
						if (termId != null) {
							value = SettingsUtil.getExperimentalDesignValue(params, termId);
						}
					}

					final boolean isEditable = !(var.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId());
					final MeasurementData data = new MeasurementData(var.getName(), value, isEditable, var.getDataType(), var);
					data.setPhenotypeId(phenotypeId);
					dataList.add(data);
				}
				final MeasurementRow row = new MeasurementRow(environment.getStockId(), environment.getLocationId(), dataList);
				row.setExperimentId((int) environment.getExperimentId());
				observations.add(row);
			}
		}

		return observations;
	}

	public static void addVariateToObservations(final MeasurementVariable mvar, final List<MeasurementRow> observations) {
		if (observations != null) {
			for (final MeasurementRow row : observations) {
				final MeasurementData mData = new MeasurementData();
				mData.setMeasurementVariable(mvar);
				mData.setLabel(mvar.getName());
				mData.setDataType(mvar.getDataType());
				mData.setEditable(true);
				row.getDataList().add(mData);
			}
		}
	}

	public static List<String> getAddedTraits(final List<MeasurementVariable> variables, final List<MeasurementRow> observations) {
		final List<String> newTraits = new ArrayList<>();
		if (observations != null && !observations.isEmpty()) {
			final List<MeasurementVariable> workbookVariables = observations.get(0).getMeasurementVariables();
			if (workbookVariables != null && !workbookVariables.isEmpty()) {
				for (final MeasurementVariable wvar : workbookVariables) {
					if (!wvar.isFactor()) {
						boolean found = false;
						for (final MeasurementVariable var : variables) {
							if (wvar.getTermId() == var.getTermId()) {
								found = true;
								break;
							}
						}
						if (!found) {
							newTraits.add(wvar.getName());
						}
					}
				}
			}
		}
		return newTraits;
	}

	public static List<MeasurementVariable> getAddedTraitVariables(final List<MeasurementVariable> variables,
			final List<MeasurementRow> observations) {
		final List<MeasurementVariable> newTraits = new ArrayList<>();
		if (observations != null && !observations.isEmpty()) {
			final List<MeasurementVariable> workbookVariables = observations.get(0).getMeasurementVariables();
			if (workbookVariables != null && !workbookVariables.isEmpty()) {
				for (final MeasurementVariable wvar : workbookVariables) {
					if (!wvar.isFactor()) {
						boolean found = false;
						for (final MeasurementVariable var : variables) {
							if (wvar.getTermId() == var.getTermId()) {
								found = true;
								break;
							}
						}
						if (!found) {
							wvar.setOperation(Operation.ADD);
							newTraits.add(wvar);
						}
					}
				}
			}
		}
		return newTraits;
	}

	public static void clearNewlyAddedImportTraits(final List<MeasurementVariable> variables, final List<MeasurementRow> observations) {
		final List<MeasurementVariable> newTraits = WorkbookUtil.getAddedTraitVariables(variables, observations);
		final List<Integer> indexForRemoval = new ArrayList<>();
		if (observations != null && !observations.isEmpty()) {
			final List<MeasurementData> initialDataList = observations.get(0).getDataList();
			for (final MeasurementData initialData : initialDataList) {
				for (int index = 0; index < newTraits.size(); index++) {
					if (initialData.getMeasurementVariable().getTermId() == newTraits.get(index).getTermId()) {
						// means this is a newly added trait, we should remove it
						indexForRemoval.add(index);
					}
				}
			}
			if (!indexForRemoval.isEmpty()) {
				for (final MeasurementRow dataRow : observations) {
					for (final Integer removedMeasurementDataIndex : indexForRemoval) {
						dataRow.getDataList().remove(removedMeasurementDataIndex);
					}
				}
			}

		}
	}

	public static void resetWorkbookObservations(final Workbook workbook) {
		if (workbook.getObservations() != null && !workbook.getObservations().isEmpty()) {
			if (workbook.getOriginalObservations() == null || workbook.getOriginalObservations().isEmpty()) {
				final List<MeasurementRow> origObservations = new ArrayList<>();
				for (final MeasurementRow row : workbook.getObservations()) {
					origObservations.add(row.copy());
				}
				workbook.setOriginalObservations(origObservations);
			} else {
				final List<MeasurementRow> observations = new ArrayList<>();
				for (final MeasurementRow row : workbook.getOriginalObservations()) {
					observations.add(row.copy());
				}
				workbook.setObservations(observations);
			}
		}
	}

	public static void revertImportedConditionAndConstantsData(final Workbook workbook) {
		// we need to revert all data
		if (workbook != null) {
			if (workbook.getImportConditionsCopy() != null) {
				workbook.setConditions(workbook.getImportConditionsCopy());
			}
			if (workbook.getImportConstantsCopy() != null) {
				workbook.setConstants(workbook.getImportConstantsCopy());
			}

			if (workbook.getImportTrialObservationsCopy() != null) {
				workbook.setTrialObservations(workbook.getImportTrialObservationsCopy());
			}
		}
	}

	private static boolean inMeasurementDataList(final List<MeasurementData> dataList, final int termId) {
		for (final MeasurementData data : dataList) {
			if (data.getMeasurementVariable().getTermId() == termId) {
				return true;
			}
		}
		return false;
	}

	public static void addMeasurementDataToRowsExp(final List<MeasurementVariable> variableList, final List<MeasurementRow> observations,
			final boolean isVariate, final UserSelection userSelection, final OntologyService ontologyService,
			final FieldbookService fieldbookService, final String programUUID) throws MiddlewareException {
		// add new variables in measurement rows
		if (observations != null && !observations.isEmpty()) {
			for (final MeasurementVariable variable : variableList) {
				if (variable.getOperation().equals(Operation.ADD)
						&& !WorkbookUtil.inMeasurementDataList(observations.get(0).getDataList(), variable.getTermId())) {
					final StandardVariable stdVariable = ontologyService.getStandardVariable(variable.getTermId(), programUUID);
					for (final MeasurementRow row : observations) {
						final MeasurementData measurementData =
								new MeasurementData(variable.getName(), "", true, WorkbookUtil.getDataType(variable.getDataTypeId()),
										variable);

						measurementData.setPhenotypeId(null);
						final int insertIndex = WorkbookUtil.getInsertIndex(row.getDataList(), isVariate);
						row.getDataList().add(insertIndex, measurementData);
					}

					if (ontologyService.getProperty(variable.getProperty()).getTerm().getId() == TermId.BREEDING_METHOD_PROP.getId()
							&& isVariate) {
						variable.setPossibleValues(fieldbookService.getAllBreedingMethods(true, programUUID));
					} else {
						variable.setPossibleValues(WorkbookUtil.transformPossibleValues(stdVariable.getEnumerations()));
					}
				}
			}
		}
	}

	public static void addMeasurementDataToRows(final List<MeasurementVariable> variableList, final boolean isVariate,
			final UserSelection userSelection, final OntologyService ontologyService, final FieldbookService fieldbookService,
			final String programUUID) throws MiddlewareException {
		// add new variables in measurement rows
		for (final MeasurementVariable variable : variableList) {
			if (variable.getOperation().equals(Operation.ADD)) {
				final StandardVariable stdVariable = ontologyService.getStandardVariable(variable.getTermId(), programUUID);
				for (final MeasurementRow row : userSelection.getMeasurementRowList()) {
					final MeasurementData measurementData =
							new MeasurementData(variable.getName(), "", true, WorkbookUtil.getDataType(variable.getDataTypeId()), variable);

					measurementData.setPhenotypeId(null);
					final int insertIndex = WorkbookUtil.getInsertIndex(row.getDataList(), isVariate);
					row.getDataList().add(insertIndex, measurementData);
				}

				if (ontologyService.getProperty(variable.getProperty()).getTerm().getId() == TermId.BREEDING_METHOD_PROP.getId()
						&& isVariate) {
					variable.setPossibleValues(fieldbookService.getAllBreedingMethods(true, programUUID));
				} else {
					variable.setPossibleValues(WorkbookUtil.transformPossibleValues(stdVariable.getEnumerations()));
				}
			}
		}
	}

	public static void addMeasurementDataToRows(final List<MeasurementVariable> variableList,
			final List<MeasurementRow> measurementRowList, final boolean isVariate, final UserSelection userSelection,
			final OntologyService ontologyService, final FieldbookService fieldbookService, final String programUUID)
			throws MiddlewareException {
		// add new variables in measurement rows
		for (final MeasurementVariable variable : variableList) {
			if (variable.getOperation().equals(Operation.ADD)) {
				final StandardVariable stdVariable = ontologyService.getStandardVariable(variable.getTermId(), programUUID);
				for (final MeasurementRow row : measurementRowList) {
					final MeasurementData measurementData =
							new MeasurementData(variable.getName(), "", true, WorkbookUtil.getDataType(variable.getDataTypeId()), variable);

					measurementData.setPhenotypeId(null);
					final int insertIndex = WorkbookUtil.getInsertIndex(row.getDataList(), isVariate);
					row.getDataList().add(insertIndex, measurementData);
				}

				if (ontologyService.getProperty(variable.getProperty()).getTerm().getId() == TermId.BREEDING_METHOD_PROP.getId()
						&& isVariate) {
					variable.setPossibleValues(fieldbookService.getAllBreedingMethods(true, programUUID));
				} else {
					variable.setPossibleValues(WorkbookUtil.transformPossibleValues(stdVariable.getEnumerations()));
				}
			}
		}
	}

	public static void addMeasurementDataToRowsIfNecessary(final List<MeasurementVariable> variableList,
			final List<MeasurementRow> measurementRowList, final boolean isVariate, final UserSelection userSelection,
			final OntologyService ontologyService, final FieldbookService fieldbookService, final String programUUID)
			throws MiddlewareException {

		// add new variables in measurement rows
		for (final MeasurementVariable variable : variableList) {

			final StandardVariable stdVariable = ontologyService.getStandardVariable(variable.getTermId(), programUUID);

			for (final MeasurementRow row : measurementRowList) {

				// only add if the measurement data doesn't exist in row
				if (!WorkbookUtil.inMeasurementDataList(row.getDataList(), variable.getTermId())) {

					final MeasurementData measurementData =
							new MeasurementData(variable.getName(), "", true, WorkbookUtil.getDataType(variable.getDataTypeId()), variable);

					measurementData.setPhenotypeId(null);
					final int insertIndex = WorkbookUtil.getInsertIndex(row.getDataList(), isVariate);
					row.getDataList().add(insertIndex, measurementData);

				}
			}

			if (ontologyService.getProperty(variable.getProperty()).getTerm().getId() == TermId.BREEDING_METHOD_PROP.getId() && isVariate) {
				variable.setPossibleValues(fieldbookService.getAllBreedingMethods(true, programUUID));
			} else {
				variable.setPossibleValues(WorkbookUtil.transformPossibleValues(stdVariable.getEnumerations()));
			}
		}

	}

	/**
	 * Gets the data type.
	 * 
	 * @param dataTypeId the data type id
	 * @return the data type
	 */
	private static String getDataType(final int dataTypeId) {
		// datatype ids: 1120, 1125, 1128, 1130
		if (dataTypeId == TermId.CHARACTER_VARIABLE.getId() || dataTypeId == TermId.TIMESTAMP_VARIABLE.getId()
				|| dataTypeId == TermId.CHARACTER_DBID_VARIABLE.getId() || dataTypeId == TermId.CATEGORICAL_VARIABLE.getId()) {
			return "C";
		} else {
			return "N";
		}
	}

	private static int getInsertIndex(final List<MeasurementData> dataList, final boolean isVariate) {
		int index = -1;
		if (dataList != null) {
			if (!isVariate) {
				for (final MeasurementData data : dataList) {
					if (!data.getMeasurementVariable().isFactor()) {
						return index;
					}
					index++;
				}
			} else {
				return dataList.size();
			}
		}
		return index;
	}

	/**
	 * Transform possible values.
	 * 
	 * @param enumerations the enumerations
	 * @return the list
	 */
	private static List<ValueReference> transformPossibleValues(final List<Enumeration> enumerations) {
		final List<ValueReference> list = new ArrayList<>();

		if (enumerations != null) {
			for (final Enumeration enumeration : enumerations) {
				list.add(new ValueReference(enumeration.getId(), enumeration.getName(), enumeration.getDescription()));
			}
		}

		return list;
	}

	public static void manageExpDesignVariablesAndObs(final Workbook workbook, final Workbook tempWorkbook) {
		// edit original factors to add/delete new/deleted variables based on tempWorkbook.getFactors
		// create map of factors in tempWorkbook and factors in workbook
		final Map<Integer, MeasurementVariable> tempFactorsMap = new HashMap<>();
		final Map<Integer, MeasurementVariable> factorsMap = new HashMap<>();
		final Map<Integer, StandardVariable> expDesignVariablesMap = new HashMap<>();

		if (tempWorkbook.getFactors() != null) {
			for (final MeasurementVariable var : tempWorkbook.getFactors()) {
				tempFactorsMap.put(var.getTermId(), var);
			}
		}

		if (workbook.getFactors() != null) {
			for (final MeasurementVariable var : workbook.getFactors()) {
				factorsMap.put(var.getTermId(), var);
			}
		}

		if (tempWorkbook.getExpDesignVariables() != null) {
			for (final StandardVariable var : tempWorkbook.getExpDesignVariables()) {
				expDesignVariablesMap.put(var.getId(), var);
			}
		}

		for (final MeasurementVariable var : tempWorkbook.getFactors()) {
			if (factorsMap.get(var.getTermId()) == null
					&& expDesignVariablesMap.get(var.getTermId()) != null) {
				var.setOperation(Operation.ADD);
				workbook.getFactors().add(var);
			}
		}

		for (final MeasurementVariable var : workbook.getFactors()) {
			if (tempFactorsMap.get(var.getTermId()) == null && var.getOperation().equals(Operation.UPDATE)) {
				var.setOperation(Operation.DELETE);
			}
		}

		// copy observations generated from experimental design
		workbook.setObservations(tempWorkbook.getObservations());
	}

	public static Map<Integer, MeasurementVariable> createVariableList(final List<MeasurementVariable> factors,
			final List<MeasurementVariable> variates) {
		final Map<Integer, MeasurementVariable> observationVariables = new HashMap<>();
		if (factors != null) {
			for (final MeasurementVariable var : factors) {
				observationVariables.put(var.getTermId(), var);
			}
		}
		if (variates != null) {
			for (final MeasurementVariable var : variates) {
				observationVariables.put(var.getTermId(), var);
			}
		}
		return observationVariables;
	}

	public static void deleteDeletedVariablesInObservations(final Map<Integer, MeasurementVariable> measurementDatasetVariables,
			final List<MeasurementRow> observations) {

		final List<Integer> deletedList = new ArrayList<>();
		if (observations != null && !observations.isEmpty()) {
			for (final MeasurementData data : observations.get(0).getDataList()) {
				if (measurementDatasetVariables.get(data.getMeasurementVariable().getTermId()) == null
						&& data.getMeasurementVariable().getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					deletedList.add(data.getMeasurementVariable().getTermId());
				}
			}
		}
        for (final Integer termId : deletedList) {
            // remove from measurement rows
            int index = 0;
            int varIndex = 0;
            boolean found = false;
            if (observations != null) {
                for (final MeasurementRow row : observations) {
                    if (index == 0) {
                        for (final MeasurementData var : row.getDataList()) {
                            if (var.getMeasurementVariable().getTermId() == termId) {
                                found = true;
                                break;
                            }
                            varIndex++;
                        }
                    }
                    if (found) {
                        row.getDataList().remove(varIndex);
                    } else {
                        break;
                    }
                    index++;
                }
            }
        }
    }

	// we would validate all conditions except for name and the study type
	public static boolean isConditionValidate(final Integer cvTermId) {
		if (cvTermId != null && cvTermId != TermId.STUDY_TYPE.getId() && cvTermId != TermId.STUDY_NAME.getId()
				&& !AppConstants.HIDE_TRIAL_VARIABLE_SETTINGS_FIELDS.getString().contains(cvTermId.toString())) {
			return true;
		}
		return false;
	}

	public static List<MeasurementVariable> getExperimentalDesignVariables(final List<MeasurementVariable> conditions) {
		final List<MeasurementVariable> expDesignVariables = new ArrayList<>();
		if (conditions != null && !conditions.isEmpty()) {
			final List<Integer> expDesignConstants = AppConstants.EXP_DESIGN_VARIABLES.getIntegerList();
			for (final MeasurementVariable condition : conditions) {
				if (expDesignConstants.contains(condition.getTermId())) {
					condition.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
					expDesignVariables.add(condition);
				}
			}
		}
		return expDesignVariables;
	}

	public static void updateTrialObservations(final Workbook workbook, final Workbook temporaryWorkbook) {
		if (temporaryWorkbook.getTrialObservations() != null) {
			workbook.setTrialObservations(temporaryWorkbook.getTrialObservations());
		}
	}

	public static MeasurementData retrieveMeasurementDataFromMeasurementRow(final Integer termId, final List<MeasurementData> dataList) {
		MeasurementData expectedMeasurementData = null;
		for (final MeasurementData measurementData : dataList) {
			final MeasurementVariable variable = measurementData.getMeasurementVariable();
			if (variable.getTermId() == termId) {
				expectedMeasurementData = measurementData;
				break;
			}
		}
		return expectedMeasurementData;
	}

	/**
	 * NOTE: Default Design is when the PLOT NO and ENTRY NO has equal value.
	 * 
	 * @param observations
	 */
	public static void resetObservationToDefaultDesign(final List<MeasurementRow> observations) {
		for (final MeasurementRow row : observations) {
			final List<MeasurementData> dataList = row.getDataList();
			final MeasurementData entryNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.ENTRY_NO.getId(), dataList);
			final MeasurementData plotNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.PLOT_NO.getId(), dataList);

			// make the PLOT_NO equal to ENTRY_NO
			plotNoData.setValue(entryNoData.getValue());
		}
	}
}
