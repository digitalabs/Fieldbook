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
package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.easymock.EasyMock;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.efficio.fieldbook.web.common.controller.GermplasmTreeController;
import com.efficio.pojos.treeview.TreeNode;

/**
 * The Class GermplasmTreeControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class GermplasmTreeControllerTest extends AbstractJUnit4SpringContextTests {

    /** The controller. */
    @Autowired
    private GermplasmTreeController controller;
    
    /** The Constant LOCAL_LIST_1. */
    private static final GermplasmList LOCAL_LIST_1 = 
            new GermplasmList(1, "Local List 1", null, "LIST", null, "Local List 1", null, 1);
    
    /** The Constant LOCAL_LIST_2. */
    private static final GermplasmList LOCAL_LIST_2 = 
            new GermplasmList(2, "Local List 2", null, "LIST", null, "Local List 2", null, 1);
    
    /** The Constant LOCAL_LIST_3. */
    private static final GermplasmList LOCAL_LIST_3 = 
            new GermplasmList(3, "Local List 3", null, "LIST", null, "Local List 3", null, 1);
    
    /** The Constant CENTRAL_LIST_1. */
    private static final GermplasmList CENTRAL_LIST_1 = 
            new GermplasmList(4, "Central List 1", null, "LIST", null, "Central List 1", null, 1);
    
    /** The Constant CENTRAL_LIST_2. */
    private static final GermplasmList CENTRAL_LIST_2 = 
            new GermplasmList(5, "Central List 2", null, "LIST", null, "Central List 2", null, 1);
    
    /** The Constant CENTRAL_LIST_3. */
    private static final GermplasmList CENTRAL_LIST_3 = 
            new GermplasmList(6, "Central List 3", null, "LIST", null, "Central List 3", null, 1);
    
    /** The Constant LOCAL_GERMPLASM_LIST_TEST_DATA. */
    private static final List<GermplasmList> LOCAL_GERMPLASM_LIST_TEST_DATA = 
            Arrays.asList(LOCAL_LIST_1, LOCAL_LIST_2, LOCAL_LIST_3);
    
    /** The Constant CENTRAL_GERMPLASM_LIST_TEST_DATA. */
    private static final List<GermplasmList> CENTRAL_GERMPLASM_LIST_TEST_DATA = 
            Arrays.asList(CENTRAL_LIST_1, CENTRAL_LIST_2, CENTRAL_LIST_3);
    
    /** The object mapper. */
    private ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
    }
    
    /**
     * Test load initial tree.
     *
     * @throws Exception the exception
     */
    @Test
    public void testLoadInitialTree() throws Exception {
        String jsonResponse = controller.loadInitialGermplasmTree();
        
        List<TreeNode> treeNodes = objectMapper.readValue(
                jsonResponse, new TypeReference<List<TreeNode>>(){});
        
        Assert.assertEquals(2, treeNodes.size());
        Assert.assertEquals("LOCAL", treeNodes.get(0).getKey());
        Assert.assertEquals("CENTRAL", treeNodes.get(1).getKey());
    }
    
    /**
     * Test expand germplasm tree local.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExpandGermplasmTreeLocal() throws Exception {
        GermplasmListManager germplasmListManager = EasyMock.createMock(GermplasmListManager.class);
        EasyMock.expect(germplasmListManager.getAllTopLevelListsBatched(50, Database.LOCAL))
                            .andReturn(LOCAL_GERMPLASM_LIST_TEST_DATA);
        EasyMock.replay(germplasmListManager);
        
        ReflectionTestUtils.setField(controller, "germplasmListManager"
                , germplasmListManager, GermplasmListManager.class);

        String jsonResponse = controller.expandGermplasmTree("LOCAL", "0");
        Assert.assertNotNull(jsonResponse);
        TreeNode[] treeNodes = objectMapper.readValue(jsonResponse, TreeNode[].class);
        
        Assert.assertEquals(3, treeNodes.length);
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(String.valueOf(i+1), treeNodes[i].getKey());
            Assert.assertEquals("Local List " + (i+1), treeNodes[i].getTitle());
        }
    }
    
    /**
     * Test expand germplasm tree central.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExpandGermplasmTreeCentral() throws Exception {
        GermplasmListManager germplasmListManager = EasyMock.createMock(GermplasmListManager.class);
        EasyMock.expect(germplasmListManager.getAllTopLevelListsBatched(50, Database.CENTRAL))
                                    .andReturn(CENTRAL_GERMPLASM_LIST_TEST_DATA);
        EasyMock.replay(germplasmListManager);
        
        ReflectionTestUtils.setField(controller, "germplasmListManager"
                , germplasmListManager, GermplasmListManager.class);

        String jsonResponse = controller.expandGermplasmTree("CENTRAL", "0");
        Assert.assertNotNull(jsonResponse);
        TreeNode[] treeNodes = objectMapper.readValue(jsonResponse, TreeNode[].class);
        
        Assert.assertEquals(3, treeNodes.length);
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(String.valueOf(i+4), treeNodes[i].getKey());
            Assert.assertEquals("Central List " + (i+1), treeNodes[i].getTitle());
        }
    }
    
    /**
     * Test expand germplasm node.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExpandGermplasmNode() throws Exception {
        GermplasmListManager germplasmListManager = EasyMock.createMock(GermplasmListManager.class);
        List<GermplasmList> emptyList = new ArrayList<GermplasmList>();
        EasyMock.expect(germplasmListManager.getGermplasmListByParentFolderIdBatched(1, 50))
                                .andReturn(emptyList);
        EasyMock.replay(germplasmListManager);
        
        ReflectionTestUtils.setField(controller, "germplasmListManager"
                , germplasmListManager, GermplasmListManager.class);

        String jsonResponse = controller.expandGermplasmTree("Local List 1", "0");
        Assert.assertEquals("[]", jsonResponse);
    }
}
