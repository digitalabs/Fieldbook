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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.nursery.controller.ManageNurseriesController;
import com.efficio.pojos.svg.Element;


// TODO: Auto-generated Javadoc
/**
 * The Class PlantingDetailsController.
 */
@Controller
@RequestMapping({PlantingDetailsController.URL})
public class PlantingDetailsController extends AbstractBaseFieldbookController{
 
 /** The Constant LOG. */
 private static final Logger LOG = LoggerFactory.getLogger(ManageNurseriesController.class);
    
    /** The Constant URL. */
    public static final String URL = "/Fieldmap/plantingDetails";

    
    /** The user selection. */
    @Resource
    private UserFieldmap userFieldmap;
    
    @Resource
    private FieldMapService fieldmapService;
    
   
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
        
        List<Element> fieldmapShapes = fieldmapService.createBlankFieldmap(userFieldmap, 5, 5);
        
        
        form.setFieldmapShapes(fieldmapShapes);
        
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
    public String submitDetails(@ModelAttribute("FieldmapForm") FieldmapForm form, Model model) {
        this.userFieldmap.setStartingColumn(form.getUserFieldmap().getStartingColumn());
        this.userFieldmap.setStartingRange(form.getUserFieldmap().getStartingRange());
        this.userFieldmap.setPlantingOrder(form.getUserFieldmap().getPlantingOrder());
        
        int startRange = form.getUserFieldmap().getStartingRange();
        int startCol = form.getUserFieldmap().getStartingRow();
        int rows = userFieldmap.getNumberOfRowsInBlock();
        int ranges = userFieldmap.getNumberOfRangesInBlock();
        int rowsPerPlot = userFieldmap.getNumberOfRowsPerPlot();
        boolean isSerpentine = userFieldmap.getPlantingOrder() == 1;
        
        int col = rows / rowsPerPlot;
        Map deletedPlot = new HashMap();
        //should list here the deleted plot in col-range format
        List entryList = new ArrayList();
        //add here the entry list
        Plot[][] plot = fieldmapService.createFieldMap(col, ranges, startRange, startCol,
                isSerpentine, deletedPlot, entryList);
        
        return "redirect:" + GenerateFieldmapController.URL;
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
