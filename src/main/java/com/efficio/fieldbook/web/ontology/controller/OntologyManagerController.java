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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
//import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.ontology.form.OntologyBrowserForm;
import com.efficio.fieldbook.web.ontology.form.OntologyMethodForm;
import com.efficio.fieldbook.web.ontology.form.OntologyScaleForm;
import com.efficio.fieldbook.web.ontology.validation.OntologyBrowserValidator;
import com.efficio.fieldbook.web.util.TreeViewUtil;


// TODO: Auto-generated Javadoc
/**
 * This controller handles the ontology screen.
 * 
 * @author Daniel Jao
 */
@Controller
@RequestMapping(OntologyManagerController.URL)
public class OntologyManagerController extends AbstractBaseFieldbookController{
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OntologyManagerController.class);
    
    /** The Constant URL. */
    public static final String URL = "/OntologyManager/manage/";
    /** AJAX PAGES **/
    public static final String TRAIT_CLASS_MODAL = "/OntologyBrowser/manage/traitClass";
    public static final String PROPERTY_MODAL = "/OntologyBrowser/manage/property";
    public static final String SCALE_MODAL = "/OntologyBrowser/manage/scale";
    public static final String METHOD_MODAL = "/OntologyBrowser/manage/method";
    
    /** The ontology service. */
    @Resource
    private OntologyService ontologyService;
    
    /** The message source. */
    @Autowired
    public MessageSource messageSource;
    
    @Resource
    private ErrorHandlerService errorHandlerService;
    
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "OntologyBrowser/manage/variable";
    }
    
    
    /**
     * Show the main import page.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="traitClass", method = RequestMethod.GET)
    public String showTraitClass(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form, Model model) {
        
        try {
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return super.showAjaxPage(model, TRAIT_CLASS_MODAL);
    }
    
    /**
     * Show the main import page.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="property", method = RequestMethod.GET)
    public String showProperty(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form, Model model) {
        
        try {
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return super.showAjaxPage(model, PROPERTY_MODAL);
    }
    
    /**
     * Show the main import page.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="scale", method = RequestMethod.GET)
    public String showScale(@ModelAttribute("ontologyScaleForm") OntologyScaleForm form, Model model) {
        
        try {
            
            List<String> variableListForScales = new ArrayList<String>();
            variableListForScales.add("Sample Variable 1");
            variableListForScales.add("Sample Variable 2");
            variableListForScales.add("Sample Variable 3");
            form.setVariablesLinkedToScale(variableListForScales);
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return super.showAjaxPage(model, SCALE_MODAL);
    }
    
    /**
     * Show the main import page.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="method", method = RequestMethod.GET)
    public String showMethod(@ModelAttribute("ontologyMethodForm") OntologyMethodForm form, Model model) {
        
        try {
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return super.showAjaxPage(model, METHOD_MODAL);
    }
    /**
     * Show the main import page.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="variable", method = RequestMethod.GET)
    public String show(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form, Model model) {
        
        try {
            List<TraitClassReference> traitRefList = (List<TraitClassReference>) ontologyService.getAllTraitGroupsHierarchy(false);
            List<TraitClassReference> traitClass = getAllTraitClassesFromHierarchy(traitRefList);
            model.addAttribute("traitClassTreeData", TreeViewUtil.convertOntologyTraitsToJson(traitRefList));
            model.addAttribute("traitClassesSuggestionList", traitClass);
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
    @RequestMapping(value="variable", method = RequestMethod.POST)
    public String saveNewVariable(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form, BindingResult result, Model model) {
        OntologyBrowserValidator validator = new OntologyBrowserValidator();
        validator.validate(form, result);
        form.setAddSuccessful("0");
        
        if (result.hasErrors()) {
            /**
             * Return the user back to form to show errors
             */
            form.setHasError("1");
            return show(form,model);
        } else {
            try {
                Operation operation = form.getVariableId() != null ? Operation.UPDATE : Operation.ADD;
                
                StandardVariable standardVariable = createStandardVariableObject(form);
                ontologyService.saveOrUpdateStandardVariable(standardVariable, operation);
                form.setAddSuccessful("1");
                
           } catch (Exception e) {
               LOG.error(e.getMessage(), e);
               form.setAddSuccessful("2");
               form.setErrorMessage(errorHandlerService.getErrorMessagesAsString(e.getMessage(), "<br/>"));
           }
        }
        return show(form, model);
    }
    
    private StandardVariable createStandardVariableObject(OntologyBrowserForm form) throws MiddlewareQueryException {
        StandardVariable standardVariable = new StandardVariable();

        if (form.getVariableId() != null) {
            standardVariable.setId(form.getVariableId());
        }
        standardVariable.setName(form.getVariableName());
        standardVariable.setDescription(form.getVariableDescription());
        standardVariable.setProperty(ontologyService.getTermById(Integer.parseInt(form.getProperty())));
        standardVariable.setMethod(ontologyService.getTermById(Integer.parseInt(form.getMethod())));
        standardVariable.setScale(ontologyService.getTermById(Integer.parseInt(form.getScale())));
        standardVariable.setDataType(ontologyService.getTermById(Integer.parseInt(form.getDataType())));
        
        if (form.getVariableId() == null) {
            standardVariable.setPhenotypicType(ontologyService.getPhenotypicTypeById(Integer.parseInt(form.getRole())));
            standardVariable.setStoredIn(ontologyService.getTermById(Integer.parseInt(form.getRole())));
        }
        standardVariable.setIsA(ontologyService.getTermById(Integer.parseInt(form.getTraitClass())));
        standardVariable.setCropOntologyId(form.getCropOntologyId());
        
        return standardVariable;
    }
    
    
    /**
     * Checks if is integer.
     *
     * @param s the s
     * @return true, if is integer
     */
    public static boolean isInteger(String s) {
        try { 
            Integer.parseInt(s); 
        } catch(NumberFormatException e) { 
            return false; 
        }
        // only got here if we didn't return false
        return true;
    }
    
    /**
     * Save new term.
     *
     * @param combo the combo
     * @param traitClass the trait class
     * @param traitClassDescription the trait class description
     * @param property the property
     * @param propertyDescription the property description
     * @param method the method
     * @param methodDescription the method description
     * @param scale the scale
     * @param scaleDescription the scale description
     * @param local the local
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value="addVariable/{combo}", method=RequestMethod.POST)
    public Map<String, String> saveNewTerm(@PathVariable String combo,
            @RequestParam String traitClass, @RequestParam String traitClassDescription,
            @RequestParam String property, @RequestParam String propertyDescription, 
            @RequestParam String method, @RequestParam String methodDescription, 
            @RequestParam String scale, @RequestParam String scaleDescription, Locale local) {
        Map<String, String> resultMap = new HashMap<String, String>();
        
        try {
            Term term = null;
            Term traitClassTerm = null;
            Integer traitClassId = null;
            String ontologyName = "";
            //add new data, use name for description if description was left blank
            ontologyName = combo;
            resultMap.put("addedNewTrait", "0");
            if (combo.equals("Property")) {
                if (propertyDescription == null || propertyDescription.equals("")) {
                    propertyDescription = property;
                }
                if(isInteger(traitClass) == false){
                    //meaning we need to save the trait class
                    traitClassTerm = ontologyService
                            .addTraitClass(traitClass, traitClassDescription, TermId.ONTOLOGY_TRAIT_CLASS.getId()).getTerm();
                    
                    resultMap.put("traitId", String.valueOf(traitClassTerm.getId()));
                    resultMap.put("traitName", traitClassTerm.getName());
                    resultMap.put("traitDefinition", traitClassTerm.getDefinition());
                    resultMap.put("addedNewTrait", "1");
                    traitClassId = Integer.valueOf(traitClassTerm.getId());
                }else{
                    traitClassId = Integer.parseInt(traitClass);
                }
                term = ontologyService.addProperty(property, propertyDescription, traitClassId).getTerm();
                
            } else if (combo.equals("Method")) {
                if (methodDescription == null || methodDescription.equals("")) {
                    methodDescription = method;
                }
                term = ontologyService.addMethod(method, methodDescription).getTerm();
            } else if (combo.equals("Scale")) {
                if (scaleDescription == null || scaleDescription.equals("")) {
                    scaleDescription = scale;
                }
                term = ontologyService.addScale(scale, scaleDescription).getTerm();
            } else {
                if (traitClassDescription == null || traitClassDescription.equals("")) {
                    traitClassDescription = traitClass;
                }
                ontologyName = "Trait Class";
                term = ontologyService.addTraitClass(traitClass, traitClassDescription, TermId.ONTOLOGY_TRAIT_CLASS.getId()).getTerm();
            }          
              
            resultMap.put("status", "1");
            resultMap.put("id", String.valueOf(term.getId()));
            resultMap.put("name", term.getName());
            resultMap.put("definition", term.getDefinition());
            Object[] args = new Object[2];
            args[0] = ontologyName;
            args[1] = term.getName();
            resultMap.put("successMessage", messageSource.getMessage("ontology.browser.modal.variable.name.save.success", args, local));
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());
        }
        return resultMap;
    }
    
    /*
     * Gets the variable name.
     *
     * @return the variable name
    */ 
    @ModelAttribute("variableNameSuggestionList")
    public List<Term> getVariableName() {
        try {
            List<Term> variables = ontologyService.getAllTermsByCvId(CvId.VARIABLES);
            return variables;
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
    
    
    /**
     * Gets the data types.
     *
     * @return the data types
     */
    @ModelAttribute("dataTypes")
    public List<Term> getDataTypes() {
        try {
            List<Term> dataTypes = ontologyService.getAllDataTypes();
            
            Collections.sort(dataTypes, new  Comparator<Term>() {
                @Override
                public int compare(Term o1, Term o2) {
                        return o1.getName().compareTo(o2.getName());
                }
            }
            );
            return dataTypes;
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
    
    
    /**
     * Gets the roles.
     *
     * @return the roles
     */
    @ModelAttribute("roles")
    public List<Term> getRoles() {
        try {
            List<Term> roles = ontologyService.getAllRoles();
            
            Collections.sort(roles, new  Comparator<Term>() {
                @Override
                public int compare(Term o1, Term o2) {
                        return o1.getName().compareTo(o2.getName());
                }
            }
            );
            
            return roles;
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Gets the trait class suggestions.
     *
     * @return the trait class suggestions
     */
    /*
    @ModelAttribute("traitClassesSuggestionList")
    public List<TraitClassReference> getTraitClassSuggestions() {
        try {
            List<TraitClassReference> traitRefList = (List<TraitClassReference>) ontologyService.getAllTraitGroupsHierarchy(false);
            List<TraitClassReference> traitClass = getAllTraitClassesFromHierarchy(traitRefList); 
            return traitClass;
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
    */
    private List<TraitClassReference> getAllTraitClassesFromHierarchy(List<TraitClassReference> refList) throws MiddlewareQueryException{
        
        List<TraitClassReference> traitClass = new ArrayList();
        for(TraitClassReference ref : refList){
            traitClass.add(ref);
            traitClass.addAll(getAllTraitClassesFromHierarchy(ref.getTraitClassChildren()));
        }
        return traitClass;
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
            LOG.error(e.getMessage(), e);
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
            LOG.error(e.getMessage(), e);
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
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
    
    /**
     * Save new term.
     *
     * @param propertyId the property id
     * @param local the local
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value="retrieve/trait/property/{propertyId}", method=RequestMethod.GET)
    public Map<String, String> retrieveTraitProperty(@PathVariable String propertyId, Locale local) {
        Map<String, String> resultMap = new HashMap<String, String>();
        
        try {
            Property property = ontologyService.getProperty(Integer.parseInt(propertyId));
            Term term = property.getIsA();
            String traitId = term  == null ? "": Integer.toString(term.getId());
            //term.getId();
            resultMap.put("status", "1");
            resultMap.put("traitId", traitId);
            
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());
        }
        return resultMap;
    }
    
    /*
     * Gets the standard variable details.
     *
     * @param variableId the variable id
     * @return the standard variable details
     *
    @ResponseBody
    @RequestMapping(value="retrieve/variable/{variableId}", method=RequestMethod.GET)
    public Map<String, String> getStandardVariableDetails(@PathVariable String variableId) {
        Map<String, String> resultMap = new HashMap<String, String>();
        
        try {
            StandardVariable stdVariable = ontologyService.getStandardVariable(Integer.parseInt(variableId));
            resultMap.put("status", "1");
            resultMap.put("name", stdVariable.getName()==null ? "" : stdVariable.getName());
            resultMap.put("description", stdVariable.getDescription()==null ? "" : stdVariable.getDescription());
            resultMap.put("dataType", checkIfNull(stdVariable.getDataType()));
            resultMap.put("role", checkIfNull(stdVariable.getStoredIn()));
            resultMap.put("cropOntologyId", stdVariable.getCropOntologyId()==null ? "" : stdVariable.getCropOntologyId());
            resultMap.put("traitClass", checkIfNull(stdVariable.getIsA()));
            resultMap.put("property", checkIfNull(stdVariable.getProperty()));
            resultMap.put("method", checkIfNull(stdVariable.getMethod()));
            resultMap.put("scale", checkIfNull(stdVariable.getScale()));
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());
        }
        
        return resultMap;
    }
    */
    
    /*
     * Check if null.
     *
     * @param term the term
     * @return the string
     * 
    private String checkIfNull(Term term) {
        return term==null ? "" : String.valueOf(term.getId());
    }
    */
    /*
     * Delete ontology.
     *
     * @param combo the combo
     * @param traitClass the trait class
     * @param property the property
     * @param method the method
     * @param scale the scale
     * @param local the local
     * @return the map
    @ResponseBody
    @RequestMapping(value="deleteOntology/{combo}", method=RequestMethod.POST)
    public Map<String, String> deleteOntology(@PathVariable String combo,
            @RequestParam String traitClass, @RequestParam String property,  
            @RequestParam String method, @RequestParam String scale, Locale local) {
        Map<String, String> resultMap = new HashMap<String, String>();
        
        String errorMessage = validateSelectedData(combo, traitClass, property, method, scale, local);
        
        if (errorMessage == null) {
            
        } else {
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", errorMessage);
        }
        
        return resultMap;
    }
    */
    /*
     * Validate selected data.
     *
     * @param combo the combo
     * @param traitClass the trait class
     * @param property the property
     * @param method the method
     * @param scale the scale
     * @param local the local
     * @return the string
    private String validateSelectedData(String combo, String traitClass, String property,  
            String method, String scale, Locale local) {
        String errorMessage = null;
        
        if (combo.equals("TraitClass")) {
            errorMessage = validateOntology("Trait Class", traitClass, local, TermId.IS_A);
        } else if (combo.equals("Property")) {
            errorMessage = validateOntology(combo, property, local, TermId.HAS_PROPERTY);
        } else if (combo.equals("Method")) {
            errorMessage = validateOntology(combo, method, local, TermId.HAS_METHOD);
        } else {
            errorMessage = validateOntology(combo, scale, local, TermId.HAS_SCALE);
        }
        return errorMessage;
    }
    */
    /*
     * Validate ontology.
     *
     * @param combo the combo
     * @param id the id
     * @param local the local
     * @param termId the term id
     * @return the string    
    private String validateOntology(String combo, String id, Locale local, TermId termId) {
        String message = null;
        try {
            if (Integer.parseInt(id) > -1) {
                message = messageSource.getMessage("ontology.browser.modal.ontology.from.central.database", new String[] {combo, ontologyService.getTermById(Integer.parseInt(id)).getName()}, local);
            } else if (ontologyService.getStandardVariableIdByTermId(Integer.parseInt(id), termId) != null){
                message = messageSource.getMessage("ontology.browser.modal.ontology.has.linked.standard.variable", new String[] {combo, ontologyService.getTermById(Integer.parseInt(id)).getName()}, local);
            }
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        return message;
    }*/
}
