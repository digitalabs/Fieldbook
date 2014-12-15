package com.efficio.fieldbook.web.naming.rules;

import java.util.List;

import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;


public class SeparatorRule implements Rule {
	
	ProcessCodeService processCodeService;
	
	private String separator;
	
	@Override
	public void init(ProcessCodeService processCodeService, Object source) {
		this.processCodeService = processCodeService;
		separator = ((AdvancingSource) source).getBreedingMethod().getSeparator();
	}

	@Override
	public List<String> runRule(List<String> input) throws RuleException {
		// append a separator string onto each element of the list - in place
		for (int i = 0; i < input.size(); i++) {
			input.set(i, input.get(i) + separator);
		}
		return input;
	}

}
