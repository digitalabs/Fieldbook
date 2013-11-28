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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
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
import org.springframework.web.bind.annotation.ResponseBody;

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
    @Resource
    private WorkbenchDataManager workbenchDataManager;
    
    
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
    
    @ModelAttribute("favoriteLocationList")
    public List<Location> getFavoriteLocationList() {
        try {
            
            List<Long> locationsIds = workbenchDataManager.getFavoriteProjectLocationIds(Long.valueOf(this.getCurrentProjectId()), 0,  Integer.MAX_VALUE);
            List<Location> dataTypes = fieldbookMiddlewareService.getFavoriteLocationByProjectId(locationsIds);
            /*dataTypes = new ArrayList();
            for(int i = 0 ; i < 50 ; i++){
                Location loc = new Location();
                loc.setLname(" LNAME " + i);
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
    
    @ResponseBody
    @RequestMapping(value="/createFieldmap/{id}", method = RequestMethod.GET)
    public Map<String, String> determineFieldMapNavigation(@PathVariable String id, Model model, HttpSession session) {
        
        session.invalidate();
        Map<String, String> result = new HashMap<String, String>();
        
        String nav = "1";
        try {
            List<Integer> trialIds = new ArrayList<Integer>();
            trialIds.add(Integer.parseInt(id));
            List<FieldMapInfo> fieldMapInfoList = fieldbookMiddlewareService.getFieldMapInfoOfTrial(trialIds);

            this.userFieldmap.setUserFieldmapInfo(fieldMapInfoList, true);
            
            for (FieldMapInfo fieldMapInfo : fieldMapInfoList) {
                List<FieldMapDatasetInfo> datasetList = fieldMapInfo.getDatasetsWithFieldMap();
                if (datasetList != null && !datasetList.isEmpty()) {
                    List<FieldMapTrialInstanceInfo> trials = datasetList.get(0).getTrialInstancesWithFieldMap();
                    if (trials != null && !trials.isEmpty()) {
                        nav = "0";
                    }
                }
            } 
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        result.put("nav", nav);
        return result;
    }
    
    @ResponseBody
    @RequestMapping(value="/selectTrialInstance", method = RequestMethod.GET)
    public Map<String, String> getFieldMapInfoData() {
        Map<String, String> result = new HashMap<String, String>();
        List<FieldMapInfo> fieldMapInfoList = userFieldmap.getFieldMapInfo();
        String size = "0";
        String fieldMapInfoJson = null;
        for (FieldMapInfo fieldMapInfo : fieldMapInfoList) {
            List<FieldMapDatasetInfo> datasetList = fieldMapInfo.getDatasetsWithFieldMap();
            if (datasetList != null && !datasetList.isEmpty()) {
                List<FieldMapTrialInstanceInfo> trials = datasetList.get(0).getTrialInstancesWithFieldMap();
                if (trials != null && !trials.isEmpty()) {
                    size = String.valueOf(trials.size());
                }
            }
            fieldMapInfoJson = convertFieldMapInfoToJson(fieldMapInfo);
        }
        
        result.put("fieldMapInfo", fieldMapInfoJson);
        result.put("size", size);
        return result;
    } 
    
    private String convertFieldMapInfoToJson(FieldMapInfo fieldMapInfo) {
        if (fieldMapInfo!= null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(fieldMapInfo);
            } catch(Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return "";
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
        try {
            form.setUserFieldmap(userFieldmap);
        } catch (NumberFormatException e) {
            LOG.error(e.toString());
        }
               
        return super.show(model);
    }
    
    @ResponseBody
    @RequestMapping(value="/createNurseryFieldmap/{id}", method = RequestMethod.GET)
    public Map<String, String> determineNurseryFieldMapNavigation(@PathVariable String id, HttpSession session) {
        
        session.invalidate();
        Map<String, String> result = new HashMap<String, String>();
        
        String nav = "1";
        try {
            List<Integer> nurseryIds = new ArrayList<Integer>();
            nurseryIds.add(Integer.parseInt(id));
            
            List<FieldMapInfo> fieldMapInfoList = fieldbookMiddlewareService.getFieldMapInfoOfNursery(nurseryIds);

            this.userFieldmap.setUserFieldmapInfo(fieldMapInfoList, false);
            
            for (FieldMapInfo fieldMapInfo : fieldMapInfoList) {
                List<FieldMapDatasetInfo> datasetList = fieldMapInfo.getDatasetsWithFieldMap();
                if (datasetList != null && !datasetList.isEmpty()) {
                    List<FieldMapTrialInstanceInfo> trials = datasetList.get(0).getTrialInstancesWithFieldMap();
                    if (trials != null && !trials.isEmpty()) {
                        FieldMapDatasetInfo dataset = datasetList.get(0);
                        nav = "0";
                        this.userFieldmap.setSelectedDatasetId(dataset.getDatasetId());
                        this.userFieldmap.setSelectedGeolocationId(dataset.getTrialInstancesWithFieldMap().get(0).getGeolocationId());
                        result.put("datasetId", this.userFieldmap.getSelectedDatasetId().toString());
                        result.put("geolocationId", this.userFieldmap.getSelectedGeolocationId().toString());
                    }
                }
            } 
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        result.put("nav", nav);
        return result;
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
        try {
            form.setUserFieldmap(userFieldmap);

        } catch (NumberFormatException e) {
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
        this.userFieldmap.setSelectedDatasetId(form.getUserFieldmap().getSelectedDatasetId());
        this.userFieldmap.setSelectedGeolocationId(form.getUserFieldmap().getSelectedGeolocationId());
        this.userFieldmap.setUserFieldmapInfo(userFieldmap.getFieldMapInfo(), this.userFieldmap.isTrial() ? true : false);
        this.userFieldmap.setNumberOfEntries(form.getUserFieldmap().getNumberOfEntries());
        this.userFieldmap.setNumberOfReps(form.getUserFieldmap().getNumberOfReps());
        this.userFieldmap.setTotalNumberOfPlots(form.getUserFieldmap().getTotalNumberOfPlots());
        this.userFieldmap.setBlockName(form.getUserFieldmap().getBlockName());
        this.userFieldmap.setFieldLocationId(form.getUserFieldmap().getFieldLocationId());
        this.userFieldmap.setFieldName(form.getUserFieldmap().getFieldName());
        this.userFieldmap.setNumberOfRangesInBlock(form.getUserFieldmap().getNumberOfRangesInBlock());
        this.userFieldmap.setNumberOfRowsInBlock(form.getUserFieldmap().getNumberOfRowsInBlock());
        this.userFieldmap.setNumberOfRowsPerPlot(form.getUserFieldmap().getNumberOfRowsPerPlot());
        this.userFieldmap.setLocationName(form.getUserFieldmap().getLocationName());
    }
}
