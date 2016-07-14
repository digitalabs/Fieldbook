/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package com.efficio.fieldbook.web.label.printing.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.reports.BuildReportException;
import org.generationcp.middleware.reports.Reporter;
import org.generationcp.middleware.reports.WLabels21;
import org.generationcp.middleware.service.api.ReportService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;

import net.sf.jasperreports.engine.JRException;

public class LabelPrintingControllerTest extends AbstractBaseIntegrationTest {

	public static final int SUCCESS_VAL = 1;
	public static final String TEST_JASPER_REPORT_FILE_TXT = "TEST_JASPER_REPORT_FILE.txt";
	public static final int SAMPLE_STUDY_ID = 25004;
	public static final String WLBL_21_JASPER_REPORT = "WLBL21";
	public static final int FAIL_VAL = 0;
	@Resource
	private LabelPrintingController labelPrintingController;

	@Test
	public void testGenerationOfPDFLabels() {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_PDF.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertTrue("Expected pdf file generated but found " + results.get("fileName").toString(),
				results.get("fileName").toString().contains("pdf"));
	}

	@Test
	public void testGenerationOfXLSLabels() {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_EXCEL.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_EXCEL.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertTrue("Expected xls file generated but found " + results.get("fileName").toString(),
				results.get("fileName").toString().contains("xls"));
	}

	@Test
	public void testGenerationOfCSVLabels() {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_CSV.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertTrue("Expected csv file generated but found " + results.get("fileName").toString(),
				results.get("fileName").toString().contains("csv"));
	}

	@Test
	public void testGenerationOfCustomReportLabels() throws BuildReportException, IOException, JRException {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		// not really a csv type.. we just want an initial UserLabelPrinting pojo object then subsequenly convert this to a jasper report type
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
		userLabelPrinting.setGenerateType(WLBL_21_JASPER_REPORT);
		userLabelPrinting.setStudyId(SAMPLE_STUDY_ID);

		// Mock the report service, we just need a report instance to test with
		ReportService reportService = Mockito.mock(ReportService.class);
		Reporter reporter = Mockito.mock(WLabels21.class);
		Mockito.when(reporter.getFileName()).thenReturn(TEST_JASPER_REPORT_FILE_TXT);
		Mockito.when(reporter.getFileExtension()).thenReturn("txt");

		// We dont care about program name in context, OutputStream is created inside the method
		Mockito.when(reportService
				.getStreamReport(Mockito.eq(userLabelPrinting.getGenerateType()), Mockito.eq(userLabelPrinting.getStudyId()),
						Mockito.anyString(), Mockito.any(OutputStream.class))).thenReturn(reporter);
		this.labelPrintingController.setReportService(reportService);
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		// Method to test
		Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, true);

