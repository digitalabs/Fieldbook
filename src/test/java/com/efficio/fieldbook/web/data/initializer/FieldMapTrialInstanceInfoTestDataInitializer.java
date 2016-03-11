
package com.efficio.fieldbook.web.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.data.initializer.FieldMapLabelTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.pojos.GermplasmList;

import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;

public class FieldMapTrialInstanceInfoTestDataInitializer {

	public static final String TRIAL_INSTANCE_NO_STRING = "test1";
	public static final int NO_OF_ENTRIES = 1;
	
	GermplasmListTestDataInitializer germplasmListTestDataInitializer;
	
	public List<FieldMapTrialInstanceInfo> createTrialFieldMapList(final boolean isStockList) {
		this.germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();
		final List<FieldMapTrialInstanceInfo> trialFieldMap = new ArrayList<FieldMapTrialInstanceInfo>();
		final FieldMapTrialInstanceInfo fieldMapInfo1 = LabelPrintingDataUtil.createFieldMapThirdTrialInstanceInfo();

		if (isStockList) {
			final List<GermplasmList> germplasmLists = 
					this.germplasmListTestDataInitializer.createGermplasmListsWithType(FieldMapTrialInstanceInfoTestDataInitializer.NO_OF_ENTRIES);
			fieldMapInfo1.setStockList(germplasmLists.get(0));
		}

		fieldMapInfo1.setTrialInstanceNo(FieldMapTrialInstanceInfoTestDataInitializer.TRIAL_INSTANCE_NO_STRING);
		trialFieldMap.add(fieldMapInfo1);
		return trialFieldMap;
	}

	public static FieldMapTrialInstanceInfo createFieldMapTrialInstanceInfo() {
		final FieldMapTrialInstanceInfo instanceInfo = new FieldMapTrialInstanceInfo();
		instanceInfo.setFieldMapLabels(FieldMapLabelTestDataInitializer.createFieldMapLabelList());
		return instanceInfo;
	}
}
