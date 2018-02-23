/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.CsvExportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.common.service.ExportAdvanceListService;
import com.efficio.fieldbook.web.common.service.KsuCsvExportStudyService;
import com.efficio.fieldbook.web.common.service.KsuExcelExportStudyService;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingRowColImpl;
import com.efficio.fieldbook.web.util.AppConstants;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class ExportStudyControllerTest {

	private static final String SAMPLE_NURSERY_FILENAME = "Sample_Nursery";
	private static final String SAMPLE_TRIAL_FILENAME = "Sample_Trial";
	private static final String ZIP_EXT = ".zip";
	private static final String CSV_EXT = ".csv";
	private static final String XLS_EXT = ".xls";

	@Mock
	private GermplasmExportService germplasmExportService;

	@Mock
	private ExportAdvanceListService exportAdvanceListService;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private CsvExportStudyService csvExportStudyService;

	@Mock
	private ExcelExportStudyService excelExportStudyService;

	@Mock
	private KsuExcelExportStudyService ksuExcelExportStudyService;

	@Mock
	private KsuCsvExportStudyService ksuCsvExportStudyService;

	@Mock
	private ExportOrderingRowColImpl exportOrderingRowColService;

	@Mock
	private HttpServletRequest req;

	@Mock
	private HttpServletResponse resp;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private ExportStudyController exportStudyController;

	private static final String CSV_CONTENT_TYPE = "text/csv";

	private UserSelection userSelection;

	@Before
	public void setUp() {
		this.userSelection = new UserSelection();
		this.exportStudyController.setUserSelection(this.userSelection);
		final PaginationListSelection paginationListSelection = Mockito.mock(PaginationListSelection.class);
		this.exportStudyController.setPaginationListSelection(paginationListSelection);
	}

	@Test
	public void testGetOutputFileNameValueChanged() {
		final String fileName = this.exportStudyController.getOutputFileName(true, "trial-test_1.xls", "trial-test.xls");
		Assert.assertEquals("Expected trial-test.xls but got trial-test_1.xls", "trial-test.xls", fileName);
	}

	@Test
	public void testGetOutputFileNameValueRetained() {
		final String fileName = this.exportStudyController.getOutputFileName(false, "trial-test_1.xls", "trial-test.xls");
		Assert.assertEquals("Expected trial-test_1.xls but got trial-test.xls", "trial-test_1.xls", fileName);
	}

	@Test
	public void testDoAdvanceExportCsvMoreThan1() throws MiddlewareQueryException, JsonProcessingException, IOException {

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1|2|3", "TempName", this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.zip"));

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		this.userSelection.setWorkbook(workbook);
		Mockito.when(this.req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1|2|3");
		Mockito.when(this.req.getParameter("exportAdvanceListGermplasmType")).thenReturn("2");

		final String ret = this.exportStudyController.doAdvanceExport(this.resp, this.req);
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> mapObject = mapper.readValue(ret, new TypeReference<Map<String, Object>>() {
		});
		final String contentType = (String) mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of zip since its more than 1 advanced list",
				FileUtils.MIME_ZIP.equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportCsvOnly1() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException {

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1", "TempName", this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		this.userSelection.setWorkbook(workbook);
		Mockito.when(this.req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1");
		Mockito.when(this.req.getParameter("exportAdvanceListGermplasmType")).thenReturn("2");
		this.exportStudyController.setExportAdvanceListService(this.exportAdvanceListService);

		final String ret = this.exportStudyController.doAdvanceExport(this.resp, this.req);
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> mapObject = mapper.readValue(ret, new TypeReference<Map<String, Object>>() {
		});
		final String contentType = (String) mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of text/csv since its just 1 advanced list",
				FileUtils.MIME_CSV.equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportXlsMoreThan1() throws MiddlewareQueryException, JsonProcessingException, IOException {

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1|2|3", "TempName", this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.zip"));

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		this.userSelection.setWorkbook(workbook);
		Mockito.when(this.req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1|2|3");
		Mockito.when(this.req.getParameter("exportAdvanceListGermplasmType")).thenReturn("1");

		final String ret = this.exportStudyController.doAdvanceExport(this.resp, this.req);
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, String> mapObject = mapper.readValue(ret, new TypeReference<Map<String, String>>() {
		});
		final String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of zip since its more than 1 advanced list",
				FileUtils.MIME_ZIP.equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportXlsOnly1() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException {

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1", "TempName", this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.xls"));

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		this.userSelection.setWorkbook(workbook);
		Mockito.when(this.req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1");
		Mockito.when(this.req.getParameter("exportAdvanceListGermplasmType")).thenReturn("1");

		final String ret = this.exportStudyController.doAdvanceExport(this.resp, this.req);
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> mapObject = mapper.readValue(ret, new TypeReference<Map<String, Object>>() {
		});
		final String contentType = (String) mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of application/vnd.ms-excel since its just 1 advanced list",
				FileUtils.MIME_MS_EXCEL.equalsIgnoreCase(contentType));
	}

	@Test
	public void testExportAdvanceListItemsInXlsExportType() {
		final StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.xls"));

		final File file = this.exportStudyController.exportAdvanceListItems("1", "1", details);
		Assert.assertTrue("Return file should not be null", file != null);
	}

	@Test
	public void testExportAdvanceListItemsInCsvExportType() {
		final StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));

		final File file =
				this.exportStudyController.exportAdvanceListItems(AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString(), "1", details);
		Assert.assertTrue("Return file should not be null", file != null);
	}

	@Test
	public void testExportAdvanceListItemsInNullExportType() {
		final StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));

		final File file = this.exportStudyController.exportAdvanceListItems("3", "1", details);
		Assert.assertTrue("Return file should be null", file == null);
	}

	@Test
	public void testDoExportNurseryInCSVFormatWithDefinedVisibleColumns()
			throws MiddlewareException, JsonParseException, JsonMappingException, IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		this.userSelection.setWorkbook(workbook);

		final String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		this.userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		final String outputFilename = ExportStudyControllerTest.SAMPLE_NURSERY_FILENAME + ExportStudyControllerTest.CSV_EXT;
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		Mockito.when(this.csvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances,
				this.getVisibleColumns())).thenReturn(outputFilename);

		Mockito.when(this.resp.getContentType()).thenReturn(ExportStudyControllerTest.CSV_CONTENT_TYPE);

		final Integer exportType = AppConstants.EXPORT_CSV.getInt();
		final Integer exportWayType = 1; // Plot Data
		final Map<String, String> data = this.getData();
		final String returnedValue = this.exportStudyController.exportFile(data, exportType, exportWayType, this.req, this.resp);
		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + ExportStudyControllerTest.CSV_CONTENT_TYPE,
				ExportStudyControllerTest.CSV_CONTENT_TYPE, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ".csv",
				generatedFilename + ExportStudyControllerTest.CSV_EXT, result.get("filename"));

		Assert.assertEquals("Expected that the returned output filename is " + outputFilename, outputFilename,
				result.get("outputFilename"));
	}

	private Map<String, String> getData() {
		final Map<String, String> data = new HashMap<String, String>();
		data.put("visibleColumns", this.getVisibleColumnsString());
		data.put("studyExportId", "0");
		return data;
	}

	private String getVisibleColumnsString() {
		return "8230,8377,8200,20368,20308";
	}

	private List<Integer> getVisibleColumns() {
		final List<Integer> visibleColumns = new ArrayList<Integer>();
		visibleColumns.add(8230);
		visibleColumns.add(8377);
		visibleColumns.add(8200);
		visibleColumns.add(20368);
		visibleColumns.add(20308);
		return visibleColumns;
	}

	@Test
	public void testDoExportTrialWith1InstanceInCSVFormat()
			throws MiddlewareException, JsonParseException, JsonMappingException, IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 1);
		this.userSelection.setWorkbook(workbook);

		final String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		this.userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		final String outputFilename = ExportStudyControllerTest.SAMPLE_TRIAL_FILENAME + ExportStudyControllerTest.CSV_EXT;
		Mockito.when(this.csvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances,
				this.getVisibleColumns())).thenReturn(outputFilename);
		Mockito.when(this.resp.getContentType()).thenReturn(ExportStudyControllerTest.CSV_CONTENT_TYPE);

		final Integer exportType = AppConstants.EXPORT_CSV.getInt();
		final Integer exportWayType = 1;
		final Map<String, String> data = this.getData();
		final String returnedValue = this.exportStudyController.exportFileTrial(data, exportType, "1", exportWayType, this.req, this.resp);
		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + ExportStudyControllerTest.CSV_CONTENT_TYPE,
				ExportStudyControllerTest.CSV_CONTENT_TYPE, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + outputFilename, outputFilename, result.get("filename"));
		Assert.assertEquals("Expected that the returned output filename is " + outputFilename, outputFilename,
				result.get("outputFilename"));
	}

	@Test
	public void testDoExportTrialWithMultipleInstancesInCSVFormat()
			throws JsonParseException, JsonMappingException, IOException, MiddlewareException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 3);
		this.userSelection.setWorkbook(workbook);

		final String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		this.userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		final String outputFilename = ExportStudyControllerTest.SAMPLE_TRIAL_FILENAME + ExportStudyControllerTest.CSV_EXT;
		Mockito.when(this.csvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances,
				this.getVisibleColumns())).thenReturn(outputFilename + ExportStudyControllerTest.ZIP_EXT);
		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_ZIP);

		final Integer exportType = AppConstants.EXPORT_CSV.getInt();
		final Integer exportWayType = 1;
		final Map<String, String> data = this.getData();
		final String returnedValue = this.exportStudyController.exportFileTrial(data, exportType, this.getTrialInstanceString(instances),
				exportWayType, this.req, this.resp);

		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + FileUtils.MIME_ZIP,
				FileUtils.MIME_ZIP, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ExportStudyControllerTest.ZIP_EXT,
				generatedFilename + ExportStudyControllerTest.ZIP_EXT, result.get("filename"));
		Assert.assertEquals("Expected that the returned output filename is " + outputFilename + ExportStudyControllerTest.ZIP_EXT,
				outputFilename + ExportStudyControllerTest.ZIP_EXT, result.get("outputFilename"));
	}

	@Test
	public void testDoExportNurseryInExcelFormat() throws MiddlewareException, JsonParseException, JsonMappingException, IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		this.userSelection.setWorkbook(workbook);

		final String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		this.userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		final String outputFilename = ExportStudyControllerTest.SAMPLE_NURSERY_FILENAME + ExportStudyControllerTest.XLS_EXT;
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		Mockito.when(this.excelExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.XLS_EXT, instances,
				this.getVisibleColumns())).thenReturn(outputFilename);

		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_MS_EXCEL);

		final Integer exportType = AppConstants.EXPORT_NURSERY_EXCEL.getInt();
		final Integer exportWayType = 1; // Plot Data
		final Map<String, String> data = this.getData();
		final String returnedValue = this.exportStudyController.exportFile(data, exportType, exportWayType, this.req, this.resp);
		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + FileUtils.MIME_MS_EXCEL,
				FileUtils.MIME_MS_EXCEL, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ExportStudyControllerTest.XLS_EXT,
				generatedFilename + ExportStudyControllerTest.XLS_EXT, result.get("filename"));
		Assert.assertEquals("Expected that the returned output filename is " + outputFilename, outputFilename,
				result.get("outputFilename"));
	}

	@Test
	public void testDoExportTrialWith1InstanceInExcelFormat()
			throws MiddlewareException, JsonParseException, JsonMappingException, IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 1);
		this.userSelection.setWorkbook(workbook);

		final String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		this.userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		final String outputFilename = ExportStudyControllerTest.SAMPLE_TRIAL_FILENAME + ExportStudyControllerTest.XLS_EXT;
		Mockito.when(this.excelExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.XLS_EXT, instances,
				this.getVisibleColumns())).thenReturn(outputFilename);
		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_ZIP);

		final Integer exportType = AppConstants.EXPORT_NURSERY_EXCEL.getInt();
		final Integer exportWayType = 1;
		final Map<String, String> data = this.getData();
		final String returnedValue = this.exportStudyController.exportFileTrial(data, exportType, "1", exportWayType, this.req, this.resp);
		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + FileUtils.MIME_ZIP,
				FileUtils.MIME_ZIP, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + outputFilename, outputFilename, result.get("filename"));
		Assert.assertEquals("Expected that the returned output filename is " + outputFilename, outputFilename,
				result.get("outputFilename"));
	}

	@Test
	public void testDoExportTrialWithMultipleInstancesInExcelFormat()
			throws JsonParseException, JsonMappingException, IOException, MiddlewareException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 3);
		this.userSelection.setWorkbook(workbook);

		final String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		this.userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		final String outputFilename = ExportStudyControllerTest.SAMPLE_TRIAL_FILENAME + ExportStudyControllerTest.ZIP_EXT;
		Mockito.when(this.excelExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.XLS_EXT, instances,
				this.getVisibleColumns())).thenReturn(outputFilename + ExportStudyControllerTest.ZIP_EXT);
		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_ZIP);

		final Integer exportType = AppConstants.EXPORT_NURSERY_EXCEL.getInt();
		final Integer exportWayType = 1;
		final Map<String, String> data = this.getData();
		final String returnedValue = this.exportStudyController.exportFileTrial(data, exportType, this.getTrialInstanceString(instances),
				exportWayType, this.req, this.resp);

		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + FileUtils.MIME_ZIP,
				FileUtils.MIME_ZIP, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ExportStudyControllerTest.ZIP_EXT,
				generatedFilename + ExportStudyControllerTest.ZIP_EXT, result.get("filename"));
		Assert.assertEquals("Expected that the returned output filename is " + outputFilename,
				outputFilename + ExportStudyControllerTest.ZIP_EXT, result.get("outputFilename"));
	}

	@Test
	public void testDoExportNurseryInKSUFieldbookCsvFormat()
			throws MiddlewareException, JsonParseException, JsonMappingException, IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		this.userSelection.setWorkbook(workbook);

		final String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		this.userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		final String outputFilename = ExportStudyControllerTest.SAMPLE_NURSERY_FILENAME + ExportStudyControllerTest.ZIP_EXT;
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		Mockito.when(this.ksuCsvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances))
				.thenReturn(outputFilename);

		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_ZIP);

		final Integer exportType = AppConstants.EXPORT_KSU_CSV.getInt();
		final Integer exportWayType = 1; // Plot Data
		final Map<String, String> data = this.getData();
		final String returnedValue = this.exportStudyController.exportFile(data, exportType, exportWayType, this.req, this.resp);
		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + FileUtils.MIME_ZIP,
				FileUtils.MIME_ZIP, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ExportStudyControllerTest.ZIP_EXT,
				generatedFilename + ExportStudyControllerTest.ZIP_EXT, result.get("filename"));
		Assert.assertEquals("Expected that the returned output filename is " + outputFilename, outputFilename,
				result.get("outputFilename"));
	}

	@Test
	public void testDoExportTrialInKSUFieldbookCsvFormat()
			throws MiddlewareException, JsonParseException, JsonMappingException, IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 3);
		this.userSelection.setWorkbook(workbook);

		final String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		this.userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		final String outputFilename = ExportStudyControllerTest.SAMPLE_TRIAL_FILENAME + ExportStudyControllerTest.ZIP_EXT;
		Mockito.when(this.ksuCsvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances))
				.thenReturn(outputFilename);
		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_ZIP);

		final Integer exportType = AppConstants.EXPORT_KSU_CSV.getInt();
		final Integer exportWayType = 1;
		final Map<String, String> data = this.getData();
		final String returnedValue = this.exportStudyController.exportFileTrial(data, exportType, this.getTrialInstanceString(instances),
				exportWayType, this.req, this.resp);
		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + FileUtils.MIME_ZIP,
				FileUtils.MIME_ZIP, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ExportStudyControllerTest.ZIP_EXT,
				generatedFilename + ExportStudyControllerTest.ZIP_EXT, result.get("filename"));
		Assert.assertEquals("Expected that the returned output filename is " + outputFilename, outputFilename,
				result.get("outputFilename"));
	}

	@Test
	public void testDoExportNurseryInKSUFieldbookExcelFormat()
			throws MiddlewareException, JsonParseException, JsonMappingException, IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		this.userSelection.setWorkbook(workbook);

		final String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		this.userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		final String outputFilename = ExportStudyControllerTest.SAMPLE_NURSERY_FILENAME + ExportStudyControllerTest.ZIP_EXT;
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		Mockito.when(this.ksuExcelExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.XLS_EXT, instances))
				.thenReturn(outputFilename);

		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_ZIP);

		final Integer exportType = AppConstants.EXPORT_KSU_EXCEL.getInt();
		final Integer exportWayType = 1; // Plot Data
		final Map<String, String> data = this.getData();
		final String returnedValue = this.exportStudyController.exportFile(data, exportType, exportWayType, this.req, this.resp);
		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + FileUtils.MIME_ZIP,
				FileUtils.MIME_ZIP, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ExportStudyControllerTest.ZIP_EXT,
				generatedFilename + ExportStudyControllerTest.ZIP_EXT, result.get("filename"));
		Assert.assertEquals("Expected that the returned output filename is " + outputFilename, outputFilename,
				result.get("outputFilename"));
	}

	@Test
	public void testDoExportTrialInKSUFieldbookExcelFormat()
			throws MiddlewareException, JsonParseException, JsonMappingException, IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 3);
		this.userSelection.setWorkbook(workbook);

		final String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		this.userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		final String outputFilename = ExportStudyControllerTest.SAMPLE_TRIAL_FILENAME + ExportStudyControllerTest.ZIP_EXT;
		Mockito.when(this.ksuExcelExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.XLS_EXT, instances))
				.thenReturn(outputFilename);
		Mockito.when(this.resp.getContentType()).thenReturn(FileUtils.MIME_ZIP);

		final Integer exportType = AppConstants.EXPORT_KSU_EXCEL.getInt();
		final Integer exportWayType = 1;
		final Map<String, String> data = this.getData();
		final String returnedValue = this.exportStudyController.exportFileTrial(data, exportType, this.getTrialInstanceString(instances),
				exportWayType, this.req, this.resp);
		final HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + FileUtils.MIME_ZIP,
				FileUtils.MIME_ZIP, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ExportStudyControllerTest.ZIP_EXT,
				generatedFilename + ExportStudyControllerTest.ZIP_EXT, result.get("filename"));
		Assert.assertEquals("Expected that the returned output filename is " + outputFilename, outputFilename,
				result.get("outputFilename"));
	}

	@Test
	public void testDoStockExport() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException {
		Mockito.when(this.exportAdvanceListService.exportStockList(1, this.germplasmExportService)).thenReturn(new File("temp.xls"));

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		this.userSelection.setWorkbook(workbook);
		Mockito.when(this.req.getParameter("exportStockListId")).thenReturn("1");

		final String ret = this.exportStudyController.doExportStockList(this.resp, this.req);
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> mapObject = mapper.readValue(ret, new TypeReference<Map<String, Object>>() {
		});
		final String contentType = (String) mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of application/vnd.ms-excel since its just 1 stock list",
				FileUtils.MIME_MS_EXCEL.equalsIgnoreCase(contentType));
	}

	private String getTrialInstanceString(final List<Integer> instances) {
		String trialInstances = "";

		for (final Integer instance : instances) {
			if ("".equalsIgnoreCase(trialInstances)) {
				trialInstances = instance.toString();
			} else {
				trialInstances = trialInstances + "|" + instance.toString();
			}
		}

		return trialInstances;
	}
}
