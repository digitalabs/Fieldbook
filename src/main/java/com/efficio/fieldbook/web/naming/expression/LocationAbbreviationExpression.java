
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.springframework.stereotype.Component;

@Component
public class LocationAbbreviationExpression implements Expression {

	public static final String KEY = "[LABBR]";

	public LocationAbbreviationExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			int startIndex = value.toString().toUpperCase().indexOf(LocationAbbreviationExpression.KEY);
			int endIndex = startIndex + LocationAbbreviationExpression.KEY.length();

			String newValue = source.getLocationAbbreviation();
			value.replace(startIndex, endIndex, newValue != null ? newValue : "");
		}
	}

	@Override
	public String getExpressionKey() {
		return LocationAbbreviationExpression.KEY;
	}
}
