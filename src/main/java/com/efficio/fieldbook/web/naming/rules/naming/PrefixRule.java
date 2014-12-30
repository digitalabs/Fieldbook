package com.efficio.fieldbook.web.naming.rules.naming;

import java.util.List;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;


public class PrefixRule implements Rule {
	
	@Resource
	private ProcessCodeService processCodeService;
	private AdvancingSource advancingSource;
	
	@Override
	public void init(Object source) {
		advancingSource = (AdvancingSource) source;
		
	}

	@Override
	public List<String> runRule(List<String> input) throws RuleException {
		// append a separator string onto each element of the list - in place
		for (int i = 0; i < input.size(); i++) {
			input.set(i, input.get(i) + processCodeService.applyToName(advancingSource.getBreedingMethod().getPrefix(), advancingSource).get(0));
		}
		return input;
	}

	
	public void setProcessCodeService(ProcessCodeService processCodeService) {
		this.processCodeService = processCodeService;
	}

}
