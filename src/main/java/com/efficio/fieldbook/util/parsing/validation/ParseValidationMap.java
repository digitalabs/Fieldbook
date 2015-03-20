package com.efficio.fieldbook.util.parsing.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 3/2/2015
 * Time: 10:10 AM
 *
 * Container class for parsing validators
 */
public class ParseValidationMap {
	private Map<Integer, List<ParsingValidator>> validationMap;

	public ParseValidationMap() {
		validationMap = new HashMap<>();
	}

	public void addValidation(Integer key, ParsingValidator validator) {
		List<ParsingValidator> validatorList = validationMap.get(key);

		if (validatorList == null) {
			validatorList = new ArrayList<>();
			validationMap.put(key, validatorList);
		}

		validatorList.add(validator);
	}

	public List<ParsingValidator> getValidations(Integer key) {
		return validationMap.get(key);
	}


}
