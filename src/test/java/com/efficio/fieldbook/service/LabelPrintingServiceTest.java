
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.generationcp.middleware.domain.etl.Workbook;
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

import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@RunWith(MockitoJUnitRunner.class)
public class LabelPrintingServiceTest {

	@Mock
	private SettingsService settingsService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private Workbook workbook;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@InjectMocks
	private LabelPrintingServiceImpl dut;

	public static final String DUMMY_TRIAL_SETTING_LABEL_NAME = "dummyTrialSettingLabel";
	public static final String DUMMY_TRIAL_ENVIRONMENT_LABEL_NAME = "dummyTrialEnvironmentLabel";
	public static final String DUMMY_GERMPLASM_LABEL_NAME = "dummyGermplasmLabel";
	public static final String DUMMY_NURSERY_LABEL_NAME = "dummyNurseryLabel";
	public static final String DUMMY_TRAIT_LABEL_NAME = "dummyTrait";
	public static final int DUMMY_TRIAL_SETTING_LABEL_TERM_ID = 1;
	public static final int DUMMY_TRIAL_ENVIRONMENT_LABEL_TERM_ID = 2;
	public static final int DUMMY_NURSERY_LABEL_TERM_ID = 3;
	public static final int DUMMY_TRAIT_TERM_ID = 4;
	public static final int DUMMY_GERMPLASM_TERM_ID = 5;

	public static final int DUMMY_TRIAL_ID = 10;
	public static final int DUMMY_NURSERY_ID = 11;

