package com.efficio.fieldbook.web.trial.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import com.efficio.fieldbook.web.AbstractBaseControllerTest;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;

public class CreateTrialControllerTest extends AbstractBaseControllerTest {
	
	@Autowired
	private CreateTrialController controller;
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Test
	public void testGetReturnsCorrectModelAndView() throws Exception {
		
		ModelAndView mav = request(CreateTrialController.URL, HttpMethod.GET.name());
		
		ModelAndViewAssert.assertViewName(mav, CreateTrialController.ANGULAR_BASE_TEMPLATE_NAME);
		ModelAndViewAssert.assertModelAttributeValue(mav, 
				AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, "TrialManager/createTrial");
		
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "basicDetailsData");
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "germplasmData");
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "environmentData");
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "trialSettingsData");
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "measurementRowCount");
		
	}
	
	@Test
	public void testUseExistingTrial() throws Exception {
		fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		controller.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		Mockito.when(fieldbookMiddlewareService.getTrialDataSet(1))
			.thenThrow(new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"));
		
		Map<String, Object> tabDetails = controller.getExistingTrialDetails(1);
		
		Assert.assertNotNull("Expecting error but did not get one", tabDetails.get("createTrialForm"));
		
		CreateTrialForm form = (CreateTrialForm) tabDetails.get("createTrialForm");
		Assert.assertTrue("Expecting error but did not get one", form.isHasError());
	}	
}
