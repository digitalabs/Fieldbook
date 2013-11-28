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

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
    private UserFieldmap userFieldmap;
    
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
        form.setUserFieldmap(this.userFieldmap);

        //TEST DATA FOR GCP-6321
        this.userFieldmap.setSelectedFieldMaps(this.userFieldmap.getFieldMapInfo());
        
        return super.show(model);
    }
    
    @RequestMapping(value="/viewFieldmap/{datasetId}/{geolocationId}", method = RequestMethod.GET)
    public String viewFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model,
            @PathVariable Integer datasetId, @PathVariable Integer geolocationId) {
        try {

            this.userFieldmap.setSelectedDatasetId(datasetId);
            this.userFieldmap.setSelectedGeolocationId(geolocationId);
            List<FieldMapInfo> fieldMapInfo = userFieldmap.getFieldMapInfo();
            FieldMapTrialInstanceInfo trialInfo = fieldMapInfo.get(0).getDataSet(datasetId).getTrialInstance(geolocationId); 
            this.userFieldmap.setNumberOfRangesInBlock(trialInfo.getRangesInBlock());
            this.userFieldmap.setNumberOfRowsInBlock(trialInfo.getColumnsInBlock(), trialInfo.getRowsPerPlot());
            this.userFieldmap.setUserFieldmapInfo(fieldMapInfo, true);
            this.userFieldmap.setNumberOfRowsPerPlot(trialInfo.getRowsPerPlot());
            this.userFieldmap.setPlantingOrder(trialInfo.getPlantingOrder());
            this.userFieldmap.setBlockName(trialInfo.getBlockName());
            
            populateFormWithSessionData(form);
            this.userFieldmap.setFieldmap(fieldmapService.generateFieldmap(this.userFieldmap));
            form.setUserFieldmap(this.userFieldmap);
            
            //TEST DATA FOR GCP-6321
            this.userFieldmap.setSelectedFieldMaps(this.userFieldmap.getFieldMapInfo());
            
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        return super.show(model);
    }
    
    @ResponseBody
    @RequestMapping(value="/exportExcel", method = RequestMethod.GET)
    public String exportExcel(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model, HttpServletResponse response) {

        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = userFieldmap.getBlockName().replace(" ", "") + "_" + currentDate + ".xls"; //changed selected name to block name for now

        response.setHeader("Content-disposition","attachment; filename=" + fileName);

        File xls = new File(fileName); // the selected name + current date
        FileInputStream in;
        
        try {
            exportExcelService.exportFieldMapToExcel(fileName, userFieldmap);

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

        List<FieldMapLabel> labels = userFieldmap.getFieldMapLabels();

      //changed selected name to block name for now
        Plot[][] plots = fieldmapService.createFieldMap(col, ranges, startRange, startCol,
                isSerpentine, deletedPlot, labels, userFieldmap.isTrial(), userFieldmap.getBlockName());
        
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
        form.setUserFieldmap(info);
    }
    
}
