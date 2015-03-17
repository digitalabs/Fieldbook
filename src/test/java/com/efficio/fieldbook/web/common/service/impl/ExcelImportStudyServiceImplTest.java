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

import static org.mockito.Mockito.*;

public class ExcelImportStudyServiceImplTest {
	
	private ExcelImportStudyServiceImpl importStudy;
	private String testValue = "testValue";
	private String currentValue = "currentValue";
	
	@Before
	public void setUp(){
		importStudy = spy(new ExcelImportStudyServiceImpl());
	}
	
	@Test
	public void testImportDataCellValuesWhenExcelCellIsNotNull(){
		MeasurementData wData = new MeasurementData();
		wData.setEditable(true);
		int columnIndex = 1;
		
		Row xlsRow = Mockito.mock(Row.class);
		Cell cell = Mockito.mock(Cell.class);
		Mockito.when(xlsRow.getCell(columnIndex)).thenReturn(cell);
		Mockito.when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(cell.getStringCellValue()).thenReturn(testValue);
		Workbook workbook = new Workbook();
		wData.setMeasurementVariable(null);
		importStudy.importDataCellValues(wData, xlsRow, columnIndex, workbook, new HashMap<Integer, MeasurementVariable>());
		Assert.assertEquals("MeasurementData value should be set from the cell value", wData.getValue(), cell.getStringCellValue());
	}
	@Test
	public void testImportDataCellValuesWhenExcelCellIsNull(){
		
		MeasurementData wData = new MeasurementData();
		wData.setEditable(true);
		wData.setValue(currentValue);
		int columnIndex = 1;		
		Row xlsRow = Mockito.mock(Row.class);
		Cell cell = Mockito.mock(Cell.class);
		Mockito.when(xlsRow.getCell(columnIndex)).thenReturn(null);
		Mockito.when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(cell.getStringCellValue()).thenReturn(testValue);
		Workbook workbook = new Workbook();
		wData.setMeasurementVariable(null);
		importStudy.importDataCellValues(wData, xlsRow, columnIndex, workbook, new HashMap<Integer, MeasurementVariable>());
		Assert.assertEquals("MeasurementData value should still be the same since the cell value is null", wData.getValue(), currentValue);
	}
	
