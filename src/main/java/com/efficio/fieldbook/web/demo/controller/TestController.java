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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.form.TestJavaForm;
import com.efficio.fieldbook.web.demo.bean.UserSelection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */

@Controller
@RequestMapping({"/test"})
public class TestController extends AbstractBaseFieldbookController{
	
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
	
    /**
     * Shows the screen.
     *
     * @param testForm the test form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("testForm") TestJavaForm testForm,  BindingResult result, Model model) {
    	model.addAttribute("testList", getDummyList());
    	return super.show(model);
    }

    /**
     * Gets the dummy list.
     *
     * @return the dummy list
     */
    private List getDummyList(){
    	List l = new ArrayList();
    	l.add("Hello 1");
    	l.add("Hello 2");
    	l.add("Hello 3");
    	return l;
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "demo/test";
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