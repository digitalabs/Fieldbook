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
		Project project = new Project();
		project.setProjectId(1L);
		project.setProjectName("Test Project");
		project.setUniqueID(UUID.randomUUID().toString());
		return project;
	}

	@Test
	public void testLoadInitialTree() {
		String result = this.controller.loadInitialTree("1", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue(!"[]".equals(result));
	}
	
	@Test
	public void testExpandTreeRoot() {
		List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder", "My Folder Description"));
		testTree.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));
		testTree.add(new StudyReference(11, "My Nursery", "My Nursery Description"));

		Mockito.doReturn(testTree).when(this.studyDataManager).getRootFolders(this.selectedProject.getUniqueID(), StudyType.nurseries());
		
		String result = this.controller.expandTree(StudyTreeController.LOCAL, "1", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue(!"[]".equals(result));
	}
	
	
	@Test
	public void testExpandTreeUnderRoot() {
		List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder", "My Folder Description"));
		testTree.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));
		testTree.add(new StudyReference(11, "My Nursery", "My Nursery Description"));

		Mockito.doReturn(testTree).when(this.studyDataManager).getChildrenOfFolder(1, this.selectedProject.getUniqueID(), StudyType.nurseries());
		
		String result = this.controller.expandTree("1", "1", "N");
		Assert.assertNotNull(result);
		Assert.assertTrue(!"[]".equals(result));
	}

}
