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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermProperty;
import org.generationcp.middleware.domain.oms.TraitClassReference;
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

import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.pojos.treeview.TreeNode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class OntologyControllerTest extends AbstractJUnit4SpringContextTests {
    
    private static final Logger LOG = LoggerFactory.getLogger(OntologyControllerTest.class);
    
    @Autowired
    OntologyService ontologyService;

    @Autowired
    private OntologyController controller;
    
    private List<StandardVariableReference> getDummyStandardVariableReference(int i){
        List<StandardVariableReference> list = new ArrayList<StandardVariableReference>();
        int count = 1;
        StandardVariableReference ref1 = new StandardVariableReference((i*100)+count++, i + " Variable 1");
        StandardVariableReference ref2 = new StandardVariableReference((i*100)+count++, i + " Variable 2");
        StandardVariableReference ref3 = new StandardVariableReference((i*100)+count++, i + " Variable 3");
        
        
       list.add(ref1);
       list.add(ref2);
       list.add(ref3);
        return list;
    }
    
    private List<PropertyReference> getDummyPropertyReference(int i){
        List<PropertyReference> propList = new ArrayList<PropertyReference>();
        int count = 1;
        PropertyReference propRef1 = new PropertyReference((i * 10) + count++, i + " Prop 1");
        PropertyReference propRef2 = new PropertyReference((i * 10) + count++, i + " Prop 2");
        PropertyReference propRef3 = new PropertyReference((i * 10) + count++, i + " Prop 3");
        
        propRef1.setStandardVariables(getDummyStandardVariableReference(1));
        propRef2.setStandardVariables(getDummyStandardVariableReference(2));
        propRef3.setStandardVariables(getDummyStandardVariableReference(3));
        
        propList.add(propRef1);
        propList.add(propRef2);
        propList.add(propRef3);
        return propList;
    }
    
    private List<TraitClassReference> getDummyData(){
        List<TraitClassReference> refList = new ArrayList<TraitClassReference>();
        TraitClassReference ref1 = new TraitClassReference(1, "Test 1");
        TraitClassReference ref2 = new TraitClassReference(2, "Test 2");
        TraitClassReference ref3 = new TraitClassReference(3, "Test 3");
        
        List<TraitClassReference> refList1 = new ArrayList<TraitClassReference>();
        TraitClassReference ref11 = new TraitClassReference(1, "Test 1 Child");
        TraitClassReference ref21 = new TraitClassReference(2, "Test 2 Child");
        TraitClassReference ref31 = new TraitClassReference(3, "Test 3 Child");
        
        ref11.setProperties(getDummyPropertyReference(10));
        ref21.setProperties(getDummyPropertyReference(20));
        ref31.setProperties(getDummyPropertyReference(30));
        
        refList1.add(ref11);
        refList1.add(ref21);
        refList1.add(ref31);
        
        ref1.setTraitClassChildren(refList1);
                
        ref1.setProperties(getDummyPropertyReference(1));
        ref2.setProperties(getDummyPropertyReference(2));
        ref3.setProperties(getDummyPropertyReference(3));
        
        refList.add(ref1);
        refList.add(ref2);
        refList.add(ref3);
        return refList;
    }
    
        @Before
        public void setUp() {
          
        }
        
    @Test
    public void testOntologyTreeJsonData(){

        try{
            
            List<TraitClassReference> traitRefList = getDummyData();
            //form.setTraitClassReferenceList(traitRefList);
            List<TreeNode> rootTree = convertJsonStringToMap(TreeViewUtil.convertOntologyTraitsToJson(traitRefList));
            //assertEquals(mainInfo.getFileIsValid(), false);
            //assertEquals(jsonMap.get('1'), false);
            //System.out.println(jsonMap.get("key"));
            assertEquals("Trait Class", rootTree.get(0).getTitle());
            assertEquals(3, rootTree.get(0).getChildren().size());
            
            assertEquals("Test 1", rootTree.get(0).getChildren().get(0).getTitle());
            assertEquals("Test 2", rootTree.get(0).getChildren().get(1).getTitle());
            assertEquals("Test 3", rootTree.get(0).getChildren().get(2).getTitle());
            
            assertEquals("1 Prop 1", rootTree.get(0).getChildren().get(0).getChildren().get(0).getTitle());
            assertEquals("1 Prop 2", rootTree.get(0).getChildren().get(0).getChildren().get(1).getTitle());
            assertEquals("1 Prop 3", rootTree.get(0).getChildren().get(0).getChildren().get(2).getTitle());
            
            assertEquals("2 Prop 1", rootTree.get(0).getChildren().get(1).getChildren().get(0).getTitle());
            assertEquals("2 Prop 2", rootTree.get(0).getChildren().get(1).getChildren().get(1).getTitle());
            assertEquals("2 Prop 3", rootTree.get(0).getChildren().get(1).getChildren().get(2).getTitle());
            
            assertEquals("1 Variable 1", rootTree.get(0).getChildren().get(0).getChildren().get(0).getChildren().get(0).getTitle());
            assertEquals("1 Variable 2", rootTree.get(0).getChildren().get(0).getChildren().get(0).getChildren().get(1).getTitle());
            assertEquals("1 Variable 3", rootTree.get(0).getChildren().get(0).getChildren().get(0).getChildren().get(2).getTitle());
            
            assertEquals("2 Variable 1", rootTree.get(0).getChildren().get(0).getChildren().get(1).getChildren().get(0).getTitle());
            assertEquals("2 Variable 2", rootTree.get(0).getChildren().get(0).getChildren().get(1).getChildren().get(1).getTitle());
            assertEquals("2 Variable 3", rootTree.get(0).getChildren().get(0).getChildren().get(1).getChildren().get(2).getTitle());
            
            assertEquals("3 Variable 1", rootTree.get(0).getChildren().get(0).getChildren().get(2).getChildren().get(0).getTitle());
            assertEquals("3 Variable 2", rootTree.get(0).getChildren().get(0).getChildren().get(2).getChildren().get(1).getTitle());
            assertEquals("3 Variable 3", rootTree.get(0).getChildren().get(0).getChildren().get(2).getChildren().get(2).getTitle());
        }catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
                  
    }
    
    protected List<TreeNode> convertJsonStringToMap(String json) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<TreeNode> lcd = mapper.readValue(json, new TypeReference<List<TreeNode>>() {});

        return lcd;
    }
    
    @Test
    public void testSaveNewTerm() {
        
        Term term = null;
        
        //create new trait class w/o desc
        String name = "Test Trait Class " + new Random().nextInt(10000);
        String definition = "";
        term = testSaveNewTerm("TraitClass", name, definition, "", "", "", "", "", "");
        assertEquals(term.getName(), name);
        assertEquals(term.getDefinition(), name);
        
        //create new property
        name = "Test Property " + new Random().nextInt(10000);
        definition = "propertyDesc";
        term = testSaveNewTerm("Property", "", "", name, definition, "", "", "", "");
        assertEquals(term.getName(), name);
        assertEquals(term.getDefinition(), definition);
        
        //create new method w/o desc
        name = "Test Method " + new Random().nextInt(10000);
        definition = "";
        term = testSaveNewTerm("Method", "", "", "", "", name, definition, "", "");
        assertEquals(term.getName(), name);
        assertEquals(term.getDefinition(), name);
        
        //create new scale 
        name = "Test Scale " + new Random().nextInt(10000);
        definition = "test";
        term = testSaveNewTerm("Scale", "", "", "", "", "", "", name, definition);
        assertEquals(term.getName(), name);
        assertEquals(term.getDefinition(), definition);
    } 
    
    private Term testSaveNewTerm(String combo,
            String traitClass, String traitClassDescription,
            String property, String propertyDescription, 
            String method, String methodDescription,
            String scale, String scaleDescription) {
        Term term = null;
        try {
            if (combo.equals("Property")) {
                if (propertyDescription == null || propertyDescription == "") {
                    propertyDescription = property;
                }
                term = ontologyService.addTerm(property, propertyDescription, CvId.PROPERTIES);
            } else if (combo.equals("Method")) {
                if (methodDescription == null || methodDescription == "") {
                    methodDescription = method;
                }
                term = ontologyService.addTerm(method, methodDescription, CvId.METHODS);
            } else if (combo.equals("Scale")) {
                if (scaleDescription == null || scaleDescription == "") {
                    scaleDescription = scale;
                }
                term = ontologyService.addTerm(scale, scaleDescription, CvId.SCALES);
            } else {
                if (traitClassDescription == null || traitClassDescription == "") {
                    traitClassDescription = traitClass;
                }
                term = ontologyService.addTraitClass(traitClass, traitClassDescription, TermId.ONTOLOGY_TRAIT_CLASS.getId()).getTerm();
            }
            return term;
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        return term;
    }
    
    @Test
    public void testSaveNewVariable() {
        try {
            List<TermProperty> termProperties = new ArrayList<TermProperty>();
            termProperties.add(new TermProperty(1, TermId.CROP_ONTOLOGY_ID.getId(), "CO:12345", 0));
            Term property = new Term(100, "PROPERTY", "PROPERTY DEF", null, termProperties);
            Term scale = new Term(200, "SCALE", "SCALE DEF", null, null);
            Term method = new Term(300, "METHOD", "METHOD DEF", null, null);
            Term dataType = new Term(400, "DATA TYPE", "DATA TYPE DEF", null, null);
            Term storedIn = new Term(1010, "STORED IN", "STORED IN DEF", null, null);
            Term traitClass = new Term(600, "TRAIT CLASS", "TRAIT CLASS DEF", null, null);
            
            StandardVariable standardVariable = new StandardVariable();
            standardVariable.setName("TestVariable" + new Random().nextInt(10000));
            standardVariable.setDescription("Test Desc");
            standardVariable.setProperty(property);
            standardVariable.setMethod(method);
            standardVariable.setScale(scale);
            standardVariable.setDataType(dataType);
            standardVariable.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
            standardVariable.setIsA(traitClass);
            standardVariable.setStoredIn(storedIn);
            standardVariable.setCropOntologyId("CO:1200");
            ontologyService.addStandardVariable(standardVariable);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
}
