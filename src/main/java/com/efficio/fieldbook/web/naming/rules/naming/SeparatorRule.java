package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.commons.ruleengine.OrderedRule;
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

		ProcessCodeService processCodeService = context.getProcessCodeService();
		String separatorExpression = source.getBreedingMethod().getSeparator();

		if (separator == null){
			separator = "";
		}
		
		for (int i = 0; i < input.size(); i++) {
			// some separator expressions perform operations on the root name, so we replace the current input with the result
			input.set(i,
					processCodeService.applyProcessCode(input.get(i) + separatorExpression, source)
							.get(0));
		}

		context.setCurrentData(input);

		return input;
	}

	@Override public String getKey() {
		return KEY;
	}
}
