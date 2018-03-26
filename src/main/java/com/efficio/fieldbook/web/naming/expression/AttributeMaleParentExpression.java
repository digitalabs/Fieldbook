package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.util.AppConstants;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AttributeMaleParentExpression extends BaseExpression {

	// Example: ATTRMP.NOTES
	public static final String PATTERN_KEY = "\\[ATTRMP\\.([^\\.]*)\\]";
	private static final Pattern pattern = Pattern.compile(PATTERN_KEY);

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source, final String capturedText) {

		final Method sourceMethod = source.getBreedingMethod();
		Integer gpid2 = null;
		if (AppConstants.METHOD_TYPE_GEN.getString().equals(sourceMethod.getMtype())) {
			// If the method is Generative, GPID2 refers to the GID of the male parent
			gpid2 = Integer.valueOf(source.getMaleGid());
		} else if (AppConstants.METHOD_TYPE_DER.getString().equals(sourceMethod.getMtype())) {
			// If the method is Derivative, GPID2 refers to the immediate source (the GID of the previous advanced generation)
			gpid2 = Integer.valueOf(source.getGermplasm().getGid());
		}

		final String attributeName = capturedText.substring(1, capturedText.length() - 1).split("\\.")[1];
		final String attributeValue = germplasmDataManager.getAttributeValue(gpid2, attributeName);

		for (final StringBuilder value : values) {

			if (gpid2 != null && !gpid2.equals(0)) {

				this.replaceExpressionWithValue(value, attributeValue);

			} else {
				this.replaceExpressionWithValue(value, "");
			}

		}
	}

	@Override
	public String getExpressionKey() {
		return AttributeMaleParentExpression.PATTERN_KEY;
	}

	@Override
	protected void replaceExpressionWithValue(final StringBuilder container, final String value) {
		final Matcher matcher = pattern.matcher(container.toString());
		while (matcher.find()) {
			container.replace(matcher.start(), matcher.end(), value);
		}
	}
}
