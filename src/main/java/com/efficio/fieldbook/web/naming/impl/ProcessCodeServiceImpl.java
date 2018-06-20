
package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.commons.util.ExpressionHelper;
import org.generationcp.commons.util.ExpressionHelperCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.naming.expression.Expression;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.trial.bean.AdvancingSource;

@Service
@Transactional
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

		ExpressionHelper.evaluateExpression(processCode, ExpressionHelper.PROCESS_CODE_PATTERN, new ExpressionHelperCallback() {

			@Override
			public void evaluateCapturedExpression(String capturedText, String originalInput, int start, int end) {
				final Expression expression;
				if (capturedText.contains(".")) {
					expression = ProcessCodeServiceImpl.this.factory.lookup(capturedText);
				} else {
					expression = ProcessCodeServiceImpl.this.factory.create(capturedText);
				}

				// It's possible for the expression to add more elements to the builders variable.
				if (expression != null) {
					expression.apply(builders, source, capturedText);
				}
			}
		});

		for (StringBuilder builder : builders) {
			newNames.add(builder.toString());
		}

		return newNames;
	}

}
