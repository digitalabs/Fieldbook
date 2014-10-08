package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.web.naming.expression.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessCodeFactory {

    public List<Expression> registeredExpressions;

    public Map<String, Expression> expressionMap;

    public void init() {
        assert(registeredExpressions != null);

        expressionMap = new HashMap<String, Expression>();
        for (Expression registeredExpression : registeredExpressions) {
            expressionMap.put(registeredExpression.getExpressionKey(), registeredExpression);
        }
    }


	public Expression create(String key) {
		Expression expression = expressionMap.get(key.toUpperCase());
		return expression;
	}

    public void setRegisteredExpressions(List<Expression> registeredExpressions) {
        this.registeredExpressions = registeredExpressions;
    }
}
