package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.trial.bean.AdvancingSource;

@Component
public class CrossTypeExpression extends BaseExpression {

	public static final String KEY = "[CRSTYP]";

	private static final String SINGLE_CROSS = "Single cross";
	private static final String DOUBLE_CROSS = "Double cross";
	private static final String TOP_CROSS_MAIZE = "Test cross";
	private static final String TOP_CROSS_WHEAT = "Three-way cross";
	private static final String BACK_CROSS = "Backcross";

	public static final String WHEAT = "wheat";
	public static final String MAIZE = "maize";

	@Autowired
	private ContextUtil contextUtil;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public CrossTypeExpression() {
	}

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source, final String capturedText) {
		String crossTypeAbbreviation = "";
		final Method breedingMethod = source.getBreedingMethod();

		final String cropName = this.contextUtil.getProjectInContext().getCropType().getCropName();

		if (breedingMethod.getMname().equals(SINGLE_CROSS)) {
			crossTypeAbbreviation = "S";
		} else if (breedingMethod.getMname().equals(DOUBLE_CROSS)) {
			crossTypeAbbreviation = "D";
		} else if (breedingMethod.getMname().equals(BACK_CROSS)) {
			crossTypeAbbreviation = getRecurrentParentType(source.getGermplasm());
		} else if(this.isTopCrossMethod(cropName, breedingMethod)){
			crossTypeAbbreviation = "T";
		}

		for (final StringBuilder container : values) {

			this.replaceExpressionWithValue(container, crossTypeAbbreviation);
		}
	}

	private String getRecurrentParentType(final ImportedGermplasm importedGermplasm){

		final Integer gid = Integer.parseInt(importedGermplasm.getGid());

		final Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(gid);

		if(germplasm.getGpid1() == null || germplasm.getGpid2() == null) {
			return "";
		}

		final Germplasm femaleParent = this.germplasmDataManager.getGermplasmByGID(germplasm.getGpid1());
		final Germplasm maleParent = this.germplasmDataManager.getGermplasmByGID(germplasm.getGpid2());

		if (maleParent.getGnpgs() >= 2
				&& (femaleParent.getGid().equals(maleParent.getGpid1()) || femaleParent.getGid().equals(maleParent.getGpid2()))) {

			return "F";
		} else if (femaleParent.getGnpgs() >= 2
				&& (maleParent.getGid().equals(femaleParent.getGpid1()) || maleParent.getGid().equals(femaleParent.getGpid2()))) {
			return "M";
		}

		return "";
	}

	private boolean isTopCrossMethod(final String cropName, final Method breedingMethod){
		return cropName.equalsIgnoreCase(WHEAT) && breedingMethod.getMname().equals(TOP_CROSS_WHEAT)
				|| cropName.equalsIgnoreCase(MAIZE) && breedingMethod.getMname().equals(TOP_CROSS_MAIZE);
	}

	@Override
	public String getExpressionKey() {
		return CrossTypeExpression.KEY;
	}
}
