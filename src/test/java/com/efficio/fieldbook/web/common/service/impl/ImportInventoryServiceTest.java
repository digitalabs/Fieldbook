package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.nursery.bean.ImportedInventoryList;
import com.efficio.fieldbook.web.util.parsing.InventoryImportParser;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 3/5/2015
 * Time: 12:16 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class ImportInventoryServiceTest {

	public static final String TEST_GERMPLASM_NAME_1 = "gname1";
	public static final int TEST_ORIGINAL_GID = 1;

	@Mock
	private InventoryService inventoryService;

	@Mock
	private InventoryImportParser parser;

	@InjectMocks
	private ImportInventoryServiceImpl dut;

	@Test
	public void testMergeImportedData() {

		ImportInventoryServiceImpl mole = spy(dut);
		doReturn(false).when(mole).mergeIndividualDetailData(any(InventoryDetails.class), any(InventoryDetails.class));

		List<InventoryDetails> original = new ArrayList<>();
		List<InventoryDetails> imported = new ArrayList<>();

		InventoryDetails orig1 = new InventoryDetails(TEST_ORIGINAL_GID, TEST_GERMPLASM_NAME_1, 1, 1, 10.0, 1, 1, 1);
		original.add(orig1);

		imported.add(new InventoryDetails(3, "TEST", 1, 1, 5.0, 1, 1, 2));

		InventoryDetails imported2 = new InventoryDetails(TEST_ORIGINAL_GID, "TEST3", 1, 1, 5.0, 1, 1, 1);
		imported.add(imported2);
		ImportedInventoryList inventoryList = new ImportedInventoryList(imported, "test");

		ArgumentCaptor<InventoryDetails> param1 = ArgumentCaptor.forClass(InventoryDetails.class);
		ArgumentCaptor<InventoryDetails> param2 = ArgumentCaptor.forClass(InventoryDetails.class);
		boolean overwrite = mole.mergeImportedData(original, inventoryList);
		assertFalse("Service unable to properly compute value of overwrite based on individual merging", overwrite);

		// we verify that individual detail is called only once, since there is only one item in the original list
		// and we use the original list as the control for importing
		verify(mole, atMost(1)).mergeIndividualDetailData(param1.capture(), param2.capture());

		assertEquals("Data merging should only done on items with the same GID", param1.getValue().getGid(), param2.getValue().getGid());

	}

	@Test
	public void testMergeIndividualData() {

	}

}
