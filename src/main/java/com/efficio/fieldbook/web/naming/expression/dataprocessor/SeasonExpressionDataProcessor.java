
package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@Component
public class SeasonExpressionDataProcessor implements ExpressionDataProcessor {

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private ContextUtil contextUtil;
	
	@Override
	public void processEnvironmentLevelData(final AdvancingSource source, final Workbook workbook, final AdvancingNursery nurseryInfo,
			final Study study) {
		final Map<Integer, String> measurementVariablesValues = new HashMap<Integer, String>();
		for (final MeasurementVariable mv : workbook.getConditions()) {
			this.addValueToMeasurementVariablesValues(mv.getValue(), mv.getPossibleValues(), mv.getTermId(), measurementVariablesValues);
		}
		source.setSeason(this.getValueOfPrioritySeasonVariable(measurementVariablesValues));
	}

	@Override
	public void processPlotLevelData(final AdvancingSource source, final MeasurementRow row) {
		if (source.getStudyType().equals(StudyType.T) && StringUtils.isBlank(source.getSeason())
				&& source.getTrailInstanceObservation() != null && source.getTrailInstanceObservation().getDataList() != null) {
			final Map<Integer, String> measurementVariablesValues = new HashMap<Integer, String>();
			for (final MeasurementData measurementData : source.getTrailInstanceObservation().getDataList()) {
				final int termId = measurementData.getMeasurementVariable().getTermId();
				final List<ValueReference> possibleValues = measurementData.getMeasurementVariable().getPossibleValues();
				this.addValueToMeasurementVariablesValues(measurementData.getValue(), possibleValues, termId, measurementVariablesValues);
			}
			source.setSeason(this.getValueOfPrioritySeasonVariable(measurementVariablesValues));
		}
	}

	private String getValueOfPrioritySeasonVariable(final Map<Integer, String> measurementVariablesValues) {
		String season = "";
		if (measurementVariablesValues.get(TermId.SEASON_MONTH.getId()) != null) {
			season = measurementVariablesValues.get(TermId.SEASON_MONTH.getId());
		} else if (measurementVariablesValues.get(TermId.SEASON_VAR_TEXT.getId()) != null) {
			season = measurementVariablesValues.get(TermId.SEASON_VAR_TEXT.getId());
		} else if (measurementVariablesValues.get(TermId.SEASON_VAR.getId()) != null) {
			season = measurementVariablesValues.get(TermId.SEASON_VAR.getId());
		}
		return season;
	}
	
	private String getSeasonVarValue(String value, List<ValueReference> possibleValues) {
		for(ValueReference valueReference: possibleValues){
			if(valueReference.getId() == Integer.parseInt(value)){
				return valueReference.getDescription();
			}
		}
		//default
		return value;
	}
	
	private void addValueToMeasurementVariablesValues(String value, List<ValueReference> possibleValues, int termId, Map<Integer, String> measurementVariablesValues) {
		if (StringUtils.isNotBlank(value)) {
			if(termId == TermId.SEASON_VAR.getId() && StringUtils.isNumeric(value) && !possibleValues.isEmpty()){
				String seasonVarValue = this.getSeasonVarValue(value, possibleValues);
				measurementVariablesValues.put(termId, seasonVarValue);
			} else {
				measurementVariablesValues.put(termId, value);
			}
		}
	}
}
