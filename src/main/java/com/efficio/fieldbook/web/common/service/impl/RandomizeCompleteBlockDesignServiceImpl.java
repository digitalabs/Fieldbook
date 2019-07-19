package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.ExperimentDesignService;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class RandomizeCompleteBlockDesignServiceImpl implements RandomizeCompleteBlockDesignService {

	private static final Logger LOG = LoggerFactory.getLogger(RandomizeCompleteBlockDesignServiceImpl.class);

	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	protected FieldbookProperties fieldbookProperties;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	public FieldbookService fieldbookService;

	@Resource
	private UserSelection userSelection;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	public ExperimentDesignGenerator experimentDesignGenerator;

	@Override
	public List<MeasurementRow> generateDesign(final List<ImportedGermplasm> germplasmList, final ExpDesignParameterUi parameter,
			final List<MeasurementVariable> trialVariables, final List<MeasurementVariable> factors, final List<MeasurementVariable> nonTrialFactors,
			final List<MeasurementVariable> variates, final List<TreatmentVariable> treatmentVariables) throws BVDesignException {

		List<MeasurementRow> measurementRowList = new ArrayList<>();
		final String block = parameter.getReplicationsCount();
		final int environments = Integer.valueOf(parameter.getNoOfEnvironments());
		final int environmentsToAdd = Integer.valueOf(parameter.getNoOfEnvironmentsToAdd());

		try {

			final List<String> treatmentFactors = new ArrayList<>();
			final List<String> levels = new ArrayList<>();

			// Key - CVTerm ID , List of values
			final Map<String, List<String>> treatmentFactorValues = new HashMap<String, List<String>>();
			final Map treatmentFactorsData = parameter.getTreatmentFactorsData();

			final List<SettingDetail> treatmentFactorList = this.userSelection.getTreatmentFactors();

			if (treatmentFactorsData != null) {
				final Iterator keySetIter = treatmentFactorsData.keySet().iterator();
				while (keySetIter.hasNext()) {
					final String key = (String) keySetIter.next();
					final Map treatmentData = (Map) treatmentFactorsData.get(key);
					treatmentFactorValues.put(key, (List) treatmentData.get("labels"));
					// add the treatment variables
					final Object pairVarObj = treatmentData.get("variableId");

					String pairVar = "";
					if (pairVarObj instanceof String) {
						pairVar = (String) pairVarObj;
					} else {
						pairVar = pairVarObj.toString();
					}
					if (key != null && NumberUtils.isNumber(key) && pairVar != null && NumberUtils.isNumber(pairVar)) {
						final int treatmentPair1 = Integer.parseInt(key);
						final int treatmentPair2 = Integer.parseInt(pairVar);
						final StandardVariable stdVar1 =
								this.fieldbookMiddlewareService.getStandardVariable(treatmentPair1, this.contextUtil.getCurrentProgramUUID());
						stdVar1.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
						final StandardVariable stdVar2 =
								this.fieldbookMiddlewareService.getStandardVariable(treatmentPair2, this.contextUtil.getCurrentProgramUUID());
						stdVar2.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
						final TreatmentVariable treatmentVar = new TreatmentVariable();
						final MeasurementVariable measureVar1 =
								ExpDesignUtil.convertStandardVariableToMeasurementVariable(stdVar1, Operation.ADD, this.fieldbookService);
						final MeasurementVariable measureVar2 =
								ExpDesignUtil.convertStandardVariableToMeasurementVariable(stdVar2, Operation.ADD, this.fieldbookService);
						measureVar1.setFactor(true);
						measureVar2.setFactor(true);

						SettingsUtil.findAndUpdateVariableName(treatmentFactorList, measureVar1);

						measureVar1.setTreatmentLabel(measureVar1.getName());
						measureVar2.setTreatmentLabel(measureVar1.getName());

						treatmentVar.setLevelVariable(measureVar1);
						treatmentVar.setValueVariable(measureVar2);
						treatmentVariables.add(treatmentVar);
					}

				}
			}

			if (treatmentFactorValues != null) {
				final Set<String> keySet = treatmentFactorValues.keySet();
				for (final String key : keySet) {
					final int level = treatmentFactorValues.get(key).size();
					treatmentFactors.add(ExpDesignUtil.cleanBVDesingKey(key));
					levels.add(Integer.toString(level));
				}
			}

			final StandardVariable stdvarTreatment =
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(), this.contextUtil.getCurrentProgramUUID());

			treatmentFactorValues.put(stdvarTreatment.getName(), Arrays.asList(Integer.toString(germplasmList.size())));
			treatmentFactors.add(stdvarTreatment.getName());
			levels.add(Integer.toString(germplasmList.size()));

			StandardVariable stdvarRep = null;
			StandardVariable stdvarPlot = null;

			final List<StandardVariable> reqVarList = this.getRequiredDesignVariables();

			for (final StandardVariable var : reqVarList) {
				if (var.getId() == TermId.REP_NO.getId()) {
					stdvarRep = var;
				} else if (var.getId() == TermId.PLOT_NO.getId()) {
					stdvarPlot = var;
				}
			}

			final Integer plotNo = StringUtil.parseInt(parameter.getStartingPlotNo(), null);
			final Integer entryNo = StringUtil.parseInt(parameter.getStartingEntryNo(), null);

			final MainDesign mainDesign = this.experimentDesignGenerator
					.createRandomizedCompleteBlockDesign(block, stdvarRep.getName(), stdvarPlot.getName(), plotNo, entryNo, stdvarTreatment.getName(), treatmentFactors,
							levels, "");

			measurementRowList = this.experimentDesignGenerator
					.generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
							variates, treatmentVariables, reqVarList, germplasmList, mainDesign, stdvarTreatment.getName(),
							treatmentFactorValues, new HashMap<Integer, Integer>());

		} catch (final BVDesignException e) {
			throw e;
		} catch (final Exception e) {
			RandomizeCompleteBlockDesignServiceImpl.LOG.error(e.getMessage(), e);
		}

		return measurementRowList;
	}

	@Override
	public List<StandardVariable> getRequiredDesignVariables() {
		final List<StandardVariable> varList = new ArrayList<>();
		try {
			final StandardVariable stdvarRep =
					this.fieldbookMiddlewareService.getStandardVariable(TermId.REP_NO.getId(), this.contextUtil.getCurrentProgramUUID());
			stdvarRep.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
			final StandardVariable stdvarPlot =
					this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), this.contextUtil.getCurrentProgramUUID());
			stdvarPlot.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);

			varList.add(stdvarRep);
			varList.add(stdvarPlot);
		} catch (final MiddlewareException e) {
			RandomizeCompleteBlockDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return varList;
	}

	@Override
	public ExpDesignValidationOutput validate(final ExpDesignParameterUi expDesignParameter, final List<ImportedGermplasm> germplasmList) {
		final Locale locale = LocaleContextHolder.getLocale();
		ExpDesignValidationOutput output = new ExpDesignValidationOutput(true, "");

		if (expDesignParameter == null || germplasmList == null) {
			return output;
		}

		if (!NumberUtils.isNumber(expDesignParameter.getReplicationsCount())) {
			output = new ExpDesignValidationOutput(false,
					this.messageSource.getMessage("experiment.design.replication.count.should.be.a.number", null, locale));
			return output;
		}

		if (expDesignParameter.getStartingPlotNo() != null && !NumberUtils.isNumber(expDesignParameter.getStartingPlotNo())) {
			output = new ExpDesignValidationOutput(false, this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
			return output;
		}
		if (expDesignParameter.getStartingEntryNo() != null && !NumberUtils.isNumber(expDesignParameter.getStartingEntryNo())) {
			output = new ExpDesignValidationOutput(false, this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
			return output;
		}

		final int replicationCount = Integer.valueOf(expDesignParameter.getReplicationsCount());

		if (replicationCount <= 0 || replicationCount >= 13) {
			output = new ExpDesignValidationOutput(false,
					this.messageSource.getMessage("experiment.design.replication.count.rcbd.error", null, locale));
			return output;
		}

		final Integer entryNumber = StringUtil.parseInt(expDesignParameter.getStartingEntryNo(), null);
		final Integer plotNumber = StringUtil.parseInt(expDesignParameter.getStartingPlotNo(), null);
		final Integer germplasmCount = germplasmList.size();
		final Integer maxEntry = germplasmCount + entryNumber - 1;
		final Integer maxPlot = (germplasmCount * replicationCount) + plotNumber - 1;

		if (Objects.equals(entryNumber, 0)) {
			output = new ExpDesignValidationOutput(false, this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
		} else if (Objects.equals(plotNumber, 0)) {
			output = new ExpDesignValidationOutput(false, this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
		} else if (entryNumber != null && maxEntry > ExperimentDesignService.MAX_ENTRY_NO) {
			output = new ExpDesignValidationOutput(false,
				this.messageSource.getMessage("experiment.design.entry.number.should.not.exceed", new Object[] {maxEntry}, locale));
		} else if (entryNumber != null && plotNumber != null && maxPlot > ExperimentDesignService.MAX_PLOT_NO) {
			output = new ExpDesignValidationOutput(false,
				this.messageSource.getMessage("experiment.design.plot.number.should.not.exceed", new Object[] {maxPlot}, locale));
		}

		return output;
	}

	@Override
	public List<Integer> getExperimentalDesignVariables(final ExpDesignParameterUi params) {
		return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId());
	}

	/**
	 * Defines if the experimental design requires breeding view licence to run
	 *
	 * @return
	 */
	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}
}
