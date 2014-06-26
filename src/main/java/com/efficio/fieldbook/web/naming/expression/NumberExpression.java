package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class NumberExpression implements Expression {

    public static final String KEY = "[NUMBER]";

    public NumberExpression() {

    }

    @Override
    public void apply(List<StringBuilder> values, AdvancingSource source) {
        for (StringBuilder value : values) {
            int startIndex = value.toString().toUpperCase().indexOf(KEY);
            int endIndex = startIndex + KEY.length();

            if (source.getPlantsSelected() != null &&
                    source.getPlantsSelected() > 0) {

                Integer newValue = source.getPlantsSelected();
                value.replace(startIndex, endIndex, newValue != null ? newValue.toString() : "");
            }
            else {
                value.replace(startIndex, endIndex, "");
            }
        }
    }

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
