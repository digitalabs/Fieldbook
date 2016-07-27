
package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
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
			if (StringUtils.isNotBlank(mv.getValue())) {
				measurementVariablesValues.put(mv.getTermId(), mv.getValue());
			}
		}
		//this method is only called once so we don't need to store the possible values of Season Var
		source.setSeason(this.getValueOfPrioritySeasonVariable(measurementVariablesValues, new HashMap<String, String>()));
	}

	@Override
	public void processPlotLevelData(final AdvancingSource source, final MeasurementRow row, Map<String, String> seasonVarValuesMap) {
		if (source.getStudyType().equals(StudyType.T) && StringUtils.isBlank(source.getSeason())
				&& source.getTrailInstanceObservation() != null && source.getTrailInstanceObservation().getDataList() != null) {
			final Map<Integer, String> measurementVariablesValues = new HashMap<Integer, String>();
			for (final MeasurementData measurementData : source.getTrailInstanceObservation().getDataList()) {
				if (StringUtils.isNotBlank(measurementData.getValue())) {
					measurementVariablesValues.put(measurementData.getMeasurementVariable().getTermId(), measurementData.getValue());
				}
			}
			source.setSeason(this.getValueOfPrioritySeasonVariable(measurementVariablesValues, seasonVarValuesMap));
		}
	}

	private String getValueOfPrioritySeasonVariable(final Map<Integer, String> measurementVariablesValues, Map<String, String> seasonVarValuesMap) {
		String season = "";
		//SEASON_MONTH, SEASON_VAR_TEXT, and SEASON_VAR are the only season variables that can be added to a study.
		if (measurementVariablesValues.get(TermId.SEASON_MONTH.getId()) != null) {
			season = measurementVariablesValues.get(TermId.SEASON_MONTH.getId());
		} else if (measurementVariablesValues.get(TermId.SEASON_VAR_TEXT.getId()) != null) {
			season = measurementVariablesValues.get(TermId.SEASON_VAR_TEXT.getId());
		} else if (measurementVariablesValues.get(TermId.SEASON_VAR.getId()) != null) {
			final String seasonVarValue = measurementVariablesValues.get(TermId.SEASON_VAR.getId());
			if (StringUtils.isNumeric(seasonVarValue)) {
				// season is the numeric code referring to the category
				if(seasonVarValuesMap.get(seasonVarValue) != null){
					season = seasonVarValuesMap.get(seasonVarValue);
				} else {
					season = this.ontologyVariableDataManager.retrieveVariableCategoricalValue(this.contextUtil.getCurrentProgramUUID(),
						TermId.SEASON_VAR.getId(), Integer.parseInt(seasonVarValue));
					seasonVarValuesMap.put(seasonVarValue, season);
				}
			} else {
				// season captured is the description
				season = seasonVarValue;
			}
		}
		return season;
	}
}
