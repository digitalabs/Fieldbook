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

import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ManageNurseriesForm;
import com.efficio.fieldbook.web.nursery.form.NurseryDetailsForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;

/**
 * The Class ManageNurseriesController.
 */
@Controller
@RequestMapping({"/NurseryManager", ManageNurseriesController.URL})
public class ManageNurseriesController extends AbstractBaseFieldbookController{
    
    private static final Logger LOG = LoggerFactory.getLogger(ManageNurseriesController.class);
    
    /** The Constant URL. */
    public static final String URL = "/NurseryManager/manageNurseries";
    public static final String PAGINATION_TEMPLATE = "/NurseryManager/showNurseriesPagination";

    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    
    /**
     * Shows the manage nurseries screen
     *
     * @param manageNurseriesForm the manage nurseries form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("manageNurseriesForm") ManageNurseriesForm form, Model model) {
        try {
            List<StudyDetails> nurseryDetailsList = fieldbookMiddlewareService
                    .getAllLocalNurseryDetails();
            getUserSelection().setStudyDetailsList(nurseryDetailsList);
            form.setNurseryDetailsList(getUserSelection().getStudyDetailsList());
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
            , @ModelAttribute("manageNurseriesForm") ManageNurseriesForm form, Model model) {
        List<StudyDetails> nurseryDetailsList = getUserSelection().getStudyDetailsList();
        if(nurseryDetailsList != null){
            form.setNurseryDetailsList(nurseryDetailsList);
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
    public String submitDetails(@ModelAttribute("manageNurseriesForm") NurseryDetailsForm form
            , BindingResult result, Model model) {
        return "redirect:" + FileUploadController.URL;
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/manageNurseries";
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
    public UserSelection getUserSelection() {
        return this.userSelection;
    }
}