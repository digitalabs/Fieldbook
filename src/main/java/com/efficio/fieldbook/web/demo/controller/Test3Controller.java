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
package com.efficio.fieldbook.web.demo.controller;

import java.io.IOException;

import javax.annotation.Resource;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.demo.validation.TestFileUploadFormValidator;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.form.Test3JavaForm;
import com.efficio.fieldbook.web.demo.bean.UserSelection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The Class Test3Controller.
 */
@Controller
@RequestMapping({"/test3"})
public class Test3Controller extends AbstractBaseFieldbookController{
	
    /** The user selection. */
    @Resource
    private UserSelection userSelection;

    /** The fieldbook service. */
    @Resource
    private FieldbookService fieldbookService;
	
    /**
     * Show.
     *
     * @param testForm the test form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("test3Form") Test3JavaForm testForm,  Model model) {
    		return super.show(model);
    }

    /**
     * Upload file.
     *
     * @param uploadForm the upload form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String uploadFile(@ModelAttribute("test3Form") Test3JavaForm uploadForm, BindingResult result, Model model) {
        TestFileUploadFormValidator validator = new TestFileUploadFormValidator();
        validator.validate(uploadForm, result);

        if (result.hasErrors()) {
            /**
             * Return the user back to form to show errors
             */
            return show(uploadForm, model);
        } else {

            try {
                String tempFileName = fieldbookService.storeUserWorkbook(uploadForm.getFile().getInputStream());
                uploadForm.setFileName(tempFileName);
            } catch (IOException e) {
                e.printStackTrace();
                result.reject("uploadForm.file", "Error occurred while uploading file.");
            }

            return show(uploadForm, model);
        }
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "demo/test3";
    }
    
    /**
     * Gets the user selection.
     *
     * @return the user selection
     */
    public UserSelection getUserSelection() {
        return this.userSelection;
    }
}