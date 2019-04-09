package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.PRepDesignService;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.FieldbookProperties;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class PRepDesignServiceImpl implements PRepDesignService {

	private static final Logger LOG = LoggerFactory.getLogger(ResolvableIncompleteBlockDesignServiceImpl.class);
	public static final String EXPERIMENT_DESIGN_REPLICATION_PERCENTAGE_SHOULD_BE_BETWEEN_ZERO_AND_HUNDRED =
		"experiment.design.replication.percentage.should.be.between.zero.and.hundred";
	public static final String EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_NUMBER = "experiment.design.block.size.should.be.a.number";
	public static final String EXPERIMENT_DESIGN_REPLICATION_COUNT_SHOULD_BE_A_NUMBER =
		"experiment.design.replication.count.should.be.a.number";
	public static final String PLOT_NUMBER_SHOULD_BE_IN_RANGE = "plot.number.should.be.in.range";
	public static final String ENTRY_NUMBER_SHOULD_BE_IN_RANGE = "entry.number.should.be.in.range";

	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	protected WorkbenchService workbenchService;

	@Resource
	protected FieldbookProperties fieldbookProperties;

	@Resource
	public FieldbookService fieldbookService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	public ExperimentDesignGenerator experimentDesignGenerator;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Override
	public List<MeasurementRow> generateDesign(
		final List<ImportedGermplasm> germplasmList, final ExpDesignParameterUi parameter, final List<MeasurementVariable> trialVariables,
		final List<MeasurementVariable> factors, final List<MeasurementVariable> nonTrialFactors, final List<MeasurementVariable> variates,
		final List<TreatmentVariable> treatmentVariables) throws BVDesignException {
		List<MeasurementRow> measurementRowList = new ArrayList<>();

		final int nTreatments = germplasmList.size();
		final int blockSize = Integer.parseInt(parameter.getBlockSize());
		final int replicationPercentage = parameter.getReplicationPercentage();
		final int replicationNumber = Integer.parseInt(parameter.getReplicationsCount());
		final int environments = Integer.parseInt(parameter.getNoOfEnvironments());
		final int environmentsToAdd = Integer.parseInt(parameter.getNoOfEnvironmentsToAdd());

		try {

			final StandardVariable stdvarTreatment =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(), this.contextUtil.getCurrentProgramUUID());

			final Map<Integer, StandardVariable> requiredVariablesMap = this.getRequiredDesignVariablesMap();
			final StandardVariable stdvarBlock = requiredVariablesMap.get(TermId.BLOCK_NO.getId());
			final StandardVariable stdvarPlot = requiredVariablesMap.get(TermId.PLOT_NO.getId());

			final Integer plotNo = StringUtil.parseInt(parameter.getStartingPlotNo(), null);
			Integer entryNo = StringUtil.parseInt(parameter.getStartingEntryNo(), null);

			if (!Objects.equals(stdvarTreatment.getId(), TermId.ENTRY_NO.getId())) {
				entryNo = null;
			}

			final List<ListItem> replicationListItems =
				this.experimentDesignGenerator
					.createReplicationListItemForPRepDesign(germplasmList, replicationPercentage, replicationNumber);
			final MainDesign mainDesign = this.experimentDesignGenerator
				.createPRepDesign(blockSize, nTreatments, replicationListItems, stdvarTreatment.getName(),
					stdvarBlock.getName(), stdvarPlot.getName(), plotNo, entryNo);

			measurementRowList = this.experimentDesignGenerator
				.generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
					variates, treatmentVariables, new ArrayList<StandardVariable>(requiredVariablesMap.values()), germplasmList, mainDesign,
					stdvarTreatment.getName(), null,
					new HashMap<Integer, Integer>());

		} catch (final BVDesignException e) {
			throw e;
		} catch (final Exception e) {
			PRepDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return measurementRowList;
	}

	@Override
	public List<StandardVariable> getRequiredDesignVariables() {
		final List<StandardVariable> varList = new ArrayList<>();
		try {
			final StandardVariable stdvarBlock =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), this.contextUtil.getCurrentProgramUUID());
			final StandardVariable stdvarPlot =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), this.contextUtil.getCurrentProgramUUID());

			stdvarBlock.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
			stdvarPlot.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);

			varList.add(stdvarBlock);
			varList.add(stdvarPlot);
		} catch (final MiddlewareException e) {
			PRepDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return varList;
	}

	@Override
	public ExpDesignValidationOutput validate(
		final ExpDesignParameterUi expDesignParameter, final List<ImportedGermplasm> germplasmList) {

		final Locale locale = LocaleContextHolder.getLocale();
		ExpDesignValidationOutput output = new ExpDesignValidationOutput(true, "");
		try {
			if (expDesignParameter != null && germplasmList != null) {

				if (expDesignParameter.getReplicationPercentage() == null || expDesignParameter.getReplicationPercentage() < 0
					|| expDesignParameter.getReplicationPercentage() > 100) {
					output = new ExpDesignValidationOutput(
						false,
						this.messageSource
							.getMessage(EXPERIMENT_DESIGN_REPLICATION_PERCENTAGE_SHOULD_BE_BETWEEN_ZERO_AND_HUNDRED, null, locale));
					return output;
				} else if (!NumberUtils.isNumber(expDesignParameter.getBlockSize())) {
					output = new ExpDesignValidationOutput(
						false,
						this.messageSource.getMessage(EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_NUMBER, null, locale));
					return output;
				} else if (!NumberUtils.isNumber(expDesignParameter.getReplicationsCount())) {
					output = new ExpDesignValidationOutput(
						false,
						this.messageSource.getMessage(EXPERIMENT_DESIGN_REPLICATION_COUNT_SHOULD_BE_A_NUMBER, null, locale));
					return output;
				} else if (expDesignParameter.getStartingPlotNo() != null && !NumberUtils
					.isNumber(expDesignParameter.getStartingPlotNo())) {
					output = new ExpDesignValidationOutput(
						false,
						this.messageSource.getMessage(PLOT_NUMBER_SHOULD_BE_IN_RANGE, null, locale));
					return output;
				} else if (expDesignParameter.getStartingEntryNo() != null && !NumberUtils
					.isNumber(expDesignParameter.getStartingEntryNo())) {
					output = new ExpDesignValidationOutput(
						false,
						this.messageSource.getMessage(ENTRY_NUMBER_SHOULD_BE_IN_RANGE, null, locale));
					return output;
				} else {
					final Integer entryNumber = StringUtil.parseInt(expDesignParameter.getStartingEntryNo(), null);
					final Integer plotNumber = StringUtil.parseInt(expDesignParameter.getStartingPlotNo(), null);

					if (Objects.equals(entryNumber, 0)) {
						output = new ExpDesignValidationOutput(
							false,
							this.messageSource.getMessage(ENTRY_NUMBER_SHOULD_BE_IN_RANGE, null, locale));
					} else if (Objects.equals(plotNumber, 0)) {
						output = new ExpDesignValidationOutput(
							false,
							this.messageSource.getMessage(PLOT_NUMBER_SHOULD_BE_IN_RANGE, null, locale));
					}
				}
			}
		} catch (final Exception e) {
			output = new ExpDesignValidationOutput(
				false,
				this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}

		return output;
	}

	@Override
	public List<Integer> getExperimentalDesignVariables(final ExpDesignParameterUi params) {
		return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId(),
			TermId.PERCENTAGE_OF_REPLICATION.getId());
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}

	protected Map<Integer, StandardVariable> getRequiredDesignVariablesMap() {
		final Map<Integer, StandardVariable> map = new HashMap<>();
		final List<StandardVariable> requiredDesignVariables = this.getRequiredDesignVariables();
		for (final StandardVariable standardVariable : requiredDesignVariables) {
			map.put(standardVariable.getId(), standardVariable);
		}
		return map;
	}

	protected void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
