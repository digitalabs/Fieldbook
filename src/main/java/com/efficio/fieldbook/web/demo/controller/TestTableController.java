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
import com.efficio.fieldbook.web.demo.bean.TestJavaBean;
import com.efficio.fieldbook.web.demo.form.Test2JavaForm;
import com.efficio.fieldbook.web.demo.bean.UserSelection;
import com.efficio.fieldbook.web.demo.validation.TestValidator;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.util.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * The Class TestTableController.
 */
@Controller
@RequestMapping({"/testTable"})
public class TestTableController extends AbstractBaseFieldbookController{
	
    private static final Logger LOG = LoggerFactory.getLogger(TestTableController.class);
    
    /** The germplasm data manager. */
    @Resource
    private GermplasmDataManager germplasmDataManager;
    
    /** The test java bean. */
    @Resource
    private TestJavaBean testJavaBean;

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

        try {
            LOG.debug(testJavaBean.getName());
            testForm.setLocationList(germplasmDataManager
                    .getAllBreedingLocations());
            testForm.setMethodList(germplasmDataManager.getAllMethods());
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
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
    @RequestMapping(value = "doSubmit", method = RequestMethod.POST)
    public String submit(@ModelAttribute("test2JavaForm") Test2JavaForm testForm, BindingResult result, Model model) {
        TestValidator validator = new TestValidator();
        validator.validate(testForm, result);

        if (result.hasErrors()) {
            //Return the user back to form to show errors
            return show(testForm, model);
        } else {
            // at this point, we can assume that program has reached an error condition. we return user to the form
            return show(testForm, model);
        }
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "demo/testTable";
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