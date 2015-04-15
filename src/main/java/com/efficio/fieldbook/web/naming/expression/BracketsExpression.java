package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.middleware.manager.GermplasmNameType;

import java.util.List;

public class BracketsExpression implements Expression {

	public static final String KEY = "[BRACKETS]";

	public BracketsExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {

			int startIndex = value.toString().toUpperCase().indexOf(KEY);
			int endIndex = startIndex + KEY.length();

			String newRootName = source.getRootName();

			if (source.getRootNameType() != null && isCrossNameType(source.getRootNameType())) {

				//if root name already has parentheses
				if (newRootName.charAt(0) != '('
						|| newRootName.charAt(newRootName.length() - 1) != ')') {
					value.replace(startIndex, endIndex, ")");
					value.insert(0, "(");
					continue;
				}
			} else {
				value.replace(startIndex, endIndex, "");
			}
		}
	}

	protected boolean isCrossNameType(Integer nameTypeId) {
		return GermplasmNameType.CROSS_NAME.getUserDefinedFieldID() == nameTypeId
				|| GermplasmNameType.ALTERNATE_CROSS_NAME.getUserDefinedFieldID() == nameTypeId;
	}

	@Override
	public String getExpressionKey() {
		return KEY;
	}
}