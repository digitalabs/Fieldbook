package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.rules.OrderedRule;
import com.efficio.fieldbook.web.naming.rules.RuleExecutionContext;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/18/2015
 * Time: 4:03 PM
 */
public abstract class BranchingRule extends OrderedRule {
	public void prepareContextForBranchingToKey(RuleExecutionContext context, String targetKey) {
		List<String> executionOrder = context.getExecutionOrder();

		int index = executionOrder.lastIndexOf(targetKey);

		if (index != -1) {
			context.setCurrentExecutionIndex(index);
		}

	}
}