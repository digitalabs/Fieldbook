
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.efficio.fieldbook.util.ExpressionHelper;
import com.efficio.fieldbook.util.ExpressionHelperCallback;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.springframework.stereotype.Component;

@Component
public class BulkCountExpression implements Expression {

	public static final String KEY = "[BCOUNT]";

	public BulkCountExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			int startIndex = value.toString().toUpperCase().indexOf(BulkCountExpression.KEY);
			int endIndex = startIndex + BulkCountExpression.KEY.length();

			if (source.getRootName() != null) {
				BulkExpressionHelperCallback callback = new BulkExpressionHelperCallback();
				ExpressionHelper.evaluateExpression(source.getRootName(), "-([0-9]*)B", callback);

				StringBuilder lastBulkCount = callback.getLastBulkCount();
				if (lastBulkCount.length() > 0) {
					value.replace(startIndex, endIndex, "-" + (Integer.valueOf(lastBulkCount.toString()) + 1) + "B");
				} else {
					value.replace(startIndex, endIndex, "-B");
				}
			} else {
				value.replace(startIndex, endIndex, "-B");
			}
		}
	}

	private class BulkExpressionHelperCallback implements ExpressionHelperCallback {

		final StringBuilder lastBulkCount = new StringBuilder();

		@Override
		public void evaluateCapturedExpression(String capturedText, String originalInput, int start, int end) {
			if ("-B".equals(capturedText)) {
				this.lastBulkCount.replace(0, this.lastBulkCount.length(), "1");
			} else {
				String newCapturedText = capturedText.replaceAll("[-B]*", "");
				if (newCapturedText != null && NumberUtils.isNumber(newCapturedText)) {
					this.lastBulkCount.replace(0, this.lastBulkCount.length(), newCapturedText);
				}
			}
		}

		public StringBuilder getLastBulkCount() {
			return this.lastBulkCount;
		}
	}

	@Override
	public String getExpressionKey() {
		return BulkCountExpression.KEY;
	}
}
