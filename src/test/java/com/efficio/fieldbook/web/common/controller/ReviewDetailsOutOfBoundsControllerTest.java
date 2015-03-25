package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.common.bean.Datum;
import com.efficio.fieldbook.web.common.bean.ReviewOutOfBoundsChanges;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.bean.Value;
import com.efficio.fieldbook.web.common.form.ReviewDetailsOutOfBoundsForm;

public class ReviewDetailsOutOfBoundsControllerTest {

	private ReviewDetailsOutOfBoundsController reviewDetailsOutOfBoundsController;
	
	@Mock
	private Workbook workbook;
	
	@Before
	public void setUp() {
		
		MockitoAnnotations.initMocks(this);
		
		reviewDetailsOutOfBoundsController = spy(new ReviewDetailsOutOfBoundsController());
		reviewDetailsOutOfBoundsController.setStudySelection(generateUserSelection());
		
		when(workbook.getMeasurementDatasetVariables()).thenReturn(generateMeasurementVariables());
		
	}
	
	
	@Test
	public void testCheckIfCategoricalTraitHasOutOfBoundsData(){
		Boolean val = reviewDetailsOutOfBoundsController.checkIfCategoricalTraitHasOutOfBoundsData(1001);
		assertTrue("The data contains out of bounds data so it should return true",val);
	}
	
