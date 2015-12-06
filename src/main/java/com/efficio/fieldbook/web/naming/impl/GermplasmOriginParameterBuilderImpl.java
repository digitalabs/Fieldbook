package com.efficio.fieldbook.web.naming.impl;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.commons.service.GermplasmOriginGenerationService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.naming.service.GermplasmOriginParameterBuilder;

@Service
public class GermplasmOriginParameterBuilderImpl implements GermplasmOriginParameterBuilder {

	@Resource
	private GermplasmOriginGenerationService germplasmOriginGenerationService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Override
	public GermplasmOriginGenerationParameters build(Workbook workbook, String plotNumber) throws FieldbookException {
		final GermplasmOriginGenerationParameters originGenerationParameters = new GermplasmOriginGenerationParameters();
		originGenerationParameters.setCrop(this.contextUtil.getProjectInContext().getCropType().getCropName());
		originGenerationParameters.setStudyName(workbook.getStudyName());
		originGenerationParameters.setStudyType(workbook.getStudyDetails().getStudyType());

		// Origin string generation logic when advancing *requires* LOCATION_ABBR(8189) variable to be present in general settings.
		MeasurementVariable locationAbbrVariable = workbook.findConditionById(TermId.LOCATION_ABBR.getId());
		if (locationAbbrVariable == null) {
			throw new FieldbookException("nursery.advance.no.location.abbr.variable.setup");
		}
		if (StringUtils.isBlank(locationAbbrVariable.getValue())) {
			throw new FieldbookException("nursery.advance.no.location.abbr.value.set");
		}
		originGenerationParameters.setLocation(locationAbbrVariable.getValue());

		// Origin string generation logic when advancing *requires* Crop_season_Code(8371) variable to be present in general settings.
		MeasurementVariable seasonVariable = workbook.findConditionById(TermId.SEASON_VAR.getId());
		if (seasonVariable == null) {
			throw new FieldbookException("nursery.advance.no.season.variable.setup");
		}
		String season = "";
		if (StringUtils.isBlank(seasonVariable.getValue())) {
			throw new FieldbookException("nursery.advance.no.season.value.set");
		}
		Variable variable =
				this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), seasonVariable.getTermId(), true,
						false);
		for (TermSummary ts : variable.getScale().getCategories()) {
			if (ts.getId().equals(Integer.valueOf(seasonVariable.getValue()))) {
				season = ts.getDefinition();
				break;
			}
		}
		originGenerationParameters.setSeason(season);
		originGenerationParameters.setPlotNumber(plotNumber);
		return originGenerationParameters;
	}

}
