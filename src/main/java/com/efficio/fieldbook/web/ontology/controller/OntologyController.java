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

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.TraitReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.ontology.form.OntologyBrowserForm;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import org.generationcp.middleware.service.api.OntologyService;


// TODO: Auto-generated Javadoc
/**
 * This controller handles the ontology screen.
 * 
 * @author Daniel Jao
 */
@Controller
@RequestMapping(OntologyController.URL)
public class OntologyController extends AbstractBaseFieldbookController{
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OntologyController.class);
    
    /** The Constant URL. */
    public static final String URL = "/OntologyBrowser/";
    
    /** The ontology service. */
    @Resource
    private OntologyService ontologyService;
    
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "OntologyBrowser/main";
    }
    
   
   
    /**
     * Show the main import page.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form, Model model) {
        //this set the necessary info from the session variable
        //OntologyDataManager.getTraitGroups()
        try {
            List<TraitReference> traitRefList = (List<TraitReference>) ontologyService.getTraitGroups();//getDummyData();
            form.setTraitReferenceList(traitRefList);
            form.setTreeData(TreeViewUtil.convertOntologyTraitsToJson(traitRefList));
            form.setSearchTreeData(TreeViewUtil.convertOntologyTraitsToSearchSingleLevelJson(traitRefList));
            form.setDataTypes(ontologyService.getAllDataTypes());
            form.setRoles(ontologyService.getAllRoles());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	return super.show(model);
    }
    
    
    /**
     * Save new variable.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String saveNewVariable(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form, BindingResult result, Model model) {
        return show(form, model);
    }
  
    /**
     * Gets the trait class suggestions.
     *
     * @return the trait class suggestions
     */
    @ModelAttribute("traitClassesSuggestionList")
    public List<TraitReference> getTraitClassSuggestions() {
        try {
            List<TraitReference> traitClass = ontologyService.getAllTraitClasses();
            return traitClass;
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * Gets the property suggestions.
     *
     * @return the property suggestions
     */
    @ModelAttribute("propertiesSuggestionList")
    public List<Property> getPropertySuggestions() {
        try {
            List<Property> properties = ontologyService.getAllProperties();
            return properties;
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * Gets the method suggestions.
     *
     * @return the method suggestions
     */
    @ModelAttribute("methodsSuggestionList")
    public List<Method> getMethodSuggestions() {
        try {
            List<Method> methods = ontologyService.getAllMethods();
            return methods;
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * Gets the scale suggestions.
     *
     * @return the scale suggestions
     */
    @ModelAttribute("scalesSuggestionList")
    public List<Scale> getScaleSuggestions() {
        try {
            List<Scale> scales = ontologyService.getAllScales();
            return scales;
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        }

        return null;
    }
}
