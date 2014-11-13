/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package com.efficio.fieldbook.utils.test;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;

/**
 * The Class SettingsUtil.
 */
public class WorkbookTestUtil {

	public static boolean areDetailsFilteredVariables(List<SettingDetail> checkVariables, String variableList) {
		if (checkVariables != null) {
			for (SettingDetail var : checkVariables) {
				if (!variableList.contains(var.getVariable().getCvTermId().toString())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public static List<SettingDetail> createCheckVariables() {
		List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();
		
		checkVariables.add(createSettingDetail(TermId.CHECK_START.getId(), "1"));
		checkVariables.add(createSettingDetail(TermId.CHECK_INTERVAL.getId(), "4"));
		checkVariables.add(createSettingDetail(TermId.CHECK_PLAN.getId(), "8414"));
		
		return checkVariables;
	}

	private static SettingDetail createSettingDetail(int id, String value) {
		SettingDetail checkVar = new SettingDetail();
		SettingVariable var = new SettingVariable();
		var.setCvTermId(id);
		checkVar.setVariable(var);
		checkVar.setValue(value);
		return checkVar;
	}

}
