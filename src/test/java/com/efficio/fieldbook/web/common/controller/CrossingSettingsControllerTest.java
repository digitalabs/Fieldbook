package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.CrossImportSettings;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;
import com.efficio.fieldbook.web.common.exception.InvalidInputException;
import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.common.service.impl.CrossingTemplateExcelExporter;
import com.efficio.fieldbook.web.util.CrossesListUtil;
import org.generationcp.commons.data.initializer.ImportedCrossesTestDataInitializer;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.commons.service.impl.SettingsPresetServiceImpl;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class CrossingSettingsControllerTest {

	public static final String TEST_SEQUENCE_NAME_VALUE = "PRE1";
	public static final String SUCCESS_VALUE = "1";
	public static final String TEST_SETTING_NAME = "mySettingName";
	public static final Integer TEST_BREEDING_METHOD_ID = 1;
	public static final String SETTING_PREFIX = "PRE";
	public static final String SETTING_SEPARATOR = "-";
	public static final Integer TEST_PROGRAM_PRESET_ID = 1;
	public static final String TEST_PROGRAM_ID = "2";
	public static final int DUMMY_STUDY_ID = 2;
	public static final int DUMMY_TOOL_ID = 2;
	public static final int NUMBER_OF_MONTHS = 12;
	public static final String DUMMY_ABS_PATH = "dummy/abs/path";
	public static final String TEST_ENTRY_CODE = "testEntryCode";
	public static final String TEST_SEED_SOURCE = "testSeedSource";
	public static final String TEST_MALE_PARENT = "testMaleParent";
	public static final int ENTRY_ID = 56;
	public static final String TEST_FEMALE_PARENT = "testFemaleParent";
	public static final int MGID = 836;
	public static final int FGID = 535;
	private static final String TEST_DUPLICATE = "SID-1";
	public static final String FEMALE_PLOT = "1";
	public static final String BREEDING_METHOD = "Test Method";
	public static final String MALE_PLOT = "2";
	public static final String MALE_STUDY_NAME = "maleStudy";
	public static final Integer CROSSING_DATE = 20161212;
	public static final String NOTES = "Test notes";
	public static final String FEMALE_PEDIGREE = "-";
	public static final String MALE_PEDIGREE = "-";

	private ImportedCrossesTestDataInitializer importedCrossesTestDataInitializer;
	@Mock
	private CrossExpansionProperties crossExpansionProperties;
	@Mock
	private WorkbenchService workbenchService;
	@Mock
	private PresetDataManager presetDataManager;
	@Mock
	private UserSelection studySelection;
	@Mock
	private HttpServletRequest request;
	@Mock
	private CrossingTemplateExcelExporter crossingTemplateExcelExporter;
	@Mock
	private MessageSource messageSource;
	@Mock
	private GermplasmListManager germplasmListManager;
	@Mock
	private GermplasmDataManager germplasmDataManager;
	@Mock
	private OntologyDataManager ontologyDataManager;
	@Mock
	private ContextUtil contextUtil;
	@Mock
	private CrossingService crossingService;

	private CrossesListUtil crossesListUtil;

	@Spy
	private final SettingsPresetService settingsPresetService = new SettingsPresetServiceImpl();

	@InjectMocks
	private CrossingSettingsController crossingSettingsController;

	@Before
	public void setup() {
		this.crossesListUtil = new CrossesListUtil();
		this.crossesListUtil.setOntologyDataManager(this.ontologyDataManager);
		this.crossingSettingsController.setCrossesListUtil(this.crossesListUtil);
		this.mockMappingOfHeadersToOntology();

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(CrossingSettingsControllerTest.DUMMY_STUDY_ID);
		workbook.setStudyDetails(studyDetails);
		Mockito.when(this.studySelection.getWorkbook()).thenReturn(workbook);
		Mockito.when(this.crossExpansionProperties.getHybridBreedingMethods()).thenReturn(new HashSet<Integer>(Arrays.asList(1)));
		Mockito.when(this.germplasmDataManager.getMethodCodeByMethodIds(this.crossExpansionProperties.getHybridBreedingMethods()))
				.thenReturn(new ArrayList<String>(Arrays.asList("TCR")));
		this.importedCrossesTestDataInitializer = new ImportedCrossesTestDataInitializer();
	}

	private void mockMappingOfHeadersToOntology() {
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId()))
				.thenReturn(this.getTerm(ColumnLabels.FEMALE_PARENT));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FGID.getId())).thenReturn(this.getTerm(ColumnLabels.FGID));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(this.getTerm(ColumnLabels.MALE_PARENT));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MGID.getId())).thenReturn(this.getTerm(ColumnLabels.MGID));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(this.getTerm(ColumnLabels.SEED_SOURCE));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS_FEMALE_GID.getId()))
				.thenReturn(this.getTerm(ColumnLabels.CROSS_FEMALE_GID));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS_MALE_GID.getId()))
				.thenReturn(this.getTerm(ColumnLabels.CROSS_MALE_GID));
	}

	private Term getTerm(final ColumnLabels columnLabel) {
		final int id = columnLabel.getTermId().getId();
		final String name = columnLabel.getName();
		return new Term(id, name, name);
	}

	@Test
	public void testGenerateNextNameInSequenceSuccess() throws InvalidInputException {

		final CrossSetting settingObject = Mockito.mock(CrossSetting.class);
		final CrossNameSetting nameSetting = Mockito.mock(CrossNameSetting.class);

		try {
			Mockito.doReturn(CrossingSettingsControllerTest.TEST_SEQUENCE_NAME_VALUE).when(this.crossingService)
					.getNextNameInSequence(ArgumentMatchers.<CrossNameSetting>isNull());
			final Map<String, String> output =
					this.crossingSettingsController.generateSequenceValue(Mockito.mock(CrossSetting.class), this.request);

			Assert.assertNotNull(output);
			Assert.assertEquals(CrossingSettingsControllerTest.SUCCESS_VALUE, output.get("success"));
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_SEQUENCE_NAME_VALUE, output.get("sequenceValue"));
		} catch (final RuntimeException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGenerateNextNameInSequenceInvalidInputError() throws InvalidInputException {

		final String errorMessage = "Please select a starting sequence number larger than 10";
		final CrossSetting settingObject = Mockito.mock(CrossSetting.class);
		final CrossNameSetting nameSetting = Mockito.mock(CrossNameSetting.class);

		Mockito.doThrow(new InvalidInputException(errorMessage)).when(this.crossingService)
				.getNextNameInSequence(ArgumentMatchers.<CrossNameSetting>isNull());

		final Map<String, String> result = this.crossingSettingsController.generateSequenceValue(Mockito.mock(CrossSetting.class), this.request);
		Assert.assertEquals(errorMessage, result.get(CrossingSettingsController.ERROR));
		Assert.assertEquals("0", result.get(CrossingSettingsController.SUCCESS_KEY));

	}

	@Test
	public void testGenerateNextNameInSequenceRuntimeError() throws InvalidInputException {

		final String errorMessage = "runtime error";
		final CrossSetting settingObject = Mockito.mock(CrossSetting.class);
		final CrossNameSetting nameSetting = Mockito.mock(CrossNameSetting.class);

		Mockito.doThrow(new InvalidInputException(errorMessage)).when(this.crossingService)
				.getNextNameInSequence(ArgumentMatchers.<CrossNameSetting>isNull());

		final Map<String, String> result = this.crossingSettingsController.generateSequenceValue(Mockito.mock(CrossSetting.class), this.request);
		Assert.assertEquals(errorMessage, result.get(CrossingSettingsController.ERROR));
		Assert.assertEquals("0", result.get(CrossingSettingsController.SUCCESS_KEY));

	}

	@Test
	public void testSubmitCrossingSetting() {
		final CrossSetting setting = Mockito.mock(CrossSetting.class);
		final Map<String, Object> output = this.crossingSettingsController.submitCrossSettings(setting);

		Mockito.verify(this.studySelection).setCrossSettings(setting);

		Assert.assertNotNull(output);
		Assert.assertEquals(1, output.get("success"));
	}

	@Test
	public void testSaveAndSubmitCrossSettingNewSetting() {
		try {
			final CrossingSettingsController mole = Mockito.spy(this.crossingSettingsController);
			final CrossSetting sampleSetting = this.constructCrossSetting();
			Mockito.doReturn(CrossingSettingsControllerTest.TEST_PROGRAM_ID).when(mole).getCurrentProgramID();
			Mockito.doReturn(CrossingSettingsControllerTest.DUMMY_TOOL_ID).when(mole).getFieldbookToolID();

			Mockito.doReturn(new ArrayList<ProgramPreset>()).when(this.presetDataManager)
					.getProgramPresetFromProgramAndTool(Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());

			final ArgumentCaptor<ProgramPreset> param = ArgumentCaptor.forClass(ProgramPreset.class);
			mole.submitAndSaveCrossSettings(this.constructCrossSetting());

			Mockito.verify(this.presetDataManager).saveOrUpdateProgramPreset(param.capture());

			final ProgramPreset captured = param.getValue();
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_SETTING_NAME, captured.getName());
			Assert.assertEquals(this.settingsPresetService.convertPresetSettingToXml(sampleSetting, CrossSetting.class),
					captured.getConfiguration());

			// we verify that the program preset that we have is blank
			Assert.assertEquals(0, captured.getProgramPresetId());

		} catch (MiddlewareQueryException | JAXBException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testSaveAndSubmitCrossSettingPreviousSetting() {
		try {
			final CrossingSettingsController mole = Mockito.spy(this.crossingSettingsController);
			final CrossSetting sampleSetting = this.constructCrossSetting();
			Mockito.doReturn(CrossingSettingsControllerTest.TEST_PROGRAM_ID).when(mole).getCurrentProgramID();
			Mockito.doReturn(CrossingSettingsControllerTest.DUMMY_TOOL_ID).when(mole).getFieldbookToolID();

			Mockito.doReturn(this.constructDummyPresetList()).when(this.presetDataManager)
					.getProgramPresetFromProgramAndTool(Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());

			final ArgumentCaptor<ProgramPreset> param = ArgumentCaptor.forClass(ProgramPreset.class);
			mole.submitAndSaveCrossSettings(this.constructCrossSetting());

			Mockito.verify(this.presetDataManager).saveOrUpdateProgramPreset(param.capture());

			final ProgramPreset captured = param.getValue();
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_SETTING_NAME, captured.getName());
			Assert.assertEquals(this.settingsPresetService.convertPresetSettingToXml(sampleSetting, CrossSetting.class),
					captured.getConfiguration());

			// we verify that the program preset that we have is blank
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_PROGRAM_PRESET_ID.longValue(), captured.getProgramPresetId());

		} catch (MiddlewareQueryException | JAXBException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetHarvestMonth() {
		final List<Map<String, String>> harvestMonths = this.crossingSettingsController.getHarvestMonths();

		Assert.assertNotNull(harvestMonths);
		Assert.assertEquals(CrossingSettingsControllerTest.NUMBER_OF_MONTHS, harvestMonths.size());

		for (final Map<String, String> harvestMonth : harvestMonths) {
			Assert.assertTrue(harvestMonth.containsKey(CrossingSettingsController.ID));
			Assert.assertTrue(harvestMonth.containsKey(CrossingSettingsController.TEXT));

			Assert.assertEquals(2, harvestMonth.get(CrossingSettingsController.ID).length());
		}
	}

	@Test
	public void testGetHarvestYears() {
		final List<String> harvestYears = this.crossingSettingsController.getHarvestYears();

		Assert.assertNotNull(harvestYears);
		Assert.assertEquals(CrossingSettingsController.YEAR_INTERVAL * 2 + 1, harvestYears.size());

		final String firstDisplayed = harvestYears.get(0);

		final int currentYearIndex = harvestYears.size() / 2;
		final String currentYearDisplayed = harvestYears.get(currentYearIndex);

		final Calendar cal = DateUtil.getCalendarInstance();
		Assert.assertEquals(Integer.toString(cal.get(Calendar.YEAR) + 10), firstDisplayed);
		Assert.assertEquals(Integer.toString(cal.get(Calendar.YEAR)), currentYearDisplayed);
	}

	@Test
	public void testRetrieveImportSettings() {
		try {
			final CrossingSettingsController mole = Mockito.spy(this.crossingSettingsController);
			Mockito.doReturn(CrossingSettingsControllerTest.TEST_PROGRAM_ID).when(mole).getCurrentProgramID();
			Mockito.doReturn(CrossingSettingsControllerTest.DUMMY_TOOL_ID).when(mole).getFieldbookToolID();

			Mockito.doReturn(this.constructDummyPresetList()).when(this.presetDataManager)
					.getProgramPresetFromProgramAndTool(Matchers.anyString(), Matchers.anyInt(), Matchers.anyString());

			final List<CrossImportSettings> output = mole.getAvailableCrossImportSettings();
			Assert.assertTrue(output.size() > 0);
			final CrossImportSettings setting = output.get(0);
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_SETTING_NAME, setting.getName());
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_BREEDING_METHOD_ID, setting.getBreedingMethodID());
			Assert.assertEquals(CrossingSettingsControllerTest.SETTING_PREFIX, setting.getCrossPrefix());
			Assert.assertEquals(CrossingSettingsControllerTest.SETTING_SEPARATOR, setting.getParentageDesignationSeparator());
		} catch (MiddlewareQueryException | JAXBException e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testDoCrossingExportSuccess() throws Exception {

		Mockito.when(this.crossingTemplateExcelExporter.export(Matchers.anyInt(), ArgumentMatchers.<String>isNull(), Matchers.anyInt()))
				.thenReturn(new FileExportInfo(CrossingSettingsControllerTest.DUMMY_ABS_PATH, CrossingSettingsControllerTest.DUMMY_ABS_PATH));

		final Map<String, Object> jsonResult = this.crossingSettingsController.doCrossingExport();

		Assert.assertEquals("should return success", Boolean.TRUE, jsonResult.get("isSuccess"));
		Assert.assertEquals("should return the correct output path", CrossingSettingsControllerTest.DUMMY_ABS_PATH,
				jsonResult.get("outputFilename"));
	}

	@Test
	public void testDoCrossingExportFail() throws Exception {
		final Workbook wb = Mockito.mock(Workbook.class);
		final StudyDetails studyDetails = Mockito.mock(StudyDetails.class);

		Mockito.when(wb.getStudyDetails()).thenReturn(studyDetails);
		Mockito.when(studyDetails.getId()).thenReturn(CrossingSettingsControllerTest.DUMMY_STUDY_ID);
		Mockito.when(wb.getStudyName()).thenReturn("dummy study name");
		Mockito.when(this.studySelection.getWorkbook()).thenReturn(wb);

		final File file = Mockito.mock(File.class);
		Mockito.when(this.crossingTemplateExcelExporter.export(Matchers.anyInt(), Matchers.anyString(), Matchers.anyInt()))
				.thenThrow(new CrossingTemplateExportException("export.error"));

		Mockito.when(this.messageSource.getMessage(Matchers.anyString(), Matchers.any(String[].class), Matchers.anyString(),
				Matchers.eq(LocaleContextHolder.getLocale()))).thenReturn("export.error");

		final Map<String, Object> jsonResult = this.crossingSettingsController.doCrossingExport();

		Assert.assertEquals("should return success", Boolean.FALSE, jsonResult.get("isSuccess"));
		Assert.assertEquals("should return the correct error message", "export.error", jsonResult.get("errorMessage"));
	}

	@Test
	public void testDeleteCrossList() {
		final Integer crossListId = 1;
		this.crossingSettingsController.deleteCrossList(crossListId);

		Mockito.verify(this.germplasmListManager, times(1)).deleteGermplasmListByListIdPhysically(crossListId);
	}

	@Test
	public void testDeleteSetting() {
		final Integer programPresetId = 1;
		this.crossingSettingsController.deleteCrossSetting(programPresetId);

		Mockito.verify(this.presetDataManager, times(1)).deleteProgramPreset(programPresetId);
	}

	@Test
	public void testGetHybridMethods() {
		final Set<Integer> hybridMethods = this.crossingSettingsController.getHybridMethods();
		Assert.assertNotNull("The hybrid methods should not be null", hybridMethods);
		Assert.assertFalse("The Hybrid methods should not be empty", hybridMethods.isEmpty());

	}

	@Test
	public void testCheckForHybridMethodsTrue() {
		final List<ImportedCrosses> importedCrosses = this.importedCrossesTestDataInitializer.createImportedCrossesList(1, true);
		Assert.assertTrue("The imported crosses should have hybrid methods",
				this.crossingSettingsController.checkForHybridMethods(importedCrosses));
	}

	@Test
	public void testCheckForHybridMethodsFalse() {
		final List<ImportedCrosses> importedCrosses = this.importedCrossesTestDataInitializer.createImportedCrossesList(1, false);
		Assert.assertFalse("The imported crosses should not have hybrid methods",
				this.crossingSettingsController.checkForHybridMethods(importedCrosses));
	}

	public List<ProgramPreset> constructDummyPresetList() throws JAXBException {
		final ProgramPreset existing = new ProgramPreset();
		existing.setName(CrossingSettingsControllerTest.TEST_SETTING_NAME);
		existing.setProgramPresetId(CrossingSettingsControllerTest.TEST_PROGRAM_PRESET_ID);
		existing.setConfiguration(this.settingsPresetService.convertPresetSettingToXml(this.constructCrossSetting(), CrossSetting.class));

		final List<ProgramPreset> presetList = new ArrayList<>();
		presetList.add(existing);

		return presetList;
	}

	protected CrossSetting constructCrossSetting() {
		final CrossSetting setting = new CrossSetting();
		setting.setName(CrossingSettingsControllerTest.TEST_SETTING_NAME);

		final BreedingMethodSetting methodSetting =
				new BreedingMethodSetting(CrossingSettingsControllerTest.TEST_BREEDING_METHOD_ID, false, false);
		setting.setBreedingMethodSetting(methodSetting);

		final CrossNameSetting nameSetting = new CrossNameSetting();
		nameSetting.setPrefix(CrossingSettingsControllerTest.SETTING_PREFIX);
		nameSetting.setSeparator(CrossingSettingsControllerTest.SETTING_SEPARATOR);
		setting.setCrossNameSetting(nameSetting);
		final AdditionalDetailsSetting additionalDetailsSetting = new AdditionalDetailsSetting(0, "");
		setting.setAdditionalDetailsSetting(additionalDetailsSetting);

		return setting;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetImportedCrossesListSuccess() throws Exception {

		final ArrayList<GermplasmListData> germplasmListDatas = new ArrayList<>();
		final GermplasmList germplasmList = new GermplasmList();
		final GermplasmListData germplasmListData = new GermplasmListData(771, germplasmList, 45, CrossingSettingsControllerTest.ENTRY_ID,
				CrossingSettingsControllerTest.TEST_ENTRY_CODE, CrossingSettingsControllerTest.TEST_SEED_SOURCE, "testDesignation",
				"testGroupName", 0, 5);
		germplasmListData.setMaleParent(CrossingSettingsControllerTest.TEST_MALE_PARENT);
		germplasmListData.setFgid(CrossingSettingsControllerTest.FGID);
		germplasmListData.setMgid(CrossingSettingsControllerTest.MGID);
		germplasmListData.setFemaleParent(CrossingSettingsControllerTest.TEST_FEMALE_PARENT);
		germplasmListData.setFemalePedigree(CrossingSettingsControllerTest.FEMALE_PEDIGREE);
		germplasmListData.setMalePedigree(CrossingSettingsControllerTest.MALE_PEDIGREE);

		germplasmListDatas.add(germplasmListData);
		Mockito.when(this.germplasmListManager.retrieveListDataWithParents(80)).thenReturn(germplasmListDatas);
		Mockito.when(this.germplasmListManager.getGermplasmListById(80)).thenReturn(germplasmList);
		final UserDefinedField userDefinedField = new UserDefinedField();
		Mockito.when(this.germplasmDataManager
				.getUserDefinedFieldByTableTypeAndCode(Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
				.thenReturn(userDefinedField);

		final Map<String, Object> testResponseMap = this.crossingSettingsController.getImportedCrossesList("80");
		final List<String> tableHeaderList = (List<String>) testResponseMap.get(CrossesListUtil.TABLE_HEADER_LIST);
		final List<Map<String, Object>> testMasterList = (List<Map<String, Object>>) testResponseMap.get(CrossesListUtil.LIST_DATA_TABLE);

		Assert.assertEquals("The master list should contain 1 record: ", 1, testMasterList.size());
		final Map<String, Object> data = testMasterList.get(0);
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.TEST_SEED_SOURCE));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.ENTRY_ID));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.FGID_INDEX)));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.FGID));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue(data.containsValue(
				CrossingSettingsControllerTest.TEST_FEMALE_PARENT + "/" + CrossingSettingsControllerTest.TEST_MALE_PARENT));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertTrue(data.containsValue(""));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.MGID_INDEX)));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.MGID));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.FEMALE_PEDIGREE));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.MALE_PEDIGREE));

	}

	@Test
	public void testGetImportedCrossesListEmpty() throws Exception {
		final Map<String, Object> testResponseMap = this.crossingSettingsController.getImportedCrossesList();
		Assert.assertTrue("The response map should be empty", testResponseMap.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetImportedCrossesListWithSessionData() throws Exception {
		this.fillUpUserSelectionWithImportedCrossTestData();

		final UserDefinedField userDefinedField = new UserDefinedField();
		Mockito.when(this.germplasmDataManager
				.getUserDefinedFieldByTableTypeAndCode(Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
				.thenReturn(userDefinedField);

		final Map<String, Object> testResponseMap = this.crossingSettingsController.getImportedCrossesList();
		Assert.assertFalse("The response map should not be empty", testResponseMap.isEmpty());

		final List<String> tableHeaderList = (List<String>) testResponseMap.get(CrossesListUtil.TABLE_HEADER_LIST);
		final List<Map<String, Object>> testMasterList = (List<Map<String, Object>>) testResponseMap.get(CrossesListUtil.LIST_DATA_TABLE);
		final Map<String, Object> data = testMasterList.get(0);
		Assert.assertEquals("The master list should contain 1 record: ", 1, testMasterList.size());
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.TEST_SEED_SOURCE));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.ENTRY_ID));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.FGID_INDEX)));
		Assert.assertTrue(data.containsValue(Integer.toString(CrossingSettingsControllerTest.FGID)));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue(data.containsValue(
				CrossingSettingsControllerTest.TEST_FEMALE_PARENT + "/" + CrossingSettingsControllerTest.TEST_MALE_PARENT));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.TEST_SEED_SOURCE));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.MGID_INDEX)));
		Assert.assertTrue(data.containsValue(Integer.toString(CrossingSettingsControllerTest.MGID)));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.FEMALE_CROSS)));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.MALE_CROSS)));
		Assert.assertTrue(data.containsKey(tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX)));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.BREEDING_METHOD));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.FEMALE_PEDIGREE));
		Assert.assertTrue(data.containsValue(CrossingSettingsControllerTest.MALE_PEDIGREE));
	}

	private void fillUpUserSelectionWithImportedCrossTestData() {
		Mockito.when(this.studySelection.getImportedCrossesList()).thenReturn(new ImportedCrossesList());
		final List<ImportedCrosses> importedCrossesList = this.studySelection.getImportedCrossesList().getImportedCrosses();
		final ImportedCrosses importedCrosses = new ImportedCrosses();
		importedCrosses.setEntryId(CrossingSettingsControllerTest.ENTRY_ID);
		importedCrosses.setCross(CrossingSettingsControllerTest.TEST_FEMALE_PARENT + "/" + CrossingSettingsControllerTest.TEST_MALE_PARENT);
		importedCrosses.setEntryCode(CrossingSettingsControllerTest.TEST_ENTRY_CODE);
		importedCrosses.setFemaleDesig(CrossingSettingsControllerTest.TEST_FEMALE_PARENT);
		importedCrosses.setFemaleGid(Integer.toString(CrossingSettingsControllerTest.FGID));
		importedCrosses.setMaleDesig(CrossingSettingsControllerTest.TEST_MALE_PARENT);
		importedCrosses.setMaleGid(Integer.toString(CrossingSettingsControllerTest.MGID));
		importedCrosses.setSource(CrossingSettingsControllerTest.TEST_SEED_SOURCE);
		importedCrosses.setDuplicate(CrossingSettingsControllerTest.TEST_DUPLICATE);
		importedCrosses.setFemalePlotNo(CrossingSettingsControllerTest.FEMALE_PLOT);
		importedCrosses.setMalePlotNo(CrossingSettingsControllerTest.MALE_PLOT);
		importedCrosses.setMaleStudyName(CrossingSettingsControllerTest.MALE_STUDY_NAME);
		importedCrosses.setRawBreedingMethod(CrossingSettingsControllerTest.BREEDING_METHOD);
		importedCrosses.setCrossingDate(CrossingSettingsControllerTest.CROSSING_DATE);
		importedCrosses.setNotes(CrossingSettingsControllerTest.NOTES);
		importedCrosses.setFemalePedigree(CrossingSettingsControllerTest.FEMALE_PEDIGREE);
		importedCrosses.setMalePedigree(CrossingSettingsControllerTest.MALE_PEDIGREE);
		importedCrossesList.add(importedCrosses);
	}

}
