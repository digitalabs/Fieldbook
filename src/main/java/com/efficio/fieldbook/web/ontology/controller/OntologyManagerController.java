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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.ontology.bean.EnumerationOperation;
import com.efficio.fieldbook.web.ontology.form.OntologyBrowserForm;
import com.efficio.fieldbook.web.ontology.form.OntologyMethodForm;
import com.efficio.fieldbook.web.ontology.form.OntologyModalForm;
import com.efficio.fieldbook.web.ontology.form.OntologyPropertyForm;
import com.efficio.fieldbook.web.ontology.form.OntologyScaleForm;
import com.efficio.fieldbook.web.ontology.form.OntologyTraitClassForm;
import com.efficio.fieldbook.web.ontology.validation.OntologyBrowserValidator;
import com.efficio.fieldbook.web.util.TreeViewUtil;


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
    
    /** AJAX PAGES *. */
    public static final String TRAIT_CLASS_MODAL = "/OntologyBrowser/manage/traitClass";
    
    /** The Constant PROPERTY_MODAL. */
    public static final String PROPERTY_MODAL = "/OntologyBrowser/manage/property";
    
    /** The Constant SCALE_MODAL. */
    public static final String SCALE_MODAL = "/OntologyBrowser/manage/scale";
    
    /** The Constant METHOD_MODAL. */
    public static final String METHOD_MODAL = "/OntologyBrowser/manage/method";
    
    /** The Constant LINKED_VARIABLES_MODAL. */
    public static final String LINKED_VARIABLES_MODAL = "/OntologyBrowser/manage/linkedVariable";
    
    /** The ontology service. */
    @Resource
    private OntologyService ontologyService;
    
    /** The message source. */
    @Autowired
    public MessageSource messageSource;
    
    /** The error handler service. */
    @Resource
    private ErrorHandlerService errorHandlerService;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    
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
    public String showTraitClass(@ModelAttribute("ontologyTraitClassForm") OntologyTraitClassForm form
            , Model model) {
        
        return super.showAjaxPage(model, TRAIT_CLASS_MODAL);
    }

    /**
     * Save trait class.
     *
     * @param form the form
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value="traitClass", method = RequestMethod.POST)
    public Map<String, Object> saveTraitClass(
            @ModelAttribute("ontologyTraitClassForm") OntologyTraitClassForm form) {
         return saveOntology(form);
    }
    
    /**
     * Show the main import page.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="property", method = RequestMethod.GET)
    public String showProperty(@ModelAttribute("ontologyPropertyForm") OntologyPropertyForm form, Model model) {
        
        return super.showAjaxPage(model, PROPERTY_MODAL);
    }

    /**
     * Save property.
     *
     * @param form the form
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value="property", method = RequestMethod.POST)
    public Map<String, Object> saveProperty(@ModelAttribute("ontologyPropertyForm") OntologyPropertyForm form) {

        return saveOntology(form);
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
        
        return super.showAjaxPage(model, SCALE_MODAL);
    }
    
    /**
     * Save scale.
     *
     * @param form the form
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value="scale", method = RequestMethod.POST)
    public Map<String, Object> saveScale(@ModelAttribute("ontologyScaleForm") OntologyScaleForm form) {

        return saveOntology(form);
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
        
        return super.showAjaxPage(model, METHOD_MODAL);
    }
    
    /**
     * Save method.
     *
     * @param form the form
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value="method", method = RequestMethod.POST)
    public Map<String, Object> saveMethod(@ModelAttribute("ontologyMethodForm") OntologyMethodForm form) {

        return saveOntology(form);
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
    	return showManageVariable(form, false, 0, model);
    }
    /**
     * Show the main import page.
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="variable/id/{variableId}", method = RequestMethod.GET)
    public String showVariablePopup(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form, 
    		@PathVariable int variableId, Model model) {
        
    	return showManageVariable(form, true, variableId, model);
    }
    
    public String showManageVariable(OntologyBrowserForm form, boolean showAsPopup, int variableId, Model model) {
        
        try {
            List<TraitClassReference> traitRefList = 
                    (List<TraitClassReference>) ontologyService.getAllTraitGroupsHierarchy(false);
            List<TraitClassReference> traitClass = getAllTraitClassesFromHierarchy(traitRefList);
            List<TraitClassReference> parentList = new ArrayList<TraitClassReference>();
            TraitClassReference refAll = new TraitClassReference(0, "ALL");
            refAll.setTraitClassChildren(traitRefList);
            parentList.add(refAll);
            model.addAttribute("traitClassTreeData", TreeViewUtil.convertOntologyTraitsToJson(parentList, null));
            model.addAttribute("traitClassesSuggestionList", traitClass);
            model.addAttribute("propertiesSuggestionList", ontologyService.getAllPropertiesWithTraitClass());
            model.addAttribute("methodsSuggestionList", ontologyService.getAllMethods());
            model.addAttribute("scalesSuggestionList", ontologyService.getAllScales());
            model.addAttribute("isPopup", showAsPopup ? "1" : "0");
            form.setFromPopup(showAsPopup ? "1" : "0");
            model.addAttribute("preselectVariableId", variableId);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
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
    public String saveNewVariable(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form
            , BindingResult result, Model model) {
        OntologyBrowserValidator validator = new OntologyBrowserValidator();
        boolean isPopup = false;
        //validations for delete and update
        if (form.getIsDelete().equals(1)) {
            validateDelete(form, result);
        } else if (form.getIsDelete().equals(0) 
                || (form.getIsDelete().equals(0) && form.getVariableId() > -1)) {
            LOG.debug("hallow");
            validator.validate(form, result);
        } 
        
        form.setAddSuccessful("0");
        
        if (result.hasErrors()) {
            /**
             * Return the user back to form to show errors
             */
            form.setHasError("1");
            return show(form,model);
        } else {
            try {
                if (form.getIsDelete().equals(1)) {
                    //delete
                    ontologyService.deleteStandardVariable(form.getVariableId());
                } else {
                    //add or update
                    Operation operation = form.getVariableId() != null ? Operation.UPDATE : Operation.ADD;
                    StandardVariable standardVariable = new StandardVariable();
                    //if variable is from local, add/update the variable
                    //if it's from central, get the standard variable object only for update of valid value
                    if (form.getVariableId() == null || form.getVariableId() < 0) {
                        standardVariable = createStandardVariableObject(form, operation);
                        ontologyService.saveOrUpdateStandardVariable(standardVariable, operation);
                        standardVariable = ontologyService.getStandardVariable(standardVariable.getId());
                    } else {
                        standardVariable = ontologyService.getStandardVariable(form.getVariableId());
                        form.setNewVariableName(standardVariable.getName());
                    }
                    saveConstraintsAndValidValues(form, standardVariable);
                    form.setVariableId(standardVariable.getId());
                    form.setVariableName(standardVariable.getName());
                }
                form.setAddSuccessful("1");
                model.addAttribute("isPopup", form.getFromPopup());
                
                if(form.getFromPopup() != null && form.getFromPopup().equalsIgnoreCase("1")){
                	isPopup = true;
                }
           } catch (MiddlewareQueryException e) {
               LOG.error(e.getMessage(), e);
               form.setAddSuccessful("2");
               form.setErrorMessage(errorHandlerService.getErrorMessagesAsString(e.getCode(), null, "\n"));
           } catch (MiddlewareException e) {
               LOG.error(e.getMessage(), e);
               form.setAddSuccessful("2");
               form.setErrorMessage(errorHandlerService.getErrorMessagesAsString(e.getMessage(), null, "\n"));
           }
        }
        return showManageVariable(form, isPopup, 0, model);
    }
    
    /**
     * Save new variable.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value="variable/verify", method = RequestMethod.POST)
    public String saveVerifyNewVariable(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form
            , BindingResult result, Model model) {
        boolean error = false;
            try {
               
                    //add or update
                    Operation operation = form.getVariableId() != null ? Operation.UPDATE : Operation.ADD;
                    StandardVariable standardVariable = new StandardVariable();
                    //if variable is from local, add/update the variable
                    //if it's from central, get the standard variable object only for update of valid value
                    
                    Integer standardVariableId = null;
                    if (form.getVariableId() == null || form.getVariableId() < 0) {
                        standardVariable = createStandardVariableObject(form, operation);
                        standardVariableId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(standardVariable.getProperty().getName(), standardVariable.getScale().getName(), standardVariable.getMethod().getName(),standardVariable.getPhenotypicType());
                    }                                      
                    
                    if(form.getVariableId() == null && standardVariableId != null){
                    	error = true;
                    }else if(form.getVariableId() != null && standardVariableId != null && form.getVariableId().intValue() != standardVariableId.intValue()){
                    	error = true;
                    }
                    
                
                
           } catch (MiddlewareQueryException e) {
               LOG.error(e.getMessage(), e);
               form.setAddSuccessful("2");
               form.setErrorMessage(errorHandlerService.getErrorMessagesAsString(e.getCode(), null, "\n"));
           }
            if(error){
            	return "error";
            }
            return "success";
       
    }
    
    /**
     * Save constraints and valid values.
     *
     * @param form the form
     * @param stdVariable the std variable
     * @throws MiddlewareQueryException the middleware query exception
     * @throws MiddlewareException the middleware exception
     */
    private void saveConstraintsAndValidValues(OntologyBrowserForm form, StandardVariable stdVariable) 
            throws MiddlewareQueryException, MiddlewareException {
        String dataTypeId = form.getDataType() == null ? form.getDataTypeId() : form.getDataType(); 
        String dataType = ontologyService.getTermById(Integer.parseInt(dataTypeId)).getName();
        if (dataType.contains("Categorical")) {
            saveValidValues(form, stdVariable);
            //if datatype is changed to categorical, delete constraints
            if (stdVariable.getConstraints() != null) {
                ontologyService.deleteStandardVariableMinMaxConstraints(stdVariable.getId());
            }
        } else if (dataType.contains("Numeric variable")) {
            saveConstraints(form, stdVariable); 
            //if datatype is changed to numeric, delete valid values
            if (stdVariable.getEnumerations() != null) {
                deleteValidValues(stdVariable);
            }
        } else {
            //if datatype is neither numeric nor categorical, delete constraints/valid values
            if (stdVariable.getEnumerations() != null) {
                deleteValidValues(stdVariable);
            }
            if (stdVariable.getConstraints() != null) {
                ontologyService.deleteStandardVariableMinMaxConstraints(stdVariable.getId());
            }
        }
    }
    
    /**
     * Save constraints.
     *
     * @param form the form
     * @param stdVariable the std variable
     * @throws MiddlewareQueryException the middleware query exception
     * @throws MiddlewareException the middleware exception
     */
    private void saveConstraints(OntologyBrowserForm form, StandardVariable stdVariable) 
            throws MiddlewareQueryException, MiddlewareException {
        Double minValue = form.getMinValue();
        Double maxValue = form.getMaxValue();
        
        if (minValue == null && maxValue == null && stdVariable.getConstraints() != null) {
            //delete constraints
            ontologyService.deleteStandardVariableMinMaxConstraints(stdVariable.getId());
        } else if (stdVariable.getConstraints() != null){
            //update constraints
            ontologyService.addOrUpdateStandardVariableMinMaxConstraints(stdVariable.getId(), 
                    new VariableConstraints(stdVariable.getConstraints().getMinValueId(), 
                            stdVariable.getConstraints().getMaxValueId(), minValue, maxValue));
        } else {
            //add constraints
            ontologyService.addOrUpdateStandardVariableMinMaxConstraints(stdVariable.getId(), 
                    new VariableConstraints(minValue, maxValue));
        }
    }
    
    /**
     * Save valid values.
     *
     * @param form the form
     * @param stdVariable the std variable
     * @throws MiddlewareQueryException the middleware query exception
     */
    private void saveValidValues(OntologyBrowserForm form, StandardVariable stdVariable) 
            throws MiddlewareQueryException, MiddlewareException {
        List<EnumerationOperation> enumerations = convertToEnumerationOperation(form.getEnumerations());
        for (EnumerationOperation enumeration : enumerations) {
            if (enumeration.getOperation() > 0) {
                //add valid value
            	//to make sure the standard variable is updated 
                ontologyService.addStandardVariableValidValue(stdVariable, 
                        new Enumeration(enumeration.getId(), enumeration.getName()
                                , enumeration.getDescription(), 0));
            } else if (enumeration.getOperation() < 0) {
                //delete valid value
                ontologyService.deleteStandardVariableValidValue(stdVariable.getId(), enumeration.getId());
                List<Enumeration> enumerationVar = stdVariable.getEnumerations();
                List<Enumeration> newStdEnumList = new ArrayList();
                for(int i = 0 ; i < enumerationVar.size() ; i++){
                	Enumeration enumVar = enumerationVar.get(i);
                	if(enumeration.getId() != null && enumVar.getId().intValue() == enumeration.getId().intValue()){
                		continue;
                	}else{
                		newStdEnumList.add(enumVar);
                	}
                }
                stdVariable.setEnumerations(newStdEnumList);
            }
        }
    } 
    
    private void deleteValidValues(StandardVariable stdVariable) throws MiddlewareQueryException {
        for (Enumeration enumeration : stdVariable.getEnumerations()) {
            ontologyService.deleteStandardVariableValidValue(stdVariable.getId(), enumeration.getId());
        }
    }
    
    /**
     * Convert to enumeration operation.
     *
     * @param enumerations the enumerations
     * @return the list
     */
    private static List<EnumerationOperation> convertToEnumerationOperation(String enumerations) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(enumerations, new TypeReference<List<EnumerationOperation>>() { });
        } catch(Exception e) {
            LoggerFactory.getLogger(OntologyManagerController.class).error(e.getMessage(), e);
        }
        return new ArrayList<EnumerationOperation>();
    }
    
    /**
     * Creates the standard variable object.
     *
     * @param form the form
     * @param operation the operation
     * @return the standard variable
     * @throws MiddlewareQueryException the middleware query exception
     */
    private StandardVariable createStandardVariableObject(OntologyBrowserForm form, Operation operation) 
            throws MiddlewareQueryException {
        StandardVariable standardVariable = new StandardVariable();
        String description = null;
        
        if (form.getVariableId() != null) {
            standardVariable.setId(form.getVariableId());
        }
        
        if (operation.equals(Operation.ADD)) {
            standardVariable.setName(form.getVariableName());
        } else {
            standardVariable.setName(form.getNewVariableName());
        }

        if (form.getVariableDescription() == null) {
            description = form.getNewVariableName();
        } else {
            description = form.getVariableDescription();
        }
        
        standardVariable.setName(form.getNewVariableName());
        standardVariable.setDescription(description);
        standardVariable.setProperty(ontologyService.getTermById(Integer.parseInt(form.getProperty())));
        standardVariable.setMethod(ontologyService.getTermById(Integer.parseInt(form.getMethod())));
        standardVariable.setScale(ontologyService.getTermById(Integer.parseInt(form.getScale())));
        standardVariable.setDataType(ontologyService.getTermById(Integer.parseInt(form.getDataType())));
        
            standardVariable.setPhenotypicType(ontologyService.getPhenotypicTypeById(
                    Integer.parseInt(form.getRole())));
            standardVariable.setStoredIn(ontologyService.getTermById(Integer.parseInt(form.getRole())));
            
        standardVariable.setCropOntologyId(form.getCropOntologyDisplay());
        
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
                if(!isInteger(traitClass)){
                    //meaning we need to save the trait class
                    traitClassTerm = ontologyService
                            .addTraitClass(traitClass, traitClassDescription
                                    , TermId.ONTOLOGY_TRAIT_CLASS.getId()).getTerm();
                    
                    resultMap.put("traitId", String.valueOf(traitClassTerm.getId()));
                    resultMap.put("traitName", traitClassTerm.getName());
                    resultMap.put("traitDefinition", traitClassTerm.getDefinition());
                    resultMap.put("addedNewTrait", "1");
                    traitClassId = Integer.valueOf(traitClassTerm.getId());
                }else{
                    traitClassId = Integer.parseInt(traitClass);
                }
                term = ontologyService.addProperty(property.trim(), propertyDescription, traitClassId).getTerm();
                
            } else if (combo.equals("Method")) {
                if (methodDescription == null || methodDescription.equals("")) {
                    methodDescription = method;
                }
                term = ontologyService.addMethod(method.trim(), methodDescription).getTerm();
            } else if (combo.equals("Scale")) {
                if (scaleDescription == null || scaleDescription.equals("")) {
                    scaleDescription = scale;
                }
                term = ontologyService.addScale(scale.trim(), scaleDescription).getTerm();
            } else {
                if (traitClassDescription == null || traitClassDescription.equals("")) {
                    traitClassDescription = traitClass;
                }
                ontologyName = "Trait Class";
                term = ontologyService.addTraitClass(traitClass.trim(), traitClassDescription
                        , TermId.ONTOLOGY_TRAIT_CLASS.getId()).getTerm();
            }          
              
            resultMap.put("status", "1");
            resultMap.put("id", String.valueOf(term.getId()));
            resultMap.put("name", term.getName());
            resultMap.put("definition", term.getDefinition());
            Object[] args = new Object[2];
            args[0] = ontologyName;
            args[1] = term.getName();
            resultMap.put("successMessage", messageSource
                    .getMessage("ontology.browser.modal.variable.name.save.success", args, local));
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());
        }
        return resultMap;
    }
    
    /**
     * Gets the variable name.
     *
     * @return the variable name
     */
    @ModelAttribute("variableNameSuggestionList")
    public List<Term> getVariableName() {
        try {
            return ontologyService.getAllTermsByCvId(CvId.VARIABLES);
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

    private List<TraitClassReference> getAllTraitClassesFromHierarchy(List<TraitClassReference> refList) 
            throws MiddlewareQueryException{
        
        List<TraitClassReference> traitClass = new ArrayList<TraitClassReference>();
        for(TraitClassReference ref : refList){
            traitClass.add(ref);
            traitClass.addAll(getAllTraitClassesFromHierarchy(ref.getTraitClassChildren()));
        }
        return traitClass;
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
            resultMap.put("status", "1");
            resultMap.put("traitId", traitId);
            resultMap.put("cropOntologyId", property.getCropOntologyId());
            
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());
        }
        return resultMap;
    }
    
    /**
     * Gets the standard variable details.
     *
     * @param variableId the variable id
     * @return the standard variable details
     */
    @ResponseBody
    @RequestMapping(value="retrieve/variable/{variableId}", method=RequestMethod.GET)
    public Map<String, String> getStandardVariableDetails(@PathVariable String variableId) {
        Map<String, String> resultMap = new HashMap<String, String>();
        
        try {
            StandardVariable stdVariable = 
                    ontologyService.getStandardVariable(Integer.parseInt(variableId));
            resultMap.put("status", "1");
            resultMap.put("name", stdVariable.getName()==null ? "" : stdVariable.getName());
            resultMap.put("description", stdVariable.getDescription() == null 
                                ? "" : stdVariable.getDescription());
            resultMap.put("dataType", checkIfNull(stdVariable.getDataType()));
            resultMap.put("role", checkIfNull(stdVariable.getStoredIn()));
            resultMap.put("cropOntologyDisplay", stdVariable.getCropOntologyId() == null 
                                ? "" : stdVariable.getCropOntologyId());
            resultMap.put("traitClass", checkIfNull(stdVariable.getIsA()));
            resultMap.put("property", checkIfNull(stdVariable.getProperty()));
            resultMap.put("method", checkIfNull(stdVariable.getMethod()));
            resultMap.put("scale", checkIfNull(stdVariable.getScale()));
            if (stdVariable.getConstraints() != null) {
                resultMap.put("minValue", String.valueOf(stdVariable.getConstraints().getMinValue()));
                resultMap.put("maxValue", String.valueOf(stdVariable.getConstraints().getMaxValue()));
            } else {
                resultMap.put("minValue", "");
                resultMap.put("maxValue", "");
            }
            
            resultMap.put("validValues", convertEnumerationsToJSON(stdVariable.getEnumerations()));
            
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());
        }
        
        return resultMap;
    }
    
    /**
     * Convert enumerations to json.
     *
     * @param enumerations the enumerations
     * @return the string
     */
    private String convertEnumerationsToJSON(List<Enumeration> enumerations) {
        if (enumerations!= null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(enumerations);
            } catch(Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return "";
    }
    
    /**
     * Show the main import page.
     *
     * @param ontologyType the ontology type
     * @param id the id
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="retrieve/linked/variable/{ontologyType}/{id}", method = RequestMethod.GET)
    public String getLinkedVariable(@PathVariable String ontologyType
            , @PathVariable String id, Model model) {
        
        try {
            List<StandardVariable> standardVariableList = new ArrayList<StandardVariable>();
            
            if("ManageProperty".equalsIgnoreCase(ontologyType)){
                standardVariableList = ontologyService.getStandardVariablesByProperty(Integer.valueOf(id));
            }else if("ManageTraitClass".equalsIgnoreCase(ontologyType)){
                standardVariableList = ontologyService.getStandardVariablesByTraitClass(Integer.valueOf(id));
            }else if("ManageMethod".equalsIgnoreCase(ontologyType)){
                standardVariableList = ontologyService.getStandardVariablesByMethod(Integer.valueOf(id));
            }else if("ManageScale".equalsIgnoreCase(ontologyType)){
                standardVariableList = ontologyService.getStandardVariablesByScale(Integer.valueOf(id));
            }
            
            if(standardVariableList != null && !standardVariableList.isEmpty()){
                Collections.sort(standardVariableList, new Comparator<Object>(){
                    @Override
                    public int compare(Object o1, Object o2) {
                        return ((StandardVariable)o1).getName().toUpperCase()
                                .compareTo(((StandardVariable)o2).getName().toUpperCase());
                    }
                    
                }); 
            }
            model.addAttribute("linkedStandardVariableList", standardVariableList);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, LINKED_VARIABLES_MODAL);
    }
     
    /**
     * Check if null.
     *
     * @param term the term
     * @return the string
     */
    private String checkIfNull(Term term) {
        return term == null ? "" : String.valueOf(term.getId());
    }
    
    /**
     * Validate delete.
     *
     * @param o the o
     * @param errors the errors
     */
    private void validateDelete(Object o, Errors errors) {
        OntologyBrowserForm form = (OntologyBrowserForm) o;
        try {
            if (form.getVariableId() > -1) {
                errors.rejectValue("variableName", "ontology.browser.cannot.delete.central.variable"
                        , new String[] {
                                ontologyService.getStandardVariable(form.getVariableId()).getName()}
                                    , "ontology.browser.cannot.delete.central.variable");
            } else if (ontologyService.countProjectsByVariable(form.getVariableId()) > 0) {
                errors.rejectValue("variableName", "ontology.browser.cannot.delete.linked.variable"
                        , new String[] {
                            ontologyService.getStandardVariable(form.getVariableId()).getName()}
                                    , "ontology.browser.cannot.delete.linked.variable");
            }
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    /**
     * Save ontology.
     *
     * @param form the form
     * @return the map
     */
    private Map<String, Object> saveOntology(OntologyModalForm form) {
        Map<String, Object> result = new HashMap<String, Object>();
        Locale locale = LocaleContextHolder.getLocale();
        String ontologyName = null;

        try {
            if (form instanceof OntologyTraitClassForm) {
                ontologyName = messageSource.getMessage("ontology.browser.modal.trait.class", null, locale);
                String desc = ((OntologyTraitClassForm) form).getManageTraitClassDescription();
                if(desc != null && desc.equalsIgnoreCase("")){
                    desc = ((OntologyTraitClassForm) form).getManageTraitClassName().trim();
                }
                result.put("savedObject", ontologyService.addOrUpdateTraitClass(
                        ((OntologyTraitClassForm) form).getManageTraitClassName().trim(), 
                        desc, 
                        ((OntologyTraitClassForm) form).getManageParentTraitClassId()));

                List<TraitClassReference> traitRefList = 
                        (List<TraitClassReference>) ontologyService.getAllTraitGroupsHierarchy(false);
                List<TraitClassReference> traitClass = getAllTraitClassesFromHierarchy(traitRefList);
                List<TraitClassReference> parentList = new ArrayList<TraitClassReference>();
                TraitClassReference refAll = new TraitClassReference(0, "ALL");
                refAll.setTraitClassChildren(traitRefList);
                parentList.add(refAll);
                try {
                    result.put("traitClassTreeData", TreeViewUtil.convertOntologyTraitsToJson(parentList, null));
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                result.put("traitClassesSuggestionList", traitClass);

            }
            else if (form instanceof OntologyPropertyForm) {
                ontologyName = messageSource.getMessage("ontology.browser.modal.property.name", null, locale);
                String desc = ((OntologyPropertyForm) form).getManagePropertyDescription();
                if(desc != null && desc.equalsIgnoreCase("")){
                    desc = ((OntologyPropertyForm) form).getManagePropertyName().trim();
                }
                result.put("savedObject", ontologyService.addOrUpdateProperty(
                        ((OntologyPropertyForm)form).getManagePropertyName().trim(), 
                        desc, 
                        ((OntologyPropertyForm)form).getManagePropTraitClassId(),
                        ((OntologyPropertyForm)form).getCropOntologyId()));
            }
            else if (form instanceof OntologyScaleForm) {
                ontologyName = messageSource.getMessage("ontology.browser.modal.scale.name", null, locale);
                String desc = ((OntologyScaleForm) form).getManageScaleDescription();
                if(desc != null && desc.equalsIgnoreCase("")){
                    desc = ((OntologyScaleForm) form).getManageScaleName().trim();
                }
                result.put("savedObject", ontologyService.addOrUpdateScale(
                        ((OntologyScaleForm) form).getManageScaleName().trim(), 
                        desc));
            }
            else if (form instanceof OntologyMethodForm) {
                ontologyName = messageSource.getMessage("ontology.browser.modal.method.name", null, locale);
                String desc = ((OntologyMethodForm) form).getManageMethodDescription();
                if(desc != null && desc.equalsIgnoreCase("")){
                    desc = ((OntologyMethodForm) form).getManageMethodName().trim();
                }
                result.put("savedObject", ontologyService.addOrUpdateMethod(
                        ((OntologyMethodForm) form).getManageMethodName().trim(), 
                        desc));
            }
            
            if (form.isAddMode()) { //add mode
                result.put("successMessage", 
                        messageSource.getMessage("ontology.browser.modal.add.ontology.successful", 
                                new Object[] {ontologyName, form.getName()}, 
                                locale));
            } 
            else { //edit mode
                result.put("successMessage", 
                        messageSource.getMessage("ontology.browser.modal.update.ontology.successful", 
                                new Object[] {ontologyName, form.getName()}, 
                                locale));
            }

            result.put("status", "1");
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("status",  "0");
            result.put("errorMessage", errorHandlerService.getErrorMessagesAsString(e.getCode()
                    , new Object[] {ontologyName, form.getName()}, "<br/>"));
        } catch (MiddlewareException e) {
            LOG.error(e.getMessage(), e);
            result.put("status",  "0");
            result.put("errorMessage", errorHandlerService.getErrorMessagesAsString(e.getMessage()
                    , null, "<br/>"));
        }
        return result;
    }
    
    /**
     * Delete ontology.
     *
     * @param id the id
     * @param name the name
     * @param ontology the ontology
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value = "deleteOntology/{ontology}", method = RequestMethod.POST)
    public Map<String, Object> deleteOntology(@RequestParam(required=false) Integer id
            , @RequestParam(required=false) String name, @PathVariable String ontology) {
        Map<String, Object> result = new HashMap<String, Object>();
        Locale locale = LocaleContextHolder.getLocale();
        
        String ontologyTypeName = 
                messageSource.getMessage("ontology.browser.modal." + ontology+".name", null, locale);
        
        try {
            ontologyService.deleteTraitClass(id);
            result.put("status", "1");
            result.put("successMessage", messageSource
                        .getMessage("ontology.browser.modal.delete.ontology.successful", 
                    new Object[] {ontologyTypeName, name}, locale));
            if("traitClass".equalsIgnoreCase(ontology)){
                List<TraitClassReference> traitRefList = 
                        (List<TraitClassReference>) ontologyService.getAllTraitGroupsHierarchy(false);
                List<TraitClassReference> traitClass = getAllTraitClassesFromHierarchy(traitRefList);
                List<TraitClassReference> parentList = new ArrayList<TraitClassReference>();
                TraitClassReference refAll = new TraitClassReference(0, "ALL");
                refAll.setTraitClassChildren(traitRefList);
                parentList.add(refAll);
                try {
                    result.put("traitClassTreeData", TreeViewUtil.convertOntologyTraitsToJson(parentList, null));
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                result.put("traitClassesSuggestionList", traitClass);
            }
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("status", "0");
            result.put("errorMessage", errorHandlerService.getErrorMessagesAsString(
                            e.getMessage(), new Object[] {ontologyTypeName, name}, "<br/>"));
        }
        
        return result;
    }
    
    /**
     * Delete ontology.
     *
     * @param id the id
     * @param name the name
     * @param ontology the ontology
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value = "categorical/verify/{standardVariableId}/{enumerationId}", method = RequestMethod.GET)
    public Map<String, Object> deleteValidValue(@PathVariable String standardVariableId, 
    		@PathVariable String enumerationId) {
        Map<String, Object> result = new HashMap<String, Object>();
        
        try {
        	boolean isValidDelete = ontologyService.validateDeleteStandardVariableEnumeration(Integer.parseInt(standardVariableId),Integer.parseInt(enumerationId));
        	if(isValidDelete)
        		result.put("status", "1");
        	else
        		result.put("status", "0");
        } catch(MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("status", "0");          
        }
        
        return result;
    }
}
