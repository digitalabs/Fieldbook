package com.efficio.fieldbook.web.naming.rules;

public interface Rule {


	public Object runRule(RuleExecutionContext context) throws RuleException;

	public String getNextRuleStepKey(RuleExecutionContext context);

	public String getKey();
}
