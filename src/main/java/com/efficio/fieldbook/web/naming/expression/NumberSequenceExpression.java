package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@Service
public abstract class NumberSequenceExpression implements Expression {

	protected void applyNumberSequence(List<StringBuilder> values, AdvancingSource source) {
		if (source.isForceUniqueNameGeneration()) {
			for (StringBuilder value : values) {
				int startIndex = value.toString().toUpperCase().indexOf(getExpressionKey());
				int endIndex = startIndex + getExpressionKey().length();

				value.replace(startIndex, endIndex, "(" + Integer.toString(source.getCurrentMaxSequence() + 1) + ")");

			}

			return;
		}


    	if (source.isBulk()) {
	        for (StringBuilder value : values) {
	            int startIndex = value.toString().toUpperCase().indexOf(getExpressionKey());
	            int endIndex = startIndex + getExpressionKey().length();
	
	            if (source.getPlantsSelected() != null &&
	                    source.getPlantsSelected() > 1) {
	
	                Integer newValue = source.getPlantsSelected();
                	value.replace(startIndex, endIndex, newValue != null ? newValue.toString() : "");
	            } else {
	                value.replace(startIndex, endIndex, "");
	            }
	        }
    	} else {
			List<StringBuilder> newNames = new ArrayList<StringBuilder>();
			int startCount = 1;

			if (source.getCurrentMaxSequence() > -1) {
				startCount = source.getCurrentMaxSequence() + 1;
			}

			for (StringBuilder value : values) {
				int startIndex = value.toString().toUpperCase().indexOf(getExpressionKey());
				int endIndex = startIndex + getExpressionKey().length();
				
				if (source.getPlantsSelected() != null &&
		                source.getPlantsSelected() > 0) {
					
					for (int i = startCount; i < startCount + source.getPlantsSelected(); i++) {
						StringBuilder newName = new StringBuilder(value);
						newName.replace(startIndex, endIndex, String.valueOf(i));
						newNames.add(newName);
					}
				} else {
					newNames.add(value.replace(startIndex, endIndex, ""));
				}
			}
	
			values.clear();
			values.addAll(newNames);
    	}
	}
}