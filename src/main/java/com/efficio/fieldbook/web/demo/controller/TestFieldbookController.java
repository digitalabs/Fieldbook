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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.bean.TestJavaBean;
import com.efficio.fieldbook.web.demo.bean.UserSelection;
import com.efficio.fieldbook.web.demo.form.TestJavaForm;

import javax.annotation.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */

@Controller
@RequestMapping({"/fieldbook"})
public class TestFieldbookController extends AbstractBaseFieldbookController{
	
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
	
    /** The bean. */
    @Resource
    private TestJavaBean bean;
	
    /**
     * Show.
     *
     * @param testForm the test form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("testForm") TestJavaForm testForm, Model model) {
    	return super.show(model);
    }

    /**
     * Upload file.
     *
     * @param testForm the test form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String uploadFile(@ModelAttribute("testForm") TestJavaForm testForm, 
            BindingResult result, Model model) {
        //FileUploadFormValidator validator = new FileUploadFormValidator();
        //validator.validate(uploadForm, result);

    	//for adding of error
    	result.reject("testForm.username", "test error msg");
    	bean.setAge("10");
    	bean.setName("Hello"+System.currentTimeMillis());
    	
        if (result.hasErrors()) {
            // Return the user back to form to show errors
            return show(testForm,model);
        } else {
            // at this point, we can assume that program has reached an error condition. 
            // we return user to the form
            return show(testForm,model);
        }
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "demo/testPage";
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