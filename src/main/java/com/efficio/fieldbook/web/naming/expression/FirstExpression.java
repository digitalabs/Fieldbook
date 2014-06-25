package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

/**
 * Used as a separator with a string literal immediately following it.
 * Otherwise, it will be disregarded.
 */
public class FirstExpression implements Expression {

    public static final String KEY = "[FIRST]";

	public FirstExpression() {

	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		String separatorExpression = source.getBreedingMethod().getSeparator();

		for (StringBuilder value : values) {
			if (separatorExpression != null && separatorExpression.contains(KEY)) {
				int start = separatorExpression.indexOf(KEY) + KEY.length();
				int end = separatorExpression.indexOf("[", start);
				if (end == -1) {
					end = separatorExpression.length();
				}
				String literalSeparator = separatorExpression.substring(start, end);
				
				int index = source.getRootName().indexOf(literalSeparator);
				if (index > -1) {
					String newRootName = source.getRootName().substring(0, index);
					start = value.indexOf(source.getRootName());
					end = start + source.getRootName().length();
					value.replace(start, end, newRootName);
				}
			}

			int startIndex = value.indexOf(KEY);
			int endIndex = startIndex + KEY.length();

			value.replace(startIndex, endIndex, "");
		}
	}

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
