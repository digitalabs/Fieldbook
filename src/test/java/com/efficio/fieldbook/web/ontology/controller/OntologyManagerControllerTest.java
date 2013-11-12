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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.generationcp.middleware.domain.dms.Enumeration;
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
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Chezka Camille Arevalo
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class OntologyManagerControllerTest extends AbstractJUnit4SpringContextTests{
    
    private static final Logger LOG = LoggerFactory.getLogger(OntologyControllerTest.class);
    
    @Autowired
    OntologyService ontologyService;
    
    private StandardVariable standardVariable;
    
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
        
        Term dataType = new Term(400, "DATA TYPE", "DATA TYPE DEF", null, null);
        Term storedIn = new Term(1010, "STORED IN", "STORED IN DEF", null, null);
        Term traitClass = new Term(600, "TRAIT CLASS", "TRAIT CLASS DEF", null, null);
        
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
    }

    @Test
    public void testSaveConstraints() throws MiddlewareQueryException, MiddlewareException {
        Double minValue = null, maxValue = null;
        
        //delete constraint
        ontologyService.deleteStandardVariableMinMaxConstraints(standardVariable.getId());
        standardVariable = ontologyService.getStandardVariable(standardVariable.getId());
        assertNull(standardVariable.getConstraints());
        
        minValue = 0.0;
        maxValue = 10.0;
        
        //add constraint
        ontologyService.addOrUpdateStandardVariableMinMaxConstraints(standardVariable.getId(), new VariableConstraints(Double.valueOf(minValue.toString()), Double.valueOf(maxValue.toString())));
        standardVariable = ontologyService.getStandardVariable(standardVariable.getId());
        assertEquals(standardVariable.getConstraints().getMinValue(), minValue);
        assertEquals(standardVariable.getConstraints().getMaxValue(), maxValue);
        
        //edit
        minValue = 1.0;
        maxValue = 10.0;
        Integer minValueId = standardVariable.getConstraints().getMinValueId();
        Integer maxValueId = standardVariable.getConstraints().getMaxValueId();
        ontologyService.addOrUpdateStandardVariableMinMaxConstraints(standardVariable.getId(), 
                new VariableConstraints(minValueId, maxValueId, Double.valueOf(minValue.toString()), Double.valueOf(maxValue.toString())));
        standardVariable = ontologyService.getStandardVariable(standardVariable.getId());
        assertEquals(standardVariable.getConstraints().getMinValue(), minValue);
        assertEquals(standardVariable.getConstraints().getMaxValue(), maxValue);
    } 
    
    @Test
    public void testSaveValidValues() throws MiddlewareQueryException, MiddlewareException {
        //add
        List<Enumeration> enumerations = new ArrayList<Enumeration>();
        String randNum = String.valueOf(new Random().nextInt(10000));
        enumerations.add(new Enumeration(null, randNum, "Low " + randNum, 0));
        for (Enumeration enumeration : enumerations) {
            ontologyService.addStandardVariableValidValue(standardVariable, enumeration);
        }
        standardVariable = ontologyService.getStandardVariable(standardVariable.getId());
        assertEquals(standardVariable.getEnumerations().get(0).getName(), randNum);
        assertEquals(standardVariable.getEnumerations().get(0).getDescription(), "Low " + randNum);
        
        //delete
        enumerations = standardVariable.getEnumerations();
        for (Enumeration enumeration : enumerations) {
            ontologyService.deleteStandardVariableValidValue(standardVariable.getId(), enumeration.getId());
        }
        standardVariable = ontologyService.getStandardVariable(standardVariable.getId());
        assertNull(standardVariable.getEnumerations());
    }
}
