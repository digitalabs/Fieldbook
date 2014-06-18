package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class SeasonExpression extends Expression {

	public SeasonExpression(AdvancingSource source) {
		super(source);
	}

	@Override
	public void apply(StringBuilder value) {
		int startIndex = value.indexOf(Expression.SEASON);
		int endIndex = startIndex + Expression.SEASON.length();
		
		String newValue = getSource().getSeason();
		value.replace(startIndex, endIndex, newValue);
	}

}
