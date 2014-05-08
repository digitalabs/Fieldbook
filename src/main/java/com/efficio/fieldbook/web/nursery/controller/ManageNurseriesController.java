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

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ManageNurseriesForm;
import com.efficio.fieldbook.web.nursery.form.NurseryDetailsForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.pojos.treeview.TreeNode;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.pojos.GermplasmList;
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
          
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        //return "NurseryManager/manageNurseries";
    	return "NurseryManager/ver2.0/manageNurseries";
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