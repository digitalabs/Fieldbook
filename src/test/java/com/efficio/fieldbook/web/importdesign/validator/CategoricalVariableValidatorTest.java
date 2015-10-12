
package com.efficio.fieldbook.web.importdesign.validator;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.junit.Test;
import org.springframework.context.NoSuchMessageException;

import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.data.initializer.DesignImportDataInitializer;
import com.efficio.fieldbook.web.importdesign.validator.CategoricalVariableValidator;

public class CategoricalVariableValidatorTest {

	@Test
	public void testHasPossibleValuesReturnFalseForCategoricalVariableWithoutPossibleValues() throws NoSuchMessageException,
			DesignValidationException {
		final StandardVariable categoricalVariable =
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.VARIATE, DesignImportDataInitializer.AFLAVER_5_ID,
						"AflavER_1_5", "", "", "", DesignImportDataInitializer.CATEGORICAL_VARIABLE, "C", "", "");
		categoricalVariable.setEnumerations(null);

		Assert.assertFalse("Expecting to retun false for categorical variables with no possible values.",
				CategoricalVariableValidator.hasPossibleValues(categoricalVariable));

	}

	@Test
	public void testHasPossibleValuesReturnTrueForCategoricalVariableWithPossibleValues() throws NoSuchMessageException,
			DesignValidationException {
		final StandardVariable categoricalVariable =
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.VARIATE, DesignImportDataInitializer.AFLAVER_5_ID,
						"AflavER_1_5", "", "", "", DesignImportDataInitializer.CATEGORICAL_VARIABLE, "C", "", "");

		Assert.assertTrue("Expecting to retun false for categorical variables with no possible values.",
				CategoricalVariableValidator.hasPossibleValues(categoricalVariable));

	}

	@Test
	public void testIsPartOfValidValuesForCategoricalVariableForNonPossibleValue() throws NoSuchMessageException, DesignValidationException {
		final String nonValidValue = "11";
		final StandardVariable categoricalVariable =
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.VARIATE, DesignImportDataInitializer.AFLAVER_5_ID,
						"AflavER_1_5", "", "", "", DesignImportDataInitializer.CATEGORICAL_VARIABLE, "C", "", "");
		categoricalVariable.setEnumerations(DesignImportDataInitializer.createPossibleValues(10));

		Assert.assertFalse("Expecting to return false when the input is not part of the valid values of the categorical variable.",
				CategoricalVariableValidator.isPartOfValidValuesForCategoricalVariable(nonValidValue, categoricalVariable));

	}

	@Test
	public void testIsPartOfValidValuesForCategoricalVariableForAPossibleValue() throws NoSuchMessageException, DesignValidationException {
		final String nonValidValue = "10";
		final StandardVariable categoricalVariable =
				DesignImportDataInitializer.createStandardVariable(PhenotypicType.VARIATE, DesignImportDataInitializer.AFLAVER_5_ID,
						"AflavER_1_5", "", "", "", DesignImportDataInitializer.CATEGORICAL_VARIABLE, "C", "", "");
		categoricalVariable.setEnumerations(DesignImportDataInitializer.createPossibleValues(10));

		Assert.assertTrue("Expecting to return true when the input is part of the valid values of the categorical variable.",
				CategoricalVariableValidator.isPartOfValidValuesForCategoricalVariable(nonValidValue, categoricalVariable));

	}
}
