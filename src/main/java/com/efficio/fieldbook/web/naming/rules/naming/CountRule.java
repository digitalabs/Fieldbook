package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.rules.OrderedRule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.rules.RuleExecutionContext;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CountRule extends OrderedRule {

	public static final String KEY = "Count";
	
	@Override
	public Object runRule(RuleExecutionContext context) throws RuleException {
		// create counts first - we need a list in case we have a sequence
		NamingRuleExecutionContext nameContext = (NamingRuleExecutionContext) context;

		ProcessCodeService service = nameContext.getProcessCodeService();
		AdvancingSource source = nameContext.getAdvancingSource();
		List<String> counts = service.applyToName(
				source.getBreedingMethod().getCount(), source);

		List<String> input = nameContext.getCurrentData();

		for (String name : input) {
			for (int i = 0; i < counts.size(); i++) {
				counts.set(i, name + counts.get(i));
			}
		}

		// store current data in temp before overwriting it with count data, so that it can be restored for another try later on
		nameContext.setTempData(nameContext.getCurrentData());

		// place the processed name data with count information as current rule execution output
		nameContext.setCurrentData(counts);
		return counts;
	}

	@Override public String getKey() {
		return KEY;
	}
}