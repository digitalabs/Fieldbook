
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
	public void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingNursery nurseryInfo, Study study) {
		Map<Integer, String> measurementVariablesValues = new HashMap<Integer, String>();
		for (MeasurementVariable mv : workbook.getConditions()) {
			if(StringUtils.isNotBlank(mv.getValue())){
				measurementVariablesValues.put(mv.getTermId(), mv.getValue());
			}
		}
		source.setSeason(this.getValueOfPrioritySeasonVariable(measurementVariablesValues));
	}

	@Override
	public void processPlotLevelData(AdvancingSource source, MeasurementRow row) {
		// no implementation, SeasonExpression does not need plot level data
	}

	private String getValueOfPrioritySeasonVariable(Map<Integer, String> measurementVariablesValues) {
		String season = "";
		if(measurementVariablesValues.get(TermId.SEASON_MONTH.getId()) != null){
			season = measurementVariablesValues.get(TermId.SEASON_MONTH.getId());
		} else if(measurementVariablesValues.get(TermId.SEASON_VAR_TEXT.getId()) != null){
			season = measurementVariablesValues.get(TermId.SEASON_VAR_TEXT.getId());
		} else if(measurementVariablesValues.get(TermId.SEASON_VAR.getId()) != null){
			String seasonVarValue = measurementVariablesValues.get(TermId.SEASON_VAR.getId());
			if (StringUtils.isNumeric(seasonVarValue)) {
				// season is the numeric code referring to the category
				season =
						this.ontologyVariableDataManager.retrieveVariableCategoricalValue(contextUtil.getCurrentProgramUUID(),
								TermId.SEASON_VAR.getId(), Integer.parseInt(seasonVarValue));
			} else {
				// season captured is the description
				season = seasonVarValue;
			}
		}
		return season;
	}
}
