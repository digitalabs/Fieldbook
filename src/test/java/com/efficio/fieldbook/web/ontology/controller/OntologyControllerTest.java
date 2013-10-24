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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TraitReference;
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
        PropertyReference propRef1 = new PropertyReference((i*10)+count++, i + " Prop 1");
        PropertyReference propRef2 = new PropertyReference((i*10)+count++, i + " Prop 2");
        PropertyReference propRef3 = new PropertyReference((i*10)+count++, i + " Prop 3");
        
        propRef1.setStandardVariables(getDummyStandardVariableReference(1));
        propRef2.setStandardVariables(getDummyStandardVariableReference(2));
        propRef3.setStandardVariables(getDummyStandardVariableReference(3));
        
        propList.add(propRef1);
        propList.add(propRef2);
        propList.add(propRef3);
        return propList;
    }
    
    private List<TraitReference> getDummyData(){
        List<TraitReference> refList = new ArrayList<TraitReference>();
        TraitReference ref1 = new TraitReference(1, "Test 1");
        TraitReference ref2 = new TraitReference(2, "Test 2");
        TraitReference ref3 = new TraitReference(3, "Test 3");
        
                
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
                
                List<TraitReference> traitRefList = getDummyData();
                //form.setTraitReferenceList(traitRefList);
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
        
}
