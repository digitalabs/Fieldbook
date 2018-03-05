package com.efficio.fieldbook.web.common.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.service.api.SampleListService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.common.service.CsvExportSampleListService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SampleListUtilTest;


@RunWith(MockitoJUnitRunner.class)
public class ExportSampleListControllerTest {

	private static final String SAMPLE_TRIAL_FILENAME = "Trial33-SampleList";
	private static final String CSV_EXT = ".csv";
	private static String UPLOAD_DIRECTORY = "";

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private CsvExportSampleListService csvExportSampleListService;

	@Mock
	private SampleListService sampleListService;

	@Mock
	private HttpServletRequest req;

	@Mock
	private HttpServletResponse resp;

	@InjectMocks
	private ExportSampleListController exportSampleListController;

	@Before
	public void setUp() {
		Mockito.doReturn(ExportSampleListControllerTest.UPLOAD_DIRECTORY).when(this.fieldbookProperties).getUploadDirectory();
	}

	@Test
	public void testDoExportSampleListInCSVFormat()
		throws IOException {
		List<SampleDetailsDTO> sampleDetailsDTOs = SampleListUtilTest.initSampleDetailsDTOs();
		final String outputFilename = ExportSampleListControllerTest.SAMPLE_TRIAL_FILENAME + ExportSampleListControllerTest.CSV_EXT;
		Mockito.doReturn(sampleDetailsDTOs).when(this.sampleListService).getSampleDetailsDTOs(Matchers.anyInt());
		Mockito.when(this.csvExportSampleListService.export(Matchers.any(List.class), Matchers.anyString(), Matchers.any(List.class)))
			.thenReturn(new FileExportInfo(outputFilename, outputFilename));
		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_CSV);

		final Integer exportType = AppConstants.EXPORT_CSV.getInt();
		final Map<String, String> data = this.getData();

		final String returnedValue = this.exportSampleListController.exportSampleList(data, exportType, this.req, this.resp);
		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		assertThat(true,equalTo((Boolean) result.get(exportSampleListController.IS_SUCCESS)));
		assertThat(FileUtils.MIME_CSV,equalTo(result.get("contentType")));
		assertThat(outputFilename,equalTo(result.get("filename")));
		assertThat(outputFilename,equalTo(result.get("outputFilename")));

	}

	private Map<String, String> getData() {
		final Map<String, String> data = new HashMap<>();
		data.put("visibleColumns", SampleListUtilTest.getColumnsExport());
		data.put("listname", "SampleList");
		data.put("listId", "35");
		data.put("studyname", "Trial33");
		return data;
	}
}
