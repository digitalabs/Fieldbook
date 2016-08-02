
package com.efficio.etl.web.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.etl.web.bean.FileUploadForm;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class FileUploadFormValidator implements Validator {

	public final static String FILE_NOT_FOUND_ERROR = "error.file.not.found";
	public final static String FILE_NOT_EXCEL_ERROR = "error.file.not.excel";

	@Override
	public boolean supports(Class<?> aClass) {
		return FileUploadForm.class.isAssignableFrom(aClass);
	}

	@Override
	public void validate(Object o, Errors errors) {
		FileUploadForm form = (FileUploadForm) o;

		MultipartFile file = form.getFile();

		if (file == null) {
			errors.rejectValue("file", FileUploadFormValidator.FILE_NOT_FOUND_ERROR);
		} else {
			boolean isExcelFile = file.getOriginalFilename().contains(".xls") || file.getOriginalFilename().contains(".xlsx");
			if (!isExcelFile) {

				errors.rejectValue("file", FileUploadFormValidator.FILE_NOT_EXCEL_ERROR);
			}
		}
	}
}
