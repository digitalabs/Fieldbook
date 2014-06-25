package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public interface Expression {

	public void apply(List<StringBuilder> values, AdvancingSource source);

    public String getExpressionKey();
}
