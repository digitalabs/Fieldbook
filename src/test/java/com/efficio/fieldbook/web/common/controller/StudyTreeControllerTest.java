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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.pojos.treeview.TreeNode;

public class StudyTreeControllerTest extends AbstractBaseControllerIntegrationTest {

	/** The controller. */
	private StudyTreeController controller;

	@Autowired
	private StudyTreeController studyTreeController;

	private static final String LOCAL = "LOCAL";
	private static final Integer ROOT_STUDY = 1;
	private static final String TYPE_NURSERY = "N";
	private static final String TYPE_TRIAL = "T";

	private static final String DUMMY_PROGRAM_UUID = "1234567890";

	private static final TreeNode ROOT_NURSERIES = new TreeNode(StudyTreeControllerTest.LOCAL, AppConstants.NURSERIES.getString(), true,
			"lead", AppConstants.FOLDER_ICON_PNG.getString(), StudyTreeControllerTest.DUMMY_PROGRAM_UUID);

	private static final TreeNode ROOT_TRIALS = new TreeNode(StudyTreeControllerTest.LOCAL, AppConstants.TRIALS.getString(), true, "lead",
			AppConstants.FOLDER_ICON_PNG.getString(), StudyTreeControllerTest.DUMMY_PROGRAM_UUID);

	private static final FolderReference FOLDER_1 = new FolderReference(StudyTreeControllerTest.ROOT_STUDY, 2, "Folder_1", "Folder 1");

	private static final FolderReference FOLDER_2 = new FolderReference(StudyTreeControllerTest.ROOT_STUDY, 3, "Folder_2", "Folder 1");

	private static final FolderReference SUBFOLDER_OF_FOLDER_2 = new FolderReference(3, 4, "Sub_Folder_of_Folder_2",
			"Sub-Folder of Folder 2");

	private static final FolderReference NURSERY_OF_FOLDER_2 = new FolderReference(3, 5, "Nursery_of_Folder_2", "Nursery of Folder 2");

	private static final FolderReference NURSERY_OF_SUBFOLDER = new FolderReference(4, 6, "Nursery_of_Sub_Folder_2",
			"Nursery of Sub-Folder 2");

	private static final FolderReference TRIAL_OF_FOLDER_2 = new FolderReference(3, 7, "Trial_of_Folder_2", "Trial of Folder 2");

	private static final FolderReference TRIAL_OF_SUBFOLDER = new FolderReference(4, 8, "Trial_of_Sub_Folder_2", "Trial of Sub-Folder 2");

	private static final List<FolderReference> STUDIES = Arrays.asList(StudyTreeControllerTest.FOLDER_1, StudyTreeControllerTest.FOLDER_2,
			StudyTreeControllerTest.SUBFOLDER_OF_FOLDER_2, StudyTreeControllerTest.NURSERY_OF_FOLDER_2,
			StudyTreeControllerTest.NURSERY_OF_SUBFOLDER, StudyTreeControllerTest.TRIAL_OF_FOLDER_2,
			StudyTreeControllerTest.TRIAL_OF_SUBFOLDER);

	private Project selectedProject;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Before
	public void setUp() throws Exception {
		this.controller = Mockito.spy(this.studyTreeController);
		this.selectedProject = this.createProject();
		Mockito.when(this.controller.getCurrentProgramUUID()).thenReturn(this.selectedProject.getUniqueID());
		this.mockFieldbookServiceAndItsMethods();
	}

	private Project createProject() {
		Project project = new Project();
		project.setProjectId(1L);
		project.setProjectName("Project 1");
		project.setUniqueID("12345678");
		return project;
	}

	private void mockFieldbookServiceAndItsMethods() throws MiddlewareQueryException {
		FieldbookService fieldbookService = Mockito.mock(FieldbookService.class);
		Mockito.when(fieldbookService.getRootFolders(Matchers.anyString()))
				.thenReturn(this.getChildren(StudyTreeControllerTest.ROOT_STUDY));
		for (int i = 1; i <= 8; i++) {
			Mockito.when(fieldbookService.getChildrenOfFolder(i, this.selectedProject.getUniqueID())).thenReturn(
					this.getChildrenOfFolder(i));
			Mockito.when(fieldbookService.isStudy(i)).thenReturn(!this.isFolder(i));
			Mockito.when(fieldbookService.getStudyType(i)).thenReturn(this.getStudyType(i));
		}
		ReflectionTestUtils.setField(this.controller, "fieldbookMiddlewareService", fieldbookService, FieldbookService.class);
	}

