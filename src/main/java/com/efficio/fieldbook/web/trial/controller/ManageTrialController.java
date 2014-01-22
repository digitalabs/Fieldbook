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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.nursery.form.ManageNurseriesForm;
import com.efficio.fieldbook.web.trial.bean.TrialSelection;
import com.efficio.fieldbook.web.trial.form.ManageTrialForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;

/**
 * The Class ManageTrialController.
 */
@Controller
@RequestMapping({"/TrialManager", ManageTrialController.URL})
public class ManageTrialController extends AbstractBaseFieldbookController{

    
    private static final Logger LOG = LoggerFactory.getLogger(ManageTrialController.class);
    
    /** The Constant URL. */
    public static final String URL = "/TrialManager/manageTrial";
    public static final String PAGINATION_TEMPLATE = "/TrialManager/showTrialPagination";

    @Resource
    private FieldbookService fieldbookMiddlewareService;
  
    @Resource
    private TrialSelection trialSelection;
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
        try {
            List<StudyDetails> nurseryDetailsList = 
                    fieldbookMiddlewareService.getAllLocalTrialStudyDetails();
            /*
            StudyDetails det1 = new StudyDetails(
                "studyName", "title", "pmKey", "objective", "", "", null, 1, "", "");                        
            for(int i = 0 ; i < 50  ; i++){
                det1.setId(4);
                nurseryDetailsList.add(det1);
            }
            */
            getTrialSelection().setStudyDetailsList(nurseryDetailsList);
            form.setTrialDetailsList(getTrialSelection().getStudyDetailsList());
            form.setCurrentPage(1);
        }
        catch (MiddlewareQueryException e){
            LOG.error(e.getMessage(), e);
        }
    	return super.show(model);
    }
    
    /**
     * Get for the pagination of the list
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/page/{pageNum}", method = RequestMethod.GET)
    public String getPaginatedList(@PathVariable int pageNum
            , @ModelAttribute("manageTrialForm") ManageTrialForm form, Model model) {
        List<StudyDetails> nurseryDetailsList = getTrialSelection().getStudyDetailsList();
        if(nurseryDetailsList != null){
            form.setTrialDetailsList(nurseryDetailsList);
            form.setCurrentPage(pageNum);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    
    /**
     * Submits the details.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String submitDetails(@ModelAttribute("manageTrialForm") ManageTrialForm form
            , BindingResult result, Model model) {
        //return "redirect:" + FileUploadController.URL;
        return super.show(model);
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "TrialManager/manageTrial";
    }
    
    /**
     * Gets the form.
     *
     * @return the form
     */
    @ModelAttribute("form")
    public ManageNurseriesForm getForm() {
        return new ManageNurseriesForm();
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getUserSelection()
     */
    
    public TrialSelection getTrialSelection() {
        return this.trialSelection;
    }
    
}