package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.util.AppConstants;
import junit.framework.Assert;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.CsvExportSampleListService;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.SampleListUtilTest;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.service.api.SampleListService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class ExportSampleListControllerTest {

	private static final String STUDYNAME = "studyname";
	private static final String LIST_ID = "listId";
	private static final String LISTNAME = "listname";
	private static final String VISIBLE_COLUMNS = "visibleColumns";
	private static final String SAMPLE_STUDY_FILENAME = "Study33-SampleList";
	private static final String CSV_EXT = ".csv";
	public static final String CUSTOM_ENUMERATOR_VARIABLE_NAME = "CustomEnumeratorVariableName";

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

	@Test
	public void testDoExportSampleListInCSVFormat()
		throws IOException {
		final List<SampleDetailsDTO> sampleDetailsDTOs = SampleListUtilTest.initSampleDetailsDTOs();
		Mockito.doReturn(sampleDetailsDTOs).when(this.sampleListService).getSampleDetailsDTOs(Matchers.anyInt());
		final String downloadFilename = ExportSampleListControllerTest.SAMPLE_STUDY_FILENAME + ExportSampleListControllerTest.CSV_EXT;
		final String outputFilename = "./someDirectory/output" + downloadFilename;
		Mockito.when(this.csvExportSampleListService.export(Matchers.any(List.class), Matchers.anyString(), Matchers.any(List.class), Matchers.anyString()))
			.thenReturn(new FileExportInfo(outputFilename, downloadFilename));
		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_CSV);
		Mockito.when(this.sampleListService.getObservationVariableName(Mockito.anyInt())).thenReturn(CUSTOM_ENUMERATOR_VARIABLE_NAME);

		final Integer exportType = AppConstants.EXPORT_CSV.getInt();
		final Map<String, String> data = this.getData();

		final String returnedValue = this.exportSampleListController.exportSampleList(data, exportType, this.req, this.resp);

		final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.sampleListService).getSampleDetailsDTOs(Integer.valueOf(data.get(LIST_ID)));
		Mockito.verify(this.csvExportSampleListService).export(Matchers.eq(sampleDetailsDTOs), filenameCaptor.capture(),
				Matchers.eq(Arrays.asList(data.get(VISIBLE_COLUMNS).split(","))), Matchers.eq(CUSTOM_ENUMERATOR_VARIABLE_NAME));
		Assert.assertEquals(FileUtils.sanitizeFileName(data.get(STUDYNAME) + "-" + data.get(LISTNAME)), filenameCaptor.getValue());
		
		// Verify JSON
		final Map<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);
		assertThat(true,equalTo((Boolean) result.get(this.exportSampleListController.IS_SUCCESS)));
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
