
package com.efficio.fieldbook.web.util.parsing;

import java.util.Map;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.junit.Assert;
import org.junit.Test;

public class InventoryHeaderLabelsTest {

	@Test
	public void testHeaders_Advanced() {
		Map<InventoryHeaderLabels, Integer> advancedHeadersMap = this.createInventoryHeaderLabelsMap(GermplasmListType.ADVANCED);
		Assert.assertEquals(9, advancedHeadersMap.size());
		Assert.assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.ENTRY));
		Assert.assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.DESIGNATION));
		Assert.assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.PARENTAGE));
		Assert.assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.GID));
		Assert.assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.SOURCE));
		Assert.assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.LOCATION));
		Assert.assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.AMOUNT));
		Assert.assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.SCALE));
		Assert.assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.COMMENT));
	}

	@Test
	public void testHeaders_Crosses() {
		Map<InventoryHeaderLabels, Integer> crossesHeadersMap = InventoryHeaderLabels.headers(GermplasmListType.CROSSES);
		Assert.assertEquals(12, crossesHeadersMap.size());
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.ENTRY));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.DESIGNATION));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.PARENTAGE));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.GID));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.SOURCE));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.DUPLICATE));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.BULK_WITH));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.BULK_COMPL));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.LOCATION));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.AMOUNT));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.SCALE));
		Assert.assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.COMMENT));
	}

	private Map<InventoryHeaderLabels, Integer> createInventoryHeaderLabelsMap(GermplasmListType germplasmListType) {
		return InventoryHeaderLabels.headers(germplasmListType);
	}

	@Test
	public void testGetHeaderNames_Advanced() {
		Map<InventoryHeaderLabels, Integer> advancedHeadersMap = this.createInventoryHeaderLabelsMap(GermplasmListType.ADVANCED);
		String[] headers = InventoryHeaderLabels.getHeaderNames(advancedHeadersMap);
		Assert.assertEquals(advancedHeadersMap.keySet().size(), headers.length);
		Assert.assertEquals(9, headers.length);
		Assert.assertTrue(headers[0].equals(InventoryHeaderLabels.ENTRY.getName()));
		Assert.assertTrue(headers[1].equals(InventoryHeaderLabels.DESIGNATION.getName()));
		Assert.assertTrue(headers[2].equals(InventoryHeaderLabels.PARENTAGE.getName()));
		Assert.assertTrue(headers[3].equals(InventoryHeaderLabels.GID.getName()));
		Assert.assertTrue(headers[4].equals(InventoryHeaderLabels.SOURCE.getName()));
		Assert.assertTrue(headers[5].equals(InventoryHeaderLabels.LOCATION.getName()));
		Assert.assertTrue(headers[6].equals(InventoryHeaderLabels.AMOUNT.getName()));
		Assert.assertTrue(headers[7].equals(InventoryHeaderLabels.SCALE.getName()));
		Assert.assertTrue(headers[8].equals(InventoryHeaderLabels.COMMENT.getName()));
	}

	@Test
	public void testGetHeaderNames_Crosses() {
		Map<InventoryHeaderLabels, Integer> crossesHeadersMap = this.createInventoryHeaderLabelsMap(GermplasmListType.CROSSES);
		String[] headers = InventoryHeaderLabels.getHeaderNames(crossesHeadersMap);
		Assert.assertEquals(crossesHeadersMap.keySet().size(), headers.length);
		Assert.assertEquals(12, headers.length);
		Assert.assertTrue(headers[0].equals(InventoryHeaderLabels.ENTRY.getName()));
		Assert.assertTrue(headers[1].equals(InventoryHeaderLabels.DESIGNATION.getName()));
		Assert.assertTrue(headers[2].equals(InventoryHeaderLabels.PARENTAGE.getName()));
		Assert.assertTrue(headers[3].equals(InventoryHeaderLabels.GID.getName()));
		Assert.assertTrue(headers[4].equals(InventoryHeaderLabels.SOURCE.getName()));
		Assert.assertTrue(headers[5].equals(InventoryHeaderLabels.DUPLICATE.getName()));
		Assert.assertTrue(headers[6].equals(InventoryHeaderLabels.BULK_WITH.getName()));
		Assert.assertTrue(headers[7].equals(InventoryHeaderLabels.BULK_COMPL.getName()));
		Assert.assertTrue(headers[8].equals(InventoryHeaderLabels.LOCATION.getName()));
		Assert.assertTrue(headers[9].equals(InventoryHeaderLabels.AMOUNT.getName()));
		Assert.assertTrue(headers[10].equals(InventoryHeaderLabels.SCALE.getName()));
		Assert.assertTrue(headers[11].equals(InventoryHeaderLabels.COMMENT.getName()));
	}
}
