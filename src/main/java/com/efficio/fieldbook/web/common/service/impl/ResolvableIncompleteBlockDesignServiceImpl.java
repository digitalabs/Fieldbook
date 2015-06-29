
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.annotation.Resource;

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
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

@Service
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

	@Override
	public List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList, ExpDesignParameterUi parameter,
			List<MeasurementVariable> trialVariables, List<MeasurementVariable> factors, List<MeasurementVariable> nonTrialFactors,
			List<MeasurementVariable> variates, List<TreatmentVariable> treatmentVariables) throws BVDesignException {

		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();

		int nTreatments = germplasmList.size();
		String blockSize = parameter.getBlockSize();
		String replicates = parameter.getReplicationsCount();
		int environments = Integer.valueOf(parameter.getNoOfEnvironments());
		int environmentsToAdd = Integer.valueOf(parameter.getNoOfEnvironmentsToAdd());
		// we need to add the 4 vars
		try {

			StandardVariable stdvarTreatment = this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),
					contextUtil.getCurrentProgramUUID());
			StandardVariable stdvarRep = null;
			StandardVariable stdvarBlock = null;
			StandardVariable stdvarPlot = null;

			List<StandardVariable> reqVarList = this.getRequiredVariable();

			for (StandardVariable var : reqVarList) {
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

			MainDesign mainDesign =
					ExpDesignUtil.createResolvableIncompleteBlockDesign(blockSize, Integer.toString(nTreatments), replicates,
							stdvarTreatment.getName(), stdvarRep.getName(), stdvarBlock.getName(), stdvarPlot.getName(),
							parameter.getNblatin(), parameter.getReplatinGroups(), "", parameter.getUseLatenized());

			measurementRowList =
					ExpDesignUtil.generateExpDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
							variates, treatmentVariables, reqVarList, germplasmList, mainDesign, this.workbenchService,
							this.fieldbookProperties, stdvarTreatment.getName(), null, this.fieldbookService);

		} catch (BVDesignException e) {
			throw e;
		} catch (Exception e) {
			ResolvableIncompleteBlockDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return measurementRowList;
	}

	@Override
	public List<StandardVariable> getRequiredVariable() {
		List<StandardVariable> varList = new ArrayList<StandardVariable>();
		try {
			StandardVariable stdvarRep = this.fieldbookMiddlewareService.getStandardVariable(TermId.REP_NO.getId(),
					contextUtil.getCurrentProgramUUID());
			StandardVariable stdvarBlock = this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(),
					contextUtil.getCurrentProgramUUID());
			StandardVariable stdvarPlot = this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(),
					contextUtil.getCurrentProgramUUID());

			stdvarRep.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
			stdvarBlock.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
			stdvarPlot.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
			
			varList.add(stdvarRep);
			varList.add(stdvarBlock);
			varList.add(stdvarPlot);
		} catch (MiddlewareException e) {
			ResolvableIncompleteBlockDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return varList;
	}

	@Override
	public ExpDesignValidationOutput validate(ExpDesignParameterUi expDesignParameter, List<ImportedGermplasm> germplasmList) {
		Locale locale = LocaleContextHolder.getLocale();
		ExpDesignValidationOutput output = new ExpDesignValidationOutput(true, "");
		try {
			if (expDesignParameter != null && germplasmList != null) {
				if (!NumberUtils.isNumber(expDesignParameter.getBlockSize())) {
					output =
							new ExpDesignValidationOutput(false, this.messageSource.getMessage(
									"experiment.design.block.size.should.be.a.number", null, locale));
				} else if (!NumberUtils.isNumber(expDesignParameter.getReplicationsCount())) {
					output =
							new ExpDesignValidationOutput(false, this.messageSource.getMessage(
									"experiment.design.replication.count.should.be.a.number", null, locale));
				} else {
					int blockSize = Integer.valueOf(expDesignParameter.getBlockSize());
					int replicationCount = Integer.valueOf(expDesignParameter.getReplicationsCount());
					int treatmentSize = germplasmList.size();
					int blockLevel = treatmentSize / blockSize;

					if (replicationCount <= 1 || replicationCount >= 13) {
						output =
								new ExpDesignValidationOutput(false, this.messageSource.getMessage(
										"experiment.design.replication.count.resolvable.error", null, locale));
					} else if (blockSize <= 1) {
						output =
								new ExpDesignValidationOutput(false, this.messageSource.getMessage(
										"experiment.design.block.size.should.be.a.greater.than.1", null, locale));
					} else if (blockLevel == 1) {
						output =
								new ExpDesignValidationOutput(false, this.messageSource.getMessage(
										"experiment.design.block.level.should.be.greater.than.one", null, locale));
					} else if (treatmentSize % blockSize != 0) {
						output =
								new ExpDesignValidationOutput(false, this.messageSource.getMessage(
										"experiment.design.block.size.not.a.factor.of.treatment.size", null, locale));
					} else if (expDesignParameter.getUseLatenized() != null && expDesignParameter.getUseLatenized().booleanValue()) {
						// we add validation for latinize
						Integer nbLatin = expDesignParameter.getNblatin() != null ? Integer.parseInt(expDesignParameter.getNblatin()) : 0;
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
							output =
									new ExpDesignValidationOutput(false, this.messageSource.getMessage(
											"experiment.design.nblatin.should.not.be.greater.than.block.level", null, locale));
						} else if (nbLatin >= replicationCount) {
							output =
									new ExpDesignValidationOutput(false, this.messageSource.getMessage(
											"experiment.design.nblatin.should.not.be.greater.than.the.replication.count", null, locale));
						} else if (expDesignParameter.getReplicationsArrangement() != null
								&& expDesignParameter.getReplicationsArrangement().intValue() == 3) {
							// meaning adjacent
							StringTokenizer tokenizer = new StringTokenizer(expDesignParameter.getReplatinGroups(), ",");
							int totalReplatingGroup = 0;

							while (tokenizer.hasMoreTokens()) {
								totalReplatingGroup += Integer.parseInt(tokenizer.nextToken());
							}
							if (totalReplatingGroup != replicationCount) {
								output =
										new ExpDesignValidationOutput(false, this.messageSource.getMessage(
												"experiment.design.replating.groups.not.equal.to.replicates", null, locale));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			output =
					new ExpDesignValidationOutput(false, this.messageSource.getMessage("experiment.design.invalid.generic.error", null,
							locale));
		}

		return output;
	}

	@Override
	public List<Integer> getExperimentalDesignVariables(ExpDesignParameterUi params) {
		if (params.getUseLatenized() != null && params.getUseLatenized()) {
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId(),
					TermId.NO_OF_CBLKS_LATINIZE.getId(), TermId.REPLICATIONS_MAP.getId(), TermId.NO_OF_REPS_IN_COLS.getId());
		} else {
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId());
		}
	}
}
