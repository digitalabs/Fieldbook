package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public abstract class GroupCountExpression implements Expression {

	public int countContinuousExpressionOccurrence(String expression, String currentValue) {
		Pattern pattern = Pattern.compile("((?:" + expression + ")+)");
		Matcher matcher = pattern.matcher(currentValue);

		String lastMatch = null;
		while (matcher.find()) {
			lastMatch = matcher.group();
		}

		int count = StringUtils.countMatches(lastMatch, expression);

		return count;
	}

	@Override public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			String currentValue = value.toString();

			int count = countContinuousExpressionOccurrence(getTargetCountExpression(),
					currentValue);

			if (count > 2) {

				currentValue = currentValue.replaceAll(getTargetCountExpression(), "");
				currentValue = currentValue.replace(getExpressionKey(),
						getTargetCountExpression() + "*" + String.valueOf(count));
				value.delete(0, value.length());
				value.append(currentValue);
			} else {
				value.delete(0, value.length());
				value.append(currentValue.replace(getExpressionKey(), getTargetCountExpression()));
			}

		}
	}

	public abstract String getTargetCountExpression();
}
