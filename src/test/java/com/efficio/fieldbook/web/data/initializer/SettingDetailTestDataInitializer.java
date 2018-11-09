
package com.efficio.fieldbook.web.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;

public class SettingDetailTestDataInitializer {

	public static SettingDetail createSettingDetail(final int cvTermId, final String name, final String value,
			final PhenotypicType role) {
		final SettingDetail settingDetail = SettingDetailTestDataInitializer.createSettingDetail(cvTermId, name, value, role.name());
		settingDetail.setRole(role);
		return settingDetail;
	}
	
	public static SettingDetail createSettingDetail(final int cvTermId, final String name, final String value,
			final String label) {

		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(cvTermId);
		variable.setName(name);
		variable.setRole(label);
		final SettingDetail settingDetail = new SettingDetail(variable, null, value, false);

		if (cvTermId == TermId.CHECK_PLAN.getId()) {

			final List<ValueReference> possibleValues = new ArrayList<ValueReference>();
			possibleValues.add(new ValueReference(8414, "1", "Insert each check in turn"));
			possibleValues.add(new ValueReference(8415, "2", "Insert all checks at each position"));
			settingDetail.setPossibleValues(possibleValues);

		} else if (cvTermId == TermId.CHECK_INTERVAL.getId()) {
			settingDetail.setPossibleValues(new ArrayList<ValueReference>());
		}

		return settingDetail;
	}

	public static SettingDetail createSettingDetail(final Integer cvTermId) {

		final SettingDetail settingDetail = new SettingDetail();
		final SettingVariable settingVariable = new SettingVariable();
		settingVariable.setCvTermId(cvTermId);
		settingDetail.setVariable(settingVariable);

		return settingDetail;
	}

}
