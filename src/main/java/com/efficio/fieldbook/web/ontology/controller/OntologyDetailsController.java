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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PropertyTree;

/**
 * The Class OntologyDetailsController.
 */
@Controller
public class OntologyDetailsController extends AbstractBaseFieldbookController {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(OntologyDetailsController.class);

	/** The ontology service. */
	@Resource
	private OntologyService ontologyService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private ContextUtil contextUtil;

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
					this.ontologyDataManager.getStandardVariablesForPhenotypicType(phenotype,
							this.contextUtil.getCurrentProgramUUID(), 0, Integer.MAX_VALUE);

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

		} catch (MiddlewareException e) {
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
			standardVariables = this.ontologyService.getStandardVariablesByProperty(propertyId,
					this.contextUtil.getCurrentProgramUUID());

			ObjectMapper om = new ObjectMapper();
			return om.writeValueAsString(standardVariables);
		} catch (JsonGenerationException e) {
			OntologyDetailsController.LOG.error("Error generating JSON for property trees " + e.getMessage(), e);
		} catch (JsonMappingException e) {
			OntologyDetailsController.LOG.error("Error mapping JSON for property trees " + e.getMessage(), e);
		} catch (IOException e) {
			OntologyDetailsController.LOG.error("Error writing JSON for property trees " + e.getMessage(), e);
		} catch (MiddlewareException e) {
			OntologyDetailsController.LOG.error("Error querying Ontology Manager for full Ontology Tree " + e.getMessage(), e);
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
			StandardVariable standardVariable = this.ontologyService.getStandardVariable(id,
					this.contextUtil.getCurrentProgramUUID());

			ObjectMapper om = new ObjectMapper();
			return om.writeValueAsString(standardVariable);
		} catch (JsonGenerationException e) {
			OntologyDetailsController.LOG.error("Error generating JSON for property trees " + e.getMessage(), e);
		} catch (JsonMappingException e) {
			OntologyDetailsController.LOG.error("Error mapping JSON for property trees " + e.getMessage(), e);
		} catch (IOException e) {
			OntologyDetailsController.LOG.error("Error writing JSON for property trees " + e.getMessage(), e);
		} catch (MiddlewareException e) {
			OntologyDetailsController.LOG.error("Error querying Ontology Manager for full Ontology Tree " + e.getMessage(), e);
		}

		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/OntologyBrowser/getDistinctValue/{variableId}", method = RequestMethod.GET)
	public List<ValueReference> getDistinctValues(@PathVariable int variableId) {
		try {
			return this.ontologyService.getDistinctStandardVariableValues(variableId);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
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
		List<PropertyTree> propertyTreeList = propertyTrees;
		// We might encounter a trait class node - if so, process sub trait class nodes
		if (!traitClassReference.getTraitClassChildren().isEmpty()) {
			for (TraitClassReference subTraitClass : traitClassReference.getTraitClassChildren()) {
				propertyTreeList = this.processTreeTraitClasses(classId, svMap, stdVars, propertyTreeList, subTraitClass);
			}
		}
		// and process properties of that trait class (if the class is selected, or we are defaulting to all classes
		if (!traitClassReference.getProperties().isEmpty() && (classId == 0 || classId.equals(traitClassReference.getId()))) {
			propertyTreeList = this.processTreeProperties(svMap, stdVars, propertyTreeList, traitClassReference);
		}
		return propertyTreeList;

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
