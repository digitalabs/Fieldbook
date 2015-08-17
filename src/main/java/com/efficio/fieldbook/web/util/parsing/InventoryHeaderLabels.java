
package com.efficio.fieldbook.web.util.parsing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.gms.GermplasmListType;

public enum InventoryHeaderLabels {
	ENTRY("ENTRY", true, true), DESIGNATION("DESIGNATION", true, true), PARENTAGE("PARENTAGE", true, true), GID("GID", true,
			true), SOURCE("SOURCE", true, true), DUPLICATE("DUPLICATE", false, true), BULK_WITH("BULK WITH", false,
					true), BULK_COMPL("BULK COMPL?", false, true), LOCATION("LOCATION", true, true), AMOUNT("AMOUNT", true,
							true), UNITS("UNITS", true, true), STOCKID("STOCKID", true, false), COMMENT("COMMENT", true, true);

	private final String name;
	private final boolean isAnAdvancedListHeader;
	private final boolean isARequiredHeader;

	private InventoryHeaderLabels(String name, boolean isAnAdvancedListHeader, boolean isARequiredHeader) {
		this.name = name;
		this.isAnAdvancedListHeader = isAnAdvancedListHeader;
		this.isARequiredHeader = isARequiredHeader;
	}

	public String getName() {
		return this.name;
	}

	public boolean isAnAdvancedListHeader() {
		return this.isAnAdvancedListHeader;
	}

	public boolean isARequiredHeader() {
		return this.isARequiredHeader;
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

	public static Map<InventoryHeaderLabels, Integer> getRequiredHeadersMap(GermplasmListType listType) {
		InventoryHeaderLabels[] values = InventoryHeaderLabels.values();
		Map<InventoryHeaderLabels, Integer> headers = new LinkedHashMap<InventoryHeaderLabels, Integer>();
		int columnIndex = 0;

		for (InventoryHeaderLabels inventoryHeaderLabels : values) {
			if (listType == GermplasmListType.ADVANCED && !inventoryHeaderLabels.isAnAdvancedListHeader) {
				continue;
			}
			if (inventoryHeaderLabels.isARequiredHeader()) {
				headers.put(inventoryHeaderLabels, columnIndex);
				columnIndex++;
			}

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

	public static String[] getRequiredHeaderNames(Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap) {
		List<String> headers = new ArrayList<String>();
		String[] requiredHeaders;
		for (InventoryHeaderLabels inventoryHeaderLabels : inventoryHeaderLabelsMap.keySet()) {
			if (inventoryHeaderLabels.isARequiredHeader) {
				headers.add(inventoryHeaderLabels.getName());
			}
		}
		requiredHeaders = new String[headers.size()];
		for (int i = 0; i < headers.size(); i++) {
			requiredHeaders[i] = headers.get(i);
		}
		return requiredHeaders;
	}
}
