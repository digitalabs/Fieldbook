/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.common.controller;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.generationcp.commons.service.ExportService;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.ExportAdvanceListService;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingRowColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverRangeImpl;
import com.efficio.fieldbook.web.util.AppConstants;

public class ExportStudyControllerTest{

	private ExportStudyController exportStudyController;
	
	@Before
	public void setUp(){
		exportStudyController = new ExportStudyController();
	}

	@Test
	public void testGetOutputFileNameValueChanged() {
		String fileName = this.exportStudyController.getOutputFileName(true, "trial-test_1.xls",
				"trial-test.xls");
		Assert.assertEquals("Expected trial-test.xls but got trial-test_1.xls", "trial-test.xls",
				fileName);
	}

	@Test
	public void testGetOutputFileNameValueRetained() {
		String fileName = this.exportStudyController.getOutputFileName(false, "trial-test_1.xls",
				"trial-test.xls");
		Assert.assertEquals("Expected trial-test_1.xls but got trial-test.xls", "trial-test_1.xls",
				fileName);
	}
	
	@Test
	public void testDoAdvanceExportCsvMoreThan1() throws MiddlewareQueryException, JsonProcessingException, IOException{
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		Mockito.when(exportAdvanceListService.exportAdvanceGermplasmList("1|2|3", "TempName", exportService,  AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.zip"));
		
		
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
		Map<String, String> mapObject = mapper.readValue(ret,new TypeReference<Map<String, String>>() {});
		String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of zip since its more than 1 advanced list", "application/zip".equalsIgnoreCase(contentType));
	}
	
	@Test
	public void testDoAdvanceExportCsvOnly1() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException{
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		Mockito.when(exportAdvanceListService.exportAdvanceGermplasmList("1", "TempName", exportService,  AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));
		
		
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
		Map<String, String> mapObject = mapper.readValue(ret,new TypeReference<Map<String, String>>() {});
		String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of text/csv since its just 1 advanced list", "text/csv".equalsIgnoreCase(contentType));
	}
	
	
	@Test
	public void testDoAdvanceExportXlsMoreThan1() throws MiddlewareQueryException, JsonProcessingException, IOException{
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		Mockito.when(exportAdvanceListService.exportAdvanceGermplasmList("1|2|3", "TempName", exportService,  AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.zip"));
		
		
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
		Map<String, String> mapObject = mapper.readValue(ret,new TypeReference<Map<String, String>>() {});
		String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of zip since its more than 1 advanced list", "application/zip".equalsIgnoreCase(contentType));
	}
	
	@Test
	public void testDoAdvanceExportXlsOnly1() throws MiddlewareQueryException, JsonParseException, JsonMappingException, IOException {
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		Mockito.when(exportAdvanceListService.exportAdvanceGermplasmList("1", "TempName", exportService,  AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.xls"));
		
		
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
		Map<String, String> mapObject = mapper.readValue(ret,new TypeReference<Map<String, String>>() {});
		String contentType = mapObject.get("contentType");
		Assert.assertTrue("Should have a content type of application/vnd.ms-excel since its just 1 advanced list", "application/vnd.ms-excel".equalsIgnoreCase(contentType));
	}
	
	@Test 
	public void testExportAdvanceListItemsInXlsExportType(){
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);
		StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");
		
		Mockito.when(exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), exportService,  AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString())).thenReturn(new File("temp.xls"));
		
		File file = exportStudyControllerMock.exportAdvanceListItems("1", "1", details);
		Assert.assertTrue("Return file should not be null", file != null);
	}
	
	@Test 
	public void testExportAdvanceListItemsInCsvExportType(){
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);
		StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");
		
		Mockito.when(exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), exportService,  AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));
		
		File file = exportStudyControllerMock.exportAdvanceListItems("2", "1", details);
		Assert.assertTrue("Return file should not be null", file != null);
	}
	
	@Test 
	public void testExportAdvanceListItemsInNullExportType(){
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		ExportService exportService = Mockito.mock(ExportService.class);
		Mockito.doReturn(exportService).when(exportStudyControllerMock).getExportServiceImpl();
		ExportAdvanceListService exportAdvanceListService = Mockito.mock(ExportAdvanceListService.class);
		exportStudyControllerMock.setExportAdvanceListService(exportAdvanceListService);
		StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");
		
		Mockito.when(exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), exportService,  AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString())).thenReturn(new File("temp.csv"));
		
		File file = exportStudyControllerMock.exportAdvanceListItems("3", "1", details);
		Assert.assertTrue("Return file should be null", file == null);
	}
	
	@Test
	public void testGetExportServiceImpl(){
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		Assert.assertTrue("Should return export service class", exportStudyControllerMock.getExportServiceImpl() instanceof ExportService);
	}
	
	@Test
	public void testExportGetUserSelection(){
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		exportStudyControllerMock.setUserSelection(new UserSelection());
		Assert.assertTrue("Should return a class type userSelect", exportStudyControllerMock.getUserSelection() instanceof UserSelection);
	}
	
	@Test
	public void testGetExportOrderServiceRow(){
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		exportStudyControllerMock.setExportOrderingRowColService(Mockito.mock(ExportOrderingRowColImpl.class));
		Assert.assertTrue("Should return ExportOrderingRowColImpl type", exportStudyControllerMock.getExportOrderService(1) instanceof ExportOrderingRowColImpl );
	}
	
	@Test
	public void testGetExportOrderServiceSerpentineOverRow(){
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		exportStudyControllerMock.setExportOrderingSerpentineOverRangeService(Mockito.mock(ExportOrderingSerpentineOverRangeImpl.class));
		Assert.assertTrue("Should return ExportOrderingSerpentineOverRangeImpl type", exportStudyControllerMock.getExportOrderService(2) instanceof ExportOrderingSerpentineOverRangeImpl );
	}
	
	@Test
	public void testGetExportOrderServiceSerpentineOverColumn(){
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		exportStudyControllerMock.setExportOrderingSerpentineOverColumnService(Mockito.mock(ExportOrderingSerpentineOverColImpl.class));
		Assert.assertTrue("Should return exportOrderingSerpentineOverColumnService type", exportStudyControllerMock.getExportOrderService(3) instanceof ExportOrderingSerpentineOverColImpl );
		
	}
	
	@Test
	public void testGetExportOrderServiceWhenTypeIs4(){
		ExportStudyController exportStudyControllerMock = Mockito.spy(new ExportStudyController());
		exportStudyControllerMock.setExportOrderingRowColService(Mockito.mock(ExportOrderingRowColImpl.class));
		Assert.assertTrue("Should return ExportOrderingRowColImpl type", exportStudyControllerMock.getExportOrderService(4) instanceof ExportOrderingRowColImpl );		
	}
}