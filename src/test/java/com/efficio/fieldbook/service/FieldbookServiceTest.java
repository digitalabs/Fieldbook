package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.Assert;

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
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.PossibleValuesCache;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;

public class FieldbookServiceTest {
       
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
    public void setUp() throws MiddlewareQueryException{
    	org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
    	List<Location> allLocation = new ArrayList<Location>();
    	allLocation.add(new Location(1));
    	allLocation.add(new Location(2));
    	Mockito.when(fieldbookMiddlewareService.getAllLocations()).thenReturn(allLocation);
    	Mockito.when(fieldbookMiddlewareService.getAllBreedingLocations()).thenReturn(new ArrayList<Location>());
    	
    	setUpStandardVariablesForChecks(fieldbookMiddlewareService);
    	
    	List<Person> personsList = new ArrayList<Person>();
    	personsList.add(createPerson(200));
    	
    	Mockito.when(fieldbookMiddlewareService.getAllPersonsOrderedByLocalCentral()).thenReturn(personsList);
    	
    	fieldbookServiceImpl = new FieldbookServiceImpl(fieldbookMiddlewareService, new PossibleValuesCache());
    	
    	List<ValueReference> possibleValues = new ArrayList<ValueReference>();
    	for(int i = 0 ; i < 5 ; i++){
    		possibleValues.add(new ValueReference(i, "Name: "+i));
    	}
    	
    	locationVariable = new MeasurementVariable();
    	nonLocationVariable = new MeasurementVariable();
    	
    	locationVariable.setTermId(TermId.LOCATION_ID.getId());    	
    	nonLocationVariable.setTermId(TermId.PI_ID.getId());
    	nonLocationVariable.setPossibleValues(possibleValues);    	
    }
    
