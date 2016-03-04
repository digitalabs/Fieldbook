
package com.efficio.fieldbook.web.common.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.efficio.fieldbook.web.study.KsuCsvWorkbookProcessor;
import org.apache.commons.lang.math.NumberUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
	private KsuCsvWorkbookProcessor parser;

	private Workbook workbook;

	@Before
	public void setUp() throws Exception {
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook();
		this.parser = new KsuCsvWorkbookProcessor(this.workbook, this.testTrialInstanceNo, this.rowsMap);
		Mockito.when(this.csvMap.get(0)).thenReturn(Arrays.asList(this.rowHeaders));
	}

	@Test(expected = FileParsingException.class)
	public void testParseCsvMapInvalidHeader() throws Exception {
		Mockito.when(this.csvMap.get(0)).thenReturn(Arrays.asList("PLOT_NO", "GID", "DESIGNATION", "ENTRY_NO"));
		this.parser.parseCsvMap(this.csvMap);
	}

	@Test
	public void testGetColumnIndexesFromObservation() throws Exception {
		// setup list of measurement variables
		final List<MeasurementVariable> measurementVariables =
				Arrays.asList(Mockito.mock(MeasurementVariable.class), Mockito.mock(MeasurementVariable.class));
		Mockito.when(measurementVariables.get(0).getTermId()).thenReturn(TermId.PLOT_NO.getId());
		Mockito.when(measurementVariables.get(1).getTermId()).thenReturn(TermId.ENTRY_NO.getId());

		final List<Integer> indexes =
				this.parser.getColumnIndexesFromObservation(this.csvMap, measurementVariables, this.testTrialInstanceNo);

		Assert.assertTrue(indexes.size() == 3);
		Assert.assertEquals(NumberUtils.createInteger(this.testTrialInstanceNo), indexes.get(0));
		Assert.assertEquals("plot", this.rowHeaders[indexes.get(1)]);
		Assert.assertEquals("ENTRY_NO", this.rowHeaders[indexes.get(2)]);

	}

}
