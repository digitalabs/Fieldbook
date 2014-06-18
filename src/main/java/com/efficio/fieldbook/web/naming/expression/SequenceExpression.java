package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class SequenceExpression extends Expression {

	public SequenceExpression(AdvancingSource source) {
		super(source);
	}

	@Override
	public void apply(List<StringBuilder> values) {
		List<StringBuilder> newNames = new ArrayList<StringBuilder>();
		for (StringBuilder value : values) {
			int startIndex = value.indexOf(Expression.SEQUENCE);
			int endIndex = startIndex + Expression.SEQUENCE.length();
			
			for (int i = 0; i < getSource().getPlantsSelected(); i++) {
				StringBuilder newName = new StringBuilder(value);
				newName.replace(startIndex, endIndex, String.valueOf(i));
				newNames.add(newName);
			}
		}
		values.clear();
		values.addAll(newNames);
	}

}
