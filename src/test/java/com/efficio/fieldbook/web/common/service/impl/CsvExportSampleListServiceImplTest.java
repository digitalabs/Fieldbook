package com.efficio.fieldbook.web.common.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.util.SampleListUtilTest;

public class CsvExportSampleListServiceImplTest {


	private static final String CSV_EXT = ".csv";

	private static CsvExportSampleListServiceImpl csvExportSampleListService;

	private static String FILENAME = "TestFileName.csv";
	private static String UPLOAD_DIRECTORY = "";

	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);

		CsvExportSampleListServiceImplTest.csvExportSampleListService = Mockito.spy(new CsvExportSampleListServiceImpl());
		Mockito.doReturn(Mockito.mock(File.class)).when(CsvExportSampleListServiceImplTest.csvExportSampleListService)
			.generateCSVFile(Matchers.any(List.class), Matchers.any(List.class), Matchers.anyString());

	}

	@Test
	public void testCSVSampleListExport() throws IOException {
		final List<SampleDetailsDTO> sampleDetailsDTOs = SampleListUtilTest.initSampleDetailsDTOs();
		final List<String> visibleColumns = SampleListUtilTest.getVisibleColumns();
		final FileExportInfo exportInfo = CsvExportSampleListServiceImplTest.csvExportSampleListService
			.export(sampleDetailsDTOs, CsvExportSampleListServiceImplTest.FILENAME, visibleColumns);

		assertThat(CsvExportSampleListServiceImplTest.CSV_EXT,
				equalTo(exportInfo.getDownloadFileName().substring(exportInfo.getDownloadFileName().lastIndexOf("."))));
	}
}
