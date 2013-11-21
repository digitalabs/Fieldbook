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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.ExportExcelService;
import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.nursery.controller.ManageNurseriesController;
import com.efficio.fieldbook.web.trial.controller.ManageTrialController;

@Controller
@RequestMapping({GenerateFieldmapController.URL})
public class GenerateFieldmapController extends AbstractBaseFieldbookController{

    private static final Logger LOG = LoggerFactory.getLogger(GenerateFieldmapController.class);

    public static final String URL = "/Fieldmap/generateFieldmapView";

    @Resource
    private UserFieldmap userFieldMap;
    
    @Resource
    private FieldMapService fieldmapService;
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;

    @Resource
    private ExportExcelService exportExcelService;
    
    private static final int BUFFER_SIZE = 4096 * 4;

    @RequestMapping(method = RequestMethod.GET)
    public String showGeneratedFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {
        
        populateFormWithSessionData(form);
        
        return super.show(model);
    }
    @ResponseBody
    @RequestMapping(value="/exportExcel", method = RequestMethod.GET)
    public String exportExcel(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model, HttpServletResponse response) {

        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = userFieldMap.getSelectedName().replace(" ", "") + "_" + currentDate + ".xls";

        response.setHeader("Content-disposition","attachment; filename=" + fileName);

        File xls = new File(fileName); // the selected name + current date
        FileInputStream in;
        
        try {
            exportExcelService.exportFieldMapToExcel(fileName, userFieldMap);

            in = new FileInputStream(xls);
            OutputStream out = response.getOutputStream();

            byte[] buffer= new byte[BUFFER_SIZE]; // use bigger if you want
            int length = 0;

            while ((length = in.read(buffer)) > 0){
                 out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } catch (FieldbookException e) {
            LOG.error(e.getMessage(), e);
        } catch (FileNotFoundException e) {
        	LOG.error(e.getMessage(), e);
        } catch (IOException e) {
        	LOG.error(e.getMessage(), e);
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
        this.userFieldMap.setStartingColumn(form.getUserFieldmap().getStartingColumn());
        this.userFieldMap.setStartingRange(form.getUserFieldmap().getStartingRange());
        this.userFieldMap.setPlantingOrder(form.getUserFieldmap().getPlantingOrder());
        
        int startRange = userFieldMap.getStartingRange() - 1;
        int startCol = userFieldMap.getStartingColumn() - 1;
        int rows = userFieldMap.getNumberOfRowsInBlock();
        int ranges = userFieldMap.getNumberOfRangesInBlock();
        int rowsPerPlot = userFieldMap.getNumberOfRowsPerPlot();
        boolean isSerpentine = userFieldMap.getPlantingOrder() == 2;
        
        int col = rows / rowsPerPlot;
        //should list here the deleted plot in col-range format
        Map deletedPlot = new HashMap();
        if (form.getMarkedCells() != null && !form.getMarkedCells().isEmpty()) {
            List<String> markedCells = Arrays.asList(form.getMarkedCells().split(","));
            
            for (String markedCell : markedCells) {
                deletedPlot.put(markedCell, markedCell);
            }
        }

        //List<String> entryList = fieldmapService.generateFieldMapLabels(userFieldMap);
        List<FieldMapLabel> labels = userFieldMap.getFieldMapLabels();

        Plot[][] plots = fieldmapService.createFieldMap(col, ranges, startRange, startCol,
                isSerpentine, deletedPlot, labels, userFieldMap.isTrial(), userFieldMap.getSelectedName());
        userFieldMap.setFieldmap(plots);
        form.setUserFieldmap(userFieldMap);

        return "redirect:" + GenerateFieldmapController.URL;
    }
    
    @RequestMapping(value="/showMainPage", method = RequestMethod.GET)
    public String redirectToMainScreen(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {

        if (userFieldMap.isTrial()) {
            return "redirect:" + ManageTrialController.URL;
        }
        else {
            return "redirect:" + ManageNurseriesController.URL;
        }
    }
        
    public UserFieldmap getUserFieldmap() {
        return userFieldMap;
    }
    
    public void setUserFieldmap(UserFieldmap userFieldmap) {
        this.userFieldMap = userFieldmap;
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "Fieldmap/generateFieldmapView";
    }

    private void populateFormWithSessionData(FieldmapForm form) {
        UserFieldmap info = userFieldMap;
        info.setNumberOfRowsInBlock(userFieldMap.getNumberOfRowsInBlock());
        info.setNumberOfRangesInBlock(userFieldMap.getNumberOfRangesInBlock());
        info.setNumberOfEntries(userFieldMap.getNumberOfEntries());
        info.setNumberOfReps(userFieldMap.getNumberOfReps());
        info.setNumberOfRowsPerPlot(userFieldMap.getNumberOfRowsPerPlot());
        info.setSelectedName(userFieldMap.getSelectedName());
        info.setPlantingOrder(userFieldMap.getPlantingOrder());
        info.setBlockName(userFieldMap.getBlockName());
        info.setEntryNumbers(userFieldMap.getEntryNumbers());
        info.setFieldLocationId(userFieldMap.getFieldLocationId());
        info.setFieldName(userFieldMap.getFieldName());
        info.setGermplasmNames(userFieldMap.getGermplasmNames());
        info.setReps(userFieldMap.getReps());
        info.setStartingColumn(userFieldMap.getStartingColumn());
        info.setStartingRange(userFieldMap.getStartingRange());
        info.setTotalNumberOfPlots(userFieldMap.getTotalNumberOfPlots());
        info.setTrial(userFieldMap.isTrial());
        info.setLocationName(userFieldMap.getLocationName());
        
        form.setUserFieldmap(info);
    }
    
}
