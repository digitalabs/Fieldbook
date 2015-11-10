
package com.efficio.fieldbook.web.data.initializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.efficio.pojos.labelprinting.LabelPrintingProcessingParams;

public class LabelPrintingProcessingParamsTestDataInitializer {

	private static final int FIELD_ID = 8250;

	public static LabelPrintingProcessingParams createLabelPrintingProcessingParams() {

		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setLabelHeaders(new HashMap<Integer, String>());

		return params;

	}

	public static LabelPrintingProcessingParams createLabelPrintingProcessingParamsWithAllFieldIDs() {

		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setLabelHeaders(new HashMap<Integer, String>());

		final List<Integer> allFieldIdList = new ArrayList<Integer>();
		allFieldIdList.add(LabelPrintingProcessingParamsTestDataInitializer.FIELD_ID);

		params.setAllFieldIDs(allFieldIdList);

		return params;
	}
}
