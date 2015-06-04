
package com.efficio.fieldbook.web.naming.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.efficio.fieldbook.web.naming.expression.Expression;

public class ProcessCodeFactory {

	private List<Expression> registeredExpressions;

	private Map<String, Expression> expressionMap;

	public void init() {
		assert this.registeredExpressions != null;

		this.expressionMap = new HashMap<>();
		for (Expression registeredExpression : this.registeredExpressions) {
			this.expressionMap.put(registeredExpression.getExpressionKey(), registeredExpression);
		}
	}

	public Expression create(String key) {
		return this.expressionMap.get(key.toUpperCase());
	}

	public void setRegisteredExpressions(List<Expression> registeredExpressions) {
		this.registeredExpressions = registeredExpressions;
	}
}
