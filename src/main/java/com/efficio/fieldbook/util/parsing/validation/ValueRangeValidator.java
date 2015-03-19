package com.efficio.fieldbook.util.parsing.validation;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/26/2015
 * Time: 5:20 PM
 */
public class ValueRangeValidator extends ParsingValidator{

	private List<String> acceptedValues;
	public static final String GENERIC_INVALID_VALUE_MESSAGE = "common.parse.validation.error.unaccepted.value";

	public ValueRangeValidator(List<String> acceptedValues) {
		this(acceptedValues, true);
	}

	public ValueRangeValidator(List<String> acceptedValueParam, boolean skipIfEmpty) {
		super(skipIfEmpty);

		this.acceptedValues = new ArrayList<>();
		if (acceptedValueParam != null) {
			for (String acceptedValue : acceptedValueParam) {
				acceptedValues.add(acceptedValue.toUpperCase());
			}
		}

		setValidationErrorMessage(GENERIC_INVALID_VALUE_MESSAGE);
	}

	@Override public boolean isParsedValueValid(String value) {

		if (StringUtils.isEmpty(value)) {
			return isSkipIfEmpty();
		} else if (acceptedValues.isEmpty()) {
			return true;
		} else {
			return acceptedValues.contains(value.toUpperCase());
		}
	}
}
