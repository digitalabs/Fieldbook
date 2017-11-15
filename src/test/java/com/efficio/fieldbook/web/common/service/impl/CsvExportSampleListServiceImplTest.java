package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SampleListUtilTest;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CsvExportSampleListServiceImplTest {


	private static final String CSV_EXT = ".csv";

	private static CsvExportSampleListServiceImpl csvExportSampleListService;

	private static String FILENAME = "TestFileName.csv";
	private static String UPLOAD_DIRECTORY = "";

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);

		CsvExportSampleListServiceImplTest.csvExportSampleListService = Mockito.spy(new CsvExportSampleListServiceImpl());
		CsvExportSampleListServiceImplTest.csvExportSampleListService.setFieldbookProperties(this.fieldbookProperties);
		Mockito.doReturn(Mockito.mock(File.class)).when(CsvExportSampleListServiceImplTest.csvExportSampleListService)
			.generateCSVFile(Matchers.any(List.class), Matchers.any(List.class), Matchers.anyString());
		Mockito.doReturn(CsvExportSampleListServiceImplTest.UPLOAD_DIRECTORY).when(this.fieldbookProperties).getUploadDirectory();

	}

	@Test
	public void testCSVSampleListExport() throws IOException {
		final List<SampleDetailsDTO> sampleDetailsDTOs = SampleListUtilTest.initSampleDetailsDTOs();
		final List<String> visibleColumns = SampleListUtilTest.getVisibleColumns();
		final String outputFilename = CsvExportSampleListServiceImplTest.csvExportSampleListService
			.export(sampleDetailsDTOs, CsvExportSampleListServiceImplTest.FILENAME, visibleColumns);

		assertThat(CsvExportSampleListServiceImplTest.CSV_EXT,equalTo(outputFilename.substring(outputFilename.lastIndexOf("."))));
	}
}
