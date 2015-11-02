
package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
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
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.SessionUtility;

@RunWith(MockitoJUnitRunner.class)
public class OpenTrialControllerTest {

	private static final int NO_OF_TRIAL_INSTANCES = 3;
	private static final int NO_OF_OBSERVATIONS = 5;
	private static final int TRIAL_ID = 1;
	private static final int WORKBENCH_USER_ID = 1;
	private static final long WORKBENCH_PROJECT_ID = 1L;
	private static final String WORKBENCH_PROJECT_NAME = "Project 1";
	private static final int IBDB_USER_ID = 1;
	private static final String PROGRAM_UUID = "68f0d114-5b5b-11e5-885d-feff819cdc9f";
	public static final String TEST_TRIAL_NAME = "dummyTrial";

	@Mock
	private HttpServletRequest httpRequest;

	@Mock
	private HttpSession httpSession;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private CreateTrialForm createTrialForm;

	@Mock
	private Model model;

	@Mock
	private RedirectAttributes redirectAttributes;

	@Mock
	protected FieldbookService fieldbookMiddlewareService;

	@Mock
	protected com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private ErrorHandlerService errorHandlerService;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private OpenTrialController openTrialController;

	@Before
	public void setUp() {

		Project project = this.createProject();
		DmsProject dmsProject = this.createDmsProject();
		WorkbenchRuntimeData workbenchRuntimeData = new WorkbenchRuntimeData();
		workbenchRuntimeData.setUserId(WORKBENCH_USER_ID);

		Mockito.when(this.workbenchService.getCurrentIbdbUserId(1L, WORKBENCH_USER_ID)).thenReturn(IBDB_USER_ID);
		Mockito.when(this.workbenchDataManager.getWorkbenchRuntimeData()).thenReturn(workbenchRuntimeData);
		Mockito.when(this.workbenchDataManager.getLastOpenedProjectAnyUser()).thenReturn(project);
		Mockito.when(this.studyDataManager.getProject(1)).thenReturn(dmsProject);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);
		Project testProject = new Project();
		testProject.setProjectId(1L);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);

		this.initializeOntology();

		WorkbookDataUtil.setTestWorkbook(null);

	}

	@Test
	public void testOpenTrialNoRedirect() throws Exception {

		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);
		// TODO: Initialize the treatment factors inside WorkbookDataUtil
		workbook.setTreatmentFactors(new ArrayList<TreatmentVariable>());

		Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(OpenTrialControllerTest.TRIAL_ID)).thenReturn(workbook);

		String out =
				this.openTrialController.openTrial(this.createTrialForm, OpenTrialControllerTest.TRIAL_ID, this.model, this.httpSession,
						this.redirectAttributes);

		Mockito.verify(this.fieldbookMiddlewareService).getTrialDataSet(OpenTrialControllerTest.TRIAL_ID);

		Assert.assertEquals("should return the base angular template", AbstractBaseFieldbookController.ANGULAR_BASE_TEMPLATE_NAME, out);
	}

	@Test
	public void testOpenTrialRedirectForIncompatibleStudy() throws Exception {

		Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(OpenTrialControllerTest.TRIAL_ID)).thenThrow(
				MiddlewareQueryException.class);

		String out =
				this.openTrialController.openTrial(this.createTrialForm, OpenTrialControllerTest.TRIAL_ID, this.model, this.httpSession,
						this.redirectAttributes);

		Assert.assertEquals("should redirect to manage trial page", "redirect:" + ManageTrialController.URL, out);

		ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);

		Mockito.verify(this.redirectAttributes).addFlashAttribute(arg1.capture(), arg2.capture());
		Assert.assertEquals("value should be redirectErrorMessage", "redirectErrorMessage", arg1.getValue());

	}

	@Test
	public void testSessionClearOnOpenTrial() {

		MockHttpSession mockSession = new MockHttpSession();

		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);
		workbook.setTreatmentFactors(new ArrayList<TreatmentVariable>());

		mockSession.setAttribute(SessionUtility.USER_SELECTION_SESSION_NAME, new UserSelection());
		mockSession.setAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME, new ArrayList<Integer>());

		try {
			Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(Matchers.anyInt())).thenReturn(workbook);
			this.openTrialController.openTrial(new CreateTrialForm(), OpenTrialControllerTest.TRIAL_ID, new ExtendedModelMap(),
					mockSession, Mockito.mock(RedirectAttributes.class));
		} catch (MiddlewareException e) {
			this.handleUnexpectedException(e);
		}

		Assert.assertNull("Controller does not properly reset user selection object on open of trial",
				mockSession.getAttribute(SessionUtility.USER_SELECTION_SESSION_NAME));
		Assert.assertNull("Controller does not properly reset the pagination list selection",
				mockSession.getAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME));
	}

	@Test
	public void testHappyPathOpenTrialCheckModelAttributes() {

		Model model = new ExtendedModelMap();

		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);
		workbook.setTreatmentFactors(new ArrayList<TreatmentVariable>());

		try {

			Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(Matchers.anyInt())).thenReturn(workbook);

			this.openTrialController.openTrial(new CreateTrialForm(), OpenTrialControllerTest.TRIAL_ID, model, new MockHttpSession(),
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

		} catch (MiddlewareException e) {
			this.handleUnexpectedException(e);
		}
	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsExisting() {

		Workbook originalWorkbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1);
		originalWorkbook.setStudyDetails(studyDetails);
		String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 0 since there is already existing study", "0", isPreviewEditable);

	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsNull() {

		Workbook originalWorkbook = new Workbook();
		String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);

	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsNotNullAndIdIsNull() {

		Workbook originalWorkbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		originalWorkbook.setStudyDetails(studyDetails);
		String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);
	}

	@Test
	public void testIsPreviewEditableIfOriginalWorkbookIsNull() {

		Workbook originalWorkbook = null;
		String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);

	}

	@Test
	public void testGetFilteredTrialObservations() {

		Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		List<MeasurementRow> filteredTrialObservations =
				this.openTrialController.getFilteredTrialObservations(workbook.getTrialObservations(), "2");

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

		List<MeasurementRow> filteredTrialObservations =
				this.openTrialController.getFilteredTrialObservations(workbook.getTrialObservations(), "");

		Assert.assertEquals("Expecting the number of trial observations is the same after the method call.",
				workbook.getTotalNumberOfInstances(), filteredTrialObservations.size());
	}

	@Test
	public void testGetFilteredObservations() {

		Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		List<MeasurementRow> filteredObservations = this.openTrialController.getFilteredObservations(workbook.getObservations(), "2");

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

		List<MeasurementRow> filteredObservations = this.openTrialController.getFilteredObservations(workbook.getObservations(), "");

		Assert.assertEquals("Expecting the number of observations is the same after the method call.", workbook.getObservations().size(),
				filteredObservations.size());
	}

	protected void handleUnexpectedException(Exception e) {
		Assert.fail("Unexpected error during unit test : " + e.getMessage());
	}

	protected DmsProject createDmsProject() {
		DmsProject dmsProject = new DmsProject();
		dmsProject.setProjectId(TRIAL_ID);
		dmsProject.setName(TEST_TRIAL_NAME);
		dmsProject.setProgramUUID(PROGRAM_UUID);
		return dmsProject;
	}

	private Project createProject() {
		Project project = new Project();
		project.setProjectId(WORKBENCH_PROJECT_ID);
		project.setProjectName(WORKBENCH_PROJECT_NAME);
		return project;
	}

	protected void initializeOntology() {

		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);

		for (MeasurementVariable mvar : workbook.getAllVariables()) {

			StandardVariable stdVar = this.convertToStandardVariable(mvar);
			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(stdVar.getId(), PROGRAM_UUID)).thenReturn(stdVar);
		}

		// StudyName
		StandardVariable studyName =
				this.createStandardVariable(8005, "STUDY_NAME", "Study", "DBCV", "Assigned", 1120, "Character variable", "STUDY");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8005, PROGRAM_UUID)).thenReturn(studyName);

		// StudyTitle
		StandardVariable studyTitle =
				this.createStandardVariable(8007, "STUDY_TITLE", "Study title", "Text", "Assigned", 1120, "Character variable", "STUDY");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8007, PROGRAM_UUID)).thenReturn(studyTitle);

		// StudyObjective
		StandardVariable studyObjective =
				this.createStandardVariable(8030, "STUDY_OBJECTIVE", "Study objective", "Text", "Described", 1120, "Character variable",
						"STUDY");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8030, PROGRAM_UUID)).thenReturn(studyObjective);

		// StartDate
		StandardVariable startDate =
				this.createStandardVariable(8050, "START_DATE", "Start date", "Date (yyyymmdd)", "Assigned", 1117, "Date variable", "STUDY");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8050, PROGRAM_UUID)).thenReturn(startDate);

		// EndDate
		StandardVariable endDate =
				this.createStandardVariable(8060, "END_DATE", "End date", "Date (yyyymmdd)", "Assigned", 1117, "Date variable", "STUDY");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8060, PROGRAM_UUID)).thenReturn(endDate);

		StandardVariable plotNo =
				this.createStandardVariable(8200, "PLOT_NO", "Field plot", "Number", "Enumerated", 1110, "Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8200, PROGRAM_UUID)).thenReturn(plotNo);

		StandardVariable repNo =
				this.createStandardVariable(8210, "REP_NO", "Replication factor", "Number", "Enumerated", 1110, "Numeric variable",
						"TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8210, PROGRAM_UUID)).thenReturn(repNo);

		StandardVariable blockNo =
				this.createStandardVariable(8220, "BLOCK_NO", "Blocking factor", "Number", "Enumerated", 1110, "Numeric variable",
						"TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8220, PROGRAM_UUID)).thenReturn(blockNo);

		StandardVariable row =
				this.createStandardVariable(8581, "ROW", "Row in layout", "Number", "Enumerated", 1110, "Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8581, PROGRAM_UUID)).thenReturn(row);

		StandardVariable col =
				this.createStandardVariable(8582, "COL", "Column in layout", "Number", "Enumerated", 1110, "Numeric variable",
						"TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8582, PROGRAM_UUID)).thenReturn(col);

	}

	protected StandardVariable convertToStandardVariable(final MeasurementVariable measurementVar) {
		final StandardVariable stdVar =
				this.createStandardVariable(measurementVar.getTermId(), measurementVar.getName(), measurementVar.getProperty(),
						measurementVar.getScale(), measurementVar.getMethod(), measurementVar.getDataTypeId(),
						measurementVar.getDataType(), measurementVar.getLabel());
		return stdVar;
	}

	protected StandardVariable createStandardVariable(int termId, String name, String property, String scale, String method,
			int dataTypeId, String dataType, String label) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setId(termId);
		stdVar.setName(name);
		stdVar.setProperty(new Term(0, property, ""));
		stdVar.setScale(new Term(0, scale, ""));
		stdVar.setMethod(new Term(0, method, ""));
		stdVar.setDataType(new Term(dataTypeId, dataType, ""));
		stdVar.setPhenotypicType(PhenotypicType.getPhenotypicTypeForLabel(label));
		return stdVar;
	}
}
