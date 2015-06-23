
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.Enumeration;
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

	public static Integer getMeasurementVariableId(List<MeasurementVariable> variables, String name) {
		if (variables != null && !variables.isEmpty()) {
			for (MeasurementVariable variable : variables) {
				if (variable.getName().equalsIgnoreCase(name)) {
					return variable.getTermId();
				}
			}
		}
		return null;
	}

	public static String getMeasurementVariableName(List<MeasurementVariable> variables, int id) {
		if (variables != null && !variables.isEmpty()) {
			for (MeasurementVariable variable : variables) {
				if (variable != null && variable.getTermId() == id) {
					return variable.getName();
				}
			}
		}
		return null;
	}

	public static String getValueByIdInRow(List<MeasurementVariable> variables, int termId, MeasurementRow row) {
		String label = WorkbookUtil.getMeasurementVariableName(variables, termId);
		if (label != null) {
			return row.getMeasurementDataValue(label);
		}
		return null;
	}

	public static String getCodeValueByIdInRow(List<MeasurementVariable> variables, int termId, MeasurementRow row) {
		String label = WorkbookUtil.getMeasurementVariableName(variables, termId);
		if (label != null) {
			return row.getMeasurementData(label).getValue();
		}
		return null;
	}

	public static List<MeasurementRow> filterObservationsByTrialInstance(List<MeasurementRow> observations, String trialInstance) {
		List<MeasurementRow> list = new ArrayList<MeasurementRow>();
		if (observations != null && !observations.isEmpty()) {
			List<MeasurementVariable> variables = observations.get(0).getMeasurementVariables();
			for (MeasurementRow row : observations) {
				String value = WorkbookUtil.getValueByIdInRow(variables, TermId.TRIAL_INSTANCE_FACTOR.getId(), row);
				if (value == null || value != null && value.equals(trialInstance)) {
					list.add(row);
				}
			}
		}
		return list;
	}

	public static MeasurementVariable getMeasurementVariable(List<MeasurementVariable> variables, int id) {
		if (variables != null && !variables.isEmpty()) {
			for (MeasurementVariable variable : variables) {
				if (variable != null && variable.getTermId() == id) {
					return variable;
				}
			}
		}
		return null;
	}

	public static List<MeasurementRow> createMeasurementRows(List<List<ValueReference>> list, List<MeasurementVariable> variables) {
		List<MeasurementRow> observations = new ArrayList<MeasurementRow>();

		if (list != null && !list.isEmpty()) {
			for (List<ValueReference> row : list) {
				List<MeasurementData> dataList = new ArrayList<MeasurementData>();
				for (ValueReference ref : row) {
					MeasurementVariable var = WorkbookUtil.getMeasurementVariable(variables, ref.getId());
					if (var != null) {
						boolean isEditable = !ref.getId().equals(TermId.TRIAL_INSTANCE_FACTOR.getId());
						MeasurementData data = new MeasurementData(var.getName(), ref.getName(), isEditable, var.getDataType(), var);
						dataList.add(data);
					}
				}
				observations.add(new MeasurementRow(dataList));
			}
		}

		return observations;
	}

	public static List<MeasurementRow> createMeasurementRowsFromEnvironments(List<Environment> environments,
			List<MeasurementVariable> variables) {
		return WorkbookUtil.createMeasurementRowsFromEnvironments(environments, variables, null);
	}

	public static List<MeasurementRow> createMeasurementRowsFromEnvironments(List<Environment> environments,
			List<MeasurementVariable> variables, ExpDesignParameterUi params) {

		List<MeasurementRow> observations = new ArrayList<MeasurementRow>();

		if (environments != null) {
			for (Environment environment : environments) {
				List<MeasurementData> dataList = new ArrayList<MeasurementData>();
				for (MeasurementVariable var : variables) {
					String value = environment.getManagementDetailValues().get(Integer.toString(var.getTermId()));
					Integer phenotypeId = null;
					if (value == null) {
						value = environment.getTrialDetailValues().get(Integer.toString(var.getTermId()));
						phenotypeId = environment.getPhenotypeIDMap().get(Integer.toString(var.getTermId()));
					}
					if (params != null && value == null) {
						TermId termId = TermId.getById(var.getTermId());
						if (termId != null) {
							value = SettingsUtil.getExperimentalDesignValue(params, termId);
						}
					}

					boolean isEditable = !(var.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId());
					MeasurementData data = new MeasurementData(var.getName(), value, isEditable, var.getDataType(), var);
					data.setPhenotypeId(phenotypeId);
					dataList.add(data);
				}
				MeasurementRow row = new MeasurementRow(environment.getStockId(), environment.getLocationId(), dataList);
				row.setExperimentId((int) environment.getExperimentId());
				observations.add(row);
			}
		}

		return observations;
	}

	public static void addVariateToObservations(MeasurementVariable mvar, List<MeasurementRow> observations) {
		if (observations != null) {
			for (MeasurementRow row : observations) {
				MeasurementData mData = new MeasurementData();
				mData.setMeasurementVariable(mvar);
				mData.setLabel(mvar.getName());
				mData.setDataType(mvar.getDataType());
				mData.setEditable(true);
				row.getDataList().add(mData);
			}
		}
	}

	public static List<String> getAddedTraits(List<MeasurementVariable> variables, List<MeasurementRow> observations) {
		List<String> newTraits = new ArrayList<String>();
		if (observations != null && !observations.isEmpty()) {
			List<MeasurementVariable> workbookVariables = observations.get(0).getMeasurementVariables();
			if (workbookVariables != null && !workbookVariables.isEmpty()) {
				for (MeasurementVariable wvar : workbookVariables) {
					if (!wvar.isFactor()) {
						boolean found = false;
						for (MeasurementVariable var : variables) {
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

	public static List<MeasurementVariable> getAddedTraitVariables(List<MeasurementVariable> variables, List<MeasurementRow> observations) {
		List<MeasurementVariable> newTraits = new ArrayList<MeasurementVariable>();
		if (observations != null && !observations.isEmpty()) {
			List<MeasurementVariable> workbookVariables = observations.get(0).getMeasurementVariables();
			if (workbookVariables != null && !workbookVariables.isEmpty()) {
				for (MeasurementVariable wvar : workbookVariables) {
					if (!wvar.isFactor()) {
						boolean found = false;
						for (MeasurementVariable var : variables) {
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

	public static void clearNewlyAddedImportTraits(List<MeasurementVariable> variables, List<MeasurementRow> observations) {
		List<MeasurementVariable> newTraits = WorkbookUtil.getAddedTraitVariables(variables, observations);
		List<Integer> indexForRemoval = new ArrayList<Integer>();
		if (observations != null && !observations.isEmpty()) {
			List<MeasurementData> initialDataList = observations.get(0).getDataList();
			for (MeasurementData initialData : initialDataList) {
				for (int index = 0; index < newTraits.size(); index++) {
					if (initialData.getMeasurementVariable().getTermId() == newTraits.get(index).getTermId()) {
						// means this is a newly added trait, we should remove it
						indexForRemoval.add(Integer.valueOf(index));
					}
				}
			}
			if (indexForRemoval != null && !indexForRemoval.isEmpty()) {
				for (MeasurementRow dataRow : observations) {
					for (Integer removedMeasurementDataIndex : indexForRemoval) {
						dataRow.getDataList().remove(removedMeasurementDataIndex);
					}
				}
			}

		}
	}

	public static void resetWorkbookObservations(Workbook workbook) {
		if (workbook.getObservations() != null && !workbook.getObservations().isEmpty()) {
			if (workbook.getOriginalObservations() == null || workbook.getOriginalObservations().isEmpty()) {
				List<MeasurementRow> origObservations = new ArrayList<MeasurementRow>();
				for (MeasurementRow row : workbook.getObservations()) {
					origObservations.add(row.copy());
				}
				workbook.setOriginalObservations(origObservations);
			} else {
				List<MeasurementRow> observations = new ArrayList<MeasurementRow>();
				for (MeasurementRow row : workbook.getOriginalObservations()) {
					observations.add(row.copy());
				}
				workbook.setObservations(observations);
			}
		}
	}

	public static void revertImportedConditionAndConstantsData(Workbook workbook) {
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

	private static boolean inMeasurementDataList(List<MeasurementData> dataList, int termId) {
		for (MeasurementData data : dataList) {
			if (data.getMeasurementVariable().getTermId() == termId) {
				return true;
			}
		}
		return false;
	}

	public static void addMeasurementDataToRowsExp(List<MeasurementVariable> variableList, 
			List<MeasurementRow> observations, boolean isVariate, 
			UserSelection userSelection, OntologyService ontologyService, 
			FieldbookService fieldbookService, String programUUID)
			throws MiddlewareException {
		// add new variables in measurement rows
		if (observations != null && !observations.isEmpty()) {
			for (MeasurementVariable variable : variableList) {
				if (variable.getOperation().equals(Operation.ADD)
						&& !WorkbookUtil.inMeasurementDataList(observations.get(0).getDataList(), variable.getTermId())) {
					StandardVariable stdVariable = ontologyService.getStandardVariable(variable.getTermId(),
							programUUID);
					for (MeasurementRow row : observations) {
						MeasurementData measurementData =
								new MeasurementData(variable.getName(), "", true, WorkbookUtil.getDataType(variable.getDataTypeId()),
										variable);

						measurementData.setPhenotypeId(null);
						int insertIndex = WorkbookUtil.getInsertIndex(row.getDataList(), isVariate);
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

	public static void addMeasurementDataToRows(List<MeasurementVariable> variableList, boolean isVariate, UserSelection userSelection,
			OntologyService ontologyService, FieldbookService fieldbookService, String programUUID) throws MiddlewareException {
		// add new variables in measurement rows
		for (MeasurementVariable variable : variableList) {
			if (variable.getOperation().equals(Operation.ADD)) {
				StandardVariable stdVariable = ontologyService.getStandardVariable(variable.getTermId(),programUUID);
				for (MeasurementRow row : userSelection.getMeasurementRowList()) {
					MeasurementData measurementData =
							new MeasurementData(variable.getName(), "", true, WorkbookUtil.getDataType(variable.getDataTypeId()), variable);

					measurementData.setPhenotypeId(null);
					int insertIndex = WorkbookUtil.getInsertIndex(row.getDataList(), isVariate);
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

	public static void addMeasurementDataToRows(List<MeasurementVariable> variableList, 
			List<MeasurementRow> measurementRowList,
			boolean isVariate, UserSelection userSelection, OntologyService ontologyService, 
			FieldbookService fieldbookService, String programUUID)
			throws MiddlewareException {
		// add new variables in measurement rows
		for (MeasurementVariable variable : variableList) {
			if (variable.getOperation().equals(Operation.ADD)) {
				StandardVariable stdVariable = ontologyService.getStandardVariable(
						variable.getTermId(),programUUID);
				for (MeasurementRow row : measurementRowList) {
					MeasurementData measurementData =
							new MeasurementData(variable.getName(), "", true, WorkbookUtil.getDataType(variable.getDataTypeId()), variable);

					measurementData.setPhenotypeId(null);
					int insertIndex = WorkbookUtil.getInsertIndex(row.getDataList(), isVariate);
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

	/**
	 * Gets the data type.
	 *
	 * @param dataTypeId the data type id
	 * @return the data type
	 */
	private static String getDataType(int dataTypeId) {
		// datatype ids: 1120, 1125, 1128, 1130
		if (dataTypeId == TermId.CHARACTER_VARIABLE.getId() || dataTypeId == TermId.TIMESTAMP_VARIABLE.getId()
				|| dataTypeId == TermId.CHARACTER_DBID_VARIABLE.getId() || dataTypeId == TermId.CATEGORICAL_VARIABLE.getId()) {
			return "C";
		} else {
			return "N";
		}
	}

	private static int getInsertIndex(List<MeasurementData> dataList, boolean isVariate) {
		int index = -1;
		if (dataList != null) {
			if (!isVariate) {
				for (MeasurementData data : dataList) {
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
	private static List<ValueReference> transformPossibleValues(List<Enumeration> enumerations) {
		List<ValueReference> list = new ArrayList<ValueReference>();

		if (enumerations != null) {
			for (Enumeration enumeration : enumerations) {
				list.add(new ValueReference(enumeration.getId(), enumeration.getName(), enumeration.getDescription()));
			}
		}

		return list;
	}

	public static void manageExpDesignVariablesAndObs(Workbook workbook, Workbook tempWorkbook) {
		// edit original factors to add/delete new/deleted variables based on tempWorkbook.getFactors
		// create map of factors in tempWorkbook and factors in workbook
		Map<Integer, MeasurementVariable> tempFactorsMap = new HashMap<Integer, MeasurementVariable>();
		Map<Integer, MeasurementVariable> factorsMap = new HashMap<Integer, MeasurementVariable>();
		Map<Integer, StandardVariable> expDesignVariablesMap = new HashMap<Integer, StandardVariable>();

		if (tempWorkbook.getFactors() != null) {
			for (MeasurementVariable var : tempWorkbook.getFactors()) {
				tempFactorsMap.put(Integer.valueOf(var.getTermId()), var);
			}
		}

		if (workbook.getFactors() != null) {
			for (MeasurementVariable var : workbook.getFactors()) {
				factorsMap.put(Integer.valueOf(var.getTermId()), var);
			}
		}

		if (tempWorkbook.getExpDesignVariables() != null) {
			for (StandardVariable var : tempWorkbook.getExpDesignVariables()) {
				expDesignVariablesMap.put(Integer.valueOf(var.getId()), var);
			}
		}

		for (MeasurementVariable var : tempWorkbook.getFactors()) {
			if (factorsMap.get(Integer.valueOf(var.getTermId())) == null
					&& expDesignVariablesMap.get(Integer.valueOf(var.getTermId())) != null) {
				var.setOperation(Operation.ADD);
				workbook.getFactors().add(var);
			}
		}

		for (MeasurementVariable var : workbook.getFactors()) {
			if (tempFactorsMap.get(Integer.valueOf(var.getTermId())) == null && var.getOperation().equals(Operation.UPDATE)) {
				var.setOperation(Operation.DELETE);
			}
		}

		// copy observations generated from experimental design
		workbook.setObservations(tempWorkbook.getObservations());
	}

	public static Map<Integer, MeasurementVariable> createVariableList(List<MeasurementVariable> factors, List<MeasurementVariable> variates) {
		Map<Integer, MeasurementVariable> observationVariables = new HashMap<Integer, MeasurementVariable>();
		if (factors != null) {
			for (MeasurementVariable var : factors) {
				observationVariables.put(Integer.valueOf(var.getTermId()), var);
			}
		}
		if (variates != null) {
			for (MeasurementVariable var : variates) {
				observationVariables.put(Integer.valueOf(var.getTermId()), var);
			}
		}
		return observationVariables;
	}

	public static void deleteDeletedVariablesInObservations(Map<Integer, MeasurementVariable> measurementDatasetVariables,
			List<MeasurementRow> observations) {

		List<Integer> deletedList = new ArrayList<Integer>();
		if (observations != null && !observations.isEmpty()) {
			for (MeasurementData data : observations.get(0).getDataList()) {
				if (measurementDatasetVariables.get(Integer.valueOf(data.getMeasurementVariable().getTermId())) == null
						&& data.getMeasurementVariable().getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					deletedList.add(Integer.valueOf(data.getMeasurementVariable().getTermId()));
				}
			}
		}
		if (deletedList != null) {
			for (Integer termId : deletedList) {
				// remove from measurement rows
				int index = 0;
				int varIndex = 0;
				boolean found = false;
				if (observations != null) {
					for (MeasurementRow row : observations) {
						if (index == 0) {
							for (MeasurementData var : row.getDataList()) {
								if (var.getMeasurementVariable().getTermId() == termId.intValue()) {
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
	}

	// we would validate all conditions except for name and the study type
	public static boolean isConditionValidate(Integer cvTermId) {
		if (cvTermId != null && cvTermId.intValue() != TermId.STUDY_TYPE.getId() && cvTermId.intValue() != TermId.STUDY_NAME.getId()
				&& !AppConstants.HIDE_TRIAL_VARIABLE_SETTINGS_FIELDS.getString().contains(cvTermId.toString())) {
			return true;
		}
		return false;
	}

	public static List<MeasurementVariable> getExperimentalDesignVariables(List<MeasurementVariable> conditions) {
		List<MeasurementVariable> expDesignVariables = new ArrayList<MeasurementVariable>();
		if (conditions != null && !conditions.isEmpty()) {
			List<Integer> expDesignConstants = AppConstants.EXP_DESIGN_VARIABLES.getIntegerList();
			for (MeasurementVariable condition : conditions) {
				if (expDesignConstants.contains(condition.getTermId())) {
					expDesignVariables.add(condition);
				}
			}
		}
		return expDesignVariables;
	}

	public static void updateTrialObservations(Workbook workbook, Workbook temporaryWorkbook) {
		if (temporaryWorkbook.getTrialObservations() != null) {
			workbook.setTrialObservations(temporaryWorkbook.getTrialObservations());
		}
	}
}
