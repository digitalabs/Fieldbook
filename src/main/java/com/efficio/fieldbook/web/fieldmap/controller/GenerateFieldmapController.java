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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.service.api.FieldbookService;
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

@Controller
@RequestMapping({GenerateFieldmapController.URL})
public class GenerateFieldmapController extends AbstractBaseFieldbookController{

    public static final String URL = "/Fieldmap/generateFieldmapView";

    @Resource
    private UserFieldmap userFieldmap;
    
    @Resource
    private FieldMapService fieldmapService;
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;

    @RequestMapping(method = RequestMethod.GET)
    public String showGeneratedFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {
        
        //TODO: FOR testing only, remove this
        populateFormWithSessionData(form);
        
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
//        this.userFieldmap.setStartingColumn(form.getUserFieldmap().getStartingColumn());
//        this.userFieldmap.setStartingRange(form.getUserFieldmap().getStartingRange());
//        this.userFieldmap.setPlantingOrder(form.getUserFieldmap().getPlantingOrder());
        
        int startRange = form.getUserFieldmap().getStartingRange() - 1;
        int startCol = form.getUserFieldmap().getStartingColumn() - 1;
        int rows = userFieldmap.getNumberOfRowsInBlock();
        int ranges = userFieldmap.getNumberOfRangesInBlock();
        int rowsPerPlot = userFieldmap.getNumberOfRowsPerPlot();
        boolean isSerpentine = userFieldmap.getPlantingOrder() == 1;
        
        int col = rows / rowsPerPlot;
        //should list here the deleted plot in col-range format
        Map deletedPlot = new HashMap();
        if (form.getMarkedCells() != null && !form.getMarkedCells().isEmpty()) {
            List<String> markedCells = Arrays.asList(form.getMarkedCells().split(","));
            
            for (String markedCell : markedCells) {
                deletedPlot.put(markedCell, markedCell);
            }
        }

        List<String> entryList = fieldmapService.generateFieldMapLabels(userFieldmap);

        Plot[][] plots = fieldmapService.createFieldMap(col, ranges, startRange, startCol,
                isSerpentine, deletedPlot, entryList);
        userFieldmap.setFieldmap(plots);
        form.setUserFieldmap(userFieldmap);

        return super.show(model);
        //return "forward:" + GenerateFieldmapController.URL;
        //return "redirect:" + GenerateFieldmapController.URL;
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

    private void populateFormWithSessionData(FieldmapForm form) {
        UserFieldmap info = userFieldmap;
        info.setNumberOfRowsInBlock(userFieldmap.getNumberOfRowsInBlock());
        info.setNumberOfRangesInBlock(userFieldmap.getNumberOfRangesInBlock());
        info.setNumberOfEntries(userFieldmap.getNumberOfEntries());
        info.setNumberOfReps(userFieldmap.getNumberOfReps());
        info.setNumberOfRowsPerPlot(userFieldmap.getNumberOfRowsPerPlot());
        info.setSelectedName(userFieldmap.getSelectedName());
        info.setPlantingOrder(userFieldmap.getPlantingOrder());
        info.setBlockName(userFieldmap.getBlockName());
        info.setEntryNumbers(userFieldmap.getEntryNumbers());
        info.setFieldLocationId(userFieldmap.getFieldLocationId());
        info.setFieldName(userFieldmap.getFieldName());
        info.setGermplasmNames(userFieldmap.getGermplasmNames());
        info.setReps(userFieldmap.getReps());
        info.setStartingColumn(userFieldmap.getStartingColumn());
        info.setStartingRange(userFieldmap.getStartingRange());
        info.setTotalNumberOfPlots(userFieldmap.getTotalNumberOfPlots());
        info.setTrial(userFieldmap.isTrial());
        info.setLocationName(userFieldmap.getLocationName());
        
        form.setUserFieldmap(info);
    }
    
}
