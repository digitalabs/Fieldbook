
package com.efficio.fieldbook.web.importdesign.validator;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.junit.Test;
import org.springframework.context.NoSuchMessageException;

import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;

public class CategoricalVariableValidatorTest {

	@Test
	public void testIsPartOfValidValuesForCategoricalVariableForNonPossibleValue() throws NoSuchMessageException, DesignValidationException {
		final String nonValidValue = "11";
		final StandardVariable categoricalVariable =
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.VARIATE,
						DesignImportTestDataInitializer.AFLAVER_5_ID, "AflavER_1_5", "", "", "",
						DesignImportTestDataInitializer.CATEGORICAL_VARIABLE, "C", "", "");
		categoricalVariable.setEnumerations(DesignImportTestDataInitializer.createPossibleValues(10));

		Assert.assertFalse("Expecting to return false when the input is not part of the valid values of the categorical variable.",
				CategoricalVariableValidator.isPartOfValidValuesForCategoricalVariable(nonValidValue, categoricalVariable));

	}

	@Test
	public void testIsPartOfValidValuesForCategoricalVariableForAPossibleValue() throws NoSuchMessageException, DesignValidationException {
		final String nonValidValue = "10";
		final StandardVariable categoricalVariable =
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.VARIATE,
						DesignImportTestDataInitializer.AFLAVER_5_ID, "AflavER_1_5", "", "", "",
						DesignImportTestDataInitializer.CATEGORICAL_VARIABLE, "C", "", "");
		categoricalVariable.setEnumerations(DesignImportTestDataInitializer.createPossibleValues(10));

		Assert.assertTrue("Expecting to return true when the input is part of the valid values of the categorical variable.",
				CategoricalVariableValidator.isPartOfValidValuesForCategoricalVariable(nonValidValue, categoricalVariable));

	}
}
