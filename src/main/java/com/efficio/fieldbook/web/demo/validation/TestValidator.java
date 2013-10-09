/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.demo.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.efficio.fieldbook.web.demo.form.Test2JavaForm;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public class TestValidator implements Validator {

    /** The Constant FILE_NOT_FOUND_ERROR. */
    public final static String FILE_NOT_FOUND_ERROR = "error.file.not.found";
    
    /** The Constant FILE_NOT_EXCEL_ERROR. */
    public final static String FILE_NOT_EXCEL_ERROR = "error.file.not.excel";

    /* (non-Javadoc)
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> aClass) {
        return Test2JavaForm.class.isAssignableFrom(aClass);
    }

    /* (non-Javadoc)
     * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
     */
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