package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.util.AppConstants;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AttributeSourceExpression extends AttributeExpression {

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public static final String ATTRIBUTE_KEY = "ATTRSC";
	public static final String PATTERN_KEY = "\\[" + ATTRIBUTE_KEY + "\\.([^\\.]*)\\]"; // Example: ATTRSC.NOTES
	private static final Pattern pattern = Pattern.compile(PATTERN_KEY);

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source, final String capturedText) {
		for (StringBuilder value : values) {
			String newValue = "";
			final String attributeName = capturedText.substring(1, capturedText.length() - 1).split("\\.")[1];
			if (source.getBreedingMethod().getMtype().equals(AppConstants.METHOD_TYPE_DER.getString())
				|| source.getBreedingMethod().getMtype().equals(AppConstants.METHOD_TYPE_MAN.getString())) {
				newValue = germplasmDataManager.getAttributeValue(Integer.parseInt(source.getGermplasm().getGid()), attributeName);
			}
			this.replaceAttributeExpressionWithValue(value, ATTRIBUTE_KEY, attributeName, newValue);
		}
	}

	@Override
	public String getExpressionKey() {
		return AttributeSourceExpression.PATTERN_KEY;
	}

}
