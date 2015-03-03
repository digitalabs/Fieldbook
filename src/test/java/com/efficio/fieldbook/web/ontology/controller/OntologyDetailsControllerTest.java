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
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.common.bean.PropertyTree;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.ontology.form.OntologyDetailsForm;

public class OntologyDetailsControllerTest  extends AbstractBaseControllerIntegrationTest {
	
	public static final Logger log = LoggerFactory.getLogger(OntologyDetailsControllerTest.class);

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
        
        OntologyService ontologyService = Mockito.mock(OntologyService.class);
        StandardVariable stdvar = createStandardVariableTestData();
        Mockito.when(ontologyService.getStandardVariable(8050)).thenReturn(stdvar);
        Mockito.when(ontologyService.countProjectsByVariable(8050)).thenReturn(123456L);
        Mockito.when(ontologyService.countExperimentsByVariable(8050, 1010)).thenReturn(789000L);
        controller.setOntologyService(ontologyService);
        String result = controller.getOntologyDetails(variableId, form, model);
        
        Assert.assertEquals(OntologyDetailsController.DETAILS_TEMPLATE, result);
        Assert.assertEquals(stdvar, form.getVariable());
    }
    
    public void testGetPropertiesBySettingsMode() {
    	
    	List<StandardVariableReference> filteredVariables = new ArrayList<StandardVariableReference>();
    	List<TraitClassReference> ontologyTree = createOntologyTree();
    	StandardVariable sv1 = createStandardVariable(1);
    	StandardVariable sv2 = createStandardVariable(2);
    	StandardVariable sv3 = createStandardVariable(3);
    	
    	FieldbookService fieldbookService = EasyMock.createMock(FieldbookService.class);
    	OntologyService ontologyService = EasyMock.createMock(OntologyService.class);
    	try {
			EasyMock.expect(fieldbookService.filterStandardVariablesForSetting(1, new ArrayList<SettingDetail>())).andReturn(filteredVariables);
			EasyMock.expect(ontologyService.getAllTraitGroupsHierarchy(true)).andReturn(ontologyTree);
			EasyMock.expect(ontologyService.getStandardVariable(1)).andReturn(sv1);
			EasyMock.expect(ontologyService.getStandardVariable(2)).andReturn(sv2);
			EasyMock.expect(ontologyService.getStandardVariable(3)).andReturn(sv3);
		} catch (MiddlewareQueryException e) {
			log.error(e.getMessage());
		}
    	
    	List<PropertyTree> result = controller.getPropertiesBySettingsGroup(1, 101, false);
    	
    }
    
    // TODO : make this a common fixture for Ontology browsing screens
    private List<TraitClassReference> createOntologyTree() {
		List<TraitClassReference> tree = new ArrayList<TraitClassReference>();
		return tree;
	}

	private StandardVariable createStandardVariable(int i) {
		StandardVariable sv = new StandardVariable();
		sv.setId(i);
		return sv;
	}

	/**
     * Creates the standard variable test data.
     *
     * @return the standard variable
     */
    private StandardVariable createStandardVariableTestData() {
        Term property = new Term(100, "PROPERTY", "PROPERTY DEF");
        Term scale = new Term(200, "SCALE", "SCALE DEF");
        Term method = new Term(300, "METHOD", "METHOD DEF");
        Term dataType = new Term(400, "DATA TYPE", "DATA TYPE DEF");
        Term storedIn = new Term(1010, "STORED IN", "STORED IN DEF");
        Term traitClass = new Term(600, "TRAIT CLASS", "TRAIT CLASS DEF");
        VariableConstraints constraints = new VariableConstraints(10.0, 50.0);
        List<Enumeration> enumerations = new ArrayList<Enumeration>();
        enumerations.add(new Enumeration(1, "ENUM1", "ENUM1 DESC", 0));
        enumerations.add(new Enumeration(2, "ENUM1", "ENUM2 DESC", 0));
        enumerations.add(new Enumeration(3, "ENUM1", "ENUM3 DESC", 0));
        enumerations.add(new Enumeration(4, "ENUM1", "ENUM4 DESC", 0));
        
        StandardVariable stdvar = 
                new StandardVariable(property, scale, method, dataType, storedIn, traitClass, 
                        PhenotypicType.TRIAL_DESIGN);
        stdvar.setConstraints(constraints);
        stdvar.setEnumerations(enumerations);
        stdvar.setName("VARIABLE1");
        stdvar.setDescription("VARIABLE DESCRIPTION");
        
        return stdvar;
    }
    
    
}
