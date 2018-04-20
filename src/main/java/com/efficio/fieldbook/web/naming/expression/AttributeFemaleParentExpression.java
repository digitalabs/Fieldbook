package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.util.AppConstants;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AttributeFemaleParentExpression extends AttributeExpression {

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
		} else if (AppConstants.METHOD_TYPE_DER.getString().equals(breedingMethod.getMtype()) || AppConstants.METHOD_TYPE_MAN.getString()
				.equals(breedingMethod.getMtype())) {

			// if the method is Derivative or Maintenance, GPID1 refers to the female parent of the group source
			final Integer groupSourceGID = getGroupSourceGID(source);
			gpid1 = getSourceParentGID(groupSourceGID);

		}

		final String attributeName = capturedText.substring(1, capturedText.length() - 1).split("\\.")[1];
		final String attributeValue = germplasmDataManager.getAttributeValue(gpid1, attributeName);

		for (final StringBuilder value : values) {
			replaceAttributeExpressionWithValue(value, ATTRIBUTE_KEY, attributeName, attributeValue);
		}
	}

	@Override
	public String getExpressionKey() {
		return AttributeFemaleParentExpression.PATTERN_KEY;
	}

	protected Integer getSourceParentGID(final Integer gid) {
		final Germplasm groupSource = this.germplasmDataManager.getGermplasmByGID(gid);
		if (groupSource != null) {
			return groupSource.getGpid1();
		} else {
			return null;
		}
	}
}
