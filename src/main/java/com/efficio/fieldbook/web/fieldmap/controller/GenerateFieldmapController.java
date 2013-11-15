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
    
    @RequestMapping(method = RequestMethod.POST)
    public String generateFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) throws Exception {
        /*System.out.println("GENERATING FIELD MAP...." + form.getMarkedCells());
        //TODO: FOR testing only, remove this 
        populateFormWithSessionData(form);
        
        List<String> fieldmapLabels = fieldmapService.createFieldmap(form.getUserFieldmap());
        form.setFieldmapLabels(fieldmapLabels);
        
        if (form.getMarkedCells() != null && !form.getMarkedCells().isEmpty()) {
            List<String> markedCells = Arrays.asList(form.getMarkedCells().replace("cell", "").split(","));
            
            List<Element> fieldmapShapes = fieldmapService.createFieldmap(form.getUserFieldmap(), markedCells, 5, 5);
            form.setFieldmapShapes(fieldmapShapes);
        }*/
//        populateFormWithDummyData(form);
        
        UserFieldmap info = userFieldmap;
        form.setUserFieldmap(info);
        int reps = info.getNumberOfReps().intValue();
        int startRange = info.getStartingRange();
        int startCol = info.getStartingColumn();
        int rows = info.getNumberOfRowsInBlock();
        int ranges = info.getNumberOfRangesInBlock();
        int rowsPerPlot = info.getNumberOfRowsPerPlot();
        boolean isSerpentine = info.getPlantingOrder() == 1;
        int col = rows / rowsPerPlot;
        
        Map deletedPlot = new HashMap();
        if (form.getMarkedCells() != null && !form.getMarkedCells().isEmpty()) {
            List<String> markedCells = Arrays.asList(form.getMarkedCells().split(","));
            
            for (String markedCell : markedCells) {
                deletedPlot.put(markedCell, markedCell);
            }
        }

        FieldMapInfo fieldMapInfo = fieldbookMiddlewareService.getFieldMapInfoOfNursery(-209);
        UserFieldmap map = new UserFieldmap();
        map.setUserFieldmapInfo(fieldMapInfo, false);
        form.setUserFieldmap(userFieldmap);
        List<String> entryList = fieldmapService.generateFieldMapLabels(map);
        Plot[][] plots = fieldmapService.createFieldMap(col, ranges, startRange, startCol,
                isSerpentine, deletedPlot, entryList);
        info.setFieldmap(plots);
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
