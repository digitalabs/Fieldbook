
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.ExportService;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;

public class ExportAdvanceListServiceImplTest {

	private ExportAdvanceListServiceImpl exportAdvanceListServiceImpl;
	private FieldbookProperties fieldbookProperties;
	private InventoryService inventoryMiddlewareService;
	private FieldbookService fieldbookMiddlewareService;
	private String advancedListIds;
	private final String tempDirectory = "";
	private final String studyName = "StudyName";
	private ExportService exportServiceImpl;

	@Before
	public void setUp() throws MiddlewareQueryException {
		this.exportAdvanceListServiceImpl = Mockito.spy(new ExportAdvanceListServiceImpl());
		this.fieldbookProperties = Mockito.mock(FieldbookProperties.class);
		this.inventoryMiddlewareService = Mockito.mock(InventoryService.class);
		this.fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		this.exportServiceImpl = Mockito.mock(ExportService.class);

		Mockito.when(this.fieldbookProperties.getUploadDirectory()).thenReturn(this.tempDirectory);
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("TempGermplasmListName");
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(Matchers.anyInt())).thenReturn(germplasmList);

		this.exportAdvanceListServiceImpl.setFieldbookProperties(this.fieldbookProperties);
		this.exportAdvanceListServiceImpl.setInventoryMiddlewareService(this.inventoryMiddlewareService);
		this.exportAdvanceListServiceImpl.setMessageSource(Mockito.mock(MessageSource.class));
		this.exportAdvanceListServiceImpl.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		this.advancedListIds = "1|2|3";
	}

	@Test
	public void testParseDelimitedAdvanceGermplasmListIds() {
		List<Integer> advanceIds = this.exportAdvanceListServiceImpl.parseDelimitedAdvanceGermplasmListIds(this.advancedListIds);
		Assert.assertEquals("There should be 3 advance germplasm ids", 3, advanceIds.size());
		Assert.assertEquals("1st ID should be 1", 1, advanceIds.get(0).intValue());
		Assert.assertEquals("2nd ID should be 2", 2, advanceIds.get(1).intValue());
		Assert.assertEquals("3rd ID should be 3", 3, advanceIds.get(2).intValue());
	}

	@Test
	public void testGenerateAdvanceListColumnValues() {
		List<Map<Integer, ExportColumnValue>> exportColumnsValuesList =
				this.exportAdvanceListServiceImpl.generateAdvanceListColumnValues(this.generateSampleInventoryDetailsList(5),
						this.exportAdvanceListServiceImpl.generateAdvanceListColumnHeaders(false));
		Assert.assertEquals("There should be 5 set of column values", 5, exportColumnsValuesList.size());
		// we check random data
		Assert.assertEquals("The 1st GID should be 0", "0", exportColumnsValuesList.get(0).get(TermId.GID.getId()).getValue());
		Assert.assertEquals("The 2nd GID should be 1", "1", exportColumnsValuesList.get(1).get(TermId.GID.getId()).getValue());
		Assert.assertEquals("The 3rd GID should be 2", "2", exportColumnsValuesList.get(2).get(TermId.GID.getId()).getValue());
		Assert.assertEquals("The 4th GID should be 3", "3", exportColumnsValuesList.get(3).get(TermId.GID.getId()).getValue());
		Assert.assertEquals("The 5th GID should be 4", "4", exportColumnsValuesList.get(4).get(TermId.GID.getId()).getValue());
	}

	@Test
	public void testGetInventoryAmountWhenAmountIsNull() {
		InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setAmount(null);

		Assert.assertEquals("The Amount should return be empty string", "",
				this.exportAdvanceListServiceImpl.getInventoryAmount(inventoryDetails));
	}

	@Test
	public void testGetInventoryAmountWhenAmountIsNonNull() {
		InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setAmount(2.22);

		Assert.assertEquals("The Amount should be 2.22", "2.22", this.exportAdvanceListServiceImpl.getInventoryAmount(inventoryDetails));
	}

	@Test
	public void testGetInventoryScaleWhenScaleIsNull() {
		InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setScaleName(null);

		Assert.assertEquals("The Scale should return be empty string", "",
				this.exportAdvanceListServiceImpl.getInventoryValue(inventoryDetails.getScaleName()));
	}

	@Test
	public void testGetInventoryScaleWhenScaleIsNonNull() {
		String scaleName = "scaleTest";
		InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setScaleName(scaleName);

		Assert.assertEquals("The Scale should be " + scaleName, scaleName,
				this.exportAdvanceListServiceImpl.getInventoryValue(inventoryDetails.getScaleName()));
	}

	@Test
	public void testGetInventoryCommentWhenCommentIsNull() {
		InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setComment(null);

		Assert.assertEquals("The Comment should return be empty string", "",
				this.exportAdvanceListServiceImpl.getInventoryValue(inventoryDetails.getComment()));
	}

	@Test
	public void testGetInventoryCommentWhenCommentIsNonNull() {
		String comment = "commentTest";
		InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 2);
		inventoryDetails.setComment(comment);

		Assert.assertEquals("The Comment should be " + comment, comment,
				this.exportAdvanceListServiceImpl.getInventoryValue(inventoryDetails.getComment()));
	}

	@Test
	public void testGenerateAdvanceListColumnHeaders() {
		List<ExportColumnHeader> exportColumnHeaders = this.exportAdvanceListServiceImpl.generateAdvanceListColumnHeaders(false);

		Assert.assertEquals("1st column should be ENTRY NO", exportColumnHeaders.get(0).getId().intValue(), TermId.ENTRY_NO.getId());
		Assert.assertEquals("2nd column should be DESIG", exportColumnHeaders.get(1).getId().intValue(), TermId.DESIG.getId());
		Assert.assertEquals("3rd column should be CROSS", exportColumnHeaders.get(2).getId().intValue(), TermId.CROSS.getId());
		Assert.assertEquals("4th column should be GID", exportColumnHeaders.get(3).getId().intValue(), TermId.GID.getId());
		Assert.assertEquals("5th column should be SOURCE", exportColumnHeaders.get(4).getId().intValue(), TermId.SOURCE.getId());
		Assert.assertEquals("6th column should be LOCATION_ID", exportColumnHeaders.get(5).getId().intValue(), TermId.LOCATION_ID.getId());
		Assert.assertEquals("7th column should be INVENTORY_AMOUNT", exportColumnHeaders.get(6).getId().intValue(),
				AppConstants.TEMPORARY_INVENTORY_AMOUNT.getInt());
		Assert.assertEquals("8th column should be INVENTORY_SCALE", exportColumnHeaders.get(7).getId().intValue(),
				AppConstants.TEMPORARY_INVENTORY_SCALE.getInt());
		Assert.assertEquals("9th column should be INVENTORY_COMMENT", exportColumnHeaders.get(8).getId().intValue(),
				AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt());
	}

	@Test
	public void testGenerateAdvanceListColumnHeadersForCrosses() {
		List<ExportColumnHeader> exportColumnHeaders = this.exportAdvanceListServiceImpl.generateAdvanceListColumnHeaders(true);

		Assert.assertEquals("1st column should be ENTRY NO", exportColumnHeaders.get(0).getId().intValue(), TermId.ENTRY_NO.getId());
		Assert.assertEquals("2nd column should be DESIG", exportColumnHeaders.get(1).getId().intValue(), TermId.DESIG.getId());
		Assert.assertEquals("3rd column should be CROSS", exportColumnHeaders.get(2).getId().intValue(), TermId.CROSS.getId());
		Assert.assertEquals("4th column should be GID", exportColumnHeaders.get(3).getId().intValue(), TermId.GID.getId());
		Assert.assertEquals("5th column should be SOURCE", exportColumnHeaders.get(4).getId().intValue(), TermId.SOURCE.getId());
		Assert.assertEquals("6th column should be DUPLICATE", exportColumnHeaders.get(5).getId().intValue(), TermId.DUPLICATE.getId());
		Assert.assertEquals("7th column should be BULK WITH", exportColumnHeaders.get(6).getId().intValue(), TermId.BULK_WITH.getId());
		Assert.assertEquals("8th column should be BULK COMPL", exportColumnHeaders.get(7).getId().intValue(), TermId.BULK_COMPL.getId());
		Assert.assertEquals("9th column should be LOCATION_ID", exportColumnHeaders.get(8).getId().intValue(), TermId.LOCATION_ID.getId());
		Assert.assertEquals("10th column should be INVENTORY_AMOUNT", exportColumnHeaders.get(9).getId().intValue(),
				AppConstants.TEMPORARY_INVENTORY_AMOUNT.getInt());
		Assert.assertEquals("11th column should be INVENTORY_SCALE", exportColumnHeaders.get(10).getId().intValue(),
				AppConstants.TEMPORARY_INVENTORY_SCALE.getInt());
		Assert.assertEquals("12th column should be INVENTORY_COMMENT", exportColumnHeaders.get(11).getId().intValue(),
				AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt());
	}

	@Test
	public void testGetFileNamePath() {
		String fileName = "TestName";
		String fileNamePath = this.exportAdvanceListServiceImpl.getFileNamePath(fileName);

		Assert.assertEquals("Should have the same full file name path", fileNamePath, File.separator + fileName);
	}

	@Test
	public void testZipFileNameList() throws IOException {
		List<String> filenameList = new ArrayList<String>();
		filenameList.add("temp.csv");
		filenameList.add("temp1.csv");
		File file = new File("temp.csv");
		File file2 = new File("temp1.csv");
		File fileZip = new File("Test.zip");
		file.createNewFile();
		file2.createNewFile();
		file.deleteOnExit();
		file2.deleteOnExit();
		fileZip.deleteOnExit();
		this.exportAdvanceListServiceImpl.zipFileNameList(fileZip.getName(), filenameList);
		ZipFile zf = new ZipFile(fileZip);
		Assert.assertEquals("There should be 2 files in the zip file", 2, zf.size());
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvMoreThan1AdvanceItem() throws IOException {
		Mockito.doReturn(true).when(this.exportAdvanceListServiceImpl).zipFileNameList(Matchers.anyString(), Matchers.anyList());
		Mockito.doReturn("TempGermplasmListName").when(this.exportAdvanceListServiceImpl).getFileNamePath(Matchers.anyString());
		Mockito.when(this.exportServiceImpl.generateCSVFile(Matchers.anyList(), Matchers.anyList(), Matchers.anyString())).thenReturn(
				new File("Temp"));

		File file =
				this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList(this.advancedListIds, this.studyName, this.exportServiceImpl,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString());
		Assert.assertTrue("Return should be a zip file", file.getAbsolutePath().indexOf(".zip") != -1);
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvOnly1Item() throws IOException {
		Mockito.doReturn("TempGermplasmListName").when(this.exportAdvanceListServiceImpl).getFileNamePath(Matchers.anyString());
		Mockito.when(this.exportServiceImpl.generateCSVFile(Matchers.anyList(), Matchers.anyList(), Matchers.anyString())).thenReturn(
				new File("Temp"));

		File file =
				this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.exportServiceImpl,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString());
		Assert.assertTrue("Return should be a csv file", file.getAbsolutePath().indexOf(".csv") != -1);
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvThrowsIOException() throws MiddlewareQueryException, IOException {
		Mockito.doReturn("TempGermplasmListName").when(this.exportAdvanceListServiceImpl).getFileNamePath(Matchers.anyString());
		Mockito.when(this.exportServiceImpl.generateCSVFile(Matchers.anyList(), Matchers.anyList(), Matchers.anyString())).thenThrow(
				new IOException());
		File file =
				this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.exportServiceImpl,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString());
		Assert.assertEquals("Should return noFile since there was an error", file.getName(), "noFile");
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvThrowsMiddlewareException() throws MiddlewareQueryException, IOException {
		Mockito.doReturn("TempGermplasmListName").when(this.exportAdvanceListServiceImpl).getFileNamePath(Matchers.anyString());
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(Matchers.anyInt())).thenThrow(
				new MiddlewareQueryException("error"));

		File file =
				this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.exportServiceImpl,
						AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString());
		Assert.assertEquals("Should return noFile since there was an error", file.getName(), "noFile");
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsOnly1Item() throws IOException {
		Mockito.doReturn("TempGermplasmListName").when(this.exportAdvanceListServiceImpl).getFileNamePath(Matchers.anyString());
		Mockito.when(
				this.exportServiceImpl.generateExcelFileForSingleSheet(Matchers.anyList(), Matchers.anyList(), Matchers.anyString(),
						Matchers.anyString())).thenReturn(new FileOutputStream(new File("temp")));

		File file =
				this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.exportServiceImpl,
						AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString());
		Assert.assertTrue("Return should be a xls file", file.getAbsolutePath().indexOf(".xls") != -1);
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsThrowsIOException() throws IOException {
		Mockito.doReturn("TempGermplasmListName").when(this.exportAdvanceListServiceImpl).getFileNamePath(Matchers.anyString());
		Mockito.when(
				this.exportServiceImpl.generateExcelFileForSingleSheet(Matchers.anyList(), Matchers.anyList(), Matchers.anyString(),
						Matchers.anyString())).thenThrow(new IOException());

		File file =
				this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.exportServiceImpl,
						AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString());
		Assert.assertEquals("Should return noFile since there was an error", file.getName(), "noFile");
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsThrowsMiddlewareException() throws IOException, MiddlewareQueryException {
		Mockito.doReturn("TempGermplasmListName").when(this.exportAdvanceListServiceImpl).getFileNamePath(Matchers.anyString());
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(Matchers.anyInt())).thenThrow(
				new MiddlewareQueryException("error"));

		File file =
				this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.exportServiceImpl,
						AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString());
		Assert.assertEquals("Should return noFile since there was an error", file.getName(), "noFile");
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsMoreThan1AdvanceItem() throws IOException {
		Mockito.doReturn(true).when(this.exportAdvanceListServiceImpl).zipFileNameList(Matchers.anyString(), Matchers.anyList());
		Mockito.doReturn("TempGermplasmListName").when(this.exportAdvanceListServiceImpl).getFileNamePath(Matchers.anyString());
		Mockito.when(
				this.exportServiceImpl.generateExcelFileForSingleSheet(Matchers.anyList(), Matchers.anyList(), Matchers.anyString(),
						Matchers.anyString())).thenReturn(new FileOutputStream(new File("temp")));

		File file =
				this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList(this.advancedListIds, this.studyName, this.exportServiceImpl,
						AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString());
		Assert.assertTrue("Return should be a zip file", file.getAbsolutePath().indexOf(".zip") != -1);
	}

	@Test
	public void testGetInventoryDetailValueInfo() {
		Integer[] columnHeaderIds =
				new Integer[] {TermId.ENTRY_NO.getId(), TermId.DESIG.getId(), TermId.CROSS.getId(), TermId.GID.getId(),
						TermId.SOURCE.getId(), TermId.DUPLICATE.getId(), TermId.BULK_WITH.getId(), TermId.BULK_COMPL.getId(),
						TermId.LOCATION_ID.getId(), AppConstants.TEMPORARY_INVENTORY_AMOUNT.getInt(),
						AppConstants.TEMPORARY_INVENTORY_SCALE.getInt(), AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt()};
		InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 1);
		for (int i = 0; i < columnHeaderIds.length; i++) {
			String result = this.exportAdvanceListServiceImpl.getInventoryDetailValueInfo(inventoryDetails, columnHeaderIds[i]);
			Assert.assertFalse("Inventory detail value should not be empty", "".equals(result));
		}
	}

	@Test
	public void testGetInventoryDetailValueInfoNotInCondition() {
		InventoryDetails inventoryDetails = this.getSampleInventoryDetails(1, 1);
		String result = this.exportAdvanceListServiceImpl.getInventoryDetailValueInfo(inventoryDetails, 0);
		Assert.assertEquals("Should return empty string since there is not matching condition for the ID", "", result);
	}

	private List<InventoryDetails> generateSampleInventoryDetailsList(int rows) {
		List<InventoryDetails> inventoryDetailList = new ArrayList<InventoryDetails>();
		for (int i = 0; i < rows; i++) {
			inventoryDetailList.add(this.getSampleInventoryDetails(i, i));
		}
		return inventoryDetailList;
	}

	private InventoryDetails getSampleInventoryDetails(int gid, int entryId) {
		InventoryDetails inventoryDetails =
				new InventoryDetails(gid, "germplasmName", 1, 1, "locationName", 1, 2.0, 1, "sourceName", 1, "scaleName", "comment");
		inventoryDetails.setEntryId(1);
		inventoryDetails.setDuplicate("Plot Dupe: " + entryId + 1);
		inventoryDetails.setBulkWith("SID-" + (entryId + 1) + "1");
		inventoryDetails.setBulkCompl("Y");
		return inventoryDetails;
	}
}
