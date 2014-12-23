package com.efficio.fieldbook.web.naming.service;

import java.util.List;

import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;


public interface RulesService {
	
	public List<String> runRules() throws RuleException;
	
	public void setRules(List<Rule> rules);
	
	public void setInitObject(Object object);

}
