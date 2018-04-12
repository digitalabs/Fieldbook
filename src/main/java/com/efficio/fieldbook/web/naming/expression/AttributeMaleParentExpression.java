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
public class AttributeMaleParentExpression extends BaseExpression {

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
			final Integer groupSourceGid = this.getGroupSourceGid(source);
			final Germplasm groupSource = this.germplasmDataManager.getGermplasmByGID(groupSourceGid);
			if (groupSource != null) {
				gpid2 = groupSource.getGpid2();
			}
		}

		final String attributeName = capturedText.substring(1, capturedText.length() - 1).split("\\.")[1];
		final String attributeValue = germplasmDataManager.getAttributeValue(gpid2, attributeName);

		for (final StringBuilder value : values) {
			this.replaceAttributeExpressionWithValue(value, attributeName, attributeValue);
		}
	}

	protected Integer getGroupSourceGid(final AdvancingSource source) {

		final Integer sourceGpid1 = source.getGermplasm().getGpid1();
		final Integer sourceGpid2 = source.getGermplasm().getGpid2();
		final Method sourceMethod = source.getSourceMethod();

		if (sourceMethod != null && sourceMethod.getMtype() != null && AppConstants.METHOD_TYPE_GEN.getString()
				.equals(sourceMethod.getMtype()) || source.getGermplasm().getGnpgs() < 0 && (sourceGpid1 != null && sourceGpid1.equals(0))
				&& (sourceGpid2 != null && sourceGpid2.equals(0))) {
			// If the source germplasm is a new CROSS, then the group source is the cross itself
			return Integer.valueOf(source.getGermplasm().getGid());
		} else {
			// Else group source gid is always the female parent of the source germplasm.
			return source.getGermplasm().getGpid1();
		}

	}

	@Override
	public String getExpressionKey() {
		return AttributeMaleParentExpression.PATTERN_KEY;
	}

	protected void replaceAttributeExpressionWithValue(final StringBuilder container, final String attributeName, final String value) {
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
