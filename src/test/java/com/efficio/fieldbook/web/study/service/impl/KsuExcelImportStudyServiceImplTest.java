
package com.efficio.fieldbook.web.study.service.impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class KsuExcelImportStudyServiceImplTest {

	private KsuExcelImportStudyServiceImpl ksuExcelImportStudy;


    @Before
    public void setup() {
        this.ksuExcelImportStudy = new KsuExcelImportStudyServiceImpl(Mockito.mock(Workbook.class), "", "");
    }

	@Test
	public void testGetColumnHeaders() throws WorkbookParserException {
		final Sheet sheet = Mockito.mock(Sheet.class);
		final Row row = Mockito.mock(Row.class);
		Mockito.doReturn(row).when(sheet).getRow(0);

		final int noOfColumnHeaders = 5;
		Mockito.doReturn((short) noOfColumnHeaders).when(row).getLastCellNum();

		for (int i = 0; i < noOfColumnHeaders; i++) {
			final Cell cell = Mockito.mock(Cell.class);
			Mockito.doReturn(cell).when(row).getCell(i);
			Mockito.doReturn("TempValue" + i).when(cell).getStringCellValue();
		}

		final String[] headerNames = this.ksuExcelImportStudy.getColumnHeaders(sheet);
		for (int i = 0; i < headerNames.length; i++) {
			Assert.assertEquals("Expecting to return TempValue" + i + " but returned " + headerNames[i], "TempValue" + i, headerNames[i]);
		}
	}

	@Test
	public void testValidateNumberOfSheets_ReturnsExceptionForInvalidNoOfSheet() {
		final org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		Mockito.doReturn(2).when(xlsBook).getNumberOfSheets();


		try {
			this.ksuExcelImportStudy.validateNumberOfSheets(xlsBook);
			Assert.fail("Expecting to return an exception for invalid number of sheets but didn't.");
		} catch (final WorkbookParserException e) {
			Assert.assertEquals("error.workbook.import.invalidNumberOfSheets", e.getMessage());
		}
	}

	@Test
	public void testValidateNumberOfSheets_ReturnsNoExceptionForValidNoOfSheet() {
		final org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		Mockito.doReturn(1).when(xlsBook).getNumberOfSheets();
        this.ksuExcelImportStudy = new KsuExcelImportStudyServiceImpl(Mockito.mock(Workbook.class), "", "");

		try {
			this.ksuExcelImportStudy.validateNumberOfSheets(xlsBook);
		} catch (final WorkbookParserException e) {
			Assert.fail("Expecting to return no exception for valid number of sheets but didn't.");
		}
	}

/*	@Test
	public void testValidate_ReturnsAnExceptionForInvalidHeaderNames() {
		try {
			final org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
			final Sheet observationSheet = Mockito.mock(Sheet.class);
			Mockito.doReturn(observationSheet).when(xlsBook).getSheetAt(0);
            this.ksuExcelImportStudy.setParsedData(xlsBook);

			// invalid header names, since it lacks Designation column
			final String[] headerNames = {"Plot", "Entry_no", "GID"};

			this.ksuExcelImportStudy.validateObservationColumns();
			Assert.fail("Expecting to return an exception for invalid headers but didn't.");
		} catch (final WorkbookParserException e) {
			Assert.assertEquals("error.workbook.import.requiredColumnsMissing", e.getMessage());
		}
	}

	@Test
	public void testValidate_ReturnsNoExceptionForValidHeaderNames() {
		try {
			final org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
			final Sheet observationSheet = Mockito.mock(Sheet.class);
			Mockito.doReturn(observationSheet).when(xlsBook).getSheetAt(0);
            this.ksuExcelImportStudy.setParsedData(xlsBook);

			final String[] headerNames = {"plot", "ENTRY_NO", "GID", "DESIGNATION"};

			this.ksuExcelImportStudy.validateObservationColumns();
		} catch (final WorkbookParserException e) {
			Assert.fail("Expecting to not return an exception for valid headers but didn't.");
		}
	}*/
}
