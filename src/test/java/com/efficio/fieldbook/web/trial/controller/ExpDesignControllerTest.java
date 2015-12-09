
package com.efficio.fieldbook.web.trial.controller;

import junit.framework.Assert;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ExpDesignControllerTest {

	@Mock
	private CrossExpansionProperties crossExpansionProperties;
	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private ExpDesignController expDesignController;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsCimmytProfileWithWheatCropReturnsTrueForCimmytProfileAndWheatCrop() {
		Mockito.doReturn("cimmyt").when(this.crossExpansionProperties).getProfile();
		final Project project = new Project();
		final CropType cropType = new CropType();
		cropType.setCropName("wheat");
		project.setCropType(cropType);
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();

		Assert.assertTrue("Expecting to return true when the the profile is cimmyt and crop is wheat.",
				this.expDesignController.isCimmytProfileWithWheatCrop());
	}

	@Test
	public void testIsCimmytProfileWithWheatCropReturnsFalseForDefaultProfileAndWheatCrop() {
		Mockito.doReturn("default").when(this.crossExpansionProperties).getProfile();
		final Project project = new Project();
		final CropType cropType = new CropType();
		cropType.setCropName("wheat");
		project.setCropType(cropType);
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();

		Assert.assertFalse("Expecting to return false when the the profile is default and crop is wheat.",
				this.expDesignController.isCimmytProfileWithWheatCrop());
	}

	@Test
	public void testIsCimmytProfileWithWheatCropReturnsFalseForCimmytProfileAndNonWheatCrop() {
		Mockito.doReturn("cimmyt").when(this.crossExpansionProperties).getProfile();
		final Project project = new Project();
		final CropType cropType = new CropType();
		cropType.setCropName("maize");
		project.setCropType(cropType);
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();

		Assert.assertFalse("Expecting to return false when the the profile is cimmyt and crop is non wheat.",
				this.expDesignController.isCimmytProfileWithWheatCrop());
	}

}
