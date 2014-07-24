package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public abstract class NumberSequenceExpression implements Expression {

	protected void applyNumberSequence(List<StringBuilder> values, AdvancingSource source) {
    	if (source.isBulk()) {
	        for (StringBuilder value : values) {
	            int startIndex = value.toString().toUpperCase().indexOf(getExpressionKey());
	            int endIndex = startIndex + getExpressionKey().length();
	
	            if (source.getPlantsSelected() != null &&
	                    source.getPlantsSelected() > 1) {
	
	                Integer newValue = source.getPlantsSelected();
	                value.replace(startIndex, endIndex, newValue != null ? newValue.toString() : "");
	            }
	            else {
	                value.replace(startIndex, endIndex, "");
	            }
	        }
    	}
    	else {
			List<StringBuilder> newNames = new ArrayList<StringBuilder>();
			for (StringBuilder value : values) {
				int startIndex = value.toString().toUpperCase().indexOf(getExpressionKey());
				int endIndex = startIndex + getExpressionKey().length();
				
				if (source.getPlantsSelected() != null &&
		                source.getPlantsSelected() > 0) {
					
					for (int i = 1; i <= source.getPlantsSelected(); i++) {
						StringBuilder newName = new StringBuilder(value);
						newName.replace(startIndex, endIndex, String.valueOf(i));
						newNames.add(newName);
					}
				}
				else {
					newNames.add(value.replace(startIndex, endIndex, ""));
				}
			}
	
			values.clear();
			values.addAll(newNames);
    	}
	}
}
