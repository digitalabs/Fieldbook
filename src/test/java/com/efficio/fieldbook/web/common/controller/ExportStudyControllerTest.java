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
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingRowColImpl;
import com.efficio.fieldbook.web.util.AppConstants;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class ExportStudyControllerTest {

	private static final String SAMPLE_NURSERY_FILENAME = "Sample_Nursery";

	private static final String SAMPLE_TRIAL_FILENAME = "Sample_Trial";

	private static final String ZIP_EXT = ".zip";

	private static final String CSV_EXT = ".csv";

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
	private ExportOrderingRowColImpl exportOrderingRowColService;
	
	@Mock
	private ExcelExportStudyService excelExportStudyService;

	@Mock
	private HttpServletRequest req;

	@Mock
	private HttpServletResponse resp;
	
	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private ExportStudyController exportStudyController;

	private static final String CSV_CONTENT_TYPE = "text/csv";
	private static final String ZIP_CONTENT_TYPE = "application/zip";
	
	private UserSelection userSelection;
	
	@Before
	public void setUp() {
		this.userSelection = new UserSelection();
		this.exportStudyController.setUserSelection(userSelection);
		PaginationListSelection paginationListSelection = Mockito.mock(PaginationListSelection.class);
		exportStudyController.setPaginationListSelection(paginationListSelection);
		Mockito.doReturn(null).when(paginationListSelection).getReviewFullWorkbook("0");
	}

	@Test
	public void testGetOutputFileNameValueChanged() {
		String fileName = this.exportStudyController.getOutputFileName(true, "trial-test_1.xls", "trial-test.xls");
		Assert.assertEquals("Expected trial-test.xls but got trial-test_1.xls", "trial-test.xls", fileName);
	}

	@Test
	public void testGetOutputFileNameValueRetained() {
		String fileName = this.exportStudyController.getOutputFileName(false, "trial-test_1.xls", "trial-test.xls");
		Assert.assertEquals("Expected trial-test_1.xls but got trial-test.xls", "trial-test_1.xls", fileName);
	}

	@Test
	public void testDoAdvanceExportCsvMoreThan1() throws MiddlewareQueryException, JsonProcessingException, IOException {

		Mockito.when(
				this.exportAdvanceListService.exportAdvanceGermplasmList("1|2|3", "TempName", this.germplasmExportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.zip"));

		
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		Mockito.when(req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1|2|3");
		Mockito.when(req.getParameter("exportAdvanceListGermplasmType")).thenReturn("2");

		String ret = this.exportStudyController.doAdvanceExport(resp, req);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> mapObject = mapper.readValue(ret, new TypeReference<Map<String, Object>>() {
		});
		String contentType = (String) mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of zip since its more than 1 advanced list",
				"application/zip".equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportCsvOnly1() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException {

		Mockito.when(
				this.exportAdvanceListService.exportAdvanceGermplasmList("1", "TempName", this.germplasmExportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));

		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		Mockito.when(req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1");
		Mockito.when(req.getParameter("exportAdvanceListGermplasmType")).thenReturn("2");
		this.exportStudyController.setExportAdvanceListService(this.exportAdvanceListService);

		String ret = this.exportStudyController.doAdvanceExport(resp, req);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> mapObject = mapper.readValue(ret, new TypeReference<Map<String, Object>>() {
		});
		String contentType = (String) mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of text/csv since its just 1 advanced list", "text/csv".equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportXlsMoreThan1() throws MiddlewareQueryException, JsonProcessingException, IOException {

		Mockito.when(
				this.exportAdvanceListService.exportAdvanceGermplasmList("1|2|3", "TempName", this.germplasmExportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.zip"));

		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		Mockito.when(req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1|2|3");
		Mockito.when(req.getParameter("exportAdvanceListGermplasmType")).thenReturn("1");

		String ret = this.exportStudyController.doAdvanceExport(resp, req);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> mapObject = mapper.readValue(ret, new TypeReference<Map<String, String>>() {
		});
		String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of zip since its more than 1 advanced list",
				"application/zip".equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportXlsOnly1() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException {

		Mockito.when(
				this.exportAdvanceListService.exportAdvanceGermplasmList("1", "TempName", this.germplasmExportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.xls"));

		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		Mockito.when(req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1");
		Mockito.when(req.getParameter("exportAdvanceListGermplasmType")).thenReturn("1");

		String ret = this.exportStudyController.doAdvanceExport(resp, req);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> mapObject = mapper.readValue(ret, new TypeReference<Map<String, Object>>() {
		});
		String contentType = (String) mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of application/vnd.ms-excel since its just 1 advanced list",
				"application/vnd.ms-excel".equalsIgnoreCase(contentType));
	}

	@Test
	public void testExportAdvanceListItemsInXlsExportType() {
		StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(
				this.exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), this.germplasmExportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.xls"));

		File file = this.exportStudyController.exportAdvanceListItems("1", "1", details);
		Assert.assertTrue("Return file should not be null", file != null);
	}

	@Test
	public void testExportAdvanceListItemsInCsvExportType() {
		StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(
				this.exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), this.germplasmExportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));

		File file = this.exportStudyController.exportAdvanceListItems(AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString(), "1", details);
		Assert.assertTrue("Return file should not be null", file != null);
	}

	@Test
	public void testExportAdvanceListItemsInNullExportType() {
		StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(
				this.exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), this.germplasmExportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));

		File file = this.exportStudyController.exportAdvanceListItems("3", "1", details);
		Assert.assertTrue("Return file should be null", file == null);
	}


	@Test
	public void testDoExportNurseryInCSVFormatWithDefinedVisibleColumns() throws MiddlewareException, JsonParseException,
	JsonMappingException, IOException {

		// Inputs
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);

		String outputFilename = ExportStudyControllerTest.SAMPLE_NURSERY_FILENAME;
		String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		Integer exportType = AppConstants.EXPORT_CSV.getInt();
		Integer exportWayType = 1; // Plot Data
		Map<String, String> data = this.getData();

		// Mock Object Method Calls
		userSelection.setWorkbook(workbook);
		userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);
		
		Mockito.when(this.resp.getContentType()).thenReturn(ExportStudyControllerTest.CSV_CONTENT_TYPE);
		Mockito.when(
				this.csvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances,
						this.getVisibleColumns())).thenReturn(outputFilename + ExportStudyControllerTest.CSV_EXT);
		
		String returnedValue = this.exportStudyController.exportFile(data, exportType, exportWayType, this.req, this.resp);

		HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + ExportStudyControllerTest.CSV_CONTENT_TYPE + " but returned "
				+ result.get("contentType"), ExportStudyControllerTest.CSV_CONTENT_TYPE, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ".csv but returned " + result.get("filename"),
				generatedFilename + ExportStudyControllerTest.CSV_EXT, result.get("filename"));
		Assert.assertEquals(
				"Expected that the returned output filename is " + outputFilename + ".csv but returned " + result.get("outputFilename"),
				outputFilename + ExportStudyControllerTest.CSV_EXT, result.get("outputFilename"));
	}
	
	private Map<String, String> getData() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("visibleColumns", this.getVisibleColumnsString());
		data.put("studyExportId", "0");
		return data;
	}

	private String getVisibleColumnsString() {
		return "8230,8377,8200,20368,20308";
	}

	private List<Integer> getVisibleColumns() {
		List<Integer> visibleColumns = new ArrayList<Integer>();
		visibleColumns.add(8230);
		visibleColumns.add(8377);
		visibleColumns.add(8200);
		visibleColumns.add(20368);
		visibleColumns.add(20308);
		return visibleColumns;
	}

	@Test
	public void testDoExportTrialWith1InstanceInCSVFormat() throws MiddlewareException, JsonParseException, JsonMappingException,
	IOException {

		// Inputs
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 1);

		String outputFilename = ExportStudyControllerTest.SAMPLE_TRIAL_FILENAME;
		String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		Integer exportType = AppConstants.EXPORT_CSV.getInt();
		Integer exportWayType = 1;
		Map<String, String> data = this.getData();

		// Mock Object Method Calls
		userSelection.setWorkbook(workbook);
		userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);
		
		Mockito.when(
				this.csvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances,
						this.getVisibleColumns())).thenReturn(outputFilename + ExportStudyControllerTest.CSV_EXT);
		Mockito.when(this.resp.getContentType()).thenReturn(ExportStudyControllerTest.CSV_CONTENT_TYPE);

		String returnedValue = this.exportStudyController.exportFileTrial(data, exportType, "1", exportWayType, this.req, this.resp);
		HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);
		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + ExportStudyControllerTest.CSV_CONTENT_TYPE, ExportStudyControllerTest.CSV_CONTENT_TYPE, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + outputFilename + ".csv",
				outputFilename + ExportStudyControllerTest.CSV_EXT, result.get("filename"));
		Assert.assertEquals(
				"Expected that the returned output filename is " + outputFilename + ".csv",
				outputFilename + ExportStudyControllerTest.CSV_EXT, result.get("outputFilename"));
	}

	@Test
	public void testDoExportTrialWithMultipleInstancesInCSVFormat() throws JsonParseException,
	JsonMappingException, IOException, MiddlewareException {

		// Inputs
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 3);

		String outputFilename = ExportStudyControllerTest.SAMPLE_TRIAL_FILENAME;
		String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);

		Integer exportType = AppConstants.EXPORT_CSV.getInt();
		Integer exportWayType = 1;
		Map<String, String> data = this.getData();

		// Mock Object Method Calls
		userSelection.setWorkbook(workbook);
		userSelection.getWorkbook().getStudyDetails().setStudyName(generatedFilename);

		Mockito.when(
				this.csvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances,
						this.getVisibleColumns())).thenReturn(outputFilename + ExportStudyControllerTest.ZIP_EXT);
		Mockito.when(this.resp.getContentType()).thenReturn(ExportStudyControllerTest.ZIP_CONTENT_TYPE);
		
		String returnedValue =
				this.exportStudyController.exportFileTrial(data, exportType, this.getTrialInstanceString(instances), exportWayType,
						this.req, this.resp);
		HashMap<String, Object> result = new ObjectMapper().readValue(returnedValue, HashMap.class);
		Assert.assertTrue("Unable to properly generate export", (Boolean) result.get(ExportStudyController.IS_SUCCESS));
		Assert.assertEquals("Expected that the returned content type is " + ExportStudyControllerTest.ZIP_CONTENT_TYPE, ExportStudyControllerTest.ZIP_CONTENT_TYPE, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ".zip",
				generatedFilename + ExportStudyControllerTest.ZIP_EXT, result.get("filename"));
		Assert.assertEquals(
				"Expected that the returned output filename is " + outputFilename + ".zip",
				outputFilename + ExportStudyControllerTest.ZIP_EXT, result.get("outputFilename"));
	}

	@Test
	public void testDoStockExport() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException {
		Mockito.when(this.exportAdvanceListService.exportStockList(1, this.germplasmExportService)).thenReturn(new File("temp.xls"));

		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		Mockito.when(req.getParameter("exportStockListId")).thenReturn("1");

		String ret = this.exportStudyController.doExportStockList(resp, req);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> mapObject = mapper.readValue(ret, new TypeReference<Map<String, Object>>() {
		});
		String contentType = (String) mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of application/vnd.ms-excel since its just 1 stock list",
				"application/vnd.ms-excel".equalsIgnoreCase(contentType));
	}


	private String getTrialInstanceString(List<Integer> instances) {
		String trialInstances = "";

		for (Integer instance : instances) {
			if ("".equalsIgnoreCase(trialInstances)) {
				trialInstances = instance.toString();
			} else {
				trialInstances = trialInstances + "|" + instance.toString();
			}
		}

		return trialInstances;
	}
}
