package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.ExperimentDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
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
import java.util.Objects;
import java.util.StringTokenizer;

@Service
@Transactional
public class ResolvableIncompleteBlockDesignServiceImpl implements ResolvableIncompleteBlockDesignService {

	private static final Logger LOG = LoggerFactory.getLogger(ResolvableIncompleteBlockDesignServiceImpl.class);

	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	@Resource
	protected WorkbenchService workbenchService;
	@Resource
	protected FieldbookProperties fieldbookProperties;
	@Resource
	private ResourceBundleMessageSource messageSource;
	@Resource
	public FieldbookService fieldbookService;
	@Resource
	public ContextUtil contextUtil;
	@Resource
	public ExperimentDesignGenerator experimentDesignGenerator;

	@Override
	public List<MeasurementRow> generateDesign(final List<ImportedGermplasm> germplasmList, final ExpDesignParameterUi parameter,
			final List<MeasurementVariable> trialVariables, final List<MeasurementVariable> factors, final List<MeasurementVariable> nonTrialFactors,
			final List<MeasurementVariable> variates, final List<TreatmentVariable> treatmentVariables) throws BVDesignException {

		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();

		final int nTreatments = germplasmList.size();
		final String blockSize = parameter.getBlockSize();
		final String replicates = parameter.getReplicationsCount();
		final int environments = Integer.valueOf(parameter.getNoOfEnvironments());
		final int environmentsToAdd = Integer.valueOf(parameter.getNoOfEnvironmentsToAdd());
		// we need to add the 4 vars
		try {

			final StandardVariable stdvarTreatment =
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(), contextUtil.getCurrentProgramUUID());
			StandardVariable stdvarRep = null;
			StandardVariable stdvarBlock = null;
			StandardVariable stdvarPlot = null;

			final List<StandardVariable> reqVarList = this.getRequiredDesignVariables();

			for (final StandardVariable var : reqVarList) {
				if (var.getId() == TermId.REP_NO.getId()) {
					stdvarRep = var;
				} else if (var.getId() == TermId.BLOCK_NO.getId()) {
					stdvarBlock = var;
				} else if (var.getId() == TermId.PLOT_NO.getId()) {
					stdvarPlot = var;
				}
			}

			if (parameter.getUseLatenized() != null && parameter.getUseLatenized().booleanValue()) {
				if (parameter.getReplicationsArrangement() != null) {
					if (parameter.getReplicationsArrangement().intValue() == 1) {
						// column
						parameter.setReplatinGroups(parameter.getReplicationsCount());
					} else if (parameter.getReplicationsArrangement().intValue() == 2) {
						// rows
						String rowReplatingGroup = "";
						for (int i = 0; i < Integer.parseInt(parameter.getReplicationsCount()); i++) {
							if (rowReplatingGroup != null && !rowReplatingGroup.equalsIgnoreCase("")) {
								rowReplatingGroup += ",";
							}
							rowReplatingGroup += "1";
						}
						parameter.setReplatinGroups(rowReplatingGroup);
					}
				}
			}

			final Integer plotNo = StringUtil.parseInt(parameter.getStartingPlotNo(), null);
			Integer entryNo = StringUtil.parseInt(parameter.getStartingEntryNo(), null);

			if (!Objects.equals(stdvarTreatment.getId(), TermId.ENTRY_NO.getId())) {
				entryNo = null;
			}

			final MainDesign mainDesign = experimentDesignGenerator
					.createResolvableIncompleteBlockDesign(blockSize, Integer.toString(nTreatments), replicates, stdvarTreatment.getName(),
							stdvarRep.getName(), stdvarBlock.getName(), stdvarPlot.getName(), plotNo, entryNo, parameter.getNblatin(),
							parameter.getReplatinGroups(), "", parameter.getUseLatenized());

			measurementRowList = experimentDesignGenerator
					.generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
							variates, treatmentVariables, reqVarList, germplasmList, mainDesign, stdvarTreatment.getName(), null,
							new HashMap<Integer, Integer>());

		} catch (final BVDesignException e) {
			throw e;
		} catch (final Exception e) {
			ResolvableIncompleteBlockDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return measurementRowList;
	}

	@Override
	public List<StandardVariable> getRequiredDesignVariables() {
		final List<StandardVariable> varList = new ArrayList<StandardVariable>();
		try {
			final StandardVariable stdvarRep =
					this.fieldbookMiddlewareService.getStandardVariable(TermId.REP_NO.getId(), contextUtil.getCurrentProgramUUID());
			final StandardVariable stdvarBlock =
					this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), contextUtil.getCurrentProgramUUID());
			final StandardVariable stdvarPlot =
					this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), contextUtil.getCurrentProgramUUID());

			stdvarRep.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
			stdvarBlock.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
			stdvarPlot.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);

			varList.add(stdvarRep);
			varList.add(stdvarBlock);
			varList.add(stdvarPlot);
		} catch (final MiddlewareException e) {
			ResolvableIncompleteBlockDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return varList;
	}

	@Override
	public ExpDesignValidationOutput validate(final ExpDesignParameterUi expDesignParameter, final List<ImportedGermplasm> germplasmList) {
		final Locale locale = LocaleContextHolder.getLocale();
		ExpDesignValidationOutput output = new ExpDesignValidationOutput(true, "");
		try {
			if (expDesignParameter != null && germplasmList != null) {
				if (!NumberUtils.isNumber(expDesignParameter.getBlockSize())) {
					output = new ExpDesignValidationOutput(false,
							this.messageSource.getMessage("experiment.design.block.size.should.be.a.number", null, locale));
					return output;
				} else if (!NumberUtils.isNumber(expDesignParameter.getReplicationsCount())) {
					output = new ExpDesignValidationOutput(false,
							this.messageSource.getMessage("experiment.design.replication.count.should.be.a.number", null, locale));
					return output;
				} else if (expDesignParameter.getStartingPlotNo() != null && !NumberUtils
						.isNumber(expDesignParameter.getStartingPlotNo())) {
					output = new ExpDesignValidationOutput(false,
							this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
					return output;
				} else if (expDesignParameter.getStartingEntryNo() != null && !NumberUtils
						.isNumber(expDesignParameter.getStartingEntryNo())) {
					output = new ExpDesignValidationOutput(false,
							this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
					return output;
				} else {
					final int blockSize = Integer.valueOf(expDesignParameter.getBlockSize());
					final int replicationCount = Integer.valueOf(expDesignParameter.getReplicationsCount());
					final int treatmentSize = germplasmList.size();
					final int blockLevel = treatmentSize / blockSize;
					final Integer entryNumber = StringUtil.parseInt(expDesignParameter.getStartingEntryNo(), null);
					final Integer plotNumber = StringUtil.parseInt(expDesignParameter.getStartingPlotNo(), null);
					final Integer maxEntry = treatmentSize + entryNumber - 1;
					final Integer maxPlot = (treatmentSize * replicationCount) + plotNumber - 1;

					if (Objects.equals(entryNumber, 0)) {
						output = new ExpDesignValidationOutput(false,
								this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
					} else if (Objects.equals(plotNumber, 0)) {
						output = new ExpDesignValidationOutput(false,
								this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
					} else if (replicationCount <= 1 || replicationCount >= 13) {
						output = new ExpDesignValidationOutput(false,
								this.messageSource.getMessage("experiment.design.replication.count.resolvable.error", null, locale));
					} else if (entryNumber != null && maxEntry > ExperimentDesignService.MAX_ENTRY_NO) {
						output = new ExpDesignValidationOutput(false, this.messageSource
							.getMessage("experiment.design.entry.number.should.not.exceed", new Object[] {maxEntry}, locale));
					} else if (entryNumber != null && plotNumber != null && maxPlot > ExperimentDesignService.MAX_PLOT_NO) {
						output = new ExpDesignValidationOutput(false, this.messageSource
							.getMessage("experiment.design.plot.number.should.not.exceed", new Object[] {maxPlot}, locale));
					} else if (blockSize <= 1) {
						output = new ExpDesignValidationOutput(false,
								this.messageSource.getMessage("experiment.design.block.size.should.be.a.greater.than.1", null, locale));
					} else if (blockLevel == 1) {
						output = new ExpDesignValidationOutput(false,
								this.messageSource.getMessage("experiment.design.block.level.should.be.greater.than.one", null, locale));
					} else if (treatmentSize % blockSize != 0) {
						output = new ExpDesignValidationOutput(false,
								this.messageSource.getMessage("experiment.design.block.size.not.a.factor.of.treatment.size", null, locale));
					} else if (expDesignParameter.getUseLatenized() != null && expDesignParameter.getUseLatenized().booleanValue()) {
						// we add validation for latinize
						final Integer nbLatin = expDesignParameter.getNblatin() != null ? Integer.parseInt(expDesignParameter.getNblatin()) : 0;
						/*
						 * The value set for "nblatin" xml parameter cannot be value higher than or equal the block level value. To get the
						 * block levels, we just need to divide the "ntreatments" value by the "blocksize" value. This means the BVDesign
						 * tool works to any value you specify in the "nblatin" parameter as long as it does not exceed the computed block
						 * levels value. As mentioned in the requirements, an "nblatin" parameter with value 0 means there is no
						 * latinization that will take place. The sum of the values set for "replatingroups" should always be equal to the
						 * "nreplicates" value specified by the plant breeder.
						 */

						// nbLatin should be less than the block level
						if (nbLatin >= blockLevel) {
							output = new ExpDesignValidationOutput(false, this.messageSource
									.getMessage("experiment.design.nblatin.should.not.be.greater.than.block.level", null, locale));
						} else if (nbLatin >= replicationCount) {
							output = new ExpDesignValidationOutput(false, this.messageSource
									.getMessage("experiment.design.nblatin.should.not.be.greater.than.the.replication.count", null,
											locale));
						} else if (expDesignParameter.getReplicationsArrangement() != null
								&& expDesignParameter.getReplicationsArrangement().intValue() == 3) {
							// meaning adjacent
							final StringTokenizer tokenizer = new StringTokenizer(expDesignParameter.getReplatinGroups(), ",");
							int totalReplatingGroup = 0;

							while (tokenizer.hasMoreTokens()) {
								totalReplatingGroup += Integer.parseInt(tokenizer.nextToken());
							}
							if (totalReplatingGroup != replicationCount) {
								output = new ExpDesignValidationOutput(false, this.messageSource
										.getMessage("experiment.design.replating.groups.not.equal.to.replicates", null, locale));
							}
						}
					}
				}
			}
		} catch (final Exception e) {
			output = new ExpDesignValidationOutput(false,
					this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}

		return output;
	}

	@Override
	public List<Integer> getExperimentalDesignVariables(final ExpDesignParameterUi params) {
		if (params.getUseLatenized() != null && params.getUseLatenized()) {
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId(),
					TermId.NO_OF_CBLKS_LATINIZE.getId(), TermId.REPLICATIONS_MAP.getId(), TermId.NO_OF_REPS_IN_COLS.getId());
		} else {
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId());
		}
	}

	public void setExperimentDesignGenerator(final ExperimentDesignGenerator experimentDesignGenerator) {
		this.experimentDesignGenerator = experimentDesignGenerator;
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
