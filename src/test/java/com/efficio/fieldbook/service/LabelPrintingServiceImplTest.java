package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.pojos.labelprinting.LabelPrintingProcessingParams;
import com.google.zxing.common.BitMatrix;

import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class LabelPrintingServiceImplTest {

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

		final Project project = mock(Project.class);
		when(project.getCropType()).thenReturn(new CropType(MAIZE_CROP_STR));

		final Tool fieldbookWeb = new Tool();
		fieldbookWeb.setToolId(23L);
		fieldbookWeb.setToolName("fieldbook_web");

		when(workbenchService.getFieldbookWebTool()).thenReturn(fieldbookWeb);

		// init mocks
		when(workbenchService.getProjectById(TEST_PROJECT_ID)).thenReturn(project);

		final ArrayList<ProgramPreset> notEmptySearchResult = new ArrayList<>();
		final ProgramPreset searchResultPreset = new ProgramPreset();
		searchResultPreset.setProgramUuid(DUMMY_PROGRAM_UUID);
		searchResultPreset.setToolSection(ToolSection.FBK_LABEL_PRINTING.name());
		searchResultPreset.setToolId(23);
		searchResultPreset.setName(TEST_EXISTING_PRESET_NAME);
		searchResultPreset.setConfiguration(PROGRAM_PRESET_CONFIG);
		notEmptySearchResult.add(searchResultPreset);

		when(presetDataManager.getProgramPresetFromProgramAndToolByName(TEST_EXISTING_PRESET_NAME,
				DUMMY_PROGRAM_UUID, 23,
				ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(notEmptySearchResult);

		when(presetDataManager.getProgramPresetFromProgramAndToolByName(
				TEST_NON_EXISTING_PRESET_NAME,
				DUMMY_PROGRAM_UUID, 23,
				ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(new ArrayList<ProgramPreset>());

		when(presetDataManager.getProgramPresetFromProgramAndTool(DUMMY_PROGRAM_UUID, 23,
				ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(
				notEmptySearchResult);

		final ArrayList<StandardPreset> standardPresetSearchResults = new ArrayList<>();
		final StandardPreset sp = new StandardPreset();
		sp.setName(TEST_EXISTING_PRESET_NAME);
		sp.setCropName(MAIZE_CROP_STR);
		sp.setToolId(23);
		sp.setToolSection(ToolSection.FBK_LABEL_PRINTING.name());
		sp.setConfiguration(STANDARD_PRESET_CONFIG);
		standardPresetSearchResults.add(sp);

		when(workbenchService
				.getStandardPresetByCropAndPresetName(TEST_EXISTING_PRESET_NAME, 23, MAIZE_CROP_STR,
						ToolSection.FBK_LABEL_PRINTING.name()))
				.thenReturn(standardPresetSearchResults);
		when(workbenchService
				.getStandardPresetByCropAndPresetName(TEST_NON_EXISTING_PRESET_NAME, 23,
						MAIZE_CROP_STR, ToolSection.FBK_LABEL_PRINTING.name()))
				.thenReturn(new ArrayList<StandardPreset>());

		when(workbenchService.getStandardPresetByCrop(23, MAIZE_CROP_STR,
				ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(standardPresetSearchResults);

		when(workbenchService.getStandardPresetById(TEST_PRESET_ID)).thenReturn(sp);
		when(presetDataManager.getProgramPresetById(TEST_PRESET_ID)).thenReturn(searchResultPreset);
		
		when(contextUtil.getCurrentProgramUUID()).thenReturn(DUMMY_PROGRAM_UUID);
	}

	@Test
	public void testGetAllLabelPrintingPresets() throws Exception {
		List<LabelPrintingPresets> presetList = serviceDUT.getAllLabelPrintingPresets(
				TEST_PROJECT_ID.intValue());

		assertTrue("should have more than 1 item", presetList.size() > 1);
	}

	@Test
	public void testGetLabelPrintingPresetConfig() throws Exception {
		String config = serviceDUT.getLabelPrintingPresetConfig(
				TEST_PRESET_ID, LabelPrintingPresets.STANDARD_PRESET);

		assertEquals("should retrieve a standard preset config", STANDARD_PRESET_CONFIG, config);

		config = serviceDUT.getLabelPrintingPresetConfig(
				TEST_PRESET_ID, LabelPrintingPresets.PROGRAM_PRESET);

		assertEquals("should retrieve a program preset config", PROGRAM_PRESET_CONFIG, config);
	}

	@Test
	public void testGetLabelPrintingProgramPreset() throws Exception {
		LabelPrintingPresets preset = serviceDUT.getLabelPrintingPreset(
				TEST_PRESET_ID, LabelPrintingPresets.STANDARD_PRESET);

		assertEquals("should retrieve a standard preset", TEST_EXISTING_PRESET_NAME,
				preset.getName());

		preset = serviceDUT.getLabelPrintingPreset(
				TEST_PRESET_ID, LabelPrintingPresets.PROGRAM_PRESET);

		assertEquals("should retrieve a program preset", TEST_EXISTING_PRESET_NAME,
				preset.getName());

	}

	@Test
	public void testGetAllLabelPrintingPresetsByName() throws Exception {
		List<LabelPrintingPresets> programPresetList = serviceDUT
				.getAllLabelPrintingPresetsByName(TEST_EXISTING_PRESET_NAME,
						TEST_PROJECT_ID.intValue(),
						LabelPrintingPresets.PROGRAM_PRESET);

		List<LabelPrintingPresets> emptyProgramPresetList = serviceDUT
				.getAllLabelPrintingPresetsByName(TEST_NON_EXISTING_PRESET_NAME,
						TEST_PROJECT_ID.intValue(),
						LabelPrintingPresets.PROGRAM_PRESET);

		List<LabelPrintingPresets> standardPresetList = serviceDUT
				.getAllLabelPrintingPresetsByName(TEST_EXISTING_PRESET_NAME,
						TEST_PROJECT_ID.intValue(),
						LabelPrintingPresets.STANDARD_PRESET);

		List<LabelPrintingPresets> emptyStandardPresetList = serviceDUT
				.getAllLabelPrintingPresetsByName(TEST_NON_EXISTING_PRESET_NAME,
						TEST_PROJECT_ID.intValue(),
						LabelPrintingPresets.STANDARD_PRESET);

		assertTrue("should have an item", standardPresetList.size() > 0);
		assertTrue("should have an item", programPresetList.size() > 0);

		assertTrue("should be empty", emptyProgramPresetList.isEmpty());
		assertTrue("should be empty", emptyStandardPresetList.isEmpty());

		assertEquals("should be the same item as we searched on", TEST_EXISTING_PRESET_NAME,
				programPresetList.get(0).getName());
		assertEquals("should be the same item as we searched on", TEST_EXISTING_PRESET_NAME,
				standardPresetList.get(0).getName());
	}
	
	
	@Test
	public void testHasInventoryValues_ReturnsTrueForEntriesWithInventory() throws MiddlewareQueryException{
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		Integer studyId = workbook.getStudyDetails().getId();
		List<GermplasmList> germplasmLists = createGermplasmLists(1);
		GermplasmList germplasmList = germplasmLists.get(0);
		Integer numOfEntries = germplasmList.getListData().size();
		when(fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.NURSERY)).thenReturn(germplasmLists);
		when(inventoryMiddlewareService.getInventoryDetailsByGermplasmList(germplasmList.getId(),null)).thenReturn(createInventoryDetailList(numOfEntries));
		
		Assert.assertTrue("Expecting to return true for germplasm list entries with inventory details.",serviceDUT.hasInventoryValues(studyId, workbook.isNursery()));
	}
	
	@Test
	public void testHasInventoryValues_ReturnsFalseForEntriesWithoutInventory() throws MiddlewareQueryException{
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		Integer studyId = workbook.getStudyDetails().getId();
		List<GermplasmList> germplasmLists = createGermplasmLists(1);
		GermplasmList germplasmList = germplasmLists.get(0);
		when(fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.NURSERY)).thenReturn(germplasmLists);
		when(inventoryMiddlewareService.getInventoryDetailsByGermplasmList(germplasmList.getId())).thenReturn(new ArrayList<InventoryDetails>());
		
		Assert.assertFalse("Expecting to return false for germplasm list entries with inventory details.",serviceDUT.hasInventoryValues(studyId, workbook.isNursery()));
	}
	
	@Test
	public void testEncodeBardcodeInEnglishCharacters(){
		BitMatrix bitMatrix = serviceDUT.encodeBarcode("Test", 100, 200);
		Assert.assertNotNull("Bit Matrix Barcode should be not null since characters are in English ASCII" , bitMatrix);
	}
	
	@Test
	public void testEncodeBardcodeInNonEnglishCharacters(){
		BitMatrix bitMatrix = serviceDUT.encodeBarcode("乙七九", 100, 200);
		Assert.assertNull("Bit Matrix Barcode should be null since parameter is non-english ascii" , bitMatrix);
	}
	
	@Test
	public void testPopulateValuesFromMeasurementNoData(){
		
		Map<Integer, String> values = new HashMap<>();
		LabelPrintingProcessingParams params = createLabelPrintingProcessingParams();
		MeasurementRow measurementRow = createMeasurementRow();
		
		Boolean hasData = serviceDUT.populateValuesFromMeasurement(params, measurementRow, 1, values, true);
		
		Assert.assertFalse("should be false", hasData);
	}
	
	@Test
	public void testPopulateValuesFromMeasurementWithData(){
		
		Map<Integer, String> values = new HashMap<>();
		LabelPrintingProcessingParams params = createLabelPrintingProcessingParams();
		MeasurementRow measurementRow = createMeasurementRow();
		
		Boolean hasData = serviceDUT.populateValuesFromMeasurement(params, measurementRow, TermId.TRIAL_LOCATION.getId(), values, true);
		
		Assert.assertTrue("Should be true", hasData);
		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "Manila" ,values.get(TermId.TRIAL_LOCATION.getId()));
		
	}
	
	@Test
	public void testPopulateValuesForTrial(){
		
		Integer testTermId =  TermId.TRIAL_LOCATION.getId();
		
		Mockito.doReturn(testTermId).when(serviceDUT).getCounterpartTermId(Mockito.anyInt());
		
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		Map<Integer, String> values = new HashMap<>();
		LabelPrintingProcessingParams params = createLabelPrintingProcessingParams();
		
		params.setVariableMap(new HashMap<Integer, MeasurementVariable>());
		for (MeasurementVariable mv : workbook.getAllVariables()){
			params.getVariableMap().put(mv.getTermId(), mv);
		}
		
		params.setEnvironmentData(createMeasurementRow());
		
		serviceDUT.populateValuesForTrial(params, workbook, testTermId, values, true);
		
		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "Manila" ,values.get(TermId.TRIAL_LOCATION.getId()));
	}
	
	@Test
	public void testPopulateValuesForNursery(){
		
		Integer testTermId =  TermId.TRIAL_LOCATION.getId();
		
		Mockito.doReturn(testTermId).when(serviceDUT).getCounterpartTermId(Mockito.anyInt());
		
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		Map<Integer, String> values = new HashMap<>();
		LabelPrintingProcessingParams params = createLabelPrintingProcessingParams();
		
		serviceDUT.populateValuesForNursery(params, workbook, testTermId, values, true);
		
		Assert.assertEquals("The value of LOCATION_NAME should be added to values map", "" ,values.get(TermId.TRIAL_LOCATION.getId()));
	}

	private List<GermplasmList> createGermplasmLists(int numOfEntries) {
		List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		
		for(int i = 0; i < numOfEntries; i++ ){
			Integer id = i+1;
			GermplasmList germplasmList = new GermplasmList();
			germplasmList.setId(id);
			germplasmList.setName("List " + id);
			germplasmList.setDescription("Description " + id);
			germplasmList.setListData(getGermplasmListData(numOfEntries));
			
			germplasmLists.add(germplasmList);
		}
		
		return germplasmLists;
	}
	
	private List<GermplasmListData> getGermplasmListData(int numOfEntries){
		List<GermplasmListData> germplasmListData = new ArrayList<GermplasmListData>();
		
		for(int i = 0; i < numOfEntries; i++){
			Integer id = i+1;
			GermplasmListData listData = new GermplasmListData();
			listData.setId(id);
			listData.setDesignation("Designation"+id);
			listData.setEntryCode("EntryCode"+id);
			listData.setGroupName("GroupName"+id);
			
			germplasmListData.add(listData);
		}
		
		return germplasmListData;
	}
	
	private List<InventoryDetails> createInventoryDetailList(Integer numOfEntries){
		List<InventoryDetails> inventoryDetails = new ArrayList<InventoryDetails>();
		
		for(int i = 0; i < numOfEntries; i++){
			int id = i+1;
			InventoryDetails invDetails = new InventoryDetails();
			invDetails.setLotId(id);
			invDetails.setGid(id);
			inventoryDetails.add(invDetails);
		}
		
		return inventoryDetails;
	}
	
	private LabelPrintingProcessingParams createLabelPrintingProcessingParams(){
		LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setLabelHeaders(new HashMap<Integer, String>());
		return params;
	}
	
	private MeasurementRow createMeasurementRow(){
		
		MeasurementRow measurementRow = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(createMeasurementData(TermId.LOCATION_ID.getId(), "LOCATION_ID", "123"));
		dataList.add(createMeasurementData(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME", "Manila"));
		dataList.add(createMeasurementData(111, "Some Variable", "Test Data"));
		measurementRow.setDataList(dataList);
		
		return measurementRow;
	}
	
	private MeasurementData createMeasurementData(Integer termId, String label, String value){
		MeasurementData measurementData = new MeasurementData(label, value);
		measurementData.setDataType("C");
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		
		measurementData.setMeasurementVariable(measurementVariable);
		return measurementData;
	}
	
	
}