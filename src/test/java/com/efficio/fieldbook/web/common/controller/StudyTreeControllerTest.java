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
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

public class StudyTreeControllerTest {

	private static final String DUMMY_PROGRAM_UUID = "1234567890";

	private Project selectedProject;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private MessageSource messageSource;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private StudyTreeController controller;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.selectedProject = this.createProject();
		Mockito.doReturn(this.selectedProject.getUniqueID()).when(this.contextUtil).getCurrentProgramUUID();
	}

	private Project createProject() {
		final Project project = new Project();
		project.setProjectId(1L);
		project.setProjectName("Test Project");
		project.setUniqueID(UUID.randomUUID().toString());
		return project;
	}

	@Test
	public void testLoadInitialTree() {
		final String result = this.controller.loadInitialTree("1", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue(!"[]".equals(result));
	}

	@Test
	public void testExpandTreeRoot() {
		final List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder 1", "My Folder Description"));
		testTree.add(new FolderReference(1, 4, "My Folder 2", "My Folder Description"));
		testTree.add(new StudyReference(11, "My Nursery", "My Nursery Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID, StudyType.N));

		Mockito.doReturn(testTree).when(this.studyDataManager).getRootFolders(this.selectedProject.getUniqueID(), StudyType.nurseries());

		final List<Reference> childOfMyFolder1 = new ArrayList<>();
		childOfMyFolder1.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));

		Mockito.doReturn(childOfMyFolder1).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final List<Reference> childOfMyFolder2 = new ArrayList<>();
		childOfMyFolder2.add(new StudyReference(12, "My Trial", "My Trial Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.T));

		Mockito.doReturn(childOfMyFolder2).when(this.studyDataManager)
		.getChildrenOfFolder(4, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		Mockito.doReturn(null).when(this.studyDataManager)
		.getChildrenOfFolder(3, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree(StudyTreeController.LOCAL, "0", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Folder 1 should be displayed", result.contains("My Folder 1"));
		Assert.assertFalse("My Folder 2 should not be displayed", result.contains("My Folder 2"));
		Assert.assertTrue("My Nursery should be displayed", result.contains("My Nursery"));
	}

	@Test
	public void testExpandTreeRootWithNoDescendants() {
		Mockito.doReturn(null).when(this.studyDataManager).getRootFolders(this.selectedProject.getUniqueID(), StudyType.nurseries());

		final String result = this.controller.expandTree(StudyTreeController.LOCAL, "0", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue("No items should be displayed", "[]".equals(result));
	}

	@Test
	public void testExpandTreeRootWithOnlyEmptyFolderAsDescendant() {
		final List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder", "My Folder Description"));

		Mockito.doReturn(testTree).when(this.studyDataManager).getRootFolders(this.selectedProject.getUniqueID(), StudyType.nurseries());

		Mockito.doReturn(null).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree(StudyTreeController.LOCAL, "0", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Folder should be displayed", result.contains("My Folder"));
	}

	@Test
	public void testExpandTreeRootWithFolderWithSubFolderAsDescendants() {
		final List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder", "My Folder Description"));

		Mockito.doReturn(testTree).when(this.studyDataManager).getRootFolders(this.selectedProject.getUniqueID(), StudyType.nurseries());

		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		Mockito.doReturn(null).when(this.studyDataManager)
		.getChildrenOfFolder(3, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree(StudyTreeController.LOCAL, "0", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Folder should be displayed", result.contains("My Folder"));
	}

	@Test
	public void testExpandTreeRootWithFolderWithOnlyAppropriateLeafAsDescendants() {
		final List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder", "My Folder Description"));

		Mockito.doReturn(testTree).when(this.studyDataManager).getRootFolders(this.selectedProject.getUniqueID(), StudyType.nurseries());

		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new StudyReference(11, "My Nursery", "My Nursery Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.N));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree(StudyTreeController.LOCAL, "0", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Folder should be displayed", result.contains("My Folder"));
	}

	@Test
	public void testExpandTreeRootWithFolderWithOnlyInappropriateLeafAsDescendants() {
		final List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder", "My Folder Description"));

		Mockito.doReturn(testTree).when(this.studyDataManager).getRootFolders(this.selectedProject.getUniqueID(), StudyType.nurseries());

		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new StudyReference(12, "My Trial", "My Trial Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.T));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree(StudyTreeController.LOCAL, "0", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue("No items should be displayed", "[]".equals(result));
	}

	@Test
	public void testExpandTreeRootWithFolderWithDifferentTypeOfLeavesAsDescendants() {
		final List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder", "My Folder Description"));

		Mockito.doReturn(testTree).when(this.studyDataManager).getRootFolders(this.selectedProject.getUniqueID(), StudyType.nurseries());

		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new StudyReference(11, "My Nursery", "My Nursery Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.N));
		childOfMyFolder.add(new StudyReference(12, "My Trial", "My Trial Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.T));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree(StudyTreeController.LOCAL, "0", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Folder should be displayed", result.contains("My Folder"));
	}

	@Test
	public void testExpandTreeRootWithFoldersWithAndWithoutAppropriateLeavesForToolAsDescendants() {
		final List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder 1", "My Folder Description"));
		testTree.add(new FolderReference(1, 5, "My Folder 2", "My Folder Description"));
		testTree.add(new FolderReference(1, 7, "My Folder 3", "My Folder Description"));

		Mockito.doReturn(testTree).when(this.studyDataManager).getRootFolders(this.selectedProject.getUniqueID(), StudyType.nurseries());

		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final List<Reference> childOfMyFolder2 = new ArrayList<>();
		childOfMyFolder2.add(new StudyReference(12, "My Trial", "My Trial Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.T));

		Mockito.doReturn(childOfMyFolder2).when(this.studyDataManager)
		.getChildrenOfFolder(5, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final List<Reference> childOfMySubFolder = new ArrayList<>();
		childOfMySubFolder.add(new StudyReference(11, "My Nursery", "My Nursery Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.N));

		Mockito.doReturn(childOfMySubFolder).when(this.studyDataManager)
		.getChildrenOfFolder(3, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		Mockito.doReturn(null).when(this.studyDataManager)
		.getChildrenOfFolder(7, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree(StudyTreeController.LOCAL, "0", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Folder 1 should be displayed", result.contains("My Folder 1"));
		Assert.assertFalse("My Folder 2 should not be displayed", result.contains("My Folder 2"));
		Assert.assertTrue("My Folder 3 should be displayed", result.contains("My Folder 3"));
	}

	@Test
	public void testExpandTreeUnderRoot() {
		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new FolderReference(2, 3, "My Sub Folder 1", "My Sub Folder Description"));
		childOfMyFolder.add(new FolderReference(2, 4, "My Sub Folder 2", "My Sub Folder Description"));
		childOfMyFolder.add(new StudyReference(12, "My Trial", "My Trial Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.T));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.trials());

		Mockito.doReturn(null).when(this.studyDataManager)
		.getChildrenOfFolder(3, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final List<Reference> childOfMySubFolder2 = new ArrayList<>();
		childOfMySubFolder2.add(new StudyReference(11, "My Nursery", "My Nursery Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.N));

		Mockito.doReturn(childOfMySubFolder2).when(this.studyDataManager)
		.getChildrenOfFolder(4, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree("2", "0", "T");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Sub Folder 1 should be displayed", result.contains("My Sub Folder 1"));
		Assert.assertFalse("My Sub Folder 1 should not be displayed", result.contains("My Sub Folder 2"));
		Assert.assertTrue("My Trial should be displayed", result.contains("My Trial"));

	}

	@Test
	public void testExpandTreeUnderRootWithNoDescendants() {
		Mockito.doReturn(null).when(this.studyDataManager).getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.trials());

		final String result = this.controller.expandTree("2", "0", "T");
		Assert.assertNotNull(result);
		Assert.assertTrue("No items should be displayed", "[]".equals(result));
	}

	@Test
	public void testExpandTreeUnderRootWithOnlyEmptyFolderAsDescendant() {
		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.trials());

		Mockito.doReturn(null).when(this.studyDataManager)
		.getChildrenOfFolder(3, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree("2", "0", "T");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Sub Folder should be displayed", result.contains("My Sub Folder"));
	}

	@Test
	public void testExpandTreeUnderRootWithFolderWithEmptySubFolderAsDescendants() {
		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.trials());

		final List<Reference> childOfMySubFolder = new ArrayList<>();
		childOfMySubFolder.add(new FolderReference(3, 4, "My Sub Sub Folder ", "My Sub Folder Description"));

		Mockito.doReturn(childOfMySubFolder).when(this.studyDataManager)
		.getChildrenOfFolder(3, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		Mockito.doReturn(null).when(this.studyDataManager)
		.getChildrenOfFolder(4, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree("2", "0", "T");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Sub Folder should be displayed", result.contains("My Sub Folder"));
	}

	@Test
	public void testExpandTreeUnderRootWithFolderWithOnlyAppropriateLeafAsDescendants() {
		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.trials());

		final List<Reference> childOfMySubFolder = new ArrayList<>();
		childOfMySubFolder.add(new StudyReference(12, "My Trial", "My Trial Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.T));

		Mockito.doReturn(childOfMySubFolder).when(this.studyDataManager)
		.getChildrenOfFolder(3, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree("2", "0", "T");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Sub Folder should be displayed", result.contains("My Sub Folder"));
	}

	@Test
	public void testExpandTreeUnderRootWithFolderWithOnlyInappropriateLeafAsDescendants() {
		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.trials());

		final List<Reference> childOfMySubFolder = new ArrayList<>();
		childOfMySubFolder.add(new StudyReference(11, "My Nursery", "My Nursery Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.N));

		Mockito.doReturn(childOfMySubFolder).when(this.studyDataManager)
		.getChildrenOfFolder(3, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree("2", "0", "T");
		Assert.assertNotNull(result);
		Assert.assertTrue("No items should be displayed", "[]".equals(result));
	}

	@Test
	public void testExpandTreeUnderRootWithFolderWithDifferentTypeOfLeavesAsDescendants() {
		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.trials());

		final List<Reference> childOfSubMyFolder = new ArrayList<>();
		childOfSubMyFolder.add(new StudyReference(11, "My Nursery", "My Nursery Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.N));
		childOfSubMyFolder.add(new StudyReference(12, "My Trial", "My Trial Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.T));

		Mockito.doReturn(childOfSubMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(3, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree("2", "0", "T");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertTrue("My Sub Folder should be displayed", result.contains("My Sub Folder"));
	}

	@Test
	public void testExpandTreeUnderRootWithFoldersWithAndWithoutAppropriateLeavesForToolAsDescendants() {
		final List<Reference> childOfMyFolder = new ArrayList<>();
		childOfMyFolder.add(new FolderReference(2, 3, "My Sub Folder 1", "My Folder Description"));
		childOfMyFolder.add(new FolderReference(2, 6, "My Sub Folder 2", "My Folder Description"));
		childOfMyFolder.add(new FolderReference(2, 8, "My Sub Folder 3", "My Folder Description"));

		Mockito.doReturn(childOfMyFolder).when(this.studyDataManager)
		.getChildrenOfFolder(2, this.selectedProject.getUniqueID(), StudyType.trials());

		final List<Reference> childOfMySubFolder = new ArrayList<>();
		childOfMySubFolder.add(new StudyReference(11, "My Nursery", "My Nursery Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.N));

		Mockito.doReturn(childOfMySubFolder).when(this.studyDataManager)
		.getChildrenOfFolder(3, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final List<Reference> childOfMySubFolder2 = new ArrayList<>();
		childOfMySubFolder2.add(new StudyReference(12, "My Trial", "My Trial Description", StudyTreeControllerTest.DUMMY_PROGRAM_UUID,
				StudyType.T));

		Mockito.doReturn(childOfMySubFolder2).when(this.studyDataManager)
		.getChildrenOfFolder(6, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		Mockito.doReturn(null).when(this.studyDataManager)
		.getChildrenOfFolder(8, this.selectedProject.getUniqueID(), StudyType.nurseriesAndTrials());

		final String result = this.controller.expandTree("2", "0", "T");
		Assert.assertNotNull(result);
		Assert.assertTrue("There should should be items displayed", !"[]".equals(result));
		Assert.assertFalse("My Sub Folder 1 should not be displayed", result.contains("My Sub Folder 1"));
		Assert.assertTrue("My Folder 2 should not be displayed", result.contains("My Sub Folder 2"));
		Assert.assertTrue("My Folder 3 should be displayed", result.contains("My Sub Folder 3"));
	}

}
