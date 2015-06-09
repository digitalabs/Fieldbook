
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.Assert;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.bean.PossibleValuesCache;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;

public class FieldbookServiceTest {

	private static final String DUMMY_PROGRAM_UUID = "1234567890";
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
	private static final String TRIAL_DESIGN = "Trial Design";
	private static final int TRIAL_DESIGN_ID = 1100;
	private static final String TRIAL_ENVIRONMENT_INFORMATION = "Trial Environment Information";
	private static final int TRIAL_ENV_ID = 1020;
	private static final String NUMERIC_VARIABLE = "Numeric variable";
	private static final String FIELD_TRIAL = "Field trial";
	private static final int FIELD_TRIAL_ID = 4100;
	private static final String NUMBER = "Number";
	private static final int NUMBER_ID = 6040;
	private static final String ED_CHECK_START = "ED - Check Start";
	private static final int CHECK_START_PROPERTY_ID = 2153;

	private FieldbookServiceImpl fieldbookServiceImpl;
	private MeasurementVariable locationVariable;
	private MeasurementVariable nonLocationVariable;

	@Before
	public void setUp() throws MiddlewareQueryException {
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService =
				Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		List<Location> allLocation = new ArrayList<Location>();
		allLocation.add(new Location(1));
		allLocation.add(new Location(2));
		Mockito.when(fieldbookMiddlewareService.getAllLocations()).thenReturn(allLocation);
		Mockito.when(fieldbookMiddlewareService.getAllBreedingLocations()).thenReturn(new ArrayList<Location>());

		this.setUpStandardVariablesForChecks(fieldbookMiddlewareService);

		List<Person> personsList = new ArrayList<Person>();
		personsList.add(this.createPerson(200));

		Mockito.when(fieldbookMiddlewareService.getAllPersonsOrderedByLocalCentral()).thenReturn(personsList);

		this.fieldbookServiceImpl = new FieldbookServiceImpl(fieldbookMiddlewareService, new PossibleValuesCache());

		this.fieldbookServiceImpl.setContextUtil(Mockito.mock(ContextUtil.class));

		List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		for (int i = 0; i < 5; i++) {
			possibleValues.add(new ValueReference(i, "Name: " + i));
		}

		this.locationVariable = new MeasurementVariable();
		this.nonLocationVariable = new MeasurementVariable();

		this.locationVariable.setTermId(TermId.LOCATION_ID.getId());
		this.nonLocationVariable.setTermId(TermId.PI_ID.getId());
		this.nonLocationVariable.setPossibleValues(possibleValues);
	}

