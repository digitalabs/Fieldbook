package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.web.naming.rules.Rule;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/13/2015
 * Time: 4:56 PM
 */
public class RulesPostProcessor implements BeanPostProcessor{


	private RuleFactory ruleFactory;

	@Override public Object postProcessBeforeInitialization(Object o, String s)
			throws BeansException {
		// do nothing
		return o;
	}

	@Override public Object postProcessAfterInitialization(Object o, String s)
			throws BeansException {

		if (o instanceof Rule) {
			Rule rule = (Rule) o;
			ruleFactory.addRule(rule);
		}

		return o;
	}

	public void setRuleFactory(RuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}
}
