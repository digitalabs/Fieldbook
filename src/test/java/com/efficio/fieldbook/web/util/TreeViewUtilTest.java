
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.pojos.treeview.TreeTableNode;

public class TreeViewUtilTest extends AbstractBaseIntegrationTest {

	private static final Integer LIST_USER_ID = 1;

	/** The Constant LIST_1. */
	private static final GermplasmList LIST_1 = new GermplasmList(1, "List 1", null, "FOLDER", TreeViewUtilTest.LIST_USER_ID,
			"List Description 1", null, 1);

	/** The Constant LIST_2. */
	private static final GermplasmList LIST_2 = new GermplasmList(2, "List 2", null, null, TreeViewUtilTest.LIST_USER_ID, null, null, 1);

	/** The Constant LIST_3. */
	private static final GermplasmList LIST_3 = new GermplasmList(3, "List 3", null, "LST", TreeViewUtilTest.LIST_USER_ID,
			"A very long long long long long description ", TreeViewUtilTest.LIST_1, 1);

	/** The Constant GERMPLASM_LIST_TEST_DATA. */
	private static final List<GermplasmList> GERMPLASM_LIST_TEST_DATA = Arrays.asList(TreeViewUtilTest.LIST_1, TreeViewUtilTest.LIST_2,
			TreeViewUtilTest.LIST_3);

	private static final List<GermplasmList> EMPTY_GERMPLASM_LIST_TEST_DATA = new ArrayList<GermplasmList>();

	private static final Integer ROOT_STUDY = 1;
	private static final String DUMMY_PROGRAM_UUID = "1234567890";

	private static final FolderReference FOLDER_1 = new FolderReference(TreeViewUtilTest.ROOT_STUDY, 2, "Folder_1", "Folder 1",
			TreeViewUtilTest.DUMMY_PROGRAM_UUID);

	private static final FolderReference TRIAL_OF_FOLDER_1 = new FolderReference(TreeViewUtilTest.FOLDER_1.getId(), 3, "Trial_of_Folder_1",
			"Trial of Folder 1", TreeViewUtilTest.DUMMY_PROGRAM_UUID);

	private static final List<GermplasmList> NULL_GERMPLASM_LIST_TEST_DATA = null;
	private static GermplasmListManager germplasmListManager;
	private static UserDataManager userDataManager;
	private static List<UserDefinedField> userDefinedFields;

	@BeforeClass
	public static void setupClass() throws MiddlewareQueryException {
		TreeViewUtilTest.mockGermplasmListManagerAndSomeOfItsMethods();
		TreeViewUtilTest.mockUserDataManagerAndSomeOfItsMethods();
	}

	private static void mockUserDataManagerAndSomeOfItsMethods() throws MiddlewareQueryException {
		TreeViewUtilTest.userDataManager = Mockito.mock(UserDataManager.class);
		Mockito.when(TreeViewUtilTest.userDataManager.getUserById(TreeViewUtilTest.LIST_USER_ID)).thenReturn(null);
	}

	private static void mockGermplasmListManagerAndSomeOfItsMethods() throws MiddlewareQueryException {
		TreeViewUtilTest.germplasmListManager = Mockito.mock(GermplasmListManager.class);
		TreeViewUtilTest.userDefinedFields = TreeViewUtilTest.createGermplasmListUserDefinedFields();
		Mockito.when(TreeViewUtilTest.germplasmListManager.getGermplasmListTypes()).thenReturn(TreeViewUtilTest.userDefinedFields);
	}

