package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.naming.service.RulesService;


public class RulesServiceImpl implements RulesService{
	
	@Resource
	ProcessCodeService processCodeService;
	
	private List<Rule> rules = new ArrayList<Rule>();
	
	private List<String> starter = new ArrayList<>();
	
	public void init(String ruleNamespace){}

	public void init(String ruleNamespace, String starter){}
	
	// FIXME : catch RuleExceptions here?
	public List<String> runRules() throws RuleException {
		
		List<String> input = new ArrayList<>();
		if(!starter.isEmpty()) {
			input = starter;
		}
		for (Rule rule : rules) {
			input = rule.runRule(input);
		}
		return input;
	}

	
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}
	

}
