
package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@Component
public class SeasonExpressionDataProcessor implements ExpressionDataProcessor {

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingNursery nurseryInfo, Study study)
			throws FieldbookException {
		source.setSeason(getSeason(workbook));
	}

	@Override
	public void processPlotLevelData(AdvancingSource source, MeasurementRow row) {
		// no implementation, SeasonExpression does not need plot level data
	}

	String getSeason(Workbook workbook) throws FieldbookException {
		String season = "";
		if (workbook.getStudyDetails().getStudyType() == StudyType.N) {
			for (MeasurementVariable mv : workbook.getConditions()) {
				if (mv.getTermId() == TermId.SEASON.getId()) {
					season = mv.getValue();
				} else if (mv.getTermId() == TermId.SEASON_DRY.getId()) {
					season = mv.getValue();
				} else if (mv.getTermId() == TermId.SEASON_MONTH.getId()) {
					season = mv.getValue();
				} else if (mv.getTermId() == TermId.SEASON_VAR.getId()) {
					// categorical variable - the value returned is the key to another term
					if (mv.getValue().equals("")) {
						// the user has failed to choose a season from the available choices
						throw new FieldbookException("nursery.advance.no.code.selected.for.season");
					}
					// ambulance at the base of the cliff - we do not know if the season will be the numeric
					// category code, or the text category description, so we will be safe here
					if (StringUtils.isNumeric(mv.getValue())) {
						// season is the numeric code referring to the category
						season =
								this.ontologyVariableDataManager.retrieveVariableCategoricalValue(contextUtil.getCurrentProgramUUID(),
										mv.getTermId(), Integer.parseInt(mv.getValue()));
					} else {
						// season captured is the description
						season = mv.getValue();
					}
				} else if (mv.getTermId() == TermId.SEASON_VAR_TEXT.getId()) {
					season = mv.getValue();
				} else if (mv.getTermId() == TermId.SEASON_WET.getId()) {
					season = mv.getValue();
				}
			}
		}
		return season;
	}
}
