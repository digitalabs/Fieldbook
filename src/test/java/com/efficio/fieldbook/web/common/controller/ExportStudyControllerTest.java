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

import junit.framework.Assert;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.service.ExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.CsvExportStudyService;
import com.efficio.fieldbook.web.common.service.ExportAdvanceListService;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingRowColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverRangeImpl;
import com.efficio.fieldbook.web.util.AppConstants;

public class ExportStudyControllerTest {

	private static final String SAMPLE_NURSERY_FILENAME = "Sample_Nursery";

	private static final String SAMPLE_TRIAL_FILENAME = "Sample_Trial";

	private static final String ZIP_EXT = ".zip";

	private static final String CSV_EXT = ".csv";

	private ExportStudyController exportStudyController;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private CsvExportStudyService csvExportStudyService;

	@Mock
	private ExportOrderingRowColImpl exportOrderingRowColService;

	@Mock
	private HttpServletRequest req;

	@Mock
	private HttpServletResponse resp;

	private static final String CSV_CONTENT_TYPE = "text/csv";
	private static final String ZIP_CONTENT_TYPE = "application/zip";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.exportStudyController = new ExportStudyController();
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
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		Mockito.when(
				exportAdvanceListService.exportAdvanceGermplasmList("1|2|3", "TempName", exportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.zip"));

		UserSelection userSelection = new UserSelection();
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		Mockito.doReturn(userSelection).when(exportStudyControllerMock).getUserSelection();
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1|2|3");
		Mockito.when(req.getParameter("exportAdvanceListGermplasmType")).thenReturn("2");
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);

		String ret = exportStudyControllerMock.doAdvanceExport(resp, req);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> mapObject = mapper.readValue(ret, new TypeReference<Map<String, String>>() {
		});
		String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of zip since its more than 1 advanced list",
				"application/zip".equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportCsvOnly1() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		Mockito.when(
				exportAdvanceListService.exportAdvanceGermplasmList("1", "TempName", exportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));

		UserSelection userSelection = new UserSelection();
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		Mockito.doReturn(userSelection).when(exportStudyControllerMock).getUserSelection();
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1");
		Mockito.when(req.getParameter("exportAdvanceListGermplasmType")).thenReturn("2");
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);

		String ret = exportStudyControllerMock.doAdvanceExport(resp, req);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> mapObject = mapper.readValue(ret, new TypeReference<Map<String, String>>() {
		});
		String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of text/csv since its just 1 advanced list", "text/csv".equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportXlsMoreThan1() throws MiddlewareQueryException, JsonProcessingException, IOException {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		Mockito.when(
				exportAdvanceListService.exportAdvanceGermplasmList("1|2|3", "TempName", exportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.zip"));

		UserSelection userSelection = new UserSelection();
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		Mockito.doReturn(userSelection).when(exportStudyControllerMock).getUserSelection();
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1|2|3");
		Mockito.when(req.getParameter("exportAdvanceListGermplasmType")).thenReturn("1");
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);

		String ret = exportStudyControllerMock.doAdvanceExport(resp, req);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> mapObject = mapper.readValue(ret, new TypeReference<Map<String, String>>() {
		});
		String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of zip since its more than 1 advanced list",
				"application/zip".equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportXlsOnly1() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		Mockito.when(
				exportAdvanceListService.exportAdvanceGermplasmList("1", "TempName", exportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.xls"));

		UserSelection userSelection = new UserSelection();
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		Mockito.doReturn(userSelection).when(exportStudyControllerMock).getUserSelection();
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter("exportAdvanceListGermplasmIds")).thenReturn("1");
		Mockito.when(req.getParameter("exportAdvanceListGermplasmType")).thenReturn("1");
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);

