package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.rules.OrderedRule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.rules.RuleExecutionContext;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrefixRule extends OrderedRule {

	public static final String KEY = "Prefix";

	@Override
	public Object runRule(RuleExecutionContext context) throws RuleException {
		NamingRuleExecutionContext nameContext = (NamingRuleExecutionContext) context;
		// append a separator string onto each element of the list - in place
		List<String> input = nameContext.getCurrentData();

		ProcessCodeService processCodeService = nameContext.getProcessCodeService();
		AdvancingSource advancingSource = nameContext.getAdvancingSource();
		String prefix = advancingSource.getBreedingMethod().getPrefix();

		if (prefix == null) {
			prefix = "";
		}

		for (int i = 0; i < input.size(); i++) {
			input.set(i, input.get(i) + processCodeService.applyToName(prefix, advancingSource).get(0));
		}

		nameContext.setCurrentData(input);

		return input;
	}

	@Override public String getKey() {
		return KEY;
	}
}
