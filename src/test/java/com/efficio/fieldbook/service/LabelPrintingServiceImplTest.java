
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
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
	LabelPrintingServiceImpl serviceDUT = Mockito.spy(new LabelPrintingServiceImpl());

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

		Mockito.when(
				this.presetDataManager.getProgramPresetFromProgramAndToolByName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23, ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(
				notEmptySearchResult);

		Mockito.when(
				this.presetDataManager.getProgramPresetFromProgramAndToolByName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23, ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(
				new ArrayList<ProgramPreset>());

		Mockito.when(
				this.presetDataManager.getProgramPresetFromProgramAndTool(LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID, 23,
						ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(notEmptySearchResult);

		final ArrayList<StandardPreset> standardPresetSearchResults = new ArrayList<>();
		final StandardPreset sp = new StandardPreset();
		sp.setName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME);
		sp.setCropName(LabelPrintingServiceImplTest.MAIZE_CROP_STR);
		sp.setToolId(23);
		sp.setToolSection(ToolSection.FBK_LABEL_PRINTING.name());
		sp.setConfiguration(LabelPrintingServiceImplTest.STANDARD_PRESET_CONFIG);
		standardPresetSearchResults.add(sp);

		Mockito.when(
				this.workbenchService.getStandardPresetByCropAndPresetName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME, 23,
						LabelPrintingServiceImplTest.MAIZE_CROP_STR, ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(
				standardPresetSearchResults);
		Mockito.when(
				this.workbenchService.getStandardPresetByCropAndPresetName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME, 23,
						LabelPrintingServiceImplTest.MAIZE_CROP_STR, ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(
				new ArrayList<StandardPreset>());

		Mockito.when(
				this.workbenchService.getStandardPresetByCrop(23, LabelPrintingServiceImplTest.MAIZE_CROP_STR,
						ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(standardPresetSearchResults);

		Mockito.when(this.workbenchService.getStandardPresetById(LabelPrintingServiceImplTest.TEST_PRESET_ID)).thenReturn(sp);
		Mockito.when(this.presetDataManager.getProgramPresetById(LabelPrintingServiceImplTest.TEST_PRESET_ID)).thenReturn(
				searchResultPreset);

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(LabelPrintingServiceImplTest.DUMMY_PROGRAM_UUID);
	}

	@Test
	public void testGetAllLabelPrintingPresets() throws Exception {
		List<LabelPrintingPresets> presetList =
				this.serviceDUT.getAllLabelPrintingPresets(LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue());

		Assert.assertTrue("should have more than 1 item", presetList.size() > 1);
	}

	@Test
	public void testGetLabelPrintingPresetConfig() throws Exception {
		String config =
				this.serviceDUT.getLabelPrintingPresetConfig(LabelPrintingServiceImplTest.TEST_PRESET_ID,
						LabelPrintingPresets.STANDARD_PRESET);

		Assert.assertEquals("should retrieve a standard preset config", LabelPrintingServiceImplTest.STANDARD_PRESET_CONFIG, config);

		config =
				this.serviceDUT.getLabelPrintingPresetConfig(LabelPrintingServiceImplTest.TEST_PRESET_ID,
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
		List<LabelPrintingPresets> programPresetList =
				this.serviceDUT.getAllLabelPrintingPresetsByName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue(), LabelPrintingPresets.PROGRAM_PRESET);

		List<LabelPrintingPresets> emptyProgramPresetList =
				this.serviceDUT.getAllLabelPrintingPresetsByName(LabelPrintingServiceImplTest.TEST_NON_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue(), LabelPrintingPresets.PROGRAM_PRESET);

		List<LabelPrintingPresets> standardPresetList =
				this.serviceDUT.getAllLabelPrintingPresetsByName(LabelPrintingServiceImplTest.TEST_EXISTING_PRESET_NAME,
						LabelPrintingServiceImplTest.TEST_PROJECT_ID.intValue(), LabelPrintingPresets.STANDARD_PRESET);

		List<LabelPrintingPresets> emptyStandardPresetList =
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
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		Integer studyId = workbook.getStudyDetails().getId();
		List<GermplasmList> germplasmLists = this.createGermplasmLists(1);
		GermplasmList germplasmList = germplasmLists.get(0);
		Integer numOfEntries = germplasmList.getListData().size();
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.NURSERY)).thenReturn(
				germplasmLists);
		Mockito.when(this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(germplasmList.getId(), null)).thenReturn(
				this.createInventoryDetailList(numOfEntries));

		Assert.assertTrue("Expecting to return true for germplasm list entries with inventory details.",
				this.serviceDUT.hasInventoryValues(studyId, workbook.isNursery()));
	}

	@Test
	public void testHasInventoryValues_ReturnsFalseForEntriesWithoutInventory() throws MiddlewareQueryException {
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		Integer studyId = workbook.getStudyDetails().getId();
		List<GermplasmList> germplasmLists = this.createGermplasmLists(1);
		GermplasmList germplasmList = germplasmLists.get(0);
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.NURSERY)).thenReturn(
				germplasmLists);
		Mockito.when(this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(germplasmList.getId())).thenReturn(
				new ArrayList<InventoryDetails>());

		Assert.assertFalse("Expecting to return false for germplasm list entries with inventory details.",
				this.serviceDUT.hasInventoryValues(studyId, workbook.isNursery()));
	}

	@Test
	public void testEncodeBardcodeInEnglishCharacters() {
		BitMatrix bitMatrix = this.serviceDUT.encodeBarcode("Test", 100, 200);
		Assert.assertNotNull("Bit Matrix Barcode should be not null since characters are in English ASCII", bitMatrix);
	}

	@Test
	public void testEncodeBardcodeInNonEnglishCharacters() {
		BitMatrix bitMatrix = this.serviceDUT.encodeBarcode("乙七九", 100, 200);
		Assert.assertNull("Bit Matrix Barcode should be null since parameter is non-english ascii", bitMatrix);
	}

	@Test
	public void testPopulateValuesFromMeasurementNoData() {

		Map<Integer, String> values = new HashMap<>();
		LabelPrintingProcessingParams params = this.createLabelPrintingProcessingParams();
		MeasurementRow measurementRow = this.createMeasurementRow();

		Boolean hasData = this.serviceDUT.populateValuesFromMeasurement(params, measurementRow, 1, values, true);

		Assert.assertFalse("should be false", hasData);
	}

	@Test
	public void testPopulateValuesFromMeasurementWithData() {

		Map<Integer, String> values = new HashMap<>();
		LabelPrintingProcessingParams params = this.createLabelPrintingProcessingParams();
		MeasurementRow measurementRow = this.createMeasurementRow();

		Boolean hasData =
				this.serviceDUT.populateValuesFromMeasurement(params, measurementRow, TermId.TRIAL_LOCATION.getId(), values, true);

		Assert.assertTrue("Should be true", hasData);
		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "Manila", values.get(TermId.TRIAL_LOCATION.getId()));

	}

	@Test
	public void testPopulateValuesForTrial() {

		Integer testTermId = TermId.TRIAL_LOCATION.getId();

		Mockito.doReturn(testTermId).when(this.serviceDUT).getCounterpartTermId(Matchers.anyInt());

		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		Map<Integer, String> values = new HashMap<>();
		LabelPrintingProcessingParams params = this.createLabelPrintingProcessingParams();

		params.setVariableMap(new HashMap<Integer, MeasurementVariable>());
		for (MeasurementVariable mv : workbook.getAllVariables()) {
			params.getVariableMap().put(mv.getTermId(), mv);
		}

		params.setEnvironmentData(this.createMeasurementRow());

		this.serviceDUT.populateValuesForTrial(params, workbook, testTermId, values, true);

		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "Manila", values.get(TermId.TRIAL_LOCATION.getId()));
	}

	@Test
	public void testPopulateValuesForNursery() {

		Integer testTermId = TermId.TRIAL_LOCATION.getId();

		Mockito.doReturn(testTermId).when(this.serviceDUT).getCounterpartTermId(Matchers.anyInt());

		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		Map<Integer, String> values = new HashMap<>();
		LabelPrintingProcessingParams params = this.createLabelPrintingProcessingParams();

		this.serviceDUT.populateValuesForNursery(params, workbook, testTermId, values, true);

		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "", values.get(TermId.TRIAL_LOCATION.getId()));
	}

	@Test
	public void testProcessUserSpecificLabelsForInstanceForStockList() {
		LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setInstanceInfo(this.createFieldMapTrialInstanceInfo());
		params.setIsStockList(true);
		params.setInventoryDetailsMap(this.createInventoryDetailsMap());

		Workbook workbook = WorkbookDataUtil.getTestWorkbook(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION, StudyType.N);

		Map<Integer, String> userSpecifiedLabels = new HashMap<Integer, String>();
		for (InventoryDetails inventoryDetail : params.getInventoryDetailsMap().values()) {
			Mockito.doReturn(userSpecifiedLabels).when(this.serviceDUT)
					.extractDataForUserSpecifiedLabels(params, null, inventoryDetail, true, workbook);
			Mockito.doReturn(userSpecifiedLabels).when(this.serviceDUT)
					.extractDataForUserSpecifiedLabels(params, null, inventoryDetail, false, workbook);
		}

		this.serviceDUT.processUserSpecificLabelsForInstance(params, workbook);

		Assert.assertEquals(LabelPrintingServiceImplTest.NO_OF_STOCK_LIST_ENTRIES, params.getInstanceInfo().getFieldMapLabels().size());
	}

	@Test
	public void testProcessUserSpecificLabelsForInstanceForStudy() {
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION, StudyType.N);
		this.setExperimentId(workbook);
		LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setInstanceInfo(this.createFieldMapTrialInstanceInfo());
		params.setIsStockList(false);
		params.setInstanceMeasurements(workbook.getObservations());

		Map<Integer, String> userSpecifiedLabels = new HashMap<Integer, String>();
		for (MeasurementRow measurement : params.getInstanceMeasurements()) {
			Mockito.doReturn(userSpecifiedLabels).when(this.serviceDUT)
					.extractDataForUserSpecifiedLabels(params, measurement, null, true, workbook);
			Mockito.doReturn(userSpecifiedLabels).when(this.serviceDUT)
					.extractDataForUserSpecifiedLabels(params, measurement, null, false, workbook);
		}

		this.serviceDUT.processUserSpecificLabelsForInstance(params, workbook);

		Assert.assertEquals(LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION, params.getInstanceInfo().getFieldMapLabels()
				.size());
	}

	private void setExperimentId(Workbook workbook) {
		int i = 1;
		for (MeasurementRow measurement : workbook.getObservations()) {
			measurement.setExperimentId(i);
			i++;
		}
	}

	private FieldMapTrialInstanceInfo createFieldMapTrialInstanceInfo() {
		FieldMapTrialInstanceInfo instanceInfo = new FieldMapTrialInstanceInfo();
		instanceInfo.setFieldMapLabels(this.createFieldMapLabelList());
		return instanceInfo;
	}

	private List<FieldMapLabel> createFieldMapLabelList() {
		List<FieldMapLabel> labelFields = new ArrayList<FieldMapLabel>();

		for (int i = 1; i <= LabelPrintingServiceImplTest.NO_OF_GERMPLASM_LIST_OBSERVATION; i++) {
			FieldMapLabel fieldMapLabel = new FieldMapLabel();
			fieldMapLabel.setExperimentId(i);
			labelFields.add(fieldMapLabel);
		}
		return labelFields;
	}

	private Map<String, InventoryDetails> createInventoryDetailsMap() {
		Map<String, InventoryDetails> inventoryDetails = new HashMap<String, InventoryDetails>();

		for (int i = 1; i <= LabelPrintingServiceImplTest.NO_OF_STOCK_LIST_ENTRIES; i++) {
			inventoryDetails.put(String.valueOf(i), new InventoryDetails());
		}

		return inventoryDetails;
	}

	private List<GermplasmList> createGermplasmLists(int numOfEntries) {
		List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();

		for (int i = 0; i < numOfEntries; i++) {
			Integer id = i + 1;
			GermplasmList germplasmList = new GermplasmList();
			germplasmList.setId(id);
			germplasmList.setName("List " + id);
			germplasmList.setDescription("Description " + id);
			germplasmList.setListData(this.getGermplasmListData(numOfEntries));

			germplasmLists.add(germplasmList);
		}

		return germplasmLists;
	}

	private List<GermplasmListData> getGermplasmListData(int numOfEntries) {
		List<GermplasmListData> germplasmListData = new ArrayList<GermplasmListData>();

		for (int i = 0; i < numOfEntries; i++) {
			Integer id = i + 1;
			GermplasmListData listData = new GermplasmListData();
			listData.setId(id);
			listData.setDesignation("Designation" + id);
			listData.setEntryCode("EntryCode" + id);
			listData.setGroupName("GroupName" + id);

			germplasmListData.add(listData);
		}

		return germplasmListData;
	}

	private List<InventoryDetails> createInventoryDetailList(Integer numOfEntries) {
		List<InventoryDetails> inventoryDetails = new ArrayList<InventoryDetails>();

		for (int i = 0; i < numOfEntries; i++) {
			int id = i + 1;
			InventoryDetails invDetails = new InventoryDetails();
			invDetails.setLotId(id);
			invDetails.setGid(id);
			inventoryDetails.add(invDetails);
		}

		return inventoryDetails;
	}

	private LabelPrintingProcessingParams createLabelPrintingProcessingParams() {
		LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setLabelHeaders(new HashMap<Integer, String>());
		return params;
	}

	private MeasurementRow createMeasurementRow() {

		MeasurementRow measurementRow = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.createMeasurementData(TermId.LOCATION_ID.getId(), "LOCATION_ID", "123"));
		dataList.add(this.createMeasurementData(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "Manila"));
		dataList.add(this.createMeasurementData(111, "Some Variable", "Test Data"));
		measurementRow.setDataList(dataList);

		return measurementRow;
	}

	private MeasurementData createMeasurementData(Integer termId, String label, String value) {
		MeasurementData measurementData = new MeasurementData(label, value);
		measurementData.setDataType("C");
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());

		measurementData.setMeasurementVariable(measurementVariable);
		return measurementData;
	}

}
