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

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

@Controller
@RequestMapping(AddOrRemoveTraitsController.URL)
public class AddOrRemoveTraitsController extends AbstractBaseFieldbookController{

    public static final String URL = "/NurseryManager/addOrRemoveTraits";
    
    @Resource
    private UserSelection userSelection;

    @Override
    public String getContentName() {
        return "NurseryManager/addOrRemoveTraits";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, Model model, HttpSession session) {
    	session.invalidate();
    	return super.show(model);
    }

    @RequestMapping(method = RequestMethod.POST)
    public String showDetails(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, BindingResult result, Model model) {
    	//TODO
        return "redirect:" + SaveNurseryController.URL;
    }
    
    @Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

}