package com.efficio.fieldbook.util.labelprinting;

import java.util.Comparator;

import org.generationcp.middleware.pojos.GermplasmListData;

public class GermplasmListDataEntryNumberComparator implements Comparator<GermplasmListData> {

	@Override
	public int compare(final GermplasmListData germplasmListData1, final GermplasmListData germplasmListData2) {
		if (germplasmListData1 == null || germplasmListData2 == null) {
			throw new IllegalArgumentException("Could not compare null values");
		}
		return compare(germplasmListData1.getEntryId(), germplasmListData2.getEntryId());
	}

	// I don't know why this isn't in Long...
	private static int compare(final long a, final long b) {
		return a < b ? -1
				: a > b ? 1
				: 0;
	}
}
