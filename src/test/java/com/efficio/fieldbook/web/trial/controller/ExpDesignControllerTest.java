package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.internal.DesignLicenseUtil;
import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
import com.efficio.fieldbook.service.internal.breedingview.BVLicenseParseException;
import com.efficio.fieldbook.service.internal.breedingview.License;
import com.efficio.fieldbook.service.internal.breedingview.Status;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.AugmentedRandomizedBlockDesignService;
import com.efficio.fieldbook.web.common.service.EntryListOrderDesignService;
import com.efficio.fieldbook.web.common.service.PRepDesignService;
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
import org.generationcp.middleware.domain.dms.DesignType;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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
	private AugmentedRandomizedBlockDesignService augmentedRandomizedBlockDesignService;

	@Mock
	private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;

	@Mock
	private EntryListOrderDesignService entryListOrderDesignService;

	@Mock
	private ResolvableRowColumnDesignService resolvableRowColumnDesign;

	@Mock
	private PRepDesignService pRepDesignService;

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
		final Project project = new Project();
		project.setCropType(new CropType("maize"));
		this.expDesignController.setFieldbookProperties(this.fieldbookProperties);
		this.messageSource.setUseCodeAsDefaultMessage(true);
		this.expDesignController.setMessageSource(this.messageSource);
	}

	@Test
	public void testRetrieveDesignTypes() {
		final List<DesignType> designTypes = this.expDesignController.retrieveDesignTypes();
		Assert.assertEquals("7 core design types are expected to be returned.", 7, designTypes.size());
	}

	@Test
	public void testShowMeasurementsisExpiredAndisExpiringWithinThirtyDaysCalled() {
		final Model model = Mockito.mock(Model.class);
		final ExpDesignParameterUi expDesignParameterUi = this.createExpDesignParameterUiTestData();
		final List<ImportedGermplasm> germplasmList = this.mockGermplasmList();
		this.mockDesignValidation(expDesignParameterUi, germplasmList);

		Mockito.when(randomizeCompleteBlockDesign.requiresBreedingViewLicence()).thenReturn(Boolean.TRUE);
		this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.designLicenseUtil).isExpired(ArgumentMatchers.<BVDesignLicenseInfo>isNull());
		Mockito.verify(this.designLicenseUtil).isExpiringWithinThirtyDays(ArgumentMatchers.<BVDesignLicenseInfo>isNull());
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
		Mockito.doReturn(true).when(this.designLicenseUtil).isExpired(ArgumentMatchers.<BVDesignLicenseInfo>isNull());
		Mockito.when(randomizeCompleteBlockDesign.requiresBreedingViewLicence()).thenReturn(Boolean.TRUE);

		final ExpDesignValidationOutput output = this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.designLicenseUtil, Mockito.times(0)).isExpiringWithinThirtyDays(ArgumentMatchers.<BVDesignLicenseInfo>isNull());
		Mockito.verify(this.designLicenseUtil, Mockito.times(0)).isExpiringWithinThirtyDays(ArgumentMatchers.<BVDesignLicenseInfo>any());

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

		Mockito.when(randomizeCompleteBlockDesign.requiresBreedingViewLicence()).thenReturn(Boolean.TRUE);

		Mockito.doReturn(bvDesignLicenseInfo).when(this.designLicenseUtil).retrieveLicenseInfo();
		Mockito.doReturn(false).when(this.designLicenseUtil).isExpired(bvDesignLicenseInfo);
		Mockito.doReturn(true).when(this.designLicenseUtil).isExpiringWithinThirtyDays(bvDesignLicenseInfo);

		final ExpDesignValidationOutput output = this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.designLicenseUtil).isExpired(Mockito.any(BVDesignLicenseInfo.class));
		Mockito.verify(this.designLicenseUtil).isExpiringWithinThirtyDays(Mockito.any(BVDesignLicenseInfo.class));

		Assert.assertTrue(
			"The output should be valid because it's only a warning. This means the generation of design still executed.",
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

		Mockito.when(randomizeCompleteBlockDesign.requiresBreedingViewLicence()).thenReturn(Boolean.TRUE);

		// mock valid license
		Mockito.doReturn(false).when(this.designLicenseUtil).isExpired(ArgumentMatchers.<BVDesignLicenseInfo>isNull());
		Mockito.doReturn(false).when(this.designLicenseUtil).isExpiringWithinThirtyDays(ArgumentMatchers.<BVDesignLicenseInfo>isNull());

		final ExpDesignValidationOutput output = this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.designLicenseUtil).isExpired(ArgumentMatchers.<BVDesignLicenseInfo>isNull());
		Mockito.verify(this.designLicenseUtil).isExpiringWithinThirtyDays(ArgumentMatchers.<BVDesignLicenseInfo>isNull());

		Assert.assertTrue(
			"The output should be valid because the license is valid. This means the generation of design still executed.",
			output.isValid());

		Assert.assertEquals("The message should be empty", "", output.getMessage());
	}

	@Test
	public void testShowMeasurementsBreedingViewLicenseNotRequired() throws BVLicenseParseException {
		final Model model = Mockito.mock(Model.class);
		final ExpDesignParameterUi expDesignParameterUi = this.createExpDesignParameterUiTestData();
		expDesignParameterUi.setDesignType(5);
		final List<ImportedGermplasm> germplasmList = this.mockGermplasmList();
		this.mockDesignValidation(expDesignParameterUi, germplasmList);

		Mockito.when(entryListOrderDesignService.requiresBreedingViewLicence()).thenReturn(Boolean.FALSE);
		final ExpDesignValidationOutput expParameterOutput = new ExpDesignValidationOutput(true, "");
		Mockito.doReturn(expParameterOutput).when(this.entryListOrderDesignService).validate(expDesignParameterUi, germplasmList);

		final ExpDesignValidationOutput output = this.expDesignController.showMeasurements(model, expDesignParameterUi);

		Mockito.verify(this.entryListOrderDesignService, Mockito.times(2)).requiresBreedingViewLicence();
		Mockito.verify(this.designLicenseUtil, Mockito.never()).isExpired(Mockito.any(BVDesignLicenseInfo.class));
		Mockito.verify(this.designLicenseUtil, Mockito.never()).isExpiringWithinThirtyDays(Mockito.any(BVDesignLicenseInfo.class));
		Mockito.verify(this.designLicenseUtil, Mockito.never()).retrieveLicenseInfo();

		Assert.assertTrue(
			"The output should be valid because the license is valid. This means the generation of design still executed.",
			output.isValid());

		Assert.assertEquals("The message should be empty", "", output.getMessage());
	}

	@Test
	public void testGetExpDesignService() {

		Assert.assertSame(
			this.randomizeCompleteBlockDesign,
			this.expDesignController.getExpDesignService(DesignType.RANDOMIZED_COMPLETE_BLOCK.getId()));
		Assert.assertSame(
			this.resolveIncompleteBlockDesign,
			this.expDesignController.getExpDesignService(DesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId()));
		Assert.assertSame(
			this.resolvableRowColumnDesign,
			this.expDesignController.getExpDesignService(DesignType.ROW_COL.getId()));
		Assert.assertSame(
			this.augmentedRandomizedBlockDesignService,
			this.expDesignController.getExpDesignService(DesignType.AUGMENTED_RANDOMIZED_BLOCK.getId()));
		Assert.assertSame(
			this.entryListOrderDesignService,
			this.expDesignController.getExpDesignService(DesignType.ENTRY_LIST_ORDER.getId()));
		Assert.assertSame(
			this.pRepDesignService,
			this.expDesignController.getExpDesignService(DesignType.P_REP.getId()));
		Assert.assertNull(
			this.expDesignController.getExpDesignService(10101));
	}
}
