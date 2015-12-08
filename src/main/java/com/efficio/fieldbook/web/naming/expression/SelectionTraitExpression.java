package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.springframework.stereotype.Component;

@Component
public class SelectionTraitExpression implements Expression {

    public static final String KEY = "[SELTRAIT]";

    public SelectionTraitExpression() {
    }

    @Override
    public void apply(List<StringBuilder> values, AdvancingSource source) {
        for (StringBuilder value : values) {
            int startIndex = value.toString().toUpperCase().indexOf(SeasonExpression.KEY);
            int endIndex = startIndex + SeasonExpression.KEY.length();

            String selectionTrait = source.getSelectionTraitValue() == null ? "" : source.getSelectionTraitValue();
            value.replace(startIndex, endIndex, selectionTrait);
        }

    }

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
