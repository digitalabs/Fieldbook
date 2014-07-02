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
package com.efficio.fieldbook.web.ontology.controller;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PropertyTree;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.ontology.bean.OntologyUsage;
import com.efficio.fieldbook.web.ontology.form.OntologyDetailsForm;

/**
 * The Class OntologyDetailsController.
 */
@Controller
public class OntologyDetailsController extends AbstractBaseFieldbookController {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OntologyDetailsController.class);
    
    /** The Constant DETAILS_TEMPLATE. */
    public static final String DETAILS_TEMPLATE = "/OntologyBrowser/detailTab";

    /** The ontology service. */
    @Resource
    private OntologyService ontologyService;
    
    @Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    /**
     * Gets the ontology details.
     *
     * @param variableId the variable id
     * @param form the form
     * @param model the model
     * @return the ontology details
     */
    @RequestMapping(value = "/OntologyBrowser/details/{variableId}", method = RequestMethod.GET)
    public String getOntologyDetails(@PathVariable int variableId,  
            @ModelAttribute("ontologyDetailsForm") OntologyDetailsForm form, Model model) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            StandardVariable variable = ontologyService.getStandardVariable(variableId);
            if (variable != null && variable.getName() != null && !"".equals(variable.getName())) {
                resultMap.put("status", "success");
                resultMap.put("variable", variable);
                
                NumberFormat numberFormat = NumberFormat.getIntegerInstance();
                form.setProjectCount(numberFormat.format(
                                ontologyService.countProjectsByVariable(variableId)));
                form.setObservationCount(numberFormat.format(
                                ontologyService.countExperimentsByVariable(
                                        variableId, variable.getStoredIn().getId())));
                form.setVariable(variable);
                
                if (variable.getPhenotypicType() == PhenotypicType.TRIAL_DESIGN 
                		&& variable.getDataType().getId() == TermId.NUMERIC_VARIABLE.getId()) {
                	//look for possible pairs
                	List<StandardVariable> pairs = fieldbookMiddlewareService.getPossibleTreatmentPairs(variable.getId(), variable.getProperty().getId());
                	ObjectMapper objectMapper = new ObjectMapper();
                	form.setPossiblePairsJson(objectMapper.writeValueAsString(pairs));
                }
            } else {
                resultMap.put("status", "notfound");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "fail");
        }
        return super.showAjaxPage(model, DETAILS_TEMPLATE);
    }
    
    /**
     * Gets Properties, Trait Class and Standard Variables, based on a numeric filter that corresponds to the following
     * 
     * -- 1 : Management Details
     * -- 3 : Traits
     * -- 7 : Nursery Conditions
     * -- 9 : Selection Variates
     * 
     * @param mode : integer corresponding to the filter described above
     * @return String Representation of a PropertyTree Object : properties with nested Trait Class and Standard Variables
     * 
     */
    @ResponseBody
    @RequestMapping(value = "/OntologyBrowser/settings/properties", method = RequestMethod.GET)
    public List<PropertyTree> getPropertiesBySettingsGroup(@RequestParam(required=true) Integer groupId, 
    		@RequestParam(required=false) Integer classId) {
    	try {
    		
    		if(classId == null) {
    			// zero value will allow all classes
    			classId = new Integer(0);
    		}
    		
    		// create a Map - - we will select from this list to return, as the include method and scale information
    		Map<Integer, StandardVariable> svMap = new HashMap<Integer, StandardVariable>();

    		// Fetch the list of filtered Standard Variable References and extract ids (this would be better if 
    		// filtered SVs were full objects)
    		List<StandardVariableReference> stdVars = fieldbookService.filterStandardVariablesForSetting(groupId, new ArrayList<SettingDetail>());
    		List<Integer> ids = new ArrayList<Integer>();
    		for (StandardVariableReference standardVariableReference : stdVars) {
				ids.add(standardVariableReference.getId());
			}
    		
    		// Fetch filtered Standard Variables using the list of ids just created
    		// FIXME : this is doing an individual fetch for each sv under the hood
    		List<StandardVariable> standardVariables = ontologyService.getStandardVariables(ids);
    		
    		// fill the StabdardVariableMap - keyed by svId
    		for (StandardVariable standardVariable : standardVariables) {
				svMap.put(Integer.valueOf(standardVariable.getId()), standardVariable);
			}
    		
    		// property trees are designed to facade a PropertyReference, a TraitClassReference and a list of StandardVariables
    		List<PropertyTree> propertyTrees = new ArrayList<PropertyTree>();
    		
    		// fetch the Ontology Tree and navigate through. Look for Properties that have Standard Variables.
    		// Create a Property Tree, check if SVs are in the filtered list and add if so.
    		List<TraitClassReference> tree = ontologyService.getAllTraitGroupsHierarchy(true);
    		for (TraitClassReference root : tree) {
    			for (TraitClassReference traitClassReference : root.getTraitClassChildren()) {
    				// filter on the optional classId provided, or allow if zero=ALL
    				if(classId.equals(new Integer(0)) || classId.equals(traitClassReference.getId())) {
						for (PropertyReference property : traitClassReference.getProperties()) {
							if(!property.getStandardVariables().isEmpty()) {
								PropertyTree propertyTree = new PropertyTree(property);
								propertyTree.setTraitClass(traitClassReference);
								for (StandardVariableReference svRef : property.getStandardVariables()) {
									if(stdVars.contains(svRef)) {
										// if std variable is in the limited set, then add to the result
										propertyTree.getStandardVariables().add(svMap.get(svRef.getId()));
									}									
								}
								// only add to the result if we have Std Variables to return
								if(!propertyTree.getStandardVariables().isEmpty()) {
									propertyTrees.add(propertyTree);
								}
							}
						}
    				}
				}
			}
			return propertyTrees;
		} catch (MiddlewareQueryException e) {
			LOG.error("Error querying Ontology Manager for full Ontology Tree " + e.getMessage());
		}
    	return new ArrayList<PropertyTree>();
    } 
    
    /**
     * Fetches Property and associated Standard Variables by PropertyId
     * 
     * @param propertyId
     * @return List of type StandardVariable
     */
    @ResponseBody
    @RequestMapping(value = "/OntologyBrowser/variables", method = RequestMethod.GET)
    public String getStandardVariablesByProperty(@RequestParam(required=true) Integer propertyId) {
    	
    	List<StandardVariable> standardVariables;
		try {
			standardVariables = ontologyService.getStandardVariablesByProperty(propertyId);

			ObjectMapper om = new ObjectMapper();
			return om.writeValueAsString(standardVariables);
		} catch (JsonGenerationException e) {
			LOG.error("Error generating JSON for property trees " + e.getMessage());
		} catch (JsonMappingException e) {
			LOG.error("Error mapping JSON for property trees " + e.getMessage());
		} catch (IOException e) {
			LOG.error("Error writing JSON for property trees " + e.getMessage());
		} catch (MiddlewareQueryException e) {
			LOG.error("Error querying Ontology Manager for full Ontology Tree " + e.getMessage());
		}
    	 			
    	return "[]";
    } 
    
    /**
     * Fetches Standard Variable by Id
     * 
     * @param id : standard variable id
     * @return StandardVariable
     */
    @ResponseBody
    @RequestMapping(value = "/OntologyBrowser/variables/{id}", method = RequestMethod.GET)
    public String getStandardVariableById(@PathVariable int id) {
		try {
			StandardVariable standardVariable = ontologyService.getStandardVariable(id);

			ObjectMapper om = new ObjectMapper();
			return om.writeValueAsString(standardVariable);
		} catch (JsonGenerationException e) {
			LOG.error("Error generating JSON for property trees " + e.getMessage());
		} catch (JsonMappingException e) {
			LOG.error("Error mapping JSON for property trees " + e.getMessage());
		} catch (IOException e) {
			LOG.error("Error writing JSON for property trees " + e.getMessage());
		} catch (MiddlewareQueryException e) {
			LOG.error("Error querying Ontology Manager for full Ontology Tree " + e.getMessage());
		}
    	 			
    	return "[]";
    } 
      
    /**
     * Gets the usage stats for standard variables 
     *
     * @return the ontology usage details
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     */
    @SuppressWarnings("unchecked")
	@ResponseBody
    @RequestMapping(value = "/OntologyBrowser/variables/usage", method = RequestMethod.GET)
    public String getUsageBySettingsMode(@RequestParam(required=true) Integer mode, @RequestParam(required=false) Boolean flat,
    		@RequestParam(required=false) Integer maxResults) {
    	try {
    		
    		if(flat == null) flat = true;
    		
    		// Fetch the list of filtered Standard Variable References and extract ids (this would be better if 
    		// filtered SVs were full objects)
    		List<StandardVariableReference> stdVars = fieldbookService.filterStandardVariablesForSetting(mode, new ArrayList<SettingDetail>());
    		if(maxResults == null) maxResults = stdVars.size();
    		LOG.info("Filtering for " + mode + " : results : " + stdVars.size());
    		
    		List<Integer> ids = new ArrayList<Integer>();
    		for (StandardVariableReference standardVariableReference : stdVars) {
				ids.add(standardVariableReference.getId());
			}
    		// create a Map - - we will select from this list to return, as the include method and scale information
    		Map<Integer, StandardVariable> svMap = new HashMap<Integer, StandardVariable>();
    		// FIXME : this is doing an individual fetch for each sv under the hood
    		// fetch filtered Standard Variables 
    		List<StandardVariable> standardVariables = ontologyService.getStandardVariables(ids);
    		for (StandardVariable standardVariable : standardVariables) {
				svMap.put(Integer.valueOf(standardVariable.getId()), standardVariable);
			}
    		
    		// Collecting stats in a TreeMap to sort on experimental usage
    		Map<Long, List<String>> usageMap = new TreeMap<Long, List<String>>(Collections.reverseOrder());
    		List<OntologyUsage> usageList = new ArrayList<OntologyUsage>();
    		
    		List<TraitClassReference> tree = ontologyService.getAllTraitGroupsHierarchy(true);
    		for (TraitClassReference root : tree) {
    			for (TraitClassReference traitClassReference : root.getTraitClassChildren()) {
					for (PropertyReference property : traitClassReference.getProperties()) {
						if(!property.getStandardVariables().isEmpty()) {
							for (StandardVariableReference svRef : property.getStandardVariables()) {
								if(stdVars.contains(svRef)) {
									StandardVariable sv = svMap.get(svRef.getId());
									long projectCount = ontologyService.countProjectsByVariable(svRef.getId());
									long experimentCount = ontologyService.countExperimentsByVariable(sv.getId(), sv.getStoredIn().getId());
									// if std variable is in the limited set, then add to the result
									StringBuilder resultBuilder = new StringBuilder().append(svRef.getId());
									resultBuilder.append(":");
									resultBuilder.append(svRef.getName());
									resultBuilder.append(":");
									resultBuilder.append(svRef.getDescription());
									resultBuilder.append(":");
									resultBuilder.append(projectCount);
									resultBuilder.append(":");
									resultBuilder.append(experimentCount);
									OntologyUsage ou = new OntologyUsage();
									ou.setStandardVariable(sv);
									ou.setProjectCount(Long.valueOf(projectCount));
									ou.setExperimentCount(Long.valueOf(experimentCount));
									if(usageMap.get(Long.valueOf(experimentCount)) == null) {
										usageMap.put(Long.valueOf(experimentCount), new ArrayList<String>());
									}
									usageMap.get(Long.valueOf(experimentCount)).add(resultBuilder.toString());
									usageList.add(ou);
								}
								else LOG.info("Missed : " + svRef.getId() + ":" + svRef.getName() + ":" + svRef.getDescription());
							}
						}
					}					
				}
			}
    		
	    	ObjectMapper om = new ObjectMapper();
	    	if(flat) {
	    		return om.writeValueAsString(usageMap.values());
	    	}
	    	else {
	    		// FIXME : aim to avoid warning suppression
	    		Collections.sort(usageList);
	    		if(maxResults < usageList.size()) {
	    			return om.writeValueAsString(usageList.subList(0, maxResults));
	    		}
	    		return om.writeValueAsString(usageList);
	    	}
			
		} catch (JsonGenerationException e) {
			LOG.error("Error generating JSON for property trees " + e.getMessage());
		} catch (JsonMappingException e) {
			LOG.error("Error mapping JSON for property trees " + e.getMessage());
		} catch (IOException e) {
			LOG.error("Error writing JSON for property trees " + e.getMessage());
		} catch (MiddlewareQueryException e) {
			LOG.error("Error querying Ontology Manager for full Ontology Tree " + e.getMessage());
		}
    	return "[]";
    } 
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return null;
    }
    
}
