
package com.efficio.fieldbook.web.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.data.initializer.FieldMapLabelTestDataInitializer;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;

public class FieldMapTrialInstanceInfoTestDataInitializer {

	public static final String TRIAL_INSTANCE_NO_STRING = "test1";

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
