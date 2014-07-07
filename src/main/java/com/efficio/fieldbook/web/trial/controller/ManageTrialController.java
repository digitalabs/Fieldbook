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
package com.efficio.fieldbook.web.trial.controller;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ManageNurseriesForm;
import com.efficio.fieldbook.web.trial.form.ManageTrialForm;

/**
 * The Class ManageTrialController.
 */
@Controller
@RequestMapping({"/TrialManager", ManageTrialController.URL})
public class ManageTrialController extends AbstractBaseFieldbookController{

    
    private static final Logger LOG = LoggerFactory.getLogger(ManageTrialController.class);
    /** The Constant URL. */
    public static final String URL = "/TrialManager/manageTrial";
  
    /**
     * Shows the manage nurseries screen
     *
     * @param manageNurseriesForm the manage nurseries form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("manageTrialForm") ManageTrialForm form, Model model) {
    	return super.show(model);
    }
    
   
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "TrialManager/manageTrial";
    }
   
}