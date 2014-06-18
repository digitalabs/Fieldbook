package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class BracketsExpression extends Expression {

	public BracketsExpression(AdvancingSource source) {
		super(source);
	}

	@Override
	public void apply(List<StringBuilder> values) {
		for (StringBuilder value : values) {
			int startIndex = value.indexOf(Expression.BRACKETS);
			int endIndex = startIndex + Expression.BRACKETS.length();
			
			value.insert(0, "(");
			value.replace(startIndex, endIndex, ")");
		}
	}

}
