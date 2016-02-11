
package com.efficio.fieldbook.service.initializer;

import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.oms.TermId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelPrintingServiceDataInitializer {

	public static final int TEST_GEOLOCATION_ID = 1;
	public static final String TEST_SITE_NAME = "IRRI";
	public static final int TEST_FIELD_MAP_LABEL_SIZE = 20;
	public static final int TEST_REP_NO = 2;
	public static final int TEST_EXPERIMENT_NUMBER = 1;
    public static final String TEST_FIELDBOOK_NAME = "Test";

	public static List<StudyTrialInstanceInfo> generateStudyTrialInstanceInfoList() {
        final List<StudyTrialInstanceInfo> infoList = new ArrayList<>();
        final FieldMapTrialInstanceInfo fieldMapInfo = new FieldMapTrialInstanceInfo(TEST_GEOLOCATION_ID, TEST_SITE_NAME, generateTestFieldMapLabels());
        final StudyTrialInstanceInfo info = new StudyTrialInstanceInfo(fieldMapInfo, TEST_FIELDBOOK_NAME);
        infoList.add(info);

        return infoList;

    }

	public static List<FieldMapLabel> generateTestFieldMapLabels() {
		final List<FieldMapLabel> labelList = new ArrayList<>();


		for (int i = 0; i < TEST_FIELD_MAP_LABEL_SIZE; i++) {

			final FieldMapLabel label = new FieldMapLabel(TEST_EXPERIMENT_NUMBER, i, "germplasm" + i, TEST_REP_NO, i);
            final Map<Integer, String> userFields = new HashMap<>();
            userFields.put(TermId.ENTRY_NO.getId(), Integer.toString(i));
            label.setUserFields(userFields);
			labelList.add(label);
		}

		return labelList;

	}
}
