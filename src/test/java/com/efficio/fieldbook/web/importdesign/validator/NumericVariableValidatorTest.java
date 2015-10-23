
package com.efficio.fieldbook.web.importdesign.validator;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.ontology.Scale;
import org.junit.Test;

import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.importdesign.validator.NumericVariableValidator;

public class NumericVariableValidatorTest {

	@Test
	public void testIsNumericValueWithinTheRangeReturnsFalseForValuesBeyondRange() {

		final StandardVariable variable =
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.VARIATE, 20421, "SPAD_CCI", "", "", "",
						DesignImportTestDataInitializer.NUMERIC_VARIABLE, "N", "", "");
		final Scale numericScale = new Scale();
		numericScale.setMinValue("1");
		numericScale.setMaxValue("100");
		variable.setScale(numericScale);

		String valueToValidate = "123";
		Assert.assertFalse("Expecting a false to return for a value greater than the maximum value.",
				NumericVariableValidator.isNumericValueWithinTheRange(valueToValidate, variable, numericScale));

		valueToValidate = "0";
		Assert.assertFalse("Expecting a false to return for a value lesser than the minimum value.",
				NumericVariableValidator.isNumericValueWithinTheRange(valueToValidate, variable, numericScale));

	}

	@Test
	public void testIsNumericValueWithinTheRangeReturnsFalseForValuesWithinTheRange() {

		final StandardVariable variable =
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.VARIATE, 20421, "SPAD_CCI", "", "", "",
						DesignImportTestDataInitializer.NUMERIC_VARIABLE, "N", "", "");
		final Scale numericScale = new Scale();
		numericScale.setMinValue("1");
		numericScale.setMaxValue("100");
		variable.setScale(numericScale);

		String valueToValidate = "100";
		Assert.assertTrue("Expecting a true to return for a value equal to the maximum value.",
				NumericVariableValidator.isNumericValueWithinTheRange(valueToValidate, variable, numericScale));

		valueToValidate = "1";
		Assert.assertTrue("Expecting a true to return for a value equal to the minimum value.",
				NumericVariableValidator.isNumericValueWithinTheRange(valueToValidate, variable, numericScale));

		valueToValidate = "50";
		Assert.assertTrue("Expecting a true to return for a value within the range.",
				NumericVariableValidator.isNumericValueWithinTheRange(valueToValidate, variable, numericScale));

	}

	@Test
	public void testIsNumericValueWithinTheRangeReturnsTrueWhenTheScaleOfStandardVariableHasNoSpecifiedRange() {

		final StandardVariable variable =
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.VARIATE, 20421, "SPAD_CCI", "", "", "",
						DesignImportTestDataInitializer.NUMERIC_VARIABLE, "N", "", "");
		final Scale numericScale = new Scale();
		variable.setScale(numericScale);

		final String valueToValidate = "100";
		Assert.assertTrue("Expecting to return true when the scale of the numeric variable has no specified range.",
				NumericVariableValidator.isNumericValueWithinTheRange(valueToValidate, variable, numericScale));

	}

	@Test
	public void testIsValidNumericValueForNumericVariable() {
		final StandardVariable variable =
				DesignImportTestDataInitializer.createStandardVariable(PhenotypicType.VARIATE, 20421, "SPAD_CCI", "", "", "",
						DesignImportTestDataInitializer.NUMERIC_VARIABLE, "N", "", "");
		final Scale numericScale = new Scale();
		numericScale.setMinValue("1");
		numericScale.setMaxValue("100");
		variable.setScale(numericScale);

		// checking if the input is a number
		String valueToValidate = "no0";
		Assert.assertFalse("Expected to return false for non-numeric input.",
				NumericVariableValidator.isValidNumericValueForNumericVariable(valueToValidate, variable, numericScale));

		valueToValidate = "10";
		Assert.assertTrue("Expected to return true for numeric input that is within the range of possible values of the numeric variable.",
				NumericVariableValidator.isValidNumericValueForNumericVariable(valueToValidate, variable, numericScale));
	}
}
