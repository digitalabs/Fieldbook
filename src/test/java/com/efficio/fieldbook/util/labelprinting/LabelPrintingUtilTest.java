package com.efficio.fieldbook.util.labelprinting;

import java.util.List;

import org.generationcp.middleware.data.initializer.GermplasmListDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.ListEntryLotDetailsTestDataInitializer;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class LabelPrintingUtilTest {

	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private LabelPrintingUtil labelPrintingUtil;

	@Before
	public void setUp() {
		Mockito.when(this.messageSource.getMessage("label.printing.available.fields.entry.num", null,
				LocaleContextHolder.getLocale())).thenReturn("ENTRY NO");

	}

	@Test
	public void testGetListOfIDsForStockIds() {
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(2);
		lotRows.get(1).setWithdrawalStatus(GermplasmInventory.COMMITTED);
		final String stockIds = this.labelPrintingUtil.getListOfIDs(lotRows,
				AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID);
		Assert.assertEquals(lotRows.get(0).getStockIds(), stockIds);
	}

	@Test
	public void testGetListOfIDsForLotss() {
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(2);
		lotRows.get(1).setWithdrawalStatus(GermplasmInventory.COMMITTED);
		final String lots = this.labelPrintingUtil.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_SEED_LOT_ID);
		Assert.assertEquals(lotRows.get(0).getLotId().toString(), lots);
	}

	@Test
	public void testGetSelectedFieldValueForEntryId() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt(), germplasmListData, new UserLabelPrinting());
		Assert.assertEquals(germplasmListData.getEntryId().toString(), selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueForEntryIdWithHeader() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt(), germplasmListData, new UserLabelPrinting(),
				true);
		Assert.assertEquals("ENTRY NO : " + germplasmListData.getEntryId().toString(), selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueForListName() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setName("LIST NAME");
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_FIELDS_LIST_NAME.getInt(), germplasmListData, userLabelPrinting);
		Assert.assertEquals(userLabelPrinting.getName(), selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueForGID() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt(), germplasmListData, new UserLabelPrinting());
		Assert.assertEquals(germplasmListData.getGid().toString(), selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueForDesignation() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_FIELDS_DESIGNATION.getInt(), germplasmListData, new UserLabelPrinting());
		Assert.assertEquals(germplasmListData.getDesignation(), selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueForGroupName() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_FIELDS_CROSS.getInt(), germplasmListData, new UserLabelPrinting());
		Assert.assertEquals(germplasmListData.getGroupName(), selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueForSeedSource() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_SEED_SOURCE.getInt(), germplasmListData, new UserLabelPrinting());
		Assert.assertEquals(germplasmListData.getSeedSource(), selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueForStockIds() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(2);
		lotRows.get(1).setWithdrawalStatus(GermplasmInventory.COMMITTED);
		germplasmListData.getInventoryInfo().setLotRows(lotRows);
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getInt(), germplasmListData, new UserLabelPrinting());
		Assert.assertEquals(lotRows.get(0).getStockIds(), selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueForStockIdsEmptyString() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		germplasmListData.setInventoryInfo(new ListDataInventory(1, 1));
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getInt(), germplasmListData, new UserLabelPrinting());
		Assert.assertEquals("", selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueForLotIds() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(2);
		lotRows.get(1).setWithdrawalStatus(GermplasmInventory.COMMITTED);
		germplasmListData.getInventoryInfo().setLotRows(lotRows);
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt(), germplasmListData, new UserLabelPrinting());
		Assert.assertEquals(lotRows.get(0).getLotId().toString(), selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueForLotIdsEmptyString() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		germplasmListData.setInventoryInfo(new ListDataInventory(1, 1));
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt(), germplasmListData, new UserLabelPrinting());
		Assert.assertEquals("", selectedFieldValue);
	}

	@Test
	public void testGetSelectedFieldValueBarcodes() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_FIELDS_LIST_NAME.getString());
		userLabelPrinting.setName("LIST NAME");
		final String selectedFieldValue = this.labelPrintingUtil.getSelectedFieldValue(
				AppConstants.AVAILABLE_LABEL_BARCODE.getInt(), germplasmListData, userLabelPrinting);
		Assert.assertEquals(userLabelPrinting.getName(), selectedFieldValue);
	}

	@Test
	public void testGetBarcodeStringForEntryId() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting);
		Assert.assertEquals(germplasmListData.getEntryId().toString(), barcode);
	}

	@Test
	public void testGetBarcodeStringForEntryIdWithHeader() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting, true);
		Assert.assertEquals("ENTRY NO : " + germplasmListData.getEntryId(), barcode);
	}

	@Test
	public void testGetBarcodeStringForListName() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setName("LIST NAME");
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_FIELDS_LIST_NAME.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting);
		Assert.assertEquals(userLabelPrinting.getName(), barcode);
	}

	@Test
	public void testGetBarcodeStringForGID() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_FIELDS_GID.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting);
		Assert.assertEquals(germplasmListData.getGid().toString(), barcode);
	}

	@Test
	public void testGetBarcodeStringForDesignation() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_FIELDS_DESIGNATION.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting);
		Assert.assertEquals(germplasmListData.getDesignation(), barcode);
	}

	@Test
	public void testGetBarcodeStringForGroupName() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_FIELDS_CROSS.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting);
		Assert.assertEquals(germplasmListData.getGroupName(), barcode);
	}

	@Test
	public void testGetBarcodeStringForSeedSource() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_SEED_SOURCE.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting);
		Assert.assertEquals(germplasmListData.getSeedSource(), barcode);
	}

	@Test
	public void testGetBarcodeStringForStockIds() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(2);
		lotRows.get(1).setWithdrawalStatus(GermplasmInventory.COMMITTED);
		germplasmListData.getInventoryInfo().setLotRows(lotRows);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting);
		Assert.assertEquals(lotRows.get(0).getStockIds(), barcode);
	}

	@Test
	public void testGetBarcodeStringForStockIdsEmptyString() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		germplasmListData.setInventoryInfo(new ListDataInventory(1, 1));
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting);
		Assert.assertEquals(" ", barcode);
	}

	@Test
	public void testGetBarcodeStringForLotIds() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(2);
		lotRows.get(1).setWithdrawalStatus(GermplasmInventory.COMMITTED);
		germplasmListData.getInventoryInfo().setLotRows(lotRows);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting);
		Assert.assertEquals(lotRows.get(0).getLotId().toString(), barcode);
	}

	@Test
	public void testGetBarcodeStringForLotIdsEmptyString() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		germplasmListData.setInventoryInfo(new ListDataInventory(1, 1));
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeString(germplasmListData, userLabelPrinting);
		Assert.assertEquals(" ", barcode);
	}

	@Test
	public void testGetBarcodeStringForSeedPrepForStockIds() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeStringForSeedPrep(germplasmListData, userLabelPrinting,
				false, lotRows.get(0));
		Assert.assertEquals(lotRows.get(0).getStockIds(), barcode);
	}

	@Test
	public void testGetBarcodeStringForSeedPrepForLotIds() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(1);
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setFirstBarcodeField(AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getString());
		final String barcode = this.labelPrintingUtil.getBarcodeStringForSeedPrep(germplasmListData, userLabelPrinting,
				false, lotRows.get(0));
		Assert.assertEquals(lotRows.get(0).getLotId().toString(), barcode);
	}
}
