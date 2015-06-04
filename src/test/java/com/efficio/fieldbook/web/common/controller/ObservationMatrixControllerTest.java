
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.ValidationService;

public class ObservationMatrixControllerTest {

	private ObservationMatrixController observationMatrixController;

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);

		this.observationMatrixController = Mockito.spy(new ObservationMatrixController());

	}

	@Test
	public void testCopyMeasurementValue() {

		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		this.observationMatrixController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			Assert.assertEquals("The origRow's measurement value must be equal to the valueRow's measurement value", origRow.getDataList()
					.get(x).getValue(), valueRow.getDataList().get(x).getValue());
		}

	}

	@Test
	public void testCopyMeasurementValueNullEmptyPossibleValues() {

		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		MeasurementData nullData = new MeasurementData();
		nullData.setcValueId(null);
		nullData.setDataType(null);
		nullData.setEditable(false);
		nullData.setLabel(null);
		nullData.setPhenotypeId(null);
		nullData.setValue(null);

		MeasurementVariable measurementVariable = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<>();
		measurementVariable.setPossibleValues(possibleValues);
		nullData.setMeasurementVariable(measurementVariable);

		origRow.getDataList().add(nullData);
		valueRow.getDataList().add(nullData);

		this.observationMatrixController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			Assert.assertEquals("The origRow's measurement value must be equal to the valueRow's measurement value", origRow.getDataList()
					.get(x).getValue(), valueRow.getDataList().get(x).getValue());
		}

	}

	@Test
	public void testCopyMeasurementValueNullNullPossibleValuesAndValueIsNotEmpty() {

		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		MeasurementData data = new MeasurementData();
		data.setcValueId("1234");
		data.setDataType(null);
		data.setEditable(false);
		data.setLabel(null);
		data.setPhenotypeId(null);
		data.setValue(null);

		MeasurementData data2 = new MeasurementData();
		data2.setcValueId(null);
		data2.setDataType(null);
		data2.setEditable(false);
		data2.setLabel(null);
		data2.setPhenotypeId(null);
		data2.setValue("jjasd");

		MeasurementVariable measurementVariable = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		measurementVariable.setPossibleValues(possibleValues);
		data.setMeasurementVariable(measurementVariable);

		origRow.getDataList().add(data);
		valueRow.getDataList().add(data2);

		this.observationMatrixController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			Assert.assertEquals("The origRow's measurement value must be equal to the valueRow's measurement value", origRow.getDataList()
					.get(x).getValue(), valueRow.getDataList().get(x).getValue());
		}

	}

	@Test
	public void testCopyMeasurementValueWithCustomCategoricalValue() {

		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());

		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		possibleValues.add(new ValueReference());
		possibleValues.get(0).setId(1);
		possibleValues.get(0).setKey("1");
		possibleValues.get(1).setId(2);
		possibleValues.get(1).setKey(origRow.getDataList().get(0).getValue());

		origRow.getDataList().get(0).getMeasurementVariable().setPossibleValues(possibleValues);

		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());
		valueRow.getDataList().get(0).setAccepted(true);

		this.observationMatrixController.copyMeasurementValue(origRow, valueRow, true);

		Assert.assertTrue(origRow.getDataList().get(0).isCustomCategoricalValue());

	}

	private List<MeasurementData> generateTestDataList() {

		List<MeasurementData> dataList = new ArrayList<>();

		for (int x = 0; x < 10; x++) {
			MeasurementData data = new MeasurementData();
			data.setcValueId(UUID.randomUUID().toString());
			data.setDataType(UUID.randomUUID().toString());
			data.setEditable(true);
			data.setLabel(UUID.randomUUID().toString());
			data.setPhenotypeId(x);
			data.setValue(UUID.randomUUID().toString());
			data.setMeasurementVariable(new MeasurementVariable());
			dataList.add(data);
		}

		MeasurementData nullData = new MeasurementData();
		nullData.setcValueId(null);
		nullData.setDataType(null);
		nullData.setEditable(false);
		nullData.setLabel(null);
		nullData.setPhenotypeId(null);
		nullData.setValue(null);

		MeasurementVariable measurementVariable = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		measurementVariable.setPossibleValues(possibleValues);
		nullData.setMeasurementVariable(measurementVariable);
		dataList.add(nullData);

		MeasurementData emptyData = new MeasurementData();
		emptyData.setcValueId("");
		emptyData.setDataType("");
		emptyData.setEditable(false);
		emptyData.setLabel("");
		emptyData.setPhenotypeId(0);
		emptyData.setValue("");
		emptyData.setMeasurementVariable(measurementVariable);
		dataList.add(emptyData);

		return dataList;
	}

	@Test
	public void testEditExperimentCells() throws MiddlewareQueryException {
		int termId = 2000;
		ExtendedModelMap model = new ExtendedModelMap();
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		dataList.add(this.generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName1"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<MeasurementData>();
		dataList.add(this.generateTestMeasurementData(termId, "2nd", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.observationMatrixController.setStudySelection(userSelection);
		this.observationMatrixController.editExperimentCells(1, termId, model);
		MeasurementData data = (MeasurementData) model.get("measurementData");
		Assert.assertEquals("Should be able to return a copy of the measurement data, so the value should be the same", "2nd",
				data.getValue());
		Assert.assertEquals("Should be able to return a copy of the measurement data, so the id should be the same", termId, data
				.getMeasurementVariable().getTermId());
	}

	@Test
	public void testUpdateExperimentCellDataIfNotDiscard() {
		int termId = 2000;
		String newValue = "new value";
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		dataList.add(this.generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName1"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<MeasurementData>();
		dataList.add(this.generateTestMeasurementData(termId, "2nd", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.observationMatrixController.setStudySelection(userSelection);
		this.observationMatrixController.setValidationService(Mockito.mock(ValidationService.class));
		Map<String, String> data = new HashMap<String, String>();
		data.put("index", "1");
		data.put("termId", Integer.toString(termId));
		data.put("value", newValue);
		data.put("isNew", "1");
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter("isDiscard")).thenReturn("0");

		Map<String, Object> results = this.observationMatrixController.updateExperimentCellData(data, req);

		@SuppressWarnings("unchecked")
		Map<String, Object> dataMap = (Map<String, Object>) results.get("data");
		Assert.assertEquals("Should have the new value already", newValue, dataMap.get("TestVarName2"));
	}

	@Test
	public void testUpdateExperimentCellDataIfDiscard() {
		int termId = 2000;
		String newValue = "new value";
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		dataList.add(this.generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<MeasurementData>();
		dataList.add(this.generateTestMeasurementData(termId, "2nd", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.observationMatrixController.setStudySelection(userSelection);
		this.observationMatrixController.setValidationService(Mockito.mock(ValidationService.class));
		Map<String, String> data = new HashMap<String, String>();
		data.put("index", "1");
		data.put("termId", Integer.toString(termId));
		data.put("value", newValue);
		data.put("isNew", "1");
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter("isDiscard")).thenReturn("1");

		Map<String, Object> results = this.observationMatrixController.updateExperimentCellData(data, req);

		@SuppressWarnings("unchecked")
		Map<String, Object> dataMap = (Map<String, Object>) results.get("data");
		Assert.assertEquals("Should have the old value since we discard the saving", "2nd", dataMap.get("TestVarName2"));
	}

	@Test
	public void testMarkExperimentCellDataAsAccepted() {
		int termId = 2000;
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName1"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.generateTestMeasurementData(termId, "2nd", TermId.CATEGORICAL_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.observationMatrixController.setStudySelection(userSelection);
		this.observationMatrixController.setValidationService(Mockito.mock(ValidationService.class));
		Map<String, String> data = new HashMap<>();

		data.put("index", "1");
		data.put("termId", Integer.toString(termId));

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		Map<String, Object> results = this.observationMatrixController.markExperimentCellDataAsAccepted(data, req);

		@SuppressWarnings("unchecked")
		Map<String, Object> dataMap = (Map<String, Object>) results.get("data");

		Assert.assertTrue("The Accepted flag should be true", (boolean) ((Object[]) dataMap.get("TestVarName2"))[1]);

	}

	@Test
	public void testMarkExperimentCellDataAsAcceptedForNumeric() {
		int termId = 2000;
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName1"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.generateTestMeasurementData(termId, "1", TermId.NUMERIC_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.observationMatrixController.setStudySelection(userSelection);
		this.observationMatrixController.setValidationService(Mockito.mock(ValidationService.class));
		Map<String, String> data = new HashMap<>();

		data.put("index", "1");
		data.put("termId", Integer.toString(termId));

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		Map<String, Object> results = this.observationMatrixController.markExperimentCellDataAsAccepted(data, req);

		@SuppressWarnings("unchecked")
		Map<String, Object> dataMap = (Map<String, Object>) results.get("data");

		Assert.assertTrue("The Accepted flag should be true", (boolean) ((Object[]) dataMap.get("TestVarName2"))[1]);

	}

	@Test
	public void testMarkAllExperimentDataAsAccepted() {
		int termId = 2000;
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName1"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.generateTestMeasurementData(termId, "2nd", TermId.CATEGORICAL_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		dataList = new ArrayList<>();
		dataList.add(this.generateTestMeasurementData(termId, "3rd", TermId.CATEGORICAL_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName3"));
		row.setDataList(dataList);
		measurementRowList.add(row);

		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		this.observationMatrixController.setStudySelection(userSelection);
		this.observationMatrixController.markAllExperimentDataAsAccepted();

		for (MeasurementRow measurementRow : userSelection.getMeasurementRowList()) {
			if (measurementRow != null && measurementRow.getMeasurementVariables() != null) {
				for (MeasurementData var : measurementRow.getDataList()) {
					if (var != null
							&& !StringUtils.isEmpty(var.getValue())
							&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
									.getMeasurementVariable().getPossibleValues().isEmpty())) {
						Assert.assertTrue(var.isAccepted());
						Assert.assertTrue(var.isCustomCategoricalValue());
					} else {
						Assert.assertFalse(var.isAccepted());
						Assert.assertFalse(var.isCustomCategoricalValue());
					}
				}
			}
		}

	}

	@Test
	public void testMarkAllExperimentDataAsMissing() {
		int termId = 2000;
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName1"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.generateTestMeasurementData(termId, "2nd", TermId.CATEGORICAL_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		dataList = new ArrayList<>();
		dataList.add(this.generateTestMeasurementData(termId, "3rd", TermId.CATEGORICAL_VARIABLE.getId(), new ArrayList<ValueReference>(),
				"TestVarName3"));
		row.setDataList(dataList);
		measurementRowList.add(row);

		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		this.observationMatrixController.setStudySelection(userSelection);
		this.observationMatrixController.markAllExperimentDataAsMissing();

		for (MeasurementRow measurementRow : userSelection.getMeasurementRowList()) {
			if (measurementRow != null && measurementRow.getMeasurementVariables() != null) {
				for (MeasurementData var : measurementRow.getDataList()) {
					if (var != null) {
						if (var != null
								&& !StringUtils.isEmpty(var.getValue())
								&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
										.getMeasurementVariable().getPossibleValues().isEmpty())) {
							Assert.assertTrue(var.isAccepted());
							if (this.observationMatrixController.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var
									.getMeasurementVariable().getPossibleValues())) {
								Assert.assertEquals(ObservationMatrixController.MISSING_VALUE, var.getValue());
							} else {
								Assert.assertFalse("0".equals(var.getValue()));
							}
						} else {
							Assert.assertFalse(var.isAccepted());
						}
					}
				}
			}
		}

	}

	@Test
	public void testIsCategoricalValueOutOfBounds() {
		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		possibleValues.add(new ValueReference());
		possibleValues.get(0).setId(1);
		possibleValues.get(0).setKey("1");
		possibleValues.get(1).setId(2);
		possibleValues.get(1).setKey("2");

		Assert.assertFalse("2 is in possible values so the return value should be false",
				this.observationMatrixController.isCategoricalValueOutOfBounds("2", "", possibleValues));
		Assert.assertTrue("3 is NOT in possible values so the return value should be true",
				this.observationMatrixController.isCategoricalValueOutOfBounds("3", "", possibleValues));
		Assert.assertFalse("2 is in possible values so the return value should be false",
				this.observationMatrixController.isCategoricalValueOutOfBounds(null, "2", possibleValues));
		Assert.assertTrue("3 is NOT in possible values so the return value should be true",
				this.observationMatrixController.isCategoricalValueOutOfBounds(null, "3", possibleValues));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsRange() {
		MeasurementVariable var = new MeasurementVariable();
		var.setMinRange(Double.valueOf("1"));
		var.setMaxRange(Double.valueOf("10"));
		Assert.assertFalse("Should return false since 2 is not out of range",
				this.observationMatrixController.isNumericalValueOutOfBounds("2", var));
		Assert.assertTrue("Should return true since 21 is out of range",
				this.observationMatrixController.isNumericalValueOutOfBounds("21", var));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsNoRange() {
		MeasurementVariable var = new MeasurementVariable();

		Assert.assertFalse("Should return false since 2 is not out of range",
				this.observationMatrixController.isNumericalValueOutOfBounds("2", var));
		Assert.assertFalse("Should return false since 21 is not out of range",
				this.observationMatrixController.isNumericalValueOutOfBounds("21", var));
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
