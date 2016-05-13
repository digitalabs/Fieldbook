
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

@RunWith(MockitoJUnitRunner.class)
public class KsuCsvWorkbookParserTest {

	private final String testTrialInstanceNo = "1";

	private final String[] rowHeaders = {"ENTRY_TYPE", "GID", "DESIGNATION", "ENTRY_NO", "REP_NO", "plot", "MONTH_OBS", "DAY_OBS",
			"GW100_g", "TGW_g"};

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
	private final KsuCsvWorkbookParser parser = Mockito.spy(new KsuCsvWorkbookParser(this.ksuCsvImportStudyService, this.workbook,
			this.testTrialInstanceNo, this.rowsMap));

	@Before
	public void setUp() throws Exception {
		Mockito.when(this.csvMap.get(0)).thenReturn(Arrays.asList(this.rowHeaders));
		Mockito.when(this.ksuCsvImportStudyService.isValidHeaderNames(this.rowHeaders)).thenReturn(true);
	}

	@Test
	public void testParseCsvMap() throws Exception {
		Mockito.doNothing()
				.when(this.parser)
				.importDataToWorkbook(Matchers.eq(this.csvMap), Matchers.eq(this.workbook), Matchers.eq(this.testTrialInstanceNo),
						Matchers.eq(this.rowsMap));

		this.parser.parseCsvMap(this.csvMap);

		Mockito.verify(this.parser, Mockito.times(1)).importDataToWorkbook(Matchers.eq(this.csvMap), Matchers.eq(this.workbook),
				Matchers.eq(this.testTrialInstanceNo), Matchers.eq(this.rowsMap));
	}

	@Test(expected = FileParsingException.class)
	public void testParseCsvMapInvalidHeader() throws Exception {
		Mockito.when(this.ksuCsvImportStudyService.isValidHeaderNames(this.rowHeaders)).thenReturn(false);
		this.parser.parseCsvMap(this.csvMap);
	}

	@Test
	public void testGetColumnIndexesFromObservation() throws Exception {
		// setup list of measurement variables
		final List<MeasurementVariable> measurementVariables =
				Arrays.asList(Mockito.mock(MeasurementVariable.class), Mockito.mock(MeasurementVariable.class));
		Mockito.when(measurementVariables.get(0).getTermId()).thenReturn(TermId.PLOT_NO.getId());
		Mockito.when(measurementVariables.get(1).getTermId()).thenReturn(TermId.ENTRY_NO.getId());
		Mockito.when(this.ksuCsvImportStudyService.getLabelFromKsuRequiredColumn(Matchers.any(MeasurementVariable.class)))
				.thenCallRealMethod();

		final List<Integer> indexes =
				this.parser.getColumnIndexesFromObservation(this.csvMap, measurementVariables, this.testTrialInstanceNo);

		Assert.assertTrue(indexes.size() == 3);
		Assert.assertEquals(NumberUtils.createInteger(this.testTrialInstanceNo), indexes.get(0));
		Assert.assertEquals("plot", this.rowHeaders[indexes.get(1)]);
		Assert.assertEquals("ENTRY_NO", this.rowHeaders[indexes.get(2)]);

	}

	@Test
	public void testGetRealNumericValueOfWholeNumber() {
		final String value = "24";
		final String expectedResult = "24";
		final String actualResult = this.parser.getRealNumericValue(value);
		Assert.assertEquals("The expected value is " + expectedResult, expectedResult, actualResult);
	}

	@Test
	public void testGetRealNumericValueOfDecimalNumberWithZeroDecimals() {
		final String value = "24.0";
		final String expectedResult = "24";
		final String actualResult = this.parser.getRealNumericValue(value);
		Assert.assertEquals("The expected value is " + expectedResult, expectedResult, actualResult);
	}

	@Test
	public void testGetRealNumericValueOfDecimalNumberWithNonZeroDecimals() {
		final String value = "24.30";
		final String expectedResult = "24.3";
		final String actualResult = this.parser.getRealNumericValue(value);
		Assert.assertEquals("The expected value is " + expectedResult, expectedResult, actualResult);
	}

	@Test
	public void testGetRealNumericValueOfNonNumericValue() {
		final String value = "notANumber";
		final String expectedResult = "";
		final String actualResult = this.parser.getRealNumericValue(value);
		Assert.assertEquals("The expected value is " + expectedResult, expectedResult, actualResult);
	}

}
