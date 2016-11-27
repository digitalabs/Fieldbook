package com.efficio.fieldbook.util.labelprinting.comparators;

import java.util.Comparator;

import org.generationcp.middleware.pojos.GermplasmListData;

public class GermplasmListDataEntryNumberComparator implements Comparator<GermplasmListData> {

	@Override
	public int compare(final GermplasmListData germplasmListData1, final GermplasmListData germplasmListData2) {
		if (germplasmListData1 == null || germplasmListData2 == null) {
			throw new IllegalArgumentException("Could not compare null values");
		}
		return ComparatorUtil.compareNumbers(germplasmListData1.getEntryId(), germplasmListData2.getEntryId());
	}
}