	private TermId getStudyType(int i) {
		switch (i) {
			case 5:
				return TermId.NURSERY;
			case 6:
				return TermId.NURSERY;
			case 7:
				return TermId.TRIAL;
			case 8:
				return TermId.TRIAL;
			default:
				return null;
		}
	}

	private List<Reference> getChildrenOfFolder(Integer parentId) {
		List<Reference> children = new ArrayList<Reference>();
		for (FolderReference folderReference : StudyTreeControllerTest.STUDIES) {
			if (parentId.equals(folderReference.getParentFolderId())) {
				children.add(folderReference);
			}
		}
		return children;
	}

	private List<FolderReference> getChildren(Integer parentId) {
		List<FolderReference> children = new ArrayList<FolderReference>();
		for (FolderReference folderReference : StudyTreeControllerTest.STUDIES) {
			if (parentId.equals(folderReference.getParentFolderId())) {
				children.add(folderReference);
			}
		}
		return children;
	}

	@Test
	public void testLoadInitialTree_Nurseries() {
		List<TreeNode> root = new ArrayList<TreeNode>();
		root.add(StudyTreeControllerTest.ROOT_NURSERIES);
		try {
			String expectedOutcome = this.objectMapper.writeValueAsString(root);
			String actualOutcome = this.controller.loadInitialTree("", StudyTreeControllerTest.TYPE_NURSERY);
			Assert.assertEquals("The root folder should be " + AppConstants.NURSERIES.getString(), expectedOutcome, actualOutcome);
		} catch (JsonGenerationException e) {
			Assert.fail(e.getMessage());
		} catch (JsonMappingException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testLoadInitialTree_Trials() {
		List<TreeNode> root = new ArrayList<TreeNode>();
		root.add(StudyTreeControllerTest.ROOT_TRIALS);
		try {
			String expectedOutcome = this.objectMapper.writeValueAsString(root);
			String actualOutcome = this.controller.loadInitialTree("", StudyTreeControllerTest.TYPE_TRIAL);
			Assert.assertEquals("The root folder should be " + AppConstants.TRIALS.getString(), expectedOutcome, actualOutcome);
		} catch (JsonGenerationException e) {
			Assert.fail(e.getMessage());
		} catch (JsonMappingException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testExpandTree_AllNurseriesDirectlyUnderRoot() {
		try {
			List<TreeNode> children = this.convertToTreeNodes(this.getChildren(StudyTreeControllerTest.ROOT_STUDY), true, false);
			String expectedOutcome = this.objectMapper.writeValueAsString(children);
			System.out.println(expectedOutcome);
			String actualOutcome = this.controller.expandTree(StudyTreeControllerTest.LOCAL, "0", StudyTreeControllerTest.TYPE_NURSERY);
			System.out.println(actualOutcome);
			Assert.assertEquals("The root folder should be " + AppConstants.NURSERIES.getString(), expectedOutcome, actualOutcome);
		} catch (JsonGenerationException e) {
			Assert.fail(e.getMessage());
		} catch (JsonMappingException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	private List<TreeNode> convertToTreeNodes(List<FolderReference> children, boolean isNursery, boolean isFolderOnly) {
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		for (FolderReference folderReference : children) {
			boolean isFolder = this.isFolder(folderReference.getId());
			if (isFolderOnly && !isFolder) {
				continue;
			}
			if (folderReference.getId() < 5) {
				treeNodes.add(this.createTreeNode(folderReference, isFolder));
			} else if (isNursery && (folderReference.getId() == 5 || folderReference.getId() == 6)) {
				treeNodes.add(this.createTreeNode(folderReference, isFolder));
			} else if (!isNursery && (folderReference.getId() == 6 || folderReference.getId() == 7)) {
				treeNodes.add(this.createTreeNode(folderReference, isFolder));
			}
		}
		return treeNodes;
	}

	private boolean isFolder(Integer id) {
		if (id >= 5) {
			return false;
		}
		return true;
	}

	private TreeNode createTreeNode(FolderReference folderReference, boolean isFolder) {
		TreeNode treeNode = new TreeNode();
		treeNode.setKey(folderReference.getId().toString());
		treeNode.setTitle(folderReference.getName());
		treeNode.setIsFolder(isFolder);
		treeNode.setIsLazy(!this.getChildren(folderReference.getId()).isEmpty());
		if (isFolder) {
			treeNode.setIcon(AppConstants.FOLDER_ICON_PNG.getString());
		} else {
			treeNode.setIcon(AppConstants.STUDY_ICON_PNG.getString());
		}
		return treeNode;
	}

	@Test
	public void testExpandTree_AllTrialsDirectlyUnderRoot() {
		try {
			List<TreeNode> children = this.convertToTreeNodes(this.getChildren(StudyTreeControllerTest.ROOT_STUDY), false, false);
			String expectedOutcome = this.objectMapper.writeValueAsString(children);
			System.out.println(expectedOutcome);
			String actualOutcome = this.controller.expandTree(StudyTreeControllerTest.LOCAL, "0", StudyTreeControllerTest.TYPE_TRIAL);
			System.out.println(actualOutcome);
			Assert.assertEquals("The root folder should be " + AppConstants.TRIALS.getString(), expectedOutcome, actualOutcome);
		} catch (JsonGenerationException e) {
			Assert.fail(e.getMessage());
		} catch (JsonMappingException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testExpandTree_AllTrialsFoldersDirectlyUnderRoot() {
		try {
			List<TreeNode> children = this.convertToTreeNodes(this.getChildren(StudyTreeControllerTest.ROOT_STUDY), true, true);
			String expectedOutcome = this.objectMapper.writeValueAsString(children);
			System.out.println(expectedOutcome);
			String actualOutcome = this.controller.expandTree(StudyTreeControllerTest.LOCAL, "1", StudyTreeControllerTest.TYPE_NURSERY);
			System.out.println(actualOutcome);
			Assert.assertEquals("The root folder should be " + AppConstants.NURSERIES.getString(), expectedOutcome, actualOutcome);
		} catch (JsonGenerationException e) {
			Assert.fail(e.getMessage());
		} catch (JsonMappingException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testExpandTree_AllNurseriesFoldersDirectlyUnderRoot() {
		try {
			List<TreeNode> children = this.convertToTreeNodes(this.getChildren(StudyTreeControllerTest.ROOT_STUDY), false, true);
			String expectedOutcome = this.objectMapper.writeValueAsString(children);
			System.out.println(expectedOutcome);
			String actualOutcome = this.controller.expandTree(StudyTreeControllerTest.LOCAL, "1", StudyTreeControllerTest.TYPE_TRIAL);
			System.out.println(actualOutcome);
			Assert.assertEquals("The root folder should be " + AppConstants.TRIALS.getString(), expectedOutcome, actualOutcome);
		} catch (JsonGenerationException e) {
			Assert.fail(e.getMessage());
		} catch (JsonMappingException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testExpandTree_AllNurseriesDirectlyUnderFolder2() {
		try {
			List<TreeNode> children = this.convertToTreeNodes(this.getChildren(3), true, false);
			String expectedOutcome = this.objectMapper.writeValueAsString(children);
			System.out.println(expectedOutcome);
			String actualOutcome = this.controller.expandTree("3", "0", StudyTreeControllerTest.TYPE_NURSERY);
			System.out.println(actualOutcome);
			Assert.assertEquals("The parent folder should be FOLDER_2", expectedOutcome, actualOutcome);
		} catch (JsonGenerationException e) {
			Assert.fail(e.getMessage());
		} catch (JsonMappingException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testExpandTree_AllTrialsDirectlyUnderFolder2() {
		try {
			List<TreeNode> children = this.convertToTreeNodes(this.getChildren(3), false, false);
			String expectedOutcome = this.objectMapper.writeValueAsString(children);
			String actualOutcome = this.controller.expandTree("3", "0", StudyTreeControllerTest.TYPE_TRIAL);
			Assert.assertEquals("The parent folder should be FOLDER_2", expectedOutcome, actualOutcome);
		} catch (JsonGenerationException e) {
			Assert.fail(e.getMessage());
		} catch (JsonMappingException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}
}
