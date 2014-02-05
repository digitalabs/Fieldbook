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
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.util.Debug;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.SummaryDetailsForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

/**
 * The Class SummaryDetailsController.
 */
@Controller
@RequestMapping(SummaryDetailsController.URL)
public class SummaryDetailsController extends AbstractBaseFieldbookController{
    
    /** The Constant URL. */
    public static final String URL = "/NurseryManager/summary";
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/summary";
    }
    
    /**
     * Sets the user selection.
     *
     * @param userSelection the new user selection
     */
    public void setUserSelection(UserSelection userSelection) {
        this.userSelection = userSelection;
    }

    /**
     * Gets the user selection.
     *
     * @return the user selection
     */
    public UserSelection getUserSelection() {
        return this.userSelection;
    }
    
    @ModelAttribute("form")
    public SummaryDetailsForm getForm() {
        return new SummaryDetailsForm();
    }


    /**
     * Shows the screen.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("summaryDetailsForm") SummaryDetailsForm form
            , BindingResult result, Model model, HttpSession session) {
        
        // Set form values based on user selection
        Workbook workbook = userSelection.getWorkbook();
        
        //nursery book name, title, objective
        StudyDetails studyDetails = workbook.getStudyDetails();
        form.setBookName(studyDetails.getStudyName());
        form.setTitle(studyDetails.getTitle());
        form.setObjective(studyDetails.getObjective());

        //nursery sequence, principal investigator, location, breeding method
        List<MeasurementVariable> conditions = workbook.getConditions();
        Debug.println(0, conditions.toString());
        for (MeasurementVariable condition : conditions){
            if ("NID".equalsIgnoreCase(condition.getName())){
                form.setSequenceNumber(condition.getValue());                
            } else if ("PI Name".equalsIgnoreCase(condition.getName())){
                    form.setPrincipalInvestigator(condition.getValue());                
            } else if ("Site".equalsIgnoreCase(condition.getName())){
                form.setLocation(condition.getValue());
            } else if ("Breeding Method".equalsIgnoreCase(condition.getName())){
                form.setBreedingMethod(condition.getValue());
            }
        }

        // traits
        List<MeasurementVariable> traits = new ArrayList<MeasurementVariable>();
        traits.addAll(workbook.getConstants());
        traits.addAll(workbook.getVariates());
        form.setTraits(traits);

        session.invalidate();
    	return super.show(model);
    }

    /**
     * Goes to the next screen.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String nextScreen(@ModelAttribute("summaryDetailsForm") SummaryDetailsForm form
            , BindingResult result, Model model) {
        return "redirect:" + ManageNurseriesController.URL;
    }

}