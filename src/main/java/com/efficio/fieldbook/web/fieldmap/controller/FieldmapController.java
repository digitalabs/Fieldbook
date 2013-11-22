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

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;


// TODO: Auto-generated Javadoc
/**
 * The Class FieldmapController.
 */
@Controller
@RequestMapping({FieldmapController.URL})
public class FieldmapController extends AbstractBaseFieldbookController{
 
 /** The Constant LOG. */
 private static final Logger LOG = LoggerFactory.getLogger(FieldmapController.class);
    
    /** The Constant URL. */
    public static final String URL = "/Fieldmap/enterFieldDetails";

    
    /** The user selection. */
    @Resource
    private UserFieldmap userFieldmap;
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    /**
     * Gets the data types.
     *
     * @return the data types
     */
    @ModelAttribute("locationList")
    public List<Location> getLocationList() {
        try {
            List<Location> dataTypesOrig = fieldbookMiddlewareService.getAllLocations();
            List<Location> dataTypes = dataTypesOrig;
            /*
            for(int i = 0 ; i < 5000 ; i++){
                Location loc = new Location();
                loc.setLname(dataTypesOrig.size() + " LNAME " + i);
                loc.setLocid(i);
                dataTypes.add(loc);
            }
            */
            
            return dataTypes;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
   
    /**
     * Show trial.
     *
     * @param form the form
     * @param id the id
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(value="/trial/{id}", method = RequestMethod.GET)
    public String showTrial(@ModelAttribute("fieldmapForm") FieldmapForm form, 
            @PathVariable String id, 
            Model model, HttpSession session) {
        session.invalidate();
        
        try {
            //TODO: GET FROM FORM
            List<DatasetReference> datasets = fieldbookMiddlewareService.getDatasetReferences(Integer.parseInt(id));
            if (Integer.parseInt(id) < 0) {
                userFieldmap.setSelectedDatasetId(datasets.get(0).getId());
            } else {
                userFieldmap.setSelectedDatasetId(datasets.get(datasets.size()-1).getId());
            }
            userFieldmap.setSelectedGeolocationId(fieldbookMiddlewareService.getGeolocationId(Integer.parseInt(id)));
            
            FieldMapInfo fieldMapInfo = fieldbookMiddlewareService.getFieldMapInfoOfTrial(Integer.parseInt(id));
            
            this.userFieldmap.setUserFieldmapInfo(fieldMapInfo, true);
            /*
            this.userFieldmap = new UserFieldmap();
            this.userFieldmap.setNumberOfRowsPerPlot(2);
            */
            form.setUserFieldmap(userFieldmap);    
        } catch (NumberFormatException e) {
            LOG.error(e.toString());
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString());
        }
        
       
        return super.show(model);
    }
    
    /**
     * Show nursery.
     *
     * @param form the form
     * @param id the id
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(value="/nursery/{id}", method = RequestMethod.GET)
    public String showNursery(@ModelAttribute("fieldmapForm") FieldmapForm form, 
            @PathVariable String id, 
            Model model, HttpSession session) {
        session.invalidate();
        
        try {
            //TODO: GET FROM FORM
            List<DatasetReference> datasets = fieldbookMiddlewareService.getDatasetReferences(Integer.parseInt(id));
            userFieldmap.setSelectedDatasetId(datasets.get(datasets.size()-1).getId());
            userFieldmap.setSelectedGeolocationId(fieldbookMiddlewareService.getGeolocationId(Integer.parseInt(id)));
            
            FieldMapInfo fieldMapInfo = fieldbookMiddlewareService.getFieldMapInfoOfNursery(Integer.parseInt(id));
            this.userFieldmap.setUserFieldmapInfo(fieldMapInfo, false);
            form.setUserFieldmap(userFieldmap);
        } catch (NumberFormatException e) {
            LOG.error(e.toString());
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString());
        }
               
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
    public String submitDetails(@ModelAttribute("fieldmapForm") FieldmapForm form, BindingResult result, Model model) {
        
        //if fieldmap already exists, forward to GenerateFieldmapController with the fieldmap data. (populate userFieldMap)
        //otherwise, redirect to PlantingDetailsController
        
        setUserFieldMapDetails(form);
        return "redirect:" + PlantingDetailsController.URL;
    } 
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "Fieldmap/enterFieldDetails";
    }
    
    
    /**
     * Gets the user fieldmap.
     *
     * @return the user fieldmap
     */
    public UserFieldmap getUserFieldmap() {
        return this.userFieldmap;
    }
    
    private void setUserFieldMapDetails(FieldmapForm form) {        
        this.userFieldmap.setBlockName(form.getUserFieldmap().getBlockName());
        this.userFieldmap.setFieldLocationId(form.getUserFieldmap().getFieldLocationId());
        this.userFieldmap.setFieldName(form.getUserFieldmap().getFieldName());
        this.userFieldmap.setNumberOfRangesInBlock(form.getUserFieldmap().getNumberOfRangesInBlock());
        this.userFieldmap.setNumberOfRowsInBlock(form.getUserFieldmap().getNumberOfRowsInBlock());
        this.userFieldmap.setNumberOfRowsPerPlot(form.getUserFieldmap().getNumberOfRowsPerPlot());
        this.userFieldmap.setLocationName(form.getUserFieldmap().getLocationName());
    }
}