	@Test
	public void testIsCategoricalValueOutOfBounds(){
		MeasurementData data = generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar");
		assertTrue("1 is invalid so it is out of bounds", reviewDetailsOutOfBoundsController.isCategoricalValueOutOfBounds(data));
		
		MeasurementData data2 = generateTestMeasurementData(1001, "5001", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar");
		assertFalse("5001 is valid so it is not out of bounds ", reviewDetailsOutOfBoundsController.isCategoricalValueOutOfBounds(data2));
		
		MeasurementData data3 = generateTestMeasurementData(1001, "", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar");
		assertFalse("If the value is empty then it should always be false", reviewDetailsOutOfBoundsController.isCategoricalValueOutOfBounds(data3));
	}
	
	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsRange(){
		MeasurementData data = new MeasurementData();		
		MeasurementVariable var = new MeasurementVariable();
		var.setMinRange(Double.valueOf("1"));
		var.setMaxRange(Double.valueOf("10"));
		data.setMeasurementVariable(var);
		data.setValue("2");
		Assert.assertFalse("Should return false since 2 is not out of range", reviewDetailsOutOfBoundsController.isNumericalValueOutOfBounds(data));
		data.setValue("21");
		Assert.assertTrue("Should return true since 21 is out of range", reviewDetailsOutOfBoundsController.isNumericalValueOutOfBounds(data));
	}
	
	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsNoRange(){
		MeasurementData data = new MeasurementData();		
		MeasurementVariable var = new MeasurementVariable();
		
		data.setMeasurementVariable(var);
		data.setValue("2");				
		Assert.assertFalse("Should return false since 2 is not out of range", reviewDetailsOutOfBoundsController.isNumericalValueOutOfBounds(data));
		data.setValue("21");
		Assert.assertFalse("Should return false since 21 is not out of range", reviewDetailsOutOfBoundsController.isNumericalValueOutOfBounds(data));
	}
	
	
	
	@Test
	public void testFilterColumnsForReviewDetailsTable(){
		
		List<MeasurementVariable> measurementVariables = reviewDetailsOutOfBoundsController.filterColumnsForReviewDetailsTable(generateMeasurementVariables(), 1001);
		assertEquals(4,measurementVariables.size());
		assertEquals(TermId.ENTRY_NO.getId(), measurementVariables.get(0).getTermId());
		assertEquals(TermId.PLOT_NO.getId(),measurementVariables.get(1).getTermId());
		assertEquals(TermId.TRIAL_LOCATION.getId(),measurementVariables.get(2).getTermId());
		assertEquals(1001,measurementVariables.get(3).getTermId());
		
	}
	
	@Test
	public void testGenerateDatatableDataMap(){
		
		Map<String, String> trialInstanceLocationMap = new HashMap<>();
		trialInstanceLocationMap.put("1", "CIMMYT, HARRARE");
		
		MeasurementRow row = reviewDetailsOutOfBoundsController.getUserSelection().getMeasurementRowList().get(0);
		Map<String, Object> retVal = reviewDetailsOutOfBoundsController.generateDatatableDataMap(0, row, 1001, trialInstanceLocationMap);
		assertTrue("The first row has no out of bounds data so map should be empty.", retVal.isEmpty());
		
		MeasurementRow row2 = reviewDetailsOutOfBoundsController.getUserSelection().getMeasurementRowList().get(1);
		Map<String, Object> retVal2 = reviewDetailsOutOfBoundsController.generateDatatableDataMap(1, row2, 1001, trialInstanceLocationMap);
		assertFalse("The first row has  out of bounds data so map should have valyes.", retVal2.isEmpty());
	}
	
	@Test
	public void testGetCategoricalWithOutOfBoundsOnly(){
		List<MeasurementVariable> measurementVariables = reviewDetailsOutOfBoundsController.getTraitsWithOutOfBoundsOnly(generateMeasurementVariables());
		assertFalse(measurementVariables.isEmpty());
	}
	
	@Test
	public void testGetPossibleValueIDByValue(){
		
		String retVal = reviewDetailsOutOfBoundsController.getPossibleValueIDByValue("1", generatePossibleValues());
		assertEquals("The value 1 is equal to id (5001) in possible values","5001", retVal);
		
		String retVal2 = reviewDetailsOutOfBoundsController.getPossibleValueIDByValue("", generatePossibleValues());
		assertEquals("The blank value will return blank","", retVal2);
		
		String retVal3 = reviewDetailsOutOfBoundsController.getPossibleValueIDByValue("99", generatePossibleValues());
		assertEquals("if 99 value is out of bounds, the method will just return the same value","99", retVal3);
		
	}

	
	@Test
	public void testUpdateMeasurementData(){
		
		Value value = new Value();
		value.setNewValue("1000");
		value.setIsSelected(true);
		value.setRowIndex(0);
	
		MeasurementData measurementData1 = generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar");
		value.setAction("1");
		reviewDetailsOutOfBoundsController.updateMeasurementData(measurementData1, value);
		assertEquals("1", measurementData1.getValue());
		
		MeasurementData measurementData2 = generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar");
		value.setAction("2");
		reviewDetailsOutOfBoundsController.updateMeasurementData(measurementData2, value);
		assertEquals(value.getNewValue(), measurementData2.getValue());
		
		MeasurementData measurementData3 = generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar");
		value.setAction("3");
		reviewDetailsOutOfBoundsController.updateMeasurementData(measurementData3, value);
		assertEquals("missing", measurementData3.getValue());
		
	}
	
	@Test
	public void testSetMeasurementDataValue(){
		
		Value value = new Value();
		value.setNewValue("1");
		value.setIsSelected(true);
		value.setRowIndex(0);
		
		MeasurementData measurementData = generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar");
		
		reviewDetailsOutOfBoundsController.setMeasurementDataValue("1", measurementData, value);
		
		assertEquals("1", measurementData.getValue());
		assertTrue(measurementData.isAccepted());
		
	}
	
	@Test
	public void testSubmitDetails_Next(){
		
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		measurementVariable.setTermId(1001);
		measurementVariable.setPossibleValues(generatePossibleValues());
		measurementVariable.setFactor(false);
		
		ReviewDetailsOutOfBoundsForm form = new ReviewDetailsOutOfBoundsForm();
		form.setMeasurementVariable(measurementVariable);
		form.setTraitIndex(0);
		form.setTraitSize(2);
		
		reviewDetailsOutOfBoundsController.submitDetails("next", form, null);
		
		assertEquals(1002, form.getMeasurementVariable().getTermId());
		assertEquals(1, form.getTraitIndex());

	}
	
	@Test
	public void testSubmitDetails_Previous(){
		
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(1002);
		measurementVariable.setPossibleValues(generatePossibleValues());
		measurementVariable.setFactor(false);
		measurementVariable.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		
		ReviewDetailsOutOfBoundsForm form = new ReviewDetailsOutOfBoundsForm();
		form.setMeasurementVariable(measurementVariable);
		form.setTraitIndex(1);
		form.setTraitSize(2);
		reviewDetailsOutOfBoundsController.submitDetails("previous", form, null);
		
		assertEquals(1001, form.getMeasurementVariable().getTermId());
		assertEquals(0, form.getTraitIndex());
		
	}
	
	@Test
	public void testProcessOutOfBoundsChanges_OutOfBounds(){
		
		List<Datum> data = new ArrayList<>();
		List<Value> values = new ArrayList<>();
		Value value = new Value();
		value.setRowIndex(0);
		value.setIsSelected(true);
		value.setAction("2");
		value.setNewValue("9999");
		values.add(value);
		Datum datum = new Datum();
		datum.setTermId(1001);
		datum.setValues(values);
		data.add(datum);
		
		ReviewOutOfBoundsChanges changes = new ReviewOutOfBoundsChanges();
		changes.setData(data);
		
		reviewDetailsOutOfBoundsController.processOutOfBoundsChanges(changes);
		
		MeasurementRow row = reviewDetailsOutOfBoundsController.getUserSelection().getMeasurementRowList().get(0);
		
		assertEquals("9999", row.getMeasurementData(1001).getValue());
		assertTrue(row.getMeasurementData(1001).isAccepted());
		
	}
	
	@Test
	public void testProcessOutOfBoundsChanges_ValidValue(){
		
		List<Datum> data = new ArrayList<>();
		List<Value> values = new ArrayList<>();
		Value value = new Value();
		value.setRowIndex(0);
		value.setIsSelected(true);
		value.setAction("2");
		value.setNewValue("1");
		values.add(value);
		Datum datum = new Datum();
		datum.setTermId(1001);
		datum.setValues(values);
		data.add(datum);
		
		ReviewOutOfBoundsChanges changes = new ReviewOutOfBoundsChanges();
		changes.setData(data);
		
		reviewDetailsOutOfBoundsController.processOutOfBoundsChanges(changes);
		
		MeasurementRow row = reviewDetailsOutOfBoundsController.getUserSelection().getMeasurementRowList().get(0);
		
		assertEquals("5001", row.getMeasurementData(1001).getValue());
		assertFalse(row.getMeasurementData(1001).isAccepted());
		
	}
	
	@Test
	public void testIsValueOutOfRangeIfMissing(){
		MeasurementData data = new MeasurementData();
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setMinRange(Double.valueOf(1));
		measurementVariable.setMaxRange(Double.valueOf(10));
		data.setMeasurementVariable(measurementVariable);
		Assert.assertTrue("Should return true since missing is out of bounds", reviewDetailsOutOfBoundsController.isValueOutOfRange("MISSING", data));
	}
	
	@Test
	public void testIsValueOutOfRangeIfWithinRange(){
		MeasurementData data = new MeasurementData();
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setMinRange(Double.valueOf(1));
		measurementVariable.setMaxRange(Double.valueOf(10));
		data.setMeasurementVariable(measurementVariable);
		Assert.assertFalse("Should return false since value is within range", reviewDetailsOutOfBoundsController.isValueOutOfRange("3", data));
	}
	
	@Test
	public void testIsValueOutOfRangeIfOutOfRangeRange(){
		MeasurementData data = new MeasurementData();
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setMinRange(Double.valueOf(1));
		measurementVariable.setMaxRange(Double.valueOf(10));
		data.setMeasurementVariable(measurementVariable);
		Assert.assertTrue("Should return True since value is out of range", reviewDetailsOutOfBoundsController.isValueOutOfRange("13", data));
	}
	
	private UserSelection generateUserSelection(){
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		
		MeasurementRow row1 = new MeasurementRow();
		List<MeasurementData> dataList1 = new ArrayList<MeasurementData>();
		dataList1.add(generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(), "CharacterVar"));
		dataList1.add(generateTestMeasurementData(1001, "5001", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar"));
		dataList1.add(generateTestMeasurementData(1002, "1", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar2"));
		row1.setDataList(dataList1);
		measurementRowList.add(row1);
		
		MeasurementRow row2 = new MeasurementRow();
		List<MeasurementData> dataList2 = new ArrayList<MeasurementData>();
		dataList2.add(generateTestMeasurementData(1000, "2nd", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(), "CharacterVar"));
		dataList2.add(generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar"));
		dataList2.add(generateTestMeasurementData(1002, "5002", TermId.CATEGORICAL_VARIABLE.getId(), generatePossibleValues(), "CategVar2"));
		row2.setDataList(dataList2);
		measurementRowList.add(row2);
		
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(workbook);
		
		return userSelection;
	}
	
	private List<ValueReference> generatePossibleValues(){
		List<ValueReference> possibleValues = new ArrayList<>();
		
		ValueReference val1 = new ValueReference();
		val1.setId(5001);
		val1.setKey("5001");
		val1.setName("1");
		val1.setDescription("Possible Value 1");
		possibleValues.add(val1);
		
		ValueReference val2 = new ValueReference();
		val2.setId(5002);
		val2.setName("2");
		val2.setKey("5002");
		val2.setDescription("Possible Value 2");
		possibleValues.add(val2);
		
		ValueReference val3 = new ValueReference();
		val3.setId(5003);
		val3.setKey("5003");
		val3.setName("3");
		val3.setDescription("Possible Value 3");
		possibleValues.add(val3);
		
		
		return possibleValues;
	}
	
	private List<MeasurementVariable> generateMeasurementVariables(){
		List<MeasurementVariable> measurementVariables = new ArrayList<>();
		
		MeasurementVariable var1 = new MeasurementVariable();
		var1.setTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
		var1.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		measurementVariables.add(var1);
		
		MeasurementVariable var2 = new MeasurementVariable();
		var2.setTermId(TermId.ENTRY_NO.getId());
		var2.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		measurementVariables.add(var2);
		
		MeasurementVariable var3= new MeasurementVariable();
		var3.setTermId(TermId.PLOT_NO.getId());
		var3.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		measurementVariables.add(var3);
		
		MeasurementVariable var4= new MeasurementVariable();
		var4.setTermId(TermId.TRIAL_LOCATION.getId());
		var4.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		measurementVariables.add(var4);
		
		MeasurementVariable var5= new MeasurementVariable();
		var5.setTermId(1001);
		var5.setPossibleValues(generatePossibleValues());
		var5.setFactor(false);
		var5.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		measurementVariables.add(var5);
		
		MeasurementVariable var6= new MeasurementVariable();
		var6.setTermId(1002);
		var6.setPossibleValues(generatePossibleValues());
		var6.setFactor(false);
		var6.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		measurementVariables.add(var6);
		
		MeasurementVariable var7= new MeasurementVariable();
		var7.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		var7.setTermId(1003);
		measurementVariables.add(var7);
		
		return measurementVariables;
	}
	
	private MeasurementData generateTestMeasurementData(int termId, String value, int dataTypeId, List<ValueReference> possibleValues, String varName){
		MeasurementData emptyData = new MeasurementData();
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setDataTypeId(dataTypeId);
		measurementVariable.setPossibleValues(possibleValues);
		measurementVariable.setName(varName);
		
		emptyData.setcValueId("");
		emptyData.setDataType("");
		emptyData.setEditable(false);
		emptyData.setLabel("");
		emptyData.setPhenotypeId(0);
		emptyData.setValue(value);
		emptyData.setMeasurementVariable(measurementVariable);
		return emptyData;
	}
}
