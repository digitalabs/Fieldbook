/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TreeTableNode;

public class GermplasmTreeControllerTestIT extends AbstractBaseControllerIntegrationTest {

	private static final String ROOT_FOLDER_NAME = "Lists";

	/** The controller. */
	@Autowired
	private GermplasmTreeController controller;

	private static final Integer LIST_USER_ID = 1;
	private static final String TEST_GERMPLASM_LIST = "Test Germplasm List";
	private static final String LISTS = "LISTS";
	private static final String NAME_NOT_UNIQUE = "Name not unique";

	/** The Constant LIST_1. */
	private static final GermplasmList LIST_1 = new GermplasmList(1, "List 1", null, "LST", GermplasmTreeControllerTestIT.LIST_USER_ID,
			"List 1", null, 1);

	/** The Constant LIST_2. */
	private static final GermplasmList LIST_2 = new GermplasmList(2, "List 2", null, "LST", GermplasmTreeControllerTestIT.LIST_USER_ID,
			"List 2", null, 1);

	/** The Constant LOCAL_LIST_3. */
	private static final GermplasmList LIST_3 = new GermplasmList(3, "List 3", null, "LST", GermplasmTreeControllerTestIT.LIST_USER_ID,
			"List 3", null, 1);

	/** The Constant LOCAL_GERMPLASM_LIST_TEST_DATA. */
	private static final List<GermplasmList> GERMPLASM_LIST_TEST_DATA = Arrays.asList(GermplasmTreeControllerTestIT.LIST_1,
			GermplasmTreeControllerTestIT.LIST_2, GermplasmTreeControllerTestIT.LIST_3);

	private static final List<GermplasmList> EMPTY_GERMPLASM_LIST_TEST_DATA = new ArrayList<GermplasmList>();

