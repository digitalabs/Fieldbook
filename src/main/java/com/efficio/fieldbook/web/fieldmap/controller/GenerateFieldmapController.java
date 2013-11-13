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

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.pojos.svg.Element;

@Controller
@RequestMapping({GenerateFieldmapController.URL})
public class GenerateFieldmapController extends AbstractBaseFieldbookController{

    public static final String URL = "/Fieldmap/generateFieldmapView";

    @Resource
    private UserFieldmap userFieldmap;
    
    @Resource
    private FieldMapService fieldmapService;
    

    @RequestMapping(method = RequestMethod.GET)
    public String showGeneratedFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {
        
        //TODO: FOR testing only, remove this 
        populateFormWithDummyData(form);
        
        List<String> fieldmapLabels = fieldmapService.createFieldmap(form.getUserFieldmap());
        form.setFieldmapLabels(fieldmapLabels);
        
        List<Element> fieldmapShapes = fieldmapService.createBlankFieldmap(form.getUserFieldmap(), 5, 5);
        form.setFieldmapShapes(fieldmapShapes);
        
        return super.show(model);
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String generateFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {
        System.out.println("GENERATING FIELD MAP...." + form.getMarkedCells());
        //TODO: FOR testing only, remove this 
        populateFormWithDummyData(form);
        
        List<String> fieldmapLabels = fieldmapService.createFieldmap(form.getUserFieldmap());
        form.setFieldmapLabels(fieldmapLabels);
        
        if (form.getMarkedCells() != null && !form.getMarkedCells().isEmpty()) {
            List<String> markedCells = Arrays.asList(form.getMarkedCells().replace("cell", "").split(","));
            
            List<Element> fieldmapShapes = fieldmapService.createFieldmap(form.getUserFieldmap(), markedCells, 5, 5);
            form.setFieldmapShapes(fieldmapShapes);
        }
        return super.show(model);
    }

    
    public UserFieldmap getUserFieldmap() {
        return userFieldmap;
    }
    
    public void setUserFieldmap(UserFieldmap userFieldmap) {
        this.userFieldmap = userFieldmap;
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "Fieldmap/generateFieldmapView";
    }

    private void populateFormWithDummyData(FieldmapForm form) {
        UserFieldmap info = new UserFieldmap();
        info.setNumberOfRowsInBlock(10);
        info.setNumberOfRangesInBlock(10);
        info.setNumberOfEntries("10");
        info.setNumberOfReps("7");
        info.setNumberOfRowsPerPlot(2);
        info.setSelectedName("Fieldbook 1");
        info.setPlantingOrder(1);
        form.setUserFieldmap(info);
    }
    
}
