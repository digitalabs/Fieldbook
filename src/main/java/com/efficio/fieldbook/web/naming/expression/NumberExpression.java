package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class NumberExpression implements Expression {

    public static final String KEY = "[NUMBER]";

    public NumberExpression() {

    }

    @Override
    public void apply(List<StringBuilder> values, AdvancingSource source) {
        if (source.getPlantsSelected() != null &&
                source.getPlantsSelected() > 0) {

            for (StringBuilder value : values) {
                int startIndex = value.indexOf(KEY);
                int endIndex = startIndex + KEY.length();

                Integer newValue = source.getPlantsSelected();
                value.replace(startIndex, endIndex, newValue != null ? newValue.toString() : "");
            }
        }
    }

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
