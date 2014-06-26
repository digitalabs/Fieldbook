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
			int startIndex = value.toString().toUpperCase().indexOf(KEY);
			int endIndex = startIndex + KEY.length();
			
			String newValue = source.getSeason();
			value.replace(startIndex, endIndex, newValue != null ? newValue : "");
		}
	}

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