		// Assertions
		Assert.assertNotNull("We expect that results has value", results);
		Assert.assertEquals("Label Printing report should be success", SUCCESS_VAL, results.get(LabelPrintingController.IS_SUCCESS));
		Assert.assertEquals("We get the generated label printing file", results.get("fileName"), reporter.getFileName());
	}

	@Test
	public void testGenerationOfLabelsUsingAnUnknownType() {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();

		// we create a nonsense userLabelPrinting obj with an invalid generate type
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setGenerateType("");

		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		// Method to test
		Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		// Assertions
		Assert.assertNotNull("We expect that results has value", results);
		Assert.assertEquals("Label Printing report should NOT be a success", FAIL_VAL, results.get(LabelPrintingController.IS_SUCCESS));

	}

	@Test
	public void testGetSelectedLabelFieldsForPDF() {
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
		String selectedLabelFields = this.labelPrintingController.getSelectedLabelFields(userLabelPrinting);
		String expected = userLabelPrinting.getLeftSelectedLabelFields() + "," + userLabelPrinting.getRightSelectedLabelFields();
		Assert.assertEquals("Expecting the return results is " + expected + " but returned " + selectedLabelFields, expected,
				selectedLabelFields);
	}

	@Test
	public void testGetSelectedLabelFieldsForCSV() {
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
		String selectedLabelFields = this.labelPrintingController.getSelectedLabelFields(userLabelPrinting);
		String expected = userLabelPrinting.getMainSelectedLabelFields();
		Assert.assertEquals("Expecting the return results is " + expected + " but returned " + selectedLabelFields, expected,
				selectedLabelFields);
	}

	@Test
	public void testGetLabelPrintingCustomReportsIfThereIsStudyId() throws MiddlewareQueryException {
		WorkbenchService workbenchService = Mockito.mock(WorkbenchService.class);
		Integer studyId = new Integer(3);
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setStudyId(studyId);
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		this.labelPrintingController.setWorkbenchService(workbenchService);
		CrossExpansionProperties crossExpansionProperties = new CrossExpansionProperties();
		crossExpansionProperties.setProfile("Cimmyt");
		this.labelPrintingController.setCrossExpansionProperties(crossExpansionProperties);
		List<StandardPreset> standardPresets = new ArrayList<StandardPreset>();
		StandardPreset preset = new StandardPreset();
		preset.setConfiguration(
				"<reports><profile>cimmyt</profile><report><code>WLBL05</code><name>labels without design, wheat</name></report><report><code>WLBL21</code><name>labels with design, wheat</name></report></reports>");
		standardPresets.add(preset);
		Mockito.when(workbenchService.getStandardPresetByCrop(Matchers.anyInt(), Matchers.anyString(), Matchers.anyString()))
				.thenReturn(standardPresets);
		Tool fbTool = new Tool();
		fbTool.setToolId(new Long(1));
		Mockito.when(workbenchService.getFieldbookWebTool()).thenReturn(fbTool);
		ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		Project project = new Project();
		CropType cropType = new CropType();
		cropType.setCropName("Test");
		project.setCropType(cropType);
		Mockito.when(contextUtil.getProjectInContext()).thenReturn(project);
		this.labelPrintingController.setContextUtil(contextUtil);
		List<CustomReportType> presets = this.labelPrintingController.getLabelPrintingCustomReports();
		Assert.assertEquals("Should return 2 presets since there is a study", 2, presets.size());
	}

	@Test
	public void testGetLabelPrintingCustomReportsIfThereIsNoStudyId() throws MiddlewareQueryException {
		WorkbenchService workbenchService = Mockito.mock(WorkbenchService.class);
		Integer studyId = null;
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setStudyId(studyId);
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		this.labelPrintingController.setWorkbenchService(workbenchService);
		List<CustomReportType> presets = this.labelPrintingController.getLabelPrintingCustomReports();
		Assert.assertEquals("Should return no preset since there is not study", 0, presets.size());
	}
	
	@Test
	public void testExportFileInCSVFormat() throws UnsupportedEncodingException {
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		String filename = "filename.csv";
		String contentType = "[application/octet-stream;charset=utf-8]";
		String contentDisposition = "[attachment; filename=\"filename.csv\"; filename*=utf-8''filename.csv]";
		userLabelPrinting.setFilenameWithExtention(filename);
		userLabelPrinting.setFilenameDLLocation("C://tmp/" + filename);
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		ResponseEntity<FileSystemResource> reponseEntity = this.labelPrintingController.exportFile(Mockito.mock(HttpServletRequest.class));
		
		final String responseEntityContentDisposition  = reponseEntity.getHeaders().get(FieldbookUtil.CONTENT_DISPOSITION).toString();
		Assert.assertEquals("The content disposition should be " + contentDisposition, contentDisposition, responseEntityContentDisposition);
		final String responseEntityContentType = reponseEntity.getHeaders().get(FieldbookUtil.CONTENT_TYPE).toString();
		Assert.assertEquals("The content type should be " + contentType, contentType, responseEntityContentType);
		final String reponseEntityFileName = reponseEntity.getBody().getFilename();
		Assert.assertEquals("The file name should be " + filename, filename, reponseEntityFileName);
	}
	
	@Test
	public void testExportFileInExcelFormat() throws UnsupportedEncodingException {
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		String filename = "filename.xls";
		String contentType = "[application/vnd.ms-excel;charset=utf-8]";
		String contentDisposition = "[attachment; filename=\"filename.xls\"; filename*=utf-8''filename.xls]";
		userLabelPrinting.setFilenameWithExtention(filename);
		userLabelPrinting.setFilenameDLLocation("C://tmp/" + filename);
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		ResponseEntity<FileSystemResource> reponseEntity = this.labelPrintingController.exportFile(Mockito.mock(HttpServletRequest.class));
		
		final String responseEntityContentDisposition  = reponseEntity.getHeaders().get(FieldbookUtil.CONTENT_DISPOSITION).toString();
		Assert.assertEquals("The content disposition should be " + contentDisposition, contentDisposition, responseEntityContentDisposition);
		final String responseEntityContentType = reponseEntity.getHeaders().get(FieldbookUtil.CONTENT_TYPE).toString();
		Assert.assertEquals("The content type should be " + contentType, contentType, responseEntityContentType);
		final String reponseEntityFileName = reponseEntity.getBody().getFilename();
		Assert.assertEquals("The file name should be " + filename, filename, reponseEntityFileName);
	}
	
	@Test
	public void testExportFileInPDFFormat() throws UnsupportedEncodingException {
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		String filename = "filename.pdf";
		String contentType = "[application/pdf;charset=utf-8]";
		String contentDisposition = "[attachment; filename=\"filename.pdf\"; filename*=utf-8''filename.pdf]";
		userLabelPrinting.setFilenameWithExtention(filename);
		userLabelPrinting.setFilenameDLLocation("C://tmp/" + filename);
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		ResponseEntity<FileSystemResource> reponseEntity = this.labelPrintingController.exportFile(Mockito.mock(HttpServletRequest.class));
		
		final String responseEntityContentDisposition  = reponseEntity.getHeaders().get(FieldbookUtil.CONTENT_DISPOSITION).toString();
		Assert.assertEquals("The content disposition should be " + contentDisposition, contentDisposition, responseEntityContentDisposition);
		final String responseEntityContentType = reponseEntity.getHeaders().get(FieldbookUtil.CONTENT_TYPE).toString();
		Assert.assertEquals("The content type should be " + contentType, contentType, responseEntityContentType);
		final String reponseEntityFileName = reponseEntity.getBody().getFilename();
		Assert.assertEquals("The file name should be " + filename, filename, reponseEntityFileName);
	}
}
