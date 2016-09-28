package com.efficio.fieldbook.web.experimentdesign;

import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.service.ExperimentDesignService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ExperimentDesignValidator {

	@Resource
	private MessageSource messageSource;

	public void validateAugmentedDesign(ExpDesignParameterUi expDesignParameterUi, List<ImportedGermplasm> importedGermplasmList)
			throws DesignValidationException {

		if (expDesignParameterUi != null && importedGermplasmList != null) {

			int treatmentSize = importedGermplasmList.size();

			validateIfCheckEntriesExistInGermplasmList(importedGermplasmList);
			validateStartingPlotNo(expDesignParameterUi, treatmentSize);
			validateStartingEntryNo(expDesignParameterUi, treatmentSize);
			validateNumberOfBlocks(expDesignParameterUi);

		}

	}

	void validateIfCheckEntriesExistInGermplasmList(List<ImportedGermplasm> importedGermplasmList) throws DesignValidationException {

		for (ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())) {
				return;
			}
		}

		throw new DesignValidationException(
				this.messageSource.getMessage("germplasm.list.check.required.augmented.design", null, LocaleContextHolder.getLocale()));

	}

	void validateStartingPlotNo(ExpDesignParameterUi expDesignParameterUi, int treatmentSize) throws DesignValidationException {

		String startingPlotNo = expDesignParameterUi.getStartingPlotNo();

		if (startingPlotNo != null && NumberUtils.isNumber(startingPlotNo)) {
			Integer plotNumber = Integer.valueOf(startingPlotNo);
			if (plotNumber != 0 && ((treatmentSize + plotNumber) <= ExperimentDesignService.MAX_STARTING_ENTRY_PLOT_NO)) {
				return;
			}
		}

		throw new DesignValidationException(
				this.messageSource.getMessage("plot.number.should.be.in.range", null, LocaleContextHolder.getLocale()));

	}

	void validateStartingEntryNo(ExpDesignParameterUi expDesignParameterUi, int treatmentSize) throws DesignValidationException {

		String startingEntryNo = expDesignParameterUi.getStartingEntryNo();

		if (startingEntryNo != null && NumberUtils.isNumber(startingEntryNo)) {
			Integer entryNumber = Integer.valueOf(startingEntryNo);
			if (entryNumber != 0 && ((treatmentSize + entryNumber) <= ExperimentDesignService.MAX_STARTING_ENTRY_PLOT_NO)) {
				return;
			}
		}

		throw new DesignValidationException(
				this.messageSource.getMessage("entry.number.should.be.in.range", null, LocaleContextHolder.getLocale()));

	}

	void validateNumberOfBlocks(ExpDesignParameterUi expDesignParameterUi) throws DesignValidationException {

		if (!NumberUtils.isNumber(expDesignParameterUi.getNumberOfBlocks())) {
			throw new DesignValidationException(
					this.messageSource.getMessage("number.of.blocks.should.be.numeric", null, LocaleContextHolder.getLocale()));
		}

	}

}
