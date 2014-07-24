package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class NumberExpression extends NumberSequenceExpression implements Expression {

    public static final String KEY = "[NUMBER]";

    public NumberExpression() {

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
