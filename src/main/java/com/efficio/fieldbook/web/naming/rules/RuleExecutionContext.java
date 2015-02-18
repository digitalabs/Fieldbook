package com.efficio.fieldbook.web.naming.rules;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public interface RuleExecutionContext {
	public List<String> getExecutionOrder();

	public Object getRuleExecutionOutput();

	public void setCurrentExecutionIndex(int index);

	public int getCurrentExecutionIndex();
}
