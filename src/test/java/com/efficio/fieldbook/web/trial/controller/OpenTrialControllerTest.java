package com.efficio.fieldbook.web.trial.controller;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.TabInfo;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.SessionUtility;

@RunWith(MockitoJUnitRunner.class)
public class OpenTrialControllerTest {
	private static final int NO_OF_TRIAL_INSTANCES = 3;
	private static final int NO_OF_OBSERVATIONS = 5;
	private static final int TRIAL_ID = 1;
	public static final String TEST_TRIAL_NAME = "dummyTrial";

	@Mock
	private UserSelection userSelection;

	@Mock
	private HttpSession session;

	@Mock
	private CreateTrialForm createTrialForm;

	@Mock
	private Workbook trialWorkbook;

	@Mock
	private Model model;

	@Mock
	private RedirectAttributes redirectAttributes;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private ErrorHandlerService errorHandlerService;

	@InjectMocks
	private OpenTrialController openTrialController;

	@Test
	public void testOpenTrialNoRedirect() throws Exception {
		final OpenTrialController moleOpenTrialController = setupOpenTrialController();
		Workbook workbook = mock(Workbook.class);
		when(fieldbookService.getTrialDataSet(TRIAL_ID)).thenReturn(workbook);

		doNothing().when(moleOpenTrialController)
				.setModelAttributes(createTrialForm, TRIAL_ID, model, workbook);

		String out = moleOpenTrialController
				.openTrial(createTrialForm, TRIAL_ID, model, session, redirectAttributes);

		verify(fieldbookService).getTrialDataSet(TRIAL_ID);
		assertEquals("should return the base angular template",
				OpenTrialController.ANGULAR_BASE_TEMPLATE_NAME, out);
	}

	@Test
	public void testOpenTrialRedirectForIncompatibleStudy() throws Exception {
		final OpenTrialController moleOpenTrialController = setupOpenTrialController();

		when(fieldbookService.getTrialDataSet(TRIAL_ID)).thenThrow(MiddlewareQueryException.class);

		String out = moleOpenTrialController
				.openTrial(createTrialForm, TRIAL_ID, model, session, redirectAttributes);

		assertEquals("should redirect to manage trial page",
				"redirect:" + ManageTrialController.URL, out);

		ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);

