package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.internal.DesignLicenseUtil;
import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
import com.efficio.fieldbook.service.internal.breedingview.BVLicenseParseException;
import com.efficio.fieldbook.service.internal.breedingview.License;
import com.efficio.fieldbook.service.internal.breedingview.Status;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.importdesign.service.DesignImportService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class ExpDesignControllerTest {

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
	private DesignLicenseUtil designLicenseUtil;

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
	public void testRetrieveDesignTypes() {
		final List<DesignTypeItem> designTypes = this.expDesignController.retrieveDesignTypes();
		Assert.assertEquals("6 core design types are expected to be returned.", 6, designTypes.size());
	}

	@Test
	public void testShowMeasurementsisExpiredAndisExpiringWithinThirtyDaysCalled() {
		final Model model = Mockito.mock(Model.class);
		final ExpDesignParameterUi expDesignParameterUi = this.createExpDesignParameterUiTestData();
		final List<ImportedGermplasm> germplasmList = this.mockGermplasmList();
		this.mockDesignValidation(expDesignParameterUi, germplasmList);

		this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.designLicenseUtil).isExpired(Mockito.any(BVDesignLicenseInfo.class));
		Mockito.verify(this.designLicenseUtil).isExpiringWithinThirtyDays(Mockito.any(BVDesignLicenseInfo.class));
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
	public void testShowMeasurementsisExpiredHasError() {
		final Model model = Mockito.mock(Model.class);
		final ExpDesignParameterUi expDesignParameterUi = this.createExpDesignParameterUiTestData();
		final List<ImportedGermplasm> germplasmList = this.mockGermplasmList();
		this.mockDesignValidation(expDesignParameterUi, germplasmList);

		// mock license has expired
		Mockito.doReturn(true).when(this.designLicenseUtil).isExpired(Mockito.any(BVDesignLicenseInfo.class));

		final ExpDesignValidationOutput output = this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.designLicenseUtil, Mockito.times(0)).isExpiringWithinThirtyDays(Mockito.any(BVDesignLicenseInfo.class));

		Assert.assertFalse("The output should be invalid. This means the generation of design is not executed.", output.isValid());

		final String message = this.messageSource.getMessage("experiment.design.license.expired", null, LocaleContextHolder.getLocale());
		Assert.assertEquals("The message should be " + message, message, output.getMessage());
	}

	@Test
	public void testShowMeasurementsisExpiringWithinThirtyDaysHasWarning() throws BVLicenseParseException {
		final Model model = Mockito.mock(Model.class);
		final ExpDesignParameterUi expDesignParameterUi = this.createExpDesignParameterUiTestData();
		final List<ImportedGermplasm> germplasmList = this.mockGermplasmList();
		this.mockDesignValidation(expDesignParameterUi, germplasmList);

		// mock license has not yet expired but expiring in thirty days
		BVDesignLicenseInfo bvDesignLicenseInfo = new BVDesignLicenseInfo();
		bvDesignLicenseInfo.setStatus(new Status());
		bvDesignLicenseInfo.getStatus().setLicense(new License());
		bvDesignLicenseInfo.getStatus().getLicense().setExpiryDays("29");

		Mockito.doReturn(bvDesignLicenseInfo).when(this.designLicenseUtil).retrieveLicenseInfo();
		Mockito.doReturn(false).when(this.designLicenseUtil).isExpired(bvDesignLicenseInfo);
		Mockito.doReturn(true).when(this.designLicenseUtil).isExpiringWithinThirtyDays(bvDesignLicenseInfo);

		final ExpDesignValidationOutput output = this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.designLicenseUtil).isExpired(Mockito.any(BVDesignLicenseInfo.class));
		Mockito.verify(this.designLicenseUtil).isExpiringWithinThirtyDays(Mockito.any(BVDesignLicenseInfo.class));

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
		Mockito.doReturn(false).when(this.designLicenseUtil).isExpired(Mockito.any(BVDesignLicenseInfo.class));
		Mockito.doReturn(false).when(this.designLicenseUtil).isExpiringWithinThirtyDays(Mockito.any(BVDesignLicenseInfo.class));

		final ExpDesignValidationOutput output = this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.designLicenseUtil).isExpired(Mockito.any(BVDesignLicenseInfo.class));
		Mockito.verify(this.designLicenseUtil).isExpiringWithinThirtyDays(Mockito.any(BVDesignLicenseInfo.class));

		Assert.assertTrue("The output should be valid because the license is valid. This means the generation of design still executed.",
				output.isValid());

		Assert.assertEquals("The message should be empty", "", output.getMessage());

	}
}
