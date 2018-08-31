
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.common.bean.Datum;
import com.efficio.fieldbook.web.common.bean.ReviewOutOfBoundsChanges;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.bean.Value;
import com.efficio.fieldbook.web.common.form.ReviewDetailsOutOfBoundsForm;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import junit.framework.Assert;

public class ReviewDetailsOutOfBoundsControllerTest {


	private static final int STUDY_ID = 10101;
	private static final String LOCATION_ID1 = "7001";
	private static final String LOCATION_ID2 = "7002";
	private static final String LOCATION_ID3 = "7003";
	private static final String LOCATION_ID4 = "7004";
	private static final String LOCATION_ID5 = "7005";

	private ReviewDetailsOutOfBoundsController reviewDetailsOutOfBoundsController;

	@Mock
	private Workbook workbook;
	
	@Mock
	private StudyDataManager studyDataManager;
	
	private BiMap<String, String> studyLocationsMap;

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);

		this.reviewDetailsOutOfBoundsController = Mockito.spy(new ReviewDetailsOutOfBoundsController());
		final UserSelection userSelection = this.generateUserSelection();
		this.reviewDetailsOutOfBoundsController.setStudySelection(userSelection);
		this.reviewDetailsOutOfBoundsController.setStudyDataManager(this.studyDataManager);
		
		this.studyLocationsMap = HashBiMap.create();
		this.studyLocationsMap.put(LOCATION_ID1, RandomStringUtils.randomAlphabetic(20));
		this.studyLocationsMap.put(LOCATION_ID2, RandomStringUtils.randomAlphabetic(20));
		this.studyLocationsMap.put(LOCATION_ID3, RandomStringUtils.randomAlphabetic(20));
		this.studyLocationsMap.put(LOCATION_ID4, RandomStringUtils.randomAlphabetic(20));
		this.studyLocationsMap.put(LOCATION_ID5, RandomStringUtils.randomAlphabetic(20));
		Mockito.when(this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(STUDY_ID))
				.thenReturn(this.studyLocationsMap);
		Mockito.when(this.workbook.getTrialObservations()).thenReturn(userSelection.getMeasurementRowList());
		Mockito.when(this.workbook.getMeasurementDatasetVariables()).thenReturn(this.generateMeasurementVariables());
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(STUDY_ID);
		Mockito.when(this.workbook.getStudyDetails()).thenReturn(studyDetails);
	}

	@Test
	public void testCheckIfCategoricalTraitHasOutOfBoundsData() {
		Boolean val = this.reviewDetailsOutOfBoundsController.checkIfCategoricalTraitHasOutOfBoundsData(1001);
		Assert.assertTrue("The data contains out of bounds data so it should return true", val);
	}

	@Test
	public void testIsCategoricalValueOutOfBounds() {
		MeasurementData data =
				this.generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(), "CategVar");
		Assert.assertTrue("1 is invalid so it is out of bounds",
				this.reviewDetailsOutOfBoundsController.isCategoricalValueOutOfBounds(data));

		MeasurementData data2 =
				this.generateTestMeasurementData(1001, "5001", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(),
						"CategVar");
		Assert.assertFalse("5001 is valid so it is not out of bounds ",
				this.reviewDetailsOutOfBoundsController.isCategoricalValueOutOfBounds(data2));

		MeasurementData data3 =
				this.generateTestMeasurementData(1001, "", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(), "CategVar");
		Assert.assertFalse("If the value is empty then it should always be false",
				this.reviewDetailsOutOfBoundsController.isCategoricalValueOutOfBounds(data3));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsRange() {
		MeasurementData data = new MeasurementData();
		MeasurementVariable var = new MeasurementVariable();
		var.setMinRange(Double.valueOf("1"));
		var.setMaxRange(Double.valueOf("10"));
		data.setMeasurementVariable(var);
		data.setValue("2");
		Assert.assertFalse("Should return false since 2 is not out of range",
				this.reviewDetailsOutOfBoundsController.isNumericalValueOutOfBounds(data));
		data.setValue("21");
		Assert.assertTrue("Should return true since 21 is out of range",
				this.reviewDetailsOutOfBoundsController.isNumericalValueOutOfBounds(data));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsNoRange() {
		MeasurementData data = new MeasurementData();
		MeasurementVariable var = new MeasurementVariable();

		data.setMeasurementVariable(var);
		data.setValue("2");
		Assert.assertFalse("Should return false since 2 is not out of range",
				this.reviewDetailsOutOfBoundsController.isNumericalValueOutOfBounds(data));
		data.setValue("21");
		Assert.assertFalse("Should return false since 21 is not out of range",
				this.reviewDetailsOutOfBoundsController.isNumericalValueOutOfBounds(data));
	}

	@Test
	public void testFilterColumnsForReviewDetailsTableWithLocationIdPresent() {

		List<MeasurementVariable> measurementVariables =
				this.reviewDetailsOutOfBoundsController.filterColumnsForReviewDetailsTable(this.generateMeasurementVariables(), 1001);
		Assert.assertEquals(4, measurementVariables.size());
		Assert.assertEquals(TermId.ENTRY_NO.getId(), measurementVariables.get(0).getTermId());
		Assert.assertEquals(TermId.PLOT_NO.getId(), measurementVariables.get(1).getTermId());
		Assert.assertEquals(TermId.LOCATION_ID.getId(), measurementVariables.get(2).getTermId());
		Assert.assertEquals(1001, measurementVariables.get(3).getTermId());

	}
	
	@Test
	public void testFilterColumnsForReviewDetailsTableWithoutLocationIdPresent() {

		final List<MeasurementVariable> variables = this.generateMeasurementVariables();
		Iterator<MeasurementVariable> iterator = variables.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getTermId() == TermId.LOCATION_ID.getId()) {
				iterator.remove();
			}
		}
		List<MeasurementVariable> measurementVariables =
				this.reviewDetailsOutOfBoundsController.filterColumnsForReviewDetailsTable(variables, 1001);
		Assert.assertEquals(4, measurementVariables.size());
		Assert.assertEquals(TermId.TRIAL_INSTANCE_FACTOR.getId(), measurementVariables.get(0).getTermId());
		Assert.assertEquals(TermId.ENTRY_NO.getId(), measurementVariables.get(1).getTermId());
		Assert.assertEquals(TermId.PLOT_NO.getId(), measurementVariables.get(2).getTermId());
		Assert.assertEquals(1001, measurementVariables.get(3).getTermId());
	}

	@Test
	public void testGenerateDatatableDataMap() {

		Map<String, String> trialInstanceLocationMap = new HashMap<>();
		trialInstanceLocationMap.put("1", "CIMMYT, HARRARE");

		MeasurementRow row = this.reviewDetailsOutOfBoundsController.getUserSelection().getMeasurementRowList().get(0);
		Map<String, Object> retVal =
				this.reviewDetailsOutOfBoundsController.generateDatatableDataMap(0, row, 1001, trialInstanceLocationMap);
		Assert.assertTrue("The first row has no out of bounds data so map should be empty.", retVal.isEmpty());

		MeasurementRow row2 = this.reviewDetailsOutOfBoundsController.getUserSelection().getMeasurementRowList().get(1);
		Map<String, Object> retVal2 =
				this.reviewDetailsOutOfBoundsController.generateDatatableDataMap(1, row2, 1001, trialInstanceLocationMap);
		Assert.assertFalse("The first row has  out of bounds data so map should have valyes.", retVal2.isEmpty());
	}

	@Test
	public void testGetCategoricalWithOutOfBoundsOnly() {
		List<MeasurementVariable> measurementVariables =
				this.reviewDetailsOutOfBoundsController.getTraitsWithOutOfBoundsOnly(this.generateMeasurementVariables());
		Assert.assertFalse(measurementVariables.isEmpty());
	}

	@Test
	public void testGetPossibleValueIDByValue() {

		String retVal = this.reviewDetailsOutOfBoundsController.getPossibleValueIDByValue("1", this.generatePossibleValues());
		Assert.assertEquals("The value 1 is equal to id (5001) in possible values", "5001", retVal);

		String retVal2 = this.reviewDetailsOutOfBoundsController.getPossibleValueIDByValue("", this.generatePossibleValues());
		Assert.assertEquals("The blank value will return blank", "", retVal2);

		String retVal3 = this.reviewDetailsOutOfBoundsController.getPossibleValueIDByValue("99", this.generatePossibleValues());
		Assert.assertEquals("if 99 value is out of bounds, the method will just return the same value", "99", retVal3);

	}

	@Test
	public void testUpdateMeasurementData() {

		Value value = new Value();
		value.setNewValue("1000");
		value.setIsSelected(true);
		value.setRowIndex(0);

		MeasurementData measurementData1 =
				this.generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(), "CategVar");
		value.setAction("1");
		this.reviewDetailsOutOfBoundsController.updateMeasurementData(measurementData1, value);
		Assert.assertEquals("1", measurementData1.getValue());

		MeasurementData measurementData2 =
				this.generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(), "CategVar");
		value.setAction("2");
		this.reviewDetailsOutOfBoundsController.updateMeasurementData(measurementData2, value);
		Assert.assertEquals(value.getNewValue(), measurementData2.getValue());

		MeasurementData measurementData3 =
				this.generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(), "CategVar");
		value.setAction("3");
		this.reviewDetailsOutOfBoundsController.updateMeasurementData(measurementData3, value);
		Assert.assertEquals("missing", measurementData3.getValue());

	}

	@Test
	public void testSetMeasurementDataValue() {

		Value value = new Value();
		value.setNewValue("1");
		value.setIsSelected(true);
		value.setRowIndex(0);

		MeasurementData measurementData =
				this.generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(), "CategVar");

		this.reviewDetailsOutOfBoundsController.setMeasurementDataValue("1", measurementData, value);

		Assert.assertEquals("1", measurementData.getValue());
		Assert.assertTrue(measurementData.isAccepted());

	}

	@Test
	public void testSubmitDetails_Next() {

		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		measurementVariable.setTermId(1001);
		measurementVariable.setPossibleValues(this.generatePossibleValues());
		measurementVariable.setFactor(false);

		ReviewDetailsOutOfBoundsForm form = new ReviewDetailsOutOfBoundsForm();
		form.setMeasurementVariable(measurementVariable);
		form.setTraitIndex(0);
		form.setTraitSize(2);

		this.reviewDetailsOutOfBoundsController.submitDetails("next", form, null);

		Assert.assertEquals(1002, form.getMeasurementVariable().getTermId());
		Assert.assertEquals(1, form.getTraitIndex());

	}

	@Test
	public void testSubmitDetails_Previous() {

		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(1002);
		measurementVariable.setPossibleValues(this.generatePossibleValues());
		measurementVariable.setFactor(false);
		measurementVariable.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());

		ReviewDetailsOutOfBoundsForm form = new ReviewDetailsOutOfBoundsForm();
		form.setMeasurementVariable(measurementVariable);
		form.setTraitIndex(1);
		form.setTraitSize(2);
		this.reviewDetailsOutOfBoundsController.submitDetails("previous", form, null);

		Assert.assertEquals(1001, form.getMeasurementVariable().getTermId());
		Assert.assertEquals(0, form.getTraitIndex());

	}

	@Test
	public void testProcessOutOfBoundsChanges_OutOfBounds() {

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

		this.reviewDetailsOutOfBoundsController.processOutOfBoundsChanges(changes);

		MeasurementRow row = this.reviewDetailsOutOfBoundsController.getUserSelection().getMeasurementRowList().get(0);

		Assert.assertEquals("9999", row.getMeasurementData(1001).getValue());
		Assert.assertTrue(row.getMeasurementData(1001).isAccepted());

	}

	@Test
	public void testProcessOutOfBoundsChanges_ValidValue() {

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

		this.reviewDetailsOutOfBoundsController.processOutOfBoundsChanges(changes);

		MeasurementRow row = this.reviewDetailsOutOfBoundsController.getUserSelection().getMeasurementRowList().get(0);

		Assert.assertEquals("5001", row.getMeasurementData(1001).getValue());
		Assert.assertFalse(row.getMeasurementData(1001).isAccepted());

	}

	@Test
	public void testIsValueOutOfRangeIfMissing() {
		MeasurementData data = new MeasurementData();
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setMinRange(Double.valueOf(1));
		measurementVariable.setMaxRange(Double.valueOf(10));
		data.setMeasurementVariable(measurementVariable);
		Assert.assertTrue("Should return true since missing is out of bounds",
				this.reviewDetailsOutOfBoundsController.isValueOutOfRange("MISSING", data));
	}

	@Test
	public void testIsValueOutOfRangeIfWithinRange() {
		MeasurementData data = new MeasurementData();
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setMinRange(Double.valueOf(1));
		measurementVariable.setMaxRange(Double.valueOf(10));
		data.setMeasurementVariable(measurementVariable);
		Assert.assertFalse("Should return false since value is within range",
				this.reviewDetailsOutOfBoundsController.isValueOutOfRange("3", data));
	}

	@Test
	public void testIsValueOutOfRangeIfOutOfRangeRange() {
		MeasurementData data = new MeasurementData();
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setMinRange(Double.valueOf(1));
		measurementVariable.setMaxRange(Double.valueOf(10));
		data.setMeasurementVariable(measurementVariable);
		Assert.assertTrue("Should return True since value is out of range",
				this.reviewDetailsOutOfBoundsController.isValueOutOfRange("13", data));
	}
	
	@Test
	public void testGetTrialInstanceLocationMap() {
		final Map<String, String> map = this.reviewDetailsOutOfBoundsController.getTrialInstanceLocationMap();
		Assert.assertEquals(2, map.size());
		Assert.assertNotNull(map.get("1"));
		Assert.assertEquals(this.studyLocationsMap.get(LOCATION_ID4), map.get("1"));
		Assert.assertNotNull(map.get("2"));
		Assert.assertEquals(this.studyLocationsMap.get(LOCATION_ID5), map.get("2"));
	}

	private UserSelection generateUserSelection() {
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();

		MeasurementRow row1 = new MeasurementRow();
		List<MeasurementData> dataList1 = new ArrayList<MeasurementData>();
		dataList1.add(this.generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"CharacterVar"));
		dataList1.add(this.generateTestMeasurementData(1001, "5001", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(),
				"CategVar"));
		dataList1.add(this.generateTestMeasurementData(1002, "1", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(),
				"CategVar2"));
		dataList1.add(this.generateTestMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId(), "1", TermId.NUMERIC_VARIABLE.getId(),
				new ArrayList<ValueReference>(), TermId.TRIAL_INSTANCE_FACTOR.name()));
		dataList1.add(this.generateTestMeasurementData(TermId.LOCATION_ID.getId(), LOCATION_ID4, TermId.NUMERIC_VARIABLE.getId(),
				new ArrayList<ValueReference>(), "LOCATION_NAME"));
		row1.setDataList(dataList1);
		measurementRowList.add(row1);

		MeasurementRow row2 = new MeasurementRow();
		List<MeasurementData> dataList2 = new ArrayList<MeasurementData>();
		dataList2.add(this.generateTestMeasurementData(1000, "2nd", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"CharacterVar"));
		dataList2.add(this.generateTestMeasurementData(1001, "1", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(),
				"CategVar"));
		dataList2.add(this.generateTestMeasurementData(1002, "5002", TermId.CATEGORICAL_VARIABLE.getId(), this.generatePossibleValues(),
				"CategVar2"));
		dataList2.add(this.generateTestMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId(), "2", TermId.NUMERIC_VARIABLE.getId(),
				new ArrayList<ValueReference>(), TermId.TRIAL_INSTANCE_FACTOR.name()));
		dataList2.add(this.generateTestMeasurementData(TermId.LOCATION_ID.getId(), LOCATION_ID5, TermId.NUMERIC_VARIABLE.getId(),
				new ArrayList<ValueReference>(), "LOCATION_NAME"));
		row2.setDataList(dataList2);
		measurementRowList.add(row2);

		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(this.workbook);

		return userSelection;
	}

	private List<ValueReference> generatePossibleValues() {
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

	private List<MeasurementVariable> generateMeasurementVariables() {
		List<MeasurementVariable> measurementVariables = new ArrayList<>();

		MeasurementVariable var1 = new MeasurementVariable();
		var1.setTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
		var1.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		measurementVariables.add(var1);

		MeasurementVariable var2 = new MeasurementVariable();
		var2.setTermId(TermId.ENTRY_NO.getId());
		var2.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		measurementVariables.add(var2);

		MeasurementVariable var3 = new MeasurementVariable();
		var3.setTermId(TermId.PLOT_NO.getId());
		var3.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		measurementVariables.add(var3);

		MeasurementVariable var4 = new MeasurementVariable();
		var4.setTermId(TermId.LOCATION_ID.getId());
		var4.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		measurementVariables.add(var4);

		MeasurementVariable var5 = new MeasurementVariable();
		var5.setTermId(1001);
		var5.setPossibleValues(this.generatePossibleValues());
		var5.setFactor(false);
		var5.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		measurementVariables.add(var5);

		MeasurementVariable var6 = new MeasurementVariable();
		var6.setTermId(1002);
		var6.setPossibleValues(this.generatePossibleValues());
		var6.setFactor(false);
		var6.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		measurementVariables.add(var6);

		MeasurementVariable var7 = new MeasurementVariable();
		var7.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		var7.setTermId(1003);
		measurementVariables.add(var7);

		return measurementVariables;
	}

	private MeasurementData generateTestMeasurementData(int termId, String value, int dataTypeId, List<ValueReference> possibleValues,
			String varName) {
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
