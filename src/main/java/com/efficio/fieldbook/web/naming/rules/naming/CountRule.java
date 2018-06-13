
package com.efficio.fieldbook.web.naming.rules.naming;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.ruleengine.OrderedRule;
import org.generationcp.commons.ruleengine.RuleException;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.trial.bean.AdvancingSource;

@Component
public class CountRule extends OrderedRule<NamingRuleExecutionContext> {

	public static final String KEY = "Count";
	public static final String DEFAULT_COUNT = "[NUMBER]";

	@Override
	public Object runRule(NamingRuleExecutionContext context) throws RuleException {
		// create counts first - we need a list in case we have a sequence

		ProcessCodeService service = context.getProcessCodeService();
		AdvancingSource source = context.getAdvancingSource();

		List<String> input = context.getCurrentData();

		List<String> counts = new ArrayList<>();

		for (String currentInput : input) {
			counts.addAll(service.applyProcessCode(currentInput, source.getBreedingMethod().getCount(), source));
		}

		// store current data in temp before overwriting it with count data, so that it can be restored for another try later on
		context.setTempData(context.getCurrentData());

		if (!counts.isEmpty()) {
			// place the processed name data with count information as current rule execution output
			context.setCurrentData(counts);
			return counts;
		} else {
			return input;
		}

	}

	@Override
	public String getKey() {
		return CountRule.KEY;
	}
}
