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

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.form.LabelPrintingForm;
import net.sf.jasperreports.engine.JRException;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.data.initializer.FieldMapInfoTestDataInitializer;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.reports.BuildReportException;
import org.generationcp.middleware.reports.Reporter;
import org.generationcp.middleware.reports.WLabels21;
import org.generationcp.middleware.service.api.ReportService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LabelPrintingControllerTest extends AbstractBaseIntegrationTest {

	private static final String CSV_EXT = ".csv";
	private static final String XLS_EXT = ".xls";
	private static final String PDF_EXT = ".pdf";
	private static final String EMPTY_STRING = "";
	public static final int SUCCESS_VAL = 1;
	public static final String TEST_JASPER_REPORT_FILE_TXT = "TEST_JASPER_REPORT_FILE.txt";
	public static final int SAMPLE_STUDY_ID = 25004;
	public static final String WLBL_21_JASPER_REPORT = "WLBL21";
	public static final String RETURN_VALUE = "/template/base-template";
	public static final String FILE_NAME = "Study-Field-Map-Labels-";
	public static final int FAIL_VAL = 0;

	@Resource
	private UserFieldmap userFieldmap;

	@Resource
	private LabelPrintingController labelPrintingController;
	
	@Mock
	protected ContextUtil contextUtil;

	@Mock
	protected WorkbenchDataManager workbenchDataManager;
	
	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	private static FieldMapInfoTestDataInitializer fieldMapInfoTDI;

	@BeforeClass
	public static void setUpClass() {
		LabelPrintingControllerTest.fieldMapInfoTDI = new FieldMapInfoTestDataInitializer();
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.labelPrintingController.setContextUtil(this.contextUtil);
		this.labelPrintingController.setWorkbenchDataManager(this.workbenchDataManager);
		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();

		final Tool fieldbookTool = new Tool();
		fieldbookTool.setToolId(1l);
		Mockito.when(this.workbenchDataManager.getToolWithName(ToolName.FIELDBOOK_WEB.getName())).thenReturn(fieldbookTool);

	}
	
	@Test
	public void testGenerationOfPDFLabels() {
		final List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		final UserLabelPrinting userLabelPrinting =
				LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_PDF.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		final Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertEquals("Label Printing should be successful", LabelPrintingControllerTest.SUCCESS_VAL,
				results.get(LabelPrintingController.IS_SUCCESS));
		final String filenameInMap = results.get(LabelPrintingController.FILE_NAME).toString();
		Assert.assertTrue("Expected pdf file generated but found " + filenameInMap,
				filenameInMap.endsWith(PDF_EXT));
		Assert.assertEquals(LabelPrintingDataUtil.FILE_NAME_DL_PDF, userLabelPrinting.getFilenameWithExtension());
		
		final String outputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		final File outputFile = new File(userLabelPrinting.getFilenameDLLocation());
		Assert.assertEquals(outputDirectoryFile, outputFile.getParentFile());
		Assert.assertEquals(filenameInMap, outputFile.getAbsolutePath());
		Assert.assertTrue(outputFile.getName().startsWith(LabelPrintingDataUtil.FILE_NAME));
		Assert.assertTrue(outputFile.getName().endsWith(PDF_EXT));
	}

	@Test
	public void testGenerationOfXLSLabels() {
		final List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		final UserLabelPrinting userLabelPrinting =
				LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_EXCEL.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_EXCEL.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		final Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertEquals("Label Printing should be successful", LabelPrintingControllerTest.SUCCESS_VAL,
				results.get(LabelPrintingController.IS_SUCCESS));
		final String filenameInMap = results.get(LabelPrintingController.FILE_NAME).toString();
		Assert.assertTrue("Expected pdf file generated but found " + filenameInMap,
				filenameInMap.endsWith(XLS_EXT));
		Assert.assertEquals(LabelPrintingDataUtil.FILE_NAME_DL_XLS, userLabelPrinting.getFilenameWithExtension());
		
		final String outputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		final File outputFile = new File(userLabelPrinting.getFilenameDLLocation());
		Assert.assertEquals(outputDirectoryFile, outputFile.getParentFile());
		Assert.assertEquals(filenameInMap, outputFile.getAbsolutePath());
		Assert.assertTrue(outputFile.getName().startsWith(LabelPrintingDataUtil.FILE_NAME));
		Assert.assertTrue(outputFile.getName().endsWith(XLS_EXT));
	}

	@Test
	public void testGenerationOfCSVLabels() {
		final List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		final UserLabelPrinting userLabelPrinting =
				LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_CSV.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		final Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertEquals("Label Printing should be successful", LabelPrintingControllerTest.SUCCESS_VAL,
				results.get(LabelPrintingController.IS_SUCCESS));
		final String filenameInMap = results.get(LabelPrintingController.FILE_NAME).toString();
		Assert.assertTrue("Expected pdf file generated but found " + filenameInMap,
				filenameInMap.endsWith(CSV_EXT));
		Assert.assertEquals(LabelPrintingDataUtil.FILE_NAME_DL_CSV, userLabelPrinting.getFilenameWithExtension());
		
		final String outputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		final File outputFile = new File(userLabelPrinting.getFilenameDLLocation());
		Assert.assertEquals(outputDirectoryFile, outputFile.getParentFile());
		Assert.assertEquals(filenameInMap, outputFile.getAbsolutePath());
		Assert.assertTrue(outputFile.getName().startsWith(LabelPrintingDataUtil.FILE_NAME));
		Assert.assertTrue(outputFile.getName().endsWith(CSV_EXT));
	}

	@Ignore // FIXME fail on fresh db because there is no program. Create program for test
	@Test
	public void testGenerationOfCustomReportLabels() throws BuildReportException, IOException, JRException {
		final List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		// not really a csv type.. we just want an initial UserLabelPrinting pojo object then subsequenly convert this to a jasper report
		// type
		final UserLabelPrinting userLabelPrinting =
				LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
		userLabelPrinting.setGenerateType(LabelPrintingControllerTest.WLBL_21_JASPER_REPORT);
		userLabelPrinting.setStudyId(LabelPrintingControllerTest.SAMPLE_STUDY_ID);
		userLabelPrinting.setTitle("Description");
		userLabelPrinting.setName("Name");

		// Mock the report service, we just need a report instance to test with
		final ReportService reportService = Mockito.mock(ReportService.class);
		final Reporter reporter = Mockito.mock(WLabels21.class);
		Mockito.when(reporter.getFileName()).thenReturn(LabelPrintingControllerTest.TEST_JASPER_REPORT_FILE_TXT);
		Mockito.when(reporter.getFileExtension()).thenReturn("txt");

		// We dont care about program name in context, OutputStream is created inside the method
		Mockito.when(reportService.getStreamReport(Matchers.eq(userLabelPrinting.getGenerateType()),
				Matchers.eq(userLabelPrinting.getStudyId()), Matchers.anyString(), Matchers.any(OutputStream.class))).thenReturn(reporter);
		this.labelPrintingController.setReportService(reportService);
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		// Method to test
		final Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, true);

		// Assertions
		Assert.assertNotNull("We expect that results has value", results);
		Assert.assertEquals("Label Printing report should be success", LabelPrintingControllerTest.SUCCESS_VAL,
				results.get(LabelPrintingController.IS_SUCCESS));
		Assert.assertEquals("We get the generated label printing file", results.get(LabelPrintingController.FILE_NAME), reporter.getFileName());
	}

	@Test
	public void testGenerationOfLabelsUsingAnUnknownType() {
		final List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();

		// we create a nonsense userLabelPrinting obj with an invalid generate type
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setGenerateType(LabelPrintingControllerTest.EMPTY_STRING);

		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		// Method to test
		final Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		// Assertions
		Assert.assertNotNull("We expect that results has value", results);
		Assert.assertEquals("Label Printing report should NOT be a success", LabelPrintingControllerTest.FAIL_VAL,
				results.get(LabelPrintingController.IS_SUCCESS));

	}

	@Test
	public void testGetSelectedLabelFieldsForPDF() {
		final UserLabelPrinting userLabelPrinting =
				LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
		final String selectedLabelFields = this.labelPrintingController.getSelectedLabelFields(userLabelPrinting);
		final String expected = userLabelPrinting.getLeftSelectedLabelFields() + "," + userLabelPrinting.getRightSelectedLabelFields();
		Assert.assertEquals("Expecting the return results is " + expected + " but returned " + selectedLabelFields, expected,
				selectedLabelFields);
	}

	@Test
	public void testGetSelectedLabelFieldsForCSV() {
		final UserLabelPrinting userLabelPrinting =
				LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
		final String selectedLabelFields = this.labelPrintingController.getSelectedLabelFields(userLabelPrinting);
		final String expected = userLabelPrinting.getMainSelectedLabelFields();
		Assert.assertEquals("Expecting the return results is " + expected + " but returned " + selectedLabelFields, expected,
				selectedLabelFields);
	}

	@Test
	public void testGetLabelPrintingCustomReportsIfThereIsStudyId() throws MiddlewareQueryException {
		final Integer studyId = new Integer(3);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setStudyId(studyId);
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		final CrossExpansionProperties crossExpansionProperties = new CrossExpansionProperties();
		crossExpansionProperties.setProfile("Cimmyt");
		this.labelPrintingController.setCrossExpansionProperties(crossExpansionProperties);
		final List<StandardPreset> standardPresets = new ArrayList<StandardPreset>();
		final StandardPreset preset = new StandardPreset();
		preset.setConfiguration(
				"<reports><profile>cimmyt</profile><report><code>WLBL05</code><name>labels without design, wheat</name></report><report><code>WLBL21</code><name>labels with design, wheat</name></report></reports>");
		standardPresets.add(preset);
		Mockito.when(this.workbenchDataManager.getStandardPresetFromCropAndTool(Matchers.anyString(), Matchers.anyInt(), Matchers.anyString()))
				.thenReturn(standardPresets);
		final Tool fbTool = new Tool();
		fbTool.setToolId(new Long(1));
		Mockito.when(this.workbenchDataManager.getToolWithName(ToolName.FIELDBOOK_WEB.getName())).thenReturn(fbTool);
		final ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		final Project project = new Project();
		final CropType cropType = new CropType();
		cropType.setCropName("Test");
		project.setCropType(cropType);
		Mockito.when(contextUtil.getProjectInContext()).thenReturn(project);
		this.labelPrintingController.setContextUtil(contextUtil);
		final List<CustomReportType> presets = this.labelPrintingController.getLabelPrintingCustomReports();
		Assert.assertEquals("Should return 2 presets since there is a study", 2, presets.size());
	}

	@Test
	public void testGetLabelPrintingCustomReportsIfThereIsNoStudyId() throws MiddlewareQueryException {
		final Integer studyId = null;
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setStudyId(studyId);
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		final List<CustomReportType> presets = this.labelPrintingController.getLabelPrintingCustomReports();

		Assert.assertEquals("Should return no preset since there is not study", 0, presets.size());
	}

	@Test
	public void testShowFieldmapLabelDetails() {
		final UserLabelPrinting userLabelPrinting =
				LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		this.userFieldmap.setSelectedFieldMaps(LabelPrintingControllerTest.fieldMapInfoTDI.createFieldMapInfoList( 1));
		final LabelPrintingForm form = Mockito.mock(LabelPrintingForm.class);
		final Model model = Mockito.mock(Model.class);
		final HttpSession session = Mockito.mock(HttpSession.class);
		final Locale locale = LocaleContextHolder.getLocale();

		final String returnValue = this.labelPrintingController.showFieldmapLabelDetails(form, model, session, locale);
		final UserLabelPrinting resultUserLabelPrinting = this.labelPrintingController.getUserLabelPrinting();

		Assert.assertEquals("The return value should be " + LabelPrintingControllerTest.RETURN_VALUE,
				LabelPrintingControllerTest.RETURN_VALUE, returnValue);
		Assert.assertNull("The filename should be null.", resultUserLabelPrinting.getStudyId());
		Assert.assertEquals("The field map info's value should be " + this.userFieldmap.getSelectedFieldMaps().get(0),
				this.userFieldmap.getSelectedFieldMaps().get(0), resultUserLabelPrinting.getFieldMapInfo());
		Assert.assertEquals("The Barcode Needed's Value should be '0'.", "0", resultUserLabelPrinting.getBarcodeNeeded());
		Assert.assertEquals("The Include Column Heading in Non Pdf's Value should be '1'.", "1",
				resultUserLabelPrinting.getIncludeColumnHeadinginNonPdf());
		Assert.assertEquals("The Number of Labels per row should be 3.", "3", resultUserLabelPrinting.getNumberOfLabelPerRow());
		Assert.assertEquals("The First Barcode Field's value should be an empty String.", LabelPrintingControllerTest.EMPTY_STRING,
				resultUserLabelPrinting.getFirstBarcodeField());
		Assert.assertEquals("The Second Barcode Field's value should be an empty String.", LabelPrintingControllerTest.EMPTY_STRING,
				resultUserLabelPrinting.getSecondBarcodeField());
		Assert.assertEquals("The Third Barcode Field's value should be an empty.", LabelPrintingControllerTest.EMPTY_STRING,
				resultUserLabelPrinting.getThirdBarcodeField());
		Assert.assertTrue("The Field Maps should be existing.", resultUserLabelPrinting.isFieldMapsExisting());
		Assert.assertEquals("The Settings name should be an empty String.", LabelPrintingControllerTest.EMPTY_STRING,
				resultUserLabelPrinting.getSettingsName());

		final String fileName = LabelPrintingControllerTest.FILE_NAME + new SimpleDateFormat("yyyyMMdd").format(new Date());
		Assert.assertEquals("The file name should be " + fileName, fileName, resultUserLabelPrinting.getFilename());
	}
		
	public void testExportFileInCSVFormat() throws UnsupportedEncodingException {
		final UserLabelPrinting userLabelPrinting =
				LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		final ResponseEntity<FileSystemResource> reponseEntity =
				this.labelPrintingController.exportFile(Mockito.mock(HttpServletRequest.class));

		final String filenameWithExtension = userLabelPrinting.getFilenameWithExtension();
		final String contentDisposition =
				"[attachment; filename=\"" + filenameWithExtension + "\"; filename*=utf-8''" + filenameWithExtension + "]";
		final String responseEntityContentDisposition = reponseEntity.getHeaders().get(FileUtils.CONTENT_DISPOSITION).toString();
		Assert.assertEquals("The content disposition should be " + contentDisposition, contentDisposition,
				responseEntityContentDisposition);

		final String contentType = "[application/octet-stream;charset=utf-8]";
		final String responseEntityContentType = reponseEntity.getHeaders().get(FileUtils.CONTENT_TYPE).toString();
		Assert.assertEquals("The content type should be " + contentType, contentType, responseEntityContentType);

		final String responseEntityFileName = reponseEntity.getBody().getFilename();
		Assert.assertEquals("The file name should be " + filenameWithExtension, filenameWithExtension, responseEntityFileName);
	}

	@Test
	public void testExportFileInExcelFormat() throws UnsupportedEncodingException {
		final UserLabelPrinting userLabelPrinting =
				LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_EXCEL.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		final ResponseEntity<FileSystemResource> reponseEntity =
				this.labelPrintingController.exportFile(Mockito.mock(HttpServletRequest.class));

		final String filenameWithExtension = userLabelPrinting.getFilenameWithExtension();
		final String contentDisposition =
				"[attachment; filename=\"" + filenameWithExtension + "\"; filename*=utf-8''" + filenameWithExtension + "]";
		final String responseEntityContentDisposition = reponseEntity.getHeaders().get(FileUtils.CONTENT_DISPOSITION).toString();
		Assert.assertEquals("The content disposition should be " + contentDisposition, contentDisposition,
				responseEntityContentDisposition);

		final String contentType = "[application/vnd.ms-excel;charset=utf-8]";
		final String responseEntityContentType = reponseEntity.getHeaders().get(FileUtils.CONTENT_TYPE).toString();
		Assert.assertEquals("The content type should be " + contentType, contentType, responseEntityContentType);

		final String responseEntityFileName = reponseEntity.getBody().getFilename();
		Assert.assertEquals("The file name should be " + filenameWithExtension, filenameWithExtension, responseEntityFileName);
	}

	@Test
	public void testExportFileInPDFFormat() throws UnsupportedEncodingException {
		final UserLabelPrinting userLabelPrinting =
				LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		final ResponseEntity<FileSystemResource> reponseEntity =
				this.labelPrintingController.exportFile(Mockito.mock(HttpServletRequest.class));

		final String filenameWithExtension = userLabelPrinting.getFilenameWithExtension();
		final String contentDisposition =
				"[attachment; filename=\"" + filenameWithExtension + "\"; filename*=utf-8''" + filenameWithExtension + "]";
		final String responseEntityContentDisposition = reponseEntity.getHeaders().get(FileUtils.CONTENT_DISPOSITION).toString();
		Assert.assertEquals("The content disposition should be " + contentDisposition, contentDisposition,
				responseEntityContentDisposition);

		final String contentType = "[application/pdf;charset=utf-8]";
		final String responseEntityContentType = reponseEntity.getHeaders().get(FileUtils.CONTENT_TYPE).toString();
		Assert.assertEquals("The content type should be " + contentType, contentType, responseEntityContentType);

		final String responseEntityFileName = reponseEntity.getBody().getFilename();
		Assert.assertEquals("The file name should be " + filenameWithExtension, filenameWithExtension, responseEntityFileName);
	}

	@Test
	public void testGenerateDefaultFilenameForFieldmapLabels() {

		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFieldMapInfoList(new ArrayList<FieldMapInfo>());
		Assert.assertEquals("Study-Field-Map-Labels-" + DateUtil.getCurrentDateAsStringValue(), this.labelPrintingController.generateDefaultFilename(userLabelPrinting));

	}

	@Test
	public void testGenerateDefaultFilename() {
		final String currentDate = DateUtil.getCurrentDateAsStringValue();
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setName("Name");
		Assert.assertEquals("Labels-for-Name-" + currentDate, this.labelPrintingController.generateDefaultFilename(userLabelPrinting));
		userLabelPrinting.setNumberOfInstances("2");
		Assert.assertEquals("Labels-for-Name-2-"  +  currentDate, this.labelPrintingController.generateDefaultFilename(userLabelPrinting));
	}

	private GermplasmListData getTestGermplasmListData(final GermplasmList germplasmList, final Integer listDataId, final Integer gid,
			final String withdrawalStatus, final Double withdrawalBalance) {
		final GermplasmListData listEntry = new GermplasmListData();
		listEntry.setId(listDataId);
		listEntry.setList(germplasmList);
		listEntry.setDesignation("Designation " + listDataId);
		listEntry.setEntryCode("EntryCode " + listDataId);
		listEntry.setEntryId(listDataId);
		listEntry.setGroupName("GroupName " + listDataId);
		listEntry.setStatus(listDataId);
		listEntry.setSeedSource("SeedSource " + listDataId);
		listEntry.setGid(gid);
		// Default GROUP ID is 0
		listEntry.setGroupId(0);

		final ListDataInventory listDataInventory = new ListDataInventory(listDataId, gid);
		listDataInventory.setLotCount(0);
		listDataInventory.setActualInventoryLotCount(0);

		final ArrayList<LotDetails> lotRows = new ArrayList<>();
		final LotDetails lotDetails = new LotDetails();
		lotDetails.setWithdrawalBalance(withdrawalBalance);
		lotDetails.setWithdrawalStatus(withdrawalStatus);
		lotRows.add(lotDetails);

		listDataInventory.setLotRows(lotRows);
		listDataInventory.setReservedLotCount(0);
		listDataInventory.setStockIDs("SID:" + listDataId);

		listEntry.setInventoryInfo(listDataInventory);
		return listEntry;
	}

	private GermplasmList getTestGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(5578);
		germplasmList.setName("List " + 5578);
		germplasmList.setDescription("List " + 5578 + " Description");
		germplasmList.setDate(20150101L);
		germplasmList.setUserId(1);
		germplasmList.setType(GermplasmListType.LST.name());
		germplasmList.setStatus(1);
		germplasmList.setNotes("Some notes here");
		return germplasmList;
	}
	
	@After
	public void cleanup() {
		this.deleteTestInstallationDirectory();
	}
	
	private void deleteTestInstallationDirectory() {
		// Delete test installation directory and its contents as part of cleanup
		final File testInstallationDirectory = new File(InstallationDirectoryUtil.WORKSPACE_DIR);
		this.installationDirectoryUtil.recursiveFileDelete(testInstallationDirectory);
	}
}
