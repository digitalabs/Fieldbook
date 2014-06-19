package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

/**
 * Used as a separator with a string literal immediately following it.
 * Otherwise, it will be disregarded.
 */
public class FirstExpression extends Expression {

	public FirstExpression(AdvancingSource source) {
		super(source);
	}

	@Override
	public void apply(List<StringBuilder> values) {
		String separatorExpression = getSource().getBreedingMethod().getSeparator();
		for (StringBuilder value : values) {
			if (separatorExpression != null && separatorExpression.contains(Expression.FIRST)) {
				int start = separatorExpression.indexOf(Expression.FIRST) + Expression.FIRST.length();
				int end = separatorExpression.indexOf("[", start);
				if (end == -1) {
					end = separatorExpression.length();
				}
				String literalSeparator = separatorExpression.substring(start, end);
				
				int index = getSource().getRootName().indexOf(literalSeparator);
				if (index > -1) {
					String newRootName = getSource().getRootName().substring(0, index);
					start = value.indexOf(getSource().getRootName());
					end = start + getSource().getRootName().length();
					value.replace(start, end, newRootName);
				}
			}

			int startIndex = value.indexOf(Expression.FIRST);
			int endIndex = startIndex + Expression.FIRST.length();

			value.replace(startIndex, endIndex, "");
		}
	}

}
