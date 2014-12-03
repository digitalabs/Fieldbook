package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ExcelImportStudyServiceImplTest {
	
	private ExcelImportStudyServiceImpl importStudy;
	private String testValue = "testValue";
	private String currentValue = "currentValue";
	
	@Before
	public void setUp(){
		importStudy = new ExcelImportStudyServiceImpl();
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
}
