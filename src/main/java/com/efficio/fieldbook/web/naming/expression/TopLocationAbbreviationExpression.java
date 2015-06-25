
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.springframework.stereotype.Component;

@Component
public class TopLocationAbbreviationExpression implements Expression {

	public static final String KEY = "[TLABBR]";

	public TopLocationAbbreviationExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			int startIndex = value.toString().toUpperCase().indexOf(TopLocationAbbreviationExpression.KEY);
			int endIndex = startIndex + TopLocationAbbreviationExpression.KEY.length();

			String rootName = source.getRootName();
			String labbr = source.getLocationAbbreviation() != null ? source.getLocationAbbreviation() : "";
			if (rootName != null && rootName.toString().endsWith("T")) {
				value.replace(startIndex, endIndex, "TOP" + labbr);
			} else {
				value.replace(startIndex, endIndex, labbr);
			}
		}
	}

	@Override
	public String getExpressionKey() {
		return TopLocationAbbreviationExpression.KEY;
	}
}
