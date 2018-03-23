
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.util.AppConstants;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

@RunWith(MockitoJUnitRunner.class)
public class LabelPrintingServiceTest {

	@Mock
	private SettingsService settingsService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private Workbook workbook;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@InjectMocks
	private LabelPrintingServiceImpl unitUnderTest;

	public static final String DUMMY_TRIAL_SETTING_LABEL_NAME = "dummyTrialSettingLabel";
	public static final String DUMMY_TRIAL_ENVIRONMENT_LABEL_NAME = "dummyTrialEnvironmentLabel";
	public static final String DUMMY_TRIAL_EXPERIMENTAL_DESIGN_LABEL_NAME = "dummyExperimentalDesignLabel";
	public static final String DUMMY_GERMPLASM_LABEL_NAME = "dummyGermplasmLabel";
	public static final String DUMMY_NURSERY_LABEL_NAME = "dummyNurseryLabel";
	public static final String DUMMY_TRAIT_LABEL_NAME = "dummyTrait";
	public static final int DUMMY_TRIAL_SETTING_LABEL_TERM_ID = 1;
	public static final int DUMMY_TRIAL_ENVIRONMENT_LABEL_TERM_ID = 2;
	public static final int DUMMY_NURSERY_LABEL_TERM_ID = 3;
	public static final int DUMMY_TRAIT_TERM_ID = 4;
	public static final int DUMMY_GERMPLASM_TERM_ID = 5;
	public static final int DUMMY_EXPERIMENTAL_DESIGN_TERM_ID = 6;

	public static final int DUMMY_TRIAL_ID = 10;
	public static final int DUMMY_NURSERY_ID = 11;

	public static final Integer[] BASE_LABEL_PRINTING_FIELD_IDS = new Integer[] {
		AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt(), AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt(),
		 AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()};

	public static final Integer[] BASE_LABEL_PRINTING_FIELD_TRIAL_IDS = new Integer[] {
			AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt(), AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt(),
			AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()};

