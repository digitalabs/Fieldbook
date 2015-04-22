package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.util.ExpressionHelper;
import com.efficio.fieldbook.util.ExpressionHelperCallback;
import com.efficio.fieldbook.web.naming.expression.Expression;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessCodeServiceImpl implements ProcessCodeService {

	@Resource
	private ProcessCodeFactory factory;
	
	@Override
	public List<String> applyProcessCode(String currentInput, String processCode, final AdvancingSource source) {
		List<String> newNames = new ArrayList<String>();

		if (processCode == null) {
			return newNames;
		}

		final List<StringBuilder> builders = new ArrayList<StringBuilder>();
		builders.add(new StringBuilder(currentInput + processCode));
		
		ExpressionHelper.evaluateExpression(processCode, "\\[([^\\]]*)]", new ExpressionHelperCallback() {
            @Override
            public void evaluateCapturedExpression(String capturedText, String originalInput, int start, int end) {
            	Expression expression = factory.create(capturedText);
            	
            	//It's possible for the expression to add more elements to the builders variable.
            	if (expression != null) {
	            	expression.apply(builders, source);
            	}
            }
        });

		for (StringBuilder builder : builders) {
			newNames.add(builder.toString());
		}

		return newNames;
	}

}
