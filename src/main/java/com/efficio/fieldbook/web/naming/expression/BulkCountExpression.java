package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.util.ExpressionHelper;
import com.efficio.fieldbook.util.ExpressionHelperCallback;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

public class BulkCountExpression implements Expression {

    public static final String KEY = "[BCOUNT]";

	public BulkCountExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			int startIndex = value.toString().toUpperCase().indexOf(KEY);
			int endIndex = startIndex + KEY.length();
		
			if (source.getRootName() != null) {
				BulkExpressionHelperCallback callback = new BulkExpressionHelperCallback();
				ExpressionHelper.evaluateExpression(source.getRootName(), "-([0-9]*)B", callback);
				
				StringBuilder lastBulkCount = callback.getLastBulkCount();
				if (lastBulkCount.length() > 0) {
					value.replace(startIndex, endIndex, "-" + Integer.valueOf(lastBulkCount.toString()) + 1 + "B");
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

		@Override public void evaluateCapturedExpression(String capturedText, String originalInput,
				int start, int end) {
			if ("-B".equals(capturedText)) {
				lastBulkCount.replace(0, lastBulkCount.length(), "1");
			} else {
				String newCapturedText = capturedText.replaceAll("[-B]*", "");
				if (newCapturedText != null && NumberUtils.isNumber(newCapturedText)) {
					lastBulkCount.replace(0, lastBulkCount.length(), newCapturedText);
				}
			}
		}

		public StringBuilder getLastBulkCount() {
			return lastBulkCount;
		}
	}

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
