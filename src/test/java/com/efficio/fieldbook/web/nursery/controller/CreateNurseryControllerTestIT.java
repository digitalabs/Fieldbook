
package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.FieldbookServiceImpl;
import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.bean.PossibleValuesCache;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;

@Ignore(value ="BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
public class CreateNurseryControllerTestIT extends AbstractBaseIntegrationTest {

	@Autowired
	private CreateNurseryController controller;

	@Autowired
	private UserSelection userSelection;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Test
	public void testGet() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get(CreateNurseryController.URL))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.model().attribute(AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, "/NurseryManager/createNursery"));
	}

	@Test
	public void testSettingOfCheckVariablesInCreateNursery() {
		this.fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		FieldbookServiceImpl fieldbookServiceImpl = new FieldbookServiceImpl(this.fieldbookMiddlewareService, new PossibleValuesCache());
		this.setupMockReturns();
		this.controller.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		this.controller.setFieldbookService(fieldbookServiceImpl);

		CreateNurseryForm form = new CreateNurseryForm();
		ImportGermplasmListForm form2 = new ImportGermplasmListForm();
		Model model = Mockito.mock(Model.class);
		try {
			this.controller.show(form, form2, model, Mockito.mock(HttpSession.class), Mockito.mock(HttpServletRequest.class));
			Assert.assertNotNull(form2.getCheckVariables());
			Assert.assertTrue("Expected only check variables but the list has non check variables as well.",
					WorkbookTestUtil.areDetailsFilteredVariables(form2.getCheckVariables(), AppConstants.CHECK_VARIABLES.getString()));
		} catch (MiddlewareException e) {
			Assert.fail("Expected mock values but still called the middleware class");
		}
	}

	@Test
	public void testSettingOfCheckVariables() {
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		try {
			this.controller.setCheckVariablesInForm(form);
			Assert.assertNotNull(form.getCheckVariables());
			Assert.assertTrue("Expected only check variables but the list has non check variables as well.",
					WorkbookTestUtil.areDetailsFilteredVariables(form.getCheckVariables(), AppConstants.CHECK_VARIABLES.getString()));
		} catch (MiddlewareException e) {
			Assert.fail("Expected mock values but still called the middleware class");
		}
	}

	@Test
	public void testSettingOfCheckVariablesInUsePreviousNursery() {
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		Model model = Mockito.mock(Model.class);
		this.userSelection.setRemovedConditions(WorkbookTestUtil.createCheckVariables());
		this.controller.setUserSelection(this.userSelection);

		try {
			this.controller.getChecksForUseExistingNursery(form, -1, model, Mockito.mock(HttpSession.class), Mockito.mock(HttpServletRequest.class));
			Assert.assertNotNull(form.getCheckVariables());
			Assert.assertTrue("Expected only check variables but the list has non check variables as well.",
					WorkbookTestUtil.areDetailsFilteredVariables(form.getCheckVariables(), AppConstants.CHECK_VARIABLES.getString()));
		} catch (MiddlewareQueryException e) {
			Assert.fail("Expected mock values but still called the middleware class");
		}

	}

	@Test
	public void testSettingOfCheckVariablesInUsePreviousNurseryWhenRemovedConditionsIsNull() {
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		Model model = Mockito.mock(Model.class);
		this.userSelection.setRemovedConditions(null);
		this.controller.setUserSelection(this.userSelection);

		try {
			this.controller.getChecksForUseExistingNursery(form, -1, model, Mockito.mock(HttpSession.class), Mockito.mock(HttpServletRequest.class));
			Assert.assertNull(form.getCheckVariables());
		} catch (MiddlewareQueryException e) {
			Assert.fail("Expected mock values but still called the middleware class");
		}

	}

	private void setupMockReturns() {
		try {
			Mockito.when(this.fieldbookMiddlewareService.getPersonById(-1)).thenReturn(new Person());

			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.BREEDING_METHOD_CODE.getId(),
					Mockito.anyString())).thenReturn(
					this.createStandardVariable(TermId.BREEDING_METHOD_CODE.getId()));

			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),
					Mockito.anyString())).thenReturn(
					this.createStandardVariable(TermId.ENTRY_NO.getId()));
			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),
					Mockito.anyString())).thenReturn(
					this.createStandardVariable(TermId.DESIG.getId()));
			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.CROSS.getId(),
					Mockito.anyString())).thenReturn(
					this.createStandardVariable(TermId.CROSS.getId()));
			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),
					Mockito.anyString())).thenReturn(
					this.createStandardVariable(TermId.GID.getId()));
			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(),
					Mockito.anyString())).thenReturn(
					this.createStandardVariable(TermId.PLOT_NO.getId()));

			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_START.getId(),
					Mockito.anyString())).thenReturn(
					this.createStandardVariable(TermId.CHECK_START.getId()));
			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_INTERVAL.getId(),
					Mockito.anyString())).thenReturn(
					this.createStandardVariable(TermId.CHECK_INTERVAL.getId()));
			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_PLAN.getId(),Mockito.anyString())).thenReturn(
					this.createStandardVariable(TermId.CHECK_PLAN.getId()));

		} catch (MiddlewareException e) {
			Assert.fail("Expected mock returns but still called the middleware class.");
		}
	}

	private StandardVariable createStandardVariable(int id) {
		StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(null);
		return stdVar;
	}

	@Test
	public void testUseExistingNursery() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get(CreateNurseryController.URL + "/nursery/1"))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.model().attributeExists("createNurseryForm"))
			.andExpect(MockMvcResultMatchers.model().attribute(AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, "/NurseryManager/createNursery"));
	}

	@Test
	public void testAddErrorMessageToResult() throws Exception {
		CreateNurseryForm form = new CreateNurseryForm();

		this.controller.addErrorMessageToResult(form, new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(),
				"The term you entered is invalid"));

		Assert.assertEquals("Expecting error but did not get one", "1", form.getHasError());
	}

	@Test
	public void testSubmitWithMinimumRequiredFields() throws MiddlewareQueryException {

		CreateNurseryForm form = new CreateNurseryForm();

		SettingDetail studyName = new SettingDetail();

		List<SettingDetail> basicDetails = Arrays.asList(studyName);
		form.setBasicDetails(basicDetails);
		this.userSelection.setBasicDetails(basicDetails);

		this.userSelection.setStudyLevelConditions(new ArrayList<SettingDetail>());

		String submitResponse = this.controller.submit(form, new ExtendedModelMap());
		Assert.assertEquals("success", submitResponse);
	}

	@Test
	public void testSetLocationVariableValueWhenVariableIsLocationAndHasANonEmptyValue() throws MiddlewareQueryException {
		CreateNurseryController createNurseryController = new CreateNurseryController();
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService =
				Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		String locationId = "1001";

		Location location = new Location();
		location.setLocid(Integer.parseInt(locationId));

		Mockito.when(fieldbookMiddlewareService.getLocationById(Integer.parseInt(locationId))).thenReturn(location);
		SettingDetail detail = new SettingDetail();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.LOCATION_ID.getId());
		var.setValue(locationId);
		createNurseryController.setLocationVariableValue(detail, var);
		Assert.assertEquals("Should be able to set the value in the setting detail if its a location", locationId, detail.getValue());
	}

	@Test
	public void testSetLocationVariableValueWhenVariableIsLocationAndHasAnEmptyValue() throws MiddlewareQueryException {
		CreateNurseryController createNurseryController = new CreateNurseryController();
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService =
				Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		String locationId = "1001";

		Location location = new Location();
		location.setLocid(Integer.parseInt(locationId));

		Mockito.when(fieldbookMiddlewareService.getLocationById(Integer.parseInt(locationId))).thenReturn(location);
		SettingDetail detail = new SettingDetail();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.LOCATION_ID.getId());
		var.setValue("");
		createNurseryController.setLocationVariableValue(detail, var);
		Assert.assertEquals("Setting detail should have an empty value since the variable did not have a value itself", "",
				detail.getValue());
	}

	@Test
	public void testSetSettingDetailsValueFromVariableWhenVariableLocationIdIsANonEmptyValue() throws NumberFormatException,
	MiddlewareQueryException {
		CreateNurseryController createNurseryController = new CreateNurseryController();
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService =
				Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		String locationId = "1001";
		Location location = new Location();
		location.setLocid(Integer.parseInt(locationId));

		Mockito.when(fieldbookMiddlewareService.getLocationById(Integer.parseInt(locationId))).thenReturn(location);

		SettingDetail detail = new SettingDetail();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.LOCATION_ID.getId());
		var.setValue(locationId);

		createNurseryController.setSettingDetailsValueFromVariable(var, detail);
		Assert.assertEquals("Should be able to set the value in the setting detail if its a location", locationId, detail.getValue());
	}

	@Test
	public void testSetSettingDetailsValueFromVariableWhenVariableLocationIdIsAnEmptyValue() throws NumberFormatException,
	MiddlewareQueryException {
		CreateNurseryController createNurseryController = new CreateNurseryController();
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService =
				Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		String locationId = "1001";
		Location location = new Location();
		location.setLocid(Integer.parseInt(locationId));

		Mockito.when(fieldbookMiddlewareService.getLocationById(Integer.parseInt(locationId))).thenReturn(location);

		SettingDetail detail = new SettingDetail();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.LOCATION_ID.getId());
		var.setValue("");

		createNurseryController.setSettingDetailsValueFromVariable(var, detail);
		Assert.assertEquals("Setting detail should have an empty value if the location variable is an empty value", "", detail.getValue());
	}

	@Test
	public void testSetSettingDetailsValueFromVariableWhenVariableBreedingMethodCodeIsANonEmptyValue() throws NumberFormatException,
	MiddlewareQueryException {
		CreateNurseryController createNurseryController = new CreateNurseryController();
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService =
				Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		createNurseryController.setContextUtil(contextUtil);
		String bmCode = "TST_CODE";
		Integer bmId = 10001;
		String uniqueID = "6c87aaae-9e0f-428b-a364-44fab9fa7fd1";
		Method method = new Method();
		method.setMcode(bmCode);
		method.setMid(bmId);
		method.setUniqueID(uniqueID);

		Mockito.doReturn(uniqueID).when(contextUtil).getCurrentProgramUUID();
		Mockito.when(fieldbookMiddlewareService.getMethodByCode(bmCode, uniqueID)).thenReturn(method);

		SettingDetail detail = new SettingDetail();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.BREEDING_METHOD_CODE.getId());
		var.setValue(bmCode);

		createNurseryController.setSettingDetailsValueFromVariable(var, detail);
		Assert.assertEquals("Setting detail should the corresponding id of the matching method for the method code", bmId.toString(),
				detail.getValue());
	}
}
