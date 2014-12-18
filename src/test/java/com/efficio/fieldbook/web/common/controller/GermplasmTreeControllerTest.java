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
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.easymock.EasyMock;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TreeTableNode;

public class GermplasmTreeControllerTest extends AbstractBaseControllerIntegrationTest {

    /** The controller. */
    @Autowired
    private GermplasmTreeController controller;
    
    private static final Integer LIST_USER_ID = 1;
    private static final String TEST_GERMPLASM_LIST = "Test Germplasm List";
    private static final String LOCAL = "LOCAL";
    private static final String CENTRAL = "CENTRAL";
    
    /** The Constant LOCAL_LIST_1. */
    private static final GermplasmList LOCAL_LIST_1 = 
            new GermplasmList(-1, "Local List 1", null, "LST", LIST_USER_ID, "Local List 1", null, 1);
    
    /** The Constant LOCAL_LIST_2. */
    private static final GermplasmList LOCAL_LIST_2 = 
            new GermplasmList(-2, "Local List 2", null, "LST", LIST_USER_ID, "Local List 2", null, 1);
    
    /** The Constant LOCAL_LIST_3. */
    private static final GermplasmList LOCAL_LIST_3 = 
            new GermplasmList(-3, "Local List 3", null, "LST", LIST_USER_ID, "Local List 3", null, 1);
    
    /** The Constant CENTRAL_LIST_1. */
    private static final GermplasmList CENTRAL_LIST_1 = 
            new GermplasmList(1, "Central List 1", null, "LST", LIST_USER_ID, "Central List 1", null, 1);
    
    /** The Constant CENTRAL_LIST_2. */
    private static final GermplasmList CENTRAL_LIST_2 = 
            new GermplasmList(2, "Central List 2", null, "LST", LIST_USER_ID, "Central List 2", null, 1);
    
    /** The Constant CENTRAL_LIST_3. */
    private static final GermplasmList CENTRAL_LIST_3 = 
            new GermplasmList(3, "Central List 3", null, "LST", LIST_USER_ID, "Central List 3", null, 1);
    
    /** The Constant LOCAL_GERMPLASM_LIST_TEST_DATA. */
    private static final List<GermplasmList> LOCAL_GERMPLASM_LIST_TEST_DATA = 
            Arrays.asList(LOCAL_LIST_1, LOCAL_LIST_2, LOCAL_LIST_3);
    
    /** The Constant CENTRAL_GERMPLASM_LIST_TEST_DATA. */
    private static final List<GermplasmList> CENTRAL_GERMPLASM_LIST_TEST_DATA = 
            Arrays.asList(CENTRAL_LIST_1, CENTRAL_LIST_2, CENTRAL_LIST_3);
    
    private static final List<GermplasmList> EMPTY_GERMPLASM_LIST_TEST_DATA = 
            new ArrayList<GermplasmList>();

