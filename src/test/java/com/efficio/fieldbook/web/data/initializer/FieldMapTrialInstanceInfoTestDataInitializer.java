
package com.efficio.fieldbook.web.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.data.initializer.FieldMapLabelTestDataInitializer;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;

import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;

public class FieldMapTrialInstanceInfoTestDataInitializer {

	public static final String TRIAL_INSTANCE_NO_STRING = "test1";

	public static List<FieldMapTrialInstanceInfo> createTrialFieldMapList() {
		final List<FieldMapTrialInstanceInfo> trialFieldMap = new ArrayList<>();
		final FieldMapTrialInstanceInfo fieldMapInfo1 = LabelPrintingDataUtil.createFieldMapThirdTrialInstanceInfo();

		fieldMapInfo1.setTrialInstanceNo(FieldMapTrialInstanceInfoTestDataInitializer.TRIAL_INSTANCE_NO_STRING);
		trialFieldMap.add(fieldMapInfo1);
		return trialFieldMap;
	}

	public static FieldMapTrialInstanceInfo createFieldMapTrialInstanceInfo() {
			final FieldMapTrialInstanceInfo instanceInfo = new FieldMapTrialInstanceInfo();
			instanceInfo.setFieldMapLabels(FieldMapLabelTestDataInitializer.createFieldMapLabelList());
			return instanceInfo;
	}

	public static FieldMapTrialInstanceInfo createFieldMapTrialInstanceInfo(final int numberOfObservations) {
		final FieldMapTrialInstanceInfo instanceInfo = new FieldMapTrialInstanceInfo();
		instanceInfo.setFieldMapLabels(FieldMapLabelTestDataInitializer.createFieldMapLabelList(numberOfObservations));
		return instanceInfo;
	}
}
