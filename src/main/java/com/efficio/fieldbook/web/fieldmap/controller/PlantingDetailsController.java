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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.label.printing.service.FieldPlotLayoutIterator;

/**
 * The Class PlantingDetailsController.
 */
@Controller
@RequestMapping({PlantingDetailsController.URL})
public class PlantingDetailsController extends AbstractBaseFieldbookController{
 
    /** The Constant URL. */
    public static final String URL = "/Fieldmap/plantingDetails";

    /** The user selection. */
    @Resource
    private UserFieldmap userFieldmap;
    
    @Resource
    private FieldMapService fieldmapService;
    
    @Resource 
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private FieldPlotLayoutIterator horizontalFieldMapLayoutIterator;
    
   
    /**
     * Show.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model, HttpSession session) {
    	try {
	        setPrevValues(form);
	        List<FieldMapInfo> infos = fieldbookMiddlewareService.getAllFieldMapsInBlockByBlockId(
	        		userFieldmap.getBlockId());
	        if(this.userFieldmap.getSelectedFieldMapsToBeAdded() == null){
	        	this.userFieldmap.setSelectedFieldMapsToBeAdded(new ArrayList(this.userFieldmap.getSelectedFieldMaps()));
	        }
	        if (infos != null && !infos.isEmpty()) {
		       // this.userFieldmap.setSelectedFieldMaps(infos);
	        	//this is to add the new nusery
		        List<FieldMapInfo> fieldmapInfoList =  new ArrayList(this.userFieldmap.getSelectedFieldMapsToBeAdded());		        
		        fieldmapInfoList.addAll(infos);
	        	this.userFieldmap.setSelectedFieldMaps(fieldmapInfoList);
	            this.userFieldmap.setSelectedFieldmapList(new SelectedFieldmapList(
	                    this.userFieldmap.getSelectedFieldMaps(), this.userFieldmap.isTrial()));

	            this.userFieldmap.setFieldMapLabels(this.userFieldmap.getAllSelectedFieldMapLabels(false));
                FieldPlotLayoutIterator plotIterator = horizontalFieldMapLayoutIterator;
                this.userFieldmap.setFieldmap(fieldmapService.generateFieldmap(this.userFieldmap, 
                        plotIterator));
                
                FieldMapTrialInstanceInfo trialInfo = this.userFieldmap.getAnySelectedTrialInstance();
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
                    this.userFieldmap.setMachineRowCapacity(trialInfo.getMachineRowCapacity());
                }
	        }
	        form.setUserFieldmap(this.userFieldmap);
	        
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return super.show(model);
    }
    
    private void setPrevValues(FieldmapForm form) {
        UserFieldmap info = new UserFieldmap();
        info.setNumberOfRangesInBlock(userFieldmap.getNumberOfRangesInBlock());
        info.setNumberOfRowsInBlock(userFieldmap.getNumberOfRowsInBlock());
        form.setUserFieldmap(info);
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "Fieldmap/enterPlantingDetails";
    }
    
    /**
     * Gets the user fieldmap.
     *
     * @return the user fieldmap
     */
    public UserFieldmap getUserFieldmap() {
        return this.userFieldmap;
    }
       
}
