
package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.List;

@Component
public class SelectionTraitExpressionDataProcessor implements ExpressionDataProcessor {

	public static final String SELECTION_TRAIT_PROPERTY = "Selection Criteria";
	public static final String ALTERNATE_CATEGORICAL_TYPE_VALUE = "C";

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public void processEnvironmentLevelData(final AdvancingSource source, final Workbook workbook, final AdvancingNursery nurseryInfo,
			final Study study) {
		for (final MeasurementVariable condition : workbook.getConditions()) {
			if (condition.getProperty().equalsIgnoreCase(SELECTION_TRAIT_PROPERTY)) {
				source.setSelectionTraitValue(extractValue(condition.getValue(), condition.getTermId()));
			}
		}
	}

	@Override
	public void processPlotLevelData(final AdvancingSource source, final MeasurementRow row) {
		final List<MeasurementData> rowData = row.getDataList();

		for (final MeasurementData measurementData : rowData) {
			if (measurementData.getMeasurementVariable().getProperty().equalsIgnoreCase(SELECTION_TRAIT_PROPERTY)) {
				source.setSelectionTraitValue(extractValue(measurementData.getValue(), measurementData.getMeasurementVariable().getTermId()));
			}
		}
	}

	protected String extractValue(final String value, final Integer variableTermID) {
		if (!StringUtils.isNumeric(value)) {
			// this case happens when a character value is provided as an out of bounds value
			return value;
		}

		final String categoricalValue =
				this.ontologyVariableDataManager.retrieveVariableCategoricalValue(contextUtil.getCurrentProgramUUID(), variableTermID,
						Integer.parseInt(value));

		if (categoricalValue == null) {
			// this case happens when a numeric value is provided as an out of bounds value
			return value;
		} else {
			return categoricalValue;
		}
	}
}