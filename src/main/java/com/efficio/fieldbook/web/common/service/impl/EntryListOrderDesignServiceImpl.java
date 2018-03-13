package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.EntryListOrderDesignService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class EntryListOrderDesignServiceImpl implements EntryListOrderDesignService {

	private static final Logger LOG = LoggerFactory.getLogger(EntryListOrderDesignServiceImpl.class);

	@Resource
	private ResourceBundleMessageSource messageSource;

	private List<ImportedGermplasm> checks;

	private List<ImportedGermplasm> entries;

	/**
	 * Generate design.
	 *
	 * @param germplasmList      the germplasm list
	 * @param parameter
	 * @param trialVariables
	 * @param factors
	 * @param nonTrialFactors    the non trial factors
	 * @param variates           the variates
	 * @param treatmentVariables the treatment variables    @return the list
	 */
	@Override
	public List<MeasurementRow> generateDesign(final List<ImportedGermplasm> germplasmList, final ExpDesignParameterUi parameter,
			final List<MeasurementVariable> trialVariables, final List<MeasurementVariable> factors,
			final List<MeasurementVariable> nonTrialFactors, final List<MeasurementVariable> variates,
			final List<TreatmentVariable> treatmentVariables) throws BVDesignException {
		return null;
	}

	/**
	 * Gets the list of  variables necessary for generating a design (e.g. PLOT_NO, ENTRY_NO, REP_NO).
	 *
	 * @return list of standard variables.
	 */
	@Override
	public List<StandardVariable> getRequiredDesignVariables() {
		return null;
	}

	/**
	 * Validates the design parameters and germplasm list entries.
	 *
	 * @param expDesignParameter the exp design parameter
	 * @param germplasmList
	 * @return the exp design validation output
	 */
	@Override
	public ExpDesignValidationOutput validate(final ExpDesignParameterUi expDesignParameter, final List<ImportedGermplasm> germplasmList) {
		final Locale locale = LocaleContextHolder.getLocale();
		try {
			if (expDesignParameter != null && germplasmList != null) {
				if (expDesignParameter.getStartingPlotNo() != null && !NumberUtils
						.isNumber(expDesignParameter.getStartingPlotNo())) {
					return new ExpDesignValidationOutput(false,
							this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
				} else {
					if (checks == null) {
						this.loadChecksAndEntries(germplasmList);
					}
					if (!checks.isEmpty()) {
						if (expDesignParameter.getCheckStartingPosition() == null ||  ( expDesignParameter.getCheckStartingPosition() != null && !NumberUtils
								.isNumber(expDesignParameter.getCheckStartingPosition()))) {
							return new ExpDesignValidationOutput(false,
									this.messageSource.getMessage("germplasm.list.start.index.whole.number.error", null, locale));
						}
						if (expDesignParameter.getCheckSpacing() == null ||  (expDesignParameter.getCheckSpacing() != null
								&& !NumberUtils.isNumber(expDesignParameter.getCheckSpacing()))) {
							return new ExpDesignValidationOutput(false,
									this.messageSource.getMessage("germplasm.list.number.of.rows.between.insertion.should.be.a.whole.number", null, locale));
						}
						if (expDesignParameter.getCheckInsertionManner() == null || (expDesignParameter.getCheckInsertionManner() != null && !NumberUtils
								.isNumber(expDesignParameter.getCheckInsertionManner()))) {
							return new ExpDesignValidationOutput(false,
									this.messageSource.getMessage("check.manner.of.insertion.invalid", null, locale));
						}
						final Integer checkStartingPosition = Integer.parseInt(expDesignParameter.getCheckStartingPosition());
						final Integer checkSpacing = Integer.parseInt(expDesignParameter.getCheckSpacing());
						if (checkStartingPosition < 1) {
							return new ExpDesignValidationOutput(false,
									this.messageSource.getMessage("germplasm.list.starting.index.should.be.greater.than.zero", null, locale));
						}
						if (checkStartingPosition > entries.size()) {
							return new ExpDesignValidationOutput(false,
									this.messageSource.getMessage("germplasm.list.start.index.less.than.germplasm.error", null, locale));
						}
						if (checkSpacing < 1 ) {
							return new ExpDesignValidationOutput(false,
									this.messageSource.getMessage("germplasm.list.number.of.rows.between.insertion.should.be.greater.than.zero", null, locale));
						}
						if (checkSpacing > entries.size()) {
							return new ExpDesignValidationOutput(false,
									this.messageSource.getMessage("germplasm.list.spacing.less.than.germplasm.error", null, locale));
						}
						if (entries.size() - checks.size() == 0) {
							return new ExpDesignValidationOutput(false,
									this.messageSource.getMessage("germplasm.list.all.entries.can.not.be.checks", null, locale));
						}
					}
				}
			}
		} catch (final Exception e) {
			return new ExpDesignValidationOutput(false,
					this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}
		return new ExpDesignValidationOutput(true, "");
	}

	/**
	 * Gets the list of variables in experimental design (e.g. NUMBER_OF_REPLICATES, NUMBER_OF_REPLICATES, NBLKS)
	 *
	 * @param params
	 * @return
	 */
	@Override
	public List<Integer> getExperimentalDesignVariables(final ExpDesignParameterUi params) {
		return null;
	}

	private void loadChecksAndEntries(final List<ImportedGermplasm> importedGermplasmList) {

		checks = new LinkedList<>();
		entries = new LinkedList<>();

		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()) ||
					importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.STRESS_CHECK.getEntryTypeCategoricalId()) ||
					importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeCategoricalId())) {
				checks.add(importedGermplasm);
			} else {
				entries.add(importedGermplasm);
			}
		}
	}

}