	private void setUpStandardVariablesForChecks(FieldbookService fieldbookMiddlewareService) throws MiddlewareQueryException {
		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_START.getId())).thenReturn(
				this.createStandardVariable(new Term(FieldbookServiceTest.CHECK_START_PROPERTY_ID, FieldbookServiceTest.ED_CHECK_START,
						FieldbookServiceTest.ED_CHECK_START), new Term(FieldbookServiceTest.NUMBER_ID, FieldbookServiceTest.NUMBER,
						FieldbookServiceTest.NUMBER), new Term(FieldbookServiceTest.FIELD_TRIAL_ID, FieldbookServiceTest.FIELD_TRIAL,
						FieldbookServiceTest.FIELD_TRIAL), new Term(TermId.NUMERIC_VARIABLE.getId(), FieldbookServiceTest.NUMERIC_VARIABLE,
						FieldbookServiceTest.NUMERIC_VARIABLE), new Term(FieldbookServiceTest.TRIAL_ENV_ID,
						FieldbookServiceTest.TRIAL_ENVIRONMENT_INFORMATION, FieldbookServiceTest.TRIAL_ENVIRONMENT_INFORMATION), new Term(
						FieldbookServiceTest.TRIAL_DESIGN_ID, FieldbookServiceTest.TRIAL_DESIGN, FieldbookServiceTest.TRIAL_DESIGN),
						PhenotypicType.TRIAL_ENVIRONMENT, TermId.CHECK_START.getId(), FieldbookServiceTest.CHECK_START));
		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_INTERVAL.getId())).thenReturn(
				this.createStandardVariable(new Term(FieldbookServiceTest.CHECK_INTERVAL_PROPERTY_ID,
						FieldbookServiceTest.ED_CHECK_INTERVAL, FieldbookServiceTest.ED_CHECK_INTERVAL), new Term(
						FieldbookServiceTest.NUMBER_ID, FieldbookServiceTest.NUMBER, FieldbookServiceTest.NUMBER), new Term(
						FieldbookServiceTest.FIELD_TRIAL_ID, FieldbookServiceTest.FIELD_TRIAL, FieldbookServiceTest.FIELD_TRIAL), new Term(
						TermId.NUMERIC_VARIABLE.getId(), FieldbookServiceTest.NUMERIC_VARIABLE, FieldbookServiceTest.NUMERIC_VARIABLE),
						new Term(FieldbookServiceTest.TRIAL_ENV_ID, FieldbookServiceTest.TRIAL_ENVIRONMENT_INFORMATION,
								FieldbookServiceTest.TRIAL_ENVIRONMENT_INFORMATION), new Term(1100, FieldbookServiceTest.TRIAL_DESIGN,
								FieldbookServiceTest.TRIAL_DESIGN), PhenotypicType.TRIAL_ENVIRONMENT, TermId.CHECK_INTERVAL.getId(),
						FieldbookServiceTest.CHECK_INTERVAL));
		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_PLAN.getId())).thenReturn(
				this.createStandardVariable(new Term(FieldbookServiceTest.CHECK_PLAN_PROPERTY_ID, FieldbookServiceTest.ED_CHECK_PLAN,
						FieldbookServiceTest.ED_CHECK_PLAN), new Term(FieldbookServiceTest.CODE_ID, FieldbookServiceTest.CODE,
						FieldbookServiceTest.CODE), new Term(FieldbookServiceTest.ASSIGNED_ID, FieldbookServiceTest.ASSIGNED,
						FieldbookServiceTest.ASSIGNED), new Term(TermId.CATEGORICAL_VARIABLE.getId(),
						FieldbookServiceTest.CATEGORICAL_VARIABLE, FieldbookServiceTest.CATEGORICAL_VARIABLE), new Term(
						FieldbookServiceTest.TRIAL_ENV_ID, FieldbookServiceTest.TRIAL_ENVIRONMENT_INFORMATION,
						FieldbookServiceTest.TRIAL_ENVIRONMENT_INFORMATION), new Term(FieldbookServiceTest.TRIAL_DESIGN_ID,
						FieldbookServiceTest.TRIAL_DESIGN, FieldbookServiceTest.TRIAL_DESIGN), PhenotypicType.TRIAL_ENVIRONMENT,
						TermId.CHECK_PLAN.getId(), "CHECK_PLAN"));
	}

	private StandardVariable createStandardVariable(Term property, Term scale, Term method, Term dataType, Term storedIn, Term isA,
			PhenotypicType phenotypicType, int termId, String name) {
		StandardVariable stdVar = new StandardVariable(property, scale, method, dataType, storedIn, isA, phenotypicType);
		stdVar.setId(termId);
		stdVar.setName(name);

		return stdVar;
	}

	private Person createPerson(int id) {
		Person person = new Person("First Name", "Middle Name", "Last Name");
		person.setId(id);
		return person;
	}

	@Test
	public void testGetVariablePossibleValuesWhenVariableIsLocation() throws Exception {
		List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl.getVariablePossibleValues(this.locationVariable);
		Assert.assertEquals("The results of get all possible values for the location should return a total of 2 records", 2,
				resultPossibleValues.size());
	}

	@Test
	public void testGetVariablePossibleValuesWhenVariableIsNonLocation() throws Exception {
		List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl.getVariablePossibleValues(this.nonLocationVariable);
		Assert.assertEquals("The results of get all possible values for the non-location should return a total of 5 records", 5,
				resultPossibleValues.size());
	}

	@Test
	public void testGetAllLocations() throws Exception {
		List<ValueReference> resultPossibleValues =
				this.fieldbookServiceImpl.getAllLocationsByUniqueID(FieldbookServiceTest.DUMMY_PROGRAM_UUID);
		Assert.assertEquals("First possible value should have an id of 1 as per our test data", Integer.valueOf(1), resultPossibleValues
				.get(0).getId());
		Assert.assertEquals("Second possible value should have an id of 2 as per our test data", Integer.valueOf(2), resultPossibleValues
				.get(1).getId());
		Assert.assertEquals("There should only be 2 records as per our test data", 2, resultPossibleValues.size());
	}

	@Test
	public void testGetAllPossibleValuesWhenIdIsLocationAndGetAllRecordsIsTrue() throws Exception {
		List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl.getAllPossibleValues(this.locationVariable.getTermId(), true);
		Assert.assertEquals("First possible value should have an id of 1 as per our test data", Integer.valueOf(1), resultPossibleValues
				.get(0).getId());
		Assert.assertEquals("Second possible value should have an id of 2 as per our test data", Integer.valueOf(2), resultPossibleValues
				.get(1).getId());
		Assert.assertEquals("There should only be 2 records as per our test data", 2, resultPossibleValues.size());
	}

	@Test
	public void testGetAllPossibleValuesWhenIdIsLocationAndGetAllRecordsIsFalse() throws Exception {
		List<ValueReference> resultPossibleValues =
				this.fieldbookServiceImpl.getAllPossibleValues(this.locationVariable.getTermId(), false);
		Assert.assertEquals("There should be no records as per our test data", 0, resultPossibleValues.size());
	}

	@Test
	public void testGetAllPossibleValuesWhenIdIsNonLocation() throws Exception {
		List<ValueReference> resultPossibleValues =
				this.fieldbookServiceImpl.getAllPossibleValues(this.nonLocationVariable.getTermId(), false);
		Assert.assertEquals("There should be 1 record as per our test data", 1, resultPossibleValues.size());
		Assert.assertEquals("First possible value should have an id of 200 as per our test data", Integer.valueOf(200),
				resultPossibleValues.get(0).getId());
	}

	@Test
	public void testManageCheckVariablesWhenCheckGermplasmMainInfoIsNull() {
		// prepare test data
		UserSelection userSelection = new UserSelection();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);

		userSelection.setImportedCheckGermplasmMainInfo(null);
		userSelection.setWorkbook(workbook);
		form.setImportedCheckGermplasm(new ArrayList<ImportedGermplasm>());

		try {
			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (MiddlewareQueryException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
	}

	@Test
	public void testManageCheckVariablesWhenImportedCheckGermplasmIsNull() {
		// prepare test data
		UserSelection userSelection = new UserSelection();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);

		userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
		userSelection.setWorkbook(workbook);
		form.setImportedCheckGermplasm(null);

		try {
			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (MiddlewareQueryException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
	}

	@Test
	public void testManageCheckVariablesForAdd() {
		// prepare test data
		UserSelection userSelection = new UserSelection();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);

		userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
		userSelection.setWorkbook(workbook);
		form.setImportedCheckGermplasm(this.createImportedCheckGermplasmData());
		form.setCheckVariables(WorkbookTestUtil.createCheckVariables());

		try {
			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (MiddlewareQueryException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertTrue("Expected check variables in the conditions but none.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
	}

	private List<ImportedGermplasm> createImportedCheckGermplasmData() {
		List<ImportedGermplasm> importedGermplasms = new ArrayList<ImportedGermplasm>();
		importedGermplasms.add(new ImportedGermplasm(1, FieldbookServiceTest.DESIG, FieldbookServiceTest.CHECK));
		return importedGermplasms;
	}

	@Test
	public void testManageCheckVariablesForUpdate() {
		// prepare test data
		UserSelection userSelection = new UserSelection();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		WorkbookDataUtil.addCheckConditions();
		WorkbookDataUtil.createTrialObservations(1);
		try {
			this.addCheckVariables(workbook.getConditions());

			userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
			userSelection.setWorkbook(workbook);
			form.setImportedCheckGermplasm(this.createImportedCheckGermplasmData());
			form.setCheckVariables(WorkbookTestUtil.createCheckVariables());

			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (MiddlewareQueryException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertTrue("Expected check variables in the conditions but found none.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));

		Assert.assertTrue("Expected check variable values were updated but weren't.",
				this.areCheckVariableValuesUpdated(userSelection.getWorkbook().getConditions()));
	}

	@Test
	public void testManageCheckVariablesForUpdateWithNoTrialObservations() {
		// prepare test data
		UserSelection userSelection = new UserSelection();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		workbook.setTrialObservations(null);
		try {
			this.addCheckVariables(workbook.getConditions());

			userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
			userSelection.setWorkbook(workbook);
			form.setImportedCheckGermplasm(this.createImportedCheckGermplasmData());
			form.setCheckVariables(WorkbookTestUtil.createCheckVariables());

			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (MiddlewareQueryException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertTrue("Expected check variables in the conditions but found none.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));

		Assert.assertTrue("Expected check variable values were updated but weren't.",
				this.areCheckVariableValuesUpdated(userSelection.getWorkbook().getConditions()));
	}

	private boolean areCheckVariableValuesUpdated(List<MeasurementVariable> conditions) {
		for (MeasurementVariable var : conditions) {
			if (var.getTermId() == TermId.CHECK_START.getId() && !"1".equals(var.getValue())
					|| var.getTermId() == TermId.CHECK_INTERVAL.getId() && !"4".equals(var.getValue())
					|| var.getTermId() == TermId.CHECK_PLAN.getId() && !"8414".equals(var.getValue())) {
				return false;
			}
		}
		return true;
	}

	private void addCheckVariables(List<MeasurementVariable> conditions) throws MiddlewareQueryException {
		conditions.add(this.fieldbookServiceImpl.createMeasurementVariable(String.valueOf(TermId.CHECK_START.getId()), "2",
				Operation.UPDATE));
		conditions.add(this.fieldbookServiceImpl.createMeasurementVariable(String.valueOf(TermId.CHECK_INTERVAL.getId()), "3",
				Operation.UPDATE));
		conditions.add(this.fieldbookServiceImpl.createMeasurementVariable(String.valueOf(TermId.CHECK_PLAN.getId()), "8415",
				Operation.UPDATE));
	}

	@Test
	public void testManageCheckVariablesForDelete() {
		// prepare test data
		UserSelection userSelection = new UserSelection();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);

		try {
			this.addCheckVariables(workbook.getConditions());

			userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
			userSelection.setWorkbook(workbook);
			form.setImportedCheckGermplasm(new ArrayList<ImportedGermplasm>());
			form.setCheckVariables(WorkbookTestUtil.createCheckVariables());

			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (MiddlewareQueryException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertTrue("Expected check variables to have delete operation but found Add/Update.",
				this.areOperationsDelete(userSelection.getWorkbook().getConditions()));
	}

	private boolean areOperationsDelete(List<MeasurementVariable> conditions) {
		for (MeasurementVariable var : conditions) {
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
		UserSelection userSelection = new UserSelection();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);

		userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
		userSelection.setWorkbook(workbook);
		form.setImportedCheckGermplasm(new ArrayList<ImportedGermplasm>());

		try {
			this.fieldbookServiceImpl.manageCheckVariables(userSelection, form);
		} catch (MiddlewareQueryException e) {
			Assert.fail("Epected mocked class but original method was called.");
		}

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
	}

	@Test
	public void testCheckingOfCheckVariablesIfConditionsIsNotNullAndNotEmpty() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(workbook.getConditions()));
	}

	@Test
	public void testCheckingOfCheckVariablesIfConditionsIsNotNullButEmpty() {
		List<MeasurementVariable> conditions = new ArrayList<MeasurementVariable>();

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(conditions));
	}

	@Test
	public void testCheckingOfCheckVariablesIfConditionsIsNullAndEmpty() {
		List<MeasurementVariable> conditions = null;

		Assert.assertFalse("Expected no check variables in the conditions but found one.",
				this.fieldbookServiceImpl.hasCheckVariables(conditions));
	}

	@Test
	public void testHideExpDesignVariableInManagementSettings() {
		String expDesignVars = "8135,8131,8132,8133,8134,8136,8137,8138,8139,8142";
		StringTokenizer tokenizer = new StringTokenizer(expDesignVars, ",");
		boolean allIsHidden = true;
		while (tokenizer.hasMoreTokens()) {
			if (!FieldbookServiceImpl.inHideVariableFields(Integer.parseInt(tokenizer.nextToken()),
					AppConstants.FILTER_NURSERY_FIELDS.getString())) {
				allIsHidden = false;
				break;
			}
		}
		Assert.assertTrue("Exp Design Variables should all be captured as hidden", allIsHidden);
	}

	@Test
	public void testSaveStudyImportCrossesIfStudyIdIsNull() throws MiddlewareQueryException {
		FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		List<Integer> crossesIds = new ArrayList<Integer>();
		crossesIds.add(1);
		crossesIds.add(2);
		fieldbookService.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		fieldbookService.saveStudyImportedCrosses(crossesIds, null);
		for (Integer crossesId : crossesIds) {
			Mockito.verify(fieldbookMiddlewareService, Mockito.times(1)).updateGermlasmListInfoStudy(crossesId, 0);
		}
	}

	@Test
	public void testSaveStudyImportCrossesIfStudyIdIsNotNull() throws MiddlewareQueryException {
		FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		List<Integer> crossesIds = new ArrayList<Integer>();
		crossesIds.add(1);
		crossesIds.add(2);
		Integer studyId = 5;

		fieldbookService.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		fieldbookService.saveStudyImportedCrosses(crossesIds, studyId);
		for (Integer crossesId : crossesIds) {
			Mockito.verify(fieldbookMiddlewareService, Mockito.times(1)).updateGermlasmListInfoStudy(crossesId, studyId);
		}
	}

	@Test
	public void testSaveStudyColumnOrderingIfStudyIdIsNull() throws MiddlewareQueryException {
		FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		FieldbookService api = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(api);
		Integer studyId = null;
		String studyName = "Study Name";
		String columnOrderDelimited = "";
		fieldbookService.saveStudyColumnOrdering(studyId, studyName, columnOrderDelimited, Mockito.mock(Workbook.class));
		Mockito.verify(api, Mockito.times(0)).saveStudyColumnOrdering(Matchers.any(Integer.class), Matchers.any(String.class),
				Matchers.anyList());
	}

	@Test
	public void testSaveStudyColumnOrderingIfStudyIdIsNotNullAndColumnOrderListIsEmpty() throws MiddlewareQueryException {
		FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		FieldbookService api = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(api);
		Integer studyId = 7;
		String studyName = "Study Name";
		String columnOrderDelimited = "";
		Workbook workbook = Mockito.mock(Workbook.class);
		fieldbookService.saveStudyColumnOrdering(studyId, studyName, columnOrderDelimited, workbook);
		Mockito.verify(api, Mockito.times(0)).saveStudyColumnOrdering(Matchers.any(Integer.class), Matchers.any(String.class),
				Matchers.anyList());
		Mockito.verify(api, Mockito.times(1)).setOrderVariableByRank(workbook);
	}

	@Test
	public void testSaveStudyColumnOrderingIfStudyIdIsNotNullAndColumnOrderListIsNotEmpty() throws MiddlewareQueryException {
		FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		FieldbookService api = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(api);
		Integer studyId = 7;
		String studyName = "Study Name";
		String columnOrderDelimited = "[\"1100\", \"1900\"]";
		fieldbookService.saveStudyColumnOrdering(studyId, studyName, columnOrderDelimited, Mockito.mock(Workbook.class));
		Mockito.verify(api, Mockito.times(1)).saveStudyColumnOrdering(Matchers.any(Integer.class), Matchers.any(String.class),
				Matchers.anyList());
	}

	@Test
	public void testGetPersonByUserId_WhenUserIsNull() throws MiddlewareQueryException {

		FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		fieldbookService.setUserDataManager(userDataManager);

		Integer userId = 1;

		Mockito.doReturn(null).when(userDataManager).getUserById(userId);

		String actualValue = fieldbookService.getPersonByUserId(userId);
		Assert.assertEquals("Expecting the returned value \"\" but returned " + actualValue, "", actualValue);
	}

	@Test
	public void testGetPersonByUserId_WhenPersonIsNull() throws MiddlewareQueryException {

		FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		fieldbookService.setUserDataManager(userDataManager);

		Integer userId = 1;
		int personId = 1;
		User user = new User();
		user.setPersonid(personId);

		Mockito.doReturn(user).when(userDataManager).getUserById(userId);
		Mockito.doReturn(null).when(userDataManager).getPersonById(personId);

		String actualValue = fieldbookService.getPersonByUserId(userId);
		Assert.assertEquals("Expecting the returned value \"\" but returned " + actualValue, "", actualValue);
	}

	@Test
	public void testGetPersonByUserId_WhenPersonIsNotNull() throws MiddlewareQueryException {
		FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		fieldbookService.setUserDataManager(userDataManager);

		Integer userId = 1;
		int personId = 1;
		User user = new User();
		user.setPersonid(personId);
		Person person = new Person();
		person.setFirstName("FirstName");
		person.setMiddleName("MiddleName");
		person.setLastName("LastName");

		Mockito.doReturn(user).when(userDataManager).getUserById(userId);
		Mockito.doReturn(person).when(userDataManager).getPersonById(personId);

		String actualValue = fieldbookService.getPersonByUserId(userId);

		String expected = person.getDisplayName();
		Assert.assertEquals("Expecting to return " + expected + " but returned " + actualValue, expected, actualValue);
	}
	
	@Test
	public void testGetBreedingMethodByCode() throws MiddlewareQueryException {
		FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		fieldbookService.setContextUtil(contextUtil);
		String name = "Accession into genebank";
		String code = "AGB1";
		String programUUID = null;
		Method method = createMethod(name,code,programUUID);
		Mockito.doReturn(method).when(fieldbookMiddlewareService).
			getMethodByCode(code,programUUID);
		Mockito.doReturn(programUUID).when(contextUtil).getCurrentProgramUUID();
		String actualValue = fieldbookService.getBreedingMethodByCode(code);
		String expected = method.getMname() + " - " + method.getMcode();
		Assert.assertEquals("Expecting to return " + expected + " but returned " + actualValue, expected, actualValue);
		
	}
	
	@Test
	public void testGetBreedingMethodByCode_NullMethod() throws MiddlewareQueryException {
		FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		fieldbookService.setContextUtil(Mockito.mock(ContextUtil.class));
		ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		fieldbookService.setContextUtil(contextUtil);
		String code = "TESTCODE";
		String programUUID = "6c87aaae-9e0f-428b-a364-44fab9fa7fd1";
		Mockito.doReturn(null).when(fieldbookMiddlewareService).
			getMethodByCode(code,programUUID);
		Mockito.doReturn(programUUID).when(contextUtil).getCurrentProgramUUID();
		String actualValue = fieldbookService.getBreedingMethodByCode(code);
		String expected = "";
		Assert.assertEquals("Expecting to return " + expected + " but returned " + actualValue, expected, actualValue);
		
	}

	private Method createMethod(String name,String code,String uniqueID) {
		Method method = new Method();
		method.setMname(name);
		method.setMcode(code);
		method.setUniqueID(uniqueID);
		return method;
	}
}
