package com.efficio.fieldbook.web.naming.expression;

public abstract class BaseExpression implements Expression{
    protected void replaceExpressionWithValue(StringBuilder container, String value) {
        int startIndex = container.toString().toUpperCase().indexOf(getExpressionKey());
        int endIndex = startIndex + getExpressionKey().length();

        String replaceValue = value == null ? "" : value;
        container.replace(startIndex, endIndex, replaceValue);
    }
}