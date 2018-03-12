
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.InventoryDetailsTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementRowTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.initializer.LabelPrintingServiceDataInitializer;
import com.efficio.fieldbook.util.labelprinting.CSVLabelGenerator;
import com.efficio.fieldbook.util.labelprinting.LabelGeneratorFactory;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.data.initializer.FieldMapTrialInstanceInfoTestDataInitializer;
import com.efficio.fieldbook.web.data.initializer.LabelPrintingProcessingParamsTestDataInitializer;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.pojos.labelprinting.LabelPrintingProcessingParams;

@RunWith(value = MockitoJUnitRunner.class)
public class LabelPrintingServiceImplTest {

	private static final int NO_OF_STOCK_LIST_ENTRIES = 20;
	private static final int NO_OF_GERMPLASM_LIST_OBSERVATION = 10;
	private static final Long TEST_PROJECT_ID = 1L;
	private static final String MAIZE_CROP_STR = "maize";
	private static final String TEST_EXISTING_PRESET_NAME = "TEST_EXISTING_PRESET_NAME";
	private static final String TEST_NON_EXISTING_PRESET_NAME = "TEST_NON_EXISTING_PRESET_NAME";
	private static final int TEST_PRESET_ID = 1;
	private static final String PROGRAM_PRESET_CONFIG = "program_preset_config";
	private static final String STANDARD_PRESET_CONFIG = "standard_preset_config";
	private static final String DUMMY_PROGRAM_UUID = "1234567890";
	private static final int TEST_STUDY_ID = 1;

	private Map<String, List<MeasurementRow>> measurementData;
	private Map<String, MeasurementRow> environmentData;

	private InventoryDetailsTestDataInitializer inventoryDetailsInitializer;

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private PresetDataManager presetDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private InventoryService inventoryMiddlewareService;

	@Mock
	private LabelGeneratorFactory labelGeneratorFactory;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private OntologyDataManager ontologyDataManager;
	
	@Mock
	private UserLabelPrinting userLabelPrinting;

	@Mock
	private MessageSource messageSource;

	@Mock
	private SettingsService settingsService;

	@InjectMocks
	private LabelPrintingServiceImpl labelPrintingServiceImpl;

	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;

	@Before
	public void beforeTest() {
		this.germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();
		// init data initializer
		this.inventoryDetailsInitializer = new InventoryDetailsTestDataInitializer();

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
		searchResultPreset.setToolSection(ToolSection.PLANTING_LABEL_PRINTING_PRESET.name());
		searchResultPreset.setToolId(23);
		searchResultPreset.setName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME);
		searchResultPreset.setConfiguration(LabelPrintingServiceImplTest.PROGRAM_PRESET_CONFIG);
		notEmptySearchResult.add(searchResultPreset);

