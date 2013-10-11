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
package com.efficio.fieldbook.web.nursery.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.nursery.form.FileUploadForm;

/**
 * The Class FileUploadFormValidator.
 */
public class FileUploadFormValidator implements Validator {

    /** The Constant FILE_NOT_FOUND_ERROR. */
    public final static String FILE_NOT_FOUND_ERROR = "common.error.file.not.found";
    
    /** The Constant FILE_NOT_EXCEL_ERROR. */
    public final static String FILE_NOT_EXCEL_ERROR = "common.error.file.not.excel";
    
    /** The Constant FILE_NOT_SELECTED. */
    public final static String FILE_NOT_SELECTED = "common.error.file.no.file.selected";

    /* (non-Javadoc)
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> aClass) {
        return FileUploadForm.class.isAssignableFrom(aClass);
    }

    /* (non-Javadoc)
     * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
    public void validate(Object o, Errors errors) {
    	FileUploadForm form = (FileUploadForm) o;

        MultipartFile file = form.getFile();
        if (file == null) {
            errors.rejectValue("file", FILE_NOT_FOUND_ERROR);
        } else {
            boolean isExcelFile = file.getOriginalFilename().contains(".xls") || file.getOriginalFilename().contains(".xlsx");
            if (!isExcelFile) {
                errors.rejectValue("file", FILE_NOT_EXCEL_ERROR);
            }
        }
    }
}