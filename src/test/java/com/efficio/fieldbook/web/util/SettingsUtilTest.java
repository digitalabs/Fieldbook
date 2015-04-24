package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.generationcp.middleware.util.Debug;
import org.junit.Test;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;

public class SettingsUtilTest {

	@Test
	public void testConvertXmlDatasetToWorkbookAndBack() {
		Dataset dataset = new Dataset();
		
		dataset.setConditions(new ArrayList<Condition>());
		dataset.setFactors(new ArrayList<Factor>());
		dataset.setVariates(new ArrayList<Variate>());
		
		dataset.getConditions().add(new Condition("CONDITION1", "CONDITION1", "PERSON", "DBCV", "ASSIGNED", PhenotypicType.STUDY.toString(), "C", "Meeh", null, null, null));
		dataset.getFactors().add(new Factor("FACTOR1", "FACTOR1", "GERMPLASM ENTRY", "NUMBER", "ENUMERATED", PhenotypicType.GERMPLASM.toString(), "N", 0));
		dataset.getVariates().add(new Variate("VARIATE1", "VARIATE1", "YIELD (GRAIN)", "Kg/ha", "Paddy Rice", PhenotypicType.VARIATE.toString(), "N", TermId.NUMERIC_VARIABLE.getId(), new ArrayList<ValueReference>(), 0.0, 0.0));
		
		Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true);
		Debug.println(0, workbook);
		
		Dataset newDataset = (Dataset)SettingsUtil.convertWorkbookToXmlDataset(workbook);
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
    	List<SettingDetail> removedConditions = createCheckVariables(true);
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
    	List<SettingDetail> removedConditions = createCheckVariables(true);
    	int code = SettingsUtil.getCodeValue("8411", removedConditions, TermId.CHECK_START.getId());
    	Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
    }
	
	@Test
    public void testGetCodeValueWhenPossibleValuesIsNotNullButEmpty() {
    	List<SettingDetail> removedConditions = createCheckVariables(true);
    	int code = SettingsUtil.getCodeValue("8412", removedConditions, TermId.CHECK_INTERVAL.getId());
    	Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
    }
    
    @Test
    public void testGetCodeValueInvalid() {
    	List<SettingDetail> removedConditions = createCheckVariables(true);
    	int code = SettingsUtil.getCodeValue("8413", removedConditions, TermId.CHECK_PLAN.getId());
    	Assert.assertNotSame("Expected 1 but got " + code + " instead.", 1, code);
    }
    
    @Test
    public void testIfCheckVariablesHaveValues() {
    	List<SettingDetail> checkVariables = createCheckVariables(true);
    	boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
    	Assert.assertTrue(checksHaveValues);
    }
    
    @Test
    public void testIfCheckVariablesHaveNoValues() {
    	List<SettingDetail> checkVariables = createCheckVariables(false);
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
	public void testParseVariableIds(){
		List<Integer> variableIds = settingsUtilParseVariableIds("1|2|3");
		Assert.assertEquals("Should have 3 variable ids", 3, variableIds.size());
		Assert.assertEquals("1st Id should be 1", new Integer(1), variableIds.get(0));
		Assert.assertEquals("2nd Id should be 2", new Integer(2), variableIds.get(1));
		Assert.assertEquals("3rd Id should be 3", new Integer(3), variableIds.get(2));
	}
	
	private List<Integer> settingsUtilParseVariableIds(String variableIds){
		return SettingsUtil.parseVariableIds(variableIds);
	}
    private List<SettingDetail> createCheckVariables(boolean hasValue) {
		List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();
		
		checkVariables.add(createSettingDetail(TermId.CHECK_START.getId(), hasValue ? "1" : null));
		checkVariables.add(createSettingDetail(TermId.CHECK_INTERVAL.getId(), hasValue ? "4" : null));
		checkVariables.add(createSettingDetail(TermId.CHECK_PLAN.getId(), hasValue ? "8414" : null));
		
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
    public void testcleanSheetAndFileNameWithInvalid(){
    	String cleanedName = SettingsUtil.cleanSheetAndFileName("Test[:\\\\/*?|<>]");
    	Assert.assertEquals("String should be cleaned", "Test[_________]", cleanedName);
    }
    
    @Test
    public void testGetCodeInPossibleValues(){
    	List<ValueReference> valueRefs = new ArrayList<ValueReference>();
    	valueRefs.add(new ValueReference(8414, "1"));
    	valueRefs.add(new ValueReference(8415, "2"));    	
    	Assert.assertEquals("Should return 1 since the matching name for 8414 is 1", 1, SettingsUtil.getCodeInPossibleValues(valueRefs, "8414"));
    	Assert.assertEquals("Should return 2 since the matching name for 8415 is 2", 2, SettingsUtil.getCodeInPossibleValues(valueRefs, "8415"));
    }
}
