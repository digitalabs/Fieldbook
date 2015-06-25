/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.ontology.controller;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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
import com.efficio.fieldbook.web.ontology.form.OntologyDetailsForm;
import com.efficio.fieldbook.web.util.AppConstants;

/**
 * The Class OntologyDetailsController.
 */
@Controller
public class OntologyDetailsController extends AbstractBaseFieldbookController {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(OntologyDetailsController.class);

	/** The Constant DETAILS_TEMPLATE. */
	public static final String DETAILS_TEMPLATE = "/OntologyBrowser/detailTab";

	public static final String USAGE_DETAILS_TEMPLATE = "/OntologyBrowser/usageTab";

	/** The ontology service. */
	@Resource
	private OntologyService ontologyService;

	@Resource
	private OntologyDataManager ontologyDataManager;

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
	public String getOntologyDetails(@PathVariable int variableId, @ModelAttribute("ontologyDetailsForm") OntologyDetailsForm form,
			Model model) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			StandardVariable variable = this.ontologyService.getStandardVariable(variableId);
			if (variable != null && variable.getName() != null && !"".equals(variable.getName())) {
				resultMap.put("status", "success");
				resultMap.put("variable", variable);

				form.setVariable(variable);

				if (variable.getPhenotypicType() == PhenotypicType.TRIAL_DESIGN
						&& variable.getDataType().getId() == TermId.NUMERIC_VARIABLE.getId()) {
					// look for possible pairs
					List<StandardVariable> pairs =
							this.fieldbookMiddlewareService.getPossibleTreatmentPairs(variable.getId(), variable.getProperty().getId(),
									AppConstants.CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());
					ObjectMapper objectMapper = new ObjectMapper();
					form.setPossiblePairsJson(objectMapper.writeValueAsString(pairs));
				}
			} else {
				resultMap.put("status", "notfound");
			}
		} catch (Exception e) {
			OntologyDetailsController.LOG.error(e.getMessage(), e);
			resultMap.put("status", "fail");
		}
		return super.showAjaxPage(model, OntologyDetailsController.DETAILS_TEMPLATE);
	}

	@RequestMapping(value = "/OntologyBrowser/details/usage/{variableId}", method = RequestMethod.GET)
	public String getOntologyUsageDetails(@PathVariable int variableId, @ModelAttribute("ontologyDetailsForm") OntologyDetailsForm form,
			Model model) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			StandardVariable variable = this.ontologyService.getStandardVariable(variableId);
			if (variable != null && variable.getName() != null && !"".equals(variable.getName())) {
				resultMap.put("status", "success");
				resultMap.put("variable", variable);

				NumberFormat numberFormat = NumberFormat.getIntegerInstance();

				form.setProjectCount(numberFormat.format(this.ontologyService.countProjectsByVariable(variableId)));
				form.setObservationCount(numberFormat.format(this.ontologyService.countExperimentsByVariable(variableId, variable
						.getStoredIn().getId())));

				form.setVariable(variable);
			} else {
				resultMap.put("status", "notfound");
			}
		} catch (Exception e) {
			OntologyDetailsController.LOG.error(e.getMessage(), e);
			resultMap.put("status", "fail");
		}
		return super.showAjaxPage(model, OntologyDetailsController.USAGE_DETAILS_TEMPLATE);
	}

	/**
	 * Gets Properties, Trait Class and Standard Variables, based on a numeric filter that corresponds to the following
	 * 
	 * -- 1 : Management Details -- 2: Factors -- 3 : Traits -- 6 : Selection Variates -- 7 : Nursery Conditions
	 * 
	 * @param mode : integer corresponding to the filter described above
	 * @return String Representation of a PropertyTree Object : properties with nested Trait Class and Standard Variables
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/OntologyBrowser/settings/properties", method = RequestMethod.GET)
	public List<PropertyTree> getPropertiesBySettingsGroup(@RequestParam(required = true) Integer groupId,
			@RequestParam(required = false) Integer classId, @RequestParam(required = false) boolean useTrialFiltering) {
		try {

			if (classId == null) {
				// zero value will allow all classes to be fetched
				classId = new Integer(0);
			}

			// create a Map - - we will select from this list to return, as the include method and scale information
			Map<Integer, StandardVariableSummary> svMap = new HashMap<Integer, StandardVariableSummary>();

			// Fetch the list of filtered Standard Variable References and extract ids (this would be better if
			// filtered SVs were full objects)
			List<StandardVariableReference> stdVars;
			if (useTrialFiltering) {
				stdVars = this.fieldbookService.filterStandardVariablesForTrialSetting(groupId, new ArrayList<SettingDetail>());
			} else {
				stdVars = this.fieldbookService.filterStandardVariablesForSetting(groupId, new ArrayList<SettingDetail>());
			}

			List<Integer> ids = new ArrayList<Integer>();
			for (StandardVariableReference standardVariableReference : stdVars) {
				ids.add(standardVariableReference.getId());
			}

			// Fetch filtered Standard Variables using the list of ids just created
			List<StandardVariableSummary> standardVariables = this.ontologyService.getStandardVariableSummaries(ids);

			// fill the StandardVariableMap - keyed by svId
			for (StandardVariableSummary standardVariable : standardVariables) {
				svMap.put(Integer.valueOf(standardVariable.getId()), standardVariable);
				for (StandardVariableReference ref : stdVars) {
					if (ref.getId().equals(standardVariable.getId())) {
						standardVariable.setHasPair(ref.isHasPair());
						break;
					}
				}
			}

			// property trees are designed to facade a PropertyReference, a TraitClassReference and a list of StandardVariables
			List<PropertyTree> propertyTrees = new ArrayList<PropertyTree>();

			// fetch the Ontology Tree and navigate through. Look for Properties that have Standard Variables.
			// Create a Property Tree, check if SVs are in the filtered list and add if so.
			List<TraitClassReference> tree = this.ontologyService.getAllTraitGroupsHierarchy(true);
			for (TraitClassReference root : tree) {
				propertyTrees = this.processTreeTraitClasses(classId, svMap, stdVars, propertyTrees, root);
			}
			return propertyTrees;
		} catch (MiddlewareQueryException e) {
			OntologyDetailsController.LOG.error("Error querying Ontology Manager for full Ontology Tree " + e.getMessage());
		}
		return new ArrayList<PropertyTree>();
	}

	@ResponseBody
	@RequestMapping(value = "/OntologyBrowser/getVariablesByPhenotype", method = RequestMethod.GET,
			produces = "application/json; charset=utf-8")
	public List<PropertyTree> getStandardVariablesByPhenotype(@RequestParam("phenotypeStorageId") Integer termId) {
		try {
			PhenotypicType phenotype = PhenotypicType.getPhenotypicTypeById(termId);

			if (phenotype == null) {
				return new ArrayList<>();
			}

			Map<String, StandardVariable> standardVariableMap =
					this.ontologyDataManager.getStandardVariablesForPhenotypicType(phenotype, 0, Integer.MAX_VALUE);

			// create a Map - - we will select from this list to return, as the include method and scale information
			Map<Integer, StandardVariableSummary> svMap = new HashMap<>();
			List<StandardVariableReference> stdVars = new ArrayList<>();
			List<Integer> ids = new ArrayList<>();

			// transform standardVariableMap into standardVariableReference
			for (Map.Entry<String, StandardVariable> standardVariable : standardVariableMap.entrySet()) {
				ids.add(standardVariable.getValue().getId());
				stdVars.add(new StandardVariableReference(standardVariable.getValue().getId(), standardVariable.getKey(), standardVariable
						.getValue().getDescription()));
			}

			// Fetch filtered Standard Variables using the list of ids just created
			List<StandardVariableSummary> standardVariables = this.ontologyService.getStandardVariableSummaries(ids);

			// fill the StandardVariableMap - keyed by svId
			for (StandardVariableSummary standardVariable : standardVariables) {
				svMap.put(standardVariable.getId(), standardVariable);
				for (StandardVariableReference ref : stdVars) {
					if (ref.getId().equals(standardVariable.getId())) {
						standardVariable.setHasPair(ref.isHasPair());
						break;
					}
				}
			}

			// property trees are designed to facade a PropertyReference, a TraitClassReference and a list of StandardVariables
			List<PropertyTree> propertyTrees = new ArrayList<PropertyTree>();

			// fetch the Ontology Tree and navigate through. Look for Properties that have Standard Variables.
			// Create a Property Tree, check if SVs are in the filtered list and add if so.
			List<TraitClassReference> tree = this.ontologyService.getAllTraitGroupsHierarchy(true);
			for (TraitClassReference root : tree) {
				propertyTrees = this.processTreeTraitClasses(0, svMap, stdVars, propertyTrees, root);
			}
			return propertyTrees;

		} catch (MiddlewareQueryException e) {
			OntologyDetailsController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
	}

	/**
	 * Fetches Property and associated Standard Variables by PropertyId
	 * 
	 * @param propertyId
	 * @return List of type StandardVariable
	 */
	@ResponseBody
	@RequestMapping(value = "/OntologyBrowser/variables", method = RequestMethod.GET)
	public String getStandardVariablesByProperty(@RequestParam(required = true) Integer propertyId) {

		List<StandardVariable> standardVariables;
		try {
			standardVariables = this.ontologyService.getStandardVariablesByProperty(propertyId);

			ObjectMapper om = new ObjectMapper();
			return om.writeValueAsString(standardVariables);
		} catch (JsonGenerationException e) {
			OntologyDetailsController.LOG.error("Error generating JSON for property trees " + e.getMessage());
		} catch (JsonMappingException e) {
			OntologyDetailsController.LOG.error("Error mapping JSON for property trees " + e.getMessage());
		} catch (IOException e) {
			OntologyDetailsController.LOG.error("Error writing JSON for property trees " + e.getMessage());
		} catch (MiddlewareQueryException e) {
			OntologyDetailsController.LOG.error("Error querying Ontology Manager for full Ontology Tree " + e.getMessage());
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
			StandardVariable standardVariable = this.ontologyService.getStandardVariable(id);

			ObjectMapper om = new ObjectMapper();
			return om.writeValueAsString(standardVariable);
		} catch (JsonGenerationException e) {
			OntologyDetailsController.LOG.error("Error generating JSON for property trees " + e.getMessage());
		} catch (JsonMappingException e) {
			OntologyDetailsController.LOG.error("Error mapping JSON for property trees " + e.getMessage());
		} catch (IOException e) {
			OntologyDetailsController.LOG.error("Error writing JSON for property trees " + e.getMessage());
		} catch (MiddlewareQueryException e) {
			OntologyDetailsController.LOG.error("Error querying Ontology Manager for full Ontology Tree " + e.getMessage());
		}

		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/OntologyBrowser/getDistinctValue/{variableId}", method = RequestMethod.GET)
	public List<ValueReference> getDistinctValues(@PathVariable int variableId) {
		try {
			return this.ontologyService.getDistinctStandardVariableValues(variableId);
		} catch (MiddlewareQueryException e) {
			return new ArrayList<>();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return null;
	}

	/**
	 * Recursive kick-off spot for Ontological tree processing. Recursive node is the TraitClassNode. If Trait Classes and Properties for a
	 * TraitClass node are exhausted, then the algorithm will return with collected Properties.
	 * 
	 * @param classId
	 * 
	 * @param svMap : standard variable map. Only return standard variables from this map
	 * @param stdVars : standard variables to process
	 * @param propertyTrees : collection units for results
	 * @param traitClassReference : the class node that contains either further subClasses, and/or Properties
	 * @return
	 */
	private List<PropertyTree> processTreeTraitClasses(Integer classId, Map<Integer, StandardVariableSummary> svMap,
			List<StandardVariableReference> stdVars, List<PropertyTree> propertyTrees, TraitClassReference traitClassReference) {

		// We might encounter a trait class node - if so, process sub trait class nodes
		if (!traitClassReference.getTraitClassChildren().isEmpty()) {
			for (TraitClassReference subTraitClass : traitClassReference.getTraitClassChildren()) {
				propertyTrees = this.processTreeTraitClasses(classId, svMap, stdVars, propertyTrees, subTraitClass);
			}
		}
		// and process properties of that trait class (if the class is selected, or we are defaulting to all classes
		if (!traitClassReference.getProperties().isEmpty() && (classId == 0 || classId.equals(traitClassReference.getId()))) {
			propertyTrees = this.processTreeProperties(svMap, stdVars, propertyTrees, traitClassReference);
		}
		return propertyTrees;

	}

	/**
	 * A method that takes a Trait Class parent in the tree, and processes the list of property nodes from the Trait Class parent in the
	 * tree and extracts the Standard Variables
	 * 
	 * @param svMap
	 * @param stdVars
	 * @param propertyTrees
	 * @param traitClassReference
	 * @return the Property Trees list that collects the new items
	 * 
	 */
	private List<PropertyTree> processTreeProperties(Map<Integer, StandardVariableSummary> svMap, List<StandardVariableReference> stdVars,
			List<PropertyTree> propertyTrees, TraitClassReference traitClassReference) {
		for (PropertyReference property : traitClassReference.getProperties()) {
			if (!property.getStandardVariables().isEmpty()) {
				PropertyTree propertyTree = new PropertyTree(property);
				propertyTree.setTraitClass(traitClassReference);
				for (StandardVariableReference svRef : property.getStandardVariables()) {
					if (stdVars.contains(svRef)) {
						// if std variable is in the limited set, then add to the result
						if (svMap.get(svRef.getId()) != null) {
							propertyTree.getStandardVariables().add(svMap.get(svRef.getId()));
						}
					}
				}
				// only add to the result if we have Std Variables to return
				if (!propertyTree.getStandardVariables().isEmpty()) {
					propertyTrees.add(propertyTree);
				}
			}
		}
		return propertyTrees;
	}

	public void setOntologyService(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

}
