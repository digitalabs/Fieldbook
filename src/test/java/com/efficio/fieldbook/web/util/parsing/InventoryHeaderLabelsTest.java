package com.efficio.fieldbook.web.util.parsing;

import static org.junit.Assert.*;

import java.util.Map;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.junit.Test;

public class InventoryHeaderLabelsTest {
	
	@Test
	public void testHeaders_Advanced() {
		Map<InventoryHeaderLabels,Integer> advancedHeadersMap = 
				createInventoryHeaderLabelsMap(GermplasmListType.ADVANCED);
		assertEquals(9, advancedHeadersMap.size());
		assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.ENTRY));
		assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.DESIGNATION));
		assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.PARENTAGE));
		assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.GID));
		assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.SOURCE));
		assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.LOCATION));
		assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.AMOUNT));
		assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.SCALE));
		assertTrue(advancedHeadersMap.containsKey(InventoryHeaderLabels.COMMENT));
	}

	@Test
	public void testHeaders_Crosses() {
		Map<InventoryHeaderLabels,Integer> crossesHeadersMap = 
				InventoryHeaderLabels.headers(GermplasmListType.CROSSES);
		assertEquals(12, crossesHeadersMap.size());
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.ENTRY));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.DESIGNATION));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.PARENTAGE));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.GID));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.SOURCE));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.DUPLICATE));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.BULK_WITH));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.BULK_COMPL));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.LOCATION));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.AMOUNT));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.SCALE));
		assertTrue(crossesHeadersMap.containsKey(InventoryHeaderLabels.COMMENT));
	}

	private Map<InventoryHeaderLabels,Integer> createInventoryHeaderLabelsMap(
			GermplasmListType germplasmListType) {
		return InventoryHeaderLabels.headers(germplasmListType);
	}
	
	@Test
	public void testGetHeaderNames_Advanced() {
		Map<InventoryHeaderLabels,Integer> advancedHeadersMap = 
				createInventoryHeaderLabelsMap(GermplasmListType.ADVANCED);
		String[] headers = InventoryHeaderLabels.getHeaderNames(advancedHeadersMap);
		assertEquals(advancedHeadersMap.keySet().size(),headers.length);
		assertEquals(9, headers.length);
		assertTrue(headers[0].equals(InventoryHeaderLabels.ENTRY.getName()));
		assertTrue(headers[1].equals(InventoryHeaderLabels.DESIGNATION.getName()));
		assertTrue(headers[2].equals(InventoryHeaderLabels.PARENTAGE.getName()));
		assertTrue(headers[3].equals(InventoryHeaderLabels.GID.getName()));
		assertTrue(headers[4].equals(InventoryHeaderLabels.SOURCE.getName()));
		assertTrue(headers[5].equals(InventoryHeaderLabels.LOCATION.getName()));
		assertTrue(headers[6].equals(InventoryHeaderLabels.AMOUNT.getName()));
		assertTrue(headers[7].equals(InventoryHeaderLabels.SCALE.getName()));
		assertTrue(headers[8].equals(InventoryHeaderLabels.COMMENT.getName()));
	}

	@Test
	public void testGetHeaderNames_Crosses() {
		Map<InventoryHeaderLabels,Integer> crossesHeadersMap = 
				createInventoryHeaderLabelsMap(GermplasmListType.CROSSES);
		String[] headers = InventoryHeaderLabels.getHeaderNames(crossesHeadersMap);
		assertEquals(crossesHeadersMap.keySet().size(),headers.length);
		assertEquals(12, headers.length);
		assertTrue(headers[0].equals(InventoryHeaderLabels.ENTRY.getName()));
		assertTrue(headers[1].equals(InventoryHeaderLabels.DESIGNATION.getName()));
		assertTrue(headers[2].equals(InventoryHeaderLabels.PARENTAGE.getName()));
		assertTrue(headers[3].equals(InventoryHeaderLabels.GID.getName()));
		assertTrue(headers[4].equals(InventoryHeaderLabels.SOURCE.getName()));
		assertTrue(headers[5].equals(InventoryHeaderLabels.DUPLICATE.getName()));
		assertTrue(headers[6].equals(InventoryHeaderLabels.BULK_WITH.getName()));
		assertTrue(headers[7].equals(InventoryHeaderLabels.BULK_COMPL.getName()));
		assertTrue(headers[8].equals(InventoryHeaderLabels.LOCATION.getName()));
		assertTrue(headers[9].equals(InventoryHeaderLabels.AMOUNT.getName()));
		assertTrue(headers[10].equals(InventoryHeaderLabels.SCALE.getName()));
		assertTrue(headers[11].equals(InventoryHeaderLabels.COMMENT.getName()));
	}
}