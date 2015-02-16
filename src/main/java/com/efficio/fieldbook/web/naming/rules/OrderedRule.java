package com.efficio.fieldbook.web.naming.rules;

import com.efficio.fieldbook.web.naming.impl.RuleFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/13/2015
 * Time: 4:07 PM
 */
public abstract class OrderedRule implements Rule{

	@Override public String getNextRuleStepKey(RuleExecutionContext context) {
		List<String> sequenceOrder = context.getExecutionOrder();

		int keyIndex = sequenceOrder.indexOf(getKey());

		if ((keyIndex != -1) && (keyIndex + 1) < sequenceOrder.size()) {
			return sequenceOrder.get(keyIndex + 1);
		} else {
			return null;
		}
	}
}
