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

import javax.annotation.Resource;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.form.Test2JavaForm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The Class TestWizardController.
 */
@Controller
@RequestMapping({"/testWizard"})
public class TestWizardController extends AbstractBaseFieldbookController{
	
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
	
    /**
     * Shows the screen.
     *
     * @param testForm the test form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("test2JavaForm") Test2JavaForm testForm,  Model model) {
    	return super.show(model);
    }
    
    /**
     * Submit.
     *
     * @param testForm the test form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="doSubmit", method = RequestMethod.POST)
    public String submit(@ModelAttribute("test2JavaForm") Test2JavaForm testForm, 
            BindingResult result, Model model) {
        return show(testForm,model);
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "demo/testWizard";
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