		Mockito.when(this.presetDataManager.getProgramPresetFromProgramAndToolByName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
				LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23, ToolSection.PLANTING_LABEL_PRINTING_PRESET.name()))
				.thenReturn(notEmptySearchResult);

		Mockito.when(
				this.presetDataManager.getProgramPresetFromProgramAndToolByName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23, ToolSection.PLANTING_LABEL_PRINTING_PRESET.name()))
				.thenReturn(new ArrayList<ProgramPreset>());

		Mockito.when(this.presetDataManager.getProgramPresetFromProgramAndTool(LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23,
				ToolSection.PLANTING_LABEL_PRINTING_PRESET.name())).thenReturn(notEmptySearchResult);

		final ArrayList<StandardPreset> standardPresetSearchResults = new ArrayList<>();
		final StandardPreset sp = new StandardPreset();
		sp.setName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME);
		sp.setCropName(LabelPrintingServiceImplTest.MAIZE_CROP_STR);
		sp.setToolId(23);
		sp.setToolSection(ToolSection.PLANTING_LABEL_PRINTING_PRESET.name());
		sp.setConfiguration(LabelPrintingServiceImplTest.STANDARD_PRESET_CONFIG);
		standardPresetSearchResults.add(sp);

		this.measurementData = MeasurementRowTestDataInitializer.createMeasurementDataMap();
		this.environmentData = MeasurementRowTestDataInitializer.createEnvironmentDataMap();

		Mockito.when(this.workbenchService.getStandardPresetByCropAndPresetName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME, 23,
				LabelPrintingServiceImplTest.MAIZE_CROP_STR, ToolSection.PLANTING_LABEL_PRINTING_PRESET.name()))
				.thenReturn(standardPresetSearchResults);
		Mockito.when(this.workbenchService.getStandardPresetByCropAndPresetName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME,
				23, LabelPrintingServiceImplTest.MAIZE_CROP_STR, ToolSection.PLANTING_LABEL_PRINTING_PRESET.name()))
				.thenReturn(new ArrayList<StandardPreset>());

		Mockito.when(this.workbenchService.getStandardPresetByCrop(23, LabelPrintingServiceImplTest.MAIZE_CROP_STR,
				ToolSection.PLANTING_LABEL_PRINTING_PRESET.name())).thenReturn(standardPresetSearchResults);

		Mockito.when(this.workbenchService.getStandardPresetById(LabelPrintingServiceImplTest.TEST_PRESET_ID)).thenReturn(sp);
		Mockito.when(this.presetDataManager.getProgramPresetById(LabelPrintingServiceImplTest.TEST_PRESET_ID))
				.thenReturn(searchResultPreset);

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID);
		
		Mockito.when(this.userLabelPrinting.isStockList()).thenReturn(false);
	}

	@Test
	public void testGetAllLabelPrintingPresets() throws Exception {
		final List<LabelPrintingPresets> presetList =
				this.labelPrintingServiceImpl.getAllLabelPrintingPresets(LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue());

		Assert.assertTrue("should have at least 1 item", presetList.size() > 0);
	}

	@Test
	public void testGetLabelPrintingPresetConfig() throws Exception {
		String config = this.labelPrintingServiceImpl.getLabelPrintingPresetConfig(LabelPrintingServiceImplTest.TEST_PRESET_ID,
				LabelPrintingPresets.STANDARD_PRESET);

		Assert.assertEquals("should retrieve a standard preset config", LabelPrintingServiceImplTest.STANDARD_PRESET_CONFIG, config);

		config = this.labelPrintingServiceImpl.getLabelPrintingPresetConfig(LabelPrintingServiceImplTest.TEST_PRESET_ID,
				LabelPrintingPresets.PROGRAM_PRESET);

		Assert.assertEquals("should retrieve a program preset config", LabelPrintingServiceImplTest.PROGRAM_PRESET_CONFIG, config);
	}

	@Test
	public void testGetLabelPrintingProgramPreset() {
		LabelPrintingPresets preset = this.labelPrintingServiceImpl.getLabelPrintingPreset(LabelPrintingServiceImplTest.TEST_PRESET_ID,
				LabelPrintingPresets.STANDARD_PRESET);

		Assert.assertEquals("should retrieve a standard preset", LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME, preset.getName());

		preset = this.labelPrintingServiceImpl.getLabelPrintingPreset(LabelPrintingServiceImplTest.TEST_PRESET_ID,
				LabelPrintingPresets.PROGRAM_PRESET);

		Assert.assertEquals("should retrieve a program preset", LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME, preset.getName());

	}

	@Test
	public void testGetAllLabelPrintingPresetsByName() {
		final List<LabelPrintingPresets> programPresetList =
				this.labelPrintingServiceImpl.getAllLabelPrintingPresetsByName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue());

		final List<LabelPrintingPresets> emptyProgramPresetList =
				this.labelPrintingServiceImpl.getAllLabelPrintingPresetsByName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue());

		Assert.assertTrue("should have an item", programPresetList.size() > 0);

		Assert.assertTrue("should be empty", emptyProgramPresetList.isEmpty());
		
		Assert.assertEquals("should be the same item as we searched on", LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
				programPresetList.get(0).getName());
	}

	@Test
	public void testHasInventoryValues_ReturnsTrueForEntriesWithInventory() throws MiddlewareQueryException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		final Integer studyId = workbook.getStudyDetails().getId();
		final List<GermplasmList> germplasmLists = GermplasmListTestDataInitializer.createGermplasmLists(1);
		final GermplasmList germplasmList = germplasmLists.get(0);
		final Integer numOfEntries = germplasmList.getListData().size();
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.STUDY)).thenReturn(
				germplasmLists);
		Mockito.when(this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(germplasmList.getId(), germplasmList.getType()))
				.thenReturn(this.inventoryDetailsInitializer.createInventoryDetailList(numOfEntries));

		Assert.assertTrue("Expecting to return true for germplasm list entries with inventory details.",
				this.labelPrintingServiceImpl.hasInventoryValues(studyId));
	}

	@Test
	public void testHasInventoryValues_ReturnsFalseForEntriesWithoutInventory() throws MiddlewareQueryException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		final Integer studyId = workbook.getStudyDetails().getId();
		final List<GermplasmList> germplasmLists = GermplasmListTestDataInitializer.createGermplasmLists(1);
		final GermplasmList germplasmList = germplasmLists.get(0);
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.STUDY))
				.thenReturn(germplasmLists);
		Mockito.when(this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(germplasmList.getId()))
				.thenReturn(new ArrayList<InventoryDetails>());

		Assert.assertFalse("Expecting to return false for germplasm list entries with inventory details.",
				this.labelPrintingServiceImpl.hasInventoryValues(studyId));
	}

	@Test
	public void testPopulateValuesFromMeasurementNoData() {

		final Map<Integer, String> values = new HashMap<>();
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();
		final MeasurementRow measurementRow = MeasurementRowTestDataInitializer.createMeasurementRow();

		final Boolean hasData = this.labelPrintingServiceImpl.populateValuesFromMeasurement(params, measurementRow, 1, values, true);

		Assert.assertFalse("should be false", hasData);
	}

	@Test
	public void testPopulateValuesFromMeasurementWithData() {

		final Map<Integer, String> values = new HashMap<>();
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();
		final MeasurementRow measurementRow = MeasurementRowTestDataInitializer.createMeasurementRow();

		final Boolean hasData = this.labelPrintingServiceImpl.populateValuesFromMeasurement(params, measurementRow,
				TermId.TRIAL_LOCATION.getId(), values, true);

		Assert.assertTrue("Should be true", hasData);
		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "Manila",
				values.get(TermId.TRIAL_LOCATION.getId()));

	}

	@Test
	public void testGenerateLabelsSortLabelsEntryNumber() throws LabelPrintingException {
		final CSVLabelGenerator labelGenerator = Mockito.mock(CSVLabelGenerator.class);
		final UserLabelPrinting labelPrinting = Mockito.mock(UserLabelPrinting.class);
		Mockito.doReturn(labelGenerator).when(this.labelGeneratorFactory).retrieveLabelGenerator(AppConstants.LABEL_PRINTING_CSV.getString());

		final List<StudyTrialInstanceInfo> infoList = LabelPrintingServiceDataInitializer.generateStudyTrialInstanceInfoList();

		// we randomize the arrangement of the list
		Collections.shuffle(infoList.get(0).getTrialInstance().getFieldMapLabels());
		this.labelPrintingServiceImpl.generateLabels(AppConstants.LABEL_PRINTING_CSV.getString(), infoList, labelPrinting);

		int currentEntryNumberValue = -1;
		for (final FieldMapLabel fieldMapLabel : infoList.get(0).getTrialInstance().getFieldMapLabels()) {
			Assert.assertTrue("Labels were not re-arranged from lowest to highest via entry number",
					fieldMapLabel.getEntryNumber() > currentEntryNumberValue);
			currentEntryNumberValue = fieldMapLabel.getEntryNumber();
		}
	}

	@Test
	public void testGenerateLabelsSortLabelsPlotNumber() throws LabelPrintingException {
		final CSVLabelGenerator labelGenerator = Mockito.mock(CSVLabelGenerator.class);
		final UserLabelPrinting labelPrinting = Mockito.mock(UserLabelPrinting.class);
		Mockito.doReturn(labelGenerator).when(this.labelGeneratorFactory).retrieveLabelGenerator(AppConstants.LABEL_PRINTING_CSV.getString());

		final List<StudyTrialInstanceInfo> infoList = LabelPrintingServiceDataInitializer.generateStudyTrialInstanceInfoList();

		// we provide plot number term values for the list
		int i = 0;
		for (final FieldMapLabel fieldMapLabel : infoList.get(0).getTrialInstance().getFieldMapLabels()) {
			fieldMapLabel.setPlotNo(i++);
		}

		// we randomize the arrangement of the list
		Collections.shuffle(infoList.get(0).getTrialInstance().getFieldMapLabels());
		this.labelPrintingServiceImpl.generateLabels(AppConstants.LABEL_PRINTING_CSV.getString(), infoList, labelPrinting);

		int currentPlotNumber = -1;
		for (final FieldMapLabel fieldMapLabel : infoList.get(0).getTrialInstance().getFieldMapLabels()) {
			Assert.assertTrue("Labels were not re-arranged from lowest to highest via entry number",
					fieldMapLabel.getPlotNo() > currentPlotNumber);
			currentPlotNumber = fieldMapLabel.getPlotNo();
		}
	}

	@Test
	public void testPopulateValuesForTrial() {

		final Integer testTermId = TermId.TRIAL_LOCATION.getId();

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		final Map<Integer, String> values = new HashMap<>();
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();

		params.setVariableMap(new HashMap<Integer, MeasurementVariable>());
		for (final MeasurementVariable mv : workbook.getAllVariables()) {
			params.getVariableMap().put(mv.getTermId(), mv);
		}

		params.setEnvironmentData(MeasurementRowTestDataInitializer.createMeasurementRow());

		this.labelPrintingServiceImpl.populateValuesForTrial(params, testTermId, values, true);

		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "Manila",
				values.get(TermId.TRIAL_LOCATION.getId()));
	}

	@Test
	public void testPopulateValuesForNursery() {

		final Integer testTermId = TermId.TRIAL_LOCATION.getId();

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		final Map<Integer, String> values = new HashMap<>();
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();

		this.labelPrintingServiceImpl.populateValuesForNursery(params, workbook, testTermId, values, true);

		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "", values.get(TermId.TRIAL_LOCATION.getId()));
	}

	@Test
	public void testProcessUserSpecificLabelsForInstanceForStockList() {
		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setInstanceInfo(FieldMapTrialInstanceInfoTestDataInitializer.createFieldMapTrialInstanceInfo());
		params.setIsStockList(true);
		params.setInventoryDetailsMap(this.inventoryDetailsInitializer.createInventoryDetailsMap());
		params.setAllFieldIDs(new ArrayList<Integer>());

		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbook(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION, new StudyTypeDto("N"));

		this.labelPrintingServiceImpl.processUserSpecificLabelsForInstance(params, workbook);

		Assert.assertEquals(LabelPrintingServiceImplTest.NO_OF_STOCK_LIST_ENTRIES, params.getInstanceInfo().getFieldMapLabels().size());
	}

	@Test
	public void testProcessUserSpecificLabelsForInstanceForStudy() {

		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbook(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION, new StudyTypeDto("N"));
		this.setExperimentId(workbook);
		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setInstanceInfo(FieldMapTrialInstanceInfoTestDataInitializer.createFieldMapTrialInstanceInfo());
		params.setIsStockList(false);
		params.setInstanceMeasurements(workbook.getObservations());
		params.setAllFieldIDs(new ArrayList<Integer>());

		this.labelPrintingServiceImpl.processUserSpecificLabelsForInstance(params, workbook);

		Assert.assertEquals(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION,
				params.getInstanceInfo().getFieldMapLabels().size());
	}

	@Test
	public void testCheckAndSetFieldMapInstanceInfoForTrialEnvironmentDataOnly() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(2, 2);

		// for trial with stock list
		final boolean isTrial = true;
		final boolean isStockList = true;

		final List<FieldMapTrialInstanceInfo> trialFieldMap = FieldMapTrialInstanceInfoTestDataInitializer.createTrialFieldMapList();
		trialFieldMap.get(0).setTrialInstanceNo("1");

		final LabelPrintingProcessingParams params =
				LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParamsWithAllFieldIDs();
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setStockListId(2);

		final List<InventoryDetails> inventoryDetailList = this.inventoryDetailsInitializer.createInventoryDetailList(1);
		Mockito.when(this.inventoryMiddlewareService.getInventoryListByListDataProjectListId(Matchers.isA(Integer.class))).thenReturn(
				inventoryDetailList);

		Mockito.when(this.pedigreeService.getCrossExpansion(Matchers.isA(Integer.class), Matchers.isA(CrossExpansionProperties.class)))
				.thenReturn("cross");

		final Term term = new Term();
		term.setName("termName");
		Mockito.when(this.ontologyDataManager.getTermById(Matchers.isA(Integer.class))).thenReturn(term);

		this.labelPrintingServiceImpl.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, isStockList, params,
				this.measurementData, this.environmentData, userLabelPrinting);
		try {
			Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(0))
					.getGermplasmListsByProjectId(LabelPrintingServiceImplTest.TEST_STUDY_ID, GermplasmListType.STUDY);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the method processInventorySpecificLabelsForInstance is never invoked.");
		}
	}

	@Test
	public void testCheckAndSetFieldMapInstanceInfoForNurseryEnvironmentWithStocklistData() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(2, new StudyTypeDto("N"));

		// for nursery with stock list
		final boolean isTrial = false;
		final boolean isStockList = true;

		final List<FieldMapTrialInstanceInfo> trialFieldMap =
				FieldMapTrialInstanceInfoTestDataInitializer.createTrialFieldMapList();

		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();

		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setStockListId(4);

		final ArgumentCaptor<Integer> gidCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<CrossExpansionProperties> crossExpansionProperties = ArgumentCaptor.forClass(CrossExpansionProperties.class);

		final int numberOfEntries = 10;
		final List<InventoryDetails> inventoryDetailList = this.inventoryDetailsInitializer.createInventoryDetailList(numberOfEntries);
		this.bulkSomeStocksForTest(inventoryDetailList);
		Mockito.when(this.inventoryMiddlewareService.getInventoryListByListDataProjectListId(Matchers.isA(Integer.class))).thenReturn(
				inventoryDetailList);

		Mockito.when(this.pedigreeService.getCrossExpansion(Matchers.isA(Integer.class), Matchers.isA(CrossExpansionProperties.class)))
				.thenReturn("cross");

		this.labelPrintingServiceImpl.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, isStockList, params,
				this.measurementData, this.environmentData, userLabelPrinting);
		try {
			// verify that the gid passed for getting the cross expansion is not null
			Mockito.verify(this.pedigreeService, Mockito.times(numberOfEntries)).getCrossExpansion(gidCaptor.capture(),
					crossExpansionProperties.capture());
			Assert.assertNotNull(gidCaptor.getValue());

			Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(0)).getGermplasmListsByProjectId(
					LabelPrintingServiceImplTest.TEST_STUDY_ID, GermplasmListType.STUDY);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the method processInventorySpecificLabelsForInstance is never invoked.");
		}

	}

	@Test
	public void testCheckAndSetFieldMapInstanceInfoForGermplsmDescriptorsData() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(2, 2);

		final boolean isTrial = true;
		final boolean isStockList = true;

		final List<FieldMapTrialInstanceInfo> trialFieldMap = FieldMapTrialInstanceInfoTestDataInitializer.createTrialFieldMapList();
		trialFieldMap.get(0).setTrialInstanceNo("1");

		final LabelPrintingProcessingParams params =
				LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParamsWithGermplsmDescriptorsFields();
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setStockListId(2);

		final List<InventoryDetails> inventoryDetailList = this.inventoryDetailsInitializer.createInventoryDetailList(1);
		Mockito.when(this.inventoryMiddlewareService.getInventoryListByListDataProjectListId(Matchers.isA(Integer.class))).thenReturn(
				inventoryDetailList);

		Mockito.when(this.pedigreeService.getCrossExpansion(Matchers.isA(Integer.class), Matchers.isA(CrossExpansionProperties.class)))
				.thenReturn("cross");


		final Term groupGid = new Term();
		groupGid.setName(TermId.GROUPGID.name());
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GROUPGID.getId())).thenReturn(groupGid);

		final Term seedSource = new Term();
		seedSource.setName(TermId.SEED_SOURCE.name());
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(seedSource);


		this.labelPrintingServiceImpl.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, isStockList, params,
				this.measurementData, this.environmentData, userLabelPrinting);

		Assert.assertEquals(2, params.getLabelHeaders().size());
		Assert.assertEquals(TermId.GROUPGID.name(), params.getLabelHeaders().get(TermId.GROUPGID.getId()));
		Assert.assertEquals(TermId.SEED_SOURCE.name(), params.getLabelHeaders().get(TermId.SEED_SOURCE.getId()));

		Assert.assertEquals(2, params.getUserSpecifiedLabels().size());
		Assert.assertEquals(inventoryDetailList.get(0).getSource(), params.getUserSpecifiedLabels().get(TermId.SEED_SOURCE.getId()));
		Assert.assertEquals(inventoryDetailList.get(0).getGroupId().toString(), params.getUserSpecifiedLabels().get(TermId.GROUPGID.getId()));

	}

	/**
	 * Bulk the first two entries to the 3rd entry (for testing purposes)
	 * 
	 * @param inventoryDetailList
	 */
	private void bulkSomeStocksForTest(final List<InventoryDetails> inventoryDetailList) {
		final InventoryDetails firstEntry = inventoryDetailList.get(0);
		final InventoryDetails secondEntry = inventoryDetailList.get(1);
		final InventoryDetails thirdEntry = inventoryDetailList.get(2);

		this.setAsBulkingDonor(firstEntry);
		this.setAsBulkingDonor(secondEntry);
		this.setAsBulkingRecipient(thirdEntry);

	}

	private void setAsBulkingDonor(final InventoryDetails entry) {
		entry.setBulkCompl(InventoryDetails.BULK_COMPL_COMPLETED);
		// just make the source record id not equal to the stock source record id
		entry.setSourceRecordId(this.getRandomNumber(10, 20));
		entry.setStockSourceRecordId(entry.getSourceRecordId() + 20);
	}

	private void setAsBulkingRecipient(final InventoryDetails entry) {
		entry.setBulkCompl(InventoryDetails.BULK_COMPL_COMPLETED);
		// just make the source record id equal to the stock source record id
		entry.setSourceRecordId(this.getRandomNumber(10, 20));
		entry.setStockSourceRecordId(entry.getSourceRecordId());
	}

	private int getRandomNumber(final int minValue, final int maxValue) {
		final Random rn = new Random();
		final int range = maxValue - minValue + 1;
		return rn.nextInt(range) + minValue;
	}

	@Test
	public void testCheckAndSetFieldMapInstanceInfoForNurseryEnvironmentWithoutStocklistData() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(2, new StudyTypeDto("N"));
		workbook.getStudyDetails().setId(LabelPrintingServiceImplTest.TEST_STUDY_ID);

		// for nursery without stock list
		final boolean isTrial = false;
		final boolean isStockList = false;

		final List<FieldMapTrialInstanceInfo> trialFieldMap = FieldMapTrialInstanceInfoTestDataInitializer.createTrialFieldMapList();
		final LabelPrintingProcessingParams params =
				LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParamsWithAllFieldIDs();

		this.labelPrintingServiceImpl.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, isStockList, params,
				this.measurementData, this.environmentData, null);

		try {
			Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1))
					.getGermplasmListsByProjectId(LabelPrintingServiceImplTest.TEST_STUDY_ID, GermplasmListType.STUDY);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting that the method processInventorySpecificLabelsForInstance is invoked.");
		}
	}

	@Test
	public void testGetAvailableLabelFieldsForStockListForNursery() {

		Mockito.when(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY,
				null, Locale.getDefault())).thenReturn("Nursery Name");

		final Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(this.fieldbookMiddlewareService.getNurseryDataSet(101)).thenReturn(workbook);

		final List<LabelFields> nurseryManagementLabelFields = LabelPrintingServiceDataInitializer.createNurseryManagementLabelFields();
		Mockito.when(this.settingsService.retrieveNurseryManagementDetailsAsLabels(Matchers.isA(Workbook.class))).
				thenReturn(nurseryManagementLabelFields);

		final List<LabelFields> germplsmDescriptorsLabelFields = LabelPrintingServiceDataInitializer.createGermplsmDescriptorsLabelFields();
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(Matchers.isA(Workbook.class))).
				thenReturn(germplsmDescriptorsLabelFields);

		final Term plotNoTerm = new Term();
		plotNoTerm.setName("plotNoTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.PLOT_NO.getId())).thenReturn(plotNoTerm);

		final Term stockIDTerm = new Term();
		stockIDTerm.setName("stockIDTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(stockIDTerm);

		final Term lotLocationTerm = new Term();
		lotLocationTerm.setName("lotLocationTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.LOT_LOCATION_INVENTORY.getId())).thenReturn(lotLocationTerm);

		final Term amountTerm = new Term();
		amountTerm.setName("amountTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AMOUNT_INVENTORY.getId())).thenReturn(amountTerm);

		final Term unitsTerm = new Term();
		unitsTerm.setName("unitsTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.UNITS_INVENTORY.getId())).thenReturn(unitsTerm);

		final Term commentTerm = new Term();
		commentTerm.setName("commentTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.COMMENT_INVENTORY.getId())).thenReturn(commentTerm);


		final List<LabelFields> labelFieldForNurseryStock =
				this.labelPrintingServiceImpl.getAvailableLabelFieldsForStockList(GermplasmListType.LST, Locale.getDefault(),101);

		Assert.assertEquals(9, labelFieldForNurseryStock.size());

		final Set<String> labelFieldsNames = new HashSet<>();
		for(final LabelFields labelFields : labelFieldForNurseryStock) {
			labelFieldsNames.add(labelFields.getName());
		}

		Assert.assertTrue(labelFieldsNames.contains("Nursery Name"));
		Assert.assertTrue(labelFieldsNames.contains(nurseryManagementLabelFields.get(0).getName()));
		Assert.assertTrue(labelFieldsNames.contains(germplsmDescriptorsLabelFields.get(0).getName()));
		Assert.assertTrue(labelFieldsNames.contains(plotNoTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(stockIDTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(lotLocationTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(amountTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(unitsTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(commentTerm.getName()));
	}

	@Test
	public void testGetAvailableLabelFieldsForStockListForTrial() {

		Mockito.when(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME_KEY, null,
				Locale.getDefault())).thenReturn("Trial Name");

		final Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(101)).thenReturn(workbook);

		final List<LabelFields> trialSettingLabelFields = LabelPrintingServiceDataInitializer.createTrialSettingLabelFields();
		Mockito.when(this.settingsService.retrieveTrialSettingsAsLabels(Matchers.isA(Workbook.class))).
				thenReturn(trialSettingLabelFields);

		final List<LabelFields> environmentSettingsLabelFields =
				LabelPrintingServiceDataInitializer.createEnvironmentSettingsLabelFields();
		Mockito.when(this.settingsService.retrieveTrialEnvironmentConditionsAsLabels(Matchers.isA(Workbook.class))).
				thenReturn(environmentSettingsLabelFields);

		final List<LabelFields> germplsmDescriptorsLabelFields = LabelPrintingServiceDataInitializer.createGermplsmDescriptorsLabelFields();
		Mockito.when(this.settingsService.retrieveGermplasmDescriptorsAsLabels(Matchers.isA(Workbook.class))).
				thenReturn(germplsmDescriptorsLabelFields);

		final Term repNoTerm = new Term();
		repNoTerm.setName("repNoTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.REP_NO.getId())).thenReturn(repNoTerm);


		final Term plotNoTerm = new Term();
		plotNoTerm.setName("plotNoTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.PLOT_NO.getId())).thenReturn(plotNoTerm);

		final Term stockIDTerm = new Term();
		stockIDTerm.setName("stockIDTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(stockIDTerm);

		final Term lotLocationTerm = new Term();
		lotLocationTerm.setName("lotLocationTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.LOT_LOCATION_INVENTORY.getId())).thenReturn(lotLocationTerm);

		final Term amountTerm = new Term();
		amountTerm.setName("amountTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AMOUNT_INVENTORY.getId())).thenReturn(amountTerm);

		final Term unitsTerm = new Term();
		unitsTerm.setName("unitsTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.UNITS_INVENTORY.getId())).thenReturn(unitsTerm);

		final Term commentTerm = new Term();
		commentTerm.setName("commentTerm");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.COMMENT_INVENTORY.getId())).thenReturn(commentTerm);


		final List<LabelFields> labelFieldForNurseryStock =
				this.labelPrintingServiceImpl.getAvailableLabelFieldsForStockList(GermplasmListType.LST, Locale.getDefault(), 101);

		Assert.assertEquals(11, labelFieldForNurseryStock.size());

		final Set<String> labelFieldsNames = new HashSet<>();
		for(final LabelFields labelFields : labelFieldForNurseryStock) {
			labelFieldsNames.add(labelFields.getName());
		}

		Assert.assertTrue(labelFieldsNames.contains("Trial Name"));
		Assert.assertTrue(labelFieldsNames.contains(trialSettingLabelFields.get(0).getName()));
		Assert.assertTrue(labelFieldsNames.contains(environmentSettingsLabelFields.get(0).getName()));
		Assert.assertTrue(labelFieldsNames.contains(germplsmDescriptorsLabelFields.get(0).getName()));
		Assert.assertTrue(labelFieldsNames.contains(repNoTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(plotNoTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(stockIDTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(lotLocationTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(amountTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(unitsTerm.getName()));
		Assert.assertTrue(labelFieldsNames.contains(commentTerm.getName()));
	}


	private void setExperimentId(final Workbook workbook) {
		int i = 1;
		for (final MeasurementRow measurement : workbook.getObservations()) {
			measurement.setExperimentId(i);
			i++;
		}
	}

}
