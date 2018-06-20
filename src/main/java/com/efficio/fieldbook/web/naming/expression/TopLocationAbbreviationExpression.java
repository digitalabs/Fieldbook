
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.trial.bean.AdvancingSource;

@Component
public class TopLocationAbbreviationExpression extends BaseExpression {

	public static final String KEY = "[TLABBR]";

	public TopLocationAbbreviationExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source, final String capturedText) {
		for (StringBuilder container : values) {
			String rootName = source.getRootName();
			String labbr = source.getLocationAbbreviation() != null ? source.getLocationAbbreviation() : "";
			if (rootName != null && rootName.toString().endsWith("T")) {
                this.replaceExpressionWithValue(container, "TOP" + labbr);
			} else {
				this.replaceExpressionWithValue(container, labbr);
			}
		}
	}

	@Override
	public String getExpressionKey() {
		return TopLocationAbbreviationExpression.KEY;
	}
}
