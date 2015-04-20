package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.commons.ruleengine.OrderedRule;
import org.generationcp.commons.ruleengine.RuleException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrefixRule extends OrderedRule<NamingRuleExecutionContext> {

	public static final String KEY = "Prefix";

	@Override
	public Object runRule(NamingRuleExecutionContext context) throws RuleException {

		// append a separator string onto each element of the list - in place
		List<String> input = context.getCurrentData();

		ProcessCodeService processCodeService = context.getProcessCodeService();
		AdvancingSource advancingSource = context.getAdvancingSource();
		String prefix = advancingSource.getBreedingMethod().getPrefix();

		if (prefix == null) {
			prefix = "";
		}

		for (int i = 0; i < input.size(); i++) {
			input.set(i, processCodeService.applyProcessCode(input.get(i) + prefix, advancingSource).get(
					0));
		}

		context.setCurrentData(input);

		return input;
	}

	@Override public String getKey() {
		return KEY;
	}
}
