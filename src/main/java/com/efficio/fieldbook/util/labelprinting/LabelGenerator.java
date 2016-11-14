package com.efficio.fieldbook.util.labelprinting;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;

public interface LabelGenerator {

	/**
	 * The strategy to create labels
	 *
	 * @param dataList data used to create labels
	 * @param userLabelPrinting the information needed for label printing
	 * @param numberOfCopies how many times the same label should be printed
	 * @return the name of the file with labels being created
	 * @throws LabelPrintingException if labels could not be printed for some reasons
	 */
	String generateLabels(final List<StudyTrialInstanceInfo> dataList, final UserLabelPrinting userLabelPrinting, final int numberOfCopies) throws
			LabelPrintingException;

}
