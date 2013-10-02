package com.efficio.fieldbook.web.validation.demo;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.efficio.fieldbook.web.form.demo.Test2JavaForm;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public class TestValidator implements Validator {

    public final static String FILE_NOT_FOUND_ERROR = "error.file.not.found";
    public final static String FILE_NOT_EXCEL_ERROR = "error.file.not.excel";

    @Override
    public boolean supports(Class<?> aClass) {
        return Test2JavaForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
    	Test2JavaForm form = (Test2JavaForm) o;

        String name = form.getName();
        Integer locationId = form.getLocationId();
        Integer methodIds[] = form.getMethodIds();
       
        //name field, error code, default value
        if (name == null || name.equalsIgnoreCase("")) {
            errors.rejectValue("name", "name.required","Name is required");
        }
        
        if (locationId == null || locationId.intValue() == 0) {
            errors.rejectValue("locationId", "location.required", "Location is required");
        }
        
        if (methodIds == null || methodIds.length == 0) {
            errors.rejectValue("methodIds", "method.required", "Methods is required");
        }
        
    }
}