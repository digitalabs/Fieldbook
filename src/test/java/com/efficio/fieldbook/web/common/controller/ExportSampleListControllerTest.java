package com.efficio.fieldbook.web.common.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.common.service.CsvExportSampleListService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SampleListUtilTest;

import junit.framework.Assert;


@RunWith(MockitoJUnitRunner.class)
public class ExportSampleListControllerTest {

	private static final String STUDYNAME = "studyname";
	private static final String LIST_ID = "listId";
	private static final String LISTNAME = "listname";
	private static final String VISIBLE_COLUMNS = "visibleColumns";
	private static final String SAMPLE_STUDY_FILENAME = "Study33-SampleList";
	private static final String CSV_EXT = ".csv";
	private static final String UPLOAD_DIRECTORY = "";

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
		final List<SampleDetailsDTO> sampleDetailsDTOs = SampleListUtilTest.initSampleDetailsDTOs();
		Mockito.doReturn(sampleDetailsDTOs).when(this.sampleListService).getSampleDetailsDTOs(Matchers.anyInt());
		final String downloadFilename = ExportSampleListControllerTest.SAMPLE_STUDY_FILENAME + ExportSampleListControllerTest.CSV_EXT;
		final String outputFilename = "./someDirectory/output" + downloadFilename;
		Mockito.when(this.csvExportSampleListService.export(Matchers.any(List.class), Matchers.anyString(), Matchers.any(List.class)))
			.thenReturn(new FileExportInfo(outputFilename, downloadFilename));
		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_CSV);

		final Integer exportType = AppConstants.EXPORT_CSV.getInt();
		final Map<String, String> data = this.getData();

		final String returnedValue = this.exportSampleListController.exportSampleList(data, exportType, this.req, this.resp);

		final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.sampleListService).getSampleDetailsDTOs(Integer.valueOf(data.get(LIST_ID)));
		Mockito.verify(this.csvExportSampleListService).export(Matchers.eq(sampleDetailsDTOs), filenameCaptor.capture(),
				Matchers.eq(Arrays.asList(data.get(VISIBLE_COLUMNS).split(","))));
		Assert.assertEquals(FileUtils.sanitizeFileName(data.get(STUDYNAME) + "-" + data.get(LISTNAME)), filenameCaptor.getValue());
		
		// Verify JSON
		final Map<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);
		assertThat(true,equalTo((Boolean) result.get(exportSampleListController.IS_SUCCESS)));
		assertThat(FileUtils.MIME_CSV,equalTo(result.get(ExportSampleListController.CONTENT_TYPE)));
		assertThat(downloadFilename,equalTo(result.get(ExportSampleListController.FILENAME)));
		assertThat(outputFilename,equalTo(result.get(ExportSampleListController.OUTPUT_FILENAME)));

	}
	
	private Map<String, String> getData() {
		final Map<String, String> data = new HashMap<>();
		data.put(VISIBLE_COLUMNS, SampleListUtilTest.getColumnsExport());
		data.put(LISTNAME, "SampleList");
		data.put(LIST_ID, "35");
		data.put(STUDYNAME, "Trial33");
		return data;
	}
}
