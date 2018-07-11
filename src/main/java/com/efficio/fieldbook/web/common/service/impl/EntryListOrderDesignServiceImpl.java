package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.util.ExpDesignUtil;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.EntryListOrderDesignService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class EntryListOrderDesignServiceImpl implements EntryListOrderDesignService {

	private static final Logger LOG = LoggerFactory.getLogger(EntryListOrderDesignServiceImpl.class);

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public List<MeasurementRow> generateDesign(final List<ImportedGermplasm> germplasmList, final ExpDesignParameterUi parameter,
			final List<MeasurementVariable> trialVariables, final List<MeasurementVariable> factors,
			final List<MeasurementVariable> nonTrialFactors, final List<MeasurementVariable> variates,
			final List<TreatmentVariable> treatmentVariables) throws BVDesignException {

		final List<MeasurementRow> measurementRows = new ArrayList<>();

		final List<ImportedGermplasm> checkList = new LinkedList<>();

		final List<ImportedGermplasm> testEntryList = new LinkedList<>();

		this.loadChecksAndTestEntries(germplasmList, checkList, testEntryList);

		final Integer startingPosition =
				(StringUtils.isEmpty(parameter.getCheckStartingPosition())) ? null : Integer.parseInt(parameter.getCheckStartingPosition());

		final Integer spacing = (StringUtils.isEmpty(parameter.getCheckSpacing())) ? null : Integer.parseInt(parameter.getCheckSpacing());

		final Integer insertionManner =
				(StringUtils.isEmpty(parameter.getCheckInsertionManner())) ? null : Integer.parseInt(parameter.getCheckInsertionManner());

		final List<ImportedGermplasm> mergedGermplasmList =
				this.mergeTestAndCheckEntries(testEntryList, checkList, startingPosition, spacing, insertionManner);

		final int environments = Integer.valueOf(parameter.getNoOfEnvironments());

		for (int instanceNumber = 1; instanceNumber <= environments; instanceNumber++) {

			Integer plotNumber = Integer.parseInt(parameter.getStartingPlotNo());

			for (final ImportedGermplasm germplasm : mergedGermplasmList) {

				final MeasurementRow measurementRow =
						this.createMeasurementRow(instanceNumber, germplasm, germplasm.getEntryId(), plotNumber++, trialVariables, variates,
								nonTrialFactors, factors);
				measurementRows.add(measurementRow);
			}
		}

		return measurementRows;

	}

	@Override
	public List<StandardVariable> getRequiredDesignVariables() {

		final List<StandardVariable> varList = new ArrayList<>();

		try {

			final StandardVariable stdVarPlot =
					this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), contextUtil.getCurrentProgramUUID());
			stdVarPlot.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);

			varList.add(stdVarPlot);

		} catch (final MiddlewareException e) {
			EntryListOrderDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return varList;
	}

	@Override
	public ExpDesignValidationOutput validate(final ExpDesignParameterUi expDesignParameter, final List<ImportedGermplasm> germplasmList) {
		final Locale locale = LocaleContextHolder.getLocale();
		try {
			if (expDesignParameter != null && germplasmList != null) {
				if (expDesignParameter.getStartingPlotNo() != null && !NumberUtils.isNumber(expDesignParameter.getStartingPlotNo())) {
					return new ExpDesignValidationOutput(Boolean.FALSE,
							this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
				} else {
					final List<ImportedGermplasm> checkList = new LinkedList<>();

					final List<ImportedGermplasm> testEntryList = new LinkedList<>();

					this.loadChecksAndTestEntries(germplasmList, checkList, testEntryList);

					if (testEntryList.isEmpty()) {
						return new ExpDesignValidationOutput(Boolean.FALSE,
								this.messageSource.getMessage("germplasm.list.all.entries.can.not.be.checks", null, locale));
					}

					if (!checkList.isEmpty()) {
						if (expDesignParameter.getCheckStartingPosition() == null || !NumberUtils
								.isNumber(expDesignParameter.getCheckStartingPosition())) {
							return new ExpDesignValidationOutput(Boolean.FALSE,
									this.messageSource.getMessage("germplasm.list.start.index.whole.number.error", null, locale));
						}
						if (expDesignParameter.getCheckSpacing() == null || !NumberUtils.isNumber(expDesignParameter.getCheckSpacing())) {
							return new ExpDesignValidationOutput(Boolean.FALSE, this.messageSource
									.getMessage("germplasm.list.number.of.rows.between.insertion.should.be.a.whole.number", null, locale));
						}
						if (expDesignParameter.getCheckInsertionManner() == null || !NumberUtils
								.isNumber(expDesignParameter.getCheckInsertionManner())) {
							return new ExpDesignValidationOutput(Boolean.FALSE,
									this.messageSource.getMessage("check.manner.of.insertion.invalid", null, locale));
						}

						final Integer checkStartingPosition =
								(StringUtils.isEmpty(expDesignParameter.getCheckStartingPosition())) ? null : Integer.parseInt(expDesignParameter.getCheckStartingPosition());

						final Integer checkSpacing = (StringUtils.isEmpty(expDesignParameter.getCheckSpacing())) ? null : Integer.parseInt(expDesignParameter.getCheckSpacing());

						if (checkStartingPosition < 1) {
							return new ExpDesignValidationOutput(Boolean.FALSE, this.messageSource
									.getMessage("germplasm.list.starting.index.should.be.greater.than.zero", null, locale));
						}
						if (checkStartingPosition > testEntryList.size()) {
							return new ExpDesignValidationOutput(Boolean.FALSE,
									this.messageSource.getMessage("germplasm.list.start.index.less.than.germplasm.error", null, locale));
						}
						if (checkSpacing < 1) {
							return new ExpDesignValidationOutput(Boolean.FALSE, this.messageSource
									.getMessage("germplasm.list.number.of.rows.between.insertion.should.be.greater.than.zero", null,
											locale));
						}
						if (checkSpacing > testEntryList.size()) {
							return new ExpDesignValidationOutput(Boolean.FALSE,
									this.messageSource.getMessage("germplasm.list.spacing.less.than.germplasm.error", null, locale));
						}
						if (germplasmList.size() - checkList.size() == 0) {
							return new ExpDesignValidationOutput(Boolean.FALSE,
									this.messageSource.getMessage("germplasm.list.all.entries.can.not.be.checks", null, locale));
						}
					}
				}
			}
		} catch (final Exception e) {
			return new ExpDesignValidationOutput(Boolean.FALSE,
					this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}
		return new ExpDesignValidationOutput(Boolean.TRUE, StringUtils.EMPTY);
	}

	@Override
	public List<Integer> getExperimentalDesignVariables(final ExpDesignParameterUi params) {
		if (!StringUtils.isEmpty(params.getCheckSpacing())){
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.CHECK_START.getId(), TermId.CHECK_INTERVAL.getId(),
					TermId.CHECK_PLAN.getId());
		} else {
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
		}
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.FALSE;
	}


	private void loadChecksAndTestEntries(final List<ImportedGermplasm> importedGermplasmList, final List<ImportedGermplasm> checkList,
			final List<ImportedGermplasm> testEntryList) {

		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())) {
				testEntryList.add(importedGermplasm);
			} else {
				checkList.add(importedGermplasm);
			}
		}
	}

	private boolean isThereSomethingToMerge(final List<ImportedGermplasm> entriesList, final List<ImportedGermplasm> checkList,
			final Integer startEntry, final Integer interval) {
		Boolean isThereSomethingToMerge = Boolean.TRUE;
		if (checkList == null || checkList.isEmpty()) {
			isThereSomethingToMerge = Boolean.FALSE;
		} else if (entriesList == null || entriesList.isEmpty()) {
			isThereSomethingToMerge = Boolean.FALSE;
		} else if (startEntry < 1 || startEntry > entriesList.size() || interval < 1) {
			isThereSomethingToMerge = Boolean.FALSE;
		}
		return isThereSomethingToMerge;
	}

	private List<ImportedGermplasm> generateChecksToInsert(final List<ImportedGermplasm> checkList, final Integer checkIndex,
			final Integer insertionManner) {
		final List<ImportedGermplasm> newList = new ArrayList<>();
		if (insertionManner.equals(InsertionMannerItem.INSERT_ALL_CHECKS.getId())) {
			for (final ImportedGermplasm checkGerm : checkList) {
				newList.add(checkGerm.copy());
			}
		} else {
			final Integer checkListIndex = checkIndex % checkList.size();
			final ImportedGermplasm checkGerm = checkList.get(checkListIndex);
			newList.add(checkGerm.copy());
		}
		return newList;
	}

	private List<ImportedGermplasm> mergeTestAndCheckEntries(final List<ImportedGermplasm> testEntryList,
			final List<ImportedGermplasm> checkList, final Integer startingIndex, final Integer spacing, final Integer insertionManner) {

		if (!this.isThereSomethingToMerge(testEntryList, checkList, startingIndex, spacing)) {
			return testEntryList;
		}

		final List<ImportedGermplasm> newList = new ArrayList<>();

		int primaryEntry = 1;
		boolean isStarted = Boolean.FALSE;
		boolean shouldInsert = Boolean.FALSE;
		int checkIndex = 0;
		int intervalEntry = 0;
		for (final ImportedGermplasm primaryGermplasm : testEntryList) {
			if (primaryEntry == startingIndex || intervalEntry == spacing) {
				isStarted = Boolean.TRUE;
				shouldInsert = Boolean.TRUE;
				intervalEntry = 0;
			}

			if (isStarted) {
				intervalEntry++;
			}

			if (shouldInsert) {
				shouldInsert = Boolean.FALSE;
				List<ImportedGermplasm> checks = this.generateChecksToInsert(checkList, checkIndex, insertionManner);
				checkIndex++;
				newList.addAll(checks);
			}
			final ImportedGermplasm primaryNewGermplasm = primaryGermplasm.copy();
			primaryNewGermplasm.setEntryTypeValue(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue());
			primaryNewGermplasm.setEntryTypeCategoricalID(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());

			newList.add(primaryNewGermplasm);

			primaryEntry++;
		}

		return newList;
	}

	private MeasurementRow createMeasurementRow(final int instanceNo, final ImportedGermplasm germplasm, final int entryNo,
			final int plotNo, final List<MeasurementVariable> trialVariables, final List<MeasurementVariable> variates,
			final List<MeasurementVariable> nonTrialFactors, final List<MeasurementVariable> factors) throws MiddlewareQueryException {

		final MeasurementRow measurementRow = new MeasurementRow();

		final List<MeasurementData> dataList = new ArrayList<>();

		this.createTrialInstanceDataList(dataList, trialVariables, instanceNo);

		this.createFactorDataList(dataList, germplasm, entryNo, plotNo, nonTrialFactors, factors);

		this.createVariateDataList(dataList, variates);

		measurementRow.setDataList(dataList);

		return measurementRow;
	}

	private void createTrialInstanceDataList(final List<MeasurementData> dataList, final List<MeasurementVariable> trialVariables,
			final Integer instanceNumber) {
		final MeasurementVariable instanceVariable =
				WorkbookUtil.getMeasurementVariable(trialVariables, TermId.TRIAL_INSTANCE_FACTOR.getId());
		final MeasurementData measurementData =
				new MeasurementData(instanceVariable.getName(), instanceNumber.toString(), Boolean.FALSE, instanceVariable.getDataType(),
						instanceVariable);
		dataList.add(measurementData);
	}

	private void createFactorDataList(final List<MeasurementData> dataList, final ImportedGermplasm germplasm, final int entryNo,
			final int plotNo, final List<MeasurementVariable> nonTrialFactors, final List<MeasurementVariable> factors)
			throws MiddlewareQueryException {

		final Map<String, Integer> standardVariableMap = new HashMap<>();

		final StandardVariable stdVarPlot = this.getRequiredDesignVariables().get(0);

		if (WorkbookUtil.getMeasurementVariable(nonTrialFactors, stdVarPlot.getId()) == null) {
			final MeasurementVariable measureVar =
					ExpDesignUtil.convertStandardVariableToMeasurementVariable(stdVarPlot, Operation.ADD, fieldbookService);
			measureVar.setRole(PhenotypicType.TRIAL_DESIGN);
			nonTrialFactors.add(measureVar);
			if (WorkbookUtil.getMeasurementVariable(factors, stdVarPlot.getId()) == null) {
				factors.add(measureVar);
			}
		}

		for (final MeasurementVariable var : nonTrialFactors) {

			// do not include treatment factors
			if (var.getTreatmentLabel() == null || var.getTreatmentLabel().isEmpty()) {
				final MeasurementData measurementData;

				Integer termId = var.getTermId();
				if (termId == 0) {
					final String key = var.getProperty() + ":" + var.getScale() + ":" + var.getMethod() + ":" + PhenotypicType
							.getPhenotypicTypeForLabel(var.getLabel());
					if (standardVariableMap.get(key) == null) {
						termId = this.fieldbookMiddlewareService
								.getStandardVariableIdByPropertyScaleMethodRole(var.getProperty(), var.getScale(), var.getMethod(),
										PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
						standardVariableMap.put(key, termId);
					} else {
						termId = standardVariableMap.get(key);

					}
				}

				var.setFactor(Boolean.TRUE);

				if (termId == null) {
					// we default if null, but should not happen
					measurementData = new MeasurementData(var.getName(), StringUtils.EMPTY, Boolean.TRUE, var.getDataType(), var);
					var.setFactor(Boolean.FALSE);
					measurementData.setEditable(Boolean.TRUE);
				} else {

					if (termId == TermId.ENTRY_NO.getId()) {
						measurementData =
								new MeasurementData(var.getName(), Integer.toString(entryNo), Boolean.FALSE, var.getDataType(), var);
					} else if (termId == TermId.SOURCE.getId() || termId == TermId.GERMPLASM_SOURCE.getId()) {
						measurementData = new MeasurementData(var.getName(),
								germplasm.getSource() != null ? germplasm.getSource() : StringUtils.EMPTY, Boolean.FALSE, var.getDataType(),
								var);
					} else if (termId == TermId.GROUPGID.getId()) {
						measurementData = new MeasurementData(var.getName(),
								germplasm.getGroupId() != null ? germplasm.getGroupId().toString() : StringUtils.EMPTY, Boolean.FALSE,
								var.getDataType(), var);
					} else if (termId == TermId.STOCKID.getId()) {
						measurementData = new MeasurementData(var.getName(),
								germplasm.getStockIDs() != null ? germplasm.getStockIDs() : StringUtils.EMPTY, Boolean.FALSE,
								var.getDataType(), var);
					} else if (termId == TermId.CROSS.getId()) {
						measurementData = new MeasurementData(var.getName(), germplasm.getCross(), Boolean.FALSE, var.getDataType(), var);
					} else if (termId == TermId.DESIG.getId()) {
						measurementData = new MeasurementData(var.getName(), germplasm.getDesig(), Boolean.FALSE, var.getDataType(), var);
					} else if (termId == TermId.GID.getId()) {
						measurementData = new MeasurementData(var.getName(), germplasm.getGid(), Boolean.FALSE, var.getDataType(), var);
					} else if (termId == TermId.ENTRY_CODE.getId()) {
						measurementData =
								new MeasurementData(var.getName(), germplasm.getEntryCode(), Boolean.FALSE, var.getDataType(), var);
					} else if (termId == TermId.PLOT_NO.getId()) {
						measurementData =
								new MeasurementData(var.getName(), Integer.toString(plotNo), Boolean.FALSE, var.getDataType(), var);
					} else if (termId == TermId.ENTRY_TYPE.getId()) {
						// if germplasm has defined check value, use that
						if (germplasm.getEntryTypeCategoricalID() != null) {
							measurementData =
									new MeasurementData(var.getName(), germplasm.getEntryTypeName(), Boolean.FALSE, var.getDataType(),
											germplasm.getEntryTypeCategoricalID(), var);
						} else {
							// if germplasm does not have a defined check value, but ENTRY_TYPE factor is needed, we provide the current system default
							measurementData =
									new MeasurementData(var.getName(), SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue(), Boolean.FALSE,
											var.getDataType(), SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId(), var);
						}
					} else {
						// meaning non factor
						measurementData = new MeasurementData(var.getName(), StringUtils.EMPTY, Boolean.TRUE, var.getDataType(), var);
					}
				}

				dataList.add(measurementData);
			}
		}
	}

	private void createVariateDataList(final List<MeasurementData> dataList, final List<MeasurementVariable> variates) {
		for (final MeasurementVariable variate : variates) {
			final MeasurementData measurementData =
					new MeasurementData(variate.getName(), StringUtils.EMPTY, Boolean.TRUE, variate.getDataType(), variate);
			variate.setFactor(Boolean.FALSE);
			dataList.add(measurementData);
		}
	}
}
