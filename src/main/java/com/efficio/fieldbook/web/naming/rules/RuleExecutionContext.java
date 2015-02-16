package com.efficio.fieldbook.web.naming.rules;

import com.efficio.fieldbook.web.naming.impl.RuleFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public interface RuleExecutionContext {
	public List<String> getExecutionOrder();

	public Object getRuleExecutionOutput();
}
