package com.efficio.fieldbook.web.naming.rules;

import java.util.List;


public interface Rule {
	
	public List<String> runRule(List<String> input) throws RuleException;

	void init(Object initObject); 

}
