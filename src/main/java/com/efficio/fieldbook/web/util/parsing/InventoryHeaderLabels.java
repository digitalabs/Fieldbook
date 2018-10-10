
package com.efficio.fieldbook.web.util.parsing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.manager.api.OntologyDataManager;

public enum InventoryHeaderLabels {
	ENTRY("ENTRY", true, true, ColumnLabels.ENTRY_ID), DESIGNATION("DESIGNATION", true, true, ColumnLabels.DESIGNATION), PARENTAGE("PARENTAGE", true, true, ColumnLabels.PARENTAGE), GID("GID", true,
			true, ColumnLabels.GID), SOURCE("SOURCE", true, true, ColumnLabels.SEED_SOURCE), DUPLICATE("DUPLICATE", false, true, ColumnLabels.DUPLICATE), BULK_WITH("BULK WITH", false,
					true, ColumnLabels.BULK_WITH), BULK_COMPL("BULK COMPL?", false, true, ColumnLabels.BULK_COMPL), LOCATION("LOCATION", true, true, ColumnLabels.LOT_LOCATION), LOCATION_ABBR("LOCATION ABBR", true, true, null), AMOUNT("AMOUNT", true,
							true, null), STOCKID("STOCKID", true, false, ColumnLabels.STOCKID_INVENTORY), COMMENT("COMMENT", true, true, ColumnLabels.COMMENT);

	private final String name;
	private final boolean isAnAdvancedListHeader;
	private final boolean isARequiredHeader;
	private final ColumnLabels columnLabel;

	private InventoryHeaderLabels(final String name, final boolean isAnAdvancedListHeader, final boolean isARequiredHeader, final
			ColumnLabels columnLabel) {
		this.name = name;
		this.isAnAdvancedListHeader = isAnAdvancedListHeader;
		this.isARequiredHeader = isARequiredHeader;
		this.columnLabel = columnLabel;
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

	public ColumnLabels getColumnLabel() {
	  	return  this.columnLabel;
	}

	public static Map<InventoryHeaderLabels, Integer> headers(final GermplasmListType germplasmListType) {
		final InventoryHeaderLabels[] values = InventoryHeaderLabels.values();
		final Map<InventoryHeaderLabels, Integer> headers = new LinkedHashMap<InventoryHeaderLabels, Integer>();

		int columnIndex = 0;
		for (final InventoryHeaderLabels inventoryHeaderLabels : values) {
			if (germplasmListType == GermplasmListType.ADVANCED && !inventoryHeaderLabels.isAnAdvancedListHeader) {
				continue;
			}
			headers.put(inventoryHeaderLabels, columnIndex);
			columnIndex++;
		}

		return headers;
	}

	public static Map<InventoryHeaderLabels, Integer> getRequiredHeadersMap(final GermplasmListType listType) {
		final InventoryHeaderLabels[] values = InventoryHeaderLabels.values();
		final Map<InventoryHeaderLabels, Integer> headers = new LinkedHashMap<InventoryHeaderLabels, Integer>();
		int columnIndex = 0;

		for (final InventoryHeaderLabels inventoryHeaderLabels : values) {
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

	public static String[] getHeaderNames(final Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap, final OntologyDataManager ontologyDataManager) {
		final String[] headers = new String[inventoryHeaderLabelsMap.keySet().size()];
		int index = 0;
		for (final InventoryHeaderLabels inventoryHeaderLabels : inventoryHeaderLabelsMap.keySet()) {
			headers[index] = inventoryHeaderLabels.getColumnLabel() != null ? inventoryHeaderLabels.getColumnLabel().getTermNameFromOntology(ontologyDataManager): inventoryHeaderLabels.getName();
			index++;
		}
		return headers;
	}

	public static String[] getRequiredHeaderNames(final Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap, final OntologyDataManager ontologyDataManager) {
		final List<String> headers = new ArrayList<String>();
		String[] requiredHeaders;
		for (final InventoryHeaderLabels inventoryHeaderLabels : inventoryHeaderLabelsMap.keySet()) {
			if (inventoryHeaderLabels.isARequiredHeader) {
			  	final String requiredHeader = inventoryHeaderLabels.getColumnLabel() != null ? inventoryHeaderLabels.getColumnLabel().getTermNameFromOntology(ontologyDataManager): inventoryHeaderLabels.getName();
				headers.add(requiredHeader);
			}
		}
		requiredHeaders = new String[headers.size()];
		for (int i = 0; i < headers.size(); i++) {
			requiredHeaders[i] = headers.get(i);
		}
		return requiredHeaders;
	}
}
