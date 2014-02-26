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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.form.LabelPrintingForm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;

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
