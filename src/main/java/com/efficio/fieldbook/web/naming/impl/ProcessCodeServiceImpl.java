package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.efficio.fieldbook.util.ExpressionHelper;
import com.efficio.fieldbook.util.ExpressionHelperCallback;
import com.efficio.fieldbook.web.naming.expression.Expression;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@Service
public class ProcessCodeServiceImpl implements ProcessCodeService {

	@Resource
	private ProcessCodeFactory factory;
	
	@Override
	public List<String> applyToName(final String expression, final AdvancingSource source) {
		List<String> newNames = new ArrayList<String>();
		final List<StringBuilder> builders = new ArrayList<StringBuilder>();
		builders.add(new StringBuilder(expression));
		
		ExpressionHelper.evaluateExpression(expression, "\\[([^\\]]*)]", new ExpressionHelperCallback() {
            @Override
            public void evaluateCapturedExpression(String capturedText, String originalInput, int start, int end) {
            	Expression expression = factory.create(capturedText, source);
            	
            	if (expression != null) {
	            	expression.apply(builders);
            	}
            }
        });

		for (StringBuilder builder : builders) {
			newNames.add(builder.toString());
		}
		return newNames;
	}

}