	@Test
	public void testConvertGermplasmListToTreeTableNodes() {
		List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>(TreeViewUtilTest.GERMPLASM_LIST_TEST_DATA);
		List<TreeTableNode> treeTableNodes =
				TreeViewUtil.convertGermplasmListToTreeTableNodes(germplasmLists, TreeViewUtilTest.userDataManager,
						TreeViewUtilTest.germplasmListManager);

		Assert.assertTrue("The list should not be null", treeTableNodes != null);
		Assert.assertTrue("The list should not be empty", !treeTableNodes.isEmpty());
		Assert.assertEquals("The list should have 3 items", germplasmLists.size(), treeTableNodes.size());
		for (TreeTableNode treeTableNode : treeTableNodes) {
			GermplasmList germplasmList = null;
			switch (Integer.parseInt(treeTableNode.getId())) {
				case 1:
					germplasmList = TreeViewUtilTest.LIST_1;
					break;
				case 2:
					germplasmList = TreeViewUtilTest.LIST_2;
					break;
				case 3:
					germplasmList = TreeViewUtilTest.LIST_3;
					break;
			}
			Assert.assertEquals("The id should be " + germplasmList.getId(), Integer.toString(germplasmList.getId()), treeTableNode.getId());
			Assert.assertEquals("The name should be " + germplasmList.getName(), germplasmList.getName(), treeTableNode.getName());
			String descriptionForDisplay = this.getDescriptionForDisplay(germplasmList.getDescription());
			Assert.assertEquals("The description should be " + descriptionForDisplay, descriptionForDisplay, treeTableNode.getDescription());
			String isFolder = "0";
			if (germplasmList.getType() != null && "FOLDER".equals(germplasmList.getType())) {
				isFolder = "1";
			}
			Assert.assertTrue("The tree table node should be a folder", isFolder.equals(treeTableNode.getIsFolder()));
			int noOfEntries = germplasmList.getListData().size();
			String noOfEntriesDisplay = noOfEntries == 0 ? "" : String.valueOf(noOfEntries);
			Assert.assertEquals("The no of entries should be " + noOfEntriesDisplay, noOfEntriesDisplay, treeTableNode.getNoOfEntries());
			String parentId = TreeViewUtilTest.getParentId(germplasmList);
			Assert.assertEquals("The parent id should be " + parentId, parentId, treeTableNode.getParentId());
			String type = this.getType(germplasmList.getType());
			Assert.assertEquals("The type should be " + type, type, treeTableNode.getType());
			String owner = this.getOwnerListName(germplasmList.getUserId());
			Assert.assertEquals("The owner should be " + owner, owner, treeTableNode.getOwner());
		}
	}

	public static String getParentId(GermplasmList germplasmList) {
		Integer parentId = germplasmList.getParentId();
		if (parentId == null) {
			return "LISTS";
		}
		return String.valueOf(parentId);
	}

	private String getOwnerListName(Integer userId) {
		return "";
	}

	private String getType(String type) {
		if (type != null) {
			for (UserDefinedField listType : TreeViewUtilTest.userDefinedFields) {
				if (type.equals(listType.getFcode())) {
					return listType.getFname();
				}
			}
		}
		return "Germplasm List";
	}

	private String getDescriptionForDisplay(String germplasmDescription) {
		String description = "-";
		if (germplasmDescription != null) {
			description = germplasmDescription.replaceAll("<", "&lt;");
			description = description.replaceAll(">", "&gt;");
			if (description.length() > 27) {
				description = description.substring(0, 27) + "...";
			}
		}
		return description;
	}

	private static List<UserDefinedField> createGermplasmListUserDefinedFields() {
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
	public void testConvertGermplasmListToTreeTableNodes_NullList() {
		List<TreeTableNode> treeTableNodes =
				TreeViewUtil.convertGermplasmListToTreeTableNodes(TreeViewUtilTest.NULL_GERMPLASM_LIST_TEST_DATA,
						TreeViewUtilTest.userDataManager, TreeViewUtilTest.germplasmListManager);
		Assert.assertTrue("The list should be empty", treeTableNodes.isEmpty());
	}

	@Test
	public void testConvertGermplasmListToTreeTableNodes_EmptyList() {
		List<TreeTableNode> treeTableNodes =
				TreeViewUtil.convertGermplasmListToTreeTableNodes(TreeViewUtilTest.EMPTY_GERMPLASM_LIST_TEST_DATA,
						TreeViewUtilTest.userDataManager, TreeViewUtilTest.germplasmListManager);
		Assert.assertTrue("The list should be empty", treeTableNodes.isEmpty());
	}

	@Test
	public void convertReferenceToFolderReference() {
		List<Reference> folders = this.createReferenceListTestData();
		List<FolderReference> folRefs = TreeViewUtil.convertReferenceToFolderReference(folders);
		Assert.assertNotNull(folRefs);
		Assert.assertEquals(1, folRefs.size());
		FolderReference folderReference = folRefs.get(0);
		Assert.assertEquals(TreeViewUtilTest.TRIAL_OF_FOLDER_1.getProgramUUID(), folderReference.getProgramUUID());
		Assert.assertEquals(TreeViewUtilTest.TRIAL_OF_FOLDER_1.getId(), folderReference.getId());
		Assert.assertEquals(TreeViewUtilTest.TRIAL_OF_FOLDER_1.getName(), folderReference.getName());
	}

	private List<Reference> createReferenceListTestData() {
		List<Reference> references = new ArrayList<Reference>();
		references.add(TreeViewUtilTest.TRIAL_OF_FOLDER_1);
		return references;
	}

}