	@Before
	public void setUp() throws Exception {
		Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(Matchers.anyInt())).thenReturn(this.workbook);
		Mockito.when(this.fieldbookMiddlewareService.getNurseryDataSet(Matchers.anyInt())).thenReturn(this.workbook);
	}

	@Test
	public void testGetAvailableFieldsTrialNoFieldMap() {
		List<LabelFields> trialSettingLabels = this.createDummyTrialSettingLabels();
		List<LabelFields> trialEnvironmentLabels = this.createDummyTrialEnvironmentLabels();
		List<LabelFields> traitLabels = this.createDummyTraitLabels();
		List<LabelFields> germplasmLabels = this.createDummyGermplasmLabels();

		Mockito.when(this.settingsService.retrieveTrialSettingsAsLabels(this.workbook)).thenReturn(trialSettingLabels);
		Mockito.when(this.settingsService.retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(this.workbook)).thenReturn(
				trialEnvironmentLabels);
		Mockito.when(this.settingsService.retrieveTraitsAsLabels(this.workbook)).thenReturn(traitLabels);
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(this.workbook)).thenReturn(germplasmLabels);

		List<LabelFields> retrieved =
				this.dut.getAvailableLabelFieldsForStudy(true, false, Locale.getDefault(), LabelPrintingServiceTest.DUMMY_TRIAL_ID);

		Assert.assertNotNull(retrieved);
		this.verifyBaseLabelFieldsPresent(retrieved);
		this.verifyLabelListContainsList(retrieved, trialSettingLabels,
				"Retrieved available label list does not contain all trial setting related labels");
		this.verifyLabelListContainsList(retrieved, trialEnvironmentLabels,
				"Retrieved available label list does not contain all environment related labels");
		this.verifyLabelListContainsList(retrieved, traitLabels, "Retrieved available label list does not contain all trait related labels");
		this.verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		this.verifyNoFieldMapLabels(retrieved);
	}

	@Test
	public void testGetAvailableFieldsTrialWithFieldMap() {
		List<LabelFields> trialSettingLabels = this.createDummyTrialSettingLabels();
		List<LabelFields> trialEnvironmentLabels = this.createDummyTrialEnvironmentLabels();
		List<LabelFields> traitLabels = this.createDummyTraitLabels();
		List<LabelFields> germplasmLabels = this.createDummyGermplasmLabels();

		Mockito.when(this.settingsService.retrieveTrialSettingsAsLabels(this.workbook)).thenReturn(trialSettingLabels);
		Mockito.when(this.settingsService.retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(this.workbook)).thenReturn(
				trialEnvironmentLabels);
		Mockito.when(this.settingsService.retrieveTraitsAsLabels(this.workbook)).thenReturn(traitLabels);
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(this.workbook)).thenReturn(germplasmLabels);

		List<LabelFields> retrieved =
				this.dut.getAvailableLabelFieldsForStudy(true, true, Locale.getDefault(), LabelPrintingServiceTest.DUMMY_TRIAL_ID);

		Assert.assertNotNull(retrieved);
		this.verifyBaseLabelFieldsPresent(retrieved);
		this.verifyLabelListContainsList(retrieved, trialSettingLabels,
				"Retrieved available label list does not contain all trial setting related labels");
		this.verifyLabelListContainsList(retrieved, trialEnvironmentLabels,
				"Retrieved available label list does not contain all environment related labels");
		this.verifyLabelListContainsList(retrieved, traitLabels, "Retrieved available label list does not contain all trait related labels");
		this.verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		this.verifyFieldMapLabelsPresent(retrieved);
	}

	@Test
	public void testGetAvailableFieldsNurseryWithFieldMap() {
		List<LabelFields> nurserySettingLabels = this.createDummyNurseryManagementLabels();
		List<LabelFields> traitLabels = this.createDummyTraitLabels();
		List<LabelFields> germplasmLabels = this.createDummyGermplasmLabels();

		Mockito.when(this.settingsService.retrieveNurseryManagementDetailsAsLabels(this.workbook)).thenReturn(nurserySettingLabels);
		Mockito.when(this.settingsService.retrieveTraitsAsLabels(this.workbook)).thenReturn(traitLabels);
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(this.workbook)).thenReturn(germplasmLabels);

		List<LabelFields> retrieved =
				this.dut.getAvailableLabelFieldsForStudy(false, true, Locale.getDefault(), LabelPrintingServiceTest.DUMMY_NURSERY_ID);

		Assert.assertNotNull(retrieved);
		this.verifyBaseLabelFieldsPresent(retrieved);
		this.verifyLabelListContainsList(retrieved, nurserySettingLabels,
				"Retrieved available label list does not contain all nursery management related labels");
		this.verifyLabelListContainsList(retrieved, traitLabels, "Retrieved available label list does not contain all trait related labels");
		this.verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		this.verifyFieldMapLabelsPresent(retrieved);
	}

	@Test
	public void testGetAvailableFieldsNurseryNoFieldMap() {
		List<LabelFields> nurserySettingLabels = this.createDummyNurseryManagementLabels();
		List<LabelFields> traitLabels = this.createDummyTraitLabels();
		List<LabelFields> germplasmLabels = this.createDummyGermplasmLabels();

		Mockito.when(this.settingsService.retrieveNurseryManagementDetailsAsLabels(this.workbook)).thenReturn(nurserySettingLabels);
		Mockito.when(this.settingsService.retrieveTraitsAsLabels(this.workbook)).thenReturn(traitLabels);
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(this.workbook)).thenReturn(germplasmLabels);

		List<LabelFields> retrieved =
				this.dut.getAvailableLabelFieldsForStudy(false, false, Locale.getDefault(), LabelPrintingServiceTest.DUMMY_NURSERY_ID);

		Assert.assertNotNull(retrieved);
		this.verifyBaseLabelFieldsPresent(retrieved);
		this.verifyLabelListContainsList(retrieved, nurserySettingLabels,
				"Retrieved available label list does not contain all nursery management related labels");
		this.verifyLabelListContainsList(retrieved, traitLabels, "Retrieved available label list does not contain all trait related labels");
		this.verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		this.verifyNoFieldMapLabels(retrieved);
	}

	protected void verifyFieldMapLabelsPresent(List<LabelFields> forVerification) {
		for (Integer baseLabelPrintingFieldMapLabelId : LabelPrintingServiceImpl.BASE_LABEL_PRINTING_FIELD_MAP_LABEL_IDS) {
			boolean found = false;

			for (LabelFields labelFields : forVerification) {
				if (baseLabelPrintingFieldMapLabelId.equals(labelFields.getId())) {
					found = true;
					break;
				}
			}

			Assert.assertTrue("Field map based label was not present in retrieved", found);
		}
	}

	protected void verifyNoFieldMapLabels(List<LabelFields> forVerification) {
		for (Integer baseLabelPrintingFieldMapLabelId : LabelPrintingServiceImpl.BASE_LABEL_PRINTING_FIELD_MAP_LABEL_IDS) {
			boolean found = false;

			for (LabelFields labelFields : forVerification) {
				if (baseLabelPrintingFieldMapLabelId.equals(labelFields.getId())) {
					found = true;
					break;
				}
			}

			Assert.assertFalse("Field map based labels were still present in retrieved", found);
		}
	}

	protected void verifyLabelListContainsList(List<LabelFields> forVerification, List<LabelFields> expectedContained, String errorMessage) {
		for (LabelFields trialSettingLabel : expectedContained) {
			Assert.assertTrue(errorMessage, forVerification.contains(trialSettingLabel));
		}
	}

	protected void verifyBaseLabelFieldsPresent(List<LabelFields> forVerification) {
		for (Integer baseLabelPrintingFieldId : LabelPrintingServiceImpl.BASE_LABEL_PRINTING_FIELD_IDS) {
			boolean found = false;

			for (LabelFields labelFields : forVerification) {
				if (baseLabelPrintingFieldId.equals(labelFields.getId())) {
					found = true;
					break;
				}
			}

			Assert.assertTrue("Base label field not present in values retrieved from service", found);
		}
	}

	protected List<LabelFields> createDummyNurseryManagementLabels() {
		List<LabelFields> labelFields = new ArrayList<>();

		LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_NURSERY_LABEL_NAME, LabelPrintingServiceTest.DUMMY_NURSERY_LABEL_TERM_ID,
						false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyTrialSettingLabels() {
		List<LabelFields> labelFields = new ArrayList<>();

		LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_TRIAL_SETTING_LABEL_NAME,
						LabelPrintingServiceTest.DUMMY_TRIAL_SETTING_LABEL_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyTrialEnvironmentLabels() {
		List<LabelFields> labelFields = new ArrayList<>();

		LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_TRIAL_ENVIRONMENT_LABEL_NAME,
						LabelPrintingServiceTest.DUMMY_TRIAL_ENVIRONMENT_LABEL_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyTraitLabels() {
		List<LabelFields> labelFields = new ArrayList<>();

		LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_TRAIT_LABEL_NAME, LabelPrintingServiceTest.DUMMY_TRAIT_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyGermplasmLabels() {
		List<LabelFields> labelFields = new ArrayList<>();

		LabelFields field =
				new LabelFields(LabelPrintingServiceTest.DUMMY_GERMPLASM_LABEL_NAME, LabelPrintingServiceTest.DUMMY_GERMPLASM_TERM_ID,
						false);
		labelFields.add(field);

		return labelFields;
	}

}
