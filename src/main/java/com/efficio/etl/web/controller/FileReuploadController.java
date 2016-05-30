
package com.efficio.etl.web.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.AbstractBaseETLController;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.controller.angular.AngularMapOntologyController;
import com.efficio.etl.web.validators.FileUploadFormValidator;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@Controller
@RequestMapping(FileReuploadController.URL)
public class FileReuploadController extends AbstractBaseETLController {

	public static final String URL = "etl/workbook/reupload";

  	private static final Logger LOG = LoggerFactory.getLogger(FileReuploadController.class);

	@Resource
	private ETLService etlService;

	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Override
	public String getContentName() {
		return "etl/reupload";
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("uploadForm") FileUploadForm uploadForm, Model model, HttpSession session) {
		return super.show(model);
	}

	@RequestMapping(method = RequestMethod.POST)
	public String uploadFile(@ModelAttribute("uploadForm") FileUploadForm uploadForm, BindingResult result, Model model) {
		FileUploadFormValidator validator = new FileUploadFormValidator();
		validator.validate(uploadForm, result);

		if (result.hasErrors()) {
			/**
			 * Return the user back to form to show errors
			 */
			return this.getContentName();
		} else {

			try {
				String tempFileName = this.etlService.storeUserWorkbook(uploadForm.getFile().getInputStream());
				this.userSelection.setServerFileName(tempFileName);
				this.userSelection.setActualFileName(uploadForm.getFile().getOriginalFilename());
			} catch (IOException e) {
			  	FileReuploadController.LOG.error(e.getMessage(), e);
				result.reject("uploadForm.file", "Error occurred while uploading file.");
				return this.getContentName();
			}

			return "redirect:" + AngularMapOntologyController.URL;

		}
	}
}
