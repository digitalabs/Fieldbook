package com.efficio.fieldbook.util.labelprinting.comparators;

import java.util.Comparator;

import org.generationcp.middleware.pojos.GermplasmListData;

public class GermplasmListDataStockIdComparator implements Comparator<GermplasmListData> {

	@Override
	public int compare(final GermplasmListData germplasmListData1, final GermplasmListData germplasmListData2) {
		if (germplasmListData1 == null || germplasmListData2 == null || germplasmListData1.getInventoryInfo() == null ||
				germplasmListData2.getInventoryInfo() == null || germplasmListData1.getInventoryInfo().getLotRows() == null ||
				germplasmListData2.getInventoryInfo().getLotRows() == null ||
				germplasmListData1.getInventoryInfo().getLotRows().isEmpty() ||
				germplasmListData2.getInventoryInfo().getLotRows().isEmpty() ||
				germplasmListData1.getInventoryInfo().getLotRows().get(0).getStockIds() == null ||
				germplasmListData2.getInventoryInfo().getLotRows().get(0).getStockIds() == null) {
			throw new IllegalArgumentException("Could not compare null values");
		}
		return germplasmListData1.getInventoryInfo().getLotRows().get(0).getStockIds().compareTo(
				germplasmListData2.getInventoryInfo().getLotRows().get(0).getStockIds()) ;
	}
}
