package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.rules.OrderedRule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.rules.RuleExecutionContext;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeparatorRule extends OrderedRule {
	
	public static final String KEY = "Separator";
	
	@Override
	public Object runRule(RuleExecutionContext context) throws RuleException {
		NamingRuleExecutionContext nameContext = (NamingRuleExecutionContext) context;
		List<String> input = nameContext.getCurrentData();
		AdvancingSource source = nameContext.getAdvancingSource();
		String separator = source.getBreedingMethod().getSeparator();
		// append a separator string onto each element of the list - in place

		for (int i = 0; i < input.size(); i++) {
			input.set(i, input.get(i) + separator);
		}

		nameContext.setCurrentData(input);

		return input;
	}

	@Override public String getKey() {
		return KEY;
	}
}
