
package com.efficio.fieldbook.web.naming.impl;

import java.text.SimpleDateFormat;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.naming.service.GermplasmOriginParameterBuilder;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@Service
public class GermplasmOriginParameterBuilderImpl implements GermplasmOriginParameterBuilder {

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmOriginParameterBuilderImpl.class);

	@Override
	public GermplasmOriginGenerationParameters build(Workbook workbook, AdvancingSource advancingSource, String selectionNumber) {
		final GermplasmOriginGenerationParameters originGenerationParameters = new GermplasmOriginGenerationParameters();
		originGenerationParameters.setCrop(this.contextUtil.getProjectInContext().getCropType().getCropName());
		originGenerationParameters.setStudyName(workbook.getStudyName());
		originGenerationParameters.setStudyType(workbook.getStudyDetails().getStudyType());
		deriveLocation(workbook, originGenerationParameters, advancingSource.getTrialInstanceNumber());
		deriveSeason(workbook, originGenerationParameters);
		originGenerationParameters.setPlotNumber(advancingSource.getPlotNumber());
		originGenerationParameters.setSelectionNumber(selectionNumber);
		return originGenerationParameters;
	}

	private void deriveSeason(Workbook workbook, final GermplasmOriginGenerationParameters originGenerationParameters) {

		// To populate SEASON placeholder we look for Crop_season_Code(8371) variable in general settings.
		MeasurementVariable seasonVariable = workbook.findConditionById(TermId.SEASON_VAR.getId());
		if (seasonVariable != null && StringUtils.isNotBlank(seasonVariable.getValue())) {
			Variable variable =
					this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), seasonVariable.getTermId(), true,
							false);
			for (TermSummary seasonOption : variable.getScale().getCategories()) {
				// Sometimes the categorical value of season in Workbook is an ID string, sometimes the actual Value/Definition string.
				// Right now, only the super natural elements in the Workbook and Fieldbook universe know why.
				// So we deal with it anyway.
				if (seasonVariable.getValue().equals(seasonOption.getId().toString()) 
						|| seasonVariable.getValue().equals(seasonOption.getDefinition())) {
					originGenerationParameters.setSeason(seasonOption.getDefinition());
					break;
				}
			}
		} else {
			// Default the season to current year and month.
			SimpleDateFormat formatter = new SimpleDateFormat("YYYYMM");
			String currentYearAndMonth = formatter.format(new java.util.Date());
			LOG.debug("No Crop_season_Code(8371) variable or if present a value, was found in study: {}. Defaulting [SEASON] with: {}.",
					workbook.getStudyDetails().getStudyName(), currentYearAndMonth);
			originGenerationParameters.setSeason(currentYearAndMonth);
		}
	}

	void deriveLocation(Workbook workbook, final GermplasmOriginGenerationParameters originGenerationParameters,
			String trialInstanceNumber) {
		if (workbook.getStudyDetails().getStudyType() == StudyType.N) {
			// For Nurseris, to populate LOCATION placeholder we look for LOCATION_ABBR(8189) variable in general settings.
			MeasurementVariable locationAbbrVariable = workbook.findConditionById(TermId.LOCATION_ABBR.getId());
			if (locationAbbrVariable != null) {
				originGenerationParameters.setLocation(locationAbbrVariable.getValue());
			}
		} else if (workbook.getStudyDetails().getStudyType() == StudyType.T) {
			// For trials, we look for LOCATION_ABBR(8189) variable at trial instance/environment level.
			MeasurementRow trialInstanceObservations = workbook.getTrialObservationByTrialInstanceNo(Integer.valueOf(trialInstanceNumber));
			if (trialInstanceObservations != null) {
				for (MeasurementData trialInstanceMeasurement : trialInstanceObservations.getDataList()) {
					if (trialInstanceMeasurement.getMeasurementVariable().getTermId() == TermId.LOCATION_ABBR.getId()) {
						originGenerationParameters.setLocation(trialInstanceMeasurement.getValue());
						break;
					}
				}
			}
		}

		if (originGenerationParameters.getLocation() == null) {
			LOG.debug(
					"No LOCATION_ABBR(8189) variable was found or it is present but no value is set, in study: {}. [LOCATION] will be defaulted to be null/empty.",
					workbook.getStudyDetails().getStudyName());
		}
	}

	@Override
	public GermplasmOriginGenerationParameters build(Workbook workbook, ImportedCrosses cross) {
		final GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		parameters.setCrop(this.contextUtil.getProjectInContext().getCropType().getCropName());
		parameters.setStudyName(workbook.getStudyName());
		parameters.setStudyType(workbook.getStudyDetails().getStudyType());
		// Cross scenario is currently only for Nurseries, hard coding instance number to 1 is fine until that is not the case.
		deriveLocation(workbook, parameters, "1");
		deriveSeason(workbook, parameters);
		parameters.setMaleStudyName(cross.getMaleStudyName());
		parameters.setFemaleStudyName(cross.getFemaleStudyName());
		parameters.setMalePlotNumber(cross.getMalePlotNo());
		parameters.setFemalePlotNumber(cross.getFemalePlotNo());
		parameters.setCross(true);
		return parameters;
	}

}
