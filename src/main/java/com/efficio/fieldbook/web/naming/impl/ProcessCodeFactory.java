
package com.efficio.fieldbook.web.naming.impl;

import java.util.HashMap;
import java.util.Map;

import com.efficio.fieldbook.web.naming.expression.Expression;

public class ProcessCodeFactory {

	private Map<String, Expression> expressionMap;

	public void init() {

		this.expressionMap = new HashMap<>();

	}

	public Expression create(final String key) {
		return this.expressionMap.get(key.toUpperCase());
	}

	public void addExpression(Expression expression) {
		expressionMap.put(expression.getExpressionKey(), expression);
	}
}
