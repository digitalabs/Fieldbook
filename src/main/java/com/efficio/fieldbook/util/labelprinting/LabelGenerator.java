package com.efficio.fieldbook.util.labelprinting;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;

public interface LabelGenerator {

	/**
	 * The strategy to create labels
	 *
	 * @param dataList data used to create labels
	 * @param userLabelPrinting
	 * @return the name of the file with labels being created
	 * @throws LabelPrintingException
	 */
	String generateLabels(final List<?> dataList, final UserLabelPrinting userLabelPrinting) throws LabelPrintingException;

}
