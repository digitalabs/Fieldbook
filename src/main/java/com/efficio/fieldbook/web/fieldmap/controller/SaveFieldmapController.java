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

import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.label.printing.controller.LabelPrintingController;
import com.efficio.fieldbook.web.nursery.controller.ManageNurseriesController;
import com.efficio.fieldbook.web.trial.controller.ManageTrialController;

/**
 * The Class SaveFieldmapController.
 * 
 * Controller is being use to save the fieldmap details in the database.
 */
@Controller
@RequestMapping({SaveFieldmapController.URL})
public class SaveFieldmapController extends AbstractBaseFieldbookController{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SaveFieldmapController.class);

    /** The Constant URL. */
    public static final String URL = "/Fieldmap/saveFieldmap";

    /** The user fieldmap. */
    @Resource
    private UserFieldmap userFieldmap;
    
    /** The fieldbook middleware service. */
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private WorkbenchService workbenchService;
    

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return null;
    }
    
    /**
     * Save field map.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String saveFieldMap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {
        
        try {
            if (userFieldmap != null && userFieldmap.getSelectedFieldMaps() != null 
                    && !userFieldmap.getSelectedFieldMaps().isEmpty()) {
            	
                updateSelectedFieldMapInfo();
                int userId = this.getCurrentIbdbUserId();
                fieldbookMiddlewareService.saveOrUpdateFieldmapProperties(
                        this.userFieldmap.getSelectedFieldMaps(), userId, userFieldmap.isNew());
            }
            
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        if("1".equalsIgnoreCase(form.getSaveAndRedirectToCreateLabel())){            
            return "redirect:" + LabelPrintingController.URL+"/fieldmap";            
        }
        
        if (userFieldmap.isTrial()) {
            return "redirect:" + ManageTrialController.URL;
        } else {
            return "redirect:" + ManageNurseriesController.URL;
        }
    }

    /**
     * Update selected field map info.
     *
     * @param fieldmapUUID the fieldmap uuid
     */
    private void updateSelectedFieldMapInfo() {
        for (FieldMapInfo info : this.userFieldmap.getSelectedFieldMaps()) {
            for (FieldMapDatasetInfo datasetInfo : info.getDatasets()) {
                for (FieldMapTrialInstanceInfo trialInfo : datasetInfo.getTrialInstances()) {
                	trialInfo.setLocationId(userFieldmap.getFieldLocationId());
                	trialInfo.setFieldId(userFieldmap.getFieldId());
                	trialInfo.setBlockId(userFieldmap.getBlockId());
                    trialInfo.setRowsInBlock(userFieldmap.getNumberOfRowsInBlock());
                    trialInfo.setRangesInBlock(userFieldmap.getNumberOfRangesInBlock());
                    trialInfo.setPlantingOrder(userFieldmap.getPlantingOrder());
                    trialInfo.setRowsPerPlot(userFieldmap.getNumberOfRowsPerPlot());
                    trialInfo.setMachineRowCapacity(userFieldmap.getMachineRowCapacity());
                    trialInfo.setDeletedPlots(userFieldmap.getDeletedPlots());
                	
                    //TODO: CLEAN UP, no longer needed
                    trialInfo.setBlockName(userFieldmap.getBlockName());
                    trialInfo.setFieldName(userFieldmap.getFieldName());
                    trialInfo.setLocationName(userFieldmap.getLocationName());
                }
            }
        }
    }
}