		verify(redirectAttributes).addFlashAttribute(arg1.capture(), arg2.capture());
		assertEquals("value should be redirectErrorMessage", "redirectErrorMessage",
				arg1.getValue());

	}

	protected void prepareHappyPathScenario(OpenTrialController mocked) {
		try {
			doReturn(new TabInfo()).when(mocked)
					.prepareBasicDetailsTabInfo(any(StudyDetails.class), anyBoolean(), anyInt());
			doReturn(new TabInfo()).when(mocked).prepareGermplasmTabInfo(anyList(),
					anyBoolean());
			doReturn(new TabInfo()).when(mocked)
					.prepareEnvironmentsTabInfo(any(Workbook.class), anyBoolean());
			doReturn(new TabInfo()).when(mocked).prepareTrialSettingsTabInfo(anyList(),
					anyBoolean());
			doReturn(new TabInfo()).when(mocked).prepareMeasurementsTabInfo(anyList(),
					anyBoolean());
			doReturn(new TabInfo()).when(mocked).prepareExperimentalDesignTabInfo(
					any(ExperimentalDesignVariable.class), anyBoolean());
			when(fieldbookService.checkIfStudyHasMeasurementData(anyInt(), anyList()))
					.thenReturn(true);
			when(trialWorkbook.getObservations()).thenReturn(new ArrayList<MeasurementRow>());

			doReturn(new TabInfo()).when(mocked).prepareTreatmentFactorsInfo(anyList(),
					anyBoolean());
			doReturn(new TabInfo()).when(mocked).prepareExperimentalDesignSpecialData();

			StudyDetails studyDetails = new StudyDetails();
			studyDetails.setStudyName(TEST_TRIAL_NAME);
			when(trialWorkbook.getStudyDetails()).thenReturn(studyDetails);

			doNothing().when(mocked).setUserSelectionImportedGermplasmMainInfo(anyInt(),
					any(Model.class));

		} catch (MiddlewareQueryException e) {
			handleUnexpectedException(e);
		}
	}

	@Test
	public void testSessionClearOnOpenTrial() {
		final OpenTrialController moleOpenTrialController = setupOpenTrialController();
		prepareHappyPathScenario(moleOpenTrialController);
		MockHttpSession mockSession = new MockHttpSession();

		mockSession.setAttribute(SessionUtility.USER_SELECTION_SESSION_NAME, new UserSelection());
		mockSession.setAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME,
				new ArrayList<Integer>());

		try {
			when(fieldbookService.getTrialDataSet(anyInt())).thenReturn(trialWorkbook);
			moleOpenTrialController
					.openTrial(new CreateTrialForm(), TRIAL_ID, new ExtendedModelMap(), mockSession,
							mock(RedirectAttributes.class));
		} catch (MiddlewareQueryException e) {
			handleUnexpectedException(e);
		}

		assertNull("Controller does not properly reset user selection object on open of trial",
				mockSession.getAttribute(SessionUtility.USER_SELECTION_SESSION_NAME));
		assertNull("Controller does not properly reset the pagination list selection",
				mockSession.getAttribute(
						SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME));
	}

	@Test
	public void testHappyPathOpenTrialCheckModelAttributes() {
		final OpenTrialController moleOpenTrialController = setupOpenTrialController();
		prepareHappyPathScenario(moleOpenTrialController);

		Model model = new ExtendedModelMap();

		try {
			when(fieldbookService.getTrialDataSet(anyInt())).thenReturn(trialWorkbook);
			moleOpenTrialController
					.openTrial(new CreateTrialForm(), TRIAL_ID, model, new MockHttpSession(),
							mock(RedirectAttributes.class));

			assertTrue(
					"Controller does not properly set into the model the data for the basic details",
					model.containsAttribute("basicDetailsData"));

			assertTrue(
					"Controller does not properly set into the model the data for the germplasm tab",
					model.containsAttribute("germplasmData"));
			assertTrue(
					"Controller does not properly set into the model the data for the environments tab",
					model.containsAttribute(OpenTrialController.ENVIRONMENT_DATA_TAB));
			assertTrue(
					"Controller does not properly set into the model the data for the trial settings tab",
					model.containsAttribute("trialSettingsData"));
			assertTrue(
					"Controller does not properly set into the model the data for the measurements tab",
					model.containsAttribute("measurementsData"));
			assertTrue(
					"Controller does not properly set into the model the data for the experimental design tab",
					model.containsAttribute("experimentalDesignData"));
			assertTrue(
					"Controller does not properly set into the model the data for the treatment factors tab",
					model.containsAttribute("treatmentFactorsData"));
			assertTrue(
					"Controller does not properly set into the model the data for the germplasm list size",
					model.containsAttribute("germplasmListSize"));
			assertTrue(
					"Controller does not properly set into the model copy of the trial form",
					model.containsAttribute("createNurseryForm"));
			assertTrue(
					"Controller does not properly set into the model special data required for experimental design tab",
					model.containsAttribute("experimentalDesignSpecialData"));
			assertTrue(
					"Controller does not properly set into the model the study name",
					model.containsAttribute("studyName"));
			assertTrue(
					"Controller does not properly set into the model information on whether trial has measurements or not",
					model.containsAttribute(OpenTrialController.MEASUREMENT_DATA_EXISTING));
			assertTrue(
					"Controller does not properly set into the model the data for measurement row count",
					model.containsAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT));

		} catch (MiddlewareQueryException e) {
			handleUnexpectedException(e);
		}
	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsExisting(){
		final OpenTrialController moleOpenTrialController = setupOpenTrialController();
		Workbook originalWorkbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1);
		originalWorkbook.setStudyDetails(studyDetails);
		String isPreviewEditable = moleOpenTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 0 since there is already existing study", "0", isPreviewEditable);
	}
	
	@Test
	public void testIsPreviewEditableIfStudyDetailsIsNull(){
		final OpenTrialController moleOpenTrialController = setupOpenTrialController();
		Workbook originalWorkbook = new Workbook();		
		String isPreviewEditable = moleOpenTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);
	}
	
	@Test
	public void testIsPreviewEditableIfStudyDetailsIsNotNullAndIdIsNull(){
		final OpenTrialController moleOpenTrialController = setupOpenTrialController();
		Workbook originalWorkbook = new Workbook();		
		StudyDetails studyDetails = new StudyDetails();
		originalWorkbook.setStudyDetails(studyDetails);
		String isPreviewEditable = moleOpenTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);
	}
	
	@Test
	public void testIsPreviewEditableIfOriginalWorkbookIsNull(){
		final OpenTrialController moleOpenTrialController = setupOpenTrialController();
		Workbook originalWorkbook = null;				
		String isPreviewEditable = moleOpenTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);
	}
	
	@Test
	public void testGetFilteredTrialObservations(){
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);
		
		OpenTrialController controller = setupOpenTrialController();
		List<MeasurementRow> filteredTrialObservations = controller.getFilteredTrialObservations(workbook.getTrialObservations(),"2");
		
		Assert.assertEquals("Expecting the number of trial observations is decreased by one.", (workbook.getTotalNumberOfInstances() - 1), filteredTrialObservations.size());
		
		//expecting the trial instance no are in incremental order
		Integer trialInstanceNo = 1;
		for(MeasurementRow row : filteredTrialObservations){	
        	List<MeasurementData> dataList = row.getDataList();
        	for(MeasurementData data : dataList){
        		if (data.getMeasurementVariable() != null) {
                    MeasurementVariable var = data.getMeasurementVariable();
                    
                    if (var != null && data.getMeasurementVariable().getName() != null
                    		&& "TRIAL_INSTANCE".equalsIgnoreCase(var.getName())){
                    	Integer currentTrialInstanceNo = Integer.valueOf(data.getValue());
                    	Assert.assertEquals("Expecting trial instance the next trial instance no is " + trialInstanceNo + " but returned " 
                    				+ currentTrialInstanceNo,trialInstanceNo,currentTrialInstanceNo);
                    	trialInstanceNo++;
                    	break;
                    }
                }
        	}
        }
	}
	
	@Test
	public void testGetFilteredTrialObservationsWithNoDeletedEnvironmentId(){
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);
		
		OpenTrialController controller = setupOpenTrialController();
		List<MeasurementRow> filteredTrialObservations = controller.getFilteredTrialObservations(workbook.getTrialObservations(),"");
		
		Assert.assertEquals("Expecting the number of trial observations is the same after the method call.", (workbook.getTotalNumberOfInstances()), filteredTrialObservations.size());
	}
	
	@Test
	public void testGetFilteredObservations(){
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);
		
		OpenTrialController controller = setupOpenTrialController();
		List<MeasurementRow> filteredObservations = controller.getFilteredObservations(workbook.getObservations(),"2");
		
		Assert.assertEquals("Expecting the number of observations is decreased by " + NO_OF_OBSERVATIONS, (workbook.getObservations().size() - NO_OF_OBSERVATIONS), filteredObservations.size());
		
		//expecting the trial instance no are in incremental order
		Integer noOfTrialInstances = NO_OF_TRIAL_INSTANCES - 1;
		for(MeasurementRow row : filteredObservations){	
        	List<MeasurementData> dataList = row.getDataList();
        	for(MeasurementData data : dataList){
        		if (data.getMeasurementVariable() != null) {
                    MeasurementVariable var = data.getMeasurementVariable();
                    
                    if (var != null && data.getMeasurementVariable().getName() != null
                    		&& "TRIAL_INSTANCE".equalsIgnoreCase(var.getName())){
                    	Integer currentTrialInstanceNo = Integer.valueOf(data.getValue());
                    	Assert.assertTrue("Expecting trial instance the next trial instance no is within the "
                    			+ "possible range of trial instance no but didn't.",currentTrialInstanceNo <= noOfTrialInstances);
                    }
                }
        	}
        }
	}
	
	@Test
	public void testGetFilteredObservationsWithNoDeletedEnvironmentId(){
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);
		
		OpenTrialController controller = setupOpenTrialController();
		List<MeasurementRow> filteredObservations = controller.getFilteredObservations(workbook.getObservations(),"");
		
		Assert.assertEquals("Expecting the number of observations is the same after the method call.", workbook.getObservations().size(), filteredObservations.size());
	}
	
	
	protected void handleUnexpectedException(Exception e) {
		fail("Unexpected error during unit test : " + e.getMessage());
	}

	protected OpenTrialController setupOpenTrialController() {
		final OpenTrialController moleOpenTrialController = spy(openTrialController);
		doNothing().when(moleOpenTrialController).clearSessionData(session);
		return moleOpenTrialController;
	}
}