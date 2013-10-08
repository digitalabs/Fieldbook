package com.efficio.fieldbook.web.nursery.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.efficio.fieldbook.web.nursery.form.SaveNurseryForm;

@Component
public class SaveNurseryValidator implements Validator {
	
	//TODO: autowire studyDataManager and add study name check
	//scenario: what if re-used a name of a study that was deleted, logically sounds ok,
	//but will not be allowed by the db
	//@Resource
	//private StudyDataManager studyDataManager;

	@Override
	public boolean supports(Class<?> clazz) {
		return SaveNurseryForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		SaveNurseryForm form = (SaveNurseryForm) target;
		
		if (StringUtils.isBlank(form.getTitle())) {
			errors.rejectValue("title", "error.mandatory.field");
		}
		if (StringUtils.isBlank(form.getObjective())) {
			errors.rejectValue("objective", "error.mandatory.field");
		}
		if (StringUtils.isBlank(form.getNurseryBookName())) {
			errors.rejectValue("nurseryBookName", "error.mandatory.field");
		}
	}

	
}
