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
public class AttributeFemaleParentExpression extends BaseExpression {

	// Example: ATTRFP.NOTES
	public static final String ATTRIBUTE_KEY = "ATTRFP";
	public static final String PATTERN_KEY = "\\[" + ATTRIBUTE_KEY + "\\.([^\\.]*)\\]";

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source, final String capturedText) {

		final Method breedingMethod = source.getBreedingMethod();
		Integer gpid1 = null;
		if (AppConstants.METHOD_TYPE_GEN.getString().equals(breedingMethod.getMtype())) {
			// If the method is Generative, GPID1 refers to the GID of the female parent
			gpid1 = Integer.valueOf(source.getFemaleGid());
		} else if (AppConstants.METHOD_TYPE_DER.getString().equals(breedingMethod.getMtype())) {

			final Integer sourceGpid1 = source.getGermplasm().getGpid1();
			final Integer sourceGpid2 = source.getGermplasm().getGpid2();
			final Method sourceMethod = source.getSourceMethod();

			if (sourceMethod != null && sourceMethod.getMtype() != null && AppConstants.METHOD_TYPE_GEN.getString()
					.equals(sourceMethod.getMtype()) || source.getGermplasm().getGnpgs() < 0 && (sourceGpid1 != null && sourceGpid1
					.equals(0)) && (sourceGpid2 != null && sourceGpid2.equals(0))) {
				// If the source breeding method is generative or the source's gpid1 and gpid2 are 0, then gpid1 refers to the immediate source
				gpid1 = Integer.valueOf(source.getGermplasm().getGid());
			} else {
				// if the method is Derivative, GPID1 refers to the group source (the Female Parent GID of the previous advanced generation)
				gpid1 = source.getGermplasm().getGpid1();
			}

		}

		final String attributeName = capturedText.substring(1, capturedText.length() - 1).split("\\.")[1];
		final String attributeValue = germplasmDataManager.getAttributeValue(gpid1, attributeName);

		for (final StringBuilder value : values) {
			this.replaceAttributeExpressionWithValue(value, attributeName, attributeValue);
		}
	}

	@Override
	public String getExpressionKey() {
		return AttributeFemaleParentExpression.PATTERN_KEY;
	}

	protected void replaceAttributeExpressionWithValue(final StringBuilder container, final String attributeName, final String value) {
		final Pattern patternWithAttributeName = Pattern.compile("\\[" + ATTRIBUTE_KEY + "." + attributeName + "\\]");
		final Matcher matcher = patternWithAttributeName.matcher(container.toString());
		while (matcher.find()) {
			container.replace(matcher.start(), matcher.end(), value);
		}
	}
}
