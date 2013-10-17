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

import java.util.Arrays;
import java.util.List;

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


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/Fieldbook-servlet.xml"})
public class GermplasmTreeControllerTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private GermplasmTreeController controller;
    
    private static final GermplasmList localList1 = new GermplasmList(1, "Local List 1", null, "LIST", null, "Local List 1", null, 1);
    private static final GermplasmList localList2 = new GermplasmList(1, "Local List 2", null, "LIST", null, "Local List 2", null, 1);
    private static final GermplasmList localList3 = new GermplasmList(1, "Local List 3", null, "LIST", null, "Local List 3", null, 1);
    
    private static final GermplasmList centralList1 = new GermplasmList(1, "Central List 1", null, "LIST", null, "Central List 1", null, 1);
    private static final GermplasmList centralList2 = new GermplasmList(1, "Central List 2", null, "LIST", null, "Central List 2", null, 1);
    private static final GermplasmList centralList3 = new GermplasmList(1, "Central List 3", null, "LIST", null, "Central List 3", null, 1);
    
    private static final List<GermplasmList> localGermplasmListTestData = Arrays.asList(localList1, localList2, localList3);
    private static final List<GermplasmList> centralGermplasmListTestData = Arrays.asList(centralList1, centralList2, centralList3);
    
    private static final String rootResult = "[{\"title\":\"My List\",\"key\":\"LOCAL\",\"isFolder\":true,\"isLazy\":true,\"addClass\":\"fbtree-root-header\",\"icon\":\"false\"},{\"title\":\"Shared List\",\"key\":\"CENTRAL\",\"isFolder\":true,\"isLazy\":true,\"addClass\":\"fbtree-root-header\",\"icon\":\"false\"}]";
    private static final String localJsonResult = "[{\"title\":\"Local List 1\",\"key\":\"1\",\"isFolder\":false,\"isLazy\":true,\"addClass\":null,\"icon\":null},{\"title\":\"Local List 2\",\"key\":\"1\",\"isFolder\":false,\"isLazy\":true,\"addClass\":null,\"icon\":null},{\"title\":\"Local List 3\",\"key\":\"1\",\"isFolder\":false,\"isLazy\":true,\"addClass\":null,\"icon\":null}]";
    private static final String centralJsonResult = "[{\"title\":\"Central List 1\",\"key\":\"1\",\"isFolder\":false,\"isLazy\":true,\"addClass\":null,\"icon\":null},{\"title\":\"Central List 2\",\"key\":\"1\",\"isFolder\":false,\"isLazy\":true,\"addClass\":null,\"icon\":null},{\"title\":\"Central List 3\",\"key\":\"1\",\"isFolder\":false,\"isLazy\":true,\"addClass\":null,\"icon\":null}]";
    
    @Before
    public void setup() throws Exception {
    }
    
    @Test
    public void testLoadInitialTree() throws Exception {
        String jsonResponse = controller.loadInitialGermplasmTree();
        Assert.assertEquals(rootResult, jsonResponse);
    }
    
    @Test
    public void testExpandGermplasmTreeLocal() throws Exception {
        GermplasmListManager germplasmListManager = EasyMock.createMock(GermplasmListManager.class);
        EasyMock.expect(germplasmListManager.getAllTopLevelListsBatched(50, Database.LOCAL)).andReturn(localGermplasmListTestData);
        EasyMock.replay(germplasmListManager);
        
        ReflectionTestUtils.setField(controller, "germplasmListManager", germplasmListManager, GermplasmListManager.class);

        String jsonResponse = controller.expandGermplasmTree("LOCAL");
        Assert.assertEquals(localJsonResult, jsonResponse);
    }
    
    @Test
    public void testExpandGermplasmTreeCentral() throws Exception {
        GermplasmListManager germplasmListManager = EasyMock.createMock(GermplasmListManager.class);
        EasyMock.expect(germplasmListManager.getAllTopLevelListsBatched(50, Database.CENTRAL)).andReturn(centralGermplasmListTestData);
        EasyMock.replay(germplasmListManager);
        
        ReflectionTestUtils.setField(controller, "germplasmListManager", germplasmListManager, GermplasmListManager.class);

        String jsonResponse = controller.expandGermplasmTree("CENTRAL");
        Assert.assertEquals(centralJsonResult, jsonResponse);
    }
}
