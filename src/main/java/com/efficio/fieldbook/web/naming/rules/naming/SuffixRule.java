package com.efficio.fieldbook.web.naming.rules.naming;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;


public class SuffixRule extends NamingRule {

	@Override
	public void init(Map<String, Object> context) {
		super.init(context);
		if(advancingSource.getBreedingMethod().getSuffix() == null) {
			advancingSource.getBreedingMethod().setSuffix("");
		}
	}

	@Override
	public List<String> runRule(List<String> input) throws RuleException {
		// append a suffix string onto each element of the list - in place
		for (int i = 0; i < input.size(); i++) {
			input.set(i, input.get(i) + processCodeService.applyToName(advancingSource.getBreedingMethod().getSuffix(), advancingSource).get(0));
		}
		return input;
	}

}
