
package com.efficio.fieldbook.web.trial.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.ListDataProjectTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.VariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.DesignTypeItem;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.StudyType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.SampleListService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.util.Util;
import org.generationcp.middleware.utils.test.UnitTestDaoIDGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.trial.TestDataHelper;
import com.efficio.fieldbook.web.trial.bean.AdvanceList;
import com.efficio.fieldbook.web.trial.bean.BasicDetails;
import com.efficio.fieldbook.web.trial.bean.CrossesList;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.TabInfo;
import com.efficio.fieldbook.web.trial.bean.TreatmentFactorData;
import com.efficio.fieldbook.web.trial.bean.TreatmentFactorTabBean;
import com.efficio.fieldbook.web.trial.bean.TrialData;
import com.efficio.fieldbook.web.trial.bean.TrialSettingsBean;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class OpenTrialControllerTest {

	private static final String VARIABLE_NAME_PREFIX = "Variable ";
	private static final String VARIABLE_NAME = "Variable Name";
	private static final String SELECTION_TRAIT = "BM_CODE_VTE";
	private static final String TRAIT = "nEarsSel_Local";
	private static final int NO_OF_TRIAL_INSTANCES = 3;
	private static final int NO_OF_OBSERVATIONS = 5;
	private static final int STUDY_ID = 1;
	private static final int WORKBENCH_USER_ID = 1;
	private static final long WORKBENCH_PROJECT_ID = 1L;
	private static final String WORKBENCH_PROJECT_NAME = "Project 1";
	private static final int IBDB_USER_ID = 1;
	private static final String PROGRAM_UUID = "68f0d114-5b5b-11e5-885d-feff819cdc9f";
	public static final String TEST_STUDY_NAME = "dummyStudy";
	private static final int BM_CODE_VTE_ID = 8252;
	private static final int N_EARS_SEL = 8253;
	public static final String GERMPLASM_LIST_SIZE = "germplasmListSize";
	public static final String GERMPLASM_CHECKS_SIZE = "germplasmChecksSize";

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

	@Mock
	private OntologyVariableDataManager variableDataManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@InjectMocks
	private OpenTrialController openTrialController;

	@Mock
	private SampleListService sampleListService;

	@Mock
	private DatasetService datasetService;

	private Variable testVariable;

	@Before
	public void setUp() {
		final Project project = this.createProject();
		final DmsProject dmsProject = this.createDmsProject();
		final StudyType studyType = new StudyType();
		studyType.setName(StudyTypeDto.TRIAL_NAME);
		studyType.setLabel(StudyTypeDto.TRIAL_LABEL);
		dmsProject.setStudyType(studyType);

		Mockito.when(this.studyDataManager.getProject(1)).thenReturn(dmsProject);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(OpenTrialControllerTest.PROGRAM_UUID);

		final Project testProject = new Project();
		testProject.setProjectId(1L);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		workbook.setTrialDatasetId(1);
		workbook.setMeasurementDatesetId(1);
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1);
		workbook.setStudyDetails(studyDetails);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		this.initializeOntology();
		final VariableTypeList factors = Mockito.mock(VariableTypeList.class);
		Mockito.when(factors.findById(Matchers.anyInt())).thenReturn(null);
		Mockito.when(this.studyDataManager.getAllStudyFactors(Matchers.anyInt())).thenReturn(factors);

		final List<SampleListDTO> sampleListDTOs = new ArrayList<>();
		Mockito.when(this.sampleListService.getSampleLists(Matchers.<Integer>anyList())).thenReturn(sampleListDTOs);

		this.createTestVariable();
		Mockito.when(this.variableDataManager.getVariable(Matchers.any(String.class), Matchers.any(Integer.class), Matchers.anyBoolean())).thenReturn(this.testVariable);
		Mockito.when(studyDataManager.getStudyTypeByName(Mockito.anyString())).thenReturn(StudyTypeDto.getTrialDto());
	}

	@Test
	public void testOpenStudyNoRedirect() throws Exception {

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyTypeDto.getTrialDto());
		WorkbookTestDataInitializer.setTrialObservations(workbook);

		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(OpenTrialControllerTest.STUDY_ID)).thenReturn(workbook);
		final Study study = new Study();
		study.setStudyType(StudyTypeDto.getTrialDto());

		this.mockStandardVariables(workbook.getAllVariables());

		final String out = this.openTrialController.openTrial(this.createTrialForm, OpenTrialControllerTest.STUDY_ID, this.model,
				this.httpSession, this.redirectAttributes, null);

		Mockito.verify(this.fieldbookMiddlewareService).getStudyDataSet(OpenTrialControllerTest.STUDY_ID);

		Assert.assertEquals("should return the base angular template", AbstractBaseFieldbookController.ANGULAR_BASE_TEMPLATE_NAME, out);
	}

	@Test
	public void testOpenStudyRedirectForIncompatibleStudy() throws Exception {

		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(OpenTrialControllerTest.STUDY_ID))
				.thenThrow(MiddlewareQueryException.class);

		final String out = this.openTrialController.openTrial(this.createTrialForm, OpenTrialControllerTest.STUDY_ID, this.model,
				this.httpSession, this.redirectAttributes, null);

		Assert.assertEquals("should redirect to manage Study page", "redirect:" + ManageTrialController.URL, out);

		final ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);

		Mockito.verify(this.redirectAttributes).addFlashAttribute(arg1.capture(), arg2.capture());
		Assert.assertEquals("value should be redirectErrorMessage", "redirectErrorMessage", arg1.getValue());

	}

	@Test
	public void testSessionClearOnOpenStudy() {

		final MockHttpSession mockSession = new MockHttpSession();

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyTypeDto.getTrialDto());
		WorkbookTestDataInitializer.setTrialObservations(workbook);

		mockSession.setAttribute(SessionUtility.USER_SELECTION_SESSION_NAME, new UserSelection());
		mockSession.setAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME, new ArrayList<Integer>());

		try {
			Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(Matchers.anyInt())).thenReturn(workbook);
			this.mockStandardVariables(workbook.getAllVariables());
			this.openTrialController.openTrial(new CreateTrialForm(), OpenTrialControllerTest.STUDY_ID, new ExtendedModelMap(), mockSession,
					Mockito.mock(RedirectAttributes.class), null);
		} catch (final MiddlewareException e) {
			this.handleUnexpectedException(e);
		}

		Assert.assertNull("Controller does not properly reset user selection object on open of Study",
				mockSession.getAttribute(SessionUtility.USER_SELECTION_SESSION_NAME));
		Assert.assertNull("Controller does not properly reset the pagination list selection",
				mockSession.getAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME));
	}

	@Test
	public void testHappyPathOpenStudyCheckModelAttributes() {

		final Model model = new ExtendedModelMap();

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyTypeDto.getTrialDto());
		WorkbookTestDataInitializer.setTrialObservations(workbook);

		// Verify that workbook has Analysis and/or Analysis Summary variables
		// beforehand to check that they were later removed
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConditions()));
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConstants()));

		try {

			Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(Matchers.anyInt())).thenReturn(workbook);
			this.mockStandardVariables(workbook.getAllVariables());

			this.openTrialController.openTrial(new CreateTrialForm(), OpenTrialControllerTest.STUDY_ID, model, new MockHttpSession(),
					Mockito.mock(RedirectAttributes.class), null);

			Assert.assertTrue("Controller does not properly set into the model the data for the basic details",
					model.containsAttribute("basicDetailsData"));

			Assert.assertTrue("Controller does not properly set into the model the data for the germplasm tab",
					model.containsAttribute("germplasmData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the environments tab",
					model.containsAttribute(OpenTrialController.ENVIRONMENT_DATA_TAB));
			Assert.assertTrue("Controller does not properly set into the model the data for the Study settings tab",
					model.containsAttribute("trialSettingsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the measurements tab",
					model.containsAttribute("measurementsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the experimental design tab",
					model.containsAttribute("experimentalDesignData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the treatment factors tab",
					model.containsAttribute("treatmentFactorsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the germplasm list size",
					model.containsAttribute(OpenTrialControllerTest.GERMPLASM_LIST_SIZE));
			Assert.assertTrue("Controller does not properly set into the model copy of the study form",
					model.containsAttribute("createTrialForm"));
			Assert.assertTrue("Controller does not properly set into the model special data required for experimental design tab",
					model.containsAttribute("experimentalDesignSpecialData"));
			Assert.assertTrue("Controller does not properly set into the model the study name", model.containsAttribute("studyName"));
			Assert.assertTrue("Controller does not properly set into the model information on whether Study has measurements or not",
					model.containsAttribute(OpenTrialController.MEASUREMENT_DATA_EXISTING));
			Assert.assertTrue("Controller does not properly set into the model the data for measurement row count",
					model.containsAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT));

			Assert.assertFalse("'Analysis' and 'Analysis Summary' variables should not be displayed.", this.hasAnalysisVariables(model));

		} catch (final MiddlewareException e) {
			this.handleUnexpectedException(e);
		}
	}

	private boolean hasAnalysisVariables(final List<MeasurementVariable> variables) {
		boolean analysisVariableFound = false;
		for (final MeasurementVariable variable : variables) {
			if (VariableType.getReservedVariableTypes().contains(variable.getVariableType())) {
				analysisVariableFound = true;
				break;
			}
		}
		return analysisVariableFound;
	}

	private boolean hasAnalysisVariables(final Model model) {
		final List<SettingDetail> settingDetails = this.getSettingDetailsPossiblyWithAnalysisVariables(model);
		boolean analysisVariableFound = false;
		for (final SettingDetail settingDetail : settingDetails) {
			if (VariableType.getReservedVariableTypes().contains(settingDetail.getVariableType())) {
				analysisVariableFound = true;
				break;
			}
		}
		return analysisVariableFound;
	}

	@SuppressWarnings("unchecked")
	private List<SettingDetail> getSettingDetailsPossiblyWithAnalysisVariables(final Model model) {
		final List<SettingDetail> settingDetails = new ArrayList<>();

		final Map<String, Object> modelMap = model.asMap();

		final TabInfo experimentsDataTabInfo = (TabInfo) modelMap.get(OpenTrialController.ENVIRONMENT_DATA_TAB);
		final List<SettingDetail> managementDetailList =
				(List<SettingDetail>) experimentsDataTabInfo.getSettingMap().get("managementDetails");
		final List<SettingDetail> conditionDetails =
				(List<SettingDetail>) experimentsDataTabInfo.getSettingMap().get("trialConditionDetails");
		settingDetails.addAll(managementDetailList);
		settingDetails.addAll(conditionDetails);

		final TabInfo measurementsDataTabInfo = (TabInfo) modelMap.get("measurementsData");
		settingDetails.addAll(measurementsDataTabInfo.getSettings());

		return settingDetails;
	}

	private void mockStandardVariables(final List<MeasurementVariable> allVariables) {
		for (final MeasurementVariable measurementVariable : allVariables) {
			Mockito.doReturn(this.createStandardVariable(measurementVariable.getTermId())).when(this.fieldbookMiddlewareService)
					.getStandardVariable(measurementVariable.getTermId(), OpenTrialControllerTest.PROGRAM_UUID);
			final Variable variable = new Variable();
			variable.setId(measurementVariable.getTermId());
			variable.setName(measurementVariable.getName());
			variable.setMethod(TestDataHelper.createMethod());
			variable.setProperty(TestDataHelper.createProperty());
			variable.setScale(TestDataHelper.createScale());
			Mockito.when(this.variableDataManager.getVariable(Matchers.eq(OpenTrialControllerTest.PROGRAM_UUID),
					Matchers.eq(measurementVariable.getTermId()), Matchers.anyBoolean())).thenReturn(variable);
		}
	}

	private StandardVariable createStandardVariable(final Integer id) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(id);
		standardVariable.setProperty(new Term(1, "1", "1"));
		standardVariable.setMethod(new Term(2, "2", "2"));
		standardVariable.setScale(new Term(3, "3", "3"));
		standardVariable.setDataType(new Term(4, "4", "4"));
		return standardVariable;
	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsExisting() {

		final Workbook originalWorkbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1);
		originalWorkbook.setStudyDetails(studyDetails);
		final String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 0 since there is already existing study", "0", isPreviewEditable);

	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsNull() {

		final Workbook originalWorkbook = new Workbook();
		final String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);

	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsNotNullAndIdIsNull() {

		final Workbook originalWorkbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		originalWorkbook.setStudyDetails(studyDetails);
		final String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);
	}

	@Test
	public void testIsPreviewEditableIfOriginalWorkbookIsNull() {

		final Workbook originalWorkbook = null;
		final String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);

	}

	@Test
	public void testGetFilteredStudyObservations() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredStudyObservations =
				this.openTrialController.getFilteredTrialObservations(workbook.getTrialObservations(), "2");

		Assert.assertEquals("Expecting the number of Study observations is decreased by one.", workbook.getTotalNumberOfInstances() - 1,
				filteredStudyObservations.size());

		// expecting the trial instance no are in incremental order
		Integer trialInstanceNo = 1;
		for (final MeasurementRow row : filteredStudyObservations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (data.getMeasurementVariable() != null) {
					final MeasurementVariable var = data.getMeasurementVariable();

					if (var != null && data.getMeasurementVariable().getName() != null
							&& "TRIAL_INSTANCE".equalsIgnoreCase(var.getName())) {
						final Integer currentTrialInstanceNo = Integer.valueOf(data.getValue());
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
	public void testGetFilteredStudyObservationsWithNoDeletedEnvironmentId() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredStudyObservations =
				this.openTrialController.getFilteredTrialObservations(workbook.getTrialObservations(), "");

		Assert.assertEquals("Expecting the number of Study observations is the same after the method call.",
				workbook.getTotalNumberOfInstances(), filteredStudyObservations.size());
	}

	@Test
	public void testGetFilteredObservations() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredObservations = this.openTrialController.getFilteredObservations(workbook.getObservations(), "2");

		Assert.assertEquals("Expecting the number of observations is decreased by " + OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				workbook.getObservations().size() - OpenTrialControllerTest.NO_OF_OBSERVATIONS, filteredObservations.size());

		// expecting the trial instance no are in incremental order
		final Integer noOfTrialInstances = OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES - 1;
		for (final MeasurementRow row : filteredObservations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (data.getMeasurementVariable() != null) {
					final MeasurementVariable var = data.getMeasurementVariable();

					if (var != null && data.getMeasurementVariable().getName() != null
							&& "TRIAL_INSTANCE".equalsIgnoreCase(var.getName())) {
						final Integer currentTrialInstanceNo = Integer.valueOf(data.getValue());
						Assert.assertTrue("Expecting trial instance the next trial instance no is within the "
								+ "possible range of trial instance no but didn't.", currentTrialInstanceNo <= noOfTrialInstances);
					}
				}
			}
		}
	}

	@Test
	public void testGetFilteredObservationsWithNoDeletedEnvironmentId() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredObservations = this.openTrialController.getFilteredObservations(workbook.getObservations(), "");

		Assert.assertEquals("Expecting the number of observations is the same after the method call.", workbook.getObservations().size(),
				filteredObservations.size());
	}

	protected void handleUnexpectedException(final Exception e) {
		Assert.fail("Unexpected error during unit test : " + e.getMessage());
	}

	protected DmsProject createDmsProject() {
		final DmsProject dmsProject = new DmsProject();
		dmsProject.setProjectId(OpenTrialControllerTest.STUDY_ID);
		dmsProject.setName(OpenTrialControllerTest.TEST_STUDY_NAME);
		dmsProject.setProgramUUID(OpenTrialControllerTest.PROGRAM_UUID);
		return dmsProject;
	}

	private Project createProject() {
		final Project project = new Project();
		project.setProjectId(OpenTrialControllerTest.WORKBENCH_PROJECT_ID);
		project.setProjectName(OpenTrialControllerTest.WORKBENCH_PROJECT_NAME);
		return project;
	}

	protected void initializeOntology() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		for (final MeasurementVariable mvar : workbook.getAllVariables()) {
			final StandardVariable stdVar = this.convertToStandardVariable(mvar);
			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(stdVar.getId(), OpenTrialControllerTest.PROGRAM_UUID))
					.thenReturn(stdVar);
		}

		final StandardVariable plotNo = this.createStandardVariable(TermId.PLOT_NO.getId(), "PLOT_NO", "Field plot", "Number", "Enumerated", 1110,
				"Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(plotNo);

		final StandardVariable repNo = this.createStandardVariable(TermId.REP_NO.getId(), "REP_NO", "Replication factor", "Number", "Enumerated", 1110,
				"Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.REP_NO.getId(), OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(repNo);

		final StandardVariable blockNo = this.createStandardVariable(TermId.BLOCK_NO.getId(), "BLOCK_NO", "Blocking factor", "Number", "Enumerated", 1110,
				"Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(blockNo);

		final StandardVariable row =
				this.createStandardVariable(TermId.ROW.getId(), "ROW", "Row in layout", "Number", "Enumerated", 1110, "Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.ROW.getId(), OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(row);

		final StandardVariable col = this.createStandardVariable(TermId.COL.getId(), "COL", "Column in layout", "Number", "Enumerated", 1110,
				"Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.COL.getId(), OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(col);

		final StandardVariable prep = this.createStandardVariable(TermId.PERCENTAGE_OF_REPLICATION.getId(), "PREP", "ED - % of test entries to replicate", "Number", "Assigned", 1110,
			"Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.PERCENTAGE_OF_REPLICATION.getId(), OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(prep);

	}

	protected StandardVariable convertToStandardVariable(final MeasurementVariable measurementVar) {
		final StandardVariable stdVar = this.createStandardVariable(measurementVar.getTermId(), measurementVar.getName(),
				measurementVar.getProperty(), measurementVar.getScale(), measurementVar.getMethod(), measurementVar.getDataTypeId(),
				measurementVar.getDataType(), measurementVar.getLabel());
		return stdVar;
	}

	protected StandardVariable createStandardVariable(final int termId, final String name, final String property, final String scale,
			final String method, final int dataTypeId, final String dataType, final String label) {
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

	@Test
	public void testPrepareExperimentalDesignTabInfo_RCBD() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "3";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, new Integer(TermId.RANDOMIZED_COMPLETE_BLOCK.getId()).toString(),
				exptDesignSourceValue, nRepValue, rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RCBD", DesignTypeItem.RANDOMIZED_COMPLETE_BLOCK.getId().intValue(),
				data.getDesignType().intValue());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RCBDWithRMap() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "3";
		final String rMapValue = new Integer(TermId.REPS_IN_SINGLE_COL.getId()).toString();
		final Integer replicationsArrangement = 1;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, new Integer(TermId.RANDOMIZED_COMPLETE_BLOCK.getId()).toString(),
				exptDesignSourceValue, nRepValue, rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RCBD", DesignTypeItem.RANDOMIZED_COMPLETE_BLOCK.getId().intValue(),
				data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications map should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RIBD() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "5";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RIBD", DesignTypeItem.RESOLVABLE_INCOMPLETE_BLOCK.getId().intValue(),
				data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RIBDL() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "3";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_BLOCK_LATIN.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RIBDL", DesignTypeItem.RESOLVABLE_INCOMPLETE_BLOCK.getId().intValue(),
				data.getDesignType().intValue());
		Assert.assertTrue("Design type should be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RRCD() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "5";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RRCD", DesignTypeItem.ROW_COL.getId().intValue(), data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RRCDL() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "3";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_ROW_COL_LATIN.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RRCDL", DesignTypeItem.ROW_COL.getId().intValue(), data.getDesignType().intValue());
		Assert.assertTrue("Design type should be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_OtherDesign() {
		final String exptDesignSourceValue = "Other design.csv";
		final String nRepValue = "2";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, new Integer(TermId.OTHER_DESIGN.getId()).toString(),
				exptDesignSourceValue, nRepValue, rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be Other Design", DesignTypeItem.CUSTOM_IMPORT.getId().intValue(),
				data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_UnknownDesign() {
		final String exptDesignSourceValue = null;
		final String nRepValue = null;
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
				OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, "12345", exptDesignSourceValue, nRepValue, rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertNull("Design type should be unknown", data.getDesignType());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_PREP() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "5";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
			new Integer(TermId.P_REP.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be P_REP", DesignTypeItem.P_REP.getId().intValue(),
			data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("% of replication should be " + percentageReplication, percentageReplication, data.getReplicationPercentage());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareMeasurementVariableTabInfoForSelectionMethod() {
		final List<MeasurementVariable> variatesList = this.createVariates();

		final boolean isUsePrevious = false;
		final TabInfo tabInfo =
				this.openTrialController.prepareMeasurementVariableTabInfo(variatesList, VariableType.SELECTION_METHOD, isUsePrevious);

		final List<SettingDetail> settings = tabInfo.getSettings();
		Assert.assertEquals("Expecting only selection methods to be included.", 1, settings.size());
		Assert.assertEquals("Expecting trait alias to be used in Setting Detail.", OpenTrialControllerTest.SELECTION_TRAIT,
				settings.get(0).getVariable().getName());
		Assert.assertEquals("Operation should be UPDATE", Operation.UPDATE, settings.get(0).getVariable().getOperation());
		Assert.assertTrue("Setting Detail should be deleteable", settings.get(0).isDeletable());
		Mockito.verify(this.userSelection).setSelectionVariates(settings);
		Mockito.verify(this.userSelection, Mockito.never()).setBaselineTraitsList(Matchers.anyListOf(SettingDetail.class));
	}

	@Test
	public void testPrepareMeasurementVariableTabInfoForTrait() {
		final List<MeasurementVariable> variatesList = this.createVariates();

		final boolean isUsePrevious = false;
		final TabInfo tabInfo = this.openTrialController.prepareMeasurementVariableTabInfo(variatesList, VariableType.TRAIT, isUsePrevious);

		final List<SettingDetail> settings = tabInfo.getSettings();
		Assert.assertEquals("Expecting only traits to be included.", 1, settings.size());
		Assert.assertEquals("Expecting trait alias to be used in Setting Detail.", OpenTrialControllerTest.TRAIT,
				settings.get(0).getVariable().getName());
		Assert.assertEquals("Operation should be UPDATE", Operation.UPDATE, settings.get(0).getVariable().getOperation());
		Assert.assertTrue("Setting Detail should be deleteable", settings.get(0).isDeletable());
		Mockito.verify(this.userSelection).setBaselineTraitsList(settings);
		Mockito.verify(this.userSelection, Mockito.never()).setSelectionVariates(Matchers.anyListOf(SettingDetail.class));
	}

	@Test
	public void testPrepareMeasurementVariableTabInfoDoUsePrevious() {
		final List<MeasurementVariable> variatesList = this.createVariates();

		final boolean isUsePrevious = true;
		final TabInfo tabInfo = this.openTrialController.prepareMeasurementVariableTabInfo(variatesList, VariableType.TRAIT, isUsePrevious);

		final List<SettingDetail> settings = tabInfo.getSettings();
		Assert.assertEquals("Expecting only traits to be included.", 1, settings.size());
		Assert.assertEquals("Expecting trait alias to be used in Setting Detail.", OpenTrialControllerTest.TRAIT,
				settings.get(0).getVariable().getName());
		Assert.assertEquals("Operation should be ADD", Operation.ADD, settings.get(0).getVariable().getOperation());
		Assert.assertTrue("Setting Detail should be deleteable", settings.get(0).isDeletable());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenTraitIsAdded() {
		final List<SettingDetail> settings =
				Lists.newArrayList(this.createSettingDetail(OpenTrialControllerTest.N_EARS_SEL, OpenTrialControllerTest.TRAIT));
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = String.valueOf(OpenTrialControllerTest.N_EARS_SEL);
		Mockito.when(this.userSelection.getBaselineTraitsList()).thenReturn(settings);

		final StandardVariable standardVariable = this.createStandardVariable(OpenTrialControllerTest.N_EARS_SEL);
		standardVariable.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.N_EARS_SEL);
		Mockito.doReturn(standardVariable).when(this.fieldbookMiddlewareService).getStandardVariable(OpenTrialControllerTest.N_EARS_SEL,
				OpenTrialControllerTest.PROGRAM_UUID);

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getStandardVariable(Matchers.anyInt(), Matchers.anyString());
		// Getting of traits called twice because of null checking
		Mockito.verify(this.userSelection, Mockito.times(2)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(1)).getSelectionVariates();
		// Verify that trait was added to list of new variables and the alias from Setting Detail was used not the standard name
		Assert.assertEquals(1, newVariables.size());
		Assert.assertEquals(OpenTrialControllerTest.TRAIT, newVariables.get(0).getName());
		Assert.assertEquals(PhenotypicType.VARIATE, newVariables.get(0).getRole());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenTraitIsAddedButNotInUserSelection() {
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = String.valueOf(OpenTrialControllerTest.N_EARS_SEL);

		final StandardVariable standardVariable = this.createStandardVariable(OpenTrialControllerTest.N_EARS_SEL);
		standardVariable.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.N_EARS_SEL);
		Mockito.doReturn(standardVariable).when(this.fieldbookMiddlewareService).getStandardVariable(OpenTrialControllerTest.N_EARS_SEL,
				OpenTrialControllerTest.PROGRAM_UUID);

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getStandardVariable(Matchers.anyInt(), Matchers.anyString());
		Mockito.verify(this.userSelection, Mockito.times(1)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(1)).getSelectionVariates();
		// Verify that trait was added to list of new variables and the standard name was used
		Assert.assertEquals(1, newVariables.size());
		Assert.assertEquals(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.N_EARS_SEL,
				newVariables.get(0).getName());
		Assert.assertEquals(PhenotypicType.VARIATE, newVariables.get(0).getRole());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenSelectionMethodIsAdded() {
		final List<SettingDetail> settings = Lists
				.newArrayList(this.createSettingDetail(OpenTrialControllerTest.BM_CODE_VTE_ID, OpenTrialControllerTest.SELECTION_TRAIT));
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = String.valueOf(OpenTrialControllerTest.BM_CODE_VTE_ID);
		Mockito.when(this.userSelection.getSelectionVariates()).thenReturn(settings);

		final StandardVariable standardVariable = this.createStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID);
		standardVariable.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.BM_CODE_VTE_ID);
		Mockito.doReturn(standardVariable).when(this.fieldbookMiddlewareService).getStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID,
				OpenTrialControllerTest.PROGRAM_UUID);

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getStandardVariable(Matchers.anyInt(), Matchers.anyString());
		// Getting of selection methods called twice because of null checking
		Mockito.verify(this.userSelection, Mockito.times(1)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(2)).getSelectionVariates();
		// Verify that selection method was added to list of new variables and the alias from Setting Detail was used not the standard name
		Assert.assertEquals(1, newVariables.size());
		Assert.assertEquals(OpenTrialControllerTest.SELECTION_TRAIT, newVariables.get(0).getName());
		Assert.assertEquals(PhenotypicType.VARIATE, newVariables.get(0).getRole());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenSelectionMethodIsAddedButNotInUserSelection() {
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = String.valueOf(OpenTrialControllerTest.BM_CODE_VTE_ID);

		final StandardVariable standardVariable = this.createStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID);
		standardVariable.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.BM_CODE_VTE_ID);
		Mockito.doReturn(standardVariable).when(this.fieldbookMiddlewareService).getStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID,
				OpenTrialControllerTest.PROGRAM_UUID);

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getStandardVariable(Matchers.anyInt(), Matchers.anyString());
		Mockito.verify(this.userSelection, Mockito.times(1)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(1)).getSelectionVariates();
		// Verify that selection method was added to list of new variables and the standard name was used
		Assert.assertEquals(1, newVariables.size());
		Assert.assertEquals(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.BM_CODE_VTE_ID,
				newVariables.get(0).getName());
		Assert.assertEquals(PhenotypicType.VARIATE, newVariables.get(0).getRole());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenTraitAndSelectionMethodAdded() {
		final List<SettingDetail> traitSettings = Lists
				.newArrayList(this.createSettingDetail(OpenTrialControllerTest.BM_CODE_VTE_ID, OpenTrialControllerTest.SELECTION_TRAIT));
		Mockito.when(this.userSelection.getSelectionVariates()).thenReturn(traitSettings);
		final List<SettingDetail> selectionMethodSettings =
				Lists.newArrayList(this.createSettingDetail(OpenTrialControllerTest.N_EARS_SEL, OpenTrialControllerTest.TRAIT));
		Mockito.when(this.userSelection.getBaselineTraitsList()).thenReturn(selectionMethodSettings);
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = OpenTrialControllerTest.BM_CODE_VTE_ID + "," + OpenTrialControllerTest.N_EARS_SEL;

		final StandardVariable standardVariable1 = this.createStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID);
		standardVariable1.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.BM_CODE_VTE_ID);
		Mockito.doReturn(standardVariable1).when(this.fieldbookMiddlewareService)
				.getStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID, OpenTrialControllerTest.PROGRAM_UUID);
		final StandardVariable standardVariable2 = this.createStandardVariable(OpenTrialControllerTest.N_EARS_SEL);
		standardVariable2.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.N_EARS_SEL);
		Mockito.doReturn(standardVariable2).when(this.fieldbookMiddlewareService).getStandardVariable(OpenTrialControllerTest.N_EARS_SEL,
				OpenTrialControllerTest.PROGRAM_UUID);

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(2)).getStandardVariable(Matchers.anyInt(), Matchers.anyString());
		// Getting of trait and selection methods called twice because of null checking
		Mockito.verify(this.userSelection, Mockito.times(2)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(2)).getSelectionVariates();
		// Verify that trait and selection method was added to list of new variables and the alias from Setting Detail was used not the
		// standard name
		Assert.assertEquals(2, newVariables.size());
		final MeasurementVariable measurementVariable1 = newVariables.get(0);
		Assert.assertEquals(OpenTrialControllerTest.BM_CODE_VTE_ID, measurementVariable1.getTermId());
		Assert.assertEquals(OpenTrialControllerTest.SELECTION_TRAIT, measurementVariable1.getName());
		Assert.assertEquals(PhenotypicType.VARIATE, measurementVariable1.getRole());
		final MeasurementVariable measurementVariable2 = newVariables.get(1);
		Assert.assertEquals(OpenTrialControllerTest.N_EARS_SEL, measurementVariable2.getTermId());
		Assert.assertEquals(OpenTrialControllerTest.TRAIT, measurementVariable2.getName());
		Assert.assertEquals(PhenotypicType.VARIATE, measurementVariable2.getRole());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenIdListCsvIsNullOrEmpty() {
		final List<MeasurementVariable> newVariables = new ArrayList<>();

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, "");
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.never()).getStandardVariable(Matchers.anyInt(), Matchers.anyString());
		Mockito.verify(this.userSelection, Mockito.never()).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.never()).getSelectionVariates();
		Assert.assertTrue(newVariables.isEmpty());

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, null);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.never()).getStandardVariable(Matchers.anyInt(), Matchers.anyString());
		Mockito.verify(this.userSelection, Mockito.never()).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.never()).getSelectionVariates();
		Assert.assertTrue(newVariables.isEmpty());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenAllTraitsAlreadyInWorkbook() {
		final List<MeasurementVariable> variablesList = this.createVariates();
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = OpenTrialControllerTest.BM_CODE_VTE_ID + "," + OpenTrialControllerTest.N_EARS_SEL;

		this.openTrialController.getTraitsAndSelectionVariates(variablesList, newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.never()).getStandardVariable(Matchers.anyInt(), Matchers.anyString());
		Mockito.verify(this.userSelection, Mockito.times(1)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(1)).getSelectionVariates();
		// Verify that existing variables are reused
		Assert.assertEquals(variablesList, newVariables);
	}

	private void createTestVariable() {
		this.testVariable = new Variable();
		this.testVariable.setId(UnitTestDaoIDGenerator.generateId(Variable.class));
		this.testVariable.setName(OpenTrialControllerTest.VARIABLE_NAME);
		this.testVariable.setMethod(TestDataHelper.createMethod());
		this.testVariable.setProperty(TestDataHelper.createProperty());
		this.testVariable.setScale(TestDataHelper.createScale());
	}

	private List<MeasurementVariable> createVariates() {
		final List<MeasurementVariable> variables = new ArrayList<>();
		variables.add(this.createMeasurementVariable(OpenTrialControllerTest.BM_CODE_VTE_ID, OpenTrialControllerTest.SELECTION_TRAIT,
				"Breeding Method", "BMETH_CODE", "Observed", "VARIATE", VariableType.SELECTION_METHOD));
		variables.add(this.createMeasurementVariable(OpenTrialControllerTest.N_EARS_SEL, OpenTrialControllerTest.TRAIT, "Number of Ears",
				"Number", "Selected", "VARIATE", VariableType.TRAIT));
		return variables;
	}

	private MeasurementVariable createMeasurementVariable(final int termId, final String name, final String property, final String scale,
			final String method, final String label, final VariableType variableType) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setName(name);
		measurementVariable.setLabel(label);
		measurementVariable.setProperty(property);
		measurementVariable.setScale(scale);
		measurementVariable.setMethod(method);
		measurementVariable.setVariableType(variableType);
		measurementVariable.setRole(variableType.getRole());
		return measurementVariable;
	}

	private SettingDetail createSettingDetail(final Integer id, final String name) {
		final SettingDetail settingDetail = new SettingDetail();
		final SettingVariable settingVariable = new SettingVariable();
		settingVariable.setCvTermId(id);
		settingVariable.setName(name);
		settingDetail.setVariable(settingVariable);

		return settingDetail;
	}

	@Test
	public void testGetAdvancedList() {
		final GermplasmList germplasm = new GermplasmList();
		germplasm.setId(501);
		germplasm.setName("Advance Study List");

		final List<GermplasmList> germplasmList = new ArrayList<>();
		germplasmList.add(germplasm);

		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(Matchers.anyInt(), Matchers.any(GermplasmListType.class)))
				.thenReturn(germplasmList);

		final List<AdvanceList> advancedList = this.openTrialController.getAdvancedList(germplasm.getId());

		Assert.assertEquals("Advance List size", 1, advancedList.size());
		Assert.assertEquals("Advance List Id: ", germplasm.getId(), advancedList.get(0).getId());
		Assert.assertEquals("Advance List Name: ", germplasm.getName(), advancedList.get(0).getName());
	}

	@Test
	public void testUpdateSavedTrial() throws ParseException {
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyTypeDto.getTrialDto());
		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(OpenTrialControllerTest.STUDY_ID)).thenReturn(workbook);
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(Matchers.anyInt(), Matchers.anyString()))
			.thenReturn(StandardVariableTestDataInitializer.createStandardVariable(1, "STD"));
		final Study study = new Study();
		study.setStudyType(StudyTypeDto.getTrialDto());

		Mockito.when(
				this.variableDataManager.getVariable(Matchers.anyString(), Matchers.anyInt(), Matchers.anyBoolean()))
				.thenReturn(VariableTestDataInitializer.createVariable());

		// Verify that workbook has Analysis and/or Analysis Summary variables
		// beforehand to check that they were later removed
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConditions()));
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConstants()));

		final Map<String, Object> resultMap = this.openTrialController.updateSavedTrial(OpenTrialControllerTest.STUDY_ID);
		Assert.assertNotNull(resultMap.get(OpenTrialController.ENVIRONMENT_DATA_TAB));
		Assert.assertNotNull(resultMap.get(OpenTrialController.MEASUREMENT_DATA_EXISTING));
		Assert.assertNotNull(resultMap.get(OpenTrialController.HAS_ADVANCED_OR_CROSSES_LIST));
		Assert.assertNotNull(resultMap.get(OpenTrialController.MEASUREMENT_ROW_COUNT));
		Assert.assertNotNull(resultMap.get(OpenTrialController.MEASUREMENTS_DATA));
		Assert.assertNotNull(resultMap.get(OpenTrialController.SELECTION_VARIABLE_DATA));
		Assert.assertNotNull(resultMap.get(OpenTrialController.TRIAL_SETTINGS_DATA));

		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).loadAllObservations(workbook);
		Mockito.verify(this.userSelection, Mockito.times(1)).setWorkbook(workbook);
		Mockito.verify(this.userSelection, Mockito.times(1))
				.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(workbook.getConditions()));
		Mockito.verify(this.userSelection, Mockito.times(1))
				.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(this.userSelection.getExperimentalDesignVariables()));
		Mockito.verify(this.fieldbookMiddlewareService).hasAdvancedOrCrossesList(Matchers.anyInt());

		// Verify that Analysis and/or Analysis Summary variables are removed
		Assert.assertFalse(this.hasAnalysisVariables(workbook.getConditions()));
		Assert.assertFalse(this.hasAnalysisVariables(workbook.getConstants()));
	}

	@Test
	public void testAssignOperationOnExpDesignVariablesForExistingTrialWithoutExperimentalDesign() {
		final List<MeasurementVariable> conditions = this.initMeasurementVariableList();

		this.openTrialController.assignOperationOnExpDesignVariables(conditions);

		for (final MeasurementVariable var : conditions) {
			Assert.assertEquals("Expecting that the experimental variable's operation still set to ADD", var.getOperation(), Operation.ADD);
		}
	}

	@Test
	public void testAssignOperationOnExpDesignVariablesForExistingTrialWithExperimentalDesign() {
		final VariableTypeList factors = Mockito.mock(VariableTypeList.class);
		Mockito.when(factors.findById(Matchers.anyInt())).thenReturn(new DMSVariableType());
		Mockito.when(this.studyDataManager.getAllStudyFactors(Matchers.anyInt())).thenReturn(factors);

		final List<MeasurementVariable> conditions = this.initMeasurementVariableList();
		this.openTrialController.assignOperationOnExpDesignVariables(conditions);

		for (final MeasurementVariable var : conditions) {
			Assert.assertEquals("Expecting that the experimental variable's operation is now set to UPDATE", var.getOperation(),
				Operation.UPDATE);
		}
	}

	@Test
	public void testSetUserSelectionImportedGermplasmMainInfoGermplasmListIsNotEmpty() {

		final int germplasmListId = 111;
		final int germplasmListRef = 222;
		final int studyId = 1;
		final long checkCount = 23;
		final int germplasmCount = 1;

		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(germplasmListId);
		germplasmList.setListRef(germplasmListRef);

		final List<GermplasmList> listOfGermplasmList = new ArrayList<>();
		listOfGermplasmList.add(germplasmList);

		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.STUDY))
				.thenReturn(listOfGermplasmList);
		final ListDataProject listDataProject = ListDataProjectTestDataInitializer.createListDataProject(germplasmList, 0, 0, 1,
				"entryCode", "seedSource", "designation", "groupName", "duplicate", "notes", 20170125);
		listDataProject.setGroupId(12);

		Mockito.when(this.fieldbookMiddlewareService.getListDataProject(germplasmListId)).thenReturn(Lists.newArrayList(listDataProject));

		Mockito.when(this.fieldbookMiddlewareService.countListDataProjectByListIdAndEntryType(germplasmListId,
				SystemDefinedEntryType.CHECK_ENTRY)).thenReturn(checkCount);

		final Map<Integer, String> mockData = Maps.newHashMap();
		mockData.put(0, "StockID101, StockID102");
		Mockito.when(this.inventoryDataManager.retrieveStockIds(Matchers.anyListOf(Integer.class))).thenReturn(mockData);

		final Model model = new ExtendedModelMap();

		final UserSelection userSelection = new UserSelection();

		this.openTrialController.setUserSelectionImportedGermplasmMainInfo(userSelection, 1, model);

		final ImportedGermplasmMainInfo importedGermplasmMainInfo = userSelection.getImportedGermplasmMainInfo();

		Assert.assertEquals(germplasmListRef, importedGermplasmMainInfo.getListId().intValue());
		Assert.assertTrue(importedGermplasmMainInfo.isAdvanceImportType());
		Assert.assertNotNull(importedGermplasmMainInfo.getImportedGermplasmList());
		Assert.assertTrue(userSelection.isImportValid());

		Assert.assertEquals(Integer.valueOf(germplasmCount), model.asMap().get(OpenTrialControllerTest.GERMPLASM_LIST_SIZE));
		Assert.assertEquals(checkCount, model.asMap().get(OpenTrialControllerTest.GERMPLASM_CHECKS_SIZE));

		final ImportedGermplasm importedGermplasm = importedGermplasmMainInfo.getImportedGermplasmList().getImportedGermplasms().get(0);
		Assert.assertEquals("0", importedGermplasm.getEntryTypeValue());
		Assert.assertEquals(0, importedGermplasm.getEntryTypeCategoricalID().intValue());
		Assert.assertEquals("groupName", importedGermplasm.getCross());
		Assert.assertEquals("groupName", importedGermplasm.getCross());
		Assert.assertEquals("entryCode", importedGermplasm.getEntryCode());
		Assert.assertEquals(1, importedGermplasm.getEntryId().intValue());
		Assert.assertEquals("0", importedGermplasm.getGid());
		Assert.assertEquals(12, importedGermplasm.getMgid().intValue());
		Assert.assertEquals("seedSource", importedGermplasm.getSource());
		Assert.assertEquals(12, importedGermplasm.getGroupId().intValue());
		Assert.assertEquals("StockID101, StockID102", importedGermplasm.getStockIDs());

	}

	@Test
	public void testSubmitWhereReplaceIsNotZero() {
		final TrialData data = this.setUpTrialData();
		final Map<String, Object> returnVal = this.openTrialController.submit(1, data);

		Assert.assertNotNull("The environment data tab should not be null", returnVal.get(OpenTrialController.ENVIRONMENT_DATA_TAB));
		Assert.assertEquals("The measurement data flag should be false", false,
				returnVal.get(OpenTrialController.MEASUREMENT_DATA_EXISTING));
		Assert.assertEquals("There should be no advanced or crosses list.", false,
				returnVal.get(OpenTrialController.HAS_ADVANCED_OR_CROSSES_LIST));
		Assert.assertEquals("The measurement row count should be zero", 0, returnVal.get(OpenTrialController.MEASUREMENT_ROW_COUNT));
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(0)).saveMeasurementRows(Matchers.any(Workbook.class),
				Matchers.anyString(), Matchers.anyBoolean());
		Mockito.verify(this.fieldbookService, Mockito.times(0)).createIdNameVariablePairs(Matchers.any(Workbook.class), Matchers.anyListOf(
						SettingDetail.class),
				Matchers.anyString(), Matchers.anyBoolean());
		Mockito.verify(this.fieldbookService, Mockito.times(0)).saveStudyColumnOrdering(Matchers.anyInt(), Matchers.anyString(),
				Matchers.anyString(), Matchers.any(Workbook.class));
	}

	@Test
	public void testSubmitWhereReplaceIsZero() {
		final TrialData data = this.setUpTrialData();
		Mockito.when(this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(Matchers.eq(1), Matchers.anyListOf(Integer.class))).thenReturn(true);
		final long experimentCount = 10;
		Mockito.when(this.studyDataManager.countExperiments(Matchers.eq(1))).thenReturn(experimentCount);

		Mockito.when(this.studyDataManager.getStudyTypeByName(Mockito.anyString())).thenReturn(StudyTypeDto.getTrialDto());
		data.setBasicDetails(new BasicDetails());
		data.getBasicDetails().setStudyType(StudyTypeDto.getTrialDto());
		final Map<String, Object> returnVal = this.openTrialController.submit(0, data);

		Assert.assertNotNull("The environment data tab should not be null", returnVal.get(OpenTrialController.ENVIRONMENT_DATA_TAB));
		Assert.assertEquals("The measurement data flag should be true", true, returnVal.get(OpenTrialController.MEASUREMENT_DATA_EXISTING));
		Assert.assertEquals("The measurement row count should be " + experimentCount, experimentCount,
				returnVal.get(OpenTrialController.MEASUREMENT_ROW_COUNT));

		Mockito.verify(this.fieldbookMiddlewareService).saveMeasurementRows(Matchers.any(Workbook.class), Matchers.anyString(),
				Matchers.anyBoolean());
		Mockito.verify(this.fieldbookService).createIdNameVariablePairs(Matchers.any(Workbook.class), Matchers.anyListOf(
				SettingDetail.class),
				Matchers.anyString(), Matchers.anyBoolean());
		Mockito.verify(this.fieldbookService).saveStudyColumnOrdering(Matchers.anyInt(), ArgumentMatchers.<String>isNull(), ArgumentMatchers.<String>isNull(),
				Matchers.any(Workbook.class));
		Mockito.verify(this.fieldbookMiddlewareService).hasAdvancedOrCrossesList(Matchers.anyInt());
	}

	private TrialData setUpTrialData() {
		final TrialData data = Mockito.mock(TrialData.class);
		Mockito.when(data.getEnvironments()).thenReturn(DesignImportTestDataInitializer.createEnvironmentData(1));
		final BasicDetails basicDetails = Mockito.mock(BasicDetails.class);
		Mockito.when(basicDetails.getBasicDetails()).thenReturn(new HashMap<String, String>());
		Mockito.when(data.getBasicDetails()).thenReturn(basicDetails);
		Mockito.when(data.getBasicDetails().getStudyType()).thenReturn(StudyTypeDto.getTrialDto());
		final TrialSettingsBean trialSettings = Mockito.mock(TrialSettingsBean.class);
		Mockito.when(trialSettings.getUserInput()).thenReturn(new HashMap<String, String>());
		Mockito.when(data.getTrialSettings()).thenReturn(trialSettings);
		final TreatmentFactorTabBean treatmentFactor = Mockito.mock(TreatmentFactorTabBean.class);
		Mockito.when(treatmentFactor.getCurrentData()).thenReturn(new HashMap<String, TreatmentFactorData>());
		Mockito.when(data.getTreatmentFactors()).thenReturn(treatmentFactor);
		return data;
	}

	@Test
	public void testSetUserSelectionImportedGermplasmMainInfoGermplasmListIsEmpty() {

		final int germplasmListId = 111;
		final int studyId = 1;

		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(0)).getListDataProject(germplasmListId);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(0)).countListDataProjectByListIdAndEntryType(germplasmListId,
				SystemDefinedEntryType.CHECK_ENTRY);

		Assert.assertNull(this.userSelection.getImportedGermplasmMainInfo());
		Assert.assertFalse(this.userSelection.isImportValid());
		Assert.assertFalse(this.model.containsAttribute(OpenTrialControllerTest.GERMPLASM_LIST_SIZE));
		Assert.assertFalse(this.model.containsAttribute(OpenTrialControllerTest.GERMPLASM_CHECKS_SIZE));

	}

	@Test
	public void testSetUserSelectionImportedGermplasmMainInfoGermplasmListIsNotEmptyButListDataIsEmpty() {

		final int germplasmListId = 111;
		final int studyId = 1;

		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(germplasmListId);

		final List<GermplasmList> listOfGermplasmList = new ArrayList<>();
		listOfGermplasmList.add(germplasmList);

		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(0)).getListDataProject(germplasmListId);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(0)).countListDataProjectByListIdAndEntryType(germplasmListId,
				SystemDefinedEntryType.CHECK_ENTRY);

		Assert.assertNull(this.userSelection.getImportedGermplasmMainInfo());
		Assert.assertFalse(this.userSelection.isImportValid());
		Assert.assertFalse(this.model.containsAttribute(OpenTrialControllerTest.GERMPLASM_LIST_SIZE));
		Assert.assertFalse(this.model.containsAttribute(OpenTrialControllerTest.GERMPLASM_CHECKS_SIZE));

	}

	@Test
	public void testSetModelAttributes() throws ParseException {
		final Workbook testWorkbook = WorkbookTestDataInitializer.getTestWorkbook();
		this.openTrialController.setModelAttributes(createTrialForm, 1010, model, testWorkbook);
		Mockito.verify(this.model).addAttribute(Matchers.eq("basicDetailsData"), Matchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(Matchers.eq("germplasmData"), Matchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(Matchers.eq(OpenTrialController.ENVIRONMENT_DATA_TAB), Matchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(Matchers.eq(OpenTrialController.TRIAL_SETTINGS_DATA),
				Matchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(Matchers.eq(OpenTrialController.MEASUREMENTS_DATA),
				Matchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(Matchers.eq(OpenTrialController.SELECTION_VARIABLE_DATA),
				Matchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(Matchers.eq("experimentalDesignData"), Matchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(Matchers.eq(OpenTrialController.MEASUREMENT_DATA_EXISTING), Matchers.anyBoolean());
		Mockito.verify(this.model).addAttribute(Matchers.eq(OpenTrialController.HAS_ADVANCED_OR_CROSSES_LIST), Matchers.anyBoolean());
		Mockito.verify(this.model).addAttribute(Matchers.eq(OpenTrialController.MEASUREMENT_ROW_COUNT),
				Matchers.anyLong());
		Mockito.verify(this.model).addAttribute(Matchers.eq("treatmentFactorsData"), Matchers.any(TabInfo.class));
        Mockito.verify(this.model).addAttribute(Matchers.eq("studyTypes"), Matchers.anyListOf(StudyType.class));
		Mockito.verify(this.model).addAttribute("createTrialForm", createTrialForm);
		Mockito.verify(this.model).addAttribute(Matchers.eq("experimentalDesignSpecialData"), Matchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute("studyName", testWorkbook.getStudyDetails().getLabel());
		Mockito.verify(this.model).addAttribute("description", testWorkbook.getStudyDetails().getDescription());
		Mockito.verify(this.model).addAttribute(Matchers.eq("advancedList"), Matchers.anyListOf(AdvanceList.class));
		Mockito.verify(this.model).addAttribute(Matchers.eq("sampleList"), Matchers.anyListOf(SampleListDTO.class));
		Mockito.verify(this.model).addAttribute(Matchers.eq("crossesList"), Matchers.anyListOf(CrossesList.class));
		Mockito.verify(this.model).addAttribute("germplasmListSize", 0);
		Mockito.verify(this.model).addAttribute(Matchers.eq("isSuperAdmin"), Matchers.anyBoolean());
	}

	@Test
	public void testPrepareBasicDetailsTabInfo() throws ParseException {
		final Integer trialID = 1011;
		final StudyDetails studyDetails = createTestStudyDetails(trialID);
		final String startDate = Util.convertDate(studyDetails.getStartDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String endDate = Util.convertDate(studyDetails.getEndDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String updateDate = Util.convertDate(studyDetails.getStudyUpdate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String ownerName = RandomStringUtils.randomAlphanumeric(20);
		final String folderName = RandomStringUtils.randomAlphanumeric(20);
		Mockito.doReturn(folderName).when(this.fieldbookMiddlewareService).getFolderNameById(Matchers.anyInt());
		Mockito.doReturn(ownerName).when(this.fieldbookService).getPersonByUserId(Matchers.anyInt());

		final TabInfo tabInfo = this.openTrialController.prepareBasicDetailsTabInfo(studyDetails, false, trialID);
		final BasicDetails basicData = (BasicDetails) tabInfo.getData();
		Assert.assertNotNull(basicData);
		this.verifyBasicDetailsInfo(studyDetails, startDate, endDate, updateDate, Integer.valueOf(studyDetails.getCreatedBy()), ownerName,
				folderName, basicData);

		this.verifyUserSelectionUponBasicDetailsPreparation(studyDetails);
	}

	@Test
	public void testPrepareBasicDetailsTabInfoWithNullDates() throws ParseException {
		final Integer trialID = 1011;
		final StudyDetails studyDetails = createTestStudyDetails(trialID);
		studyDetails.setEndDate(null);
		studyDetails.setStudyUpdate(null);
		final String startDate = Util.convertDate(studyDetails.getStartDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String ownerName = RandomStringUtils.randomAlphanumeric(20);
		final String folderName = RandomStringUtils.randomAlphanumeric(20);
		Mockito.doReturn(folderName).when(this.fieldbookMiddlewareService).getFolderNameById(Matchers.anyInt());
		Mockito.doReturn(ownerName).when(this.fieldbookService).getPersonByUserId(Matchers.anyInt());

		final TabInfo tabInfo = this.openTrialController.prepareBasicDetailsTabInfo(studyDetails, false, trialID);
		final BasicDetails basicData = (BasicDetails) tabInfo.getData();
		Assert.assertNotNull(basicData);
		this.verifyBasicDetailsInfo(studyDetails, startDate, StringUtils.EMPTY, StringUtils.EMPTY,
				Integer.valueOf(studyDetails.getCreatedBy()), ownerName, folderName, basicData);

		this.verifyUserSelectionUponBasicDetailsPreparation(studyDetails);
	}

	@Test
	public void testPrepareBasicDetailsTabInfoWithNoCreatorInfo() throws ParseException {
		final Integer trialID = 1011;
		final StudyDetails studyDetails = createTestStudyDetails(trialID);
		studyDetails.setCreatedBy("");
		final String startDate = Util.convertDate(studyDetails.getStartDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String endDate = Util.convertDate(studyDetails.getEndDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String updateDate = Util.convertDate(studyDetails.getStudyUpdate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String folderName = RandomStringUtils.randomAlphanumeric(20);
		Mockito.doReturn(folderName).when(this.fieldbookMiddlewareService).getFolderNameById(Matchers.anyInt());

		final TabInfo tabInfo = this.openTrialController.prepareBasicDetailsTabInfo(studyDetails, false, trialID);
		final BasicDetails basicData = (BasicDetails) tabInfo.getData();
		Assert.assertNotNull(basicData);
		this.verifyBasicDetailsInfo(studyDetails, startDate, endDate, updateDate, null, StringUtils.EMPTY, folderName, basicData);

		this.verifyUserSelectionUponBasicDetailsPreparation(studyDetails);
	}

	@Test
	public void testPrepareBasicDetailsTabInfoWhenParentFolderIsRootFolder() throws ParseException {
		final Integer trialID = 1011;
		final StudyDetails studyDetails = createTestStudyDetails(trialID);
		studyDetails.setParentFolderId(DmsProject.SYSTEM_FOLDER_ID);
		final String startDate = Util.convertDate(studyDetails.getStartDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String endDate = Util.convertDate(studyDetails.getEndDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String updateDate = Util.convertDate(studyDetails.getStudyUpdate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String ownerName = RandomStringUtils.randomAlphanumeric(20);
		Mockito.doReturn(ownerName).when(this.fieldbookService).getPersonByUserId(Matchers.anyInt());

		final TabInfo tabInfo = this.openTrialController.prepareBasicDetailsTabInfo(studyDetails, false, trialID);
		final BasicDetails basicData = (BasicDetails) tabInfo.getData();
		Assert.assertNotNull(basicData);
		this.verifyBasicDetailsInfo(studyDetails, startDate, endDate, updateDate, Integer.valueOf(studyDetails.getCreatedBy()), ownerName,
				AppConstants.STUDIES.getString(), basicData);

		this.verifyUserSelectionUponBasicDetailsPreparation(studyDetails);
	}

	private void verifyUserSelectionUponBasicDetailsPreparation(final StudyDetails studyDetails) {
		Mockito.verify(this.userSelection).setBasicDetails(Matchers.anyListOf(SettingDetail.class));
		Mockito.verify(this.userSelection).setStudyName(studyDetails.getStudyName());
		Mockito.verify(this.userSelection).setStudyDescription(studyDetails.getDescription());
		Mockito.verify(this.userSelection).setStudyStartDate(studyDetails.getStartDate());
		Mockito.verify(this.userSelection).setStudyEndDate(studyDetails.getEndDate());
		Mockito.verify(this.userSelection).setStudyUpdate(studyDetails.getStudyUpdate());
		Mockito.verify(this.userSelection).setStudyObjective(studyDetails.getObjective());
		Mockito.verify(this.userSelection).setStudyType(studyDetails.getStudyType().getName());
	}

	private void verifyBasicDetailsInfo(final StudyDetails studyDetails, final String startDate, final String endDate,
			final String updateDate, final Integer userId, final String ownerName, final String folderName, final BasicDetails basicData) {
		// for this test asserting empty as all required fields are present
		Assert.assertTrue(basicData.getBasicDetails().isEmpty());
		Assert.assertEquals(studyDetails.getId(), basicData.getStudyID());
		Assert.assertEquals(studyDetails.getStudyName(), basicData.getStudyName());
		Assert.assertEquals(studyDetails.getDescription(), basicData.getDescription());
		Assert.assertEquals(studyDetails.getObjective(), basicData.getObjective());
		Assert.assertEquals(startDate, basicData.getStartDate());
		Assert.assertEquals(endDate, basicData.getEndDate());
		Assert.assertEquals(updateDate, basicData.getStudyUpdate());
		Assert.assertEquals(studyDetails.getStudyType(), basicData.getStudyType());
		Assert.assertEquals(studyDetails.getParentFolderId(), basicData.getFolderId().longValue());
		Assert.assertEquals(folderName, basicData.getFolderName());
		Assert.assertEquals(folderName, basicData.getFolderNameLabel());
		Assert.assertEquals(userId, basicData.getUserID());
		Assert.assertEquals(ownerName, basicData.getUserName());
		Assert.assertTrue(basicData.getIsLocked());
	}

	private StudyDetails createTestStudyDetails(final int trialID) {
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(trialID);
		studyDetails.setStudyName(RandomStringUtils.randomAlphanumeric(20));
		studyDetails.setDescription(RandomStringUtils.randomAlphanumeric(20));
		studyDetails.setObjective(RandomStringUtils.randomAlphanumeric(20));
		studyDetails.setStartDate("20110915");
		studyDetails.setEndDate("20140421");
		studyDetails.setStudyUpdate("20160601");
		studyDetails.setStudyType(StudyTypeDto.getNurseryDto());
		studyDetails.setIsLocked(true);
		final int parentFolderId = 125;
		studyDetails.setParentFolderId(parentFolderId);
		final String createdBy = "210";
		studyDetails.setCreatedBy(createdBy);
		return studyDetails;
	}

	private List<MeasurementVariable> initMeasurementVariableList() {
		final List<MeasurementVariable> conditions = new ArrayList<MeasurementVariable>();
		conditions.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), "10110"));
		conditions
				.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), "SampleFile.csv"));
		conditions.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.NUMBER_OF_REPLICATES.getId(), "2"));
		conditions.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.PERCENTAGE_OF_REPLICATION.getId(), "50"));

		for (final MeasurementVariable var : conditions) {
			var.setOperation(Operation.ADD);
		}
		return conditions;
	}
}
