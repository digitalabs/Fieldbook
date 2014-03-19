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

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;

/**
 * The Class PlantingDetailsController.
 */
@Controller
@RequestMapping({PlantingDetailsController.URL})
public class PlantingDetailsController extends AbstractBaseFieldbookController{
 
    /** The Constant URL. */
    public static final String URL = "/Fieldmap/plantingDetails";

    /** The user selection. */
    @Resource
    private UserFieldmap userFieldmap;
    
    @Resource
    private FieldMapService fieldmapService;
    
    @Resource 
    private FieldbookService fieldbookMiddlewareService;
   
    /**
     * Show.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model, HttpSession session) {
    	try {
	        setPrevValues(form);
	        System.out.println("DATASET ID IS " + userFieldmap.getSelectedDatasetId());
	        System.out.println("GEOLOCATION ID IS " + userFieldmap.getSelectedGeolocationId());
	        this.userFieldmap.setSelectedFieldMaps(fieldbookMiddlewareService.getAllFieldMapsInBlockByTrialInstanceId(
	        		userFieldmap.getSelectedDatasetId(), userFieldmap.getSelectedGeolocationId()));
            this.userFieldmap.setSelectedFieldmapList(new SelectedFieldmapList(
                    this.userFieldmap.getSelectedFieldMaps(), this.userFieldmap.isTrial()));
	        form.setUserFieldmap(this.userFieldmap);
	        
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return super.show(model);
    }
    
    private void setPrevValues(FieldmapForm form) {
        UserFieldmap info = new UserFieldmap();
        info.setNumberOfRangesInBlock(userFieldmap.getNumberOfRangesInBlock());
        info.setNumberOfRowsInBlock(userFieldmap.getNumberOfRowsInBlock());
        info.setSelectedDatasetId(userFieldmap.getSelectedDatasetId());
        info.setSelectedGeolocationId(userFieldmap.getSelectedGeolocationId());
        form.setUserFieldmap(info);
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "Fieldmap/enterPlantingDetails";
    }
    
    /**
     * Gets the user fieldmap.
     *
     * @return the user fieldmap
     */
    public UserFieldmap getUserFieldmap() {
        return this.userFieldmap;
    }
       
}
