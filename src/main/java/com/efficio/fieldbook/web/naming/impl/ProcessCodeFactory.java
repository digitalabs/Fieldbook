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
import com.efficio.fieldbook.web.naming.expression.TopLocationAbbreviationExpression;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import javax.annotation.Resource;
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
