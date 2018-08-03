package com.efficio.fieldbook.web.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.middleware.domain.etl.CategoricalDisplayValue;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;

public class DataMapUtil {

	public static final String EXPERIMENT_ID = "experimentId";

	public static final String DESIGNATION = "DESIGNATION";

	public static final String GID = "GID";


	public Map<String, Object> generateDatatableDataMap(final MeasurementRow row, String suffix, final UserSelection userSelection) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		// the 3 attributes are needed always
		dataMap.put(DataMapUtil.EXPERIMENT_ID, Integer.toString(row.getExperimentId()));

		// initialize suffix as empty string if its null
		suffix = null == suffix ? "" : suffix;

		// generate measurement row data from dataList (existing / generated data)
		for (final MeasurementData data : row.getDataList()) {
			if (data.isCategorical()) {
				final CategoricalDisplayValue categoricalDisplayValue = data.getDisplayValueForCategoricalData();

				dataMap.put(data.getMeasurementVariable().getName(), new Object[] {categoricalDisplayValue.getName() + suffix,
						categoricalDisplayValue.getDescription() + suffix, data.isAccepted(), (data.getValueStatus() != null) ? data.getValueStatus().toString() : null });

			} else if (data.isNumeric()) {
				dataMap.put(data.getMeasurementVariable().getName(), new Object[] {data.getDisplayValue() + suffix, data.isAccepted(), (data.getValueStatus() != null) ? data.getValueStatus().toString() : null });
			} else {
				dataMap.put(data.getMeasurementVariable().getName(), new Object[] {data.getDisplayValue() != null ? data.getDisplayValue() : "",
						data.getPhenotypeId() != null ? data.getPhenotypeId() : "", (data.getValueStatus() != null) ? data.getValueStatus().toString() : null });
			}
		}

		dataMap.put(DataMapUtil.DESIGNATION, row.getMeasurementDataValue(TermId.DESIG.getId()));
		dataMap.put(DataMapUtil.GID, row.getMeasurementDataValue(TermId.GID.getId()));

