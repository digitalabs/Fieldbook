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
package com.efficio.fieldbook.web.ontology.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.efficio.fieldbook.web.ontology.form.OntologyBrowserForm;

/**
 * The Class ImportGermplasmListValidator.
 */
public class OntologyBrowserValidator implements Validator  {
    

    public final static String MANDATORY_FIELD_NOT_POPULATED = "ontology.browser.modal.error";
    
    public final static String CANNOT_UPDATE_CENTRAL_VARIABLE = "ontology.browser.cannot.update.central.variable"; 

    /* (non-Javadoc)
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> aClass) {
        return OntologyBrowserForm.class.isAssignableFrom(aClass);
    }

    /* (non-Javadoc)
     * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
    public void validate(Object o, Errors errors) {
    	OntologyBrowserForm form = (OntologyBrowserForm) o;

        //on edit mode, role is disabled and not passed to the form
    	boolean isAddMode = form.getVariableId() == null;
        if (form.getVariableName() == null || form.getVariableName().equals("")) {
            errors.rejectValue("variableName", MANDATORY_FIELD_NOT_POPULATED);
        } else if (form.getDataType() == null || form.getDataType().equals("")) {
            errors.rejectValue("dataType", MANDATORY_FIELD_NOT_POPULATED);
        } else if (isAddMode && (form.getRole() == null || form.getRole().equals(""))) {
            errors.rejectValue("role", MANDATORY_FIELD_NOT_POPULATED);
        } else if (form.getTraitClass() == null || form.getTraitClass().equals("")) {
            errors.rejectValue("traitClass", MANDATORY_FIELD_NOT_POPULATED);
        } else if (form.getProperty() == null || form.getProperty().equals("")) {
            errors.rejectValue("property", MANDATORY_FIELD_NOT_POPULATED);
        } else if (form.getMethod() == null || form.getMethod().equals("")) {
            errors.rejectValue("method", MANDATORY_FIELD_NOT_POPULATED);
        } else if (form.getScale() == null || form.getScale().equals("")) {
            errors.rejectValue("scale", MANDATORY_FIELD_NOT_POPULATED);
        } else if (!isAddMode && form.getVariableId() > -1) {
            errors.rejectValue("variableName", CANNOT_UPDATE_CENTRAL_VARIABLE);
        }
        
    }
}
