
package com.efficio.fieldbook.web.naming.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.efficio.fieldbook.web.naming.expression.Expression;
import org.generationcp.middleware.exceptions.MiddlewareException;

public class ProcessCodeFactory {

	private Map<String, Expression> expressionMap;

	public void init() {

		this.expressionMap = new HashMap<>();

	}

	public Expression create(final String key) {
		return this.expressionMap.get(key.toUpperCase());
	}

	/**
	 *
	 * @param pattern
	 * @return the first Expression that match the pattern
	 */
	public Expression lookup(final String pattern) {
		if (expressionMap.containsKey(pattern)) {
			return expressionMap.get(pattern);
		}
		for (String key : expressionMap.keySet()) {
			if (key != null && pattern.matches(key)) {
				Expression expression = expressionMap.get(key);
				expressionMap.put(pattern, expression); // memoize
				return expression;
			}
		}
		return null;
	}

	public void addExpression(Expression expression) {
		expressionMap.put(expression.getExpressionKey(), expression);
	}
}
