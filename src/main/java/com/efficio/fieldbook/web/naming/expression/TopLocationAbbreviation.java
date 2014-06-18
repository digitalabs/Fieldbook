package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class TopLocationAbbreviation extends Expression {

	public TopLocationAbbreviation(AdvancingSource source) {
		super(source);
	}

	@Override
	public void apply(List<StringBuilder> values) {
		for (StringBuilder value : values) {
			int startIndex = value.indexOf(Expression.TOP_LOCATION_ABBREVIATION);
			int endIndex = startIndex + Expression.TOP_LOCATION_ABBREVIATION.length();

			String rootName = getSource().getRootName();
			String labbr = getSource().getLocationAbbreviation() != null ? getSource().getLocationAbbreviation() : "";
			if (rootName != null && rootName.toString().endsWith("T")) {
				value.replace(startIndex, endIndex, "TOP" + labbr);
			}
			else {
				value.replace(startIndex, endIndex, labbr);
			}
		}
	}

}
