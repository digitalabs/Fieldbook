package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import org.generationcp.middleware.domain.etl.Workbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

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
		when(fieldbookMiddlewareService.getTrialDataSet(anyInt())).thenReturn(workbook);
		when(fieldbookMiddlewareService.getNurseryDataSet(anyInt())).thenReturn(workbook);
	}

	@Test
	public void testGetAvailableFieldsTrialNoFieldMap() {
		List<LabelFields> trialSettingLabels = createDummyTrialSettingLabels();
		List<LabelFields> trialEnvironmentLabels = createDummyTrialEnvironmentLabels();
		List<LabelFields> traitLabels = createDummyTraitLabels();
		List<LabelFields> germplasmLabels = createDummyGermplasmLabels();


		when(settingsService.retrieveTrialSettingsAsLabels(workbook)).thenReturn(trialSettingLabels);
		when(settingsService.retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(workbook)).thenReturn(trialEnvironmentLabels);
		when(settingsService.retrieveTraitsAsLabels(workbook)).thenReturn(traitLabels);
		when(settingsService.retrieveGermplasmDescriptorsAsLabels(workbook)).thenReturn(germplasmLabels);

		List<LabelFields> retrieved = dut.getAvailableLabelFields(true, false, Locale.getDefault(), DUMMY_TRIAL_ID);

		assertNotNull(retrieved);
		verifyBaseLabelFieldsPresent(retrieved);
		verifyLabelListContainsList(retrieved, trialSettingLabels, "Retrieved available label list does not contain all trial setting related labels");
		verifyLabelListContainsList(retrieved, trialEnvironmentLabels, "Retrieved available label list does not contain all environment related labels");
		verifyLabelListContainsList(retrieved, traitLabels, "Retrieved available label list does not contain all trait related labels");
		verifyLabelListContainsList(retrieved, germplasmLabels, "Retrieved available label list does not contain all germplasm related labels");

		verifyNoFieldMapLabels(retrieved);
	}

	@Test
	public void testGetAvailableFieldsTrialWithFieldMap() {
		List<LabelFields> trialSettingLabels = createDummyTrialSettingLabels();
		List<LabelFields> trialEnvironmentLabels = createDummyTrialEnvironmentLabels();
		List<LabelFields> traitLabels = createDummyTraitLabels();
		List<LabelFields> germplasmLabels = createDummyGermplasmLabels();

		when(settingsService.retrieveTrialSettingsAsLabels(workbook))
				.thenReturn(trialSettingLabels);
		when(settingsService
				.retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(workbook))
				.thenReturn(trialEnvironmentLabels);
		when(settingsService.retrieveTraitsAsLabels(workbook)).thenReturn(traitLabels);
		when(settingsService.retrieveGermplasmDescriptorsAsLabels(workbook))
				.thenReturn(germplasmLabels);

		List<LabelFields> retrieved = dut
				.getAvailableLabelFields(true, true, Locale.getDefault(), DUMMY_TRIAL_ID);

		assertNotNull(retrieved);
		verifyBaseLabelFieldsPresent(retrieved);
		verifyLabelListContainsList(retrieved, trialSettingLabels,
				"Retrieved available label list does not contain all trial setting related labels");
		verifyLabelListContainsList(retrieved, trialEnvironmentLabels,
				"Retrieved available label list does not contain all environment related labels");
		verifyLabelListContainsList(retrieved, traitLabels,
				"Retrieved available label list does not contain all trait related labels");
		verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		verifyFieldMapLabelsPresent(retrieved);
	}

	@Test
	public void testGetAvailableFieldsNurseryWithFieldMap() {
		List<LabelFields> nurserySettingLabels = createDummyNurseryManagementLabels();
		List<LabelFields> traitLabels = createDummyTraitLabels();
		List<LabelFields> germplasmLabels = createDummyGermplasmLabels();

		when(settingsService.retrieveNurseryManagementDetailsAsLabels(workbook))
				.thenReturn(nurserySettingLabels);
		when(settingsService.retrieveTraitsAsLabels(workbook)).thenReturn(traitLabels);
		when(settingsService.retrieveGermplasmDescriptorsAsLabels(workbook))
				.thenReturn(germplasmLabels);

		List<LabelFields> retrieved = dut
				.getAvailableLabelFields(false, true, Locale.getDefault(), DUMMY_NURSERY_ID);

		assertNotNull(retrieved);
		verifyBaseLabelFieldsPresent(retrieved);
		verifyLabelListContainsList(retrieved, nurserySettingLabels,
				"Retrieved available label list does not contain all nursery management related labels");
		verifyLabelListContainsList(retrieved, traitLabels,
				"Retrieved available label list does not contain all trait related labels");
		verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		verifyFieldMapLabelsPresent(retrieved);
	}

	@Test
	public void testGetAvailableFieldsNurseryNoFieldMap() {
		List<LabelFields> nurserySettingLabels = createDummyNurseryManagementLabels();
		List<LabelFields> traitLabels = createDummyTraitLabels();
		List<LabelFields> germplasmLabels = createDummyGermplasmLabels();

		when(settingsService.retrieveNurseryManagementDetailsAsLabels(workbook))
				.thenReturn(nurserySettingLabels);
		when(settingsService.retrieveTraitsAsLabels(workbook)).thenReturn(traitLabels);
		when(settingsService.retrieveGermplasmDescriptorsAsLabels(workbook))
				.thenReturn(germplasmLabels);

		List<LabelFields> retrieved = dut
				.getAvailableLabelFields(false, false, Locale.getDefault(), DUMMY_NURSERY_ID);

		assertNotNull(retrieved);
		verifyBaseLabelFieldsPresent(retrieved);
		verifyLabelListContainsList(retrieved, nurserySettingLabels,
				"Retrieved available label list does not contain all nursery management related labels");
		verifyLabelListContainsList(retrieved, traitLabels,
				"Retrieved available label list does not contain all trait related labels");
		verifyLabelListContainsList(retrieved, germplasmLabels,
				"Retrieved available label list does not contain all germplasm related labels");

		verifyNoFieldMapLabels(retrieved);
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

			assertTrue("Field map based label was not present in retrieved", found);
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

			assertFalse("Field map based labels were still present in retrieved", found);
		}
	}

	protected void verifyLabelListContainsList(List<LabelFields> forVerification, List<LabelFields> expectedContained, String errorMessage) {
		for (LabelFields trialSettingLabel : expectedContained) {
			assertTrue(errorMessage,forVerification.contains(trialSettingLabel));
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

			assertTrue("Base label field not present in values retrieved from service", found);
		}
	}

	protected List<LabelFields> createDummyNurseryManagementLabels() {
		List<LabelFields> labelFields = new ArrayList<>();

		LabelFields field = new LabelFields(DUMMY_NURSERY_LABEL_NAME,
				DUMMY_NURSERY_LABEL_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyTrialSettingLabels() {
		List<LabelFields> labelFields = new ArrayList<>();

		LabelFields field = new LabelFields(DUMMY_TRIAL_SETTING_LABEL_NAME, DUMMY_TRIAL_SETTING_LABEL_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyTrialEnvironmentLabels() {
		List<LabelFields> labelFields = new ArrayList<>();

		LabelFields field = new LabelFields(DUMMY_TRIAL_ENVIRONMENT_LABEL_NAME,
				DUMMY_TRIAL_ENVIRONMENT_LABEL_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyTraitLabels() {
		List<LabelFields> labelFields = new ArrayList<>();

		LabelFields field = new LabelFields(DUMMY_TRAIT_LABEL_NAME,
				DUMMY_TRAIT_TERM_ID, false);
		labelFields.add(field);

		return labelFields;
	}

	protected List<LabelFields> createDummyGermplasmLabels() {
			List<LabelFields> labelFields = new ArrayList<>();

			LabelFields field = new LabelFields(DUMMY_GERMPLASM_LABEL_NAME,
					DUMMY_GERMPLASM_TERM_ID, false);
			labelFields.add(field);

			return labelFields;
		}

}