		// generate measurement row data from newly added traits (no data yet)
		if (userSelection != null && userSelection.getMeasurementDatasetVariable() != null
				&& !userSelection.getMeasurementDatasetVariable().isEmpty()) {
			for (final MeasurementVariable var : userSelection.getMeasurementDatasetVariable()) {
				if (!dataMap.containsKey(var.getName())) {
					if (var.getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())) {
						dataMap.put(var.getName(), new Object[] {"", "", true, null});
					} else {
						dataMap.put(var.getName(), "");
					}
				}
			}
		}
		return dataMap;
	}

	public Map<String, Object> generateDatatableDataMap(final ObservationDto row, final Map<String, String> nameToAliasMap,
			final UserSelection userSelection, final OntologyVariableDataManager ontologyVariableDataManager, final String programUUID) {
		final Map<String, Object> dataMap = new HashMap<>();
		// the 4 attributes are needed always
		dataMap.put("Action", Integer.toString(row.getMeasurementId()));
		dataMap.put(DataMapUtil.EXPERIMENT_ID, Integer.toString(row.getMeasurementId()));
		// We always need to return GID and DESIGNATION as keys as they are
		// expected for tooltip in table
		dataMap.put(DataMapUtil.GID, row.getGid());
		dataMap.put(DataMapUtil.DESIGNATION, row.getDesignation());

		dataMap.put(String.valueOf(TermId.SAMPLES.getId()), new Object[] { row.getSamples(), row.getPlotId() });

		final List<MeasurementVariable> measurementDatasetVariables = new ArrayList<>();
		measurementDatasetVariables.addAll(userSelection.getWorkbook().getMeasurementDatasetVariablesView());

		// generate measurement row data from dataList (existing / generated
		// data)
		for (final MeasurementDto data : row.getVariableMeasurements()) {

			final Integer variableId = data.getMeasurementVariable().getId();
			final Variable variable = ontologyVariableDataManager
					.getVariable(programUUID, variableId, true, false);
			final MeasurementVariable measurementVariable = WorkbookUtil
					.getMeasurementVariable(measurementDatasetVariables, variableId);

			// measurementVariable could be null if the trait was deleted
			if (measurementVariable != null) {
				if (variable.getScale().getDataType().equals(DataType.CATEGORICAL_VARIABLE)) {

					dataMap.put(measurementVariable.getName(), this.convertForCategoricalVariable(variable, data.getVariableValue(),
							data.getPhenotypeId(), false, (data.getValueStatus() != null) ? data.getValueStatus().toString() : null));

				} else if (variable.getScale().getDataType().equals(DataType.NUMERIC_VARIABLE)) {
					dataMap.put(measurementVariable.getName(),
							new Object[] { data.getVariableValue() != null ? data.getVariableValue() : "", true,
									data.getPhenotypeId() != null ? data.getPhenotypeId() : "", (data.getValueStatus() != null) ? data.getValueStatus().toString() : null });
				} else {
					dataMap.put(measurementVariable.getName(),
							new Object[] { data.getVariableValue() != null ? data.getVariableValue() : "",
									data.getPhenotypeId() != null ? data.getPhenotypeId() : "", (data.getValueStatus() != null) ? data.getValueStatus().toString() : null  });
				}
			}
		}

		// generate measurement row data for standard factors like
		// TRIAL_INSTANCE, ENTRY_NO, ENTRY_TYPE, PLOT_NO, PLOT_ID, etc
		this.addGermplasmAndPlotFactorsDataToDataMap(row, dataMap, measurementDatasetVariables, nameToAliasMap, ontologyVariableDataManager,
				programUUID);

		// generate measurement row data from newly added traits (no data yet)
		if (userSelection != null && userSelection.getMeasurementDatasetVariable() != null
				&& !userSelection.getMeasurementDatasetVariable().isEmpty()) {
			for (final MeasurementVariable var : userSelection.getMeasurementDatasetVariable()) {
				if (!dataMap.containsKey(var.getName())) {
					if (var.getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())) {
						dataMap.put(var.getName(), new Object[] { "", "", true, null});
					} else {
						dataMap.put(var.getName(), "");
					}
				}
			}
		}
		return dataMap;
	}

	/*
	 * 1. Generate measurement row data for standard factors like
	 * TRIAL_INSTANCE, ENTRY_NO, ENTRY_TYPE, PLOT_NO, REP_NO, BLOCK_NO, ROW,
	 * COL, PLOT_ID and add to dataMap. 2. Also adds additonal germplasm
	 * descriptors (eg. StockID) to dataMap 3. If local variable name for GID
	 * and DESIGNATION are not equal to "GID" and "DESIGNATION" respectively,
	 * add them to map as well
	 *
	 * Use the local name of the variable as key and the value of the variable
	 * as value in dataMap.
 	*/
	public void addGermplasmAndPlotFactorsDataToDataMap(final ObservationDto row, final Map<String, Object> dataMap,
			final List<MeasurementVariable> measurementDatasetVariables, final Map<String, String> nameToAliasMap,
			final OntologyVariableDataManager ontologyVariableDataManager, final String programUUID) {
		final MeasurementVariable gidVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.GID.getId());

		// Add local variable names of GID and DESIGNATiON variables if they are
		// not equal to "GID" and "DESIGNATION"
		// "GID" and "DESIGNATION" are assumed to be added beforehand to dataMap
		if (gidVar != null && !DataMapUtil.GID.equals(gidVar.getName())) {
			dataMap.put(gidVar.getName(), row.getGid());
		}
		final MeasurementVariable desigVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.DESIG.getId());
		if (desigVar != null && !DataMapUtil.DESIGNATION.equals(desigVar.getName())) {
			dataMap.put(desigVar.getName(), row.getDesignation());
		}

		final MeasurementVariable entryNoVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.ENTRY_NO.getId());
		if (entryNoVar != null) {
			dataMap.put(entryNoVar.getName(), new Object[] {row.getEntryNo(), false});
		}

		final MeasurementVariable entryCodeVar =
				WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.ENTRY_CODE.getId());
		if (entryCodeVar != null) {
			dataMap.put(entryCodeVar.getName(), new Object[] {row.getEntryCode(), false});
		}

		final MeasurementVariable entryTypeVar =
				WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.ENTRY_TYPE.getId());
		if (entryTypeVar != null) {
			dataMap.put(entryTypeVar.getName(), new Object[] {row.getEntryType(), row.getEntryType(), false});
		}

		final MeasurementVariable plotNoVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.PLOT_NO.getId());
		if (plotNoVar != null) {
			dataMap.put(plotNoVar.getName(), new Object[] {row.getPlotNumber(), false});
		}

		final MeasurementVariable repNoVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.REP_NO.getId());
		if (repNoVar != null) {
			dataMap.put(repNoVar.getName(), new Object[] {row.getRepitionNumber(), false});
		}

		final MeasurementVariable blockNoVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.BLOCK_NO.getId());
		if (blockNoVar != null) {
			dataMap.put(blockNoVar.getName(), new Object[] {row.getBlockNumber(), false});
		}

		final MeasurementVariable rowVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.ROW.getId());
		if (rowVar != null) {
			dataMap.put(rowVar.getName(), new Object[] {row.getRowNumber(), false});
		}

		final MeasurementVariable colVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.COL.getId());
		if (colVar != null) {
			dataMap.put(colVar.getName(), new Object[] {row.getColumnNumber(), false});
		}

		final MeasurementVariable trialInstanceVar =
				WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.TRIAL_INSTANCE_FACTOR.getId());
		if (trialInstanceVar != null) {
			dataMap.put(trialInstanceVar.getName(), new Object[] {row.getTrialInstance(), false});
		}

		final MeasurementVariable plotIdVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.PLOT_ID.getId());
		if (plotIdVar != null) {
			dataMap.put(plotIdVar.getName(), new Object[] {row.getPlotId(), false});
		}

		final MeasurementVariable fieldMapcolumVar =
				WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.FIELDMAP_COLUMN.getId());
		if (fieldMapcolumVar != null) {
			dataMap.put(fieldMapcolumVar.getName(), new Object[] {row.getFieldMapColumn(), false});
		}

		final MeasurementVariable fieldMapRangevar =
				WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.FIELDMAP_RANGE.getId());
		if (fieldMapRangevar != null) {
			dataMap.put(fieldMapRangevar.getName(), new Object[] {row.getFieldMapRange(), false});
		}

		for (final Pair<String, String> additionalGermplasmAttrCols : row.getAdditionalGermplasmDescriptors()) {
			final String alias = nameToAliasMap.get(additionalGermplasmAttrCols.getLeft()) != null ?
					nameToAliasMap.get(additionalGermplasmAttrCols.getLeft()) :
					additionalGermplasmAttrCols.getLeft();
			dataMap.put(alias, new Object[] {additionalGermplasmAttrCols.getRight()});
		}

		for (final Pair<String, String> additionalDesignCols : row.getAdditionalDesignFactors()) {
			final String alias = nameToAliasMap.get(additionalDesignCols.getLeft()) != null ?
					nameToAliasMap.get(additionalDesignCols.getLeft()) :
					additionalDesignCols.getLeft();
			final Optional<MeasurementVariable> columnVariable =
					WorkbookUtil.findMeasurementVariableByName(measurementDatasetVariables, alias);
			if (columnVariable.isPresent()) {
				final Variable variable =
						ontologyVariableDataManager.getVariable(programUUID, columnVariable.get().getTermId(), true, false);

				if (variable.getScale().getDataType().getId() == TermId.CATEGORICAL_VARIABLE.getId()) {
					dataMap.put(alias, this.convertForCategoricalVariable(variable, additionalDesignCols.getRight(), null, true, null));
				} else {
					dataMap.put(alias, new Object[] {additionalDesignCols.getRight()});
				}

			}

		}
	}

	public Object[] convertForCategoricalVariable(final Variable variable, final String variableValue, final Integer phenotypeId,
			final boolean isFactor, final String valueStatus) {

		if (StringUtils.isBlank(variableValue)) {
			return new Object[] {"", "", false, phenotypeId != null ? phenotypeId : "", null};
		} else {
			boolean isCategoricalValueFound = false;
			String catName = "";
			String catDisplayValue = "";

			// Find the categorical value (possible value) of the measurement
			// data, so we can get its name and definition.
			for (final TermSummary category : variable.getScale().getCategories()) {

				final String compareValue = isFactor ? String.valueOf(category.getId()) : category.getName();

				if (compareValue.equalsIgnoreCase(variableValue)) {
					catName = category.getName();
					catDisplayValue = category.getDefinition();
					isCategoricalValueFound = true;
					break;
				}
			}

			// If the measurement value is out of range from categorical values,
			// then the assumption is, it is custom value.
			// For this case, just display the measurement data as is.
			if (!isCategoricalValueFound) {
				catName = variableValue;
				catDisplayValue = variableValue;
			}

			return new Object[] {catName, catDisplayValue, true, phenotypeId != null ? phenotypeId : "", valueStatus};
		}

	}

}
