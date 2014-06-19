package com.efficio.fieldbook.web.naming.impl;

import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.naming.expression.BracketsExpression;
import com.efficio.fieldbook.web.naming.expression.BulkCountExpression;
import com.efficio.fieldbook.web.naming.expression.Expression;
import com.efficio.fieldbook.web.naming.expression.FirstExpression;
import com.efficio.fieldbook.web.naming.expression.LocationAbbreviationExpression;
import com.efficio.fieldbook.web.naming.expression.NumberExpression;
import com.efficio.fieldbook.web.naming.expression.SeasonExpression;
import com.efficio.fieldbook.web.naming.expression.SequenceExpression;
import com.efficio.fieldbook.web.naming.expression.TopLocationAbbreviation;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@Service
public class ProcessCodeFactory {

	public Expression create(String key, AdvancingSource source) {
		Expression expression = null;
		if (Expression.BRACKETS.equalsIgnoreCase(key)) {
			expression = new BracketsExpression(source);
		}
		else if (Expression.BULK_COUNT.equalsIgnoreCase(key)) {
			expression = new BulkCountExpression(source);
		}
		else if (Expression.FIRST.equalsIgnoreCase(key)) {
			expression = new FirstExpression(source);
		}
		else if (Expression.LOCATION_ABBREVIATION.equalsIgnoreCase(key)) {
			expression = new LocationAbbreviationExpression(source);
		}
		else if (Expression.NUMBER.equalsIgnoreCase(key)) {
			expression = new NumberExpression(source);
		}
		else if (Expression.SEASON.equalsIgnoreCase(key)) {
			expression = new SeasonExpression(source);
		}
		else if (Expression.SEQUENCE.equalsIgnoreCase(key)) {
			expression = new SequenceExpression(source);
		}
		else if (Expression.TOP_LOCATION_ABBREVIATION.equalsIgnoreCase(key)) {
			expression = new TopLocationAbbreviation(source);
		}
		return expression;
	}
}
