package com.efficio.fieldbook.web.naming.rules;

import java.util.List;
import java.util.Map;

import com.efficio.fieldbook.web.naming.service.ProcessCodeService;


public interface Rule {

	public List<String> runRule(List<String> input) throws RuleException;

	void init(Map<String, Object> context);
}
