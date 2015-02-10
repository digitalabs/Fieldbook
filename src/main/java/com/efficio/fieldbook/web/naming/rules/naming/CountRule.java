package com.efficio.fieldbook.web.naming.rules.naming;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;


public class CountRule extends NamingRule {
	
	@Override
	public List<String> runRule(List<String> input) throws RuleException {
		// create counts first - we need a list in case we have a sequence
		List<String> counts = processCodeService.applyToName(advancingSource.getBreedingMethod().getCount(), advancingSource);		

		for (String name : input) {
			for (int i = 0; i < counts.size(); i++) {
				counts.set(i, name + counts.get(i));
			}
		}

		return counts;
	}

}
