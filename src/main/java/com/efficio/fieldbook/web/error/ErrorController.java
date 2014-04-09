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
package com.efficio.fieldbook.web.error;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

/**
 * The Class LabelPrintingController.
 * 
 * This class would handle the label printing for the pdf and excel generation.
 */
@Controller
@RequestMapping({ErrorController.URL})
public class ErrorController extends AbstractBaseFieldbookController{
 
     /** The Constant LOG. */
     private static final Logger LOG = LoggerFactory.getLogger(ErrorController.class);
    
    /** The Constant URL. */
    public static final String URL = "/error";
    
   
   
    /**
     * Show trial label details.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @param id the id
     * @param locale the locale
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String showGeneralError(
            Model model, HttpSession session, Locale locale) {
    	    	
        return super.showError(model);
    }



	@Override
	public String getContentName() {
		// TODO Auto-generated method stub
		return null;
	}
    
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    /*
    @Override
    public String getContentName() {
        return "error/error";
    }
    */
    
}
