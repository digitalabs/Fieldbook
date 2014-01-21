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
import java.util.List;

import org.easymock.EasyMock;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermProperty;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.efficio.fieldbook.web.ontology.form.OntologyDetailsForm;

/**
 * The Class OntologyDetailsControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class OntologyDetailsControllerTest  extends AbstractJUnit4SpringContextTests {

    /** The controller. */
    @Autowired
    private OntologyDetailsController controller;
    
    /**
     * Test get ontology details.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetOntologyDetails() throws Exception {
        OntologyDetailsForm form = new OntologyDetailsForm();
        Model model = new ExtendedModelMap();
        int variableId = 8050;
        
        OntologyService ontologyService = EasyMock.createMock(OntologyService.class);
        StandardVariable stdvar = createStandardVariableTestData();
        EasyMock.expect(ontologyService.getStandardVariable(8050)).andReturn(stdvar);
        EasyMock.expect(ontologyService.countProjectsByVariable(8050)).andReturn(123456L);
        EasyMock.expect(ontologyService.countExperimentsByVariable(8050, 1010)).andReturn(789000L);
        EasyMock.replay(ontologyService);
        ReflectionTestUtils.setField(controller, "ontologyService", ontologyService, OntologyService.class);
        
        String result = controller.getOntologyDetails(variableId, form, model);
        
        Assert.assertEquals(OntologyDetailsController.DETAILS_TEMPLATE, result);
        Assert.assertEquals(stdvar, form.getVariable());
        Assert.assertEquals("123,456", form.getProjectCount());
        Assert.assertEquals("789,000", form.getObservationCount());
    }
    
    /**
     * Creates the standard variable test data.
     *
     * @return the standard variable
     */
    private StandardVariable createStandardVariableTestData() {
        List<TermProperty> termProperties = new ArrayList<TermProperty>();
        termProperties.add(new TermProperty(1, TermId.CROP_ONTOLOGY_ID.getId(), "CO:12345", 0));
        Term property = new Term(100, "PROPERTY", "PROPERTY DEF", null, termProperties);
        Term scale = new Term(200, "SCALE", "SCALE DEF", null, null);
        Term method = new Term(300, "METHOD", "METHOD DEF", null, null);
        Term dataType = new Term(400, "DATA TYPE", "DATA TYPE DEF", null, null);
        Term storedIn = new Term(1010, "STORED IN", "STORED IN DEF", null, null);
        Term traitClass = new Term(600, "TRAIT CLASS", "TRAIT CLASS DEF", null, null);
        VariableConstraints constraints = new VariableConstraints(10.0, 50.0);
        List<Enumeration> enumerations = new ArrayList<Enumeration>();
        enumerations.add(new Enumeration(1, "ENUM1", "ENUM1 DESC", 0));
        enumerations.add(new Enumeration(2, "ENUM1", "ENUM2 DESC", 0));
        enumerations.add(new Enumeration(3, "ENUM1", "ENUM3 DESC", 0));
        enumerations.add(new Enumeration(4, "ENUM1", "ENUM4 DESC", 0));
        
        StandardVariable stdvar = new StandardVariable(property, scale, method, dataType, storedIn, traitClass, 
                PhenotypicType.TRIAL_DESIGN, constraints, enumerations);
        stdvar.setName("VARIABLE1");
        stdvar.setDescription("VARIABLE DESCRIPTION");
        
        return stdvar;
    }
}
