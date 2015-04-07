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

import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TreeTableNode;
import junit.framework.Assert;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.easymock.EasyMock;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GermplasmTreeControllerTest extends AbstractBaseControllerIntegrationTest {

    private static final String ROOT_FOLDER_NAME = "Lists";

	/** The controller. */
    @Autowired
    private GermplasmTreeController controller;
    
    private static final Integer LIST_USER_ID = 1;
    private static final String TEST_GERMPLASM_LIST = "Test Germplasm List";
    private static final String LISTS = "LISTS";
    private static final String NAME_NOT_UNIQUE = "Name not unique";
    
    /** The Constant LIST_1. */
    private static final GermplasmList LIST_1 = 
            new GermplasmList(1, "List 1", null, "LST", LIST_USER_ID, "List 1", null, 1);
    
    /** The Constant LIST_2. */
    private static final GermplasmList LIST_2 = 
            new GermplasmList(2, "List 2", null, "LST", LIST_USER_ID, "List 2", null, 1);
    
    /** The Constant LOCAL_LIST_3. */
    private static final GermplasmList LIST_3 = 
            new GermplasmList(3, "List 3", null, "LST", LIST_USER_ID, "List 3", null, 1);
    
    /** The Constant LOCAL_GERMPLASM_LIST_TEST_DATA. */
    private static final List<GermplasmList> GERMPLASM_LIST_TEST_DATA = 
            Arrays.asList(LIST_1, LIST_2, LIST_3);
    
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
        
        assertEquals(1, treeNodes.size());
        assertEquals(LISTS, treeNodes.get(0).getKey());
    }
    
    /**
     * Test expand germplasm tree local.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExpandGermplasmTreeLocal() throws Exception {
        String jsonResponse = controller.expandGermplasmTree(LISTS, "0");
        assertNotNull(jsonResponse);
        TreeNode[] treeNodes = objectMapper.readValue(jsonResponse, TreeNode[].class);
        
        assertEquals(3, treeNodes.length);
        for (int i = 0; i < 3; i++) {
            assertEquals(String.valueOf((i+1)), treeNodes[i].getKey());
            assertEquals("List " + (i+1), treeNodes[i].getTitle());
        }
    }
    
    /**
     * Test expand germplasm node.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExpandGermplasmNode() throws Exception {
        String jsonResponse = controller.expandGermplasmTree("List 1", "0");
        assertEquals("[]", jsonResponse);
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
        
        assertEquals("The number of root nodes should be 1", 1, numberOfRootNodes);
        assertEquals("The first root node should be have an id of "+LISTS,
        		LISTS,rootNodes.get(0).getId());
    }

	private void mockUserDataManagerAndItsMethods() throws MiddlewareQueryException {
		UserDataManager userDataManager = mock(UserDataManager.class);
		when(userDataManager.getUserById(LIST_USER_ID)).thenReturn(null);
		ReflectionTestUtils.setField(controller, "userDataManager"
                , userDataManager, UserDataManager.class);
	}

	private void mockGermplasmListManagerAndItsMethods() throws MiddlewareQueryException {
		GermplasmListManager germplasmListManager = mock(GermplasmListManager.class);
		when(germplasmListManager.getAllTopLevelListsBatched(
        		GermplasmTreeController.BATCH_SIZE))
        			.thenReturn(GERMPLASM_LIST_TEST_DATA);
        when(germplasmListManager.getGermplasmListByParentFolderIdBatched(anyInt(), 
        		anyInt())).thenReturn(EMPTY_GERMPLASM_LIST_TEST_DATA);
        List<UserDefinedField> userDefinedFields = createGermplasmListUserDefinedFields();
        when(germplasmListManager.getGermplasmListTypes())
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
		folderType.setFcode("FOLDER");
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
		assertTrue(TEST_GERMPLASM_LIST+" should not have children",
				anyChildNode.getNumOfChildren().equals("0"));
		
		TreeTableNode localRootNode = new TreeTableNode(
	    		LISTS, AppConstants.LISTS.getString(), 
	    		null, null, null, null, "1");
		controller.markIfHasChildren(localRootNode);
		assertFalse(AppConstants.LISTS.getString()+
				" should have children",localRootNode.getNumOfChildren().equals("0"));
	}
	
	@Test
	public void testGetGermplasmListChildren() throws MiddlewareQueryException {
		TreeTableNode anyChildNode = new TreeTableNode(
	    		Integer.toString(EasyMock.anyInt()), 
	    		TEST_GERMPLASM_LIST, 
	    		null, null, null, null, "1");
		List<GermplasmList> germplasmListChildren = 
				controller.getGermplasmListChildren(anyChildNode.getId());
		assertTrue(TEST_GERMPLASM_LIST+" should have "+germplasmListChildren.size()
				+" number of children",germplasmListChildren.size()==
				EMPTY_GERMPLASM_LIST_TEST_DATA.size());
		
		TreeTableNode localRootNode = new TreeTableNode(
				LISTS, AppConstants.LISTS.getString(), 
	    		null, null, null, null, "1");
		germplasmListChildren = 
				controller.getGermplasmListChildren(localRootNode.getId());
		assertTrue(AppConstants.LISTS.getString()+
				" should have "+germplasmListChildren.size()
				+" number of children",germplasmListChildren.size()==
				GERMPLASM_LIST_TEST_DATA.size());
	}
	
	@Test
	public void testGetGermplasmListFolderChildNodes() throws MiddlewareQueryException {
		TreeTableNode localRootNode = new TreeTableNode(
				LISTS, AppConstants.LISTS.getString(), 
	    		null, null, null, null, "1");
		List<TreeTableNode> childNodes = controller.getGermplasmListFolderChildNodes(localRootNode);
		assertTrue(AppConstants.LISTS.getString()+
					" should have "+GERMPLASM_LIST_TEST_DATA.size()+" children",
					localRootNode.getNumOfChildren().equals(
							Integer.toString(GERMPLASM_LIST_TEST_DATA.size())));
		assertTrue(AppConstants.LISTS.getString()+
				" should have "+childNodes.size()+" children",!childNodes.isEmpty());
		
		TreeTableNode anyChildNode = new TreeTableNode(
	    		Integer.toString(EasyMock.anyInt()), 
	    		TEST_GERMPLASM_LIST, 
	    		null, null, null, null, "1");
		childNodes = controller.getGermplasmListFolderChildNodes(anyChildNode);
		assertTrue(TEST_GERMPLASM_LIST+
					" should have children",anyChildNode.getNumOfChildren().equals("0"));
		assertTrue(TEST_GERMPLASM_LIST+
				" should have no children",childNodes.isEmpty());
	}
	
	@Test
	public void testGetGermplasmListFolderChildNodesById() throws MiddlewareQueryException {
		List<TreeTableNode> childNodes = controller.getGermplasmListFolderChildNodes(LISTS);
		assertTrue(AppConstants.LISTS.getString()+
				" should have "+childNodes.size()+" children",!childNodes.isEmpty());
		
		childNodes = controller.getGermplasmListFolderChildNodes(Integer.toString(EasyMock.anyInt()));
		assertTrue(TEST_GERMPLASM_LIST+
				" should have no children",childNodes.isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testExpandGermplasmListFolder() throws MiddlewareQueryException {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.expandGermplasmListFolder(LISTS, model);
		List<TreeTableNode> treeNodes = (List<TreeTableNode>)
        		model.get(GermplasmTreeController.GERMPLASM_LIST_CHILD_NODES);
        assertEquals("The number of children under the node with id LOCAL should be 3",
        		3, treeNodes.size());
        for (TreeTableNode treeTableNode : treeNodes) {
        	assertEquals("The parent id of "+treeTableNode.getName()+
        			" should be "+LISTS, LISTS, treeTableNode.getParentId());
		}
	}
	
	@Test
	public void testCheckIfUniqueUsingTheRootFolderAsAnInput() throws MiddlewareQueryException{
		try {
			controller.checkIfUnique(ROOT_FOLDER_NAME);
		} catch (MiddlewareException e) {
			Assert.assertEquals(NAME_NOT_UNIQUE, e.getMessage());
		}
	}
	
	@Test
	public void testCheckIfUniqueUsingExistingListAsInput() throws MiddlewareQueryException{
		GermplasmListManager germplasmListManager = mock(GermplasmListManager.class);
		String folderName = "Sample Folder Name";
		
		when(germplasmListManager.getGermplasmListByName(folderName, 0, 1, null))
        			.thenReturn(GERMPLASM_LIST_TEST_DATA);
        ReflectionTestUtils.setField(controller, "germplasmListManager"
                , germplasmListManager, GermplasmListManager.class);
		try {
			controller.checkIfUnique(folderName);
		} catch (MiddlewareException e) {
			Assert.assertEquals(NAME_NOT_UNIQUE, e.getMessage());
		}
	}
	
	@Test
	public void testIsSimilarToRootFolderNameReturnsTrueForItemNameSimilarToRootFolder(){
		Assert.assertTrue("Expecting to return true for item name similar to \"Lists\" ", controller.isSimilarToRootFolderName(ROOT_FOLDER_NAME));
	}
	
	@Test
	public void testIsSimilarToRootFolderNameReturnsTrueForItemNameNotSimilarToRootFolder(){
		Assert.assertFalse("Expecting to return true for item name not similar to \"Lists\" ", controller.isSimilarToRootFolderName("Dummy Folder Name"));
	}
	
	public void testSaveCrossesListIfStudyIsNull() throws MiddlewareQueryException{
		UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(null);
		GermplasmTreeController treeController = new GermplasmTreeController();
		FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		Integer germplasmListId = 1;
		Integer userId = 9;
		List<ListDataProject> listDataProject = new ArrayList<ListDataProject>();
		Integer crossesId = 5;
		Mockito.when(fieldbookMiddlewareService.saveOrUpdateListDataProject(Mockito.anyInt(), Mockito.any(GermplasmListType.class), Mockito.anyInt(), Mockito.anyListOf(ListDataProject.class), Mockito.anyInt())).thenReturn(crossesId);
		treeController.setUserSelection(userSelection);
		treeController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		int savedCrossesId = treeController.saveCrossesList(germplasmListId, listDataProject, userId);
		Assert.assertEquals("Should return the same crosses Id as per simulation of saving", crossesId.intValue(), savedCrossesId);
	}
	
	@Test
	public void testSaveCrossesListIfStudyIsNotNull() throws MiddlewareQueryException{
		UserSelection userSelection = new UserSelection();
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		Integer studyId = 99;
		studyDetails.setId(studyId);
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		GermplasmTreeController treeController = new GermplasmTreeController();
		FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		Integer germplasmListId = 1;
		Integer userId = 9;
		List<ListDataProject> listDataProject = new ArrayList<ListDataProject>();
		Integer crossesId = 88;
		Mockito.when(fieldbookMiddlewareService.saveOrUpdateListDataProject(Mockito.anyInt(), Mockito.any(GermplasmListType.class), Mockito.anyInt(), Mockito.anyListOf(ListDataProject.class), Mockito.anyInt())).thenReturn(crossesId);
		treeController.setUserSelection(userSelection);
		treeController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		int savedCrossesId = treeController.saveCrossesList(germplasmListId, listDataProject, userId);
		Assert.assertEquals("Should return the same crosses Id as per simulation of saving", crossesId.intValue(), savedCrossesId);
	}
}
