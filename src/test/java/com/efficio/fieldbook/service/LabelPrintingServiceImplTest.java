
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.InventoryDetailsTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementRowTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.data.initializer.FieldMapTrialInstanceInfoTestDataInitializer;
import com.efficio.fieldbook.web.data.initializer.LabelPrintingProcessingParamsTestDataInitializer;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.pojos.labelprinting.LabelPrintingProcessingParams;
import com.google.zxing.common.BitMatrix;

@RunWith(value = MockitoJUnitRunner.class)
public class LabelPrintingServiceImplTest {

	private static final int NO_OF_STOCK_LIST_ENTRIES = 20;
	private static final int NO_OF_GERMPLASM_LIST_OBSERVATION = 10;
	public static final Long TEST_PROJECT_ID = 1L;
	public static final String MAIZE_CROP_STR = "maize";
	public static final String TEST_EXISTING_PRESET_NAME = "TEST_EXISTING_PRESET_NAME";
	public static final String TEST_NON_EXISTING_PRESET_NAME = "TEST_NON_EXISTING_PRESET_NAME";
	public static final int TEST_PRESET_ID = 1;
	public static final String PROGRAM_PRESET_CONFIG = "program_preset_config";
	public static final String STANDARD_PRESET_CONFIG = "standard_preset_config";
	public static final String DUMMY_PROGRAM_UUID = "1234567890";
	public static final int TEST_STUDY_ID = 1;

	private Map<String, List<MeasurementRow>> measurementData;
	private Map<String, MeasurementRow> environmentData;

	@Mock
	WorkbenchService workbenchService;

	@Mock
	PresetDataManager presetDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private InventoryService inventoryMiddlewareService;

	@InjectMocks
	LabelPrintingServiceImpl serviceDUT = new LabelPrintingServiceImpl();

	@Before
	public void beforeTest() throws Exception {

		final Project project = Mockito.mock(Project.class);
		Mockito.when(project.getCropType()).thenReturn(new CropType(LabelPrintingServiceImplTest.MAIZE_CROP_STR));

		final Tool fieldbookWeb = new Tool();
		fieldbookWeb.setToolId(23L);
		fieldbookWeb.setToolName("fieldbook_web");

		Mockito.when(this.workbenchService.getFieldbookWebTool()).thenReturn(fieldbookWeb);

		// init mocks
		Mockito.when(this.workbenchService.getProjectById(LabelPrintingServiceImplTest.TEST_PROJECT_ID)).thenReturn(project);

		final ArrayList<ProgramPreset> notEmptySearchResult = new ArrayList<>();
		final ProgramPreset searchResultPreset = new ProgramPreset();
		searchResultPreset.setProgramUuid(LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID);
		searchResultPreset.setToolSection(ToolSection.FBK_LABEL_PRINTING.name());
		searchResultPreset.setToolId(23);
		searchResultPreset.setName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME);
		searchResultPreset.setConfiguration(LabelPrintingServiceImplTest.PROGRAM_PRESET_CONFIG);
		notEmptySearchResult.add(searchResultPreset);