	@Test
	public void testImportDataCellValuesWhenDataHasExistingValue(){
		
		MeasurementData wData = new MeasurementData();
		wData.setEditable(true);
		wData.setValue(currentValue);
		int columnIndex = 1;	
		int termId = 2;
		Row xlsRow = Mockito.mock(Row.class);
		Cell cell = Mockito.mock(Cell.class);
		Mockito.when(xlsRow.getCell(columnIndex)).thenReturn(cell);
		Mockito.when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(cell.getStringCellValue()).thenReturn(testValue);
		Workbook workbook = new Workbook();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(termId);
		wData.setMeasurementVariable(var);
		wData.setValue("test value");
		importStudy.importDataCellValues(wData, xlsRow, columnIndex, workbook, new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", workbook.hasExistingDataOverwrite());
	}
	
	@Test
	public void testImportDataCellValuesWhenDataHasExistingValueAndCellIsNumeric(){
		
		MeasurementData wData = new MeasurementData();
		wData.setEditable(true);
		wData.setValue(currentValue);
		int columnIndex = 1;	
		int termId = 2;
		Row xlsRow = Mockito.mock(Row.class);
		Cell cell = Mockito.mock(Cell.class);
		Mockito.when(xlsRow.getCell(columnIndex)).thenReturn(cell);
		Mockito.when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_NUMERIC);
		Mockito.when(cell.getNumericCellValue()).thenReturn(Double.valueOf("1.2"));
		Workbook workbook = new Workbook();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(termId);
		wData.setMeasurementVariable(var);
		wData.setValue("test value");
		importStudy.importDataCellValues(wData, xlsRow, columnIndex, workbook, new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", workbook.hasExistingDataOverwrite());
	}
	
	@Test
	public void testImportDataCellValuesWhenDataHasExistingValueAndCellIsNumericAndHasPossibleValues(){
		
		MeasurementData wData = new MeasurementData();
		wData.setEditable(true);
		wData.setValue(currentValue);
		int columnIndex = 1;	
		int termId = 2;
		Row xlsRow = Mockito.mock(Row.class);
		Cell cell = Mockito.mock(Cell.class);
		Mockito.when(xlsRow.getCell(columnIndex)).thenReturn(cell);
		Mockito.when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_NUMERIC);
		Mockito.when(cell.getNumericCellValue()).thenReturn(Double.valueOf("1.2"));
		Workbook workbook = new Workbook();
		MeasurementVariable var = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		possibleValues.add(new ValueReference(1, "1"));
		var.setPossibleValues(possibleValues);
		var.setTermId(termId);
		var.setDataTypeId( TermId.CATEGORICAL_VARIABLE.getId());
		wData.setMeasurementVariable(var);
		wData.setValue("test value");
		importStudy.importDataCellValues(wData, xlsRow, columnIndex, workbook, new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", workbook.hasExistingDataOverwrite());
	}
	
	@Test
	public void testImportDataCellValuesWhenDataHasExistingValueAndCellIsStringAndHasPossibleValues(){
		
		MeasurementData wData = new MeasurementData();
		wData.setEditable(true);
		wData.setValue(currentValue);
		int columnIndex = 1;	
		int termId = 2;
		Row xlsRow = Mockito.mock(Row.class);
		Cell cell = Mockito.mock(Cell.class);
		Mockito.when(xlsRow.getCell(columnIndex)).thenReturn(cell);
		Mockito.when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(cell.getStringCellValue()).thenReturn(testValue);
		Workbook workbook = new Workbook();
		MeasurementVariable var = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		possibleValues.add(new ValueReference(1, "1"));
		var.setPossibleValues(possibleValues);
		var.setTermId(termId);
		var.setDataTypeId( TermId.CATEGORICAL_VARIABLE.getId());
		wData.setMeasurementVariable(var);
		wData.setValue("test value");
		importStudy.importDataCellValues(wData, xlsRow, columnIndex, workbook, new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", workbook.hasExistingDataOverwrite());
	}
	
	@Test
	public void testImportDataCellValuesWhenDataHasNoExistingValue(){
		
		MeasurementData wData = new MeasurementData();
		wData.setEditable(true);
		wData.setValue(currentValue);
		int columnIndex = 1;	
		int termId = 2;
		Row xlsRow = Mockito.mock(Row.class);
		Cell cell = Mockito.mock(Cell.class);
		Mockito.when(xlsRow.getCell(columnIndex)).thenReturn(cell);
		Mockito.when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(cell.getStringCellValue()).thenReturn(testValue);
		Workbook workbook = new Workbook();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(termId);
		wData.setMeasurementVariable(var);
		wData.setValue(null);
		importStudy.importDataCellValues(wData, xlsRow, columnIndex, workbook, new HashMap<Integer, MeasurementVariable>());
		Assert.assertFalse("Workbook flag for has existing data overwrite should be false", workbook.hasExistingDataOverwrite());
	}
	
	@Test
	public void testImportDataCellValuesWhenExcelCellIsNotNullAcceptedFlagMustAlwaysBeFalse(){
		MeasurementData wData = new MeasurementData();
		wData.setEditable(true);
		int columnIndex = 1;
		
		Row xlsRow = Mockito.mock(Row.class);
		Cell cell = Mockito.mock(Cell.class);
		Mockito.when(xlsRow.getCell(columnIndex)).thenReturn(cell);
		Mockito.when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(cell.getStringCellValue()).thenReturn(testValue);
		Workbook workbook = new Workbook();
		wData.setMeasurementVariable(null);
		importStudy.importDataCellValues(wData, xlsRow, columnIndex, workbook, new HashMap<Integer, MeasurementVariable>());
		Assert.assertFalse("The Accepted Flag must be always set to false", wData.isAccepted());
	}
	
	@Test
	public void testHasCellValueIfNull(){
		boolean resp = importStudy.hasCellValue(null);
		Assert.assertFalse("Should return false since cell is null", resp);
	}
	
	@Test
	public void testIsPropertyScaleMethodLabelCellNotNull_ReturnsTrueIfAllFieldsIsNotNull(){
		Cell propertyCell = Mockito.mock(Cell.class);
		Cell scaleCell = Mockito.mock(Cell.class);
		Cell methodCell = Mockito.mock(Cell.class);
		Cell labelCell = Mockito.mock(Cell.class);
		
		Assert.assertTrue("Expecting to return true if Property,Scale,Method,Label is not null but didn't.",
				importStudy.isPropertyScaleMethodLabelCellNotNull(propertyCell, scaleCell, methodCell, labelCell));
		
	}
	
	@Test
	public void testIsPropertyScaleMethodLabelCellNotNull_ReturnsFalseIfAtLeastOneFieldIsNull(){
		Cell propertyCell = Mockito.mock(Cell.class);
		Cell scaleCell = Mockito.mock(Cell.class);
		Cell methodCell = Mockito.mock(Cell.class);
		Cell labelCell = null;
		
		Assert.assertFalse("Expecting to return false if at least 1 field from Property,Scale,Method,Label is null but didn't.",
				importStudy.isPropertyScaleMethodLabelCellNotNull(propertyCell, scaleCell, methodCell, labelCell));
		
	}
	
	@Test
	public void testIsPropertyScaleMethodLabelCellHasStringValue_ReturnsTrueIfAllFieldsHasStringValue(){
		Cell propertyCell = Mockito.mock(Cell.class);
		Cell scaleCell = Mockito.mock(Cell.class);
		Cell methodCell = Mockito.mock(Cell.class);
		Cell labelCell = Mockito.mock(Cell.class);
		
		doReturn("Property").when(propertyCell).getStringCellValue();
		doReturn("Scale").when(scaleCell).getStringCellValue();
		doReturn("Method").when(methodCell).getStringCellValue();
		doReturn("Label").when(labelCell).getStringCellValue();
		
		Assert.assertTrue("Expecting to return true if Property,Scale,Method,Label have string value but didn't.",
				importStudy.isPropertyScaleMethodLabelCellHasStringValue(propertyCell, scaleCell, methodCell, labelCell));
	}
	
	@Test
	public void testIsPropertyScaleMethodLabelCellHasStringValue_ReturnsFalseIfAtLeastOneFromFieldsHasNoStringValue(){
		Cell propertyCell = Mockito.mock(Cell.class);
		Cell scaleCell = Mockito.mock(Cell.class);
		Cell methodCell = Mockito.mock(Cell.class);
		Cell labelCell = Mockito.mock(Cell.class);
		
		doReturn("Property").when(propertyCell).getStringCellValue();
		doReturn("Scale").when(scaleCell).getStringCellValue();
		doReturn("Method").when(methodCell).getStringCellValue();
		doReturn(null).when(labelCell).getStringCellValue();
		
		Assert.assertFalse("Expecting to return false if at least one from Property,Scale,Method,Label has no string value but didn't.",
				importStudy.isPropertyScaleMethodLabelCellHasStringValue(propertyCell, scaleCell, methodCell, labelCell));
	}
	
	@Test
	public void testGetTrialInstanceNumber_ForNursery() throws WorkbookParserException{
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		
		org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		Assert.assertEquals("Expecting to return 1 for the value of trialInstance in Nursery but didn't.","1", importStudy.getTrialInstanceNumber(workbook, xlsBook));
	}
	
	@Test
	public void testGetTrialInstanceNumber_ForTrial() throws WorkbookParserException {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.T);
		
		org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		
		String toBeReturned = "2";
		doReturn(toBeReturned).when(importStudy).getTrialInstanceNumber(xlsBook);
		
		Assert.assertEquals("Expecting to return the value returned from the getTrialInstaceNumber method but didn't.",
				toBeReturned, importStudy.getTrialInstanceNumber(workbook, xlsBook));
	}
	
	@Test
	public void testGetTrialInstanceNumber_ForTrial_ReturnsExceptionForNullTrialInstance() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.T);
		
