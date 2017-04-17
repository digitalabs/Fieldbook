
package com.efficio.fieldbook.web.data.initializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.pojos.labelprinting.LabelPrintingProcessingParams;

public class LabelPrintingProcessingParamsTestDataInitializer {

	private static final int FIELD_ID = 8250;

	public static LabelPrintingProcessingParams createLabelPrintingProcessingParams() {

		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setLabelHeaders(new HashMap<Integer, String>());
		params.setAllFieldIDs(new ArrayList<Integer>());

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

	public static LabelPrintingProcessingParams createLabelPrintingProcessingParamsWithGermplsmDescriptorsFields() {

		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setLabelHeaders(new HashMap<Integer, String>());

		final List<Integer> allFieldIdList = new ArrayList<Integer>();
		allFieldIdList.add(TermId.GROUPGID.getId());
		allFieldIdList.add(TermId.SEED_SOURCE.getId());

		params.setAllFieldIDs(allFieldIdList);

		return params;
	}
}
