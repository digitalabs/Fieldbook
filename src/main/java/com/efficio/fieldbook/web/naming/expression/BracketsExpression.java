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
			int rootNameEndIndex = value.indexOf(getSource().getBreedingMethod().getSeparator());
			if (rootNameEndIndex > -1) {
				String newRootName = value.substring(0, rootNameEndIndex);
				
				//if root name already has parentheses 
				if (newRootName.charAt(0) != '(' 
						|| newRootName.charAt(newRootName.length()-1) != ')') {
					
					value.replace(startIndex, endIndex, ")");
					value.insert(0, "(");
				}
				else {
					value.replace(startIndex, endIndex, "");
				}
			}
			else {
				value.replace(startIndex, endIndex, "");
			}
		}
	}

}