	/** The object mapper. */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		this.mockGermplasmListManagerAndItsMethods();
		this.mockUserDataManagerAndItsMethods();
	}

	/**
	 * Test load initial tree.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testLoadInitialTree() throws Exception {
		String jsonResponse = this.controller.loadInitialGermplasmTree("0");

		List<TreeNode> treeNodes = this.objectMapper.readValue(jsonResponse, new TypeReference<List<TreeNode>>() {
		});

		Assert.assertEquals(1, treeNodes.size());
		Assert.assertEquals(GermplasmTreeControllerTestIT.LISTS, treeNodes.get(0).getKey());
	}

	/**
	 * Test expand germplasm tree local.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testExpandGermplasmTreeLocal() throws Exception {
		String jsonResponse = this.controller.expandGermplasmTree(GermplasmTreeControllerTestIT.LISTS, "0");
		Assert.assertNotNull(jsonResponse);
		TreeNode[] treeNodes = this.objectMapper.readValue(jsonResponse, TreeNode[].class);

		Assert.assertEquals(3, treeNodes.length);
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals(String.valueOf(i + 1), treeNodes[i].getKey());
			Assert.assertEquals("List " + (i + 1), treeNodes[i].getTitle());
		}
	}

	/**
	 * Test expand germplasm node.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testExpandGermplasmNode() throws Exception {
		String jsonResponse = this.controller.expandGermplasmTree("List 1", "0");
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
		this.controller.loadInitialGermplasmTreeTable(model);
		List<TreeTableNode> treeNodes = (List<TreeTableNode>) model.get(GermplasmTreeController.GERMPLASM_LIST_ROOT_NODES);
		int numberOfRootNodes = 0;
		List<TreeTableNode> rootNodes = new ArrayList<TreeTableNode>();
		for (TreeTableNode treeTableNode : treeNodes) {
			if (treeTableNode.getParentId() == null) {
				rootNodes.add(treeTableNode);
				numberOfRootNodes++;
			}
		}

		Assert.assertEquals("The number of root nodes should be 1", 1, numberOfRootNodes);
		Assert.assertEquals("The first root node should be have an id of " + GermplasmTreeControllerTestIT.LISTS,
				GermplasmTreeControllerTestIT.LISTS, rootNodes.get(0).getId());
	}

	private void mockUserDataManagerAndItsMethods() throws MiddlewareQueryException {
		UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		Mockito.when(userDataManager.getUserById(GermplasmTreeControllerTestIT.LIST_USER_ID)).thenReturn(null);
		ReflectionTestUtils.setField(this.controller, "userDataManager", userDataManager, UserDataManager.class);
	}

	private void mockGermplasmListManagerAndItsMethods() throws MiddlewareQueryException {
		GermplasmListManager germplasmListManager = Mockito.mock(GermplasmListManager.class);
		Mockito.when(germplasmListManager.getAllTopLevelListsBatched(GermplasmTreeController.BATCH_SIZE)).thenReturn(
				GermplasmTreeControllerTestIT.GERMPLASM_LIST_TEST_DATA);
		Mockito.when(germplasmListManager.getGermplasmListByParentFolderIdBatched(Matchers.anyInt(), Matchers.anyInt())).thenReturn(
				GermplasmTreeControllerTestIT.EMPTY_GERMPLASM_LIST_TEST_DATA);
		List<UserDefinedField> userDefinedFields = this.createGermplasmListUserDefinedFields();
		Mockito.when(germplasmListManager.getGermplasmListTypes()).thenReturn(userDefinedFields);
		ReflectionTestUtils.setField(this.controller, "germplasmListManager", germplasmListManager, GermplasmListManager.class);
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
		TreeTableNode anyChildNode =
				new TreeTableNode(Integer.toString(EasyMock.anyInt()), GermplasmTreeControllerTestIT.TEST_GERMPLASM_LIST, null, null, null,
						null, "1");
		this.controller.markIfHasChildren(anyChildNode);
		Assert.assertTrue(GermplasmTreeControllerTestIT.TEST_GERMPLASM_LIST + " should not have children", anyChildNode.getNumOfChildren()
				.equals("0"));

		TreeTableNode localRootNode =
				new TreeTableNode(GermplasmTreeControllerTestIT.LISTS, AppConstants.LISTS.getString(), null, null, null, null, "1");
		this.controller.markIfHasChildren(localRootNode);
		Assert.assertFalse(AppConstants.LISTS.getString() + " should have children", localRootNode.getNumOfChildren().equals("0"));
	}

	@Test
	public void testGetGermplasmListChildren() throws MiddlewareQueryException {
		TreeTableNode anyChildNode =
				new TreeTableNode(Integer.toString(EasyMock.anyInt()), GermplasmTreeControllerTestIT.TEST_GERMPLASM_LIST, null, null, null,
						null, "1");
		List<GermplasmList> germplasmListChildren = this.controller.getGermplasmListChildren(anyChildNode.getId());
		Assert.assertTrue(GermplasmTreeControllerTestIT.TEST_GERMPLASM_LIST + " should have " + germplasmListChildren.size()
				+ " number of children",
				germplasmListChildren.size() == GermplasmTreeControllerTestIT.EMPTY_GERMPLASM_LIST_TEST_DATA.size());

		TreeTableNode localRootNode =
				new TreeTableNode(GermplasmTreeControllerTestIT.LISTS, AppConstants.LISTS.getString(), null, null, null, null, "1");
		germplasmListChildren = this.controller.getGermplasmListChildren(localRootNode.getId());
		Assert.assertTrue(AppConstants.LISTS.getString() + " should have " + germplasmListChildren.size() + " number of children",
				germplasmListChildren.size() == GermplasmTreeControllerTestIT.GERMPLASM_LIST_TEST_DATA.size());
	}

	@Test
	public void testGetGermplasmListFolderChildNodes() throws MiddlewareQueryException {
		TreeTableNode localRootNode =
				new TreeTableNode(GermplasmTreeControllerTestIT.LISTS, AppConstants.LISTS.getString(), null, null, null, null, "1");
		List<TreeTableNode> childNodes = this.controller.getGermplasmListFolderChildNodes(localRootNode);
		Assert.assertTrue(AppConstants.LISTS.getString() + " should have " + GermplasmTreeControllerTestIT.GERMPLASM_LIST_TEST_DATA.size()
				+ " children",
				localRootNode.getNumOfChildren().equals(Integer.toString(GermplasmTreeControllerTestIT.GERMPLASM_LIST_TEST_DATA.size())));
		Assert.assertTrue(AppConstants.LISTS.getString() + " should have " + childNodes.size() + " children", !childNodes.isEmpty());

		TreeTableNode anyChildNode =
				new TreeTableNode(Integer.toString(EasyMock.anyInt()), GermplasmTreeControllerTestIT.TEST_GERMPLASM_LIST, null, null, null,
						null, "1");
		childNodes = this.controller.getGermplasmListFolderChildNodes(anyChildNode);
		Assert.assertTrue(GermplasmTreeControllerTestIT.TEST_GERMPLASM_LIST + " should have children", anyChildNode.getNumOfChildren()
				.equals("0"));
		Assert.assertTrue(GermplasmTreeControllerTestIT.TEST_GERMPLASM_LIST + " should have no children", childNodes.isEmpty());
	}

	@Test
	public void testGetGermplasmListFolderChildNodesById() throws MiddlewareQueryException {
		List<TreeTableNode> childNodes = this.controller.getGermplasmListFolderChildNodes(GermplasmTreeControllerTestIT.LISTS);
		Assert.assertTrue(AppConstants.LISTS.getString() + " should have " + childNodes.size() + " children", !childNodes.isEmpty());

		childNodes = this.controller.getGermplasmListFolderChildNodes(Integer.toString(EasyMock.anyInt()));
		Assert.assertTrue(GermplasmTreeControllerTestIT.TEST_GERMPLASM_LIST + " should have no children", childNodes.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExpandGermplasmListFolder() throws MiddlewareQueryException {
		ExtendedModelMap model = new ExtendedModelMap();
		this.controller.expandGermplasmListFolder(GermplasmTreeControllerTestIT.LISTS, model);
		List<TreeTableNode> treeNodes = (List<TreeTableNode>) model.get(GermplasmTreeController.GERMPLASM_LIST_CHILD_NODES);
		Assert.assertEquals("The number of children under the node with id LOCAL should be 3", 3, treeNodes.size());
		for (TreeTableNode treeTableNode : treeNodes) {
			Assert.assertEquals("The parent id of " + treeTableNode.getName() + " should be " + GermplasmTreeControllerTestIT.LISTS,
					GermplasmTreeControllerTestIT.LISTS, treeTableNode.getParentId());
		}
	}

	@Test
	public void testCheckIfUniqueUsingTheRootFolderAsAnInput() throws MiddlewareQueryException {
		try {
			this.controller.checkIfUnique(GermplasmTreeControllerTestIT.ROOT_FOLDER_NAME);
		} catch (MiddlewareException e) {
			Assert.assertEquals(GermplasmTreeControllerTestIT.NAME_NOT_UNIQUE, e.getMessage());
		}
	}

	@Test
	public void testCheckIfUniqueUsingExistingListAsInput() throws MiddlewareQueryException {
		GermplasmListManager germplasmListManager = Mockito.mock(GermplasmListManager.class);
		String folderName = "Sample Folder Name";

		Mockito.when(germplasmListManager.getGermplasmListByName(folderName, 0, 1, null)).thenReturn(
				GermplasmTreeControllerTestIT.GERMPLASM_LIST_TEST_DATA);
		ReflectionTestUtils.setField(this.controller, "germplasmListManager", germplasmListManager, GermplasmListManager.class);
		try {
			this.controller.checkIfUnique(folderName);
		} catch (MiddlewareException e) {
			Assert.assertEquals(GermplasmTreeControllerTestIT.NAME_NOT_UNIQUE, e.getMessage());
		}
	}

	@Test
	public void testIsSimilarToRootFolderNameReturnsTrueForItemNameSimilarToRootFolder() {
		Assert.assertTrue("Expecting to return true for item name similar to \"Lists\" ",
				this.controller.isSimilarToRootFolderName(GermplasmTreeControllerTestIT.ROOT_FOLDER_NAME));
	}

	@Test
	public void testIsSimilarToRootFolderNameReturnsTrueForItemNameNotSimilarToRootFolder() {
		Assert.assertFalse("Expecting to return true for item name not similar to \"Lists\" ",
				this.controller.isSimilarToRootFolderName("Dummy Folder Name"));
	}

	public void testSaveCrossesListIfStudyIsNull() throws MiddlewareQueryException {
		UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(null);
		GermplasmTreeController treeController = new GermplasmTreeController();
		FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		Integer germplasmListId = 1;
		Integer userId = 9;
		List<ListDataProject> listDataProject = new ArrayList<ListDataProject>();
		Integer crossesId = 5;
		Mockito.when(
				fieldbookMiddlewareService.saveOrUpdateListDataProject(Matchers.anyInt(), Matchers.any(GermplasmListType.class),
						Matchers.anyInt(), Matchers.anyListOf(ListDataProject.class), Matchers.anyInt())).thenReturn(crossesId);
		treeController.setUserSelection(userSelection);
		treeController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		int savedCrossesId = treeController.saveCrossesList(germplasmListId, listDataProject, userId);
		Assert.assertEquals("Should return the same crosses Id as per simulation of saving", crossesId.intValue(), savedCrossesId);
	}

	@Test
	public void testSaveCrossesListIfStudyIsNotNull() throws MiddlewareQueryException {
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
		Mockito.when(
				fieldbookMiddlewareService.saveOrUpdateListDataProject(Matchers.anyInt(), Matchers.any(GermplasmListType.class),
						Matchers.anyInt(), Matchers.anyListOf(ListDataProject.class), Matchers.anyInt())).thenReturn(crossesId);
		treeController.setUserSelection(userSelection);
		treeController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		int savedCrossesId = treeController.saveCrossesList(germplasmListId, listDataProject, userId);
		Assert.assertEquals("Should return the same crosses Id as per simulation of saving", crossesId.intValue(), savedCrossesId);
	}
}
