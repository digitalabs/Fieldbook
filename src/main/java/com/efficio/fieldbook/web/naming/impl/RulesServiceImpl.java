package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.rules.RuleExecutionContext;
import com.efficio.fieldbook.web.naming.service.RulesService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class RulesServiceImpl implements RulesService{

	@Resource
	private RuleFactory ruleFactory;
	
	public RulesServiceImpl(){}
	
	// FIXME : catch RuleExceptions here?
	public Object runRules(RuleExecutionContext context) throws RuleException {
		List<String> sequenceOrder = context.getExecutionOrder();

		assert (!sequenceOrder.isEmpty());
		Rule rule = ruleFactory.getRule(sequenceOrder.get(0));

		while (rule != null) {
			rule.runRule(context);
			rule = ruleFactory.getRule(rule.getNextRuleStepKey(context));
		}

		return context.getRuleExecutionOutput();

	}

	public void setRuleFactory(RuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}
}