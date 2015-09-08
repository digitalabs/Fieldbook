package com.efficio.fieldbook.web.nursery.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.generationcp.middleware.domain.etl.*;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;

@RunWith(MockitoJUnitRunner.class)
public class EditNurseryControllerTest {

	public static final int ROOT_FOLDER_ID = 1;
	public static final int PUBLIC_NURSERY_ID = 1;
	public static final int LOCAL_NURSERY_ID = -1;
	private static final int DEFAULT_TERM_ID = 1234;
	private static final int NOT_EXIST_TERM_ID = 2345;
	private static final int DEFAULT_TERM_ID_2 = 3456;
	private static final int NURSERY_ID = 1;
	public static final int CHILD_FOLDER_ID = 2;
	@Mock
	HttpServletRequest request;

	@Mock
	HttpSession session;

	@Mock
	CreateNurseryForm createNurseryForm;

	@Mock
	ImportGermplasmListForm importGermplasmListForm;

	@Mock
	Model model;

	@Mock
	RedirectAttributes redirectAttributes;

	@Mock
	UserSelection userSelection;

	@Mock
	org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	ErrorHandlerService errorHandlerService;

	@InjectMocks
	EditNurseryController editNurseryController;

	@Test
	public void testUseExistingNurseryNoRedirect() throws Exception {
		EditNurseryController moleEditNurseryController = Mockito.spy(this.editNurseryController);

		Workbook workbook = Mockito.mock(Workbook.class);
		List<SettingDetail> basicDetails = Arrays.asList(Mockito.mock(SettingDetail.class));
		StudyDetails studyDetails = Mockito.mock(StudyDetails.class);

		// setup: we dont care actually whats happening inside controller.useExistingNursery, we just want it to return the url
		Mockito.when(studyDetails.getParentFolderId()).thenReturn((long) 1);
		Mockito.when(workbook.getStudyDetails()).thenReturn(studyDetails);
		Mockito.when(this.fieldbookMiddlewareService.getNurseryDataSet(Matchers.anyInt())).thenReturn(workbook);

		Mockito.doNothing().when(moleEditNurseryController).convertToXmlDatasetPojo(workbook);
		Mockito.doReturn(basicDetails)
				.when(moleEditNurseryController)
				.updateRequiredFields(Matchers.anyList(), Matchers.anyList(), Matchers.any(boolean[].class), Matchers.anyList(),
						Matchers.anyBoolean(), Matchers.anyString(), Matchers.anyString());
		Mockito.doReturn(basicDetails).when(moleEditNurseryController)
				.getSettingDetailsOfSection(basicDetails, this.createNurseryForm, AppConstants.FIXED_NURSERY_VARIABLES.getString());
		Mockito.doNothing().when(moleEditNurseryController)
				.setCheckVariables(Matchers.anyList(), Matchers.any(ImportGermplasmListForm.class), Matchers.eq(this.createNurseryForm));
		Mockito.doNothing().when(moleEditNurseryController).removeBasicDetailsVariables(basicDetails);
		Mockito.doNothing().when(moleEditNurseryController)
				.setFormStaticData(Matchers.eq(this.createNurseryForm), Matchers.anyString(), Matchers.eq(workbook));
		// test
		String out =
				moleEditNurseryController.useExistingNursery(this.createNurseryForm, this.importGermplasmListForm,
						EditNurseryControllerTest.NURSERY_ID, "context-info", this.model, this.request, this.redirectAttributes);

		Mockito.verify(this.fieldbookMiddlewareService).getNurseryDataSet(Matchers.anyInt());
		Assert.assertEquals("should return the URL of the base_template", AbstractBaseFieldbookController.BASE_TEMPLATE_NAME, out);
	}

	@Test
	public void testUseExistingNurseryRedirectForIncompatibleStudy() throws Exception {
		EditNurseryController moleEditNurseryController = Mockito.spy(this.editNurseryController);

		Mockito.when(this.request.getCookies()).thenReturn(new Cookie[] {});
		Mockito.doReturn("context-info").when(moleEditNurseryController).retrieveContextInfo(this.request);
		Mockito.doNothing().when(moleEditNurseryController).clearSessionData(this.session);
		Mockito.when(this.fieldbookMiddlewareService.getNurseryDataSet(EditNurseryControllerTest.NURSERY_ID)).thenThrow(
				MiddlewareQueryException.class);

		String out =
				moleEditNurseryController.useExistingNursery(this.createNurseryForm, this.importGermplasmListForm,
						EditNurseryControllerTest.NURSERY_ID, "context-info", this.model, this.request, this.redirectAttributes);
		Assert.assertEquals("should redirect to manage nurseries page", "redirect:" + ManageNurseriesController.URL, out);

		// assert that we should have produced a redirectErrorMessage
		ArgumentCaptor<String> redirectArg1 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> redirectArg2 = ArgumentCaptor.forClass(String.class);

		Mockito.verify(this.redirectAttributes).addFlashAttribute(redirectArg1.capture(), redirectArg2.capture());
		Assert.assertEquals("value should be redirectErrorMessage", "redirectErrorMessage", redirectArg1.getValue());
	}

