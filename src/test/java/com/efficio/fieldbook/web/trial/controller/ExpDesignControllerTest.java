
package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.spring.util.ToolLicenseUtil;
import org.generationcp.middleware.domain.dms.DesignTypeItem;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.Model;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.importdesign.service.DesignImportService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.util.FieldbookProperties;

@RunWith(MockitoJUnitRunner.class)
public class ExpDesignControllerTest {

	private static final String LICENSE_EXPIRED_MESSAGE = "License has expired";
	private static final String LICENSE_EXPIRING_MESSAGE = "License is expiring in 30 days";

	@Mock
	private RandomizeCompleteBlockDesignService randomizeCompleteBlockDesign;

	@Mock
	private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;

	@Mock
	private ResolvableRowColumnDesignService resolvableRowColumnDesign;

	private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private DesignImportService designImportService;

	@Mock
	private ToolLicenseUtil toolLicenseUtil;

	@Mock
	protected UserSelection userSelection;

	private final FieldbookProperties fieldbookProperties = new FieldbookProperties();

	@InjectMocks
	ExpDesignController expDesignController;

	@Before()
	public void init() {
		Mockito.doReturn("CIMMYT").when(this.crossExpansionProperties).getProfile();
		final Project project = new Project();
		project.setCropType(new CropType("maize"));
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();
		this.expDesignController.setFieldbookProperties(this.fieldbookProperties);
		this.messageSource.setUseCodeAsDefaultMessage(true);
		this.expDesignController.setMessageSource(this.messageSource);
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
		final List<DesignTypeItem> designTypes = this.expDesignController.retrieveDesignTypes();
		Assert.assertEquals("4 core design types are expected to be returned.", 4, designTypes.size());
	}

	@Test
	public void testShowMeasurementsIsToolExpiredAndIsToolExpiringWithinThirtyDaysCalled() {
		final Model model = Mockito.mock(Model.class);
		final ExpDesignParameterUi expDesignParameterUi = this.createExpDesignParameterUiTestData();
		final List<ImportedGermplasm> germplasmList = this.mockGermplasmList();
		this.mockDesignValidation(expDesignParameterUi, germplasmList);

		this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.toolLicenseUtil).isToolExpired(ToolName.breeding_view.toString());
		Mockito.verify(this.toolLicenseUtil).isToolExpiringWithinThirtyDays(ToolName.breeding_view.toString());
	}

	private void mockDesignValidation(final ExpDesignParameterUi expDesignParameterUi, final List<ImportedGermplasm> germplasmList) {
		final ExpDesignValidationOutput expParameterOutput = new ExpDesignValidationOutput(true, "");
		Mockito.doReturn(expParameterOutput).when(this.randomizeCompleteBlockDesign).validate(expDesignParameterUi, germplasmList);
	}

	private List<ImportedGermplasm> mockGermplasmList() {
		final List<ImportedGermplasm> germplasmList = new ArrayList<>();
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmList.setImportedGermplasms(germplasmList);
		final ImportedGermplasmMainInfo importedGermplasmMainInfo = new ImportedGermplasmMainInfo();
		importedGermplasmMainInfo.setImportedGermplasmList(importedGermplasmList);
		Mockito.doReturn(importedGermplasmMainInfo).when(this.userSelection).getImportedGermplasmMainInfo();
		this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		return germplasmList;
	}

	private ExpDesignParameterUi createExpDesignParameterUiTestData() {
		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setDesignType(0);
		return expDesignParameterUi;
	}

	@Test
	public void testShowMeasurementsIsToolExpiredHasError() {
		final Model model = Mockito.mock(Model.class);
		final ExpDesignParameterUi expDesignParameterUi = this.createExpDesignParameterUiTestData();
		final List<ImportedGermplasm> germplasmList = this.mockGermplasmList();
		this.mockDesignValidation(expDesignParameterUi, germplasmList);

		// mock license has expired
		Mockito.doReturn(true).when(this.toolLicenseUtil).isToolExpired(ToolName.breeding_view.toString());

		final ExpDesignValidationOutput output = this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.toolLicenseUtil, Mockito.times(0)).isToolExpiringWithinThirtyDays(ToolName.breeding_view.toString());

		Assert.assertFalse("The output should be invalid. This means the generation of design is not executed.", output.isValid());

		final String message = this.messageSource.getMessage("experiment.design.license.expired", null, LocaleContextHolder.getLocale());
		Assert.assertEquals("The message should be " + message, message, output.getMessage());
	}

	@Test
	public void testShowMeasurementsIsToolExpiringWithinThirtyDaysHasWarning() {
		final Model model = Mockito.mock(Model.class);
		final ExpDesignParameterUi expDesignParameterUi = this.createExpDesignParameterUiTestData();
		final List<ImportedGermplasm> germplasmList = this.mockGermplasmList();
		this.mockDesignValidation(expDesignParameterUi, germplasmList);

		// mock license has not yet expired but expiring in thirty days
		Mockito.doReturn(false).when(this.toolLicenseUtil).isToolExpired(ToolName.breeding_view.toString());
		Mockito.doReturn(true).when(this.toolLicenseUtil).isToolExpiringWithinThirtyDays(ToolName.breeding_view.toString());

		final ExpDesignValidationOutput output = this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.toolLicenseUtil).isToolExpired(ToolName.breeding_view.toString());
		Mockito.verify(this.toolLicenseUtil).isToolExpiringWithinThirtyDays(ToolName.breeding_view.toString());

		Assert.assertTrue("The output should be valid because it's only a warning. This means the generation of design still executed.",
				output.isValid());

		final String message = this.messageSource.getMessage("experiment.design.license.expiring", null, LocaleContextHolder.getLocale());
		Assert.assertEquals("The message should be " + message, message, output.getMessage());
	}

	@Test
	public void testShowMeasurementsValidLicense() {
		final Model model = Mockito.mock(Model.class);
		final ExpDesignParameterUi expDesignParameterUi = this.createExpDesignParameterUiTestData();
		final List<ImportedGermplasm> germplasmList = this.mockGermplasmList();
		this.mockDesignValidation(expDesignParameterUi, germplasmList);

		// mock valid license
		Mockito.doReturn(false).when(this.toolLicenseUtil).isToolExpired(ToolName.breeding_view.toString());
		Mockito.doReturn(false).when(this.toolLicenseUtil).isToolExpiringWithinThirtyDays(ToolName.breeding_view.toString());

		final ExpDesignValidationOutput output = this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.toolLicenseUtil).isToolExpired(ToolName.breeding_view.toString());
		Mockito.verify(this.toolLicenseUtil).isToolExpiringWithinThirtyDays(ToolName.breeding_view.toString());

		Assert.assertTrue("The output should be valid because the license is valid. This means the generation of design still executed.",
				output.isValid());

		Assert.assertEquals("The message should be empty", "", output.getMessage());

	}
}
