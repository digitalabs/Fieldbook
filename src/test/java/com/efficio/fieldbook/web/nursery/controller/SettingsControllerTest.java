package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;

public class SettingsControllerTest extends AbstractBaseControllerIntegrationTest {
	
	private SettingsController controller;
	
	@Before
	public void setup () {
		controller = Mockito.mock(SettingsController.class);
	}
		
	@Test
	public void testGetCheckVariables() {
		List<SettingDetail> nurseryLevelConditions = createSettingDetailVariables();
		CreateNurseryForm form = new CreateNurseryForm();
		
		List<SettingDetail> checkVariables = controller.getCheckVariables(nurseryLevelConditions, form);
		
		Assert.assertTrue("Expected only check variables but the list has non check variables as well.", 
				WorkbookTestUtil.areDetailsFilteredVariables(checkVariables, AppConstants.CHECK_VARIABLES.getString()));
	}
	
	@Test
	public void testGetBasicDetails() {
		List<SettingDetail> nurseryLevelConditions = createSettingDetailVariables();
		CreateNurseryForm form = new CreateNurseryForm();
		
		List<SettingDetail> basicDetails = controller.getSettingDetailsOfSection(nurseryLevelConditions, form, AppConstants.FIXED_NURSERY_VARIABLES.getString());
		
		Assert.assertTrue("Expected only basic detail variables but the list has non basic detail variables as well.", 
				WorkbookTestUtil.areDetailsFilteredVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString()));
	}

	private List<SettingDetail> createSettingDetailVariables() {
		List<SettingDetail> variables = new ArrayList<SettingDetail>();
		variables.add(createSettingDetail(TermId.STUDY_NAME.getId(), ""));
		variables.add(createSettingDetail(TermId.STUDY_TITLE.getId(), ""));
		variables.add(createSettingDetail(TermId.START_DATE.getId(), ""));
		variables.add(createSettingDetail(TermId.STUDY_OBJECTIVE.getId(), ""));
		variables.add(createSettingDetail(TermId.END_DATE.getId(), ""));
		variables.add(createSettingDetail(TermId.STUDY_UID.getId(), ""));
		variables.add(createSettingDetail(TermId.STUDY_UPDATE.getId(), ""));
		variables.add(createSettingDetail(TermId.TRIAL_INSTANCE_FACTOR.getId(), ""));
		variables.add(createSettingDetail(TermId.PI_NAME.getId(), ""));
		variables.add(createSettingDetail(TermId.PI_ID.getId(), ""));
		variables.add(createSettingDetail(TermId.CHECK_INTERVAL.getId(), ""));
		variables.add(createSettingDetail(TermId.CHECK_PLAN.getId(), ""));
		variables.add(createSettingDetail(TermId.CHECK_START.getId(), ""));
		return variables;
	}

	private SettingDetail createSettingDetail(Integer cvTermId, String value) {
		SettingVariable variable = new SettingVariable();
		variable.setCvTermId(cvTermId);
		SettingDetail settingDetail = new SettingDetail(variable, null, value, false);
		return settingDetail;
	}

		
}
