
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class SeasonExpression implements Expression {

	public static final String KEY = "[SEASON]";

	public SeasonExpression() {

	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			int startIndex = value.toString().toUpperCase().indexOf(SeasonExpression.KEY);
			int endIndex = startIndex + SeasonExpression.KEY.length();

			String newValue = source.getSeason();
			value.replace(startIndex, endIndex, newValue != null ? newValue : "");
		}
	}

	@Override
	public String getExpressionKey() {
		return SeasonExpression.KEY;
	}
}
