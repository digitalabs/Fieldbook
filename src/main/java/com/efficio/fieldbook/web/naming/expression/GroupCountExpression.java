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
public class GroupCountExpression implements Expression {

	public static final String KEY = "[COUNT]";
	public static final String BULK_COUNT_PREFIX = "B*";
	public static final String POUND_COUNT_PREFIX = "#*";

	@Override public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			String currentValue = value.toString();
			String countPrefix = getCountPrefix(currentValue);
			String targetCountExpression = getTargetCountExpression(countPrefix);
			CountResultBean result = countContinuousExpressionOccurrence(targetCountExpression,
					currentValue);

			if (result.getCount() > 2) {
				currentValue = value.replace(result.getStart(), result.getEnd(), "").toString();
				currentValue = currentValue.replace(countPrefix + getExpressionKey(),
						targetCountExpression + "*" + String.valueOf(result.getCount()));
				value.delete(0, value.length());
				value.append(currentValue);
			} else {
				value.delete(0, value.length());
				value.append(currentValue.replace(countPrefix + getExpressionKey(), targetCountExpression));
			}

		}
	}

	protected String getCountPrefix(String input) {
		int start = input.indexOf(KEY);
		return input.substring(start - 2, start);
	}

	protected String getTargetCountExpression(String countPrefix) {
		if (BULK_COUNT_PREFIX.equals(countPrefix)) {
			return "-B";
		} else if (POUND_COUNT_PREFIX.equals(countPrefix)){
			return "-#";
		} else {
			throw new IllegalArgumentException("Invalid count expression");
		}
	}

	public CountResultBean countContinuousExpressionOccurrence(String expression, String currentValue) {
		Pattern pattern = Pattern.compile("((?:" + expression + ")+)");
		Matcher matcher = pattern.matcher(currentValue);

		String lastMatch = null;
		int startIndex = 0;
		int endIndex = 0;
		while (matcher.find()) {
			lastMatch = matcher.group();
			startIndex = matcher.start();
			endIndex = matcher.end();
		}

		int count = StringUtils.countMatches(lastMatch, expression);

		return new CountResultBean(count, startIndex, endIndex);
	}

	@Override public String getExpressionKey() {
			return KEY;
		}

	private class CountResultBean {
		private int count;
		private int start;
		private int end;

		public CountResultBean(int count, int start, int end) {
			this.count = count;
			this.start = start;
			this.end = end;
		}

		public int getCount() {
			return count;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}
	}

}