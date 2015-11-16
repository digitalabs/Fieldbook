
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.generationcp.middleware.util.Debug;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SettingsUtilTest {

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private UserSelection userSelection;

	private static final String PROGRAM_UUID = "123456789";

	// Test data
	private static final Integer variableId = 8040;
	private static final String variableName = "PM_KEY";
	private static final String variableDescription = "Project management key ";
	private static final Term property = new Term(2002, "User", "Database user");
	private static final Term scale = new Term(6000, "DBCV", "Controlled vocabulary from a database");
	private static final Term method = new Term(4030, "Assigned", "Term, name or id assigned");
	private static final Term dataType = new Term(DataType.NUMERIC_VARIABLE.getId(), DataType.NUMERIC_VARIABLE.getName(), "Numeric Variable Description");
	private static final String cropOntologyId = "CO:1010";
	private static final Double minValue = 100.00;
	private static final Double maxValue = 200.00;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Ignore(value ="BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
	@Test
	public void testConvertXmlDatasetToWorkbookAndBack() {
		Dataset dataset = new Dataset();

		dataset.setConditions(new ArrayList<Condition>());
		dataset.setFactors(new ArrayList<Factor>());
		dataset.setVariates(new ArrayList<Variate>());

		dataset.getConditions().add(
				new Condition("CONDITION1", "CONDITION1", "PERSON", "DBCV", "ASSIGNED", PhenotypicType.STUDY.toString(), "C", "Meeh", null,
						null, null));
		dataset.getFactors().add(
				new Factor("FACTOR1", "FACTOR1", "GERMPLASM ENTRY", "NUMBER", "ENUMERATED", PhenotypicType.GERMPLASM.toString(), "N", 0));
		dataset.getVariates().add(
				new Variate("VARIATE1", "VARIATE1", "YIELD (GRAIN)", "Kg/ha", "Paddy Rice", PhenotypicType.VARIATE.toString(), "N",
						TermId.NUMERIC_VARIABLE.getId(), new ArrayList<ValueReference>(), 0.0, 0.0));

		Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true, PROGRAM_UUID);
		Debug.println(0, workbook);

		Dataset newDataset = (Dataset) SettingsUtil.convertWorkbookToXmlDataset(workbook);
		Assert.assertEquals(dataset.getConditions().get(0).getName(), newDataset.getConditions().get(0).getName());
		Assert.assertEquals(dataset.getConditions().get(0).getDescription(), newDataset.getConditions().get(0).getDescription());
		Assert.assertEquals(dataset.getConditions().get(0).getProperty(), newDataset.getConditions().get(0).getProperty());
		Assert.assertEquals(dataset.getConditions().get(0).getScale(), newDataset.getConditions().get(0).getScale());
		Assert.assertEquals(dataset.getConditions().get(0).getMethod(), newDataset.getConditions().get(0).getMethod());
		Assert.assertEquals(dataset.getConditions().get(0).getRole(), newDataset.getConditions().get(0).getRole());
		Assert.assertEquals(dataset.getConditions().get(0).getDatatype(), newDataset.getConditions().get(0).getDatatype());

		Assert.assertEquals(dataset.getFactors().get(0).getName(), newDataset.getFactors().get(0).getName());
		Assert.assertEquals(dataset.getFactors().get(0).getDescription(), newDataset.getFactors().get(0).getDescription());
		Assert.assertEquals(dataset.getFactors().get(0).getProperty(), newDataset.getFactors().get(0).getProperty());
		Assert.assertEquals(dataset.getFactors().get(0).getScale(), newDataset.getFactors().get(0).getScale());
		Assert.assertEquals(dataset.getFactors().get(0).getMethod(), newDataset.getFactors().get(0).getMethod());
		Assert.assertEquals(dataset.getFactors().get(0).getRole(), newDataset.getFactors().get(0).getRole());
		Assert.assertEquals(dataset.getFactors().get(0).getDatatype(), newDataset.getFactors().get(0).getDatatype());

		Assert.assertEquals(dataset.getVariates().get(0).getName(), newDataset.getVariates().get(0).getName());
		Assert.assertEquals(dataset.getVariates().get(0).getDescription(), newDataset.getVariates().get(0).getDescription());
		Assert.assertEquals(dataset.getVariates().get(0).getProperty(), newDataset.getVariates().get(0).getProperty());
		Assert.assertEquals(dataset.getVariates().get(0).getScale(), newDataset.getVariates().get(0).getScale());
		Assert.assertEquals(dataset.getVariates().get(0).getMethod(), newDataset.getVariates().get(0).getMethod());
		Assert.assertEquals(dataset.getVariates().get(0).getRole(), newDataset.getVariates().get(0).getRole());
		Assert.assertEquals(dataset.getVariates().get(0).getDatatype(), newDataset.getVariates().get(0).getDatatype());

	}

	@Test
	public void testIfCheckVariablesAreInFixedNurseryList() {
		Assert.assertTrue(SettingsUtil.inFixedNurseryList(TermId.CHECK_START.getId()));
		Assert.assertTrue(SettingsUtil.inFixedNurseryList(TermId.CHECK_INTERVAL.getId()));
		Assert.assertTrue(SettingsUtil.inFixedNurseryList(TermId.CHECK_PLAN.getId()));
	}

	@Test
	public void testGetCodeValueValid() {
		List<SettingDetail> removedConditions = this.createCheckVariables(true);
		int code = SettingsUtil.getCodeValue("8414", removedConditions, TermId.CHECK_PLAN.getId());
		Assert.assertEquals("Expected 1 but got " + code + " instead.", 1, code);
	}

	@Test
	public void testGetCodeValueWhenConditionsIsNull() {
		List<SettingDetail> removedConditions = null;
		int code = SettingsUtil.getCodeValue("8414", removedConditions, TermId.CHECK_PLAN.getId());
		Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
	}

	@Test
	public void testGetCodeValueWhenPossibleValuesIsNull() {
		List<SettingDetail> removedConditions = this.createCheckVariables(true);
		int code = SettingsUtil.getCodeValue("8411", removedConditions, TermId.CHECK_START.getId());
		Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
	}

	@Test
	public void testGetCodeValueWhenPossibleValuesIsNotNullButEmpty() {
		List<SettingDetail> removedConditions = this.createCheckVariables(true);
		int code = SettingsUtil.getCodeValue("8412", removedConditions, TermId.CHECK_INTERVAL.getId());
		Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
	}

	@Test
	public void testGetVariableAppConstantLabels() throws Exception {
		List<String> labels = new ArrayList<>( Arrays.asList(new String[] {"abc", "def"}) );

		Properties appConfigProp = Mockito.mock(Properties.class);
		Mockito.when(appConfigProp.getProperty(Mockito.any(String.class))).thenReturn("any value");
		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

		SettingsUtil.getVariableAppConstantLabels(labels,appConfigProp);

		Mockito.verify(appConfigProp, Mockito.times(labels.size())).getProperty(argument.capture());
		Assert.assertTrue(argument.getValue().contains("LABEL"));
	}



	@Test
	public void testGetCodeValueInvalid() {
		List<SettingDetail> removedConditions = this.createCheckVariables(true);
		int code = SettingsUtil.getCodeValue("8413", removedConditions, TermId.CHECK_PLAN.getId());
		Assert.assertNotSame("Expected 1 but got " + code + " instead.", 1, code);
	}

	@Test
	public void testIfCheckVariablesHaveValues() {
		List<SettingDetail> checkVariables = this.createCheckVariables(true);
		boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertTrue(checksHaveValues);
	}

	@Test
	public void testIfCheckVariablesHaveNoValues() {
		List<SettingDetail> checkVariables = this.createCheckVariables(false);
		boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertFalse(checksHaveValues);
	}

	@Test
	public void testIfCheckVariablesIsNull() {
		List<SettingDetail> checkVariables = null;
		boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertFalse(checksHaveValues);
	}

	@Test
	public void testIfCheckVariablesIsEmpty() {
		List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();
		boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertFalse(checksHaveValues);
	}

	@Test
	public void testParseVariableIds() {
		List<Integer> variableIds = this.settingsUtilParseVariableIds("1|2|3");
		Assert.assertEquals("Should have 3 variable ids", 3, variableIds.size());
		Assert.assertEquals("1st Id should be 1", new Integer(1), variableIds.get(0));
		Assert.assertEquals("2nd Id should be 2", new Integer(2), variableIds.get(1));
		Assert.assertEquals("3rd Id should be 3", new Integer(3), variableIds.get(2));
	}

	private List<Integer> settingsUtilParseVariableIds(String variableIds) {
		return SettingsUtil.parseVariableIds(variableIds);
	}

	private List<SettingDetail> createCheckVariables(boolean hasValue) {
		List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();

		checkVariables.add(this.createSettingDetail(TermId.CHECK_START.getId(), hasValue ? "1" : null));
		checkVariables.add(this.createSettingDetail(TermId.CHECK_INTERVAL.getId(), hasValue ? "4" : null));
		checkVariables.add(this.createSettingDetail(TermId.CHECK_PLAN.getId(), hasValue ? "8414" : null));

		return checkVariables;
	}

	private SettingDetail createSettingDetail(int cvTermId, String value) {
		SettingVariable variable = new SettingVariable();
		variable.setCvTermId(cvTermId);
		SettingDetail settingDetail = new SettingDetail(variable, null, value, false);

		if (cvTermId == TermId.CHECK_PLAN.getId()) {
			List<ValueReference> possibleValues = new ArrayList<ValueReference>();
			possibleValues.add(new ValueReference(8414, "1", "Insert each check in turn"));
			possibleValues.add(new ValueReference(8415, "2", "Insert all checks at each position"));
			settingDetail.setPossibleValues(possibleValues);
		} else if (cvTermId == TermId.CHECK_INTERVAL.getId()) {
			settingDetail.setPossibleValues(new ArrayList<ValueReference>());
		}
		return settingDetail;
	}

	@Test
	public void testcleanSheetAndFileNameWithInvalid() {
		String cleanedName = SettingsUtil.cleanSheetAndFileName("Test[:\\\\/*?|<>]");
		Assert.assertEquals("String should be cleaned", "Test[_________]", cleanedName);
	}

	@Test
	public void testGetCodeInPossibleValues() {
		List<ValueReference> valueRefs = new ArrayList<ValueReference>();
		valueRefs.add(new ValueReference(8414, "1"));
		valueRefs.add(new ValueReference(8415, "2"));
		Assert.assertEquals("Should return 1 since the matching name for 8414 is 1", 1,
				SettingsUtil.getCodeInPossibleValues(valueRefs, "8414"));
		Assert.assertEquals("Should return 2 since the matching name for 8415 is 2", 2,
				SettingsUtil.getCodeInPossibleValues(valueRefs, "8415"));
	}

	@Test
	public void testSetSettingDetailRoleForDefaultVartypes(){
		// Create a standardVaraible as pre-req
		StandardVariable standardVariable = new StandardVariable();
		standardVariable.setVariableTypes(null);

		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.CATEGORICAL_VARIATE.getId(), PROGRAM_UUID)).thenReturn(standardVariable);

		for(VariableType varType : VariableType.values()){
			SettingDetail settingDetail = new SettingDetail();
			SettingVariable settingVariable = new SettingVariable();
			settingVariable.setCvTermId(TermId.CATEGORICAL_VARIATE.getId());
			settingDetail.setVariable(settingVariable);
			settingDetail.setRole(null);

			List<SettingDetail> detailList = new ArrayList<>();
			// use any setting variable that is not a trial instance factor
			detailList.add(settingDetail);

			SettingsUtil.setSettingDetailRole(varType.getId(), detailList, userSelection, fieldbookMiddlewareService,PROGRAM_UUID);
			Assert.assertEquals("Should have the correct phenotypic type role as per the variable type", varType.getRole(),settingDetail.getRole());

		}
	}

	@Test
	public void testSetSettingDetailRoleWithTrialInstanceFactorAsRole() {
		// SettingDetail contains Trial instance factor
		List<SettingDetail> newDetails = new ArrayList<SettingDetail>();
		SettingDetail detail = new SettingDetail();
		SettingVariable variable = new SettingVariable();
		variable.setCvTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
		detail.setVariable(variable);
		newDetails.add(detail);

		// for mode, we use any that is not a germplasm descriptor
		VariableType studyDetailMode = VariableType.STUDY_DETAIL;
		SettingsUtil.setSettingDetailRole(studyDetailMode.getId().intValue(),newDetails,userSelection,fieldbookMiddlewareService,PROGRAM_UUID);
		Assert.assertEquals("Since we had a settingDetail that is a trial instance factor, the detail's role should be converted to Trial Environment",PhenotypicType.TRIAL_ENVIRONMENT, detail.getRole());

	}

	/**
	 * Test for check if given baseline traits empty or null then empty variate list should be returned.
	 */
	@Test
	public void testConvertBaselineTraitsToVariatesWithEmptyBaselineTraits(){
		List<SettingDetail> baselineTraits = new ArrayList<>();

		UserSelection userSelection = new UserSelection();

		List<Variate> baselineVariates =
				SettingsUtil.convertBaselineTraitsToVariates(baselineTraits, userSelection, fieldbookMiddlewareService, PROGRAM_UUID);

		Assert.assertEquals(baselineTraits.size(), baselineVariates.size());
	}

	/**
	 * create SettingDetail instance with variable data, possible values.
	 * @return setting details instance.
	 */
	private SettingDetail createTestDataForSettingDetails(){

		final SettingDetail settingDetail = new SettingDetail();

		SettingVariable settingVariable = new SettingVariable();
		settingVariable.setCvTermId(variableId);
		settingVariable.setName(variableName);
		settingVariable.setDescription(variableDescription);
		settingVariable.setProperty(property.getName());
		settingVariable.setScale(scale.getName());
		settingVariable.setMethod(method.getName());
		settingVariable.setDataType(dataType.getName());
		settingVariable.setCropOntologyId(cropOntologyId);
		settingVariable.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		settingVariable.setMinRange(minValue);
		settingVariable.setMaxRange(maxValue);
		settingVariable.setOperation(Operation.LIKE);
		Set<VariableType> variableTypes = new HashSet<>();
		variableTypes.add(VariableType.STUDY_DETAIL);
		settingVariable.setVariableTypes(variableTypes);

		settingDetail.setVariable(settingVariable);
		ValueReference possibleValue = new ValueReference("Key", "Name", "Description");
		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(possibleValue);
		settingDetail.setPossibleValues(possibleValues);

		ValueReference possibleValueFav = new ValueReference("Key Fav", "Name Fav", "Description Fav");
		List<ValueReference> possibleValuesFav = new ArrayList<>();
		possibleValuesFav.add(possibleValueFav);
		settingDetail.setPossibleValuesFavorite(possibleValuesFav);

		settingDetail.setValue("Setting Detail Value");
		settingDetail.setRole(PhenotypicType.STUDY);
		settingDetail.setVariableType(VariableType.STUDY_DETAIL);

		return settingDetail;
	}

	/**
	 * Create standard variable instance.
	 * @return standard variable instance.
	 */
	private StandardVariable createStandardVariable(){
		final StandardVariable stdVariable = new StandardVariable();
		stdVariable.setId(variableId);
		stdVariable.setName(variableName);
		stdVariable.setDescription(variableDescription);
		stdVariable.setProperty(property);
		stdVariable.setMethod(method);
		stdVariable.setScale(scale);
		stdVariable.setDataType(dataType);
		stdVariable.setIsA(new Term(1050, "Study condition", "Study condition class"));
		stdVariable.setConstraints(new VariableConstraints(minValue, maxValue));
		stdVariable.setCropOntologyId(cropOntologyId);
		return stdVariable;
	}
}
