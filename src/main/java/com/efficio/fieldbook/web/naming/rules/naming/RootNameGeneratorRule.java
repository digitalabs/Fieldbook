
package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.expression.RootNameExpression;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.commons.ruleengine.OrderedRule;
import org.generationcp.commons.ruleengine.RuleException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RootNameGeneratorRule extends OrderedRule<NamingRuleExecutionContext> {
	public static final String KEY = "RootNameGenerator";
	

	@Override
	public Object runRule(NamingRuleExecutionContext context) throws RuleException {

		RootNameExpression rootNameExpression = new RootNameExpression();
		AdvancingSource advancingSource = context.getAdvancingSource();

		List<StringBuilder> builders = new ArrayList<>();
		builders.add(new StringBuilder());
		rootNameExpression.apply(builders, advancingSource);

		List<String> input = context.getCurrentData();

		String name = builders.get(0).toString();
		if (name.length() == 0) {
			throw new RuleException("error.advancing.nursery.no.root.name.found",
					new Object[] {advancingSource.getGermplasm().getEntryId()}, LocaleContextHolder.getLocale());
		}

		input.add(name);

		context.setCurrentData(input);

		return input;
	}

	@Override public String getKey() {
		return KEY;
	}
}