	@Test
	public void testCheckMeasurementData() throws Exception {
		this.initializeMeasurementRowList();

		Map<String, String> result1 =
				this.editNurseryController.checkMeasurementData(this.createNurseryForm, this.model, 0,
						Integer.toString(EditNurseryControllerTest.DEFAULT_TERM_ID));

		Assert.assertTrue("the result of map with key HAS_MEASUREMENT_DATA_STR should be '1' ",
				result1.get(EditNurseryController.HAS_MEASUREMENT_DATA_STR).equals(EditNurseryController.SUCCESS));

		Map<String, String> result2 =
				this.editNurseryController.checkMeasurementData(this.createNurseryForm, this.model, 0,
						Integer.toString(EditNurseryControllerTest.DEFAULT_TERM_ID_2));

		Assert.assertTrue("the result of map with key HAS_MEASUREMENT_DATA_STR should be '0' ",
				result2.get(EditNurseryController.HAS_MEASUREMENT_DATA_STR).equals(EditNurseryController.NO_MEASUREMENT));
	}

	@Test
	public void testHasMeasurementDataEntered() throws Exception {
		this.initializeMeasurementRowList();

		Assert.assertTrue("We should have a measurementVariable in the datalist with DEFAULT_TERM_ID",
				this.editNurseryController.hasMeasurementDataEntered(EditNurseryControllerTest.DEFAULT_TERM_ID));
		Assert.assertFalse("NOT_EXIST_TERM_ID should not exist in the measurement data list",
				this.editNurseryController.hasMeasurementDataEntered(EditNurseryControllerTest.NOT_EXIST_TERM_ID));
		Assert.assertFalse("DEFAULT_TERM_ID_2 has measurement data without value (null)",
				this.editNurseryController.hasMeasurementDataEntered(EditNurseryControllerTest.DEFAULT_TERM_ID_2));
	}

	@Test
	public void testSettingOfCheckVariablesInEditNursery() {
		CreateNurseryForm form = new CreateNurseryForm();
		ImportGermplasmListForm form2 = new ImportGermplasmListForm();
		List<SettingDetail> removedConditions = WorkbookTestUtil.createCheckVariables();
		this.editNurseryController.setCheckVariables(removedConditions, form2, form);

		Assert.assertNotNull(form2.getCheckVariables());
		Assert.assertTrue("Expected check variables but the list does not have all check variables.",
				WorkbookTestUtil.areDetailsFilteredVariables(form2.getCheckVariables(), AppConstants.CHECK_VARIABLES.getString()));
	}

	@Test
	public void testGetNurseryFolderName() throws Exception {
		// case folder id = root folder
		String out = this.editNurseryController.getNurseryFolderName(EditNurseryControllerTest.ROOT_FOLDER_ID);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.never()).getFolderNameById(EditNurseryControllerTest.ROOT_FOLDER_ID);
		Assert.assertEquals("should be Nurseries", AppConstants.NURSERIES.getString(), out);

		// case not a root folder
		Mockito.when(this.fieldbookMiddlewareService.getFolderNameById(Matchers.anyInt())).thenReturn(Matchers.anyString());
		this.editNurseryController.getNurseryFolderName(EditNurseryControllerTest.CHILD_FOLDER_ID);
		Mockito.verify(this.fieldbookMiddlewareService).getFolderNameById(EditNurseryControllerTest.CHILD_FOLDER_ID);
	}

	private void initializeMeasurementRowList() {
		final Random random = new Random(1000);
		// random numbers generated up-to 3 digits only so as not to conflict with test data
		List<MeasurementData> measurementDataList =
				Arrays.asList(
						this.generateMockedMeasurementData(random.nextInt(100), Integer.toString(random.nextInt(100))),
						this.generateMockedMeasurementData(random.nextInt(100), Integer.toString(random.nextInt(100))),
						this.generateMockedMeasurementData(EditNurseryControllerTest.DEFAULT_TERM_ID,
								Integer.toString(EditNurseryControllerTest.DEFAULT_TERM_ID)),
						this.generateMockedMeasurementData(EditNurseryControllerTest.DEFAULT_TERM_ID_2, null));

		MeasurementRow measurmentRow = Mockito.mock(MeasurementRow.class);
		List<MeasurementRow> measurementRowList = Arrays.asList(measurmentRow);
		Mockito.when(measurmentRow.getDataList()).thenReturn(measurementDataList);
		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(measurementRowList);
	}

	private MeasurementData generateMockedMeasurementData(int termID, String value) {
		MeasurementData measurementData = Mockito.mock(MeasurementData.class);

		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termID);

		Mockito.when(measurementData.getMeasurementVariable()).thenReturn(measurementVariable);
		Mockito.when(measurementData.getValue()).thenReturn(value);

		return measurementData;
	}
}
