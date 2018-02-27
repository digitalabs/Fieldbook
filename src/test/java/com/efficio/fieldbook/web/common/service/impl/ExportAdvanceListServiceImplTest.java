
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
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.util.FileExportInfo;
import com.efficio.fieldbook.web.util.AppConstants;

@RunWith(MockitoJUnitRunner.class)
public class ExportAdvanceListServiceImplTest {

	private static final String SEED_AMOUNT_KG = "SEED_AMOUNT_kg";

	@Mock
	private InventoryService inventoryMiddlewareService;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private GermplasmExportService germplasmExportServiceImpl;

	private String advancedListIds;
	private final String studyName = "StudyName";

	List<InventoryDetails> inventoryDetailsList;

	@InjectMocks
	private ExportAdvanceListServiceImpl exportAdvanceListServiceImpl;

	@Before
	public void setUp() throws MiddlewareQueryException {
		this.inventoryDetailsList = this.generateSampleInventoryDetailsList(5);
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("TempGermplasmListName");
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(Matchers.anyInt())).thenReturn(germplasmList);

		this.exportAdvanceListServiceImpl.setInventoryMiddlewareService(this.inventoryMiddlewareService);
		this.exportAdvanceListServiceImpl.setMessageSource(Mockito.mock(MessageSource.class));
		this.exportAdvanceListServiceImpl.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		this.advancedListIds = "1|2|3";
	}

	@Test
	public void testParseDelimitedAdvanceGermplasmListIds() {
		final List<Integer> advanceIds = this.exportAdvanceListServiceImpl.parseDelimitedAdvanceGermplasmListIds(this.advancedListIds);
		Assert.assertEquals("There should be 3 advance germplasm ids", 3, advanceIds.size());
		Assert.assertEquals("1st ID should be 1", 1, advanceIds.get(0).intValue());
		Assert.assertEquals("2nd ID should be 2", 2, advanceIds.get(1).intValue());
		Assert.assertEquals("3rd ID should be 3", 3, advanceIds.get(2).intValue());
	}

	@Test
	public void testGenerateAdvanceListColumnValues() {
		final List<Map<Integer, ExportColumnValue>> exportColumnsValuesList =
				this.exportAdvanceListServiceImpl.generateAdvanceListColumnValues(this.inventoryDetailsList,
						this.exportAdvanceListServiceImpl.generateAdvanceListColumnHeaders(false, ""));
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
		Assert.assertEquals("7th column should be INVENTORY_AMOUNT", exportColumnHeaders.get(6).getId().intValue(),
				TermId.SEED_AMOUNT_G.getId());
		Assert.assertEquals("8th column should be STOCKID", exportColumnHeaders.get(7).getId().intValue(), TermId.STOCKID.getId());
		Assert.assertEquals("9th column should be INVENTORY_COMMENT", exportColumnHeaders.get(8).getId().intValue(),
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
		Assert.assertEquals("10th column should be INVENTORY_AMOUNT", exportColumnHeaders.get(9).getId().intValue(),
				TermId.SEED_AMOUNT_G.getId());
		Assert.assertEquals("11th column should be STOCKID", exportColumnHeaders.get(10).getId().intValue(), TermId.STOCKID.getId());
		Assert.assertEquals("12th column should be INVENTORY_COMMENT", exportColumnHeaders.get(11).getId().intValue(),
				AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt());
	}

	@Test
	public void testGetFileNamePath() throws IOException {
		final String fileName = "TestName";
		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.getFileNamePath(fileName);

		Assert.assertEquals("Should have the same full file name path", exportInfo.getFilePath(), File.separator + fileName);
	}

	@Test
	public void testZipFileNameList() throws IOException {
		final List<String> filenameList = new ArrayList<String>();
		filenameList.add("temp.csv");
		filenameList.add("temp1.csv");
		final File file = new File("temp.csv");
		final File file2 = new File("temp1.csv");
		final File fileZip = new File("Test.zip");
		file.createNewFile();
		file2.createNewFile();
		file.deleteOnExit();
		file2.deleteOnExit();
		fileZip.deleteOnExit();
		this.exportAdvanceListServiceImpl.zipFileNameList(fileZip.getName(), filenameList);
		final ZipFile zf = new ZipFile(fileZip);
		Assert.assertEquals("There should be 2 files in the zip file", 2, zf.size());
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvMoreThan1AdvanceItem() throws IOException {
		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList(this.advancedListIds, this.studyName,
				this.germplasmExportServiceImpl, AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString());
		Assert.assertTrue("Return should be a zip file", exportInfo.getFilePath().indexOf(".zip") != -1);
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvOnly1Item() throws IOException {
		Mockito.when(this.germplasmExportServiceImpl.generateCSVFile(Matchers.anyList(), Matchers.anyList(), Matchers.anyString()))
				.thenReturn(new File("Temp"));

		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString());
		Assert.assertTrue("Return should be a csv file", exportInfo.getFilePath().indexOf(".csv") != -1);
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvThrowsIOException() throws MiddlewareQueryException, IOException {
		Mockito.when(this.germplasmExportServiceImpl.generateCSVFile(Matchers.anyList(), Matchers.anyList(), Matchers.anyString()))
				.thenThrow(new IOException());
		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString());
		Assert.assertEquals("Should return noFile since there was an error", exportInfo.getDownloadFileName(), "noFile");
	}

	@Test
	public void testExportAdvanceGermplasmListInCsvThrowsMiddlewareException() throws MiddlewareQueryException, IOException {
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(Matchers.anyInt()))
				.thenThrow(new MiddlewareQueryException("error"));

		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString());
		Assert.assertEquals("Should return noFile since there was an error", exportInfo.getDownloadFileName(), "noFile");
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsOnly1Item() throws IOException {
		Mockito.when(this.germplasmExportServiceImpl.generateExcelFileForSingleSheet(Matchers.anyList(), Matchers.anyList(),
				Matchers.anyString(), Matchers.anyString())).thenReturn(new FileOutputStream(new File("temp")));

		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString());
		Assert.assertTrue("Return should be a xls file", exportInfo.getFilePath().indexOf(".xls") != -1);
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsThrowsIOException() throws IOException {
		Mockito.when(this.germplasmExportServiceImpl.generateExcelFileForSingleSheet(Matchers.anyList(), Matchers.anyList(),
				Matchers.anyString(), Matchers.anyString())).thenThrow(new IOException());

		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString());
		Assert.assertEquals("Should return noFile since there was an error", exportInfo.getDownloadFileName(), "noFile");
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsThrowsMiddlewareException() throws IOException {
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(Matchers.anyInt()))
				.thenThrow(new MiddlewareQueryException("error"));

		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList("1", this.studyName, this.germplasmExportServiceImpl,
				AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString());
		Assert.assertEquals("Should return noFile since there was an error", exportInfo.getDownloadFileName(), "noFile");
	}

	@Test
	public void testExportAdvanceGermplasmListInXlsMoreThan1AdvanceItem() throws IOException {
		Mockito.when(this.germplasmExportServiceImpl.generateExcelFileForSingleSheet(Matchers.anyList(), Matchers.anyList(),
				Matchers.anyString(), Matchers.anyString())).thenReturn(new FileOutputStream(new File("temp")));

		final FileExportInfo exportInfo = this.exportAdvanceListServiceImpl.exportAdvanceGermplasmList(this.advancedListIds, this.studyName,
				this.germplasmExportServiceImpl, AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString());
		Assert.assertTrue("Return should be a zip file", exportInfo.getFilePath().indexOf(".zip") != -1);
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
}
