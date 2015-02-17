package com.efficio.fieldbook.web.naming.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.efficio.fieldbook.web.naming.RuleConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.web.naming.rules.Rule;

import javax.annotation.Resource;

public class RuleFactory {
	
	private static Logger LOG = LoggerFactory.getLogger(RuleFactory.class);

    private Map<String, Rule> availableRules;

	private Map<String, String[]> ruleOrder;

	@Resource
	private RuleConfigurationProvider configProvider;

	public RuleFactory() {
		availableRules = new HashMap<>();
		ruleOrder = new HashMap<>();
	}

	public void init() {
		this.ruleOrder = configProvider.retrieveRuleSequenceConfiguration();
	}

	public void setAvailableRules(Map<String, Rule> availableRulesMap) {
		this.availableRules = availableRulesMap;
	}

	public void addRule(Rule rule) {
		availableRules.put(rule.getKey(), rule);
	}

	public Rule getRule(String key) {
		if (key == null) {
			return null;
		}

		return availableRules.get(key);
	}

	public int getAvailableRuleCount() {
		return availableRules.size();
	}

	public String[] getRuleSequenceForNamespace(String namespace) {
		if (!ruleOrder.containsKey(namespace)) {
			return null;
		}

		return ruleOrder.get(namespace);
	}

	public Collection<String> getAvailableConfiguredNamespaces() {
		return ruleOrder.keySet();
	}
}