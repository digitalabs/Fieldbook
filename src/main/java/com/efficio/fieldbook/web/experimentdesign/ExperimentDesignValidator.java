package com.efficio.fieldbook.web.experimentdesign;

import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.service.ExperimentDesignService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ExperimentDesignValidator {

	@Resource
	private MessageSource messageSource;

	/**
	 * Validates the parameters and germplasm entries required for generating augmented design.
	 *
	 * @param expDesignParameterUi
	 * @param importedGermplasmList
	 * @throws DesignValidationException
	 */
	public void validateAugmentedDesign(final ExpDesignParameterUi expDesignParameterUi, final List<ImportedGermplasm> importedGermplasmList)
			throws DesignValidationException {

		if (expDesignParameterUi != null && importedGermplasmList != null) {

			final int treatmentSize = importedGermplasmList.size();

			this.validateIfCheckEntriesExistInGermplasmList(importedGermplasmList);
			this.validateStartingPlotNo(expDesignParameterUi, treatmentSize);
			this.validateStartingEntryNo(expDesignParameterUi, treatmentSize);
			this.validateNumberOfBlocks(expDesignParameterUi);
			this.validateTreatmentFactors(expDesignParameterUi);

		}

	}

	private void validateTreatmentFactors(final ExpDesignParameterUi expDesignParameterUi) throws DesignValidationException {
		if (expDesignParameterUi.getTreatmentFactorsData().size() > 0) {
			throw new DesignValidationException(
				this.messageSource.getMessage("experiment.design.treatment.factors.error", null, LocaleContextHolder.getLocale()));
		}
	}

	void validateIfCheckEntriesExistInGermplasmList(final List<ImportedGermplasm> importedGermplasmList) throws DesignValidationException {

		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())) {
				return;
			}
		}

		throw new DesignValidationException(
				this.messageSource.getMessage("germplasm.list.check.required.augmented.design", null, LocaleContextHolder.getLocale()));

	}

	void validateStartingPlotNo(final ExpDesignParameterUi expDesignParameterUi, final int treatmentSize) throws DesignValidationException {

		final String startingPlotNo = expDesignParameterUi.getStartingPlotNo();

		if (startingPlotNo != null && NumberUtils.isNumber(startingPlotNo)) {
			final Integer plotNumber = Integer.valueOf(startingPlotNo);
			if (plotNumber != 0 && ((treatmentSize + plotNumber - 1) <= ExperimentDesignService.MAX_PLOT_NO)) {
				return;
			}
		}

		throw new DesignValidationException(
				this.messageSource.getMessage("plot.number.should.be.in.range", null, LocaleContextHolder.getLocale()));

	}

	void validateStartingEntryNo(final ExpDesignParameterUi expDesignParameterUi, final int treatmentSize) throws DesignValidationException {

		final String startingEntryNo = expDesignParameterUi.getStartingEntryNo();

		if (startingEntryNo != null && NumberUtils.isNumber(startingEntryNo)) {
			final Integer entryNumber = Integer.valueOf(startingEntryNo);
			if (entryNumber != 0 && ((treatmentSize + entryNumber - 1) <= ExperimentDesignService.MAX_ENTRY_NO)) {
				return;
			}
		}

		throw new DesignValidationException(
				this.messageSource.getMessage("entry.number.should.be.in.range", null, LocaleContextHolder.getLocale()));

	}

	void validateNumberOfBlocks(final ExpDesignParameterUi expDesignParameterUi) throws DesignValidationException {

		if (!NumberUtils.isNumber(expDesignParameterUi.getNumberOfBlocks())) {
			throw new DesignValidationException(
					this.messageSource.getMessage("number.of.blocks.should.be.numeric", null, LocaleContextHolder.getLocale()));
		}

	}

}
