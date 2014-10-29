package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.generationcp.middleware.pojos.Method;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class BracketsExpression implements Expression {

    public static final String KEY = "[BRACKETS]";

	public BracketsExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			int startIndex = value.toString().toUpperCase().indexOf(KEY);
			int endIndex = startIndex + KEY.length();
			String nextExpression = getExpressionAfterRootName(source.getBreedingMethod());
			int rootNameEndIndex = value.length();
			if (nextExpression != null) {
				rootNameEndIndex = value.indexOf(nextExpression);
			}
			if (rootNameEndIndex > -1) {
				String newRootName = value.substring(0, rootNameEndIndex);
				
				//if root name already has parentheses 
				if (newRootName.charAt(0) != '(' 
						|| newRootName.charAt(newRootName.length()-1) != ')') {
					
					value.replace(startIndex, endIndex, ")");
					value.insert(0, "(");
				} else {
					value.replace(startIndex, endIndex, "");
				}
			} else {
				value.replace(startIndex, endIndex, "");
			}
		}
	}

    @Override
    public String getExpressionKey() {
        return KEY;
    }
    
    private String getExpressionAfterRootName(Method method) {
    	if (method.getSeparator() != null) {
    		return method.getSeparator();
    	} else if (method.getPrefix() != null) {
    		return method.getPrefix();
    	} else if (method.getCount() != null) {
    		return method.getCount();
    	} else if (method.getSuffix() != null) {
    		return method.getSuffix();
    	}
    	return null;
    }
}