
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TreeTableNode;

public class TreeViewUtilTest {

	private static final String PROGRAM_UUID = "abcd-efgh-12345";

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
	
	// Study Tree test data
	private static final FolderReference FOLDER = new FolderReference(1, "Folder 1", "Folder 1 Description", PROGRAM_UUID);
	private static final StudyReference TRIAL = new StudyReference(100, "F1 Trial", "Trial Description", PROGRAM_UUID, StudyTypeDto.getTrialDto());
	private static final StudyReference NURSERY = new StudyReference(101, "F2 Nusery", "Nursery Description", PROGRAM_UUID, StudyTypeDto.getNurseryDto());
	private static final List<Reference> STUDY_REFERENCES = Arrays.asList(FOLDER, TRIAL, NURSERY);
	
	private static GermplasmListManager germplasmListManager;
	private static GermplasmDataManager germplasmDataManager;
	private static UserDataManager userDataManager;
	private static List<UserDefinedField> userDefinedFields;

	@BeforeClass
	public static void setupClass() {
		TreeViewUtilTest.mockGermplasmListManagerAndSomeOfItsMethods();
		TreeViewUtilTest.mockUserDataManagerAndSomeOfItsMethods();
	}

	private static void mockUserDataManagerAndSomeOfItsMethods() {
		TreeViewUtilTest.userDataManager = Mockito.mock(UserDataManager.class);
		Mockito.when(TreeViewUtilTest.userDataManager.getUserById(TreeViewUtilTest.LIST_USER_ID)).thenReturn(null);
	}

	private static void mockGermplasmListManagerAndSomeOfItsMethods() {
		TreeViewUtilTest.germplasmListManager = Mockito.mock(GermplasmListManager.class);
		TreeViewUtilTest.germplasmDataManager = Mockito.mock(GermplasmDataManager.class);
		TreeViewUtilTest.userDefinedFields = TreeViewUtilTest.createGermplasmListUserDefinedFields();
		Mockito.when(TreeViewUtilTest.germplasmDataManager.getUserDefinedFieldByFieldTableNameAndType(Mockito.isA(String.class),
				Mockito.isA(String.class))).thenReturn(TreeViewUtilTest.userDefinedFields);
	}

	@Test
	public void testConvertGermplasmListToTreeTableNodes() {

		String folderParentId = "123";

		List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>(TreeViewUtilTest.GERMPLASM_LIST_TEST_DATA);
		List<TreeTableNode> treeTableNodes =
				TreeViewUtil.convertGermplasmListToTreeTableNodes(folderParentId, germplasmLists, TreeViewUtilTest.germplasmListManager,
						TreeViewUtilTest.germplasmDataManager);

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
			String parentId = TreeViewUtil.getParentId(folderParentId, germplasmList);
			Assert.assertEquals("The parent id should be " + parentId, parentId, treeTableNode.getParentId());
			String type = this.getType(germplasmList.getType());
			Assert.assertEquals("The type should be " + type, type, treeTableNode.getType());
			String owner = this.getOwnerListName(germplasmList.getUserId());
			Assert.assertEquals("The owner should be " + owner, owner, treeTableNode.getOwner());
			Assert.assertEquals("The number of children should be 0", "0", treeTableNode.getNumOfChildren());
		}
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
		String folderParentId = "123";
		List<TreeTableNode> treeTableNodes =
				TreeViewUtil.convertGermplasmListToTreeTableNodes(folderParentId, null,
						TreeViewUtilTest.germplasmListManager, TreeViewUtilTest.germplasmDataManager);
		Assert.assertTrue("The list should be empty", treeTableNodes.isEmpty());
	}

	@Test
	public void testConvertGermplasmListToTreeTableNodes_EmptyList() {
		String folderParentId = "123";
		List<TreeTableNode> treeTableNodes =
				TreeViewUtil.convertGermplasmListToTreeTableNodes(folderParentId, new ArrayList<GermplasmList>(),
						TreeViewUtilTest.germplasmListManager, TreeViewUtilTest.germplasmDataManager);
		Assert.assertTrue("The list should be empty", treeTableNodes.isEmpty());
	}
	
	@Test
	public void testConvertStudyFolderReferencesToTreeView() {
		final boolean isLazy = false;
		final boolean isFolderOnly = false;
		final List<TreeNode> treeNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(STUDY_REFERENCES, isLazy, isFolderOnly);
		Assert.assertEquals(STUDY_REFERENCES.size(), treeNodes.size());
		assertTreeNodeExpectedValues(FOLDER, treeNodes.get(0), AppConstants.FOLDER_ICON_PNG.getString(), isLazy);
		assertTreeNodeExpectedValues(TRIAL, treeNodes.get(1), AppConstants.STUDY_ICON_PNG.getString(), isLazy);
		assertTreeNodeExpectedValues(NURSERY, treeNodes.get(2), AppConstants.STUDY_ICON_PNG.getString(), isLazy);
	}
	
	@Test
	public void testConvertStudyFolderReferencesToTreeView_IsFolderOnly() {
		final boolean isLazy = false;
		final boolean isFolderOnly = true;
		final List<TreeNode> treeNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(STUDY_REFERENCES, isLazy, isFolderOnly);
		Assert.assertEquals(STUDY_REFERENCES.size()-2, treeNodes.size());
		assertTreeNodeExpectedValues(FOLDER, treeNodes.get(0), AppConstants.FOLDER_ICON_PNG.getString(), isLazy);
	}
	
	@Test
	public void testConvertStudyFolderReferencesToTreeView_IsLazy() {
		final boolean isLazy = true;
		final boolean isFolderOnly = false;
		final List<TreeNode> treeNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(STUDY_REFERENCES, isLazy, isFolderOnly);
		Assert.assertEquals(STUDY_REFERENCES.size(), treeNodes.size());
		assertTreeNodeExpectedValues(FOLDER, treeNodes.get(0), AppConstants.FOLDER_ICON_PNG.getString(), isLazy);
		assertTreeNodeExpectedValues(TRIAL, treeNodes.get(1), AppConstants.STUDY_ICON_PNG.getString(), isLazy);
		assertTreeNodeExpectedValues(NURSERY, treeNodes.get(2), AppConstants.STUDY_ICON_PNG.getString(), isLazy);
	}

	private void assertTreeNodeExpectedValues(final Reference source, final TreeNode node, final Object icon, final Boolean isLazy) {
		Assert.assertEquals(source.getId().toString(), node.getKey());
		Assert.assertEquals(source.getName(), node.getTitle());
		Assert.assertEquals(source.isFolder(), node.getIsFolder());
		Assert.assertEquals(isLazy, node.getIsLazy());
		Assert.assertEquals(source.getProgramUUID(), node.getProgramUUID());
		Assert.assertEquals(icon, node.getIcon());
		if (!source.isFolder()) {
			final StudyReference studyReference = (StudyReference) source;
			Assert.assertEquals(studyReference.getStudyType().getName(), node.getType());
		}
	}
}
