
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.generationcp.middleware.manager.GermplasmNameType;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.trial.bean.AdvancingSource;

@Component
public class BracketsExpression extends BaseExpression {

	public static final String KEY = "[BRACKETS]";

	public BracketsExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source, final String capturedText) {
		for (StringBuilder container : values) {

			String newRootName = source.getRootName();

			if (source.getRootNameType() != null && this.isCrossNameType(source.getRootNameType())) {

				// if root name already has parentheses
				if (newRootName.charAt(0) != '(' || newRootName.charAt(newRootName.length() - 1) != ')') {
					this.replaceExpressionWithValue(container, ")");
					container.insert(0, "(");
					continue;
				}
			} else {
				this.replaceExpressionWithValue(container, "");
			}
		}
	}

	protected boolean isCrossNameType(Integer nameTypeId) {
		return GermplasmNameType.CROSS_NAME.getUserDefinedFieldID() == nameTypeId
				|| GermplasmNameType.ALTERNATE_CROSS_NAME.getUserDefinedFieldID() == nameTypeId;
	}

	@Override
	public String getExpressionKey() {
		return BracketsExpression.KEY;
	}
}
