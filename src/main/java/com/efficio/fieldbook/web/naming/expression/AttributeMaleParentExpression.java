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
	public static final String ATTRIBUTE_KEY = "ATTRMP";
	public static final String PATTERN_KEY = "\\[" + ATTRIBUTE_KEY + "\\.([^\\.]*)\\]";

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
			this.replaceAttributeExpressionWithValue(value, attributeName ,attributeValue);
		}
	}

	@Override
	public String getExpressionKey() {
		return AttributeMaleParentExpression.PATTERN_KEY;
	}

	protected void replaceAttributeExpressionWithValue(final StringBuilder container, final String attributeName ,final String value) {
		final String key = "[" + ATTRIBUTE_KEY + "." + attributeName + "]";
		int start = container.indexOf(key, 0);
		while (start > -1) {
			int end = start + key.length();
			int nextSearchStart = start + value.length();
			container.replace(start, end, value);
			start = container.indexOf(key, nextSearchStart);
		}
	}
}
