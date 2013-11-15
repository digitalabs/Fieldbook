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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.nursery.controller.ManageNurseriesController;
import com.efficio.fieldbook.web.trial.controller.ManageTrialController;

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
    
    private static final int BUFFER_SIZE = 4096;

    @RequestMapping(method = RequestMethod.GET)
    public String showGeneratedFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {
        
        //TODO: FOR testing only, remove this
        populateFormWithSessionData(form);
        
        return super.show(model);
    }
    @ResponseBody
    @RequestMapping(value="/exportExcel", method = RequestMethod.GET)
    public String exportExcel(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model, HttpServletResponse response) {
        
        response.setHeader("Content-disposition","attachment; filename=random.xls");
        File xls = new File("exported.xls"); // or whatever your file is
        FileInputStream in;
        try {
            in = new FileInputStream(xls);
        
            OutputStream out = response.getOutputStream();
    
            byte[] buffer= new byte[BUFFER_SIZE]; // use bigger if you want
            int length = 0;
    
            while ((length = in.read(buffer)) > 0){
                 out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
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
        
        int startRange = userFieldmap.getStartingRange() - 1;
        int startCol = userFieldmap.getStartingColumn() - 1;
        int rows = userFieldmap.getNumberOfRowsInBlock();
        int ranges = userFieldmap.getNumberOfRangesInBlock();
        int rowsPerPlot = userFieldmap.getNumberOfRowsPerPlot();
        boolean isSerpentine = userFieldmap.getPlantingOrder() == 2;
        
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

        return "redirect:" + GenerateFieldmapController.URL;
    }
    
    @RequestMapping(value="/showMainPage", method = RequestMethod.GET)
    public String redirectToMainScreen(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {

        if (userFieldmap.isTrial()) {
            return "redirect:" + ManageTrialController.URL;
        }
        else {
            return "redirect:" + ManageNurseriesController.URL;
        }
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
