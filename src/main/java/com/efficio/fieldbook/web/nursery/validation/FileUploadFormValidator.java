package com.efficio.fieldbook.web.nursery.validation;


import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.demo.form.Test3JavaForm;

public class FileUploadFormValidator implements Validator {

    public final static String FILE_NOT_FOUND_ERROR = "error.file.not.found";
    public final static String FILE_NOT_EXCEL_ERROR = "error.file.not.excel";
    public final static String FILE_NOT_SELECTED = "error.file.no.file.selected";

    @Override
    public boolean supports(Class<?> aClass) {
        return Test3JavaForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
    	Test3JavaForm form = (Test3JavaForm) o;

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