package com.efficio.fieldbook.web.common.util;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.middleware.domain.etl.CategoricalDisplayValue;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.common.bean.UserSelection;

public class DataMapUtil {

	public Map<String, Object> generateDatatableDataMap(final MeasurementRow row, String suffix, final UserSelection userSelection) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		// the 3 attributes are needed always
		dataMap.put("experimentId", Integer.toString(row.getExperimentId()));

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

		dataMap.put("DESIGNATION", row.getMeasurementDataValue(TermId.DESIG.getId()));
		dataMap.put("GID", row.getMeasurementDataValue(TermId.GID.getId()));

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

}
