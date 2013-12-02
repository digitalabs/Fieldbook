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
import java.util.UUID;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
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
    public String saveFieldMap(Model model) {
        
        try {
            if (userFieldmap != null && userFieldmap.getFieldMapLabels() != null && !userFieldmap.getFieldMapLabels().isEmpty()) {
                String fieldmapUUID = UUID.randomUUID().toString();
                FieldMapInfo info = createFieldMapInfo(fieldmapUUID);
                fieldbookMiddlewareService.saveOrUpdateFieldmapProperties(info);
            }
            
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        if (userFieldmap.isTrial()) {
            return "redirect:" + ManageTrialController.URL;
        }
        else {
            return "redirect:" + ManageNurseriesController.URL;
        }
    }

    private FieldMapInfo createFieldMapInfo(String fieldmapUUID) {
        FieldMapTrialInstanceInfo trialInfo = new FieldMapTrialInstanceInfo();
        trialInfo.setGeolocationId(userFieldmap.getSelectedGeolocationId());
        trialInfo.setBlockName(userFieldmap.getBlockName());
        trialInfo.setColumnsInBlock(userFieldmap.getNumberOfColumnsInBlock());
        trialInfo.setRangesInBlock(userFieldmap.getNumberOfRangesInBlock());
        trialInfo.setPlantingOrder(userFieldmap.getPlantingOrder());
        trialInfo.setFieldMapLabels(userFieldmap.getFieldMapLabels());
        trialInfo.setRowsPerPlot(userFieldmap.getNumberOfRowsPerPlot());
        trialInfo.setFieldName(userFieldmap.getFieldName());
        trialInfo.setLocationName(userFieldmap.getLocationName());
        trialInfo.setFieldmapUUID(fieldmapUUID);

        FieldMapDatasetInfo datasetInfo = new FieldMapDatasetInfo();
        datasetInfo.setDatasetId(userFieldmap.getSelectedDatasetId());
        datasetInfo.setTrialInstances(Arrays.asList(trialInfo));

        FieldMapInfo info = new FieldMapInfo(); 
        info.setFieldbookId(userFieldmap.getStudyId());
        info.setDatasets(Arrays.asList(datasetInfo));
        
        return info;
    }
}
