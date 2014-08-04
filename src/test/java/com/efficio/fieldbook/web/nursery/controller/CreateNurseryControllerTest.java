package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.efficio.fieldbook.web.AbstractBaseControllerTest;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;

public class CreateNurseryControllerTest extends AbstractBaseControllerTest {
	
	@Autowired
	private CreateNurseryController controller;
	
	@Autowired
	private UserSelection userSelection; 
	
	@Test
	public void testGetReturnsCorrectModelAndView() throws Exception {
		
		ModelAndView mav = request(CreateNurseryController.URL, HttpMethod.GET.name());
		
		ModelAndViewAssert.assertViewName(mav, CreateNurseryController.BASE_TEMPLATE_NAME);
		ModelAndViewAssert.assertModelAttributeValue(mav, 
				AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, "/NurseryManager/createNursery");
		
		Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "createNurseryForm");	
	}
	
	@Test
	public void testSubmitWithMinimumRequiredFields() throws MiddlewareQueryException {
		
		CreateNurseryForm form = new CreateNurseryForm();

		SettingDetail studyName = new SettingDetail();
		SettingVariable studyNameVariable = new SettingVariable();
		studyNameVariable.setCvTermId(TermId.STUDY_NAME.getId());
		studyName.setVariable(studyNameVariable);
				
		SettingDetail studyObj = new SettingDetail();
		SettingVariable studyObjVariable = new SettingVariable();
		studyObjVariable.setCvTermId(TermId.STUDY_OBJECTIVE.getId());
		studyObj.setVariable(studyObjVariable);
		
		List<SettingDetail> basicDetails = Arrays.asList(studyName, studyObj);
		form.setBasicDetails(basicDetails);
		userSelection.setBasicDetails(basicDetails);
		
		this.userSelection.setStudyLevelConditions(new ArrayList<SettingDetail>());
		
		String submitResponse = controller.submit(form, new ExtendedModelMap());
		Assert.assertEquals("success", submitResponse);
	}
}
