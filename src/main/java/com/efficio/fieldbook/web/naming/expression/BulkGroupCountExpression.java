package com.efficio.fieldbook.web.naming.expression;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public class BulkGroupCountExpression extends GroupCountExpression{

	public static final String KEY = "B*[COUNT]";

	@Override public String getExpressionKey() {
		return KEY;
	}

	@Override public String getTargetCountExpression() {
		return "-B";
	}
}