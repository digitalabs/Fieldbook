package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class LocationAbbreviationExpression extends Expression {

	public LocationAbbreviationExpression(AdvancingSource source) {
		super(source);
	}

	@Override
	public void apply(List<StringBuilder> values) {
		for (StringBuilder value : values) {
			int startIndex = value.indexOf(Expression.LOCATION_ABBREVIATION);
			int endIndex = startIndex + Expression.LOCATION_ABBREVIATION.length();
			
			String newValue = getSource().getLocationAbbreviation();
			value.replace(startIndex, endIndex, newValue != null ? newValue : "");
		}
	}

}
