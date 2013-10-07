package com.efficio.fieldbook.web.nursery.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;


public class ImportGermplasmListValidator implements Validator  {
	public final static String FILE_NOT_FOUND_ERROR = "error.file.not.found";
    public final static String FILE_NOT_EXCEL_ERROR = "error.file.not.excel";
    public final static String FILE_NOT_SELECTED = "error.file.no.file.selected";

    @Override
    public boolean supports(Class<?> aClass) {
        return ImportGermplasmListForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
    	ImportGermplasmListForm form = (ImportGermplasmListForm) o;

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
