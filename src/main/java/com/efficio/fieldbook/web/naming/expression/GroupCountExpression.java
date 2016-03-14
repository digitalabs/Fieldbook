
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
    public static final Integer MINIMUM_BULK_COUNT = 2;
	public static final String BULK_COUNT_PREFIX = "B*";
	public static final String POUND_COUNT_PREFIX = "#*";
    public static final String SEPARATOR = "-";

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source) {
		for (final StringBuilder value : values) {
			String currentValue = value.toString();
			final String countPrefix = this.getCountPrefix(currentValue);
			String valueWithoutProcessCode = currentValue.replace(countPrefix + this.getExpressionKey(), "");

			if (valueWithoutProcessCode.charAt(valueWithoutProcessCode.length() - 1) == SEPARATOR.charAt(0)) {
				valueWithoutProcessCode = valueWithoutProcessCode.substring(0, valueWithoutProcessCode.length() - 1);
			}

			final String targetCountExpression = this.getTargetCountExpression(countPrefix);
			final CountResultBean result = this.countContinuousExpressionOccurrence(targetCountExpression, valueWithoutProcessCode);
            currentValue = this.cleanupString(new StringBuilder(valueWithoutProcessCode), result);
            int generatedCountValue = result.getCount();

            // if the method is a bulking method, we're expected to increment the count
            if (source.getBreedingMethod().isBulkingMethod()) {
                 generatedCountValue = result.getCount() + 1;
            }

			if (generatedCountValue >= MINIMUM_BULK_COUNT) {

				currentValue = currentValue + targetCountExpression + "*" + String.valueOf(generatedCountValue);
				value.delete(0, value.length());
				value.append(currentValue);
			} else {
				value.delete(0, value.length());
                value.append(currentValue);
                value.append(SEPARATOR);
                final String repeatingLetter = targetCountExpression.substring(targetCountExpression.length() - 1,targetCountExpression.length() );

                // do while loop is used because there should be a -B or -# appended if the count is 0
                int i = 0;
                do {
                    value.append(repeatingLetter);
                    i++;
                } while (i < result.getCount());

            }

		}
	}

	protected String cleanupString(final StringBuilder value, final CountResultBean result) {
		value.replace(result.getStart(), result.getEnd(), "");

		return value.toString();
	}

	protected String getCountPrefix(final String input) {
		final int start = input.indexOf(GroupCountExpression.KEY);
		return input.substring(start - 2, start);
	}

	protected String getTargetCountExpression(final String countPrefix) {
		if (GroupCountExpression.BULK_COUNT_PREFIX.equals(countPrefix)) {
			return "-B";
		} else if (GroupCountExpression.POUND_COUNT_PREFIX.equals(countPrefix)) {
			return "-#";
		} else {
			throw new IllegalArgumentException("Invalid count expression");
		}
	}

	public CountResultBean countContinuousExpressionOccurrence(final String expression, final String currentValue) {
		final Pattern pattern = Pattern.compile("((?:" + expression + "([*][1-9])?)+)$");
		final Matcher matcher = pattern.matcher(currentValue);

		String lastMatch = null;
		int startIndex = 0;
		int endIndex = 0;
        int existingCount = 0;
		if (matcher.find()) {
            // if there is no *n instance found
            if (matcher.groupCount() == 1) {
                lastMatch = matcher.group();
            } else {
                lastMatch = matcher.group(1);
                final String existingCountString = matcher.group(2);
                if (! StringUtils.isEmpty(existingCountString)) {
                    existingCount = Integer.parseInt(existingCountString.replace("*", ""));
                }
            }

			startIndex = matcher.start();
			endIndex = matcher.end();
		}

		int count = StringUtils.countMatches(lastMatch, expression);
        if (existingCount > 0) {
            // we increment the count by the value of n (from -B*n) and subtract by 1 to remove double counting
            count += existingCount - 1;
        }

		return new CountResultBean(count, startIndex, endIndex);
	}

	@Override
	public String getExpressionKey() {
		return GroupCountExpression.KEY;
	}

    class CountResultBean {

		private final int count;
		private final int start;
		private final int end;

		public CountResultBean(final int count, final int start, final int end) {
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