	@Before
	public void setUp() throws Exception {
		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(Matchers.anyInt())).thenReturn(this.workbook);
	}

	@Test
	public void testGetAvailableFieldsTrialNoFieldMap() {
		final List<LabelFields> trialSettingLabels = this.createDummyTrialSettingLabels();
		final List<LabelFields> trialEnvironmentLabels = this.createDummyTrialEnvironmentLabels();
		final List<LabelFields> trialExperimentalDesignLabels = this.createExperimentalDesignLabels();
		final List<LabelFields> traitLabels = this.createDummyTraitLabels();
		final List<LabelFields> germplasmLabels = this.createDummyGermplasmLabels();

		Mockito.when(this.settingsService.retrieveTrialSettingsAsLabels(this.workbook)).thenReturn(trialSettingLabels);
		Mockito.when(this.settingsService.retrieveTrialEnvironmentConditionsAsLabels(this.workbook)).thenReturn(
				trialEnvironmentLabels);
		Mockito.when(this.settingsService.retrieveExperimentalDesignFactorsAsLabels(this.workbook)).thenReturn(
				trialExperimentalDesignLabels);
		Mockito.when(this.settingsService.retrieveTraitsAsLabels(this.workbook)).thenReturn(traitLabels);
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(this.workbook)).thenReturn(germplasmLabels);

		final List<LabelFields> retrieved =
				this.unitUnderTest.getAvailableLabelFieldsForStudy( false, Locale.getDefault(),
						LabelPrintingServiceTest.DUMMY_TRIAL_ID);

		Assert.assertNotNull(retrieved);
		this.verifyBaseLabelFieldsPresent(retrieved, LabelPrintingServiceTest.BASE_LABEL_PRINTING_FIELD_TRIAL_IDS);
		this.verifyLabelListContainsList(retrieved, trialSettingLabels,
				"Retrieved available label list does not contain all trial setting related labels");
		this.verifyLabelListContainsList(retrieved, trialEnvironmentLabels,
				"Retrieved available label list does not contain all environment related labels");
		this.verifyLabelListContainsList(retrieved, trialExperimentalDesignLabels,
				"Retrieved available label list does not contain all experimental design related labels");
		this.verifyLabelListContainsList(retrieved, traitLabels, "Retrieved available label list does not contain all trait related labels");
		this.verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		this.verifyNoFieldMapLabels(retrieved);
	}

	@Test
	public void testGetAvailableFieldsTrialWithFieldMap() {
		final List<LabelFields> trialSettingLabels = this.createDummyTrialSettingLabels();
		final List<LabelFields> trialEnvironmentLabels = this.createDummyTrialEnvironmentLabels();
		final List<LabelFields> trialExperimentalDesignLabels = this.createExperimentalDesignLabels();
		final List<LabelFields> traitLabels = this.createDummyTraitLabels();
		final List<LabelFields> germplasmLabels = this.createDummyGermplasmLabels();

		Mockito.when(this.settingsService.retrieveTrialSettingsAsLabels(this.workbook)).thenReturn(trialSettingLabels);
		Mockito.when(this.settingsService.retrieveTrialEnvironmentConditionsAsLabels(this.workbook)).thenReturn(
				trialEnvironmentLabels);
		Mockito.when(this.settingsService.retrieveExperimentalDesignFactorsAsLabels(this.workbook)).thenReturn(
				trialExperimentalDesignLabels);
		Mockito.when(this.settingsService.retrieveTraitsAsLabels(this.workbook)).thenReturn(traitLabels);
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(this.workbook)).thenReturn(germplasmLabels);

		final List<LabelFields> retrieved =
				this.unitUnderTest
						.getAvailableLabelFieldsForStudy(true, Locale.getDefault(), LabelPrintingServiceTest.DUMMY_TRIAL_ID);

		Assert.assertNotNull(retrieved);
		this.verifyBaseLabelFieldsPresent(retrieved , LabelPrintingServiceTest.BASE_LABEL_PRINTING_FIELD_TRIAL_IDS);
		this.verifyLabelListContainsList(retrieved, trialSettingLabels,
				"Retrieved available label list does not contain all trial setting related labels");
		this.verifyLabelListContainsList(retrieved, trialEnvironmentLabels,
				"Retrieved available label list does not contain all environment related labels");
		this.verifyLabelListContainsList(retrieved, trialExperimentalDesignLabels,
				"Retrieved available label list does not contain all experimental design related labels");
		this.verifyLabelListContainsList(retrieved, traitLabels, "Retrieved available label list does not contain all trait related labels");
		this.verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		this.verifyFieldMapLabelsPresent(retrieved);
	}

	@Test
	public void testGetAvailableFieldsNurseryWithFieldMap() {
		final List<LabelFields> nurserySettingLabels = this.createDummyNurseryManagementLabels();
		final List<LabelFields> traitLabels = this.createDummyTraitLabels();
		final List<LabelFields> germplasmLabels = this.createDummyGermplasmLabels();

		Mockito.when(this.settingsService.retrieveNurseryManagementDetailsAsLabels(this.workbook)).thenReturn(nurserySettingLabels);
		Mockito.when(this.settingsService.retrieveTraitsAsLabels(this.workbook)).thenReturn(traitLabels);
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(this.workbook)).thenReturn(germplasmLabels);

		final List<LabelFields> retrieved =
				this.unitUnderTest.getAvailableLabelFieldsForStudy(true, Locale.getDefault(),
						LabelPrintingServiceTest.DUMMY_NURSERY_ID);

		Assert.assertNotNull(retrieved);
		this.verifyBaseLabelFieldsPresent(retrieved , LabelPrintingServiceTest.BASE_LABEL_PRINTING_FIELD_IDS);
		this.verifyLabelListContainsList(retrieved, nurserySettingLabels,
				"Retrieved available label list does not contain all nursery management related labels");
		this.verifyLabelListContainsList(retrieved, traitLabels, "Retrieved available label list does not contain all trait related labels");
		this.verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		Mockito.verify(this.settingsService, Mockito.times(0)).retrieveExperimentalDesignFactorsAsLabels(this.workbook);

		this.verifyFieldMapLabelsPresent(retrieved);
	}

	@Test
	public void testGetAvailableFieldsNurseryNoFieldMap() {
		final List<LabelFields> nurserySettingLabels = this.createDummyNurseryManagementLabels();
		final List<LabelFields> traitLabels = this.createDummyTraitLabels();
		final List<LabelFields> germplasmLabels = this.createDummyGermplasmLabels();

		Mockito.when(this.settingsService.retrieveNurseryManagementDetailsAsLabels(this.workbook)).thenReturn(nurserySettingLabels);
		Mockito.when(this.settingsService.retrieveTraitsAsLabels(this.workbook)).thenReturn(traitLabels);
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(this.workbook)).thenReturn(germplasmLabels);

		final List<LabelFields> retrieved =
				this.unitUnderTest.getAvailableLabelFieldsForStudy(false, Locale.getDefault(),
						LabelPrintingServiceTest.DUMMY_NURSERY_ID);

		Assert.assertNotNull(retrieved);
		this.verifyBaseLabelFieldsPresent(retrieved, LabelPrintingServiceTest.BASE_LABEL_PRINTING_FIELD_IDS);
		this.verifyLabelListContainsList(retrieved, nurserySettingLabels,
				"Retrieved available label list does not contain all nursery management related labels");
		this.verifyLabelListContainsList(retrieved, traitLabels, "Retrieved available label list does not contain all trait related labels");
		this.verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		Mockito.verify(this.settingsService, Mockito.times(0)).retrieveExperimentalDesignFactorsAsLabels(this.workbook);

		this.verifyNoFieldMapLabels(retrieved);
	}

	@Test
	public void testGetAvailableFieldsForCrossStockList() {
		final List<LabelFields> nurserySettingLabels = this.createDummyNurseryManagementLabels();
		final List<LabelFields> germplasmLabels = this.createDummyGermplasmLabels();

		Mockito.when(this.settingsService.retrieveNurseryManagementDetailsAsLabels(this.workbook)).thenReturn(nurserySettingLabels);
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(this.workbook)).thenReturn(germplasmLabels);

		final List<LabelFields> retrieved =
				this.unitUnderTest.getAvailableLabelFieldsForStockList(GermplasmListType.CROSSES, Locale.getDefault(), LabelPrintingServiceTest.DUMMY_NURSERY_ID);

		this.verifyLabelListContainsList(retrieved, nurserySettingLabels,
				"Retrieved available label list does not contain all nursery management related labels");
		this.verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		this.verifyLabelByTermID(TermId.DUPLICATE.getId(), retrieved);
		this.verifyLabelByTermID(TermId.BULK_WITH.getId(), retrieved);
		this.verifyLabelByTermID(TermId.BULK_COMPL.getId(), retrieved);
		this.verifyLabelByTermID(TermId.PLOT_NO.getId(), retrieved);
	}

	protected void verifyFieldMapLabelsPresent(final List<LabelFields> forVerification) {
		boolean found = false;
		for (final Integer baseLabelPrintingFieldMapLabelId : LabelPrintingServiceImpl.BASE_LABEL_PRINTING_FIELD_MAP_LABEL_IDS) {

			for (final LabelFields labelFields : forVerification) {
				if (baseLabelPrintingFieldMapLabelId.equals(labelFields.getId())) {
					found = true;
					break;
				}
			}

			Assert.assertTrue("Field map based label was not present in retrieved", found);
		}
	}

	protected void verifyLabelByTermID(final int termID, final List<LabelFields> forVerification) {
		boolean found = false;
		for (final LabelFields labelFields : forVerification) {
			if (labelFields.getId() == termID) {
				found = true;
				break;
			}
		}

		Assert.assertTrue("Expected label field not found in retrieved", found);
	}

	protected void verifyNoFieldMapLabels(final List<LabelFields> forVerification) {
		for (final Integer baseLabelPrintingFieldMapLabelId : LabelPrintingServiceImpl.BASE_LABEL_PRINTING_FIELD_MAP_LABEL_IDS) {
			boolean found = false;

			for (final LabelFields labelFields : forVerification) {
				if (baseLabelPrintingFieldMapLabelId.equals(labelFields.getId())) {
					found = true;
					break;
				}
			}

			Assert.assertFalse("Field map based labels were still present in retrieved", found);
		}
	}

	protected void verifyLabelListContainsList(final List<LabelFields> forVerification, final List<LabelFields> expectedContained,
			final String errorMessage) {
		for (final LabelFields trialSettingLabel : expectedContained) {
			Assert.assertTrue(errorMessage, forVerification.contains(trialSettingLabel));
		}
	}

	protected void verifyBaseLabelFieldsPresent(final List<LabelFields> forVerification , final Integer[] baseFieldIDs) {
		for (final Integer baseLabelPrintingFieldId : baseFieldIDs) {
			boolean found = false;

			for (final LabelFields labelFields : forVerification) {
				if (baseLabelPrintingFieldId.equals(labelFields.getId())) {
					found = true;
				}
			}
			if(!found){
				Assert.assertTrue("Base label field not present in values retrieved from service", false);
			}

		}
	}

	protected List<LabelFields> createDummyNurseryManagementLabels() {
		final List<LabelFields> labelFields = new ArrayList<>();

		final LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_NURSERY_LABEL_NAME, LabelPrintingServiceTest.DUMMY_NURSERY_LABEL_TERM_ID,
						false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyTrialSettingLabels() {
		final List<LabelFields> labelFields = new ArrayList<>();

		final LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_TRIAL_SETTING_LABEL_NAME,
						LabelPrintingServiceTest.DUMMY_TRIAL_SETTING_LABEL_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyTrialEnvironmentLabels() {
		final List<LabelFields> labelFields = new ArrayList<>();

		final LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_TRIAL_ENVIRONMENT_LABEL_NAME,
						LabelPrintingServiceTest.DUMMY_TRIAL_ENVIRONMENT_LABEL_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createExperimentalDesignLabels() {
		final List<LabelFields> labelFields = new ArrayList<>();

		final LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_TRIAL_EXPERIMENTAL_DESIGN_LABEL_NAME,
						LabelPrintingServiceTest.DUMMY_EXPERIMENTAL_DESIGN_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyTraitLabels() {
		final List<LabelFields> labelFields = new ArrayList<>();

		final LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_TRAIT_LABEL_NAME, LabelPrintingServiceTest.DUMMY_TRAIT_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyGermplasmLabels() {
		final List<LabelFields> labelFields = new ArrayList<>();

		final LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_GERMPLASM_LABEL_NAME, LabelPrintingServiceTest.DUMMY_GERMPLASM_TERM_ID,
						false);
		labelFields.add(field);

		return labelFields;
	}

}
