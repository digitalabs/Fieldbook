package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class SequenceExpression implements Expression {

    public static final String KEY = "[SEQUENCE]";

	public SequenceExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		if (source.getPlantsSelected() != null &&
                source.getPlantsSelected() > 0) {
			
			List<StringBuilder> newNames = new ArrayList<StringBuilder>();
			for (StringBuilder value : values) {
				int startIndex = value.indexOf(KEY);
				int endIndex = startIndex + KEY.length();
				
				for (int i = 1; i <= source.getPlantsSelected(); i++) {
					StringBuilder newName = new StringBuilder(value);
					newName.replace(startIndex, endIndex, String.valueOf(i));
					newNames.add(newName);
				}
			}

			values.clear();
			values.addAll(newNames);
		}
	}

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
