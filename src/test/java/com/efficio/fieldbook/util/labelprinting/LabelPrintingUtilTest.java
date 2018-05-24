package com.efficio.fieldbook.util.labelprinting;

import java.util.List;

import org.generationcp.middleware.data.initializer.ListEntryLotDetailsTestDataInitializer;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.util.AppConstants;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class LabelPrintingUtilTest {
	
	@InjectMocks
	private LabelPrintingUtil labelPrintingUtil;
	
	@Test
	public void testGetListOfIDsForStockIds() {
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer.createListEntryLotDetailsList(2);
		lotRows.get(1).setWithdrawalStatus(GermplasmInventory.COMMITTED);
		final String stockIds = this.labelPrintingUtil.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID);
		Assert.assertEquals(lotRows.get(0).getStockIds(), stockIds);
	}
	
	@Test
	public void testGetListOfIDsForLotss() {
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer.createListEntryLotDetailsList(2);
		lotRows.get(1).setWithdrawalStatus(GermplasmInventory.COMMITTED);
		final String lots = this.labelPrintingUtil.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_SEED_LOT_ID);
		Assert.assertEquals(lotRows.get(0).getLotId().toString(), lots);
	}
}
