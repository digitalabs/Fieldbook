package com.efficio.fieldbook.web.naming.expression;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class BulkCountExpression extends Expression {

	public BulkCountExpression(AdvancingSource source) {
		super(source);
	}

	@Override
	public void apply(List<StringBuilder> values) {
		for (StringBuilder value : values) {
			int startIndex = value.indexOf(Expression.BULK_COUNT);
			int endIndex = startIndex + Expression.BULK_COUNT.length();
			String count = "";
		
			if (getSource().getRootName() != null) {
				String countStr = null;
				if (getSource().getRootName().contains("-B")) { //original is a bulk
					countStr = "1";
				}
				else {
					Pattern pattern = Pattern.compile("(.*)-([0-9]*)B(.*)");
					Matcher matcher = pattern.matcher(getSource().getRootName());
					if (matcher.find()) { //original is a bulk with number
						for (int i = matcher.groupCount(); i >= 1; i--) {
							String temp = matcher.group(i);
							if (getSource().getRootName().contains("-" + temp + "B") && temp.matches("[0-9]*")) {
								countStr = temp;
								break;
							}
						}
					}
				}
				if (countStr != null) {
					count = String.valueOf(Integer.valueOf(countStr) + 1);
				}
				else {
					count = "";
				}
			}
			value.replace(startIndex, endIndex, "-" + count + "B");
		}
	}
}
