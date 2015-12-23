
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;


@Component
public class GroupCountExpression extends BaseExpression {

	public static final String KEY = "[COUNT]";
    public static final Integer MINIMUM_BULK_COUNT = 3;
	public static final String BULK_COUNT_PREFIX = "B*";
	public static final String POUND_COUNT_PREFIX = "#*";
    public static final String SEPARATOR = "-";

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			String currentValue = value.toString();
			String countPrefix = this.getCountPrefix(currentValue);
			String valueWithoutProcessCode = currentValue.replace(countPrefix + this.getExpressionKey(), "");

			if (valueWithoutProcessCode.charAt(valueWithoutProcessCode.length() - 1) == SEPARATOR.charAt(0)) {
				valueWithoutProcessCode = valueWithoutProcessCode.substring(0, valueWithoutProcessCode.length() - 1);
			}

			String targetCountExpression = this.getTargetCountExpression(countPrefix);
			CountResultBean result = this.countContinuousExpressionOccurrence(targetCountExpression, valueWithoutProcessCode);
            currentValue = this.cleanupString(new StringBuilder(valueWithoutProcessCode), result);
			if (result.getCount() > MINIMUM_BULK_COUNT) {

				currentValue = currentValue + targetCountExpression + "*" + String.valueOf(result.getCount());
				value.delete(0, value.length());
				value.append(currentValue);
			} else {
				value.delete(0, value.length());
                value.append(currentValue);
                value.append(SEPARATOR);
                String repeatingLetter = targetCountExpression.substring(targetCountExpression.length() - 1,targetCountExpression.length() );

                // do while loop is used because there should be a -B or -# appended if the count is 0
                int i = 0;
                do {
                    value.append(repeatingLetter);
                    i++;
                } while (i < result.getCount());

            }

		}
	}

	protected String cleanupString(StringBuilder value, CountResultBean result) {
		value.replace(result.getStart(), result.getEnd(), "");

		return value.toString();
	}

	protected String getCountPrefix(String input) {
		int start = input.indexOf(GroupCountExpression.KEY);
		return input.substring(start - 2, start);
	}

	protected String getTargetCountExpression(String countPrefix) {
		if (GroupCountExpression.BULK_COUNT_PREFIX.equals(countPrefix)) {
			return "-B";
		} else if (GroupCountExpression.POUND_COUNT_PREFIX.equals(countPrefix)) {
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

	@Override
	public String getExpressionKey() {
		return GroupCountExpression.KEY;
	}

	private class CountResultBean {

		private final int count;
		private final int start;
		private final int end;

		public CountResultBean(int count, int start, int end) {
			this.count = count;
			this.start = start;
			this.end = end;
		}

		public int getCount() {
			return this.count;
		}

		public int getStart() {
			return this.start;
		}

		public int getEnd() {
			return this.end;
		}
	}

}
