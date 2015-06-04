
package com.efficio.fieldbook.web.trial.controller;

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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
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
		final OpenTrialController moleOpenTrialController = this.setupOpenTrialController();
		Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(this.fieldbookService.getTrialDataSet(OpenTrialControllerTest.TRIAL_ID)).thenReturn(workbook);

		Mockito.doNothing().when(moleOpenTrialController)
		.setModelAttributes(this.createTrialForm, OpenTrialControllerTest.TRIAL_ID, this.model, workbook);

		String out =
				moleOpenTrialController.openTrial(this.createTrialForm, OpenTrialControllerTest.TRIAL_ID, this.model, this.session,
						this.redirectAttributes);

		Mockito.verify(this.fieldbookService).getTrialDataSet(OpenTrialControllerTest.TRIAL_ID);
		Assert.assertEquals("should return the base angular template", AbstractBaseFieldbookController.ANGULAR_BASE_TEMPLATE_NAME, out);
	}

	@Test
	public void testOpenTrialRedirectForIncompatibleStudy() throws Exception {
		final OpenTrialController moleOpenTrialController = this.setupOpenTrialController();

		Mockito.when(this.fieldbookService.getTrialDataSet(OpenTrialControllerTest.TRIAL_ID)).thenThrow(MiddlewareQueryException.class);

		String out =
				moleOpenTrialController.openTrial(this.createTrialForm, OpenTrialControllerTest.TRIAL_ID, this.model, this.session,
						this.redirectAttributes);

		Assert.assertEquals("should redirect to manage trial page", "redirect:" + ManageTrialController.URL, out);

		ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);

		Mockito.verify(this.redirectAttributes).addFlashAttribute(arg1.capture(), arg2.capture());
		Assert.assertEquals("value should be redirectErrorMessage", "redirectErrorMessage", arg1.getValue());

	}

	protected void prepareHappyPathScenario(OpenTrialController mocked) {
		try {
			Mockito.doReturn(new TabInfo()).when(mocked)
			.prepareBasicDetailsTabInfo(Matchers.any(StudyDetails.class), Matchers.anyBoolean(), Matchers.anyInt());
			Mockito.doReturn(new TabInfo()).when(mocked).prepareGermplasmTabInfo(Matchers.anyList(), Matchers.anyBoolean());
			Mockito.doReturn(new TabInfo()).when(mocked).prepareEnvironmentsTabInfo(Matchers.any(Workbook.class), Matchers.anyBoolean());
			Mockito.doReturn(new TabInfo()).when(mocked).prepareTrialSettingsTabInfo(Matchers.anyList(), Matchers.anyBoolean());
			Mockito.doReturn(new TabInfo()).when(mocked).prepareMeasurementsTabInfo(Matchers.anyList(), Matchers.anyBoolean());
			Mockito.doReturn(new TabInfo()).when(mocked)
					.prepareExperimentalDesignTabInfo(Matchers.any(ExperimentalDesignVariable.class), Matchers.anyBoolean());
			Mockito.when(this.fieldbookService.checkIfStudyHasMeasurementData(Matchers.anyInt(), Matchers.anyList())).thenReturn(true);
			Mockito.when(this.trialWorkbook.getObservations()).thenReturn(new ArrayList<MeasurementRow>());

			Mockito.doReturn(new TabInfo()).when(mocked).prepareTreatmentFactorsInfo(Matchers.anyList(), Matchers.anyBoolean());
			Mockito.doReturn(new TabInfo()).when(mocked).prepareExperimentalDesignSpecialData();

			StudyDetails studyDetails = new StudyDetails();
			studyDetails.setStudyName(OpenTrialControllerTest.TEST_TRIAL_NAME);
			Mockito.when(this.trialWorkbook.getStudyDetails()).thenReturn(studyDetails);

			Mockito.doNothing().when(mocked).setUserSelectionImportedGermplasmMainInfo(Matchers.anyInt(), Matchers.any(Model.class));

		} catch (MiddlewareQueryException e) {
			this.handleUnexpectedException(e);
		}
	}

	@Test
	public void testSessionClearOnOpenTrial() {
		final OpenTrialController moleOpenTrialController = this.setupOpenTrialController();
		this.prepareHappyPathScenario(moleOpenTrialController);
		MockHttpSession mockSession = new MockHttpSession();

		mockSession.setAttribute(SessionUtility.USER_SELECTION_SESSION_NAME, new UserSelection());
		mockSession.setAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME, new ArrayList<Integer>());

		try {
			Mockito.when(this.fieldbookService.getTrialDataSet(Matchers.anyInt())).thenReturn(this.trialWorkbook);
			moleOpenTrialController.openTrial(new CreateTrialForm(), OpenTrialControllerTest.TRIAL_ID, new ExtendedModelMap(), mockSession,
					Mockito.mock(RedirectAttributes.class));
		} catch (MiddlewareQueryException e) {
			this.handleUnexpectedException(e);
		}

		Assert.assertNull("Controller does not properly reset user selection object on open of trial",
				mockSession.getAttribute(SessionUtility.USER_SELECTION_SESSION_NAME));
		Assert.assertNull("Controller does not properly reset the pagination list selection",
				mockSession.getAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME));
	}

	@Test
	public void testHappyPathOpenTrialCheckModelAttributes() {
		final OpenTrialController moleOpenTrialController = this.setupOpenTrialController();
		this.prepareHappyPathScenario(moleOpenTrialController);

		Model model = new ExtendedModelMap();

		try {
			Mockito.when(this.fieldbookService.getTrialDataSet(Matchers.anyInt())).thenReturn(this.trialWorkbook);
			moleOpenTrialController.openTrial(new CreateTrialForm(), OpenTrialControllerTest.TRIAL_ID, model, new MockHttpSession(),
					Mockito.mock(RedirectAttributes.class));

			Assert.assertTrue("Controller does not properly set into the model the data for the basic details",
					model.containsAttribute("basicDetailsData"));

			Assert.assertTrue("Controller does not properly set into the model the data for the germplasm tab",
					model.containsAttribute("germplasmData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the environments tab",
					model.containsAttribute(OpenTrialController.ENVIRONMENT_DATA_TAB));
			Assert.assertTrue("Controller does not properly set into the model the data for the trial settings tab",
					model.containsAttribute("trialSettingsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the measurements tab",
					model.containsAttribute("measurementsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the experimental design tab",
					model.containsAttribute("experimentalDesignData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the treatment factors tab",
					model.containsAttribute("treatmentFactorsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the germplasm list size",
					model.containsAttribute("germplasmListSize"));
			Assert.assertTrue("Controller does not properly set into the model copy of the trial form",
					model.containsAttribute("createNurseryForm"));
			Assert.assertTrue("Controller does not properly set into the model special data required for experimental design tab",
					model.containsAttribute("experimentalDesignSpecialData"));
			Assert.assertTrue("Controller does not properly set into the model the study name", model.containsAttribute("studyName"));
			Assert.assertTrue("Controller does not properly set into the model information on whether trial has measurements or not",
					model.containsAttribute(OpenTrialController.MEASUREMENT_DATA_EXISTING));
			Assert.assertTrue("Controller does not properly set into the model the data for measurement row count",
					model.containsAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT));

		} catch (MiddlewareQueryException e) {
			this.handleUnexpectedException(e);
		}
	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsExisting() {
		final OpenTrialController moleOpenTrialController = this.setupOpenTrialController();
		Workbook originalWorkbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1);
		originalWorkbook.setStudyDetails(studyDetails);
		String isPreviewEditable = moleOpenTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 0 since there is already existing study", "0", isPreviewEditable);
	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsNull() {
		final OpenTrialController moleOpenTrialController = this.setupOpenTrialController();
		Workbook originalWorkbook = new Workbook();
		String isPreviewEditable = moleOpenTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);
	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsNotNullAndIdIsNull() {
		final OpenTrialController moleOpenTrialController = this.setupOpenTrialController();
		Workbook originalWorkbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		originalWorkbook.setStudyDetails(studyDetails);
		String isPreviewEditable = moleOpenTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);
	}

	@Test
	public void testIsPreviewEditableIfOriginalWorkbookIsNull() {
		final OpenTrialController moleOpenTrialController = this.setupOpenTrialController();
		Workbook originalWorkbook = null;
		String isPreviewEditable = moleOpenTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);
	}

	@Test
	public void testGetFilteredTrialObservations() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		OpenTrialController controller = this.setupOpenTrialController();
		List<MeasurementRow> filteredTrialObservations = controller.getFilteredTrialObservations(workbook.getTrialObservations(), "2");

		Assert.assertEquals("Expecting the number of trial observations is decreased by one.", workbook.getTotalNumberOfInstances() - 1,
				filteredTrialObservations.size());

		// expecting the trial instance no are in incremental order
		Integer trialInstanceNo = 1;
		for (MeasurementRow row : filteredTrialObservations) {
			List<MeasurementData> dataList = row.getDataList();
			for (MeasurementData data : dataList) {
				if (data.getMeasurementVariable() != null) {
					MeasurementVariable var = data.getMeasurementVariable();

					if (var != null && data.getMeasurementVariable().getName() != null && "TRIAL_INSTANCE".equalsIgnoreCase(var.getName())) {
						Integer currentTrialInstanceNo = Integer.valueOf(data.getValue());
						Assert.assertEquals("Expecting trial instance the next trial instance no is " + trialInstanceNo + " but returned "
								+ currentTrialInstanceNo, trialInstanceNo, currentTrialInstanceNo);
						trialInstanceNo++;
						break;
					}
				}
			}
		}
	}

	@Test
	public void testGetFilteredTrialObservationsWithNoDeletedEnvironmentId() {
		Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		OpenTrialController controller = this.setupOpenTrialController();
		List<MeasurementRow> filteredTrialObservations = controller.getFilteredTrialObservations(workbook.getTrialObservations(), "");

		Assert.assertEquals("Expecting the number of trial observations is the same after the method call.",
				workbook.getTotalNumberOfInstances(), filteredTrialObservations.size());
	}

	@Test
	public void testGetFilteredObservations() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		OpenTrialController controller = this.setupOpenTrialController();
		List<MeasurementRow> filteredObservations = controller.getFilteredObservations(workbook.getObservations(), "2");

		Assert.assertEquals("Expecting the number of observations is decreased by " + OpenTrialControllerTest.NO_OF_OBSERVATIONS, workbook
				.getObservations().size() - OpenTrialControllerTest.NO_OF_OBSERVATIONS, filteredObservations.size());

		// expecting the trial instance no are in incremental order
		Integer noOfTrialInstances = OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES - 1;
		for (MeasurementRow row : filteredObservations) {
			List<MeasurementData> dataList = row.getDataList();
			for (MeasurementData data : dataList) {
				if (data.getMeasurementVariable() != null) {
					MeasurementVariable var = data.getMeasurementVariable();

					if (var != null && data.getMeasurementVariable().getName() != null && "TRIAL_INSTANCE".equalsIgnoreCase(var.getName())) {
						Integer currentTrialInstanceNo = Integer.valueOf(data.getValue());
						Assert.assertTrue("Expecting trial instance the next trial instance no is within the "
								+ "possible range of trial instance no but didn't.", currentTrialInstanceNo <= noOfTrialInstances);
					}
				}
			}
		}
	}

	@Test
	public void testGetFilteredObservationsWithNoDeletedEnvironmentId() {
		Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		OpenTrialController controller = this.setupOpenTrialController();
		List<MeasurementRow> filteredObservations = controller.getFilteredObservations(workbook.getObservations(), "");

		Assert.assertEquals("Expecting the number of observations is the same after the method call.", workbook.getObservations().size(),
				filteredObservations.size());
	}

	protected void handleUnexpectedException(Exception e) {
		Assert.fail("Unexpected error during unit test : " + e.getMessage());
	}

	protected OpenTrialController setupOpenTrialController() {
		final OpenTrialController moleOpenTrialController = Mockito.spy(this.openTrialController);
		Mockito.doNothing().when(moleOpenTrialController).clearSessionData(this.session);
		return moleOpenTrialController;
	}
}
