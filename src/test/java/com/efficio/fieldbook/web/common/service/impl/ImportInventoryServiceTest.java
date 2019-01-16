
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.util.parsing.InventoryImportParser;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 3/5/2015 Time: 12:16 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class ImportInventoryServiceTest {

	public static final String TEST_GERMPLASM_NAME_1 = "gname1";
	public static final int TEST_ORIGINAL_GID = 1;

	@Mock
	private InventoryImportParser parser;

	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private ImportInventoryServiceImpl dut;

	@Test
	public void testMergeImportedData() {

		ImportInventoryServiceImpl mole = Mockito.spy(this.dut);
		Mockito.doReturn(false).when(mole).mergeIndividualDetailData(Matchers.any(InventoryDetails.class),
				Matchers.any(InventoryDetails.class));

		List<InventoryDetails> original = this.createOriginalInventoryDetailsList();
		ImportedInventoryList inventoryList = this.createImportedInventoryList();

		ArgumentCaptor<InventoryDetails> param1 = ArgumentCaptor.forClass(InventoryDetails.class);
		ArgumentCaptor<InventoryDetails> param2 = ArgumentCaptor.forClass(InventoryDetails.class);
		boolean overwrite = mole.mergeImportedData(original, inventoryList);
		Assert.assertFalse("Service unable to properly compute value of overwrite based on individual merging", overwrite);

		// we verify that individual detail is called only once, since there is only one item in the original list
		// and we use the original list as the control for importing
		Mockito.verify(mole, Mockito.atMost(1)).mergeIndividualDetailData(param1.capture(), param2.capture());

		Assert.assertEquals("Data merging should only done on items with the same GID", param1.getValue().getGid(),
				param2.getValue().getGid());

	}

	private ImportedInventoryList createImportedInventoryList() {
		List<InventoryDetails> imported = new ArrayList<>();
		imported.add(new InventoryDetails(ImportInventoryServiceTest.TEST_ORIGINAL_GID, "TEST3", 1, 1, 5.0, 1, 1, 1));
		return new ImportedInventoryList(imported, "test");
	}

	private List<InventoryDetails> createOriginalInventoryDetailsList() {
		List<InventoryDetails> original = new ArrayList<>();
		original.add(new InventoryDetails(ImportInventoryServiceTest.TEST_ORIGINAL_GID, ImportInventoryServiceTest.TEST_GERMPLASM_NAME_1, 1,
				1, 10.0, 1, 1, 1));
		original.add(new InventoryDetails(3, "TEST", 1, 1, 5.0, 1, 1, 2));
		return original;
	}

	@Test
	public void testMergeInventoryDetails() throws FieldbookException {
		ImportInventoryServiceImpl mole = Mockito.spy(this.dut);
		GermplasmListType germplasmListType = GermplasmListType.CROSSES;
		List<InventoryDetails> inventoryDetailListFromDB = this.createOriginalInventoryDetailsList();
		ImportedInventoryList importedInventoryList = this.createImportedInventoryList();
		mole.mergeInventoryDetails(inventoryDetailListFromDB, importedInventoryList, germplasmListType);
		InventoryDetails inventoryDetailsFromDB = inventoryDetailListFromDB.get(0);
		InventoryDetails inventoryDetailsFromImport = importedInventoryList.getImportedInventoryDetails().get(0);
		Mockito.verify(mole, Mockito.atMost(1)).updateInventoryDetailsFromImport(inventoryDetailsFromDB, inventoryDetailsFromImport,
				germplasmListType);
		if (GermplasmListType.isCrosses(germplasmListType)) {
			Assert.assertEquals(inventoryDetailsFromImport.getDuplicate(), inventoryDetailsFromDB.getDuplicate());
			Assert.assertEquals(inventoryDetailsFromImport.getBulkWith(), inventoryDetailsFromDB.getBulkWith());
			Assert.assertEquals(inventoryDetailsFromImport.getBulkCompl(), inventoryDetailsFromDB.getBulkCompl());
		}
		Assert.assertEquals(inventoryDetailsFromImport.getLocationId(), inventoryDetailsFromDB.getLocationId());
		Assert.assertEquals(inventoryDetailsFromImport.getScaleId(), inventoryDetailsFromDB.getScaleId());
		Assert.assertEquals(inventoryDetailsFromImport.getAmount(), inventoryDetailsFromDB.getAmount());
		Assert.assertEquals(inventoryDetailsFromImport.getComment(), inventoryDetailsFromDB.getComment());
	}

	@Test
	public void testHasConflictTrue() throws FieldbookException {
		ImportInventoryServiceImpl mole = Mockito.spy(this.dut);
		List<InventoryDetails> inventoryDetailListFromDB = this.createOriginalInventoryDetailsList();
		ImportedInventoryList importedInventoryList = this.createImportedInventoryList();
		Assert.assertTrue(mole.hasConflict(inventoryDetailListFromDB, importedInventoryList));
	}

	@Test
	public void testHasConflictFalse() throws FieldbookException {
		ImportInventoryServiceImpl mole = Mockito.spy(this.dut);
		List<InventoryDetails> inventoryDetailListFromDB = new ArrayList<>();
		inventoryDetailListFromDB.add(new InventoryDetails(ImportInventoryServiceTest.TEST_ORIGINAL_GID, "TEST3", 1, 1, 5.0, 1, 1, 1));
		ImportedInventoryList importedInventoryList = this.createImportedInventoryList();
		Assert.assertFalse(mole.hasConflict(inventoryDetailListFromDB, importedInventoryList));
	}
}
