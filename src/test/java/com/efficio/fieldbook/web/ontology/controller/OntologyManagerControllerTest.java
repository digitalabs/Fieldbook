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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermProperty;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.ontology.form.OntologyBrowserForm;
import com.efficio.fieldbook.web.ontology.form.OntologyPropertyForm;

/**
 * The Class OntologyManagerControllerTest.
 *
 * @author Chezka Camille Arevalo
 */
public class OntologyManagerControllerTest extends AbstractBaseControllerIntegrationTest {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OntologyControllerTest.class);
    
    /** The ontology service. */
    @Autowired
    OntologyService ontologyService;
    
    /** The standard variable. */
    private StandardVariable standardVariable;
    
    private OntologyService mockOntologyService;
    private OntologyBrowserForm form;
    private OntologyPropertyForm propForm;
    private Term term;
    private OntologyManagerController ontologyManagerController;
     
    private static final int VARIABLE_ID = 1;
	private static final String VARIABLE_NAME = "NREP";
	private static final String VARIABLE_DEFINITION = "Number of replications in an experiment";
	
	private static final int PROPERTY_ID = 2;
	private static final String PROPERTY_NAME = "ACQ_DATE"; 
	private static final String PROPERTY_DEFINITION = "ACQ_DATE definition";
    
    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        List<TermProperty> termProperties = new ArrayList<TermProperty>();
        termProperties.add(new TermProperty(1, TermId.CROP_ONTOLOGY_ID.getId(), "CO:12345", 0));
        
        try {
        String propertyName = "property name " + new Random().nextInt(10000);
        ontologyService.addProperty(propertyName, "test property", 1087);
        Property property = ontologyService.getProperty(propertyName);
        
        String scaleName = "scale name " + new Random().nextInt(10000);
        Term scale = ontologyService.addTerm(scaleName, "test scale", CvId.SCALES);
        
        String methodName = "method name " + new Random().nextInt(10000);
        Term method = ontologyService.addTerm(methodName, methodName, CvId.METHODS);
        
        Term dataType = new Term(400, "DATA TYPE", "DATA TYPE DEF");
        Term storedIn = new Term(1010, "STORED IN", "STORED IN DEF");
        Term traitClass = new Term(600, "TRAIT CLASS", "TRAIT CLASS DEF");
        
        standardVariable = new StandardVariable();
        standardVariable.setName("TestVariable" + new Random().nextInt(10000));
        standardVariable.setDescription("Test Desc");
        standardVariable.setProperty(property.getTerm());
        standardVariable.setMethod(method);
        standardVariable.setScale(scale);
        standardVariable.setDataType(dataType);
        standardVariable.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
        standardVariable.setIsA(traitClass);
        standardVariable.setStoredIn(storedIn);
        standardVariable.setCropOntologyId("CO:1200");
        standardVariable.setConstraints(new VariableConstraints(1.0, 10.0));
        ontologyService.addStandardVariable(standardVariable);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        ontologyManagerController = new OntologyManagerController();
    	form = new OntologyBrowserForm();
    	propForm = new OntologyPropertyForm();
    	mockOntologyService = Mockito.mock(OntologyService.class);
    	term = Mockito.mock(Term.class);
    }

    @Test
    public void testSaveNewVariableConfirmPreselectVariableIdIsMaintainedIfFromPopup() throws MiddlewareQueryException{
    	int variableId = 10180;
    	String dataTypeId = "1";
    	
    	Mockito.when(term.getName()).thenReturn("Categorical");
    	Mockito.when(mockOntologyService.getStandardVariable(10180)).thenReturn(Mockito.mock(StandardVariable.class));
    	Mockito.when(mockOntologyService.getTermById(Integer.parseInt(dataTypeId))).thenReturn(term);

    	BindingResult result = Mockito.mock(BindingResult.class);
    	Model model = new ExtendedModelMap();
    	form.setIsDelete(0);
    	form.setFromPopup("1");
    	form.setPreselectVariableId(variableId);
    	form.setVariableId(variableId);
    	form.setDataTypeId(dataTypeId);
    	ontologyManagerController.setOntologyService(mockOntologyService);
    	ontologyManagerController.saveNewVariable(form, result, model);
    	Assert.assertEquals("Should return in model the same preselect variable id that was set in the form when it is a popup", variableId, model.asMap().get("preselectVariableId"));
    }
    @Test
    public void testSaveNewVariableConfirmPreselectVariableIdIsIsNotMaintainedIfNotFromPopup() throws MiddlewareQueryException{
    	int variableId = 10180;
    	String dataTypeId = "1";
    	
    	Mockito.when(term.getName()).thenReturn("Categorical");
    	Mockito.when(mockOntologyService.getStandardVariable(10180)).thenReturn(Mockito.mock(StandardVariable.class));
    	Mockito.when(mockOntologyService.getTermById(Integer.parseInt(dataTypeId))).thenReturn(term);


    	BindingResult result = Mockito.mock(BindingResult.class);
    	Model model = new ExtendedModelMap();
    	form.setIsDelete(0);
    	form.setFromPopup("0");
    	form.setPreselectVariableId(variableId);
    	form.setVariableId(variableId);
    	form.setDataTypeId(dataTypeId);
    	ontologyManagerController.setOntologyService(mockOntologyService);
    	ontologyManagerController.saveNewVariable(form, result, model);

    	Assert.assertEquals("Should return in model the 0 as preselect variable id when it is not from a popup", 0, model.asMap().get("preselectVariableId"));
    }
    
    @Test
    public void testValidateNewVariableNameExisting() throws MiddlewareQueryException, Exception {
    	
    	Term termWithValue = new Term(VARIABLE_ID, VARIABLE_NAME, VARIABLE_DEFINITION);
    	BindingResult result = null;
    	Model model = null;
    	
    	form.setVariableName(VARIABLE_NAME);
    	Mockito.when(mockOntologyService.findTermByName(VARIABLE_NAME, CvId.VARIABLES)).thenReturn(termWithValue);
    	
    	//set the mocked ontology service in the controller
    	ontologyManagerController.setOntologyService(mockOntologyService);
    	String status = ontologyManagerController.validateNewVariableName(form, result, model);
    	
    	Assert.assertEquals("error", status);
    }
    
    @Test
    public void testValidateNewVariableNameNonExisting() throws MiddlewareQueryException, Exception {
    	BindingResult result = null;
    	Model model = null;
    	String variableName = VARIABLE_NAME + new Random().nextInt();
    	
    	form.setVariableName(variableName);
    	Mockito.when(mockOntologyService.findTermByName(variableName, CvId.VARIABLES)).thenReturn(null);
    	
    	ontologyManagerController.setOntologyService(mockOntologyService);
    	String status = ontologyManagerController.validateNewVariableName(form, result, model);
    	
    	Assert.assertEquals("success", status);
    }
    
    @Test
    public void testValidationOfPropertyNameExisting() {
    	Term termWithValue = new Term(PROPERTY_ID, PROPERTY_NAME, PROPERTY_DEFINITION);
    	propForm.setManagePropertyName(PROPERTY_NAME);
    	try {
    		Mockito.when(mockOntologyService.findTermByName(PROPERTY_NAME, CvId.PROPERTIES)).thenReturn(termWithValue);
    		ontologyManagerController.setOntologyService(mockOntologyService);
    		
    		ontologyManagerController.validatePropertyName(propForm);
    	} catch (MiddlewareQueryException e) {
    		Assert.assertEquals("Expected error code error.ontology.property.exists but got " + 
    				e.getCode() + "instead", "error.ontology.property.exists", e.getCode());
    	}
    }
    
    @Test
    public void testValidationOfPropertyNameNonExisting() throws MiddlewareQueryException {
    	propForm.setManagePropertyName(PROPERTY_NAME);
    	boolean hasError = false;
    	try {
    		Mockito.when(mockOntologyService.findTermByName(PROPERTY_NAME, CvId.PROPERTIES)).thenReturn(null);
    		ontologyManagerController.setOntologyService(mockOntologyService);
    		
    		ontologyManagerController.validatePropertyName(propForm);
    	} catch (MiddlewareQueryException e) {
    		hasError = true;
    	}
    	
    	Assert.assertEquals("Not expecting an error but encountered an error instead.", false, hasError);
    }
}
