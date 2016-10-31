package com.efficio.fieldbook.util.labelprinting;

import java.util.List;

import org.generationcp.middleware.pojos.GermplasmListData;

import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;

public interface SeedPreparationLabelGenerator {

	/** The strategy to create labels
	 *
	 * @param dataList data used to create labels
	 * @param userLabelPrinting
	 * @return the name of the file with labels being created
	 * @throws LabelPrintingException
	 */
	String generateLabels(final List<GermplasmListData> dataList, final UserLabelPrinting userLabelPrinting) throws LabelPrintingException;

}
