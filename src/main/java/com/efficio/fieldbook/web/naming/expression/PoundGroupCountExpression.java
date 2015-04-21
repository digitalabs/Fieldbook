package com.efficio.fieldbook.web.naming.expression;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public class PoundGroupCountExpression extends GroupCountExpression{

	public static final String KEY = "#*[COUNT]";

	@Override public String getTargetCountExpression() {
		return "-#";
	}

	@Override public String getExpressionKey() {
		return KEY;
	}
}
