package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class SequenceExpression extends NumberSequenceExpression implements Expression {

    public static final String KEY = "[SEQUENCE]";

	public SequenceExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		applyNumberSequence(values, source);
	}

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
