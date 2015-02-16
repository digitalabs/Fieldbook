package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.rules.OrderedRule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.rules.RuleExecutionContext;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SuffixRule extends OrderedRule {

	public static final String KEY = "Suffix";

	@Override
	public Object runRule(RuleExecutionContext context) throws RuleException {
		// append a suffix string onto each element of the list - in place
		NamingRuleExecutionContext nameContext = (NamingRuleExecutionContext) context;
		ProcessCodeService processCodeService = nameContext.getProcessCodeService();
		AdvancingSource advancingSource = nameContext.getAdvancingSource();
		String suffix = advancingSource.getBreedingMethod().getSuffix();

		if (suffix == null) {
			suffix = "";
		}

		List<String> input = nameContext.getCurrentData();

		for (int i = 0; i < input.size(); i++) {
			input.set(i, input.get(i) + processCodeService.applyToName(suffix, advancingSource).get(0));
		}

		nameContext.setCurrentData(input);

		return input;
	}

	@Override public String getKey() {
		return KEY;
	}
}
