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
import org.generationcp.middleware.domain.etl.Workbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.NurseryDetailsForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

/**
 * The Class NurseryDetailsController.
 */
@Controller
@RequestMapping(NurseryDetailsController.URL)
public class NurseryDetailsController extends AbstractBaseFieldbookController{
    
    /** The Constant URL. */
    public static final String URL = "/NurseryManager/nurseryDetails";
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/nurseryDetails";
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
    public String show(@ModelAttribute("nurseryDetailsForm") NurseryDetailsForm form
            , BindingResult result, Model model, HttpSession session) {
        if (userSelection.getWorkbook() == null){
            result.reject("form.workbook", "Error occurred while parsing file.");
            userSelection.setWorkbook(new Workbook());
        }
        
        // Get the values of conditions from Workbook
        List<MeasurementVariable> conditions = userSelection.getWorkbook().getConditions();
        List<String> values = new ArrayList<String>();
        for (MeasurementVariable condition : conditions){
            values.add(condition.getValue());
        }
        form.setValues(values);
        
        form.setWorkbook(userSelection.getWorkbook());
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
    public String submitDetails(@ModelAttribute("nurseryDetailsForm") NurseryDetailsForm form
            , BindingResult result, Model model) {
        userSelection.setFieldLayoutRandom(form.getFieldLayoutRandom());
        
        // Set the values of conditions
        List<String> values = form.getValues();
        Workbook workbook = userSelection.getWorkbook();
        List<MeasurementVariable> conditions = workbook.getConditions();
        for (MeasurementVariable condition : conditions){
            condition.setValue(values.get(conditions.indexOf(condition)));
        }
        workbook.setConditions(conditions);
        userSelection.setWorkbook(workbook);

        return "redirect:" + ImportGermplasmListController.URL;
    }

}