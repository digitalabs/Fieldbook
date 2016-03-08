
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
    public static final int CAPTURED_FINAL_EXPRESSION_GROUP = 1;

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source) {
		for (final StringBuilder value : values) {
			String currentValue = value.toString();
			final String countPrefix = this.getCountPrefix(currentValue);
			String valueWithoutProcessCode = currentValue.replace(countPrefix + this.getExpressionKey(), "");

			final String targetCountExpression = this.getTargetCountExpression(countPrefix);
			final CountResultBean result = this.countExpressionOccurence(targetCountExpression, valueWithoutProcessCode);
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

    public CountResultBean countExpressionOccurence(final String expression, final String currentValue) {
        // our target is the final expression in a line name. However, numbers and other occurences of the expression could be in between
        // e.g., TEST-B-1B-2BBB the final B should still be considered even though there is a 2 and other B's in between
        final Pattern pattern = Pattern.compile("(?:-[1-9]*[0-9]*" + expression + "*(" + expression + "([*][1-9])*))$");
        final Matcher matcher = pattern.matcher(currentValue);

        int startIndex = 0;
        int endIndex = 0;
        int count = 0;
        if (matcher.find()) {
            count = 1;
            // if there is a *n instance found
            if (matcher.groupCount() == 2) {
                final String existingCountString = matcher.group(2);
                if (! StringUtils.isEmpty(existingCountString)) {
                    final int existingCount = Integer.parseInt(existingCountString.replace("*", ""));
                    count += existingCount - 1;
                }

            }

            startIndex = matcher.start(CAPTURED_FINAL_EXPRESSION_GROUP);
            endIndex = matcher.end(CAPTURED_FINAL_EXPRESSION_GROUP);
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