		String ret = exportStudyControllerMock.doAdvanceExport(resp, req);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> mapObject = mapper.readValue(ret, new TypeReference<Map<String, String>>() {
		});
		String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of application/vnd.ms-excel since its just 1 advanced list",
				"application/vnd.ms-excel".equalsIgnoreCase(contentType));
	}

	@Test
	public void testExportAdvanceListItemsInXlsExportType() {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);
		StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(
				exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), exportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.xls"));

		File file = exportStudyControllerMock.exportAdvanceListItems("1", "1", details);
		Assert.assertTrue("Return file should not be null", file != null);
	}

	@Test
	public void testExportAdvanceListItemsInCsvExportType() {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);
		StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(
				exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), exportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));

		File file = exportStudyControllerMock.exportAdvanceListItems("2", "1", details);
		Assert.assertTrue("Return file should not be null", file != null);
	}

	@Test
	public void testExportAdvanceListItemsInNullExportType() {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);
		StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(
				exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), exportService,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));

		File file = exportStudyControllerMock.exportAdvanceListItems("3", "1", details);
		Assert.assertTrue("Return file should be null", file == null);
	}

	@Test
	public void testGetExportServiceImpl() {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		Assert.assertTrue("Should return export service class", exportStudyControllerMock.getExportServiceImpl() instanceof ExportService);
	}

	@Test
	public void testExportGetUserSelection() {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		exportStudyControllerMock.setUserSelection(new UserSelection());
		Assert.assertTrue("Should return a class type userSelect", exportStudyControllerMock.getUserSelection() instanceof UserSelection);
	}

	@Test
	public void testGetExportOrderServiceRow() {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		exportStudyControllerMock.setExportOrderingRowColService(Mockito.mock(ExportOrderingRowColImpl.class));
		Assert.assertTrue("Should return ExportOrderingRowColImpl type",
				exportStudyControllerMock.getExportOrderService(1) instanceof ExportOrderingRowColImpl);
	}

	@Test
	public void testGetExportOrderServiceSerpentineOverRow() {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		exportStudyControllerMock.setExportOrderingSerpentineOverRangeService(Mockito.mock(ExportOrderingSerpentineOverRangeImpl.class));
		Assert.assertTrue("Should return ExportOrderingSerpentineOverRangeImpl type",
				exportStudyControllerMock.getExportOrderService(2) instanceof ExportOrderingSerpentineOverRangeImpl);
	}

	@Test
	public void testGetExportOrderServiceSerpentineOverColumn() {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		exportStudyControllerMock.setExportOrderingSerpentineOverColumnService(Mockito.mock(ExportOrderingSerpentineOverColImpl.class));
		Assert.assertTrue("Should return exportOrderingSerpentineOverColumnService type",
				exportStudyControllerMock.getExportOrderService(3) instanceof ExportOrderingSerpentineOverColImpl);

	}

	@Test
	public void testGetExportOrderServiceWhenTypeIs4() {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		exportStudyControllerMock.setExportOrderingRowColService(Mockito.mock(ExportOrderingRowColImpl.class));
		Assert.assertTrue("Should return ExportOrderingRowColImpl type",
				exportStudyControllerMock.getExportOrderService(4) instanceof ExportOrderingRowColImpl);
	}

	@Test
	public void testGetVisibleColumnsWhenThereIsNoVisibleColumns() {
		Assert.assertNull("Expected to return a null object for visible columns but didn't.",
				this.exportStudyController.getVisibleColumns(""));
	}

	@Test
	public void testGetVisibleColumnsWhenThereIsVisibleColumns() {
		Assert.assertEquals("Expected to return a list with 3 visible column entries.", 3,
				this.exportStudyController.getVisibleColumns("8810,8023,8024").size());
	}

	@Test
	public void testDoExportNurseryInCSVFormatWithDefinedVisibleColumns() throws MiddlewareException, JsonParseException,
			JsonMappingException, IOException {

		ExportStudyController exportStudyControllerMock = this.initializeExportStudyControllerForStudyCSVExport();

		// Inputs
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);

		String outputFilename = ExportStudyControllerTest.SAMPLE_NURSERY_FILENAME;
		String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		List<Integer> instances = WorkbookDataUtil.getTrialInstances();
		Integer exportType = AppConstants.EXPORT_CSV.getInt();
		Integer exportWayType = 1; // Plot Data
		Map<String, String> data = this.getData();

		// Mock Object Method Calls
		UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(workbook);
		exportStudyControllerMock.setUserSelection(userSelection);

		this.mockOtherRelatedMethodCallsForExportStudyMethods(exportStudyControllerMock, generatedFilename, userSelection);

		Mockito.when(this.resp.getContentType()).thenReturn(ExportStudyControllerTest.CSV_CONTENT_TYPE);
		Mockito.when(
				this.csvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances,
						this.getVisibleColumns())).thenReturn(outputFilename + ExportStudyControllerTest.CSV_EXT);

		String returnedValue = exportStudyControllerMock.exportFile(data, exportType, exportWayType, this.req, this.resp);

		HashMap<String, String> result = new ObjectMapper().readValue(returnedValue, HashMap.class);

		Assert.assertEquals("Expected that the returned content type is " + ExportStudyControllerTest.CSV_CONTENT_TYPE + " but returned "
				+ result.get("contentType"), ExportStudyControllerTest.CSV_CONTENT_TYPE, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ".csv but returned " + result.get("filename"),
				generatedFilename + ExportStudyControllerTest.CSV_EXT, result.get("filename"));
		Assert.assertEquals(
				"Expected that the returned output filename is " + outputFilename + ".csv but returned " + result.get("outputFilename"),
				outputFilename + ExportStudyControllerTest.CSV_EXT, result.get("outputFilename"));
	}

	private void mockOtherRelatedMethodCallsForExportStudyMethods(ExportStudyController exportStudyControllerMock,
			String generatedFilename, UserSelection userSelection) throws MiddlewareQueryException {
		Mockito.doReturn(userSelection).when(exportStudyControllerMock).getUserSelection();
		PaginationListSelection paginationListSelection = Mockito.mock(PaginationListSelection.class);
		Mockito.doReturn(paginationListSelection).when(exportStudyControllerMock).getPaginationListSelection();
		Mockito.doReturn(null).when(paginationListSelection).getReviewFullWorkbook("0");
		Mockito.doReturn(generatedFilename).when(exportStudyControllerMock).getFileName(userSelection);
		Mockito.when(this.ontologyService.getProperty(Matchers.anyString())).thenReturn(this.getProperty());
	}

	private Property getProperty() {
		Property prop = new Property();
		Term term = new Term();
		term.setId(-1);
		prop.setTerm(term);
		return prop;
	}

	private Map<String, String> getData() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("visibleColumns", this.getVisibleColumnsString());
		data.put("studyExportId", "0");
		return data;
	}

	private ExportStudyController initializeExportStudyControllerForStudyCSVExport() {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		exportStudyControllerMock.setOntologyService(this.ontologyService);
		exportStudyControllerMock.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		exportStudyControllerMock.setCsvExportStudyService(this.csvExportStudyService);
		exportStudyControllerMock.setExportOrderingRowColService(this.exportOrderingRowColService);
		return exportStudyControllerMock;
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

		ExportStudyController exportStudyControllerMock = this.initializeExportStudyControllerForStudyCSVExport();

		// Inputs
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 1);

		String outputFilename = ExportStudyControllerTest.SAMPLE_TRIAL_FILENAME;
		String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		List<Integer> instances = WorkbookDataUtil.getTrialInstances();
		Integer exportType = AppConstants.EXPORT_CSV.getInt();
		Integer exportWayType = 1;
		Map<String, String> data = this.getData();

		// Mock Object Method Calls
		UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(workbook);
		Mockito.doReturn(userSelection).when(exportStudyControllerMock).getUserSelection();

		this.mockOtherRelatedMethodCallsForExportStudyMethods(exportStudyControllerMock, generatedFilename, userSelection);

		Mockito.when(
				this.csvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances,
						this.getVisibleColumns())).thenReturn(outputFilename + ExportStudyControllerTest.CSV_EXT);
		Mockito.when(this.resp.getContentType()).thenReturn(ExportStudyControllerTest.CSV_CONTENT_TYPE);

		String returnedValue = exportStudyControllerMock.exportFileTrial(data, exportType, "1", exportWayType, this.req, this.resp);
		HashMap<String, String> result = new ObjectMapper().readValue(returnedValue, HashMap.class);
		Assert.assertEquals("Expected that the returned content type is " + ExportStudyControllerTest.CSV_CONTENT_TYPE + " but returned "
				+ result.get("contentType"), ExportStudyControllerTest.CSV_CONTENT_TYPE, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + outputFilename + ".csv but returned " + result.get("filename"),
				outputFilename + ExportStudyControllerTest.CSV_EXT, result.get("filename"));
		Assert.assertEquals(
				"Expected that the returned output filename is " + outputFilename + ".csv but returned " + result.get("outputFilename"),
				outputFilename + ExportStudyControllerTest.CSV_EXT, result.get("outputFilename"));
	}

	@Test
	public void testDoExportTrialWithMultipleInstancesInCSVFormat() throws JsonParseException,
			JsonMappingException, IOException, MiddlewareException {

		ExportStudyController exportStudyControllerMock = this.initializeExportStudyControllerForStudyCSVExport();

		// Inputs
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 3);

		String outputFilename = ExportStudyControllerTest.SAMPLE_TRIAL_FILENAME;
		String generatedFilename = workbook.getStudyDetails().getStudyName() + "_" + workbook.getStudyDetails().getId();
		List<Integer> instances = WorkbookDataUtil.getTrialInstances();

		Integer exportType = AppConstants.EXPORT_CSV.getInt();
		Integer exportWayType = 1;
		Map<String, String> data = this.getData();

		// Mock Object Method Calls
		UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(workbook);
		Mockito.doReturn(userSelection).when(exportStudyControllerMock).getUserSelection();

		this.mockOtherRelatedMethodCallsForExportStudyMethods(exportStudyControllerMock, generatedFilename, userSelection);

		Mockito.when(
				this.csvExportStudyService.export(workbook, generatedFilename + ExportStudyControllerTest.CSV_EXT, instances,
						this.getVisibleColumns())).thenReturn(outputFilename + ExportStudyControllerTest.ZIP_EXT);
		Mockito.when(this.resp.getContentType()).thenReturn(ExportStudyControllerTest.ZIP_CONTENT_TYPE);

		String returnedValue =
				exportStudyControllerMock.exportFileTrial(data, exportType, this.getTrialInstanceString(instances), exportWayType,
						this.req, this.resp);
		HashMap<String, String> result = new ObjectMapper().readValue(returnedValue, HashMap.class);
		Assert.assertEquals("Expected that the returned content type is " + ExportStudyControllerTest.ZIP_CONTENT_TYPE + " but returned "
				+ result.get("contentType"), ExportStudyControllerTest.ZIP_CONTENT_TYPE, result.get("contentType"));
		Assert.assertEquals("Expected that the returned filename is " + generatedFilename + ".zip but returned " + result.get("filename"),
				generatedFilename + ExportStudyControllerTest.ZIP_EXT, result.get("filename"));
		Assert.assertEquals(
				"Expected that the returned output filename is " + outputFilename + ".zip but returned " + result.get("outputFilename"),
				outputFilename + ExportStudyControllerTest.ZIP_EXT, result.get("outputFilename"));
	}

	@Test
	public void testDoStockExport() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		Mockito.when(exportAdvanceListService.exportStockList(1, exportService)).thenReturn(new File("temp.xls"));

		UserSelection userSelection = new UserSelection();
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("TempName");
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		Mockito.doReturn(userSelection).when(exportStudyControllerMock).getUserSelection();
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter("exportStockListId")).thenReturn("1");
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);

		String ret = exportStudyControllerMock.doExportStockList(resp, req);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> mapObject = mapper.readValue(ret, new TypeReference<Map<String, String>>() {
		});
		String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of application/vnd.ms-excel since its just 1 stock list",
				"application/vnd.ms-excel".equalsIgnoreCase(contentType));
	}

	@Test
	public void testGetCustomReportTypes() throws MiddlewareQueryException {
		ExportStudyController controller = new ExportStudyController();
		WorkbenchService workbenchService = Mockito.mock(WorkbenchService.class);
		controller.setWorkbenchService(workbenchService);
		CrossExpansionProperties crossExpansionProperties = new CrossExpansionProperties();
		crossExpansionProperties.setProfile("Cimmyt");
		controller.setCrossExpansionProperties(crossExpansionProperties);
		List<StandardPreset> standardPresets = new ArrayList<StandardPreset>();
		StandardPreset preset = new StandardPreset();
		preset.setConfiguration("<reports><profile>cimmyt</profile><report><code>WLBL05</code><name>labels without design, wheat</name></report><report><code>WLBL21</code><name>labels with design, wheat</name></report></reports>");
		standardPresets.add(preset);
		Mockito.when(workbenchService.getStandardPresetByCrop(Matchers.anyInt(), Matchers.anyString(), Matchers.anyString())).thenReturn(
				standardPresets);
		Tool fbTool = new Tool();
		fbTool.setToolId(new Long(1));
		Mockito.when(workbenchService.getFieldbookWebTool()).thenReturn(fbTool);
		ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		Project project = new Project();
		CropType cropType = new CropType();
		cropType.setCropName("Test");
		project.setCropType(cropType);
		Mockito.when(contextUtil.getProjectInContext()).thenReturn(project);
		controller.setContextUtil(contextUtil);
		List<CustomReportType> presets = controller.getCustomReportTypes(ToolSection.FB_TRIAL_MGR_CUSTOM_REPORT.name());
		Assert.assertEquals("Should return 2 presets since there is a study", 2, presets.size());
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
