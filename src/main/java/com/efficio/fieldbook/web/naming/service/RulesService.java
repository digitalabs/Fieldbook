package com.efficio.fieldbook.web.naming.service;

import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.rules.RuleExecutionContext;


public interface RulesService {
	
	public Object runRules(RuleExecutionContext context) throws RuleException;
}
