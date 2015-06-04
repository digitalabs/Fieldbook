
package com.efficio.fieldbook.web.util.parsing;

import java.util.LinkedHashMap;
import java.util.Map;

import org.generationcp.middleware.domain.gms.GermplasmListType;

public enum InventoryHeaderLabels {
	ENTRY("ENTRY", true), DESIGNATION("DESIGNATION", true), PARENTAGE("PARENTAGE", true), GID("GID", true), SOURCE("SOURCE", true), DUPLICATE(
			"DUPLICATE", false), BULK_WITH("BULK WITH", false), BULK_COMPL("BULK COMPL?", false), LOCATION("LOCATION", true), AMOUNT(
			"AMOUNT", true), SCALE("SCALE", true), COMMENT("COMMENT", true);

	private final String name;
	private final boolean isAnAdvancedListHeader;

	private InventoryHeaderLabels(String name, boolean isAnAdvancedListHeader) {
		this.name = name;
		this.isAnAdvancedListHeader = isAnAdvancedListHeader;
	}

	public String getName() {
		return this.name;
	}

	public boolean isAnAdvancedListHeader() {
		return this.isAnAdvancedListHeader;
	}

	public static Map<InventoryHeaderLabels, Integer> headers(GermplasmListType germplasmListType) {
		InventoryHeaderLabels[] values = InventoryHeaderLabels.values();
		Map<InventoryHeaderLabels, Integer> headers = new LinkedHashMap<InventoryHeaderLabels, Integer>();

		int columnIndex = 0;
		for (InventoryHeaderLabels inventoryHeaderLabels : values) {
			if (germplasmListType == GermplasmListType.ADVANCED && !inventoryHeaderLabels.isAnAdvancedListHeader) {
				continue;
			}
			headers.put(inventoryHeaderLabels, columnIndex);
			columnIndex++;
		}

		return headers;
	}

	public static String[] getHeaderNames(Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap) {
		String[] headers = new String[inventoryHeaderLabelsMap.keySet().size()];
		int index = 0;
		for (InventoryHeaderLabels inventoryHeaderLabels : inventoryHeaderLabelsMap.keySet()) {
			headers[index] = inventoryHeaderLabels.getName();
			index++;
		}
		return headers;
	}
}
