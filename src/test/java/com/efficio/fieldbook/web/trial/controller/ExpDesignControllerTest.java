
package com.efficio.fieldbook.web.trial.controller;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.DesignTypeItem;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.importdesign.service.DesignImportService;

@RunWith(MockitoJUnitRunner.class)
public class ExpDesignControllerTest {

	@Mock
	private RandomizeCompleteBlockDesignService randomizeCompleteBlockDesign;

	@Mock
	private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;

	@Mock
	private ResolvableRowColumnDesignService resolvableRowColumnDesign;

	@Mock
	private ResourceBundleMessageSource messageSource;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private DesignImportService designImportService;

	@InjectMocks
	ExpDesignController expDesignController;

	@Before()
	public void init() {
		Mockito.doReturn("CIMMYT").when(this.crossExpansionProperties).getProfile();
		final Project project = Mockito.mock(Project.class);
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();
		Mockito.doReturn(new CropType("WHEAT")).when(project).getCropType();
	}

	@Test
	public void testIsValidPresetDesignTemplate() {
		String fileName = "E30-Rep3-Block6-5Ind.csv";
		Assert.assertTrue("Expecting to return true when the filename is valid.",
				this.expDesignController.isValidPresetDesignTemplate(fileName));

		fileName = "E30-Rep3-Block6-5Ind";
		Assert.assertFalse("Expecting to return false when the filename does not ends with .csv.",
				this.expDesignController.isValidPresetDesignTemplate(fileName));

		fileName = "Rep3-Block6-5Ind.csv";
		Assert.assertFalse("Expecting to return false when the filename does not starts with E[\\d]+",
				this.expDesignController.isValidPresetDesignTemplate(fileName));

		fileName = "E30-Block6-5Ind.csv";
		Assert.assertFalse("Expecting to return false when the filename does not contains -Rep[\\d]+",
				this.expDesignController.isValidPresetDesignTemplate(fileName));

	}

	@Test
	public void testGetTemplateName() {
		final String templateName = "templateFileName.csv";
		Assert.assertFalse("Expecting that the template name does not ends with .csv",
				this.expDesignController.getTemplateName(templateName).endsWith(".csv"));
	}

	@Test
	public void testGetTotalNoOfEntries() {
		final String fileName = "E30-Rep3-Block6-5Ind.csv";
		final int actualNoOfEntries = this.expDesignController.getTotalNoOfEntries(fileName);
		Assert.assertEquals("Expecting that the return no of entries is 30 but returned " + actualNoOfEntries, 30, actualNoOfEntries);
	}

	@Test
	public void testGetNoOfReps() {
		final String fileName = "E30-Rep3-Block6-5Ind.csv";
		final int actualNoOfReps = this.expDesignController.getNoOfReps(fileName);
		Assert.assertEquals("Expecting that the return no of replication is 3 but returned " + actualNoOfReps, 3, actualNoOfReps);
	}

	@Test
	public void testGeneratePresetDesignTypeItem() {
		final String fileName = "E30-Rep3-Block6-5Ind.csv";

		final DesignTypeItem designTypeItem = this.expDesignController.generatePresetDesignTypeItem(fileName, 1);

		Assert.assertEquals(1, designTypeItem.getId().intValue());
		Assert.assertEquals(3, designTypeItem.getRepNo().intValue());
		Assert.assertEquals(30, designTypeItem.getTotalNoOfEntries().intValue());
		Assert.assertEquals(fileName, designTypeItem.getTemplateName());
		Assert.assertEquals(fileName.substring(0, fileName.indexOf(".csv")), designTypeItem.getName());
	}

	@Test
	public void testRetrieveDesignTypes() {
		this.expDesignController.retrieveDesignTypes();
	}
}
