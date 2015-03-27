package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.rules.OrderedRule;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/18/2015
 * Time: 4:03 PM
 */
public abstract class BranchingRule<T extends OrderedRuleExecutionContext> extends OrderedRule<T> {
	public void prepareContextForBranchingToKey(T context, String targetKey) {
		List<String> executionOrder = context.getExecutionOrder();
		int currentExecutionIndex = context.getCurrentExecutionIndex();
		List<String> previousRuleKeys = executionOrder.subList(0, currentExecutionIndex);
		int index = previousRuleKeys.lastIndexOf(targetKey);

		if (index != -1) {
			context.setCurrentExecutionIndex(index);
		}

	}
}