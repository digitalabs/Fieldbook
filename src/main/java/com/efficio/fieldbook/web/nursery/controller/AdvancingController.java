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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;

/**
 * The Class AddOrRemoveTraitsController.
 */
@Controller
@RequestMapping(AdvancingController.URL)
public class AdvancingController extends AbstractBaseFieldbookController{

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/advance/nursery";
    
    private static final String MODAL_URL = "NurseryManager/advanceNurseryModal";
    private static final String SAVE_ADVANCE_NURSERY_PAGE_TEMPLATE = "NurseryManager/saveAdvanceNursery";
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AdvancingController.class);
    
    /** The user selection. */
    @Resource
    private AdvancingNursery advancingNursery;
    
    /** The fieldbook middleware service. */
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    /** The workbench data manager. */
    @Resource
    private WorkbenchService workbenchService;
    
    @Resource
    private UserSelection userSelection;
    
    @Resource
    private OntologyService ontologyService;
    
    @Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
    
    @Resource
    private GermplasmDataManager germplasmDataManager;
    
    @Resource
    private ResourceBundleMessageSource messageSource;
    
    /** The imported germplasm list. */
    private List<ImportedGermplasm> importedGermplasmList;
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/advancingNursery";
    }
    
   
    /**
     * Shows the screen.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @param nurseryId the nursery id
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/{nurseryId}", method = RequestMethod.GET)
    public String show(@ModelAttribute("advancingNurseryform") AdvancingNurseryForm form
            , Model model, HttpServletRequest req, HttpSession session, @PathVariable int nurseryId) throws MiddlewareQueryException{
    	form.setMethodChoice("1");
    	form.setLineChoice("1");
    	form.setLineSelected("1");
    	form.setAllPlotsChoice("1");
    	form.setProjectId(this.getCurrentProjectId());
    	Study study = fieldbookMiddlewareService.getStudy(nurseryId);
    	List<Variable> varList = study.getConditions().getVariables();
    	form.setDefaultMethodId(Integer.toString(AppConstants.SINGLE_PLANT_SELECTION_SF.getInt()));
    	String defaultId = getBreedingMethodIdFromStudy(varList);
    	if (defaultId != null) {
    		form.setDefaultMethodId(defaultId);
    	}
    	advancingNursery.setStudy(study);
    	form.setLocationUrl(fieldbookProperties.getProgramLocationsUrl());
    	form.setBreedingMethodUrl(fieldbookProperties.getProgramBreedintMethodsUrl());
    	form.setNurseryId(Integer.toString(nurseryId));
    	Project project = workbenchService.getProjectById(Long.valueOf(this.getCurrentProjectId()));
    	if(AppConstants.CROP_MAIZE.getString().equalsIgnoreCase(project.getCropType().getCropName())){
    		form.setCropType(2);
    	}else if(AppConstants.CROP_WHEAT.getString().equalsIgnoreCase(project.getCropType().getCropName())){
    		form.setCropType(1);
    	}
    	
        form.setMethodVariates(filterVariablesByProperty(userSelection.getSelectionVariates(), AppConstants.PROPERTY_BREEDING_METHOD.getString()));
        form.setLineVariates(filterVariablesByProperty(userSelection.getSelectionVariates(), AppConstants.PROPERTY_PLANTS_SELECTED.getString()));
        form.setPlotVariates(form.getLineVariates());
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
    	SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
    	String currentYear = sdf.format(new Date());
    	form.setHarvestYear(currentYear);
    	form.setHarvestMonth(sdfMonth.format(new Date()));
    	
    	model.addAttribute("yearChoices", DateUtil.generateYearChoices(Integer.parseInt(currentYear)));
    	model.addAttribute("monthChoices", DateUtil.generateMonthChoices());
    	
    	return super.showAjaxPage(model, MODAL_URL);
    }
    
    /**
     * Gets the breeding methods.
     *
     * @return the breeding methods
     */
    @ResponseBody
    @RequestMapping(value="/getBreedingMethods", method = RequestMethod.GET)
    public Map<String, String> getBreedingMethods() {
        Map<String, String> result = new HashMap<String, String>();
        
        try {
			List<Method> breedingMethods = fieldbookMiddlewareService.getAllBreedingMethods(false);
			Project project = new Project();
			project.setProjectId(Long.valueOf(this.getCurrentProjectId()));
			
			List<Integer> methodIds = workbenchService
			              .getFavoriteProjectMethods(getCurrentProjectId());
			List<Method> favoriteMethods = fieldbookMiddlewareService.getFavoriteBreedingMethods(methodIds, false);						
			List<Method> allNonGenerativeMethods = fieldbookMiddlewareService.getAllBreedingMethods(true);
			//List<Method> favoriteNonGenerativeMethods = fieldbookMiddlewareService.getFavoriteBreedingMethods(methodIds, true);
                                   
            result.put("success", "1");
            result.put("allMethods", convertMethodsToJson(breedingMethods));
            result.put("favoriteMethods", convertMethodsToJson(favoriteMethods));            
            result.put("allNonGenerativeMethods", convertMethodsToJson(allNonGenerativeMethods));
            result.put("favoriteNonGenerativeMethods", convertMethodsToJson(favoriteMethods));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("success", "-1");
            result.put("errorMessage", e.getMessage());
        }
        
        return result;
    }
      
    
    /**
     * Gets the locations.
     *
     * @return the locations
     */
    @ResponseBody
    @RequestMapping(value="/getLocations", method = RequestMethod.GET)
    public Map<String, String> getLocations() {
        Map<String, String> result = new HashMap<String, String>();
        
        try {
            List<Long> locationsIds = workbenchService
                                .getFavoriteProjectLocationIds(getCurrentProjectId());
            List<Location> faveLocations = fieldbookMiddlewareService
                                .getFavoriteLocationByProjectId(locationsIds);
            List<Location> allBreedingLocations = fieldbookMiddlewareService.getAllBreedingLocations();
            List<Location> allSeedStorageLocations = fieldbookMiddlewareService.getAllSeedLocations();
            result.put("success", "1");
            result.put("favoriteLocations", convertFaveLocationToJson(faveLocations));
            result.put("allBreedingLocations", convertFaveLocationToJson(allBreedingLocations));
            result.put("allSeedStorageLocations", convertFaveLocationToJson(allSeedStorageLocations));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("success", "-1");
        }
        
        return result;
    }
    
    /**
     * Convert favorite location to json.
     *
     * @param locations the locations
     * @return the string
     */
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
    
    /**
     * Convert methods to json.
     *
     * @param breedingMethods the breeding methods
     * @return the string
     */
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
    
    /**
     * Post advance nursery.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(method = RequestMethod.POST)
    public String postAdvanceNursery(@ModelAttribute("advancingNurseryform") AdvancingNurseryForm form
            , BindingResult result, Model model) throws MiddlewareQueryException{
        
        advancingNursery.setMethodChoice(form.getMethodChoice());
        advancingNursery.setBreedingMethodId(form.getAdvanceBreedingMethodId());
        advancingNursery.setLineChoice(form.getLineChoice());
        advancingNursery.setLineSelected(form.getLineSelected() != null ? form.getLineSelected().trim() : null);
        advancingNursery.setHarvestDate(form.getHarvestDate());
        advancingNursery.setHarvestLocationId(form.getHarvestLocationId());
        advancingNursery.setHarvestLocationAbbreviation(form.getHarvestLocationAbbreviation() != null ? form.getHarvestLocationAbbreviation() : "");
        advancingNursery.setAllPlotsChoice(form.getAllPlotsChoice());
        advancingNursery.setLineVariateId(form.getLineVariateId());
        advancingNursery.setPlotVariateId(form.getPlotVariateId());
        advancingNursery.setMethodVariateId(form.getMethodVariateId());
        
        try {
        	importedGermplasmList = fieldbookService.advanceNursery(advancingNursery, userSelection.getWorkbook());
            userSelection.setImportedAdvancedGermplasmList(importedGermplasmList);
            form.setGermplasmList(importedGermplasmList);
            form.setEntries(importedGermplasmList.size());
            form.changePage(1);
            long id = (new Date()).getTime();
            getPaginationListSelection().addAdvanceDetails(Long.toString(id), form);
            form.setUniqueId(id);
        } catch (MiddlewareQueryException e) {
        	form.setErrorInAdvance(e.getMessage());
        	form.setGermplasmList(new ArrayList<ImportedGermplasm>());
        	form.setEntries(0);
        }
    	return super.showAjaxPage(model, SAVE_ADVANCE_NURSERY_PAGE_TEMPLATE);
    }
    
    private List<StandardVariableReference> filterVariablesByProperty(List<SettingDetail> variables, String propertyName) {
        List<StandardVariableReference> list = new ArrayList<StandardVariableReference>();
        if (variables != null && !variables.isEmpty()) {
            for (SettingDetail detail : variables) {
                if (detail.getVariable() != null && detail.getVariable().getProperty() != null 
                        && propertyName.equalsIgnoreCase(detail.getVariable().getProperty())) {
                    list.add(new StandardVariableReference(detail.getVariable().getCvTermId(), detail.getVariable().getName(), detail.getVariable().getDescription()));
                }
            }
        }
        return list;
    }
    
    @ResponseBody
    @RequestMapping(value="/countPlots/{ids}", method=RequestMethod.GET)
    public int countPlots(@PathVariable String ids) throws MiddlewareQueryException {
    	String[] idList = ids.split(",");
    	List<Integer> idParams = new ArrayList<Integer>();
    	for (String id : idList) {
    		if (id != null && NumberUtils.isNumber(id)) {
    			idParams.add(Double.valueOf(id).intValue());
    		}
    	}
   		return fieldbookMiddlewareService.countPlotsWithRecordedVariatesInDataset(userSelection.getWorkbook().getMeasurementDatesetId(), idParams);
    		
    }
    
    @ResponseBody
    @RequestMapping(value="/checkMethodTypeMode/{methodVariateId}", method=RequestMethod.GET)
    public String checkMethodTypeMode(@PathVariable int methodVariateId) throws MiddlewareQueryException {
    	List<MeasurementRow> observations = userSelection.getWorkbook().getObservations();
    	if (observations != null && !observations.isEmpty()) {
    		Set<Integer> methodIds = new HashSet<Integer>();
    		for (MeasurementRow row : observations) {
    			String value = row.getMeasurementDataValue(methodVariateId);
    			if (value != null && NumberUtils.isNumber(value)) {
    				methodIds.add(Double.valueOf(value).intValue());
    			}
    		}
    		if (!methodIds.isEmpty()) {
    			List<Method> methods = germplasmDataManager.getMethodsByIDs(new ArrayList<Integer>(methodIds));
    			boolean isBulk = false;
    			boolean isLine = false;
    			for (Method method : methods) {
    				if (method.isBulkingMethod() != null && method.isBulkingMethod()) {
    					isBulk = true;
    				} else {
    					isLine = true;
    				}
    				if (isBulk && isLine) {
    					return "MIXED";
    				}
    			}
    			if (isBulk) {
    				return "BULK";
    			} else {
    				return "LINE";
    			}
    		}
    	}
    	Locale locale = LocaleContextHolder.getLocale();
    	return messageSource.getMessage("nursery.advance.nursery.empty.method.error", null, locale);
    }
    
    private String getBreedingMethodIdFromStudy(List<Variable> varList) throws MiddlewareQueryException {
    	String code = null;
    	String name = null;
    	for(Variable var : varList){
    		if (var.getValue() != null && !var.getValue().equalsIgnoreCase("") && !var.getValue().equalsIgnoreCase("0")) {
	    		if (var.getVariableType().getStandardVariable().getId() == TermId.BREEDING_METHOD_ID.getId()){    			
	    			return var.getValue();
	    		}
	    		else if (var.getVariableType().getStandardVariable().getId() == TermId.BREEDING_METHOD_CODE.getId()) {
	    			code = var.getValue();
	    		}
	    		else if (var.getVariableType().getStandardVariable().getId() == TermId.BREEDING_METHOD.getId()) {
	    			name = var.getValue();
	    		}
    		}
    	}
    	Method method = null;
    	if (code != null) {
    		method = fieldbookMiddlewareService.getMethodByCode(code);
    	}
    	if (method == null && name != null) {
    		method = fieldbookMiddlewareService.getMethodByName(name);
    	}
    	
    	if (method != null) {
    		return String.valueOf(method.getMid());
    	}
    	return null;
    }
}