		Mockito.when(this.presetDataManager.getProgramPresetFromProgramAndToolByName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
				LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23, ToolSection.FBK_LABEL_PRINTING.name()))
				.thenReturn(notEmptySearchResult);

		Mockito.when(
				this.presetDataManager.getProgramPresetFromProgramAndToolByName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23, ToolSection.FBK_LABEL_PRINTING.name()))
						.thenReturn(new ArrayList<ProgramPreset>());

		Mockito.when(this.presetDataManager.getProgramPresetFromProgramAndTool(LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23,
				ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(notEmptySearchResult);

		final ArrayList<StandardPreset> standardPresetSearchResults = new ArrayList<>();
		final StandardPreset sp = new StandardPreset();
		sp.setName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME);
		sp.setCropName(LabelPrintingServiceImplTest.MAIZE_CROP_STR);
		sp.setToolId(23);
		sp.setToolSection(ToolSection.FBK_LABEL_PRINTING.name());
		sp.setConfiguration(LabelPrintingServiceImplTest.STANDARD_PRESET_CONFIG);
		standardPresetSearchResults.add(sp);

		this.measurementData = MeasurementRowTestDataInitializer.createMeasurementDataMap();
		this.environmentData = MeasurementRowTestDataInitializer.createEnvironmentDataMap();

		Mockito.when(this.workbenchService.getStandardPresetByCropAndPresetName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME, 23,
				LabelPrintingServiceImplTest.MAIZE_CROP_STR, ToolSection.FBK_LABEL_PRINTING.name()))
				.thenReturn(standardPresetSearchResults);
		Mockito.when(this.workbenchService.getStandardPresetByCropAndPresetName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME,
				23, LabelPrintingServiceImplTest.MAIZE_CROP_STR, ToolSection.FBK_LABEL_PRINTING.name()))
				.thenReturn(new ArrayList<StandardPreset>());

		Mockito.when(this.workbenchService.getStandardPresetByCrop(23, LabelPrintingServiceImplTest.MAIZE_CROP_STR,
				ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(standardPresetSearchResults);

		Mockito.when(this.workbenchService.getStandardPresetById(LabelPrintingServiceImplTest.TEST_PRESET_ID)).thenReturn(sp);
		Mockito.when(this.presetDataManager.getProgramPresetById(LabelPrintingServiceImplTest.TEST_PRESET_ID))
		.thenReturn(searchResultPreset);

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID);
	}

	@Test
	public void testGetAllLabelPrintingPresets() throws Exception {
		final List<LabelPrintingPresets> presetList =
				this.serviceDUT.getAllLabelPrintingPresets(LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue());

		Assert.assertTrue("should have more than 1 item", presetList.size() > 1);
	}

	@Test
	public void testGetLabelPrintingPresetConfig() throws Exception {
		String config = this.serviceDUT.getLabelPrintingPresetConfig(LabelPrintingServiceImplTest.TEST_PRESET_ID,
				LabelPrintingPresets.STANDARD_PRESET);

		Assert.assertEquals("should retrieve a standard preset config", LabelPrintingServiceImplTest.STANDARD_PRESET_CONFIG, config);

		config = this.serviceDUT.getLabelPrintingPresetConfig(LabelPrintingServiceImplTest.TEST_PRESET_ID,
				LabelPrintingPresets.PROGRAM_PRESET);

		Assert.assertEquals("should retrieve a program preset config", LabelPrintingServiceImplTest.PROGRAM_PRESET_CONFIG, config);
	}

	@Test
	public void testGetLabelPrintingProgramPreset() throws Exception {
		LabelPrintingPresets preset =
				this.serviceDUT.getLabelPrintingPreset(LabelPrintingServiceImplTest.TEST_PRESET_ID, LabelPrintingPresets.STANDARD_PRESET);

		Assert.assertEquals("should retrieve a standard preset", LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME, preset.getName());

		preset = this.serviceDUT.getLabelPrintingPreset(LabelPrintingServiceImplTest.TEST_PRESET_ID, LabelPrintingPresets.PROGRAM_PRESET);

		Assert.assertEquals("should retrieve a program preset", LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME, preset.getName());

	}

	@Test
	public void testGetAllLabelPrintingPresetsByName() throws Exception {
		final List<LabelPrintingPresets> programPresetList =
				this.serviceDUT.getAllLabelPrintingPresetsByName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue(), LabelPrintingPresets.PROGRAM_PRESET);

		final List<LabelPrintingPresets> emptyProgramPresetList =
				this.serviceDUT.getAllLabelPrintingPresetsByName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue(), LabelPrintingPresets.PROGRAM_PRESET);

		final List<LabelPrintingPresets> standardPresetList =
				this.serviceDUT.getAllLabelPrintingPresetsByName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue(), LabelPrintingPresets.STANDARD_PRESET);

		final List<LabelPrintingPresets> emptyStandardPresetList =
				this.serviceDUT.getAllLabelPrintingPresetsByName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue(), LabelPrintingPresets.STANDARD_PRESET);

		Assert.assertTrue("should have an item", standardPresetList.size() > 0);
		Assert.assertTrue("should have an item", programPresetList.size() > 0);

		Assert.assertTrue("should be empty", emptyProgramPresetList.isEmpty());
		Assert.assertTrue("should be empty", emptyStandardPresetList.isEmpty());

		Assert.assertEquals("should be the same item as we searched on", LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
				programPresetList.get(0).getName());
		Assert.assertEquals("should be the same item as we searched on", LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
				standardPresetList.get(0).getName());
	}

	@Test
	public void testHasInventoryValues_ReturnsTrueForEntriesWithInventory() throws MiddlewareQueryException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		final Integer studyId = workbook.getStudyDetails().getId();
		final List<GermplasmList> germplasmLists = GermplasmListTestDataInitializer.createGermplasmLists(1);
		final GermplasmList germplasmList = germplasmLists.get(0);
		final Integer numOfEntries = germplasmList.getListData().size();
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.NURSERY))
		.thenReturn(germplasmLists);
		Mockito.when(this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(germplasmList.getId(), null))
		.thenReturn(InventoryDetailsTestDataInitializer.createInventoryDetailList(numOfEntries));

		Assert.assertTrue("Expecting to return true for germplasm list entries with inventory details.",
				this.serviceDUT.hasInventoryValues(studyId, workbook.isNursery()));
	}

	@Test
	public void testHasInventoryValues_ReturnsFalseForEntriesWithoutInventory() throws MiddlewareQueryException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		final Integer studyId = workbook.getStudyDetails().getId();
		final List<GermplasmList> germplasmLists = GermplasmListTestDataInitializer.createGermplasmLists(1);
		final GermplasmList germplasmList = germplasmLists.get(0);
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.NURSERY))
		.thenReturn(germplasmLists);
		Mockito.when(this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(germplasmList.getId()))
		.thenReturn(new ArrayList<InventoryDetails>());

		Assert.assertFalse("Expecting to return false for germplasm list entries with inventory details.",
				this.serviceDUT.hasInventoryValues(studyId, workbook.isNursery()));
	}

	@Test
	public void testEncodeBardcodeInEnglishCharacters() {
		final BitMatrix bitMatrix = this.serviceDUT.encodeBarcode("Test", 100, 200);
		Assert.assertNotNull("Bit Matrix Barcode should be not null since characters are in English ASCII", bitMatrix);
	}

	@Test
	public void testEncodeBardcodeInNonEnglishCharacters() {
		final BitMatrix bitMatrix = this.serviceDUT.encodeBarcode("乙七九", 100, 200);
		Assert.assertNull("Bit Matrix Barcode should be null since parameter is non-english ascii", bitMatrix);
	}

	@Test
	public void testPopulateValuesFromMeasurementNoData() {

		final Map<Integer, String> values = new HashMap<>();
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();
		final MeasurementRow measurementRow = MeasurementRowTestDataInitializer.createMeasurementRow();

		final Boolean hasData = this.serviceDUT.populateValuesFromMeasurement(params, measurementRow, 1, values, true);

		Assert.assertFalse("should be false", hasData);
	}

	@Test
	public void testPopulateValuesFromMeasurementWithData() {

		final Map<Integer, String> values = new HashMap<>();
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();
		final MeasurementRow measurementRow = MeasurementRowTestDataInitializer.createMeasurementRow();

		final Boolean hasData =
				this.serviceDUT.populateValuesFromMeasurement(params, measurementRow, TermId.TRIAL_LOCATION.getId(), values, true);

		Assert.assertTrue("Should be true", hasData);
		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "Manila",
				values.get(TermId.TRIAL_LOCATION.getId()));

	}

	@Test
	public void testPopulateValuesForTrial() {

		final Integer testTermId = TermId.TRIAL_LOCATION.getId();

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		final Map<Integer, String> values = new HashMap<>();
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();

		params.setVariableMap(new HashMap<Integer, MeasurementVariable>());
		for (final MeasurementVariable mv : workbook.getAllVariables()) {
			params.getVariableMap().put(mv.getTermId(), mv);
		}

		params.setEnvironmentData(MeasurementRowTestDataInitializer.createMeasurementRow());

		this.serviceDUT.populateValuesForTrial(params, workbook, testTermId, values, true);

		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "Manila",
				values.get(TermId.TRIAL_LOCATION.getId()));
	}

	@Test
	public void testPopulateValuesForNursery() {

		final Integer testTermId = TermId.TRIAL_LOCATION.getId();

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		final Map<Integer, String> values = new HashMap<>();
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();

		this.serviceDUT.populateValuesForNursery(params, workbook, testTermId, values, true);

		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "", values.get(TermId.TRIAL_LOCATION.getId()));
	}

	@Test
	public void testProcessUserSpecificLabelsForInstanceForStockList() {
		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setInstanceInfo(FieldMapTrialInstanceInfoTestDataInitializer.createFieldMapTrialInstanceInfo());
		params.setIsStockList(true);
		params.setInventoryDetailsMap(InventoryDetailsTestDataInitializer.createInventoryDetailsMap());
		params.setAllFieldIDs(new ArrayList<Integer>());

		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbook(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION, StudyType.N);

		this.serviceDUT.processUserSpecificLabelsForInstance(params, workbook);

		Assert.assertEquals(LabelPrintingServiceImplTest.NO_OF_STOCK_LIST_ENTRIES, params.getInstanceInfo().getFieldMapLabels().size());
	}

	@Test
	public void testProcessUserSpecificLabelsForInstanceForStudy() {

		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbook(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION, StudyType.N);
		this.setExperimentId(workbook);
		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setInstanceInfo(FieldMapTrialInstanceInfoTestDataInitializer.createFieldMapTrialInstanceInfo());
		params.setIsStockList(false);
		params.setInstanceMeasurements(workbook.getObservations());
		params.setAllFieldIDs(new ArrayList<Integer>());

		this.serviceDUT.processUserSpecificLabelsForInstance(params, workbook);

		Assert.assertEquals(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION,
				params.getInstanceInfo().getFieldMapLabels().size());
	}

	@Test
	public void testCheckAndSetFieldMapInstanceInfoForTrialEnvironmentDataOnly() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(2, 2);

		// for trial with stock list
		final boolean isTrial = true;
		final boolean isStockList = true;

		final List<FieldMapTrialInstanceInfo> trialFieldMap =
				FieldMapTrialInstanceInfoTestDataInitializer.createTrialFieldMapList(isStockList);
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();

		this.serviceDUT.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, isTrial, isStockList, params, this.measurementData,
				this.environmentData);
		try{
			Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(0))
			.getGermplasmListsByProjectId(LabelPrintingServiceImplTest.TEST_STUDY_ID, GermplasmListType.TRIAL);
		} catch(NeverWantedButInvoked e){
			Assert.fail("Expecting that the method processInventorySpecificLabelsForInstance is never invoked.");
		}

	}

	@Test
	public void testCheckAndSetFieldMapInstanceInfoForNurseryEnvironmentWithStocklistData() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(2, StudyType.N);

		// for nursery with stock list
		final boolean isTrial = false;
		final boolean isStockList = true;

		final List<FieldMapTrialInstanceInfo> trialFieldMap =
				FieldMapTrialInstanceInfoTestDataInitializer.createTrialFieldMapList(isStockList);
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();

		this.serviceDUT.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, isTrial, isStockList, params, this.measurementData,
				this.environmentData);
		try{
			Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(0))
			.getGermplasmListsByProjectId(LabelPrintingServiceImplTest.TEST_STUDY_ID, GermplasmListType.NURSERY);
		} catch(NeverWantedButInvoked e){
			Assert.fail("Expecting that the method processInventorySpecificLabelsForInstance is never invoked.");
		}


	}

	@Test
	public void testCheckAndSetFieldMapInstanceInfoForNurseryEnvironmentWithoutStocklistData() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(2, StudyType.N);
		workbook.getStudyDetails().setId(LabelPrintingServiceImplTest.TEST_STUDY_ID);

		// for nursery without stock list
		final boolean isTrial = false;
		final boolean isStockList = false;

		final List<FieldMapTrialInstanceInfo> trialFieldMap =
				FieldMapTrialInstanceInfoTestDataInitializer.createTrialFieldMapList(isStockList);
		final LabelPrintingProcessingParams params =
				LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParamsWithAllFieldIDs();

		this.serviceDUT.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, isTrial, isStockList, params, this.measurementData,
				this.environmentData);

		try{
			Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1))
			.getGermplasmListsByProjectId(LabelPrintingServiceImplTest.TEST_STUDY_ID, GermplasmListType.NURSERY);
		} catch(TooLittleActualInvocations e){
			Assert.fail("Expecting that the method processInventorySpecificLabelsForInstance is invoked.");
		}


	}

	private void setExperimentId(final Workbook workbook) {
		int i = 1;
		for (final MeasurementRow measurement : workbook.getObservations()) {
			measurement.setExperimentId(i);
			i++;
		}
	}

}
