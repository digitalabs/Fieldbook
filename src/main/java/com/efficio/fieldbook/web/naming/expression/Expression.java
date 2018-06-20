
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.trial.bean.AdvancingSource;

public interface Expression {

	public void apply(List<StringBuilder> values, AdvancingSource source, final String capturedText);

	public String getExpressionKey();
}
