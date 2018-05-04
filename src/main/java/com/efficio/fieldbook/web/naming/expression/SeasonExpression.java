
package com.efficio.fieldbook.web.naming.expression;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.trial.bean.AdvancingSource;

@Component
public class SeasonExpression extends BaseExpression {

	public static final String KEY = "[SEASON]";

	public SeasonExpression() {

	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source, final String capturedText) {
		for (StringBuilder container : values) {


			String newValue = source.getSeason();
			// If a season value is not specified for a Nursery, then default to the current year-month
			if (newValue == null || newValue.equals("")) {
				SimpleDateFormat formatter = new SimpleDateFormat("YYYYMM");
				newValue = formatter.format(new Date());
			}

            this.replaceExpressionWithValue(container, newValue);
		}
	}

	@Override
	public String getExpressionKey() {
		return SeasonExpression.KEY;
	}
}