    private void setUpStandardVariablesForChecks(FieldbookService fieldbookMiddlewareService) throws MiddlewareQueryException{
    	Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_START.getId()))
			.thenReturn(createStandardVariable(new Term(CHECK_START_PROPERTY_ID, ED_CHECK_START, ED_CHECK_START), 
				new Term(NUMBER_ID, NUMBER, NUMBER), new Term(FIELD_TRIAL_ID, FIELD_TRIAL, FIELD_TRIAL), 
				new Term(TermId.NUMERIC_VARIABLE.getId(), NUMERIC_VARIABLE, NUMERIC_VARIABLE), 
				new Term(TRIAL_ENV_ID, TRIAL_ENVIRONMENT_INFORMATION, TRIAL_ENVIRONMENT_INFORMATION), 
				new Term(TRIAL_DESIGN_ID, TRIAL_DESIGN, TRIAL_DESIGN), PhenotypicType.TRIAL_ENVIRONMENT,
				TermId.CHECK_START.getId(), CHECK_START));
		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_INTERVAL.getId()))
			.thenReturn(createStandardVariable(new Term(CHECK_INTERVAL_PROPERTY_ID, ED_CHECK_INTERVAL, ED_CHECK_INTERVAL), 
					new Term(NUMBER_ID, NUMBER, NUMBER), new Term(FIELD_TRIAL_ID, FIELD_TRIAL, FIELD_TRIAL), 
					new Term(TermId.NUMERIC_VARIABLE.getId(), NUMERIC_VARIABLE, NUMERIC_VARIABLE), 
					new Term(TRIAL_ENV_ID, TRIAL_ENVIRONMENT_INFORMATION, TRIAL_ENVIRONMENT_INFORMATION), 
					new Term(1100, TRIAL_DESIGN, TRIAL_DESIGN), PhenotypicType.TRIAL_ENVIRONMENT,
					TermId.CHECK_INTERVAL.getId(), CHECK_INTERVAL));
		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.CHECK_PLAN.getId()))
			.thenReturn(createStandardVariable(new Term(CHECK_PLAN_PROPERTY_ID, ED_CHECK_PLAN, ED_CHECK_PLAN), 
					new Term(CODE_ID, CODE, CODE), new Term(ASSIGNED_ID, ASSIGNED, ASSIGNED), 
					new Term(TermId.CATEGORICAL_VARIABLE.getId(), CATEGORICAL_VARIABLE, CATEGORICAL_VARIABLE), 
					new Term(TRIAL_ENV_ID, TRIAL_ENVIRONMENT_INFORMATION, TRIAL_ENVIRONMENT_INFORMATION), 
					new Term(TRIAL_DESIGN_ID, TRIAL_DESIGN, TRIAL_DESIGN), PhenotypicType.TRIAL_ENVIRONMENT,
					TermId.CHECK_PLAN.getId(), "CHECK_PLAN"));
	}

	private StandardVariable createStandardVariable(Term property, Term scale, Term method, Term dataType,
			Term storedIn, Term isA, PhenotypicType phenotypicType, int termId, String name) {
    	StandardVariable stdVar = new StandardVariable(property, scale, method, dataType, storedIn, isA, phenotypicType);
    	stdVar.setId(termId);
    	stdVar.setName(name);
    	
		return stdVar;
	}

	private Person createPerson(int id){
    	Person person = new Person("First Name", "Middle Name", "Last Name");
    	person.setId(id);
    	return person;
    }
    
    @Test
    public void testGetVariablePossibleValuesWhenVariableIsLocation() throws Exception {    	
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getVariablePossibleValues(locationVariable);
    	Assert.assertEquals("The results of get all possible values for the location should return a total of 2 records", 2, resultPossibleValues.size());
    }
    @Test
    public void testGetVariablePossibleValuesWhenVariableIsNonLocation() throws Exception {    	
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getVariablePossibleValues(nonLocationVariable);
    	Assert.assertEquals("The results of get all possible values for the non-location should return a total of 5 records", 5, resultPossibleValues.size());
    }    
    @Test
    public void testGetAllLocations() throws Exception {
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getAllLocations();
    	Assert.assertEquals("First possible value should have an id of 1 as per our test data", Integer.valueOf(1), resultPossibleValues.get(0).getId());
    	Assert.assertEquals("Second possible value should have an id of 2 as per our test data", Integer.valueOf(2), resultPossibleValues.get(1).getId());
    	Assert.assertEquals("There should only be 2 records as per our test data", 2, resultPossibleValues.size());
    }
    @Test
    public void testGetAllPossibleValuesWhenIdIsLocationAndGetAllRecordsIsTrue() throws Exception {
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getAllPossibleValues(locationVariable.getTermId(), true);
    	Assert.assertEquals("First possible value should have an id of 1 as per our test data", Integer.valueOf(1), resultPossibleValues.get(0).getId());
    	Assert.assertEquals("Second possible value should have an id of 2 as per our test data", Integer.valueOf(2), resultPossibleValues.get(1).getId());
    	Assert.assertEquals("There should only be 2 records as per our test data", 2, resultPossibleValues.size());
    }      
    @Test
    public void testGetAllPossibleValuesWhenIdIsLocationAndGetAllRecordsIsFalse() throws Exception {
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getAllPossibleValues(locationVariable.getTermId(), false);
    	Assert.assertEquals("There should be no records as per our test data", 0, resultPossibleValues.size());
    }     
    @Test
    public void testGetAllPossibleValuesWhenIdIsNonLocation() throws Exception {
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getAllPossibleValues(nonLocationVariable.getTermId(), false);
    	Assert.assertEquals("There should be 1 record as per our test data", 1, resultPossibleValues.size());
    	Assert.assertEquals("First possible value should have an id of 200 as per our test data", Integer.valueOf(200), resultPossibleValues.get(0).getId());
    }      
    
    @Test
    public void testManageCheckVariablesWhenCheckGermplasmMainInfoIsNull() {
    	//prepare test data
    	UserSelection userSelection = new UserSelection();
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	WorkbookDataUtil.setTestWorkbook(null);
    	Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
    	
    	userSelection.setImportedCheckGermplasmMainInfo(null);
    	userSelection.setWorkbook(workbook);
    	form.setImportedCheckGermplasm(new ArrayList<ImportedGermplasm>());
    	
    	try {
    		fieldbookServiceImpl.manageCheckVariables(userSelection, form);
    	} catch(MiddlewareQueryException e) {
    		Assert.fail("Epected mocked class but original method was called.");
    	}
    	
    	Assert.assertFalse("Expected no check variables in the conditions but found one.", 
    			fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
    }
    
	@Test
    public void testManageCheckVariablesWhenImportedCheckGermplasmIsNull() {
		//prepare test data
    	UserSelection userSelection = new UserSelection();
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	WorkbookDataUtil.setTestWorkbook(null);
    	Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
    	
    	userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
    	userSelection.setWorkbook(workbook);
    	form.setImportedCheckGermplasm(null);
    	
    	try {
    		fieldbookServiceImpl.manageCheckVariables(userSelection, form);
    	} catch(MiddlewareQueryException e) {
    		Assert.fail("Epected mocked class but original method was called.");
    	}
    	
    	Assert.assertFalse("Expected no check variables in the conditions but found one.", 
    			fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
    }
    
    @Test
    public void testManageCheckVariablesForAdd() {
    	//prepare test data
    	UserSelection userSelection = new UserSelection();
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	WorkbookDataUtil.setTestWorkbook(null);
    	Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
    	
    	userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
    	userSelection.setWorkbook(workbook);
    	form.setImportedCheckGermplasm(createImportedCheckGermplasmData());
    	form.setCheckVariables(WorkbookTestUtil.createCheckVariables());
    	
    	try {
    		fieldbookServiceImpl.manageCheckVariables(userSelection, form);
    	} catch(MiddlewareQueryException e) {
    		Assert.fail("Epected mocked class but original method was called.");
    	}
    	
    	Assert.assertTrue("Expected check variables in the conditions but none.", 
    			fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
    }
    
    
	private List<ImportedGermplasm> createImportedCheckGermplasmData() {
		List<ImportedGermplasm> importedGermplasms = new ArrayList<ImportedGermplasm>();
		importedGermplasms.add(new ImportedGermplasm(1, DESIG, CHECK));
		return importedGermplasms;
	}

	@Test
    public void testManageCheckVariablesForUpdate() {
		//prepare test data
    	UserSelection userSelection = new UserSelection();
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	WorkbookDataUtil.setTestWorkbook(null);
    	Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
    	WorkbookDataUtil.addCheckConditions();
    	WorkbookDataUtil.createTrialObservations(1);
    	try {	
        	addCheckVariables(workbook.getConditions());
        	
        	userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
        	userSelection.setWorkbook(workbook);
        	form.setImportedCheckGermplasm(createImportedCheckGermplasmData());
        	form.setCheckVariables(WorkbookTestUtil.createCheckVariables());
        	
    		fieldbookServiceImpl.manageCheckVariables(userSelection, form);
    	} catch(MiddlewareQueryException e) {
    		Assert.fail("Epected mocked class but original method was called.");
    	}
    	
    	Assert.assertTrue("Expected check variables in the conditions but found none.", 
    			fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
    	
    	Assert.assertTrue("Expected check variable values were updated but weren't.", 
    			areCheckVariableValuesUpdated(userSelection.getWorkbook().getConditions()));
    }
	
	@Test
    public void testManageCheckVariablesForUpdateWithNoTrialObservations() {
		//prepare test data
    	UserSelection userSelection = new UserSelection();
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	WorkbookDataUtil.setTestWorkbook(null);
    	Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
    	workbook.setTrialObservations(null);
    	try {	
        	addCheckVariables(workbook.getConditions());
        	
        	userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
        	userSelection.setWorkbook(workbook);
        	form.setImportedCheckGermplasm(createImportedCheckGermplasmData());
        	form.setCheckVariables(WorkbookTestUtil.createCheckVariables());
        	
    		fieldbookServiceImpl.manageCheckVariables(userSelection, form);
    	} catch(MiddlewareQueryException e) {
    		Assert.fail("Epected mocked class but original method was called.");
    	}
    	
    	Assert.assertTrue("Expected check variables in the conditions but found none.", 
    			fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
    	
    	Assert.assertTrue("Expected check variable values were updated but weren't.", 
    			areCheckVariableValuesUpdated(userSelection.getWorkbook().getConditions()));
    }
    
    private boolean areCheckVariableValuesUpdated(List<MeasurementVariable> conditions) {
		for (MeasurementVariable var : conditions) {
			if ((var.getTermId() == TermId.CHECK_START.getId() && !"1".equals(var.getValue())) ||
				(var.getTermId() == TermId.CHECK_INTERVAL.getId() && !"4".equals(var.getValue())) ||
				(var.getTermId() == TermId.CHECK_PLAN.getId() && !"8414".equals(var.getValue()))) {
				return false;
			}
		}
		return true;
	}

	private void addCheckVariables(List<MeasurementVariable> conditions) throws MiddlewareQueryException {
		conditions.add(fieldbookServiceImpl.createMeasurementVariable(String.valueOf(TermId.CHECK_START.getId()), "2", Operation.UPDATE));
		conditions.add(fieldbookServiceImpl.createMeasurementVariable(String.valueOf(TermId.CHECK_INTERVAL.getId()), "3", Operation.UPDATE));
		conditions.add(fieldbookServiceImpl.createMeasurementVariable(String.valueOf(TermId.CHECK_PLAN.getId()), "8415", Operation.UPDATE));
	}

	@Test
    public void testManageCheckVariablesForDelete() {
    	//prepare test data
    	UserSelection userSelection = new UserSelection();
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	WorkbookDataUtil.setTestWorkbook(null);
    	Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
    	
    	try {
        	addCheckVariables(workbook.getConditions());
        	
        	userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
        	userSelection.setWorkbook(workbook);
        	form.setImportedCheckGermplasm(new ArrayList<ImportedGermplasm>());
        	form.setCheckVariables(WorkbookTestUtil.createCheckVariables());
        	
    		fieldbookServiceImpl.manageCheckVariables(userSelection, form);
    	} catch(MiddlewareQueryException e) {
    		Assert.fail("Epected mocked class but original method was called.");
    	}    	
    	
    	Assert.assertTrue("Expected check variables to have delete operation but found Add/Update.", 
    			areOperationsDelete(userSelection.getWorkbook().getConditions()));
    }
    
    private boolean areOperationsDelete(List<MeasurementVariable> conditions) {
    	for (MeasurementVariable var : conditions) {
			if ((var.getTermId() == TermId.CHECK_START.getId() && !Operation.DELETE.equals(var.getOperation())) ||
				(var.getTermId() == TermId.CHECK_INTERVAL.getId() && !Operation.DELETE.equals(var.getOperation())) ||
				(var.getTermId() == TermId.CHECK_PLAN.getId() && !Operation.DELETE.equals(var.getOperation()))) {
				return false;
			}
		}
		return true;
	}

	@Test
    public void testManageCheckVariablesForNoOperation() {
    	//prepare test data
    	UserSelection userSelection = new UserSelection();
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	WorkbookDataUtil.setTestWorkbook(null);
    	Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
    	
    	userSelection.setImportedCheckGermplasmMainInfo(new ImportedGermplasmMainInfo());
    	userSelection.setWorkbook(workbook);
    	form.setImportedCheckGermplasm(new ArrayList<ImportedGermplasm>());
    	
    	try {
    		fieldbookServiceImpl.manageCheckVariables(userSelection, form);
    	} catch(MiddlewareQueryException e) {
    		Assert.fail("Epected mocked class but original method was called.");
    	}
    	
    	Assert.assertFalse("Expected no check variables in the conditions but found one.", 
    			fieldbookServiceImpl.hasCheckVariables(userSelection.getWorkbook().getConditions()));
    }
	
	@Test
    public void testCheckingOfCheckVariablesIfConditionsIsNotNullAndNotEmpty() {
		WorkbookDataUtil.setTestWorkbook(null);
    	Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
    	
    	Assert.assertFalse("Expected no check variables in the conditions but found one.", 
    			fieldbookServiceImpl.hasCheckVariables(workbook.getConditions()));
    }
	
	@Test
    public void testCheckingOfCheckVariablesIfConditionsIsNotNullButEmpty() {
		List<MeasurementVariable> conditions = new ArrayList<MeasurementVariable>();
    	
    	Assert.assertFalse("Expected no check variables in the conditions but found one.", 
    			fieldbookServiceImpl.hasCheckVariables(conditions));
    }
	
	@Test
    public void testCheckingOfCheckVariablesIfConditionsIsNullAndEmpty() {
		List<MeasurementVariable> conditions = null;
    	
    	Assert.assertFalse("Expected no check variables in the conditions but found one.", 
    			fieldbookServiceImpl.hasCheckVariables(conditions));
    }
	
	@Test
	public void testHideExpDesignVariableInManagementSettings(){
		String expDesignVars = "8135,8131,8132,8133,8134,8136,8137,8138,8139,8142";
		StringTokenizer tokenizer = new StringTokenizer(expDesignVars, ",");
		boolean allIsHidden = true;
		while(tokenizer.hasMoreTokens()){			
			if (!FieldbookServiceImpl.inHideVariableFields(Integer.parseInt(tokenizer.nextToken()), AppConstants.FILTER_NURSERY_FIELDS.getString())) {
				allIsHidden = false;
				break;
	        }
		}
		Assert.assertTrue("Exp Design Variables should all be captured as hidden", allIsHidden);
	}
}
