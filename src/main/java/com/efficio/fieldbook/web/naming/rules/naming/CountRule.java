package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.rules.OrderedRule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CountRule extends OrderedRule<NamingRuleExecutionContext> {

	public static final String KEY = "Count";
	public static final String DEFAULT_COUNT = "[NUMBER]";

	@Override
	public Object runRule(NamingRuleExecutionContext context) throws RuleException {
		// create counts first - we need a list in case we have a sequence

		ProcessCodeService service = context.getProcessCodeService();
		AdvancingSource source = context.getAdvancingSource();
		List<String> counts = service.applyToName(
				source.getBreedingMethod().getCount(), source);

		List<String> input = context.getCurrentData();

		if (!counts.isEmpty()) {
			for (String name : input) {
				for (int i = 0; i < counts.size(); i++) {
					counts.set(i, name + counts.get(i));
				}
			}

			// store current data in temp before overwriting it with count data, so that it can be restored for another try later on
			context.setTempData(context.getCurrentData());

			// place the processed name data with count information as current rule execution output
			context.setCurrentData(counts);
			return counts;
		} else {
			return input;
		}

	}

	@Override public String getKey() {
		return KEY;
	}
}