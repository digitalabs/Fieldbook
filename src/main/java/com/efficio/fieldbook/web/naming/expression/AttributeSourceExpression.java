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
public class AttributeSourceExpression extends BaseExpression {

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public static final String PATTERN_KEY = "\\[ATTRSC\\.([^\\.]*)\\]"; // Example: ATTRSC.NOTES
	private static final Pattern pattern = Pattern.compile(PATTERN_KEY);

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source, final String capturedText) {
		for (StringBuilder value : values) {
			String newValue = "";
			if (source.getBreedingMethod().getMtype().equals(AppConstants.METHOD_TYPE_DER.getString())
				|| source.getBreedingMethod().getMtype().equals(AppConstants.METHOD_TYPE_MAN.getString())) {
				final String attributeName = capturedText.substring(1, capturedText.length() - 1).split("\\.")[1];
				newValue = germplasmDataManager.getAttributeValue(Integer.parseInt(source.getGermplasm().getGid()), attributeName);
			}
			this.replaceExpressionWithValue(value, newValue);
		}
	}

	@Override
	public String getExpressionKey() {
		return AttributeSourceExpression.PATTERN_KEY;
	}

	@Override
	protected void replaceExpressionWithValue(final StringBuilder container, final String value) {
		final Matcher matcher = pattern.matcher(container.toString());
		while (matcher.find()) {
			container.replace(matcher.start(), matcher.end(), value);
		}
	}
}
