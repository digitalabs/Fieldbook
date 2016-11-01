package com.efficio.fieldbook.util.labelprinting.comparators;

import java.util.Comparator;

import org.apache.poi.util.StringUtil;
import org.generationcp.middleware.pojos.GermplasmListData;

public class GermplasmListDataDesignationComparator implements Comparator<GermplasmListData> {

	@Override
	public int compare(final GermplasmListData germplasmListData1, final GermplasmListData germplasmListData2) {
		if (germplasmListData1 == null || germplasmListData2 == null) {
			throw new IllegalArgumentException("Could not compare null values");
		}
		return germplasmListData1.getDesignation().compareTo(germplasmListData2.getDesignation());
	}
}
