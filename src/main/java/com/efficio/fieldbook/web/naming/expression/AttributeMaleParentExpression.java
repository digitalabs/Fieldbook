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
public class AttributeMaleParentExpression extends AttributeExpression {

	// Example: ATTRMP.NOTES
	public static final String ATTRIBUTE_KEY = "ATTRMP";
	public static final String PATTERN_KEY = "\\[" + ATTRIBUTE_KEY + "\\.([^\\.]*)\\]";

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source, final String capturedText) {

		final Method breedingMethod = source.getBreedingMethod();
		Integer gpid2 = null;
		if (AppConstants.METHOD_TYPE_GEN.getString().equals(breedingMethod.getMtype())) {
			// If the method is Generative, GPID2 refers to male parent of the cross
			gpid2 = Integer.valueOf(source.getMaleGid());
		} else if (AppConstants.METHOD_TYPE_DER.getString().equals(breedingMethod.getMtype()) || AppConstants.METHOD_TYPE_MAN.getString()
				.equals(breedingMethod.getMtype())) {

			// If the method is Derivative or Maintenance, GPID2 refers to the male parent of the group source
			final Integer groupSourceGid = this.getGroupSourceGID(source);
			gpid2 = this.getSourceParentGID(groupSourceGid);

		}

		final String attributeName = capturedText.substring(1, capturedText.length() - 1).split("\\.")[1];
		final String attributeValue = germplasmDataManager.getAttributeValue(gpid2, attributeName);

		for (final StringBuilder value : values) {
			this.replaceAttributeExpressionWithValue(value, ATTRIBUTE_KEY, attributeName, attributeValue);
		}
	}

	@Override
	public String getExpressionKey() {
		return AttributeMaleParentExpression.PATTERN_KEY;
	}

	@Override
	protected Integer getSourceParentGID(final Integer gid) {
		final Germplasm groupSource = this.germplasmDataManager.getGermplasmByGID(gid);
		if (groupSource != null) {
			return groupSource.getGpid2();
		} else {
			return null;
		}
	}
}
