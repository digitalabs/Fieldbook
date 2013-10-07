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
package com.efficio.fieldbook.web.nursery.controller;

import java.io.IOException;

import org.generationcp.middleware.service.api.DataImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.validation.FileUploadFormValidator;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.nursery.form.FileUploadForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;


@Controller
@RequestMapping({"/NurseryManager", FileUploadController.URL})
public class FileUploadController extends AbstractBaseFieldbookController{

    public static final String URL = "/NurseryManager/fileUpload";
    
    @Resource
    private FieldbookService fieldbookService;
	
    @Resource
    private UserSelection userSelection;	
	
	@Resource
    private DataImportService dataImportService;
	
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("fileUploadForm") FileUploadForm uploadForm, Model model, HttpSession session) {
    	session.invalidate();
    	return super.show(model);
    }

    @RequestMapping(method = RequestMethod.POST)
    public String uploadFile(@ModelAttribute("fileUploadForm") FileUploadForm uploadForm, BindingResult result, Model model, HttpSession session) {
    	FileUploadFormValidator validator = new FileUploadFormValidator();
        validator.validate(uploadForm, result);

        if (result.hasErrors()) {
            /**
             * Return the user back to form to show errors
             */
        	return show(uploadForm,model,session);
        } else {


            try {
            	String tempFileName = fieldbookService.storeUserWorkbook(uploadForm.getFile().getInputStream());
            	userSelection.setServerFileName(tempFileName);
                userSelection.setActualFileName(uploadForm.getFile().getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
                result.reject("uploadForm.file", "Error occurred while uploading file.");
            }
            
            return "redirect:" + NurseryDetailsController.URL;
        }
    }
    
    @Override
    public String getContentName() {
        return "NurseryManager/fileUpload";
    }
    
    @ModelAttribute("form")
    public FileUploadForm getForm() {
        return new FileUploadForm();
    }
    
    public void setEtlService(FieldbookService fieldbookService) {
        this.fieldbookService = fieldbookService;
    }

    public void setUserSelection(UserSelection userSelection) {
        this.userSelection = userSelection;
    }

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}
    
}