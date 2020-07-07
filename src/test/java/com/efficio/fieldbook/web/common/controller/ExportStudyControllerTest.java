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

import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.ExportAdvanceListService;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ExportStudyControllerTest {

	@Mock
	private GermplasmExportService germplasmExportService;

	@Mock
	private ExportAdvanceListService exportAdvanceListService;

	@Mock
	private HttpServletRequest req;

	@Mock
	private HttpServletResponse resp;

	@InjectMocks
	private ExportStudyController exportStudyController;

	private UserSelection userSelection;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.userSelection = new UserSelection();
		this.exportStudyController.setUserSelection(this.userSelection);
		final PaginationListSelection paginationListSelection = Mockito.mock(PaginationListSelection.class);
		this.exportStudyController.setPaginationListSelection(paginationListSelection);
	}

	@Test
	public void testDoAdvanceExportCsvMoreThan1() throws JsonProcessingException, IOException {

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1|2|3", "TempName", this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_STUDY_CSV.getString())).thenReturn(new FileExportInfo("temp.zip", "temp.zip"));

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
		final String contentType = (String) mapObject.get(ExportStudyController.CONTENT_TYPE);
		Assert.assertTrue("Should have a content type of zip since its more than 1 advanced list",
				FileUtils.MIME_ZIP.equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportCsvOnly1() throws JsonParseException, JsonMappingException, IOException {

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1", "TempName", this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_STUDY_CSV.getString())).thenReturn(new FileExportInfo("temp.csv", "temp.csv"));

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
		final String contentType = (String) mapObject.get(ExportStudyController.CONTENT_TYPE);
		Assert.assertTrue("Should have a content type of text/csv since its just 1 advanced list",
				FileUtils.MIME_CSV.equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportXlsMoreThan1() throws JsonProcessingException, IOException {

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1|2|3", "TempName", this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_STUDY_EXCEL.getString())).thenReturn(new FileExportInfo("temp.zip", "temp.zip"));

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
		final String contentType = mapObject.get(ExportStudyController.CONTENT_TYPE);
		Assert.assertTrue("Should have a content type of zip since its more than 1 advanced list",
				FileUtils.MIME_ZIP.equalsIgnoreCase(contentType));
	}

	@Test
	public void testDoAdvanceExportXlsOnly1() throws JsonParseException, JsonMappingException, IOException {

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1", "TempName", this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_STUDY_EXCEL.getString())).thenReturn(new FileExportInfo("temp.xls", "temp.xls"));

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
		final String contentType = (String) mapObject.get(ExportStudyController.CONTENT_TYPE);
		Assert.assertTrue("Should have a content type of application/vnd.ms-excel since its just 1 advanced list",
				FileUtils.MIME_MS_EXCEL.equalsIgnoreCase(contentType));
	}

	@Test
	public void testExportAdvanceListItemsInXlsExportType() {
		final StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_STUDY_EXCEL.getString())).thenReturn(new FileExportInfo("temp.xls", "temp.xls"));

		final FileExportInfo fileExportInfo = this.exportStudyController.exportAdvanceListItems("1", "1", details);
		Assert.assertNotNull("Return file should not be null", fileExportInfo.getFilePath());
	}

	@Test
	public void testExportAdvanceListItemsInCsvExportType() {
		final StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		Mockito.when(this.exportAdvanceListService.exportAdvanceGermplasmList("1", details.getStudyName(), this.germplasmExportService,
				AppConstants.EXPORT_ADVANCE_STUDY_CSV.getString())).thenReturn(new FileExportInfo("temp.csv", "temp.csv"));

		final FileExportInfo exportInfo =
				this.exportStudyController.exportAdvanceListItems(AppConstants.EXPORT_ADVANCE_STUDY_CSV.getString(), "1", details);
		Assert.assertNotNull("Return file should not be null", exportInfo.getFilePath());
	}

	@Test
	public void testExportAdvanceListItemsInNullExportType() {
		final StudyDetails details = new StudyDetails();
		details.setStudyName("TestStudy");

		final FileExportInfo exportInfo = this.exportStudyController.exportAdvanceListItems("3", "1", details);
		Assert.assertTrue("Return file should be null", exportInfo.getDownloadFileName() == null && exportInfo.getFilePath() == null);
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


	private String getTrialInstanceString(final List<Integer> instances) {
		String trialInstances = "";

		for (final Integer instance : instances) {
			if ("".equalsIgnoreCase(trialInstances)) {
				trialInstances = instance.toString();
			} else {
				trialInstances = trialInstances + "-" + instance.toString();
			}
		}

		return trialInstances;
	}
}
