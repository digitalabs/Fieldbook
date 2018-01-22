package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AttributeSourceExpression extends BaseExpression {

	public static final String PATTERN_KEY = "\\[ATTRSC\\.([^\\.]*)\\]"; // Example: ATTRSC.NOTES
	private static final Pattern pattern = Pattern.compile(PATTERN_KEY);

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source) {
		// TODO
		for (StringBuilder value : values) {
			this.replaceExpressionWithValue(value, "SomeExampleString");
		}
	}

	@Override
	public String getExpressionKey() {
		return PATTERN_KEY;
	}

	@Override
	protected void replaceExpressionWithValue(StringBuilder container, String value) {
		Matcher matcher = pattern.matcher(container.toString());
		while (matcher.find()) {
			container.replace(matcher.start(), matcher.end(), value);
		}
	}
}
