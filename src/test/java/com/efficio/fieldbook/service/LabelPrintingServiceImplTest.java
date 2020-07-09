package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.SettingsService;
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
import com.efficio.pojos.labelprinting.LabelPrintingProcessingParams;
import org.generationcp.commons.constant.AppConstants;
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
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
	private PresetService presetService;

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

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@InjectMocks
	private LabelPrintingServiceImpl labelPrintingServiceImpl;

	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;

	@Before
	public void beforeTest() {
		this.germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();
		// init data initializer
		this.inventoryDetailsInitializer = new InventoryDetailsTestDataInitializer();

		final Project project = Mockito.mock(Project.class);
		final Tool fieldbookWeb = new Tool();
		fieldbookWeb.setToolId(23L);
		fieldbookWeb.setToolName(ToolName.FIELDBOOK_WEB.getName());

		Mockito.when(this.workbenchDataManager.getToolWithName(ToolName.FIELDBOOK_WEB.getName())).thenReturn(fieldbookWeb);

		// init mocks
		final ArrayList<ProgramPreset> notEmptySearchResult = new ArrayList<>();
		final ProgramPreset searchResultPreset = new ProgramPreset();
		searchResultPreset.setProgramUuid(LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID);
		searchResultPreset.setToolSection(ToolSection.PLANTING_LABEL_PRINTING_PRESET.name());
		searchResultPreset.setToolId(23);
		searchResultPreset.setName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME);
		searchResultPreset.setConfiguration(LabelPrintingServiceImplTest.PROGRAM_PRESET_CONFIG);
		notEmptySearchResult.add(searchResultPreset);

		Mockito.when(this.presetService.getProgramPresetFromProgramAndToolByName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
			LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23, ToolSection.PLANTING_LABEL_PRINTING_PRESET.name()))
			.thenReturn(notEmptySearchResult);

		Mockito.when(
			this.presetService.getProgramPresetFromProgramAndToolByName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME,
				LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23, ToolSection.PLANTING_LABEL_PRINTING_PRESET.name()))
			.thenReturn(new ArrayList<ProgramPreset>());

		Mockito.when(this.presetService.getProgramPresetFromProgramAndTool(LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23,
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

		Mockito.when(this.workbenchDataManager.getStandardPresetById(LabelPrintingServiceImplTest.TEST_PRESET_ID)).thenReturn(sp);
		Mockito.when(this.presetService.getProgramPresetById(LabelPrintingServiceImplTest.TEST_PRESET_ID))
			.thenReturn(searchResultPreset);

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID);
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
		Mockito.doReturn(labelGenerator).when(this.labelGeneratorFactory)
			.retrieveLabelGenerator(AppConstants.LABEL_PRINTING_CSV.getString());

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
		Mockito.doReturn(labelGenerator).when(this.labelGeneratorFactory)
			.retrieveLabelGenerator(AppConstants.LABEL_PRINTING_CSV.getString());

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
	public void testPopulateValuesForStudy() {

		final Integer testTermId = TermId.TRIAL_LOCATION.getId();

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		final Map<Integer, String> values = new HashMap<>();
		final LabelPrintingProcessingParams params = LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParams();

		params.setVariableMap(new HashMap<Integer, MeasurementVariable>());
		for (final MeasurementVariable mv : workbook.getAllVariables()) {
			params.getVariableMap().put(mv.getTermId(), mv);
		}

		params.setEnvironmentData(MeasurementRowTestDataInitializer.createMeasurementRow());

		this.labelPrintingServiceImpl.populateValuesForStudy(params, testTermId, values, true, workbook);

		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "Manila",
			values.get(TermId.TRIAL_LOCATION.getId()));
	}

	@Test
	public void testProcessUserSpecificLabelsForInstanceForStudy() {

		final Workbook workbook =
			WorkbookDataUtil.getTestWorkbook(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION, new StudyTypeDto("N"));
		this.setExperimentId(workbook);
		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setInstanceInfo(
			FieldMapTrialInstanceInfoTestDataInitializer.createFieldMapTrialInstanceInfo(workbook.getObservations().size()));
		params.setInstanceMeasurements(workbook.getObservations());
		params.setAllFieldIDs(new ArrayList<Integer>());

		this.labelPrintingServiceImpl.processUserSpecificLabelsForInstance(params, workbook);

		Assert.assertEquals(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION * workbook.getTotalNumberOfInstances(),
			params.getInstanceInfo().getFieldMapLabels().size());
	}

	@Test
	public void testCheckAndSetFieldMapInstanceInfoForStudyEnvironmentDataOnly() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(2, 2);

		final List<FieldMapTrialInstanceInfo> fieldMapList = FieldMapTrialInstanceInfoTestDataInitializer.createTrialFieldMapList();
		fieldMapList.get(0).setTrialInstanceNo("1");

		final LabelPrintingProcessingParams params =
			LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParamsWithAllFieldIDs();

		final List<InventoryDetails> inventoryDetailList = this.inventoryDetailsInitializer.createInventoryDetailList(1);
		Mockito.when(this.inventoryMiddlewareService.getInventoryListByListDataProjectListId(Matchers.isA(Integer.class))).thenReturn(
			inventoryDetailList);

		final Term term = new Term();
		term.setName("termName");
		Mockito.when(this.ontologyDataManager.getTermById(Matchers.isA(Integer.class))).thenReturn(term);

		this.labelPrintingServiceImpl.checkAndSetFieldMapInstanceInfo(fieldMapList, workbook, params,
			this.measurementData, this.environmentData);
		try {
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the method processInventorySpecificLabelsForInstance is never invoked.");
		}
	}

	private int getRandomNumber(final int minValue, final int maxValue) {
		final Random rn = new Random();
		final int range = maxValue - minValue + 1;
		return rn.nextInt(range) + minValue;
	}

	@Test
	public void testCheckAndSetFieldMapInstanceInfoForNurseryEnvironment() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(2, new StudyTypeDto("N"));
		workbook.getStudyDetails().setId(LabelPrintingServiceImplTest.TEST_STUDY_ID);

		final List<FieldMapTrialInstanceInfo> trialFieldMap = FieldMapTrialInstanceInfoTestDataInitializer.createTrialFieldMapList();
		final LabelPrintingProcessingParams params =
			LabelPrintingProcessingParamsTestDataInitializer.createLabelPrintingProcessingParamsWithAllFieldIDs();

		this.labelPrintingServiceImpl.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, params,
			this.measurementData, this.environmentData);

		try {
		} catch (final TooLittleActualInvocations e) {
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
