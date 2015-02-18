package com.efficio.fieldbook.web.naming.rules;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/13/2015
 * Time: 4:07 PM
 */
public abstract class OrderedRule implements Rule{

	@Override public String getNextRuleStepKey(RuleExecutionContext context){
		List<String> sequenceOrder = context.getExecutionOrder();
		int executionIndex = context.getCurrentExecutionIndex();

		// increment to the next rule in the sequence
		executionIndex++;
		if (executionIndex < sequenceOrder.size()) {
			String nextKey = sequenceOrder.get(executionIndex);
			context.setCurrentExecutionIndex(executionIndex);
			return nextKey;
		} else {
			return null;
		}
	}
}
