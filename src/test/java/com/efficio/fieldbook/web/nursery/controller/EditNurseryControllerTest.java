package com.efficio.fieldbook.web.nursery.controller;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.AbstractBaseControllerTest;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;

public class EditNurseryControllerTest extends AbstractBaseControllerTest {
	
	@Autowired
	private EditNurseryController controller;
	
	@Autowired
	private UserSelection userSelection;
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Test
	public void testSettingOfCheckVariablesInEditNursery() {
		CreateNurseryForm form = new CreateNurseryForm();
		ImportGermplasmListForm form2 = new ImportGermplasmListForm();
		List<SettingDetail> removedConditions = WorkbookTestUtil.createCheckVariables();
		controller.setCheckVariables(removedConditions, form2, form);
		
		Assert.assertNotNull(form2.getCheckVariables());
		Assert.assertTrue("Expected check variables but the list does not have all check variables.", 
				WorkbookTestUtil.areDetailsFilteredVariables(form2.getCheckVariables(), AppConstants.CHECK_VARIABLES.getString()));
	}
}
