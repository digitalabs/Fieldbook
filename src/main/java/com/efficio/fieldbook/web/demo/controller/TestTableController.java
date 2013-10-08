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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping({"/testTable"})
public class TestTableController extends AbstractBaseFieldbookController{
	
    @Resource
    private GermplasmDataManager germplasmDataManager;
    
    @Resource
    private TestJavaBean testJavaBean;

    @Resource
    private UserSelection userSelection;
	
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("test2JavaForm") Test2JavaForm testForm,  Model model) {

        try {
            System.out.println(testJavaBean.getName());
            testForm.setLocationList(germplasmDataManager
                    .getAllBreedingLocations());
            testForm.setMethodList(germplasmDataManager.getAllMethods());

        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        }
    	return super.show(model);
    }

    @RequestMapping(value = "doSubmit", method = RequestMethod.POST)
    public String submit(@ModelAttribute("test2JavaForm") Test2JavaForm testForm, BindingResult result, Model model) {
        TestValidator validator = new TestValidator();
        validator.validate(testForm, result);

        if (result.hasErrors()) {
            /**
             * Return the user back to form to show errors
             */
            return show(testForm, model);
        } else {

            // at this point, we can assume that program has reached an error
            // condition. we return user to the form

            return show(testForm, model);
        }
    }

    @Override
    public String getContentName() {
        return "demo/testTable";
    }
   
    public UserSelection getUserSelection() {
        return this.userSelection;
    }
}