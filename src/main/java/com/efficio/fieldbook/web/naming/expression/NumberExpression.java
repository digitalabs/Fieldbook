package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class NumberExpression extends Expression {

	public NumberExpression(AdvancingSource source) {
		super(source);
	}

	@Override
	public void apply(List<StringBuilder> values) {
		for (StringBuilder value : values) {
			int startIndex = value.indexOf(Expression.NUMBER);
			int endIndex = startIndex + Expression.NUMBER.length();
			
			Integer newValue = getSource().getPlantsSelected();
			value.replace(startIndex, endIndex, newValue != null ? newValue.toString() : "");
		}
	}

}
