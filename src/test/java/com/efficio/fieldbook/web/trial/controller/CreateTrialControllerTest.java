package com.efficio.fieldbook.web.trial.controller;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import com.efficio.fieldbook.web.AbstractBaseControllerTest;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

public class CreateTrialControllerTest extends AbstractBaseControllerTest {

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
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "experimentalDesignData");
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "measurementRowCount");
		
	}
}
