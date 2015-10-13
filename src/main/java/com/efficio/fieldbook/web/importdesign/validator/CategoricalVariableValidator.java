
package com.efficio.fieldbook.web.importdesign.validator;

import java.util.List;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;

public class CategoricalVariableValidator {

	private CategoricalVariableValidator() {
		// do nothing
	}

	/**
	 * Returns true if the given value is part of the possible values of the categorical variable
	 * 
	 * @param categoricalValue
	 * @param categoricalVariable
	 * @return
	 */
	public static boolean isPartOfValidValuesForCategoricalVariable(final String categoricalValue,
			final StandardVariable categoricalVariable) {
		final List<Enumeration> possibleValues = categoricalVariable.getEnumerations();

		for (final Enumeration possibleValue : possibleValues) {
			if (categoricalValue.equalsIgnoreCase(possibleValue.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the categorical variable has possible values
	 * 
	 * @param categoricalVariable
	 * @return
	 */
	public static boolean hasPossibleValues(final StandardVariable categoricalVariable) {
		final List<Enumeration> possibleValues = categoricalVariable.getEnumerations();

		if (possibleValues != null) {
			return true;
		}

		return false;
	}
}