	/** The object mapper. */
    private ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
    	mockGermplasmListManagerAndItsMethods();
    	mockUserDataManagerAndItsMethods();
    }
    
    /**
     * Test load initial tree.
     *
     * @throws Exception the exception
     */
    @Test
    public void testLoadInitialTree() throws Exception {
        String jsonResponse = controller.loadInitialGermplasmTree("0");
        
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
        String jsonResponse = controller.expandGermplasmTree("LOCAL", "0");
        Assert.assertNotNull(jsonResponse);
        TreeNode[] treeNodes = objectMapper.readValue(jsonResponse, TreeNode[].class);
        
        Assert.assertEquals(3, treeNodes.length);
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(String.valueOf((i+1)*-1), treeNodes[i].getKey());
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
        String jsonResponse = controller.expandGermplasmTree("CENTRAL", "0");
        Assert.assertNotNull(jsonResponse);
        TreeNode[] treeNodes = objectMapper.readValue(jsonResponse, TreeNode[].class);
        
        Assert.assertEquals(3, treeNodes.length);
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(String.valueOf(i+1), treeNodes[i].getKey());
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
        String jsonResponse = controller.expandGermplasmTree("Local List 1", "0");
        Assert.assertEquals("[]", jsonResponse);
    }
    
    /**
     * Test load initial tree table.
     *
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
	@Test
    public void testLoadInitialTreeTable() throws Exception {
        ExtendedModelMap model = new ExtendedModelMap();
    	controller.loadInitialGermplasmTreeTable(model);
        List<TreeTableNode> treeNodes = (List<TreeTableNode>)
        		model.get(GermplasmTreeController.GERMPLASM_LIST_ROOT_NODES);
        int numberOfRootNodes = 0;
        List<TreeTableNode> rootNodes = new ArrayList<TreeTableNode>();
        for (TreeTableNode treeTableNode : treeNodes) {
			if(treeTableNode.getParentId()==null) {
				rootNodes.add(treeTableNode);
				numberOfRootNodes++;
			}
		}
        
        Assert.assertEquals("The number of root nodes should be 2", 2, numberOfRootNodes);
        Assert.assertEquals("The first root node should be have an id of LOCAL",
        		"LOCAL",rootNodes.get(0).getId());
        Assert.assertEquals("The first root node should be have an id of CENTRAL", 
        		"CENTRAL", rootNodes.get(1).getId());
    }

	private void mockUserDataManagerAndItsMethods() throws MiddlewareQueryException {
		UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		Mockito.when(userDataManager.getUserById(LIST_USER_ID)).thenReturn(null);
		ReflectionTestUtils.setField(controller, "userDataManager"
                , userDataManager, UserDataManager.class);
	}

	private void mockGermplasmListManagerAndItsMethods() throws MiddlewareQueryException {
		GermplasmListManager germplasmListManager = Mockito.mock(GermplasmListManager.class);
		Mockito.when(germplasmListManager.getAllTopLevelListsBatched(
    			GermplasmTreeController.BATCH_SIZE, Database.CENTRAL))
        			.thenReturn(CENTRAL_GERMPLASM_LIST_TEST_DATA);
		Mockito.when(germplasmListManager.getAllTopLevelListsBatched(
        		GermplasmTreeController.BATCH_SIZE, Database.LOCAL))
        			.thenReturn(LOCAL_GERMPLASM_LIST_TEST_DATA);
        Mockito.when(germplasmListManager.getGermplasmListByParentFolderIdBatched(Mockito.anyInt(), 
        		Mockito.anyInt())).thenReturn(EMPTY_GERMPLASM_LIST_TEST_DATA);
        List<UserDefinedField> userDefinedFields = createGermplasmListUserDefinedFields();
        Mockito.when(germplasmListManager.getGermplasmListTypes())
        	.thenReturn(userDefinedFields);
        ReflectionTestUtils.setField(controller, "germplasmListManager"
                , germplasmListManager, GermplasmListManager.class);
	}

	private List<UserDefinedField> createGermplasmListUserDefinedFields() {
		List<UserDefinedField> userDefinedFields = new ArrayList<UserDefinedField>();
		UserDefinedField listType = new UserDefinedField();
		listType.setFcode("LST");
		listType.setFname("LIST FOLDER");
		userDefinedFields.add(listType);
		UserDefinedField folderType = new UserDefinedField();
		folderType.setFcode("LST");
		folderType.setFname("FOLDER");
		userDefinedFields.add(folderType);
		return userDefinedFields;
	}
	
	@Test
	public void testMarkIfHasChildren() throws MiddlewareQueryException {
		TreeTableNode anyChildNode = new TreeTableNode(
	    		Integer.toString(EasyMock.anyInt()), 
	    		TEST_GERMPLASM_LIST, 
	    		null, null, null, null, "1");
		controller.markIfHasChildren(anyChildNode);
		Assert.assertTrue(TEST_GERMPLASM_LIST+" should not have children",
				anyChildNode.getHasChildren().equals("0"));
		
		TreeTableNode localRootNode = new TreeTableNode(
	    		LOCAL, AppConstants.GERMPLASM_LIST_LOCAL.getString(), 
	    		null, null, null, null, "1");
		controller.markIfHasChildren(localRootNode);
		Assert.assertTrue(AppConstants.GERMPLASM_LIST_LOCAL.getString()+
				" should have children",localRootNode.getHasChildren().equals("1"));
	}
	
	@Test
	public void testGetGermplasmListChildren() throws MiddlewareQueryException {
		TreeTableNode anyChildNode = new TreeTableNode(
	    		Integer.toString(EasyMock.anyInt()), 
	    		TEST_GERMPLASM_LIST, 
	    		null, null, null, null, "1");
		List<GermplasmList> germplasmListChildren = 
				controller.getGermplasmListChildren(anyChildNode.getId());
		Assert.assertTrue(TEST_GERMPLASM_LIST+" should have "+germplasmListChildren.size()
				+" number of children",germplasmListChildren.size()==
				EMPTY_GERMPLASM_LIST_TEST_DATA.size());
		
		TreeTableNode localRootNode = new TreeTableNode(
				LOCAL, AppConstants.GERMPLASM_LIST_LOCAL.getString(), 
	    		null, null, null, null, "1");
		germplasmListChildren = 
				controller.getGermplasmListChildren(localRootNode.getId());
		Assert.assertTrue(AppConstants.GERMPLASM_LIST_LOCAL.getString()+
				" should have "+germplasmListChildren.size()
				+" number of children",germplasmListChildren.size()==
				LOCAL_GERMPLASM_LIST_TEST_DATA.size());
	}
	
	@Test
	public void testGetGermplasmListFolderChildNodes() throws MiddlewareQueryException {
		TreeTableNode localRootNode = new TreeTableNode(
				LOCAL, AppConstants.GERMPLASM_LIST_LOCAL.getString(), 
	    		null, null, null, null, "1");
		List<TreeTableNode> childNodes = controller.getGermplasmListFolderChildNodes(localRootNode);
		Assert.assertTrue(AppConstants.GERMPLASM_LIST_LOCAL.getString()+
					" should have children",localRootNode.getHasChildren().equals("1"));
		Assert.assertTrue(AppConstants.GERMPLASM_LIST_LOCAL.getString()+
				" should have "+childNodes.size()+" children",!childNodes.isEmpty());
		
		TreeTableNode anyChildNode = new TreeTableNode(
	    		Integer.toString(EasyMock.anyInt()), 
	    		TEST_GERMPLASM_LIST, 
	    		null, null, null, null, "1");
		childNodes = controller.getGermplasmListFolderChildNodes(anyChildNode);
		Assert.assertTrue(TEST_GERMPLASM_LIST+
					" should have children",anyChildNode.getHasChildren().equals("0"));
		Assert.assertTrue(TEST_GERMPLASM_LIST+
				" should have no children",childNodes.isEmpty());
	}
	
	@Test
	public void testGetGermplasmListFolderChildNodesById() throws MiddlewareQueryException {
		List<TreeTableNode> childNodes = controller.getGermplasmListFolderChildNodes(LOCAL);
		Assert.assertTrue(AppConstants.GERMPLASM_LIST_LOCAL.getString()+
				" should have "+childNodes.size()+" children",!childNodes.isEmpty());
		
		childNodes = controller.getGermplasmListFolderChildNodes(Integer.toString(EasyMock.anyInt()));
		Assert.assertTrue(TEST_GERMPLASM_LIST+
				" should have no children",childNodes.isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testExpandGermplasmListFolder() throws MiddlewareQueryException {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.expandGermplasmListFolder(LOCAL, model);
		List<TreeTableNode> treeNodes = (List<TreeTableNode>)
        		model.get(GermplasmTreeController.GERMPLASM_LIST_CHILD_NODES);
        Assert.assertEquals("The number of children under the node with id LOCAL should be 3",
        		3, treeNodes.size());
        for (TreeTableNode treeTableNode : treeNodes) {
        	Assert.assertEquals("The parent id of "+treeTableNode.getName()+
        			" should be "+LOCAL, LOCAL, treeTableNode.getParentId());
		}
        
        model = new ExtendedModelMap();
		controller.expandGermplasmListFolder(CENTRAL, model);
		treeNodes = (List<TreeTableNode>)
        		model.get(GermplasmTreeController.GERMPLASM_LIST_CHILD_NODES);
        Assert.assertEquals("The number of children under the node with id CENTRAL should be 3",
        		3, treeNodes.size());
        for (TreeTableNode treeTableNode : treeNodes) {
        	Assert.assertEquals("The parent id of "+treeTableNode.getName()+
        			" should be "+CENTRAL, CENTRAL, treeTableNode.getParentId());
		}
	}
}
