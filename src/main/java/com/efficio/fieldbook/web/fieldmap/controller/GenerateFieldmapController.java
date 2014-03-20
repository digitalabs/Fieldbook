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
import com.efficio.fieldbook.util.FieldMapUtilityHelper;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.label.printing.service.FieldPlotLayoutIterator;
import com.efficio.fieldbook.web.nursery.controller.ManageNurseriesController;
import com.efficio.fieldbook.web.trial.controller.ManageTrialController;
import com.efficio.fieldbook.web.util.DateUtil;


/**
 * The Class GenerateFieldmapController.
 * 
 * Generates the final fieldmap for the step 3.
 */
@Controller
@RequestMapping({GenerateFieldmapController.URL})
public class GenerateFieldmapController extends AbstractBaseFieldbookController{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(GenerateFieldmapController.class);

    /** The Constant URL. */
    public static final String URL = "/Fieldmap/generateFieldmapView";

    /** The user fieldmap. */
    @Resource
    private UserFieldmap userFieldmap;
    
    /** The fieldmap service. */
    @Resource
    private FieldMapService fieldmapService;
    
    @Resource
    private FieldPlotLayoutIterator verticalFieldMapLayoutIterator;
    
    @Resource
    private FieldPlotLayoutIterator horizontalFieldMapLayoutIterator;
    
    /** The fieldbook middleware service. */
    @Resource
    private FieldbookService fieldbookMiddlewareService;

    /** The export excel service. */
    @Resource
    private ExportExcelService exportExcelService;
    
    /** The Constant BUFFER_SIZE. */
    private static final int BUFFER_SIZE = 4096 * 4;

    /**
     * Show generated fieldmap.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String showGeneratedFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {
        
        form.setUserFieldmap(this.userFieldmap);

        return super.show(model);
    }
    
    /**
     * View fieldmap.
     *
     * @param form the form
     * @param model the model
     * @param datasetId the dataset id
     * @param geolocationId the geolocation id
     * @param studyType the study type
     * @return the string
     */
    @RequestMapping(value="/viewFieldmap/{studyType}/{datasetId}/{geolocationId}", method = RequestMethod.GET)
    public String viewFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model,
            @PathVariable Integer datasetId, @PathVariable Integer geolocationId, 
            @PathVariable String studyType) {
        try {

            this.userFieldmap.setSelectedDatasetId(datasetId);
            this.userFieldmap.setSelectedGeolocationId(geolocationId);
            
            this.userFieldmap.setSelectedFieldMaps(
                    fieldbookMiddlewareService.getAllFieldMapsInBlockByTrialInstanceId(
                            datasetId, geolocationId));

            FieldMapTrialInstanceInfo trialInfo = 
                    this.userFieldmap.getSelectedTrialInstanceByDatasetIdAndGeolocationId(
                                            datasetId, geolocationId);
            if (trialInfo != null) {
                this.userFieldmap.setNumberOfRangesInBlock(trialInfo.getRangesInBlock());
                this.userFieldmap.setNumberOfRowsInBlock(trialInfo.getColumnsInBlock(), 
                        trialInfo.getRowsPerPlot());
                this.userFieldmap.setNumberOfEntries(
                        (long) this.userFieldmap.getAllSelectedFieldMapLabels(false).size()); 
                this.userFieldmap.setNumberOfRowsPerPlot(trialInfo.getRowsPerPlot());
                this.userFieldmap.setPlantingOrder(trialInfo.getPlantingOrder());
                this.userFieldmap.setBlockName(trialInfo.getBlockName());
                this.userFieldmap.setFieldName(trialInfo.getFieldName());
                this.userFieldmap.setLocationName(trialInfo.getLocationName());
                this.userFieldmap.setFieldMapLabels(this.userFieldmap.getAllSelectedFieldMapLabels(false));
                this.userFieldmap.setTrial("trial".equals(studyType));
                this.userFieldmap.setMachineRowCapacity(trialInfo.getMachineRowCapacity());
                
                FieldPlotLayoutIterator plotIterator = horizontalFieldMapLayoutIterator;
                this.userFieldmap.setFieldmap(fieldmapService.generateFieldmap(this.userFieldmap, 
                        plotIterator, false));
            }
            this.userFieldmap.setSelectedFieldmapList(new SelectedFieldmapList(
                    this.userFieldmap.getSelectedFieldMaps(), this.userFieldmap.isTrial()));
            this.userFieldmap.setGenerated(false);
            form.setUserFieldmap(this.userFieldmap);
            
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        return super.show(model);
    }
    
    /**
     * Export excel.
     *
     * @param form the form
     * @param model the model
     * @param response the response
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value="/exportExcel", method = RequestMethod.GET)
    public String exportExcel(@ModelAttribute("fieldmapForm") FieldmapForm form, 
            Model model, HttpServletResponse response) {

        String currentDate = DateUtil.getCurrentDate();
        String fileName = userFieldmap.getBlockName().replace(" ", "") + "-" 
                + currentDate + ".xls"; //changed selected name to block name for now

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
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String submitDetails(@ModelAttribute("FieldmapForm") FieldmapForm form, Model model) {
        
        this.userFieldmap.setStartingColumn(form.getUserFieldmap().getStartingColumn());
        this.userFieldmap.setStartingRange(form.getUserFieldmap().getStartingRange());
        this.userFieldmap.setPlantingOrder(form.getUserFieldmap().getPlantingOrder());
        this.userFieldmap.setMachineRowCapacity(form.getUserFieldmap().getMachineRowCapacity());
        
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

//        List<FieldMapLabel> labels = userFieldmap.getFieldMapLabels();
        List<FieldMapLabel> labels = userFieldmap.getAllSelectedFieldMapLabelsToBeAdded(true);

        //we'll use vertical layouter for now
        /*
        Plot[][] plots = fieldmapService.createFieldMap(col, ranges, startRange, startCol,
                isSerpentine, deletedPlot, labels, userFieldmap.isTrial());
        */
        //we can add logic here to decide if its vertical or horizontal
        FieldPlotLayoutIterator plotIterator = horizontalFieldMapLayoutIterator;
        FieldMapUtilityHelper.markedDeletedPlot(this.userFieldmap.getFieldmap(),deletedPlot);
        Plot[][] plots = plotIterator.createFieldMap(col, ranges, startRange, startCol,
                isSerpentine, deletedPlot, labels, userFieldmap.isTrial(), this.userFieldmap.getFieldmap());
        
        userFieldmap.setFieldmap(plots);
        form.setUserFieldmap(userFieldmap);
        
        this.userFieldmap.setGenerated(true);
        
        return "redirect:" + GenerateFieldmapController.URL;
    }
    
    /**
     * Redirect to main screen.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/showMainPage", method = RequestMethod.GET)
    public String redirectToMainScreen(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {

        if (userFieldmap.isTrial()) {
            return "redirect:" + ManageTrialController.URL;
        }
        else {
            return "redirect:" + ManageNurseriesController.URL;
        }
    }
        
    /**
     * Gets the user fieldmap.
     *
     * @return the user fieldmap
     */
    public UserFieldmap getUserFieldmap() {
        return userFieldmap;
    }
    
    /**
     * Sets the user fieldmap.
     *
     * @param userFieldmap the new user fieldmap
     */
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

    /**
     * Populate form with session data.
     *
     * @param form the form
     */
    private void populateFormWithSessionData(FieldmapForm form) {
        UserFieldmap info = userFieldmap;
        form.setUserFieldmap(info);
    }
    
}
