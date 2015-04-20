package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.commons.ruleengine.OrderedRule;
import org.generationcp.commons.ruleengine.RuleException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SuffixRule extends OrderedRule<NamingRuleExecutionContext> {

	public static final String KEY = "Suffix";

	@Override
	public Object runRule(NamingRuleExecutionContext context) throws RuleException {
		// append a suffix string onto each element of the list - in place

		ProcessCodeService processCodeService = context.getProcessCodeService();
		AdvancingSource advancingSource = context.getAdvancingSource();
		String suffix = advancingSource.getBreedingMethod().getSuffix();

		if (suffix == null) {
			suffix = "";
		}

		List<String> input = context.getCurrentData();

		for (int i = 0; i < input.size(); i++) {
			input.set(i, processCodeService.applyProcessCode(input.get(i) + suffix, advancingSource).get(0));
		}

		context.setCurrentData(input);

		return input;
	}

	@Override public String getKey() {
		return KEY;
	}
}