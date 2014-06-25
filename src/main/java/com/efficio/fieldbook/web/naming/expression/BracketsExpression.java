package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class BracketsExpression implements Expression {

    public static final String KEY = "[BRACKETS]";

	public BracketsExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			int startIndex = value.indexOf(KEY);
			int endIndex = startIndex + KEY.length();
			int rootNameEndIndex = value.indexOf(source.getBreedingMethod().getSeparator());
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

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}