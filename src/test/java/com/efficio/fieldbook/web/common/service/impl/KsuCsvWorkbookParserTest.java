package com.efficio.fieldbook.web.common.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
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

@RunWith(MockitoJUnitRunner.class)
public class KsuCsvWorkbookParserTest {

	private final String testTrialInstanceNo = "1";

	private final String[] rowHeaders =
			{"ENTRY_TYPE", "GID", "DESIGNATION", "ENTRY_NO", "REP_NO", "plot", "MONTH_OBS", "DAY_OBS", "GW100_g", "TGW_g"};

	@Mock
	private Map<String, MeasurementRow> rowsMap;

	@Mock
	private Map<Integer, List<String>> csvMap;

	@InjectMocks
	private KsuCsvWorkbookParser parser;

	private Workbook workbook;
	
	@Before
	public void setUp() throws Exception {
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook();
		this.parser = new KsuCsvWorkbookParser(workbook, testTrialInstanceNo, rowsMap);
		when(csvMap.get(0)).thenReturn(Arrays.asList(rowHeaders));
	}

	@Test(expected = FileParsingException.class)
	public void testParseCsvMapInvalidHeader() throws Exception {
		when(csvMap.get(0)).thenReturn(Arrays.asList("PLOT_NO", "GID", "DESIGNATION", "ENTRY_NO"));
		parser.parseCsvMap(csvMap);
	}

	@Test
	public void testGetColumnIndexesFromObservation() throws Exception {
		// setup list of measurement variables
		List<MeasurementVariable> measurementVariables = Arrays.asList(mock(MeasurementVariable.class), mock(MeasurementVariable.class));
		when(measurementVariables.get(0).getTermId()).thenReturn(TermId.PLOT_NO.getId());
		when(measurementVariables.get(1).getTermId()).thenReturn(TermId.ENTRY_NO.getId());

		List<Integer> indexes = parser.getColumnIndexesFromObservation(csvMap, measurementVariables, testTrialInstanceNo);

		assertTrue(indexes.size() == 3);
		assertEquals(NumberUtils.createInteger(testTrialInstanceNo), indexes.get(0));
		assertEquals("plot", rowHeaders[indexes.get(1)]);
		assertEquals("ENTRY_NO", rowHeaders[indexes.get(2)]);

	}

}
