package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

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

	@Mock
	WorkbenchService workbenchService;

	@Mock
	PresetDataManager presetDataManager;

	@InjectMocks
	LabelPrintingServiceImpl serviceDUT;

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
		searchResultPreset.setProgramUuid(1);
		searchResultPreset.setToolSection(ToolSection.FBK_LABEL_PRINTING.name());
		searchResultPreset.setToolId(23);
		searchResultPreset.setName(TEST_EXISTING_PRESET_NAME);
		searchResultPreset.setConfiguration(PROGRAM_PRESET_CONFIG);
		notEmptySearchResult.add(searchResultPreset);

		when(presetDataManager.getProgramPresetFromProgramAndToolByName(TEST_EXISTING_PRESET_NAME,
				TEST_PROJECT_ID.intValue(), 23,
				ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(notEmptySearchResult);

		when(presetDataManager.getProgramPresetFromProgramAndToolByName(
				TEST_NON_EXISTING_PRESET_NAME,
				TEST_PROJECT_ID.intValue(), 23,
				ToolSection.FBK_LABEL_PRINTING.name())).thenReturn(new ArrayList<ProgramPreset>());

		when(presetDataManager.getProgramPresetFromProgramAndTool(TEST_PROJECT_ID.intValue(), 23,
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
}