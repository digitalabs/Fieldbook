package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.web.naming.expression.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessCodeFactory {

    private List<Expression> registeredExpressions;

    private Map<String, Expression> expressionMap;

    public void init() {
        assert registeredExpressions != null;

        expressionMap = new HashMap<>();
        for (Expression registeredExpression : registeredExpressions) {
            expressionMap.put(registeredExpression.getExpressionKey(), registeredExpression);
        }
    }


	public Expression create(String key) {
		return expressionMap.get(key.toUpperCase());
	}

    public void setRegisteredExpressions(List<Expression> registeredExpressions) {
        this.registeredExpressions = registeredExpressions;
    }
}
