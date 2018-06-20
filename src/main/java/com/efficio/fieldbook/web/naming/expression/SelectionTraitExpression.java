package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.trial.bean.AdvancingSource;
import org.springframework.stereotype.Component;

@Component
public class SelectionTraitExpression extends BaseExpression {

    public static final String KEY = "[SELTRAIT]";

    public SelectionTraitExpression() {
    }

    @Override
    public void apply(List<StringBuilder> values, AdvancingSource source, final String capturedText) {
        for (StringBuilder container : values) {
            this.replaceExpressionWithValue(container, source.getSelectionTraitValue());
        }

    }

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
