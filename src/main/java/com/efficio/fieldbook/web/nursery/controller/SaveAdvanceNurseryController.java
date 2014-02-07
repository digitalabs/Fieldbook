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

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

@Controller
@RequestMapping(SaveAdvanceNurseryController.URL)
public class SaveAdvanceNurseryController extends AbstractBaseFieldbookController{

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/saveAdvanceNursery";
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SaveAdvanceNurseryController.class);
    
    /** The user selection. */
    @Resource
    private AdvancingNursery advancingNursery;
    @Resource
    private FieldbookService fieldbookService;
 
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/saveAdvanceNursery";
    }    
    
    /**
     * Shows the screen
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("advancingNurseryform") AdvancingNurseryForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{
    	form.setGermplasmList(fieldbookService.advanceNursery(advancingNursery));
    	return super.show(model);
    }
       
    @RequestMapping(method = RequestMethod.POST)
    public String postAdvanceNursery(@ModelAttribute("advancingNurseryform") AdvancingNurseryForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{
    	
    	
    	return super.show(model);
    }
    
}