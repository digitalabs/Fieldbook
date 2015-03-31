package com.efficio.fieldbook.web.naming.rules.naming;

import org.generationcp.commons.ruleengine.OrderedRule;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import org.generationcp.commons.ruleengine.RuleException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeparatorRule extends OrderedRule<NamingRuleExecutionContext> {
	
	public static final String KEY = "Separator";
	
	@Override
	public Object runRule(NamingRuleExecutionContext context) throws RuleException {
		List<String> input = context.getCurrentData();
		AdvancingSource source = context.getAdvancingSource();
		String separator = source.getBreedingMethod().getSeparator();
		// append a separator string onto each element of the list - in place

		for (int i = 0; i < input.size(); i++) {
			input.set(i, input.get(i) + separator);
		}

		context.setCurrentData(input);

		return input;
	}

	@Override public String getKey() {
		return KEY;
	}
}
