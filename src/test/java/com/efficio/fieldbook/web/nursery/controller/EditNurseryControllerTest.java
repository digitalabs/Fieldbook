package com.efficio.fieldbook.web.nursery.controller;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import org.generationcp.middleware.domain.etl.*;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

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
        EditNurseryController moleEditNurseryController = spy(editNurseryController);

        Workbook workbook = mock(Workbook.class);
        List<SettingDetail> basicDetails = Arrays.asList(mock(SettingDetail.class));
        StudyDetails studyDetails = mock(StudyDetails.class);

        // setup: we dont care actually whats happening inside controller.useExistingNursery, we just want it to return the url
        when(studyDetails.getParentFolderId()).thenReturn((long) 1);
        when(workbook.getStudyDetails()).thenReturn(studyDetails);
        when(fieldbookMiddlewareService.getNurseryDataSet(anyInt())).thenReturn(workbook);

        doNothing().when(moleEditNurseryController).convertToXmlDatasetPojo(workbook);
        doReturn(basicDetails).when(moleEditNurseryController).updateRequiredFields(anyList(), anyList(), any(boolean[].class), anyList(), anyBoolean(), anyString());
        doReturn(basicDetails).when(moleEditNurseryController).getBasicDetails(basicDetails, createNurseryForm);
        doNothing().when(moleEditNurseryController).removeBasicDetailsVariables(basicDetails);
        doNothing().when(moleEditNurseryController).setFormStaticData(eq(createNurseryForm), anyString(), eq(workbook));

        // test
        String out = moleEditNurseryController.useExistingNursery(createNurseryForm, importGermplasmListForm, NURSERY_ID, "context-info", model, session, request, redirectAttributes);

        verify(fieldbookMiddlewareService).getNurseryDataSet(anyInt());
        assertEquals("should return the URL of the base_template", EditNurseryController.BASE_TEMPLATE_NAME, out);
    }

    @Test
    public void testUseExistingNurseryRedirectForIncompatibleStudy() throws Exception {
        EditNurseryController moleEditNurseryController = spy(editNurseryController);

        when(request.getCookies()).thenReturn(new Cookie[]{});
        doReturn("context-info").when(moleEditNurseryController).retrieveContextInfo(request);
        doNothing().when(moleEditNurseryController).clearSessionData(session);
        when(fieldbookMiddlewareService.getNurseryDataSet(NURSERY_ID)).thenThrow(MiddlewareQueryException.class);

        String out = moleEditNurseryController.useExistingNursery(createNurseryForm, importGermplasmListForm, NURSERY_ID, "context-info", model, session, request, redirectAttributes);
        assertEquals("should redirect to manage nurseries page", "redirect:" + ManageNurseriesController.URL, out);

        // assert that we should have produced a redirectErrorMessage
        ArgumentCaptor<String> redirectArg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> redirectArg2 = ArgumentCaptor.forClass(String.class);

        verify(redirectAttributes).addFlashAttribute(redirectArg1.capture(), redirectArg2.capture());
        assertEquals("value should be redirectErrorMessage", "redirectErrorMessage", redirectArg1.getValue());
    }

    @Test
    public void testCheckMeasurementData() throws Exception {
        initializeMeasurementRowList();

        Map<String, String> result1 = editNurseryController.checkMeasurementData(createNurseryForm, model, 0, DEFAULT_TERM_ID);

        assertTrue("the result of map with key HAS_MEASUREMENT_DATA_STR should be '1' ", result1.get(EditNurseryController.HAS_MEASUREMENT_DATA_STR).equals(EditNurseryController.SUCCESS));

        Map<String, String> result2 = editNurseryController.checkMeasurementData(createNurseryForm, model, 0, DEFAULT_TERM_ID_2);

        assertTrue("the result of map with key HAS_MEASUREMENT_DATA_STR should be '0' ", result2.get(EditNurseryController.HAS_MEASUREMENT_DATA_STR).equals(EditNurseryController.NO_MEASUREMENT));
    }

    @Test
    public void testHasMeasurementDataEntered() throws Exception {
        initializeMeasurementRowList();

        assertTrue("We should have a measurementVariable in the datalist with DEFAULT_TERM_ID", editNurseryController.hasMeasurementDataEntered(DEFAULT_TERM_ID));
        assertFalse("NOT_EXIST_TERM_ID should not exist in the measurement data list", editNurseryController.hasMeasurementDataEntered(NOT_EXIST_TERM_ID));
        assertFalse("DEFAULT_TERM_ID_2 has measurement data without value (null)", editNurseryController.hasMeasurementDataEntered(DEFAULT_TERM_ID_2));
    }

	@Test
	public void testSettingOfCheckVariablesInEditNursery() {
		CreateNurseryForm form = new CreateNurseryForm();
		ImportGermplasmListForm form2 = new ImportGermplasmListForm();
		List<SettingDetail> removedConditions = WorkbookTestUtil.createCheckVariables();
		editNurseryController.setCheckVariables(removedConditions, form2, form);

		assertNotNull(form2.getCheckVariables());
		assertTrue("Expected check variables but the list does not have all check variables.",
				WorkbookTestUtil.areDetailsFilteredVariables(form2.getCheckVariables(), AppConstants.CHECK_VARIABLES.getString()));
	}

    @Test
    public void testGetNurseryFolderName() throws Exception {
        // case nurseryId = positive
        String out = editNurseryController.getNurseryFolderName(ROOT_FOLDER_ID, PUBLIC_NURSERY_ID);
        verify(fieldbookMiddlewareService,never()).getFolderNameById(ROOT_FOLDER_ID);
        assertEquals("should be public folder", AppConstants.PUBLIC_NURSERIES.getString(), out);

        // case nurseryId = negative (local)
        String out2 = editNurseryController.getNurseryFolderName(ROOT_FOLDER_ID, LOCAL_NURSERY_ID);
        verify(fieldbookMiddlewareService,never()).getFolderNameById(ROOT_FOLDER_ID);
        assertEquals("should be a program local folder", AppConstants.PROGRAM_NURSERIES.getString(), out2);

        // case not a root folder
        when(fieldbookMiddlewareService.getFolderNameById(anyInt())).thenReturn(anyString());
        editNurseryController.getNurseryFolderName(CHILD_FOLDER_ID, LOCAL_NURSERY_ID);
        verify(fieldbookMiddlewareService).getFolderNameById(CHILD_FOLDER_ID);
    }

    private void initializeMeasurementRowList() {
        final Random random = new Random(1000);
        // random numbers generated up-to 3 digits only so as not to conflict with test data
        List<MeasurementData> measurementDataList = Arrays.asList(generateMockedMeasurementData(random.nextInt(100), Integer.toString(random.nextInt(100))),
                generateMockedMeasurementData(random.nextInt(100), Integer.toString(random.nextInt(100))), generateMockedMeasurementData(DEFAULT_TERM_ID, Integer.toString(DEFAULT_TERM_ID)),
                generateMockedMeasurementData(DEFAULT_TERM_ID_2, null));

        MeasurementRow measurmentRow = mock(MeasurementRow.class);
        List<MeasurementRow> measurementRowList = Arrays.asList(measurmentRow);
        when(measurmentRow.getDataList()).thenReturn(measurementDataList);
        when(userSelection.getMeasurementRowList()).thenReturn(measurementRowList);
    }

    private MeasurementData generateMockedMeasurementData(int termID, String value) {
        MeasurementData measurementData = mock(MeasurementData.class);

        MeasurementVariable measurementVariable = new MeasurementVariable();
        measurementVariable.setTermId(termID);

        when(measurementData.getMeasurementVariable()).thenReturn(measurementVariable);
        when(measurementData.getValue()).thenReturn(value);

        return measurementData;
    }
}
