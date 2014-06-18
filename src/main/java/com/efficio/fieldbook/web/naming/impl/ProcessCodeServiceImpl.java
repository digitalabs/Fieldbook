package com.efficio.fieldbook.web.naming.impl;

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
	public String applyToName(final String name, final AdvancingSource source) {
		String newName = name;
		final StringBuilder builder = new StringBuilder(name);
		
		ExpressionHelper.evaluateExpression(name, "\\[([^\\]]*)]", new ExpressionHelperCallback() {
            @Override
            public void evaluateCapturedExpression(String capturedText, String originalInput, int start, int end) {
            	Expression expression = factory.create(capturedText, source);
            	
            	if (expression != null) {
	            	expression.apply(builder);
            	}
            }
        });
		
		return newName;
	}

}
