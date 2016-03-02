
package com.efficio.fieldbook.web.common.service.impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class KsuExcelImportStudyServiceImplTest {

	private KsuExcelImportStudyServiceImpl ksuExcelImportStudy;

	@Before
	public void setUp() {
		this.ksuExcelImportStudy = Mockito.spy(new KsuExcelImportStudyServiceImpl());
	}

	@Test
	public void testGetColumnHeaders() throws WorkbookParserException {
		Sheet sheet = Mockito.mock(Sheet.class);
		Row row = Mockito.mock(Row.class);
		Mockito.doReturn(row).when(sheet).getRow(0);

		int noOfColumnHeaders = 5;
		Mockito.doReturn((short) noOfColumnHeaders).when(row).getLastCellNum();

		for (int i = 0; i < noOfColumnHeaders; i++) {
			Cell cell = Mockito.mock(Cell.class);
			Mockito.doReturn(cell).when(row).getCell(i);
			Mockito.doReturn("TempValue" + i).when(cell).getStringCellValue();
		}

		String[] headerNames = this.ksuExcelImportStudy.getColumnHeaders(sheet);
		for (int i = 0; i < headerNames.length; i++) {
			Assert.assertEquals("Expecting to return TempValue" + i + " but returned " + headerNames[i], "TempValue" + i, headerNames[i]);
		}
	}

	@Test
	public void testValidateNumberOfSheets_ReturnsExceptionForInvalidNoOfSheet() {
		org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		Mockito.doReturn(2).when(xlsBook).getNumberOfSheets();

		try {
			this.ksuExcelImportStudy.validateNumberOfSheets(xlsBook);
			Assert.fail("Expecting to return an exception for invalid number of sheets but didn't.");
		} catch (WorkbookParserException e) {
			Assert.assertEquals("error.workbook.import.invalidNumberOfSheets", e.getMessage());
		}
	}

	@Test
	public void testValidateNumberOfSheets_ReturnsNoExceptionForValidNoOfSheet() {
		org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		Mockito.doReturn(1).when(xlsBook).getNumberOfSheets();

		try {
			this.ksuExcelImportStudy.validateNumberOfSheets(xlsBook);
		} catch (WorkbookParserException e) {
			Assert.fail("Expecting to return no exception for valid number of sheets but didn't.");
		}
	}

	@Test
	public void testValidate_ReturnsAnExceptionForInvalidHeaderNames() {
		try {
			org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
			Sheet observationSheet = Mockito.mock(Sheet.class);
			Mockito.doNothing().when(this.ksuExcelImportStudy).validateNumberOfSheets(xlsBook);
			Mockito.doReturn(observationSheet).when(xlsBook).getSheetAt(0);

			// invalid header names, since it lacks Designation column
			String[] headerNames = {"Plot", "Entry_no", "GID"};
			Mockito.doReturn(headerNames).when(this.ksuExcelImportStudy).getColumnHeaders(observationSheet);

			this.ksuExcelImportStudy.validate(xlsBook);
			Assert.fail("Expecting to return an exception for invalid headers but didn't.");
		} catch (WorkbookParserException e) {
			Assert.assertEquals("error.workbook.import.requiredColumnsMissing", e.getMessage());
		}
	}

	@Test
	public void testValidate_ReturnsNoExceptionForValidHeaderNames() {
		try {
			org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
			Sheet observationSheet = Mockito.mock(Sheet.class);
			Mockito.doNothing().when(this.ksuExcelImportStudy).validateNumberOfSheets(xlsBook);
			Mockito.doReturn(observationSheet).when(xlsBook).getSheetAt(0);

			String[] headerNames = {"plot", "ENTRY_NO", "GID", "DESIGNATION"};
			Mockito.doReturn(headerNames).when(this.ksuExcelImportStudy).getColumnHeaders(observationSheet);

			this.ksuExcelImportStudy.validate(xlsBook);
		} catch (WorkbookParserException e) {
			Assert.fail("Expecting to not return an exception for valid headers but didn't.");
		}
	}
}
