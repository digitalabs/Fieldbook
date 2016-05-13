package com.efficio.fieldbook.web.common.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.service.FileService;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KsuCsvWorkbookParserTest {

	private final String testTrialInstanceNo = "1";

	private final String[] rowHeaders =
			{"ENTRY_TYPE", "GID", "DESIGNATION", "ENTRY_NO", "REP_NO", "plot", "MONTH_OBS", "DAY_OBS", "GW100_g", "TGW_g"};

	@Mock
	private KsuCsvImportStudyServiceImpl ksuCsvImportStudyService;

	@Mock
	private Workbook workbook;

	@Mock
	private Map<String, MeasurementRow> rowsMap;

	@Mock
	private FileService fileService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private Map<Integer, List<String>> csvMap;

	@InjectMocks
	private KsuCsvWorkbookParser parser = spy(new KsuCsvWorkbookParser(ksuCsvImportStudyService, workbook, testTrialInstanceNo, rowsMap));

	@Before
	public void setUp() throws Exception {
		when(csvMap.get(0)).thenReturn(Arrays.asList(rowHeaders));
		when(ksuCsvImportStudyService.isValidHeaderNames(rowHeaders)).thenReturn(true);
	}

	@Test
	public void testParseCsvMap() throws Exception {
		doNothing().when(parser).importDataToWorkbook(eq(csvMap), eq(workbook), eq(testTrialInstanceNo), eq(rowsMap));

		parser.parseCsvMap(csvMap);

		verify(parser, times(1)).importDataToWorkbook(eq(csvMap), eq(workbook), eq(testTrialInstanceNo), eq(rowsMap));
	}

	@Test(expected = FileParsingException.class)
	public void testParseCsvMapInvalidHeader() throws Exception {
		when(ksuCsvImportStudyService.isValidHeaderNames(rowHeaders)).thenReturn(false);
		parser.parseCsvMap(csvMap);
	}

	@Test
	public void testGetColumnIndexesFromObservation() throws Exception {
		// setup list of measurement variables
		List<MeasurementVariable> measurementVariables = Arrays.asList(mock(MeasurementVariable.class), mock(MeasurementVariable.class));
		when(measurementVariables.get(0).getTermId()).thenReturn(TermId.PLOT_NO.getId());
		when(measurementVariables.get(1).getTermId()).thenReturn(TermId.ENTRY_NO.getId());
		when(ksuCsvImportStudyService.getLabelFromKsuRequiredColumn(any(MeasurementVariable.class))).thenCallRealMethod();

		List<Integer> indexes = parser.getColumnIndexesFromObservation(csvMap, measurementVariables, testTrialInstanceNo);

		assertTrue(indexes.size() == 3);
		assertEquals(NumberUtils.createInteger(testTrialInstanceNo), indexes.get(0));
		assertEquals("plot", rowHeaders[indexes.get(1)]);
		assertEquals("ENTRY_NO", rowHeaders[indexes.get(2)]);

	}
	
	@Test
	public void testGetRealNumericValueOfWholeNumber() {
		String value = "24";
		String expectedResult = "24";
		String actualResult = parser.getRealNumericValue(value);
		assertEquals("The expected value is " + expectedResult, expectedResult, actualResult);
	}
	
	@Test
	public void testGetRealNumericValueOfDecimalNumberWithZeroDecimals() {
		String value = "24.0";
		String expectedResult = "24";
		String actualResult = parser.getRealNumericValue(value);
		assertEquals("The expected value is " + expectedResult, expectedResult, actualResult);
	}
	
	@Test
	public void testGetRealNumericValueOfDecimalNumberWithNonZeroDecimals() {
		String value = "24.30";
		String expectedResult = "24.3";
		String actualResult = parser.getRealNumericValue(value);
		assertEquals("The expected value is " + expectedResult, expectedResult, actualResult);
	}
	
	@Test
	public void testGetRealNumericValueOfNonNumericValue() {
		String value = "notANumber";
		String expectedResult = "";
		String actualResult = parser.getRealNumericValue(value);
		assertEquals("The expected value is " + expectedResult, expectedResult, actualResult);
	}

}
