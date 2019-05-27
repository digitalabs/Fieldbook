
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.LocationTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.MethodTestDataInitializer;
import org.generationcp.middleware.data.initializer.PersonTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.ValueReferenceTestDataInitializer;
import org.generationcp.middleware.data.initializer.VariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.PossibleValuesCache;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import org.generationcp.commons.constant.AppConstants;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class FieldbookServiceTest {

	private static final String LABBR = "labbr";
	private static final String METHOD_DESCRIPTION = "Method Description 5";
	private static final String LOCATION_NAME = "Loc1";
	private static final String PROGRAMUUID = "1000001";
	private static final String CHECK = "CHECK";
	private static final String DESIG = "DESIG";
	private static final String CATEGORICAL_VARIABLE = "Categorical variable";
	private static final String CODE = "Code";
	private static final int CODE_ID = 6050;
	private static final String ASSIGNED = "Assigned";
	private static final int ASSIGNED_ID = 4030;
	private static final String ED_CHECK_PLAN = "ED - Check Plan";
	private static final int CHECK_PLAN_PROPERTY_ID = 2155;
	private static final int CHECK_INTERVAL_PROPERTY_ID = 2154;
	private static final String ED_CHECK_INTERVAL = "ED - Check Interval";
	private static final String CHECK_INTERVAL = "CHECK_INTERVAL";
	private static final String CHECK_START = "CHECK_START";
	private static final String STUDY_DESIGN = "Study Design";
	private static final int STUDY_DESIGN_ID = 1100;
	private static final String STUDY_ENVIRONMENT_INFORMATION = "Study Environment Information";
	private static final int ENV_ID = 1020;
	private static final String NUMERIC_VARIABLE = "Numeric variable";
	private static final String FIELD_STUDY = "Field study";
	private static final int FIELD_STUDY_ID = 4100;
	private static final String NUMBER = "Number";
	private static final int NUMBER_ID = 6040;
	private static final String ED_CHECK_START = "ED - Check Start";
	private static final int CHECK_START_PROPERTY_ID = 2153;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@InjectMocks
	private FieldbookServiceImpl fieldbookServiceImpl;

	private MeasurementVariable locationVariable;
	private MeasurementVariable nonLocationVariable;

	private PossibleValuesCache possibleValuesCache;

	@Before
	public void setUp() throws MiddlewareException {
		final List<Location> allLocation = new ArrayList<Location>();

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(FieldbookServiceTest.PROGRAMUUID);
		allLocation.add(LocationTestDataInitializer.createLocation(1, FieldbookServiceTest.LOCATION_NAME, null));
		allLocation.add(LocationTestDataInitializer.createLocation(2, "Loc2", null));
		Mockito.when(this.fieldbookMiddlewareService.getLocationsByProgramUUID(FieldbookServiceTest.PROGRAMUUID))
				.thenReturn(allLocation);
		Mockito.when(this.fieldbookMiddlewareService.getAllBreedingLocations()).thenReturn(new ArrayList<Location>());

		final List<Person> personsList = new ArrayList<Person>();
		personsList.add(PersonTestDataInitializer.createPerson(200));

		this.fieldbookServiceImpl.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		this.possibleValuesCache = new PossibleValuesCache();
		this.fieldbookServiceImpl.setPossibleValuesCache(this.possibleValuesCache);
		this.fieldbookServiceImpl.setOntologyVariableDataManager(this.ontologyVariableDataManager);
		this.fieldbookServiceImpl.setContextUtil(this.contextUtil);

		final List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		for (int i = 0; i < 5; i++) {
			possibleValues.add(new ValueReference(i, "Name: " + i));
		}

		this.locationVariable = new MeasurementVariable();
		this.nonLocationVariable = new MeasurementVariable();

		this.locationVariable.setTermId(TermId.LOCATION_ID.getId());
		this.nonLocationVariable.setTermId(TermId.PI_ID.getId());
		this.nonLocationVariable.setPossibleValues(possibleValues);

		this.fieldbookServiceImpl.setContextUtil(this.contextUtil);
		this.setUpStandardVariablesForChecks();
	}

	private void setUpStandardVariablesForChecks() throws MiddlewareException {
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_START.getId(),
				FieldbookServiceTest.PROGRAMUUID))
				.thenReturn(StandardVariableTestDataInitializer.createStandardVariable(
						new Term(FieldbookServiceTest.CHECK_START_PROPERTY_ID, FieldbookServiceTest.ED_CHECK_START,
								FieldbookServiceTest.ED_CHECK_START),
						new Term(FieldbookServiceTest.NUMBER_ID, FieldbookServiceTest.NUMBER,
								FieldbookServiceTest.NUMBER),
						new Term(FieldbookServiceTest.FIELD_STUDY_ID, FieldbookServiceTest.FIELD_STUDY,
								FieldbookServiceTest.FIELD_STUDY),
						new Term(TermId.NUMERIC_VARIABLE.getId(), FieldbookServiceTest.NUMERIC_VARIABLE,
								FieldbookServiceTest.NUMERIC_VARIABLE),
						new Term(FieldbookServiceTest.ENV_ID, FieldbookServiceTest.STUDY_ENVIRONMENT_INFORMATION,
								FieldbookServiceTest.STUDY_ENVIRONMENT_INFORMATION),
						new Term(FieldbookServiceTest.STUDY_DESIGN_ID, FieldbookServiceTest.STUDY_DESIGN,
								FieldbookServiceTest.STUDY_DESIGN),
						PhenotypicType.TRIAL_ENVIRONMENT, TermId.CHECK_START.getId(),
						FieldbookServiceTest.CHECK_START));
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_INTERVAL.getId(),
				FieldbookServiceTest.PROGRAMUUID))
				.thenReturn(StandardVariableTestDataInitializer.createStandardVariable(
						new Term(FieldbookServiceTest.CHECK_INTERVAL_PROPERTY_ID,
								FieldbookServiceTest.ED_CHECK_INTERVAL, FieldbookServiceTest.ED_CHECK_INTERVAL),
						new Term(FieldbookServiceTest.NUMBER_ID, FieldbookServiceTest.NUMBER,
								FieldbookServiceTest.NUMBER),
						new Term(FieldbookServiceTest.FIELD_STUDY_ID, FieldbookServiceTest.FIELD_STUDY,
								FieldbookServiceTest.FIELD_STUDY),
						new Term(TermId.NUMERIC_VARIABLE.getId(), FieldbookServiceTest.NUMERIC_VARIABLE,
								FieldbookServiceTest.NUMERIC_VARIABLE),
						new Term(FieldbookServiceTest.ENV_ID, FieldbookServiceTest.STUDY_ENVIRONMENT_INFORMATION,
								FieldbookServiceTest.STUDY_ENVIRONMENT_INFORMATION),
						new Term(1100, FieldbookServiceTest.STUDY_DESIGN, FieldbookServiceTest.STUDY_DESIGN),
						PhenotypicType.TRIAL_ENVIRONMENT, TermId.CHECK_INTERVAL.getId(),
						FieldbookServiceTest.CHECK_INTERVAL));
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_PLAN.getId(),
				FieldbookServiceTest.PROGRAMUUID))
				.thenReturn(StandardVariableTestDataInitializer.createStandardVariable(
						new Term(FieldbookServiceTest.CHECK_PLAN_PROPERTY_ID, FieldbookServiceTest.ED_CHECK_PLAN,
								FieldbookServiceTest.ED_CHECK_PLAN),
						new Term(FieldbookServiceTest.CODE_ID, FieldbookServiceTest.CODE, FieldbookServiceTest.CODE),
						new Term(FieldbookServiceTest.ASSIGNED_ID, FieldbookServiceTest.ASSIGNED,
								FieldbookServiceTest.ASSIGNED),
						new Term(TermId.CATEGORICAL_VARIABLE.getId(), FieldbookServiceTest.CATEGORICAL_VARIABLE,
								FieldbookServiceTest.CATEGORICAL_VARIABLE),
						new Term(FieldbookServiceTest.ENV_ID, FieldbookServiceTest.STUDY_ENVIRONMENT_INFORMATION,
								FieldbookServiceTest.STUDY_ENVIRONMENT_INFORMATION),
						new Term(FieldbookServiceTest.STUDY_DESIGN_ID, FieldbookServiceTest.STUDY_DESIGN,
								FieldbookServiceTest.STUDY_DESIGN),
						PhenotypicType.TRIAL_ENVIRONMENT, TermId.CHECK_PLAN.getId(), "CHECK_PLAN"));
	}

	@Test
	public void testGetVariablePossibleValuesWhenVariableIsNonLocation() throws Exception {
		final List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl
				.getVariablePossibleValues(this.nonLocationVariable);
		Assert.assertEquals(
				"The results of get all possible values for the non-location should return a total of 5 records", 5,
				resultPossibleValues.size());
	}

	@Test
	public void testGetAllLocations() throws Exception {
		final List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl.getLocations(false);
		Assert.assertEquals("First possible value should have an id of 1 as per our test data", Integer.valueOf(1),
				resultPossibleValues.get(0).getId());
		Assert.assertEquals("Second possible value should have an id of 2 as per our test data", Integer.valueOf(2),
				resultPossibleValues.get(1).getId());
		Assert.assertEquals("There should only be 2 records as per our test data", 2, resultPossibleValues.size());
	}

	@Test
	public void testGetAllPossibleValuesWhenIdIsLocationAndGetAllRecordsIsFalse() throws Exception {
		final Variable variable = VariableTestDataInitializer.createVariable(DataType.LOCATION);
		Mockito.when(this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
				this.locationVariable.getTermId(), true)).thenReturn(variable);

		final List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl
				.getAllPossibleValues(this.locationVariable.getTermId(), false);
		Assert.assertEquals("First possible value should have an id of 1 as per our test data", Integer.valueOf(1),
				resultPossibleValues.get(0).getId());
		Assert.assertEquals("Second possible value should have an id of 2 as per our test data", Integer.valueOf(2),
				resultPossibleValues.get(1).getId());
		Assert.assertEquals("There should only be 2 records as per our test data", 2, resultPossibleValues.size());
	}

	@Test
	public void testGetAllPossibleValuesWhenIdIsLocationAndGetAllRecordsIsTrue() throws Exception {
		final Variable variable = VariableTestDataInitializer.createVariable(DataType.LOCATION);
		Mockito.when(this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
				this.locationVariable.getTermId(), true)).thenReturn(variable);

		final List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl
				.getAllPossibleValues(this.locationVariable.getTermId(), true);
		Assert.assertEquals("There should be no records as per our test data", 0, resultPossibleValues.size());
	}

	@Test
	public void testGetAllPossibleValuesWhenIdIsNonLocation() throws Exception {
		final Variable variable = VariableTestDataInitializer.createVariable(DataType.CATEGORICAL_VARIABLE);
		this.possibleValuesCache.addPossibleValuesByDataType(DataType.CATEGORICAL_VARIABLE,
				this.nonLocationVariable.getPossibleValues());

		Mockito.when(this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
				this.nonLocationVariable.getTermId(), true)).thenReturn(variable);

		final List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl
				.getAllPossibleValues(this.nonLocationVariable.getTermId(), false);
		Assert.assertEquals("There should be 1 record as per our test data", 5, resultPossibleValues.size());
		Assert.assertEquals("First possible value should have an id of 200 as per our test data", Integer.valueOf(0),
				resultPossibleValues.get(0).getId());
	}

	@Test
	public void testManageCheckVariablesWhenCheckGermplasmMainInfoIsNull() {
		// prepare test data
		final UserSelection userSelection = new UserSelection();
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));

		userSelection.setImportedCheckGermplasmMainInfo(null);
		userSelection.setWorkbook(workbook);
		form.setImportedCheckGermplasm(new ArrayList<ImportedGermplasm>());

		try {
			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (final MiddlewareException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
	}

	@Test
	public void testManageCheckVariablesWhenImportedCheckGermplasmIsNull() {
		// prepare test data
		final UserSelection userSelection = new UserSelection();
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));

		userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
		userSelection.setWorkbook(workbook);
		form.setImportedCheckGermplasm(null);

		try {
			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (final MiddlewareException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
	}

	@Test
	public void testManageCheckVariablesForAdd() {
		// prepare test data
		final UserSelection userSelection = new UserSelection();
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));

		userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
		userSelection.setWorkbook(workbook);
		form.setImportedCheckGermplasm(this.createImportedCheckGermplasmData());
		form.setCheckVariables(WorkbookTestUtil.createCheckVariables());

		try {
			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (final MiddlewareException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertTrue("Expected check variables in the conditions but none.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
	}

	private List<ImportedGermplasm> createImportedCheckGermplasmData() {
		final List<ImportedGermplasm> importedGermplasms = new ArrayList<ImportedGermplasm>();
		importedGermplasms.add(new ImportedGermplasm(1, FieldbookServiceTest.DESIG, FieldbookServiceTest.CHECK));
		return importedGermplasms;
	}

	@Test
	public void testManageCheckVariablesForUpdate() {
		// prepare test data
		final UserSelection userSelection = new UserSelection();
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		WorkbookDataUtil.createStudyObservations(1, workbook);
		try {
			userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
			userSelection.setWorkbook(workbook);
			form.setImportedCheckGermplasm(this.createImportedCheckGermplasmData());
			form.setCheckVariables(WorkbookTestUtil.createCheckVariables());

			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (final MiddlewareException e) {
			Assert.fail("Expected mocked class but original method was called.");
		}

		Assert.assertTrue("Expected check variables in the conditions but found none.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));

		Assert.assertTrue("Expected check variable values were updated but weren't.",
				this.areCheckVariableValuesUpdated(userSelection.getWorkbook().getConditions()));
	}

	@Test
	public void testManageCheckVariablesForUpdateWithNoStudyObservations() {
		// prepare test data
		final UserSelection userSelection = new UserSelection();
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		workbook.setTrialObservations(null);
		try {
			this.addCheckVariables(workbook.getConditions());

			userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
			userSelection.setWorkbook(workbook);
			form.setImportedCheckGermplasm(this.createImportedCheckGermplasmData());
			form.setCheckVariables(WorkbookTestUtil.createCheckVariables());

			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (final MiddlewareException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertTrue("Expected check variables in the conditions but found none.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));

		Assert.assertTrue("Expected check variable values were updated but weren't.",
				this.areCheckVariableValuesUpdated(userSelection.getWorkbook().getConditions()));
	}

	private boolean areCheckVariableValuesUpdated(final List<MeasurementVariable> conditions) {
		for (final MeasurementVariable var : conditions) {
			if (var.getTermId() == TermId.CHECK_START.getId() && !"1".equals(var.getValue())
					|| var.getTermId() == TermId.CHECK_INTERVAL.getId() && !"4".equals(var.getValue())
					|| var.getTermId() == TermId.CHECK_PLAN.getId() && !"8414".equals(var.getValue())) {
				return false;
			}
		}
		return true;
	}

	private void addCheckVariables(final List<MeasurementVariable> conditions) throws MiddlewareException {
		conditions.add(this.fieldbookServiceImpl.createMeasurementVariable(String.valueOf(TermId.CHECK_START.getId()),
				"2", Operation.UPDATE, VariableType.ENVIRONMENT_DETAIL.getRole()));
		conditions
				.add(this.fieldbookServiceImpl.createMeasurementVariable(String.valueOf(TermId.CHECK_INTERVAL.getId()),
						"3", Operation.UPDATE, VariableType.ENVIRONMENT_DETAIL.getRole()));
		conditions.add(this.fieldbookServiceImpl.createMeasurementVariable(String.valueOf(TermId.CHECK_PLAN.getId()),
				"8415", Operation.UPDATE, VariableType.ENVIRONMENT_DETAIL.getRole()));
	}

	@Test
	public void testManageCheckVariablesForDelete() {
		// prepare test data
		final UserSelection userSelection = new UserSelection();
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));

		try {
			this.addCheckVariables(workbook.getConditions());

			userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
			userSelection.setWorkbook(workbook);
			form.setImportedCheckGermplasm(new ArrayList<ImportedGermplasm>());
			form.setCheckVariables(WorkbookTestUtil.createCheckVariables());

			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (final MiddlewareException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertTrue("Expected check variables to have delete operation but found Add/Update.",
				this.areOperationsDelete(userSelection.getWorkbook().getConditions()));
	}

	private boolean areOperationsDelete(final List<MeasurementVariable> conditions) {
		for (final MeasurementVariable var : conditions) {
			if (var.getTermId() == TermId.CHECK_START.getId() && !Operation.DELETE.equals(var.getOperation())
					|| var.getTermId() == TermId.CHECK_INTERVAL.getId() && !Operation.DELETE.equals(var.getOperation())
					|| var.getTermId() == TermId.CHECK_PLAN.getId() && !Operation.DELETE.equals(var.getOperation())) {
				return false;
			}
		}
		return true;
	}

	@Test
	public void testManageCheckVariablesForNoOperation() {
		// prepare test data
		final UserSelection userSelection = new UserSelection();
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));

		userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
		userSelection.setWorkbook(workbook);
		form.setImportedCheckGermplasm(new ArrayList<ImportedGermplasm>());

		try {
			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (final MiddlewareException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
	}

	@Test
	public void testCheckingOfCheckVariablesIfConditionsIsNotNullAndNotEmpty() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(workbook.getConditions()));
	}

	@Test
	public void testCheckingOfCheckVariablesIfConditionsIsNotNullButEmpty() {
		final List<MeasurementVariable> conditions = new ArrayList<MeasurementVariable>();

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(conditions));
	}

	@Test
	public void testCheckingOfCheckVariablesIfConditionsIsNullAndEmpty() {
		final List<MeasurementVariable> conditions = null;

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(conditions));
	}

	@Test
	public void testHideExpDesignVariableInManagementSettings() {
		final String expDesignVars = "8135,8131,8132,8133,8134,8136,8137,8138,8139,8142";
		final StringTokenizer tokenizer = new StringTokenizer(expDesignVars, ",");
		boolean allIsHidden = true;
		while (tokenizer.hasMoreTokens()) {
			if (!FieldbookServiceImpl.inHideVariableFields(Integer.parseInt(tokenizer.nextToken()),
					AppConstants.FILTER_STUDY_FIELDS.getString())) {
				allIsHidden = false;
				break;
			}
		}
		Assert.assertTrue("Exp Design Variables should all be captured as hidden", allIsHidden);
	}

	@Test
	public void testSaveStudyImportCrossesIfStudyIdIsNull() throws MiddlewareQueryException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		final List<Integer> crossesIds = new ArrayList<Integer>();
		crossesIds.add(1);
		crossesIds.add(2);
		fieldbookService.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		fieldbookService.saveStudyImportedCrosses(crossesIds, null);
		for (final Integer crossesId : crossesIds) {
			Mockito.verify(fieldbookMiddlewareService, Mockito.times(1)).updateGermlasmListInfoStudy(crossesId, 0);
		}
	}

	@Test
	public void testSaveStudyImportCrossesIfStudyIdIsNotNull() throws MiddlewareQueryException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		final List<Integer> crossesIds = new ArrayList<Integer>();
		crossesIds.add(1);
		crossesIds.add(2);
		final int studyId = 5;

		fieldbookService.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		fieldbookService.saveStudyImportedCrosses(crossesIds, studyId);
		for (final Integer crossesId : crossesIds) {
			Mockito.verify(fieldbookMiddlewareService, Mockito.times(1)).updateGermlasmListInfoStudy(crossesId,
					studyId);
		}
	}

	@Test
	public void testSaveStudyColumnOrderingIfStudyIdIsNull() throws MiddlewareException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService api = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(api);
		final Integer studyId = null;
		final String studyName = "Study Name";
		final String columnOrderDelimited = "";
		fieldbookService.saveStudyColumnOrdering(studyId, studyName, columnOrderDelimited,
				Mockito.mock(Workbook.class));
		Mockito.verify(api, Mockito.times(0)).saveStudyColumnOrdering(Matchers.any(Integer.class),
				Matchers.any(String.class), ArgumentMatchers.<List<Integer>>any());
	}

	@Test
	public void testSaveStudyColumnOrderingIfStudyIdIsNotNullAndColumnOrderListIsEmpty() throws MiddlewareException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService api = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(api);
		final Integer studyId = 7;
		final String studyName = "Study Name";
		final String columnOrderDelimited = "";
		final Workbook workbook = Mockito.mock(Workbook.class);
		fieldbookService.saveStudyColumnOrdering(studyId, studyName, columnOrderDelimited, workbook);
		Mockito.verify(api, Mockito.times(0)).saveStudyColumnOrdering(Matchers.any(Integer.class),
				Matchers.any(String.class), ArgumentMatchers.<List<Integer>>any());
		Mockito.verify(api, Mockito.times(1)).setOrderVariableByRank(workbook);
	}

	@Test
	public void testSaveStudyColumnOrderingIfStudyIdIsNotNullAndColumnOrderListIsNotEmpty() throws MiddlewareException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService api = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(api);
		final Integer studyId = 7;
		final String studyName = "Study Name";
		final String columnOrderDelimited = "[\"1100\", \"1900\"]";
		fieldbookService.saveStudyColumnOrdering(studyId, studyName, columnOrderDelimited,
				Mockito.mock(Workbook.class));
		Mockito.verify(api, Mockito.times(1)).saveStudyColumnOrdering(Matchers.any(Integer.class),
				Matchers.any(String.class), ArgumentMatchers.<List<Integer>>any());
	}

	@Test
	public void testGetPersonByUserId_WhenUserIsNull() throws MiddlewareQueryException {

		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		fieldbookService.setUserDataManager(userDataManager);

		final int userId = 1;

		Mockito.doReturn(null).when(userDataManager).getUserById(userId);

		final String actualValue = fieldbookService.getPersonByUserId(userId);
		Assert.assertEquals("Expecting the returned value \"\" but returned " + actualValue, "", actualValue);
	}

	@Test
	public void testGetPersonByUserId_WhenPersonIsNull() throws MiddlewareQueryException {

		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		fieldbookService.setUserDataManager(userDataManager);

		final int userId = 1;
		final int personId = 1;
		final User user = new User();
		user.setPersonid(personId);

		Mockito.doReturn(user).when(userDataManager).getUserById(userId);
		Mockito.doReturn(null).when(userDataManager).getPersonById(personId);

		final String actualValue = fieldbookService.getPersonByUserId(userId);
		Assert.assertEquals("Expecting the returned value \"\" but returned " + actualValue, "", actualValue);
	}

	@Test
	public void testGetPersonByUserId_WhenPersonIsNotNull() throws MiddlewareQueryException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		fieldbookService.setUserDataManager(userDataManager);

		final int userId = 1;
		final int personId = 1;
		final User user = new User();
		user.setPersonid(personId);
		final Person person = new Person();
		person.setFirstName("FirstName");
		person.setMiddleName("MiddleName");
		person.setLastName("LastName");

		Mockito.doReturn(user).when(userDataManager).getUserById(userId);
		Mockito.doReturn(person).when(userDataManager).getPersonById(personId);

		final String actualValue = fieldbookService.getPersonByUserId(userId);

		final String expected = person.getDisplayName();
		Assert.assertEquals("Expecting to return " + expected + " but returned " + actualValue, expected, actualValue);
	}

	@Test
	public void testGetBreedingMethodByCode() throws MiddlewareQueryException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		final ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		fieldbookService.setContextUtil(contextUtil);
		final String name = "Accession into genebank";
		final String code = "AGB1";
		final String programUUID = null;
		final Method method = this.createMethod(name, code, programUUID);
		Mockito.doReturn(method).when(fieldbookMiddlewareService).getMethodByCode(code, programUUID);
		Mockito.doReturn(programUUID).when(contextUtil).getCurrentProgramUUID();
		final String actualValue = fieldbookService.getBreedingMethodByCode(code);
		final String expected = method.getMname() + " - " + method.getMcode();
		Assert.assertEquals("Expecting to return " + expected + " but returned " + actualValue, expected, actualValue);

	}

	@Test
	public void testGetBreedingMethodByCode_NullMethod() throws MiddlewareQueryException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		fieldbookService.setContextUtil(Mockito.mock(ContextUtil.class));
		final ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		fieldbookService.setContextUtil(contextUtil);
		final String code = "TESTCODE";
		final String programUUID = "6c87aaae-9e0f-428b-a364-44fab9fa7fd1";
		Mockito.doReturn(null).when(fieldbookMiddlewareService).getMethodByCode(code, programUUID);
		Mockito.doReturn(programUUID).when(contextUtil).getCurrentProgramUUID();
		final String actualValue = fieldbookService.getBreedingMethodByCode(code);
		final String expected = "";
		Assert.assertEquals("Expecting to return " + expected + " but returned " + actualValue, expected, actualValue);

	}

	private Method createMethod(final String name, final String code, final String uniqueID) {
		final Method method = new Method();
		method.setMname(name);
		method.setMcode(code);
		method.setUniqueID(uniqueID);
		return method;
	}

	@Test
	public void testGetPersonNameByPersonId() throws MiddlewareQueryException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		fieldbookService.setUserDataManager(userDataManager);

		final int personId = 1;
		final Person person = new Person();
		person.setFirstName("FirstName");
		person.setMiddleName("MiddleName");
		person.setLastName("LastName");

		Mockito.doReturn(person).when(userDataManager).getPersonById(personId);

		final String personName = fieldbookService.getPersonNameByPersonId(personId);
		Assert.assertEquals(person.getDisplayName(), personName);
	}

	@Test
	public void testGetPersonNameByPersonId_PersonNotFound() throws MiddlewareQueryException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		fieldbookService.setUserDataManager(userDataManager);

		final int personId = 100;
		final String expectedName = "";
		Mockito.doReturn(null).when(userDataManager).getPersonById(personId);

		final String personName = fieldbookService.getPersonNameByPersonId(personId);
		Assert.assertEquals(expectedName, personName);
	}

	@Test
	public void testAddMeasurementVariableToList() {

		final MeasurementVariable measurementVariableToAdd = new MeasurementVariable();
		measurementVariableToAdd.setTermId(TermId.OBS_UNIT_ID.getId());
		measurementVariableToAdd.setName(TermId.OBS_UNIT_ID.name());

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		this.fieldbookServiceImpl.addMeasurementVariableToList(measurementVariableToAdd, measurementVariables);

		final MeasurementVariable obsUnitIdMeasurementVariabe = measurementVariables.get(0);

		Assert.assertNotNull(obsUnitIdMeasurementVariabe);
		Assert.assertEquals(TermId.OBS_UNIT_ID.getId(), obsUnitIdMeasurementVariabe.getTermId());
		Assert.assertEquals(TermId.OBS_UNIT_ID.name(), obsUnitIdMeasurementVariabe.getName());

	}

	@Test
	public void testaddSTUDY_UIDVariableToWorkbookConditionsFalse() {
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto("T"), "Sample Study", 1,
				false);
		// Set lists to empty lists for easier testing
		workbook.setConditions(new ArrayList<MeasurementVariable>());
		workbook.setFactors(new ArrayList<MeasurementVariable>());
		workbook.getObservations().get(0).setDataList(new ArrayList<MeasurementData>());

		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(Matchers.eq(TermId.OBS_UNIT_ID.getId()),
				Matchers.anyString()))
				.thenReturn(StandardVariableTestDataInitializer.createStandardVariable(TermId.OBS_UNIT_ID.getId(),
						TermId.OBS_UNIT_ID.name()));

		this.fieldbookServiceImpl.addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(workbook, false);

		final MeasurementVariable obsUnitIdVariable = workbook.getFactors().get(0);
		Assert.assertEquals(TermId.OBS_UNIT_ID.getId(), obsUnitIdVariable.getTermId());
		Assert.assertEquals(TermId.OBS_UNIT_ID.name(), obsUnitIdVariable.getName());

		// Observation Unit id should not be added in the datalist
		Assert.assertTrue(workbook.getObservations().get(0).getDataList().isEmpty());
	}

	@Test
	public void testAddStudyUUIDConditionAndObsUnitIDFactorToWorkbookTrue() {
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto("T"), "Sample Study", 1,
				false);
		// Set lists to empty lists for easier testing
		workbook.setConditions(new ArrayList<MeasurementVariable>());
		workbook.setFactors(new ArrayList<MeasurementVariable>());
		workbook.getObservations().get(0).setDataList(new ArrayList<MeasurementData>());

		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(Matchers.eq(TermId.OBS_UNIT_ID.getId()),
				Matchers.anyString()))
				.thenReturn(StandardVariableTestDataInitializer.createStandardVariable(TermId.OBS_UNIT_ID.getId(),
						TermId.OBS_UNIT_ID.name()));

		this.fieldbookServiceImpl.addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(workbook, true);

		final MeasurementVariable obsUnitIdVariable = workbook.getFactors().get(0);
		Assert.assertEquals(TermId.OBS_UNIT_ID.getId(), obsUnitIdVariable.getTermId());
		Assert.assertEquals(TermId.OBS_UNIT_ID.name(), obsUnitIdVariable.getName());

		// Observation Unit id should be added in the datalist
		Assert.assertFalse(workbook.getObservations().get(0).getDataList().isEmpty());
		final MeasurementData mData = workbook.getObservations().get(0).getDataList().get(0);
		Assert.assertEquals(TermId.OBS_UNIT_ID.name(), mData.getLabel());
		Assert.assertTrue(mData.getValue().isEmpty());
		Assert.assertEquals(obsUnitIdVariable, mData.getMeasurementVariable());
	}

	@Test
	public void testAddMeasurementVariableToMeasurementRows() {

		final MeasurementVariable measurementVariableToAdd = new MeasurementVariable();
		measurementVariableToAdd.setTermId(TermId.OBS_UNIT_ID.getId());
		measurementVariableToAdd.setName(TermId.OBS_UNIT_ID.name());

		final List<MeasurementRow> measurementRows = new ArrayList<>();
		final MeasurementRow measurementRow = new MeasurementRow();
		measurementRow.setDataList(new ArrayList<MeasurementData>());
		measurementRows.add(measurementRow);

		this.fieldbookServiceImpl.addMeasurementVariableToMeasurementRows(measurementVariableToAdd, measurementRows);

		final List<MeasurementData> measurementDataList = measurementRows.get(0).getDataList();
		final MeasurementData obsUnitIdMeasurementData = measurementDataList.get(0);

		Assert.assertNotNull(
				"Expecting that OBS_UNIT_ID measurementData is added in the measurementData list of the measurement",
				obsUnitIdMeasurementData);
		Assert.assertEquals(TermId.OBS_UNIT_ID.getId(), obsUnitIdMeasurementData.getMeasurementVariable().getTermId());
		Assert.assertEquals(TermId.OBS_UNIT_ID.name(), obsUnitIdMeasurementData.getLabel());

	}

	@Test
	public void testIsVariableExistsInList() {

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable obsUnitIdMeasurementVariable = new MeasurementVariable();

		obsUnitIdMeasurementVariable.setName(TermId.OBS_UNIT_ID.name());
		obsUnitIdMeasurementVariable.setTermId(TermId.OBS_UNIT_ID.getId());

		measurementVariables.add(obsUnitIdMeasurementVariable);

		Assert.assertTrue("Expecting that OBS_UNIT_ID variable exists in the list",
				this.fieldbookServiceImpl.isVariableExistsInList(TermId.OBS_UNIT_ID.getId(), measurementVariables));
		Assert.assertFalse("Expecting that ENTRY_NO variable does not exist in the list",
				this.fieldbookServiceImpl.isVariableExistsInList(TermId.ENTRY_NO.getId(), measurementVariables));
	}

	@Test
	public void testResolveNameVarValueWhereIdVariableIsNotLocationId() {
		final MeasurementVariable mvar = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.BREEDING_METHOD.getId(), TermId.BREEDING_METHOD.name(), "4");
		final Variable var = VariableTestDataInitializer.createVariable(DataType.BREEDING_METHOD);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.eq(this.contextUtil.getCurrentProgramUUID()),
				Matchers.anyInt(), Matchers.eq(true))).thenReturn(var);
		Mockito.when(this.fieldbookMiddlewareService.getAllBreedingMethods(Matchers.anyBoolean()))
				.thenReturn(MethodTestDataInitializer.createMethodList(5));
		final String result = this.fieldbookServiceImpl.resolveNameVarValue(mvar);
		Assert.assertEquals("The result's value should be " + FieldbookServiceTest.METHOD_DESCRIPTION,
				FieldbookServiceTest.METHOD_DESCRIPTION, result);
	}

	@Test
	public void testgetDisplayNameWithLABBR() {
		final Location location = LocationTestDataInitializer.createLocationWithLabbr(1,
				FieldbookServiceTest.LOCATION_NAME, FieldbookServiceTest.LABBR);
		final String displayName = FieldbookServiceTest.LOCATION_NAME + " - (" + FieldbookServiceTest.LABBR + ")";
		final String result = this.fieldbookServiceImpl.getDisplayName(location);
		Assert.assertEquals("The result's value should be " + displayName, displayName, result);
	}

	@Test
	public void testgetDisplayNameWithoutLABBR() {
		final Location location = LocationTestDataInitializer.createLocation(1, FieldbookServiceTest.LOCATION_NAME);
		final String result = this.fieldbookServiceImpl.getDisplayName(location);
		Assert.assertEquals("The result's value should be " + FieldbookServiceTest.LOCATION_NAME,
				FieldbookServiceTest.LOCATION_NAME, result);
	}

	@Test
	public void testGetGermplasmListChecksSize() {
		final Variable variable = VariableTestDataInitializer.createVariable(DataType.CATEGORICAL_VARIABLE);
		this.possibleValuesCache.addPossibleValuesByDataType(DataType.CATEGORICAL_VARIABLE,
			this.nonLocationVariable.getPossibleValues());

		Mockito.when(this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
			TermId.ENTRY_TYPE.getId(), true)).thenReturn(variable);

		final List<ValueReference> entryTypes = ValueReferenceTestDataInitializer.createPossibleValues();
		final List<Integer> checkEntryTypeIds = new ArrayList<>();
		for(final ValueReference entryType: entryTypes) {
			checkEntryTypeIds.add(entryType.getId() - 1);
		}
		entryTypes.add(new ValueReference(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId(), SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName(), SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue()));
		this.fieldbookServiceImpl.getGermplasmListChecksSize(1);
		Mockito.verify(this.fieldbookMiddlewareService).countListDataProjectByListIdAndEntryTypeIds(1, checkEntryTypeIds);
	}
}
