
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;

public class ExcelImportStudyServiceImplTest {

	private static final String LABEL = "Label";
	private static final String METHOD = "Method";
	private static final String SCALE = "Scale";
	private static final String PROPERTY = "Property";
	private ExcelImportStudyServiceImpl importStudy;
	private final String testValue = "testValue";
	private final String currentValue = "currentValue";

	private MeasurementData wData;
	private int columnIndex;
	private int termId;
	private Row xlsRow;
	private Cell cell;
	private Workbook workbook;

	private Cell propertyCell;
	private Cell scaleCell;
	private Cell methodCell;
	private Cell labelCell;

	@Before
	public void setUp() {
		this.importStudy = Mockito.spy(new ExcelImportStudyServiceImpl());

		this.initTestVariables();
	}

	private void initTestVariables() {
		this.wData = this.createMeasurementData();
		this.columnIndex = 1;
		this.termId = 2;

		this.xlsRow = Mockito.mock(Row.class);
		this.cell = Mockito.mock(Cell.class);
		this.workbook = new Workbook();

		this.propertyCell = Mockito.mock(Cell.class);
		this.scaleCell = Mockito.mock(Cell.class);
		this.methodCell = Mockito.mock(Cell.class);
		this.labelCell = Mockito.mock(Cell.class);
	}

