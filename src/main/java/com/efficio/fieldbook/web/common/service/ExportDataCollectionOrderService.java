
package com.efficio.fieldbook.web.common.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;

public abstract class ExportDataCollectionOrderService {

	public abstract void reorderWorkbook(Workbook workbook);

	protected Map<String, MeasurementRow> getFieldMapExperimentsMap(List<MeasurementRow> observations) {
		Map<String, MeasurementRow> fieldmapExperiments = new HashMap();
		for (MeasurementRow experiments : observations) {
			Integer range = experiments.getRange();
			Integer column = experiments.getColumn();
			if (range != null && column != null) {
				String key = column.toString() + ":" + range.toString();
				fieldmapExperiments.put(key, experiments);
			}

		}
		return fieldmapExperiments;
	}
}
