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

import java.util.UUID;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.label.printing.controller.LabelPrintingController;
import com.efficio.fieldbook.web.nursery.controller.ManageNurseriesController;
import com.efficio.fieldbook.web.trial.controller.ManageTrialController;


@Controller
@RequestMapping({SaveFieldmapController.URL})
public class SaveFieldmapController extends AbstractBaseFieldbookController{

    private static final Logger LOG = LoggerFactory.getLogger(SaveFieldmapController.class);

    public static final String URL = "/Fieldmap/saveFieldmap";

    @Resource
    private UserFieldmap userFieldmap;
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    

    @Override
    public String getContentName() {
        return null;
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String saveFieldMap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {
        
        try {
            if (userFieldmap != null && userFieldmap.getSelectedFieldMaps() != null && !userFieldmap.getSelectedFieldMaps().isEmpty()) {
                String fieldmapUUID = UUID.randomUUID().toString();
                updateSelectedFieldMapInfo(fieldmapUUID);
                fieldbookMiddlewareService.saveOrUpdateFieldmapProperties(this.userFieldmap.getSelectedFieldMaps(), fieldmapUUID);
            }
            
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        if("1".equalsIgnoreCase(form.getSaveAndRedirectToCreateLabel())){            
            return "redirect:" + LabelPrintingController.URL+"/fieldmap";            
        }
        
        if (userFieldmap.isTrial()) {
            return "redirect:" + ManageTrialController.URL;
        }
        else {
            return "redirect:" + ManageNurseriesController.URL;
        }
    }

    private void updateSelectedFieldMapInfo(String fieldmapUUID) {
        for (FieldMapInfo info : this.userFieldmap.getSelectedFieldMaps()) {
            for (FieldMapDatasetInfo datasetInfo : info.getDatasets()) {
                for (FieldMapTrialInstanceInfo trialInfo : datasetInfo.getTrialInstances()) {
                    trialInfo.setBlockName(userFieldmap.getBlockName());
                    trialInfo.setColumnsInBlock(userFieldmap.getNumberOfColumnsInBlock());
                    trialInfo.setRangesInBlock(userFieldmap.getNumberOfRangesInBlock());
                    trialInfo.setPlantingOrder(userFieldmap.getPlantingOrder());
                    trialInfo.setRowsPerPlot(userFieldmap.getNumberOfRowsPerPlot());
                    trialInfo.setFieldName(userFieldmap.getFieldName());
                    trialInfo.setLocationName(userFieldmap.getLocationName());
                    trialInfo.setFieldmapUUID(fieldmapUUID);
                    trialInfo.setMachineRowCapacity(userFieldmap.getMachineRowCapacity());
                }
            }
        }
    }
}
