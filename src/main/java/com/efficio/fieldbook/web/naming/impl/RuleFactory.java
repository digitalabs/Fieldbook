package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.web.naming.rules.Rule;

public class RuleFactory {
	
	private static Logger LOG = LoggerFactory.getLogger(RuleFactory.class);

    private Map<String, Rule> availableRules; 
    
    private List<Rule> rulesToRun;
    
	private String[] orderedRulesToRun;
	
	public void init() {
		rulesToRun = fetchRules(orderedRulesToRun);
	}

	private List<Rule> fetchRules(String[] keys) {
		List<Rule> rules = new ArrayList<>();
		for (int i = 0; i < keys.length; i++) {
			rules.add(availableRules.get(keys[i]));
			LOG.debug("Fetching Rule : " + keys[i] + ":" + availableRules.get(keys[i]));
		}
		return rules;
	}

	public void setAvailableRules(Map<String, Rule> availableRulesMap) {
		this.availableRules = availableRulesMap;
	}

	
	public List<Rule> getRulesToRun() {
		return rulesToRun;
	}

	
	public void setRulesToRun(List<Rule> rulesToRun) {
		this.rulesToRun = rulesToRun;
	}

	
	public String[] getOrderedRulesToRun() {
		return orderedRulesToRun;
	}

	
	public void setOrderedRulesToRun(String[] orderedRulesToRun) {
		this.orderedRulesToRun = orderedRulesToRun;
	}
	

}