	@Test
	public void testImportDataCellValuesWhenExcelCellIsNotNull() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		this.wData.setMeasurementVariable(null);

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertEquals("MeasurementData value should be set from the cell value", this.wData.getValue(),
				this.cell.getStringCellValue());
	}

	@Test
	public void testImportDataCellValuesWhenExcelCellIsNull() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(null);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		this.wData.setMeasurementVariable(null);

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertEquals("MeasurementData value should still be the same since the cell value is null", this.wData.getValue(),
				this.currentValue);
	}

	@Test
	public void testImportDataCellValuesWhenDataHasExistingValue() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		final MeasurementVariable var = this.createMeasurementVariable(TermId.NUMERIC_VARIABLE.getId());

		this.wData.setMeasurementVariable(var);
		this.wData.setValue("test value");

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", this.workbook.hasExistingDataOverwrite());
	}

	@Test
	public void testImportDataCellValuesWhenDataHasExistingValueAndCellIsNumeric() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_NUMERIC);
		Mockito.when(this.cell.getNumericCellValue()).thenReturn(Double.valueOf("1.2"));

		final MeasurementVariable var = this.createMeasurementVariable(TermId.NUMERIC_VARIABLE.getId());

		this.wData.setMeasurementVariable(var);
		this.wData.setValue("test value");

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", this.workbook.hasExistingDataOverwrite());
	}

	@Test
	public void testImportDataCellValuesWhenDataHasExistingValueAndCellIsNumericAndHasPossibleValues() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_NUMERIC);
		Mockito.when(this.cell.getNumericCellValue()).thenReturn(Double.valueOf("1.2"));

		final MeasurementVariable var = this.createMeasurementVariable(TermId.CATEGORICAL_VARIABLE.getId());

		this.wData.setMeasurementVariable(var);
		this.wData.setValue("test value");

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", this.workbook.hasExistingDataOverwrite());
	}

	@Test
	public void testImportDataCellValuesWhenDataHasExistingValueAndCellIsStringAndHasPossibleValues() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		final MeasurementVariable var = this.createMeasurementVariable(TermId.CATEGORICAL_VARIABLE.getId());

		this.wData.setMeasurementVariable(var);
		this.wData.setValue("test value");

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", this.workbook.hasExistingDataOverwrite());
	}

	@Test
	public void testImportDataCellValuesWhenDataHasNoExistingValue() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		this.wData.setValue(null);

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertFalse("Workbook flag for has existing data overwrite should be false", this.workbook.hasExistingDataOverwrite());
	}

	@Test
	public void testImportDataCellValuesWhenExcelCellIsNotNullAcceptedFlagMustAlwaysBeFalse() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		this.wData.setMeasurementVariable(null);

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertFalse("The Accepted Flag must be always set to false", this.wData.isAccepted());
	}

	@Test
	public void testHasCellValueIfNull() {
		final boolean resp = this.importStudy.hasCellValue(null);
		Assert.assertFalse("Should return false since cell is null", resp);
	}

	@Test
	public void testIsPropertyScaleMethodLabelCellNotNull_ReturnsTrueIfAllFieldsIsNotNull() {
		Assert.assertTrue("Expecting to return true if Property,Scale,Method,Label is not null but didn't.",
				this.importStudy.isPropertyScaleMethodLabelCellNotNull(this.propertyCell, this.scaleCell, this.methodCell, this.labelCell));
	}

	@Test
	public void testIsPropertyScaleMethodLabelCellNotNull_ReturnsFalseIfAtLeastOneFieldIsNull() {
		this.labelCell = null;
		Assert.assertFalse("Expecting to return false if at least 1 field from Property,Scale,Method,Label is null but didn't.",
				this.importStudy.isPropertyScaleMethodLabelCellNotNull(this.propertyCell, this.scaleCell, this.methodCell, this.labelCell));
	}

	@Test
	public void testIsPropertyScaleMethodLabelCellHasStringValue_ReturnsTrueIfAllFieldsHasStringValue() {
		Mockito.doReturn(PROPERTY).when(this.propertyCell).getStringCellValue();
		Mockito.doReturn(SCALE).when(this.scaleCell).getStringCellValue();
		Mockito.doReturn(METHOD).when(this.methodCell).getStringCellValue();
		Mockito.doReturn(LABEL).when(this.labelCell).getStringCellValue();

		Assert.assertTrue("Expecting to return true if Property,Scale,Method,Label have string value but didn't.", this.importStudy
				.isPropertyScaleMethodLabelCellHasStringValue(this.propertyCell, this.scaleCell, this.methodCell, this.labelCell));
	}

	@Test
	public void testIsPropertyScaleMethodLabelCellHasStringValue_ReturnsFalseIfAtLeastOneFromFieldsHasNoStringValue() {
		Mockito.doReturn(PROPERTY).when(this.propertyCell).getStringCellValue();
		Mockito.doReturn(SCALE).when(this.scaleCell).getStringCellValue();
		Mockito.doReturn(METHOD).when(this.methodCell).getStringCellValue();
		Mockito.doReturn(null).when(this.labelCell).getStringCellValue();

		Assert.assertFalse("Expecting to return false if at least one from Property,Scale,Method,Label has no string value but didn't.",
				this.importStudy.isPropertyScaleMethodLabelCellHasStringValue(this.propertyCell, this.scaleCell, this.methodCell,
						this.labelCell));
	}

	@Test
	public void testGetTrialInstanceNumber_ForNursery() throws WorkbookParserException {
		WorkbookDataUtil.setTestWorkbook(null);
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);

		final org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		Assert.assertEquals("Expecting to return 1 for the value of trialInstance in Nursery but didn't.", "1",
				this.importStudy.getTrialInstanceNumber(workbook, xlsBook));
	}

	@Test
	public void testGetTrialInstanceNumber_ForTrial() throws WorkbookParserException {
		WorkbookDataUtil.setTestWorkbook(null);
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.T);

		final org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);

		final String toBeReturned = "2";
		Mockito.doReturn(toBeReturned).when(this.importStudy).getTrialInstanceNumber(xlsBook);

		Assert.assertEquals("Expecting to return the value returned from the getTrialInstaceNumber method but didn't.", toBeReturned,
				this.importStudy.getTrialInstanceNumber(workbook, xlsBook));
	}

	@Test
	public void testGetTrialInstanceNumber_ForTrial_ReturnsExceptionForNullTrialInstance() {
		WorkbookDataUtil.setTestWorkbook(null);
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.T);

		final org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);

		Mockito.doReturn(null).when(this.importStudy).getTrialInstanceNumber(xlsBook);

		try {
			this.importStudy.getTrialInstanceNumber(workbook, xlsBook);
			Assert.fail("Expecting to return an exception when the trial instance from the xls file is null but didn't.");
		} catch (final WorkbookParserException e) {
			// do nothing
		}
	}

	@Test
	public void testGetXlsValue_MeasurementRowIsNull() {
		final MeasurementRow temp = null;
		final MeasurementVariable var = new MeasurementVariable();
		final MeasurementVariable origVar = new MeasurementVariable();
		final MeasurementData data = new MeasurementData();

		final String expectedValue = "tempValue";
		var.setValue(expectedValue);
		Assert.assertEquals("Expecting to return the value from var when the measurement row is null but didn't.", expectedValue,
				this.importStudy.getXlsValue(var, temp, data, origVar));
	}

	@Test
	public void testGetXlsValue_OrigVarPossibleValuesIsNull() {
		final MeasurementRow temp = new MeasurementRow();
		final MeasurementVariable var = new MeasurementVariable();
		final MeasurementVariable origVar = new MeasurementVariable();
		final MeasurementData data = new MeasurementData();

		origVar.setPossibleValues(null);

		final String expectedValue = "tempValue";
		var.setValue(expectedValue);
		Assert.assertEquals("Expecting to return the value from var when the origVar's possible value is null but didn't.", expectedValue,
				this.importStudy.getXlsValue(var, temp, data, origVar));
	}

	@Test
	public void testGetXlsValue_OrigVarPossibleValuesIsEmpty() {
		final MeasurementRow temp = new MeasurementRow();
		final MeasurementVariable var = new MeasurementVariable();
		final MeasurementVariable origVar = new MeasurementVariable();
		final MeasurementData data = new MeasurementData();

		origVar.setPossibleValues(new ArrayList<ValueReference>());

		final String expectedValue = "tempValue";
		var.setValue(expectedValue);
		Assert.assertEquals("Expecting to return the value from var when the origVar's possible value is empty but didn't.", expectedValue,
				this.importStudy.getXlsValue(var, temp, data, origVar));
	}

	@Test
	public void testGetXlsValue_ReturnsXlsValueFromCategoricalVariablePossibleValues() {
		final MeasurementRow temp = new MeasurementRow();
		final MeasurementVariable var = new MeasurementVariable();
		final MeasurementVariable origVar = new MeasurementVariable();
		final MeasurementData data = new MeasurementData();

		var.setValue("tempValue");
		final List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		possibleValues.add(new ValueReference());
		origVar.setPossibleValues(possibleValues);
		origVar.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());

		final String expectedValue = "ExpectedXlsValue";
		Mockito.doReturn(expectedValue).when(this.importStudy).getCategoricalIdCellValue(var, origVar);

		Assert.assertEquals("Expecting to return the value from getCategoricalIdCellValue() but didn't.", expectedValue,
				this.importStudy.getXlsValue(var, temp, data, origVar));
	}

	@Test
	public void testIsMatchingPropertyScaleMethodLabel_ReturnsTrueIfAllFieldsValueAreMatched() {

		final MeasurementVariable var = this.createMeasurementVariable(TermId.NUMERIC_VARIABLE.getId());
		final MeasurementVariable temp = this.createMeasurementVariable(TermId.NUMERIC_VARIABLE.getId());
		Assert.assertTrue(
				"Expecting to return true if all values of property, scale, method and label of two measurement variables are the same.",
				this.importStudy.isMatchingPropertyScaleMethodLabel(var, temp));
	}

	@Test
	public void testIsMatchingPropertyScaleMethodLabel_ReturnsFalseIfAtLeast1FromFieldsValueAreNotMatched() {

		final MeasurementVariable var = this.createMeasurementVariable(TermId.NUMERIC_VARIABLE.getId());
		final MeasurementVariable temp = this.createMeasurementVariable(TermId.NUMERIC_VARIABLE.getId());
		temp.setLabel(temp.getLabel() + "deviation");

		Assert.assertFalse(
				"Expecting to return false if at least 1 value from property, scale, method and label of two measurement variables are not the same.",
				this.importStudy.isMatchingPropertyScaleMethodLabel(var, temp));
	}

	private MeasurementVariable createMeasurementVariable(final int dataTypeId) {

		final MeasurementVariable var = new MeasurementVariable();
		var.setProperty(PROPERTY);
		var.setScale(SCALE);
		var.setMethod(METHOD);
		var.setLabel(LABEL);
		var.setTermId(this.termId);
		var.setDataTypeId(dataTypeId);

		if (TermId.CATEGORICAL_VARIABLE.getId() == dataTypeId) {
			final List<ValueReference> possibleValues = new ArrayList<ValueReference>();
			possibleValues.add(new ValueReference(1, "1"));
			var.setPossibleValues(possibleValues);
		}

		return var;
	}

	private MeasurementData createMeasurementData() {
		final MeasurementData wData = new MeasurementData();
		wData.setEditable(true);
		wData.setValue(this.currentValue);

		return wData;
	}

}
