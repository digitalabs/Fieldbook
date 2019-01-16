
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import com.efficio.fieldbook.web.util.parsing.InventoryHeaderLabels;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.web.util.AppConstants;

public class ExportAdvanceListServiceImplTest {

	private static final String SEED_AMOUNT_KG = "SEED_AMOUNT_kg";
	private static final String LIST_NAME_PREFIX = "TempGermplasmListName";
	private static final int LIST_COUNT = 3;

	@Mock
	private InventoryService inventoryMiddlewareService;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private GermplasmExportService germplasmExportServiceImpl;
	
	@Mock
	private ContextUtil contextUtil;

	@Mock
	public OntologyDataManager ontologyDataManager;

	@Captor
	private ArgumentCaptor<List<String>> filenameListCaptor;
	
	@InjectMocks
	private ExportAdvanceListServiceImpl exportAdvanceListServiceImpl;
	
	private String advancedListIds;
	private final String studyName = "StudyName";
	private List<InventoryDetails> inventoryDetailsList;
	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.inventoryDetailsList = this.generateSampleInventoryDetailsList(5);
		
		final StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= LIST_COUNT; i++) {
			final GermplasmList germplasmList = new GermplasmList();
			germplasmList.setType(GermplasmListType.ADVANCED.toString());
			germplasmList.setName(LIST_NAME_PREFIX + i);
			Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(i)).thenReturn(germplasmList);
			if (sb.length() > 0){
				sb.append("|");
			}
			sb.append(i);
		}
		this.advancedListIds = sb.toString();
				
		this.exportAdvanceListServiceImpl.setInventoryMiddlewareService(this.inventoryMiddlewareService);
		this.exportAdvanceListServiceImpl.setMessageSource(Mockito.mock(MessageSource.class));
		this.exportAdvanceListServiceImpl.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		
		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.ENTRY_ID.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.ENTRY.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.DESIGNATION.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.DESIGNATION.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.PARENTAGE.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.PARENTAGE.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.GID.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.GID.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.SEED_SOURCE.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.SOURCE.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.LOCATION.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.BULK_WITH.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.BULK_WITH.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.BULK_COMPL.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.BULK_COMPL.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.DUPLICATE.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.DUPLICATE.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.STOCKID_INVENTORY.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.STOCKID.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.COMMENT.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.COMMENT.getName(), null));
	}

	@Test
	public void testParseDelimitedAdvanceGermplasmListIds() {
		final List<Integer> advanceIds = this.exportAdvanceListServiceImpl.parseDelimitedAdvanceGermplasmListIds(this.advancedListIds);
		Assert.assertEquals("There should be 3 advance germplasm ids", LIST_COUNT, advanceIds.size());
		Assert.assertEquals("1st ID should be 1", 1, advanceIds.get(0).intValue());
		Assert.assertEquals("2nd ID should be 2", 2, advanceIds.get(1).intValue());
		Assert.assertEquals("3rd ID should be 3", 3, advanceIds.get(2).intValue());
	}

	@Test
	public void testGenerateAdvanceListColumnValues() {
		final List<ExportRow> exportRows =
				this.exportAdvanceListServiceImpl.generateAdvanceListColumnValues(this.inventoryDetailsList,
						this.exportAdvanceListServiceImpl.generateAdvanceListColumnHeaders(false, ""));
		Assert.assertEquals("There should be 5 set of column values", 5, exportRows.size());
		// we check random data
		Assert.assertEquals("The 1st GID should be 0", "0", exportRows.get(0).getValueForColumn(TermId.GID.getId()));
		Assert.assertEquals("The 2nd GID should be 1", "1", exportRows.get(1).getValueForColumn(TermId.GID.getId()));
		Assert.assertEquals("The 3rd GID should be 2", "2", exportRows.get(2).getValueForColumn(TermId.GID.getId()));
		Assert.assertEquals("The 4th GID should be 3", "3", exportRows.get(3).getValueForColumn(TermId.GID.getId()));
		Assert.assertEquals("The 5th GID should be 4", "4", exportRows.get(4).getValueForColumn(TermId.GID.getId()));
	}

	@Test
	public void testGetInventoryAmountWhenAmountIsNull() {
		final InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setAmount(null);

		Assert.assertEquals("The Amount should return be empty string", "",
				this.exportAdvanceListServiceImpl.getInventoryAmount(inventoryDetails));
	}

	@Test
	public void testGetInventoryAmountWhenAmountIsNonNull() {
		final InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setAmount(2.22);

		Assert.assertEquals("The Amount should be 2.22", "2.22", this.exportAdvanceListServiceImpl.getInventoryAmount(inventoryDetails));

		// Setting a whole number as inventory amount
		inventoryDetails.setAmount(2D);
		Assert.assertEquals("The Amount should be 2.0", "2.0", this.exportAdvanceListServiceImpl.getInventoryAmount(inventoryDetails));
	}

	@Test
	public void testGetInventoryScaleWhenScaleIsNull() {
		final InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setScaleName(null);

		Assert.assertEquals("The Scale should return be empty string", "",
				this.exportAdvanceListServiceImpl.getInventoryValue(inventoryDetails.getScaleName()));
	}

	@Test
	public void testGetInventoryScaleWhenScaleIsNonNull() {
		final String scaleName = "scaleTest";
		final InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setScaleName(scaleName);

		Assert.assertEquals("The Scale should be " + scaleName, scaleName,
				this.exportAdvanceListServiceImpl.getInventoryValue(inventoryDetails.getScaleName()));
	}

	@Test
	public void testGetInventoryCommentWhenCommentIsNull() {
		final InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setComment(null);

		Assert.assertEquals("The Comment should return be empty string", "",
				this.exportAdvanceListServiceImpl.getInventoryValue(inventoryDetails.getComment()));
	}

	@Test
	public void testGetInventoryCommentWhenCommentIsNonNull() {
		final String comment = "commentTest";
		final InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setComment(comment);

		Assert.assertEquals("The Comment should be " + comment, comment,
				this.exportAdvanceListServiceImpl.getInventoryValue(inventoryDetails.getComment()));
	}

	@Test
	public void testGenerateAdvanceListColumnHeaders() {
		final List<ExportColumnHeader> exportColumnHeaders = this.exportAdvanceListServiceImpl.generateAdvanceListColumnHeaders(false, "");

		Assert.assertEquals("1st column should be ENTRY NO", exportColumnHeaders.get(0).getId().intValue(), TermId.ENTRY_NO.getId());
		Assert.assertEquals("2nd column should be DESIG", exportColumnHeaders.get(1).getId().intValue(), TermId.DESIG.getId());
		Assert.assertEquals("3rd column should be CROSS", exportColumnHeaders.get(2).getId().intValue(), TermId.CROSS.getId());
		Assert.assertEquals("4th column should be GID", exportColumnHeaders.get(3).getId().intValue(), TermId.GID.getId());
		Assert.assertEquals("5th column should be SOURCE", exportColumnHeaders.get(4).getId().intValue(), TermId.SOURCE.getId());
		Assert.assertEquals("6th column should be LOCATION_ID", exportColumnHeaders.get(5).getId().intValue(), TermId.LOCATION_ID.getId());
		Assert.assertEquals("7th column should be LOCATION_ABBR", exportColumnHeaders.get(6).getId().intValue(), TermId.LOCATION_ABBR.getId());
		Assert.assertEquals("8th column should be INVENTORY_AMOUNT", exportColumnHeaders.get(7).getId().intValue(), TermId.SEED_AMOUNT_G.getId());
		Assert.assertEquals("9th column should be STOCKID", exportColumnHeaders.get(8).getId().intValue(), TermId.STOCKID.getId());
		Assert.assertEquals("10th column should be INVENTORY_COMMENT", exportColumnHeaders.get(9).getId().intValue(),
				AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt());
	}

	@Test
	public void testGenerateAdvanceListColumnHeadersForCrosses() {
		final List<ExportColumnHeader> exportColumnHeaders = this.exportAdvanceListServiceImpl.generateAdvanceListColumnHeaders(true, "");

		Assert.assertEquals("1st column should be ENTRY NO", exportColumnHeaders.get(0).getId().intValue(), TermId.ENTRY_NO.getId());
		Assert.assertEquals("2nd column should be DESIG", exportColumnHeaders.get(1).getId().intValue(), TermId.DESIG.getId());
		Assert.assertEquals("3rd column should be CROSS", exportColumnHeaders.get(2).getId().intValue(), TermId.CROSS.getId());
		Assert.assertEquals("4th column should be GID", exportColumnHeaders.get(3).getId().intValue(), TermId.GID.getId());
		Assert.assertEquals("5th column should be SOURCE", exportColumnHeaders.get(4).getId().intValue(), TermId.SOURCE.getId());
		Assert.assertEquals("6th column should be DUPLICATE", exportColumnHeaders.get(5).getId().intValue(), TermId.DUPLICATE.getId());
		Assert.assertEquals("7th column should be BULK WITH", exportColumnHeaders.get(6).getId().intValue(), TermId.BULK_WITH.getId());
		Assert.assertEquals("8th column should be BULK COMPL", exportColumnHeaders.get(7).getId().intValue(), TermId.BULK_COMPL.getId());
		Assert.assertEquals("9th column should be LOCATION_ID", exportColumnHeaders.get(8).getId().intValue(), TermId.LOCATION_ID.getId());
		Assert.assertEquals("10th column should be LOCATION_ABBR", exportColumnHeaders.get(9).getId().intValue(), TermId.LOCATION_ABBR.getId());
		Assert.assertEquals("11th column should be SEED_AMOUNT_G", exportColumnHeaders.get(10).getId().intValue(), TermId.SEED_AMOUNT_G.getId());
		Assert.assertEquals("12th column should be STOCKID", exportColumnHeaders.get(11).getId().intValue(), TermId.STOCKID.getId());
		Assert.assertEquals("13th column should be INVENTORY_COMMENT", exportColumnHeaders.get(12).getId().intValue(),
				AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt());
	}

	@Test
	public void testGetFileNamePath() throws IOException {
		final String fileName = "TestName.xls";
		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.getFileNamePath(fileName);

		final List<File> outputDirectoryFiles = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(1, outputDirectoryFiles.size());
		Assert.assertEquals("Should have the same full file name path", outputDirectoryFiles.get(0).getAbsolutePath() + File.separator + fileName,
				exportInfo.getFilePath());
		Assert.assertEquals("Should have the same file name", fileName, exportInfo.getDownloadFileName());

	}

	private List<File> getTempOutputDirectoriesGenerated() {
		final String genericOutputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final String toolDirectory = genericOutputDirectoryPath.substring(0, genericOutputDirectoryPath.indexOf(InstallationDirectoryUtil.OUTPUT));
		final File toolDirectoryFile = new File(toolDirectory);
		Assert.assertTrue(toolDirectoryFile.exists());
		final List<File> outputDirectoryFiles = new ArrayList<>();
		for (final File file : toolDirectoryFile.listFiles()) {
			if (file.getName().startsWith("output") && file.getName() != InstallationDirectoryUtil.OUTPUT && file.isDirectory()) {
				outputDirectoryFiles.add(file);
			}
		}
		return outputDirectoryFiles;
	}

	@Test
	public void testZipFileNameList() throws IOException {
		final List<String> filenameList = new ArrayList<String>();
		filenameList.add("temp.csv");
		filenameList.add("temp1.csv");
		final File file = new File("temp.csv");
		final File file2 = new File("temp1.csv");
		file.createNewFile();
		file2.createNewFile();
		file.deleteOnExit();
		file2.deleteOnExit();
		final String zipFilenameWithoutExtension = "Test Zip File";
		final String zipFilePath = this.exportAdvanceListServiceImpl.zipFileNameList(zipFilenameWithoutExtension, filenameList);
		
		// Check location of zip file created
		final String outputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		Assert.assertEquals(outputDirectoryFile, new File(zipFilePath).getParentFile());
		// Check # of file in zip file created
		final ZipFile zf = new ZipFile(zipFilePath);
		Assert.assertEquals("There should be 2 files in the zip file", 2, zf.size());
		zf.close();
		
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvMoreThan1AdvanceItem() throws IOException {
		final String filenameWithoutExtension = this.studyName + "-" + AppConstants.ADVANCE_ZIP_DEFAULT_FILENAME.getString();
		final String expectedFilename = filenameWithoutExtension + ".zip";
		final String expectedZipFilePath = "./someDirectory/output/" + expectedFilename;

		// Need to spy so that actual zipping of file won't be done to prevent error since CSV files are not actually existing
		final ExportAdvanceListServiceImpl spyComponent = Mockito.spy(this.exportAdvanceListServiceImpl);
		Mockito.doReturn(expectedZipFilePath).when(spyComponent).zipFileNameList(Matchers.eq(filenameWithoutExtension), Matchers.anyListOf(String.class));
		final FileExportInfo exportInfo = spyComponent.exportAdvanceGermplasmList(this.advancedListIds, this.studyName,
				this.germplasmExportServiceImpl, AppConstants.EXPORT_ADVANCE_STUDY_CSV.getString());
		final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(spyComponent).zipFileNameList(filenameCaptor.capture(), filenameListCaptor.capture());
		Assert.assertEquals(filenameWithoutExtension, filenameCaptor.getValue());
		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(LIST_COUNT, outputDirectories.size());
		final List<String> fileList = filenameListCaptor.getValue();
		Assert.assertEquals(LIST_COUNT, fileList.size());
		for (int i = 1; i <= LIST_COUNT; i++) {
			final String filePath = fileList.get(i-1);
			final File outputDirectory = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
			Assert.assertTrue(outputDirectories.contains(outputDirectory));
			final String filename = LIST_NAME_PREFIX + i + AppConstants.EXPORT_CSV_SUFFIX.getString();
			Assert.assertTrue(filePath.endsWith(filename));
		}
		Assert.assertEquals(expectedZipFilePath, exportInfo.getFilePath());
		Assert.assertEquals(expectedFilename, exportInfo.getDownloadFileName());

	}

	@Test
	public void testExportAdvanceGermplasmListInCsvOnly1Item() throws IOException {
		final String listId = "1";
		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList(listId, this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_STUDY_CSV.getString());
		
		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(1, outputDirectories.size());
		final String filePath = exportInfo.getFilePath();
		final File outputDirectory = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
		Assert.assertTrue(outputDirectories.contains(outputDirectory));
		final String expectedFilename = LIST_NAME_PREFIX + listId + AppConstants.EXPORT_CSV_SUFFIX.getString();
		Assert.assertTrue(filePath.endsWith(expectedFilename));
		Assert.assertEquals(expectedFilename, exportInfo.getDownloadFileName());
		
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvThrowsIOException() throws IOException {
		Mockito.when(this.germplasmExportServiceImpl.generateCSVFile(Matchers.anyListOf(ExportRow.class), Matchers.anyListOf(
				ExportColumnHeader.class), Matchers.anyString()))
				.thenThrow(new IOException());
		final String listId = "1";
		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList(listId, this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_STUDY_CSV.getString());
		
		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(1, outputDirectories.size());
		final String filePath = exportInfo.getFilePath();
		final File outputDirectory = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
		Assert.assertTrue(outputDirectories.contains(outputDirectory));
		final String expectedFilename = LIST_NAME_PREFIX + listId + AppConstants.EXPORT_CSV_SUFFIX.getString();
		Assert.assertTrue(filePath.endsWith(expectedFilename));
		Assert.assertEquals(expectedFilename, exportInfo.getDownloadFileName());
		
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvThrowsMiddlewareException() throws IOException {
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(Matchers.anyInt()))
				.thenThrow(new MiddlewareQueryException("error"));

		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_STUDY_CSV.getString());
		Assert.assertEquals("Should return noFile since there was an error", ExportAdvanceListServiceImpl.NO_FILE, exportInfo.getFilePath());
		Assert.assertNull(exportInfo.getDownloadFileName());
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsOnly1Item() throws IOException {
		final String listId = "1";
		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList(listId, this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_STUDY_EXCEL.getString());
		
		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(1, outputDirectories.size());
		final String filePath = exportInfo.getFilePath();
		final File outputDirectory = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
		Assert.assertTrue(outputDirectories.contains(outputDirectory));
		final String expectedFilename = LIST_NAME_PREFIX + listId + AppConstants.EXPORT_XLS_SUFFIX.getString();
		Assert.assertTrue(filePath.endsWith(expectedFilename));
		Assert.assertEquals(expectedFilename, exportInfo.getDownloadFileName());
		
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsThrowsIOException() throws IOException {
		Mockito.when(this.germplasmExportServiceImpl.generateExcelFileForSingleSheet(Matchers.anyListOf(ExportRow.class), Matchers.anyListOf(
				ExportColumnHeader.class),
				Matchers.anyString(), Matchers.anyString())).thenThrow(new IOException());

		final String listId = "1";
		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList(listId, this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_STUDY_EXCEL.getString());
		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(1, outputDirectories.size());
		final String filePath = exportInfo.getFilePath();
		final File outputDirectory = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
		Assert.assertTrue(outputDirectories.contains(outputDirectory));
		final String expectedFilename = LIST_NAME_PREFIX + listId + AppConstants.EXPORT_XLS_SUFFIX.getString();
		Assert.assertTrue(filePath.endsWith(expectedFilename));
		Assert.assertEquals(expectedFilename, exportInfo.getDownloadFileName());
		
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsThrowsMiddlewareException() throws IOException {
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(Matchers.anyInt()))
				.thenThrow(new MiddlewareQueryException("error"));

		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_STUDY_EXCEL.getString());
		Assert.assertEquals("Should return noFile since there was an error", ExportAdvanceListServiceImpl.NO_FILE, exportInfo.getFilePath());
		Assert.assertNull(exportInfo.getDownloadFileName());
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsMoreThan1AdvanceItem() throws IOException {
		final String filenameWithoutExtension = this.studyName + "-" + AppConstants.ADVANCE_ZIP_DEFAULT_FILENAME.getString();
		final String expectedFilename = filenameWithoutExtension + ".zip";
		final String expectedZipFilePath = "./someDirectory/output/" + expectedFilename;

		// Need to spy so that actual zipping of file won't be done to prevent error since Excel files are not actually existing
		final ExportAdvanceListServiceImpl spyComponent = Mockito.spy(this.exportAdvanceListServiceImpl);
		Mockito.doReturn(expectedZipFilePath).when(spyComponent).zipFileNameList(Matchers.eq(filenameWithoutExtension), Matchers.anyListOf(String.class));
		final FileExportInfo exportInfo = spyComponent.exportAdvanceGermplasmList(this.advancedListIds, this.studyName,
				this.germplasmExportServiceImpl, AppConstants.EXPORT_ADVANCE_STUDY_EXCEL.getString());
		final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(spyComponent).zipFileNameList(filenameCaptor.capture(), filenameListCaptor.capture());
		Assert.assertEquals(filenameWithoutExtension, filenameCaptor.getValue());
		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(LIST_COUNT, outputDirectories.size());
		final List<String> fileList = filenameListCaptor.getValue();
		Assert.assertEquals(LIST_COUNT, fileList.size());
		for (int i = 1; i <= LIST_COUNT; i++) {
			final String filePath = fileList.get(i-1);
			final File outputDirectory = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
			Assert.assertTrue(outputDirectories.contains(outputDirectory));
			final String filename = LIST_NAME_PREFIX + i + AppConstants.EXPORT_XLS_SUFFIX.getString();
			Assert.assertTrue(filePath.endsWith(filename));
		}
		Assert.assertEquals(expectedZipFilePath, exportInfo.getFilePath());
		Assert.assertEquals(expectedFilename, exportInfo.getDownloadFileName());

	}
	
	@Test
	public void testExportStockList() throws IOException {
		final int stockListId = 1;
		Mockito.doReturn(this.inventoryDetailsList).when(this.inventoryMiddlewareService).getInventoryListByListDataProjectListId(Matchers.anyInt());
		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportStockList(stockListId, this.germplasmExportServiceImpl);
		
		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(1, outputDirectories.size());
		final String filePath = exportInfo.getFilePath();
		final File outputDirectory = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
		Assert.assertTrue(outputDirectories.contains(outputDirectory));
		final String expectedFilename = LIST_NAME_PREFIX + stockListId + AppConstants.EXPORT_XLS_SUFFIX.getString();
		Assert.assertTrue(filePath.endsWith(expectedFilename));
		Assert.assertEquals(expectedFilename, exportInfo.getDownloadFileName());
		
	}
	
	@Test
	public void testExportStockListThrowsMiddlewareException() throws IOException {
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(Matchers.anyInt()))
				.thenThrow(new MiddlewareQueryException("error"));

		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportStockList(1, this.germplasmExportServiceImpl);
		Assert.assertEquals("Should return noFile since there was an error", ExportAdvanceListServiceImpl.NO_FILE, exportInfo.getFilePath());
		Assert.assertNull(exportInfo.getDownloadFileName());
	}

	@Test
	public void testGetInventoryDetailValueInfo() {
		final Integer[] columnHeaderIds = new Integer[] {TermId.ENTRY_NO.getId(), TermId.DESIG.getId(), TermId.CROSS.getId(),
				TermId.GID.getId(), TermId.SOURCE.getId(), TermId.DUPLICATE.getId(), TermId.BULK_WITH.getId(), TermId.BULK_COMPL.getId(),
				TermId.LOCATION_ID.getId(), TermId.SEED_AMOUNT_G.getId(), AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt()};
		final InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 1);
		for (int i = 0; i < columnHeaderIds.length; i++) {
			final String result = this.exportAdvanceListServiceImpl.getInventoryDetailValueInfo(inventoryDetails, columnHeaderIds[i]);
			Assert.assertFalse("Inventory detail value should not be empty", "".equals(result));
		}
	}

	@Test
	public void testGetInventoryDetailValueInfoNotInCondition() {
		final InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 1);
		final String result = this.exportAdvanceListServiceImpl.getInventoryDetailValueInfo(inventoryDetails, 0);
		Assert.assertEquals("Should return empty string since there is not matching condition for the ID", "", result);
	}

	@Test
	public void testGetAmountsHeader() {
		final String amountsHeader = this.exportAdvanceListServiceImpl.getAmountsHeader(this.inventoryDetailsList);
		Assert.assertEquals("The amounts header should be " + ExportAdvanceListServiceImplTest.SEED_AMOUNT_KG,
				ExportAdvanceListServiceImplTest.SEED_AMOUNT_KG, amountsHeader);
	}

	private List<InventoryDetails> generateSampleInventoryDetailsList(final int rows) {
		final List<InventoryDetails> inventoryDetailList = new ArrayList<InventoryDetails>();
		for (int i = 0; i < rows; i++) {
			inventoryDetailList.add(this.getSampleInventoryDetails(i, i));
		}
		return inventoryDetailList;
	}

	private InventoryDetails getSampleInventoryDetails(final int gid, final int entryId) {
		final InventoryDetails inventoryDetails =
				new InventoryDetails(gid, "germplasmName", 1, 1, "locationName", 1, 2.0, 1, "sourceName", 1, "scaleName", "comment");
		inventoryDetails.setEntryId(1);
		inventoryDetails.setDuplicate("Plot Dupe: " + entryId + 1);
		inventoryDetails.setBulkWith("SID-" + (entryId + 1) + "1");
		inventoryDetails.setBulkCompl("Y");
		inventoryDetails.setScaleName(ExportAdvanceListServiceImplTest.SEED_AMOUNT_KG);
		return inventoryDetails;
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
