
package com.efficio.fieldbook.web.naming.rules.naming;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.web.naming.expression.Expression;
import com.efficio.fieldbook.web.naming.expression.RootNameExpression;
import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class RootNameGeneratorRule implements Rule {
	
	private ProcessCodeService processCodeService;
	private Expression rootNameExpression;
	private AdvancingSource advancingSource;

	public RootNameGeneratorRule() {
	}
	
	@Override
	public void init(ProcessCodeService processCodeService, Object advancingSource) {
		rootNameExpression = new RootNameExpression();
		this.advancingSource = (AdvancingSource) advancingSource;
	}

	// FIXME : the so called expression may in fact just be a rule
	@Override
	public List<String> runRule(List<String> input) throws RuleException {

		List<StringBuilder> builders = new ArrayList<StringBuilder>();
		builders.add(new StringBuilder());
		rootNameExpression.apply(builders, advancingSource);

		String name = builders.get(0).toString();
		if (name.length() == 0) {
			throw new RuleException("error.advancing.nursery.no.root.name.found",
					new Object[] {advancingSource.getGermplasm().getEntryId()}, LocaleContextHolder.getLocale());
		}
		input.add(name);

		return input;
	}

}