		org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		
		doReturn(null).when(importStudy).getTrialInstanceNumber(xlsBook);
		
		try {
			importStudy.getTrialInstanceNumber(workbook, xlsBook);
			Assert.fail("Expecting to return an exception when the trial instance from the xls file is null but didn't.");
		} catch (WorkbookParserException e) {
			// do nothing
		}
	}
	
	@Test
	public void testGetXlsValue_MeasurementRowIsNull(){
		MeasurementRow temp = null;
		MeasurementVariable var = new MeasurementVariable();
		MeasurementVariable origVar = new MeasurementVariable();
		MeasurementData data = new MeasurementData();
		
		String expectedValue = "tempValue";
		var.setValue(expectedValue);
		Assert.assertEquals("Expecting to return the value from var when the measurement row is null but didn't.",expectedValue,importStudy.getXlsValue(var, temp, data, origVar));
	}
	
	@Test
	public void testGetXlsValue_OrigVarPossibleValuesIsNull(){
		MeasurementRow temp = new MeasurementRow();
		MeasurementVariable var = new MeasurementVariable();
		MeasurementVariable origVar = new MeasurementVariable();
		MeasurementData data = new MeasurementData();
		
		origVar.setPossibleValues(null);
		
		String expectedValue = "tempValue";
		var.setValue(expectedValue);
		Assert.assertEquals("Expecting to return the value from var when the origVar's possible value is null but didn't.",expectedValue,importStudy.getXlsValue(var, temp, data, origVar));
	}
	
	@Test
	public void testGetXlsValue_OrigVarPossibleValuesIsEmpty(){
		MeasurementRow temp = new MeasurementRow();
		MeasurementVariable var = new MeasurementVariable();
		MeasurementVariable origVar = new MeasurementVariable();
		MeasurementData data = new MeasurementData();
		
		origVar.setPossibleValues(new ArrayList<ValueReference>());
		
		String expectedValue = "tempValue";
		var.setValue(expectedValue);
		Assert.assertEquals("Expecting to return the value from var when the origVar's possible value is empty but didn't.",expectedValue,importStudy.getXlsValue(var, temp, data, origVar));
	}
	
	@Test
	public void testGetXlsValue_ReturnsXlsValueFromCategoricalVariablePossibleValues(){
		MeasurementRow temp = new MeasurementRow();
		MeasurementVariable var = new MeasurementVariable();
		MeasurementVariable origVar = new MeasurementVariable();
		MeasurementData data = new MeasurementData();
		
		var.setValue("tempValue");
		List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		possibleValues.add(new ValueReference());
		origVar.setPossibleValues(possibleValues);
		origVar.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		
		String expectedValue = "ExpectedXlsValue";
		doReturn(expectedValue).when(importStudy).getCategoricalIdCellValue(var, origVar);
		
		Assert.assertEquals("Expecting to return the value from getCategoricalIdCellValue() but didn't.",expectedValue,importStudy.getXlsValue(var, temp, data, origVar));
	}
	
	@Test
	public void testIsMatchingPropertyScaleMethodLabel_ReturnsTrueIfAllFieldsValueAreMatched(){
		String propertyVal = "Property";
		String scaleVal = "Scale";
		String methodVal = "Method";
		String labelVal = "Label";
		
		MeasurementVariable var = new MeasurementVariable();
		var.setProperty(propertyVal);
		var.setScale(scaleVal);
		var.setMethod(methodVal);
		var.setLabel(labelVal);
		
		MeasurementVariable temp = new MeasurementVariable();
		temp.setProperty(propertyVal);
		temp.setScale(scaleVal);
		temp.setMethod(methodVal);
		temp.setLabel(labelVal);
		
		
		Assert.assertTrue("Expecting to return true if all values of property, scale, method and label of two measurement variables are the same.",
				importStudy.isMatchingPropertyScaleMethodLabel(var, temp));
	}
	
	@Test
	public void testIsMatchingPropertyScaleMethodLabel_ReturnsFalseIfAtLeast1FromFieldsValueAreNotMatched(){
		String propertyVal = "Property";
		String scaleVal = "Scale";
		String methodVal = "Method";
		String labelVal = "Label";
		
		MeasurementVariable var = new MeasurementVariable();
		var.setProperty(propertyVal);
		var.setScale(scaleVal);
		var.setMethod(methodVal);
		var.setLabel(labelVal);
		
		MeasurementVariable temp = new MeasurementVariable();
		temp.setProperty(propertyVal);
		temp.setScale(scaleVal);
		temp.setMethod(methodVal);
		temp.setLabel(labelVal + "deviation");
		
		Assert.assertFalse("Expecting to return false if at least 1 value from property, scale, method and label of two measurement variables are not the same.",
				importStudy.isMatchingPropertyScaleMethodLabel(var, temp));
	}
	
	
}
