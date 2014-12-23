package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.RulesService;

@Service
public class RulesServiceImpl implements RulesService{
	
	@Resource
	RuleFactory ruleFactory;

	private Object initObject;
	
	private List<Rule> rules = new ArrayList<Rule>();
	
	private List<String> starter = new ArrayList<>();
	
	public RulesServiceImpl(){}
	
	// FIXME : catch RuleExceptions here?
	public List<String> runRules() throws RuleException {
		
		rules = ruleFactory.getRulesToRun();
		
		List<String> input = new ArrayList<>();
		if(!starter.isEmpty()) {
			input = starter;
		}
		for (Rule rule : rules) {
			rule.init(initObject);
			input = rule.runRule(input);
		}
		return input;
	}

	
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	
	public void setInitObject(Object initObject) {
		this.initObject = initObject;
	}
	

}
