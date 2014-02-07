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
package com.efficio.fieldbook.web.nursery.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.Project;
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

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

/**
 * The Class AddOrRemoveTraitsController.
 */
@Controller
@RequestMapping(AdvancingController.URL)
public class AdvancingController extends AbstractBaseFieldbookController{

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/advance/nursery";
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AdvancingController.class);
    
    /** The user selection. */
    @Resource
    private AdvancingNursery advancingNursery;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    @Resource
    private MeasurementsGeneratorService measurementsGeneratorService;
    @Resource
    private WorkbenchDataManager workbenchDataManager;

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/advancingNursery";
    }
    
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
            
            return dataTypes;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
    
    /**
     * Gets the favorite location list.
     *
     * @return the favorite location list
     */
    @ModelAttribute("favoriteLocationList")
    public List<Location> getFavoriteLocationList() {
        try {
            
            List<Long> locationsIds = workbenchDataManager.getFavoriteProjectLocationIds(
                    Long.valueOf(this.getCurrentProjectId()), 0,  Integer.MAX_VALUE);
            List<Location> dataTypes = fieldbookMiddlewareService
                                .getFavoriteLocationByProjectId(locationsIds);
            
            return dataTypes;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
    
    @ModelAttribute("methodList")
    public List<Method> getBreedingMethodList() {
        try {
            List<Method> dataTypesOrig = fieldbookMiddlewareService.getAllBreedingMethods();
            List<Method> dataTypes = dataTypesOrig;
            
            return dataTypes;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
    
    /**
     * Gets the favorite location list.
     *
     * @return the favorite location list
     */
    @ModelAttribute("favoriteMethodList")
    public List<Method> getFavoriteMethodList() {
    	
        try {
          Project project = new Project();
          project.setProjectId(Long.valueOf(this.getCurrentProjectId()));
          
            List<Integer> methodIds = workbenchDataManager.getFavoriteProjectMethods(
            		project, 0,  Integer.MAX_VALUE);
            List<Method> dataTypes = fieldbookMiddlewareService.getFavoriteBreedingMethods(methodIds);
            
            return dataTypes;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
    	   
        return null;
    }
    
    /**
     * Shows the screen
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(value="/{nurseryId}", method = RequestMethod.GET)
    public String show(@ModelAttribute("advancingNurseryform") AdvancingNurseryForm form
            , Model model, HttpSession session, @PathVariable int nurseryId) throws MiddlewareQueryException{
    	session.invalidate();
    	form.setMethodChoice("1");
    	form.setLineChoice("1");
    	form.setLineSelected("1");
    	form.setProjectId(this.getCurrentProjectId());
    	//form.setBreedingMethods();
    	Study study = fieldbookMiddlewareService.getStudy(nurseryId);
    	List<Variable> varList = study.getConditions().getVariables();
    	form.setDefaultMethodId(Integer.toString(AppConstants.SINGLE_PLANT_SELECTION_SF));
    	for(Variable var : varList){
    		if(var.getVariableType().getStandardVariable().getId() == TermId.BREEDING_METHOD_ID.getId() && var.getValue() != null){
    			form.setDefaultMethodId(var.getValue());
    		}
    	}
    	advancingNursery.setStudy(study);
    	return super.show(model);
    }
    
    @ResponseBody
    @RequestMapping(value="/getBreedingMethods", method = RequestMethod.GET)
    public Map<String, String> getBreedingMethods() {
        Map<String, String> result = new HashMap<String, String>();;
        
        try {
            List<Method> breedingMethods = fieldbookMiddlewareService.getAllBreedingMethods();
            Project project = new Project();
            project.setProjectId(Long.valueOf(this.getCurrentProjectId()));
            
              List<Integer> methodIds = workbenchDataManager.getFavoriteProjectMethods(
                          project, 0,  Integer.MAX_VALUE);
              List<Method> favoriteMethods = fieldbookMiddlewareService.getFavoriteBreedingMethods(methodIds);
            result.put("success", "1");
            result.put("allMethods", convertMethodsToJson(breedingMethods));
            result.put("favoriteMethods", convertMethodsToJson(favoriteMethods));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("success", "-1");
            result.put("errorMessage", e.getMessage());
        }
        
        return result;
    }
    
    @ResponseBody
    @RequestMapping(value="/getLocations", method = RequestMethod.GET)
    public Map<String, String> getLocations() {
        Map<String, String> result = new HashMap<String, String>();;
        
        try {
            List<Long> locationsIds = workbenchDataManager.getFavoriteProjectLocationIds(
                    Long.valueOf(this.getCurrentProjectId()), 0,  Integer.MAX_VALUE);
            List<Location> faveLocations = fieldbookMiddlewareService
                                .getFavoriteLocationByProjectId(locationsIds);
            List<Location> allLocations = fieldbookMiddlewareService.getAllLocations();
            result.put("success", "1");
            result.put("favoriteLocations", convertFaveLocationToJson(faveLocations));
            result.put("allLocations", convertFaveLocationToJson(allLocations));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("success", "-1");
        }
        
        return result;
    }
    
    private String convertFaveLocationToJson(List<Location> locations) {
        if (locations!= null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(locations);
            } catch(Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return "";
    }
    
    private String convertMethodsToJson(List<Method> breedingMethods) {
        if (breedingMethods!= null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(breedingMethods);
            } catch(Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return "";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String postAdvanceNursery(@ModelAttribute("advancingNurseryform") AdvancingNurseryForm form
            , BindingResult result, Model model) throws MiddlewareQueryException{
        
        advancingNursery.setNamingConvention(form.getNamingConvention());
        advancingNursery.setSuffixConvention(form.getSuffixConvention());
        advancingNursery.setMethodChoice(form.getMethodChoice());
        advancingNursery.setBreedingMethodId(form.getBreedingMethodId());
        advancingNursery.setLineChoice(form.getLineChoice());
        advancingNursery.setLineSelected(form.getLineSelected());
        advancingNursery.setHarvestDate(form.getHarvestDate());
        advancingNursery.setHarvestLocationId(form.getHarvestLocationId());
        advancingNursery.setHarvestLocationAbbreviation(form.getHarvestLocationAbbreviation());
        
        return "redirect:" + SaveAdvanceNurseryController.URL;
    }
    
}