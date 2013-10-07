package com.efficio.fieldbook.web.demo.controller;

import java.io.IOException;

import javax.annotation.Resource;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.bean.UserSelection;
import com.efficio.fieldbook.web.demo.validation.FileUploadFormValidator;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.form.Test3JavaForm;

import org.generationcp.middleware.service.api.DataImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping({"/test3"})
public class Test3Controller extends AbstractBaseFieldbookController{
	
	@Resource
    private DataImportService dataImportService;
	
	@Resource
    private UserSelection userSelection;
	
	@Resource
    private FieldbookService fieldbookService;
	
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("test3Form") Test3JavaForm testForm,  Model model) {
    		return super.show(model);
    }

    @RequestMapping(method = RequestMethod.POST)
    public String uploadFile(@ModelAttribute("test3Form") Test3JavaForm uploadForm, BindingResult result, Model model) {
        FileUploadFormValidator validator = new FileUploadFormValidator();
        validator.validate(uploadForm, result);

        if (result.hasErrors()) {
            /**
             * Return the user back to form to show errors
             */
        	return show(uploadForm,model);
        } else {


            try {
            	String tempFileName = fieldbookService.storeUserWorkbook(uploadForm.getFile().getInputStream());
            	uploadForm.setFileName(tempFileName);
            } catch (IOException e) {
                e.printStackTrace();
                result.reject("uploadForm.file", "Error occurred while uploading file.");
            }
            
            return show(uploadForm,model);
        }
    }
    
    @Override
    public String getContentName() {
        return "demo/test3";
    }
    
    @Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}
}