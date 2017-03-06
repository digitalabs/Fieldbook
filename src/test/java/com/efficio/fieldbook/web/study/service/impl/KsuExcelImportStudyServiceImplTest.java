
package com.efficio.fieldbook.web.study.service.impl;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KsuExcelImportStudyServiceImplTest {

    private final static int TEST_COLUMN_HEADER_COUNT = 5;
    private final static String TEST_PROGRAM_UUID = "TEST";

	private KsuExcelImportStudyServiceImpl ksuExcelImportStudy;
    private Workbook fbWorkbook;
    private Sheet sheet;
    private Row row;
    private OntologyDataManager ontologyDataManager;

    @Before
	public void setup() {
        fbWorkbook = Mockito.mock(Workbook.class);
        ContextUtil util = Mockito.mock(ContextUtil.class);
        Mockito.doReturn(TEST_PROGRAM_UUID).when(util).getCurrentProgramUUID();
        ontologyDataManager = Mockito.mock(OntologyDataManager.class);
		this.ksuExcelImportStudy = new KsuExcelImportStudyServiceImpl(fbWorkbook, "", "");
        this.ksuExcelImportStudy.setOntologyDataManager(ontologyDataManager);

        this.ksuExcelImportStudy.setContextUtil(util);
	}

    @Test
    public void testDetectAddedTraitsNoTraitsAdded() throws WorkbookParserException{
        final Set<ChangeType> modes = new HashSet<>();
        final org.apache.poi.ss.usermodel.Workbook workbook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);

        this.setupColumnHeaderMocks();

        // here we make sure that the workbook will return the same set of header names as that mocked in our
        // "import file" when asked about the current variables recognized
        final List<MeasurementVariable> measurementVariableList = new ArrayList<>();
        for (int i=0; i < TEST_COLUMN_HEADER_COUNT; i++) {
            final MeasurementVariable mvar = new MeasurementVariable();
            mvar.setName(constructHeaderName(i));
            measurementVariableList.add(mvar);
        }

        Mockito.doReturn(sheet).when(workbook).getSheetAt(0);
        Mockito.doReturn(measurementVariableList).when(fbWorkbook).getMeasurementDatasetVariablesView();

        this.ksuExcelImportStudy.setParsedData(workbook);
        this.ksuExcelImportStudy.detectAddedTraitsAndPerformRename(modes);


        Assert.assertTrue("No change type modes should have been added if no new traits", modes.isEmpty());
    }

    @Test
    public void testDetectAddedTraitsAdditionalTraitsProvidedNotInSystem() throws WorkbookParserException{
        final Set<ChangeType> modes = new HashSet<>();
        final org.apache.poi.ss.usermodel.Workbook workbook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);

        this.setupColumnHeaderMocks();

        // to simulate a situation where there are more traits than in the workbook, we set up less values for the workbook
        final List<MeasurementVariable> measurementVariableList = new ArrayList<>();
        for (int i=0; i < TEST_COLUMN_HEADER_COUNT - 1; i++) {
            final MeasurementVariable mvar = new MeasurementVariable();
            mvar.setName(constructHeaderName(i));
            measurementVariableList.add(mvar);
        }

        Mockito.doReturn(new HashSet<StandardVariable>()).when(ontologyDataManager).
                findStandardVariablesByNameOrSynonym(Mockito.anyString(), Mockito.eq(TEST_PROGRAM_UUID));

        Mockito.doReturn(sheet).when(workbook).getSheetAt(0);
        Mockito.doReturn(measurementVariableList).when(fbWorkbook).getMeasurementDatasetVariablesView();

        this.ksuExcelImportStudy.setParsedData(workbook);
        this.ksuExcelImportStudy.detectAddedTraitsAndPerformRename(modes);


        Assert.assertFalse("Added traits should have been detected", modes.isEmpty());
    }

    protected void setupColumnHeaderMocks() {
        sheet = Mockito.mock(Sheet.class);
        row = Mockito.mock(Row.class);
        Mockito.doReturn(row).when(sheet).getRow(0);

        Mockito.doReturn((short) (TEST_COLUMN_HEADER_COUNT + 1)).when(row).getLastCellNum();

        for (int i = 0; i < TEST_COLUMN_HEADER_COUNT; i++) {
            final Cell cell = Mockito.mock(Cell.class);
            Mockito.doReturn(cell).when(row).getCell(i);
            Mockito.doReturn(constructHeaderName(i)).when(cell).getStringCellValue();
        }

        Cell cell = Mockito.mock(Cell.class);
        Mockito.doReturn(cell).when(row).getCell(TEST_COLUMN_HEADER_COUNT);
        Mockito.doReturn(KsuFieldbookUtil.PLOT).when(cell).getStringCellValue();
    }

    String constructHeaderName(final int columnNumber) {
        return "TempValue" + columnNumber;
    }

	@Test
	public void testGetColumnHeaders() throws WorkbookParserException {
		this.setupColumnHeaderMocks();

		final String[] headerNames = this.ksuExcelImportStudy.getColumnHeaders(sheet);
        // in our column header setup, last column is the plot column, with the other columns dynamically generated
		for (int i = 0; i < headerNames.length - 1; i++) {
			Assert.assertEquals("Expecting to return TempValue" + i + " but returned " + headerNames[i], "TempValue" + i, headerNames[i]);
		}

        Assert.assertEquals("Expecting plot as a column header", KsuFieldbookUtil.PLOT, headerNames[headerNames.length - 1]);
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

	@Test
	public void testValidate_ReturnsAnExceptionForInvalidHeaderNames() {
		// invalid header names, since it lacks Designation column
		final String[] headerNames = {"Plot", "Entry_no", "GID"};

		final boolean result = this.ksuExcelImportStudy.isValidHeaderNames(headerNames);
		Assert.assertFalse("Expecting to a negative result for valid header names", result);

	}

	@Test
	public void testValidate_ReturnsNoExceptionForValidHeaderNames() {
		final String[] headerNames = {"plot", "ENTRY_NO", "GID", "DESIGNATION", "PLOT_ID"};

		final boolean result = this.ksuExcelImportStudy.isValidHeaderNames(headerNames);
		Assert.assertTrue("Expecting to a positive result for valid header names", result);

	}

}
