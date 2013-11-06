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
package com.efficio.fieldbook.web.fieldmap.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

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
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.controller.FileUploadController;
import com.efficio.fieldbook.web.nursery.controller.ManageNurseriesController;
import com.efficio.fieldbook.web.nursery.form.ManageNurseriesForm;
import com.efficio.fieldbook.web.nursery.form.NurseryDetailsForm;


@Controller
@RequestMapping({PlantingDetailsController.URL})
public class PlantingDetailsController extends AbstractBaseFieldbookController{
 private static final Logger LOG = LoggerFactory.getLogger(ManageNurseriesController.class);
    
    /** The Constant URL. */
    public static final String URL = "/Fieldmap/plantingDetails";

    
    /** The user selection. */
    @Resource
    private UserFieldmap userFieldmap;
    
   
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("FieldmapForm") FieldmapForm form, Model model, HttpSession session) {
        session.invalidate();
        /*
        try {
            
            List<StudyDetails> nurseryDetailsList = fieldbookMiddlewareService.getAllLocalNurseryDetails();
            getUserSelection().setStudyDetailsList(nurseryDetailsList);
            form.setNurseryDetailsList(getUserSelection().getStudyDetailsList());
            form.setCurrentPage(1);
            
        }
        catch (MiddlewareQueryException e){
            LOG.error(e.getMessage(), e);
        }
        */
        return super.show(model);
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
    public String submitDetails(@ModelAttribute("FieldmapForm") FieldmapForm form, BindingResult result, Model model) {
        return "redirect:" + FileUploadController.URL;
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "Fieldmap/enterPlantingDetails";
    }
    
    
    public UserFieldmap getUserFieldmap() {
        return this.userFieldmap;
    }
    
    
}
