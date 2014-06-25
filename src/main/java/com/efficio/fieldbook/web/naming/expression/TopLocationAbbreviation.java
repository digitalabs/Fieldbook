package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class TopLocationAbbreviation implements Expression {

    public static final String KEY = "[TLABBR]";

	public TopLocationAbbreviation() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			int startIndex = value.indexOf(KEY);
			int endIndex = startIndex + KEY.length();

			String rootName = source.getRootName();
			String labbr = source.getLocationAbbreviation() != null ? source.getLocationAbbreviation() : "";
			if (rootName != null && rootName.toString().endsWith("T")) {
				value.replace(startIndex, endIndex, "TOP" + labbr);
			}
			else {
				value.replace(startIndex, endIndex, labbr);
			}
		}
	}

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
