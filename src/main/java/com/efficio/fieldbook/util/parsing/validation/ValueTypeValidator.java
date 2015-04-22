package com.efficio.fieldbook.util.parsing.validation;

import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.util.DateUtil;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/26/2015
 * Time: 5:27 PM
 */
public class ValueTypeValidator extends ParsingValidator{

	private Class expectedClass;

	public static final String GENERIC_INVALID_TYPE_MESSAGE = "common.parse.validation.error.invalid.type";

	public ValueTypeValidator(Class expectedClass) {
		this(expectedClass, true);
	}

	public ValueTypeValidator(Class expectedClass, boolean skipIfEmpty) {
		super(skipIfEmpty);
		this.expectedClass = expectedClass;
		setValidationErrorMessage(GENERIC_INVALID_TYPE_MESSAGE);
	}

	@Override public boolean isParsedValueValid(String value) {

		if (StringUtils.isEmpty(value) && isSkipIfEmpty()) {
			return true;
		}

		if (expectedClass.isAssignableFrom(Double.class)) {
			try {
				Double.parseDouble(value);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		} else if (expectedClass.isAssignableFrom(Date.class)) {
			return DateUtil.isValidDate(value);
		} else if (expectedClass.isAssignableFrom(Integer.class)) {
			try {
				Integer.parseInt(value);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
