
package com.efficio.fieldbook.web.nursery.validator;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.ontology.Scale;

public class NumericVariableValidator {

	/**
	 * Return true if value is within the range of possible values of the numeric variable
	 * 
	 * @param valueToValidate
	 * @param variable
	 * @param numericScale
	 * @return
	 */
	public static boolean isNumericValueWithinTheRange(final String valueToValidate, final StandardVariable variable,
			final Scale numericScale) {
		if (numericScale != null && numericScale.getMinValue() != null && numericScale.getMaxValue() != null) {
			final Double minValue = Double.valueOf(numericScale.getMinValue());
			final Double maxValue = Double.valueOf(numericScale.getMaxValue());

			final Double currentValue = Double.valueOf(valueToValidate);
			if (!(currentValue >= minValue && currentValue <= maxValue)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns true if the input is an valid number and within the specified range of the numeric variable.
	 * 
	 * @param valueToValidate
	 * @param variable
	 * @param numericScale
	 * @return
	 */
	public static boolean isValidNumericValueForNumericVariable(final String valueToValidate, final StandardVariable variable,
			final Scale numericScale) {

		if (!NumberUtils.isNumber(valueToValidate)) {
			return false;
		}

		if (!isNumericValueWithinTheRange(valueToValidate, variable, numericScale)) {
			return false;
		}

		return true;
	}
}
