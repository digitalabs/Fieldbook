package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.CrossImportSettings;
import com.efficio.fieldbook.web.common.bean.UserSelection;

import org.generationcp.commons.service.CrossNameService;
import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.commons.service.impl.SettingsPresetServiceImpl;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 1/29/2015
 * Time: 4:32 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class CrossingSettingsControllerTest {
	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private PresetDataManager presetDataManager;

	@Mock
	private UserSelection studySelection;

	@Mock
	private CrossNameService crossNameService;

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private CrossingSettingsController dut;

	private SettingsPresetService settingsPresetService;


	public static final String TEST_SEQUENCE_NAME_VALUE = "PRE1";
	public static final String SUCCESS_VALUE = "1";
	public static final String FAILURE_VALUE = "0";

	public static final String TEST_SETTING_NAME = "mySettingName";
	public static final Integer TEST_BREEDING_METHOD_ID = 1;
	public static final String SETTING_PREFIX = "PRE";
	public static final String SETTING_SEPARATOR = "-";
	public static final Integer TEST_PROGRAM_PRESET_ID = 1;
	public static final String TEST_PROGRAM_ID = "program-uuid";
	public static final int DUMMY_TOOL_ID = 2;

	@Before
	public void setUp() throws Exception {
		settingsPresetService = new SettingsPresetServiceImpl();
		dut.setSettingsPresetService(settingsPresetService);
	}

	@Test
	public void testGenerateNextNameInSequenceSuccess() {

		CrossSetting settingObject = mock(CrossSetting.class);
		CrossNameSetting nameSetting = mock(CrossNameSetting.class);

		try {
			doReturn(nameSetting).when(settingObject).getCrossNameSetting();
			doReturn(TEST_SEQUENCE_NAME_VALUE).when(crossNameService).getNextNameInSequence(any(
					CrossNameSetting.class));

			Map<String, String> output = dut.generateSequenceValue(mock(CrossSetting.class), request);

			assertNotNull(output);
			assertEquals(SUCCESS_VALUE, output.get("success"));
			assertEquals(TEST_SEQUENCE_NAME_VALUE, output.get("sequenceValue"));
		} catch (MiddlewareQueryException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGenerateNextNameInSequenceError() {
		CrossSetting settingObject = mock(CrossSetting.class);
		CrossNameSetting nameSetting = mock(CrossNameSetting.class);

		try {
			doReturn(nameSetting).when(settingObject).getCrossNameSetting();

			doThrow(MiddlewareQueryException.class).when(crossNameService).getNextNameInSequence(any(CrossNameSetting.class));

			Map<String, String> output = dut
					.generateSequenceValue(mock(CrossSetting.class), request);


		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSubmitCrossingSetting() {
		CrossSetting setting = mock(CrossSetting.class);
		Map<String, Object> output = dut.submitCrossSettings(setting);

		verify(studySelection).setCrossSettings(setting);

		assertNotNull(output);
		assertEquals(1, output.get("success"));
	}

	@Test
	public void testSaveAndSubmitCrossSettingNewSetting() {
		try {
			CrossingSettingsController mole = spy(dut);
			CrossSetting sampleSetting = constructCrossSetting();
			doReturn(TEST_PROGRAM_ID).when(mole).getCurrentProgramID(any(HttpServletRequest.class));
			doReturn(DUMMY_TOOL_ID).when(mole).getFieldbookToolID();

			doReturn(new ArrayList<ProgramPreset>()).when(presetDataManager).getProgramPresetFromProgramAndTool(anyString(), anyInt(), anyString());

			ArgumentCaptor<ProgramPreset> param = ArgumentCaptor.forClass(ProgramPreset.class);
			mole.submitAndSaveCrossSettings(constructCrossSetting(), request);

			verify(presetDataManager).saveOrUpdateProgramPreset(param.capture());

			ProgramPreset captured = param.getValue();
			assertEquals(TEST_SETTING_NAME, captured.getName());
			assertEquals(settingsPresetService.convertPresetSettingToXml(sampleSetting, CrossSetting.class), captured.getConfiguration());

			// we verify that the program preset that we have is blank
			assertEquals(0, captured.getProgramPresetId());

		} catch (MiddlewareQueryException e) {
			fail(e.getMessage());
		} catch (JAXBException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testSaveAndSubmitCrossSettingPreviousSetting() {
		try {
			CrossingSettingsController mole = spy(dut);
			CrossSetting sampleSetting = constructCrossSetting();
			doReturn(TEST_PROGRAM_ID).when(mole).getCurrentProgramID(any(HttpServletRequest.class));
			doReturn(DUMMY_TOOL_ID).when(mole).getFieldbookToolID();

			doReturn(constructDummyPresetList()).when(presetDataManager)
					.getProgramPresetFromProgramAndTool(anyString(), anyInt(), anyString());

			ArgumentCaptor<ProgramPreset> param = ArgumentCaptor.forClass(ProgramPreset.class);
			mole.submitAndSaveCrossSettings(constructCrossSetting(), request);

			verify(presetDataManager).saveOrUpdateProgramPreset(param.capture());

			ProgramPreset captured = param.getValue();
			assertEquals(TEST_SETTING_NAME, captured.getName());
			assertEquals(settingsPresetService
					.convertPresetSettingToXml(sampleSetting, CrossSetting.class),
					captured.getConfiguration());

			// we verify that the program preset that we have is blank
			assertEquals(TEST_PROGRAM_PRESET_ID.longValue(), (long)captured.getProgramPresetId());

		} catch (MiddlewareQueryException e) {
			fail(e.getMessage());
		} catch (JAXBException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRetrieveImportSettings() {
		try {
			CrossingSettingsController mole = spy(dut);
			doReturn(TEST_PROGRAM_ID).when(mole).getCurrentProgramID(any(HttpServletRequest.class));
			doReturn(DUMMY_TOOL_ID).when(mole).getFieldbookToolID();

			doReturn(constructDummyPresetList()).when(presetDataManager)
					.getProgramPresetFromProgramAndTool(anyString(), anyInt(), anyString());

			List<CrossImportSettings> output = mole.getAvailableCrossImportSettings(request);
			assertTrue(output.size() > 0);
			CrossImportSettings setting = output.get(0);
			assertEquals(TEST_SETTING_NAME, setting.getName());
			assertEquals(TEST_BREEDING_METHOD_ID, setting.getBreedingMethodID());
			assertEquals(SETTING_PREFIX, setting.getCrossPrefix());
			assertEquals(SETTING_SEPARATOR, setting.getParentageDesignationSeparator());
		} catch (MiddlewareQueryException e) {
			fail(e.getMessage());
		} catch (JAXBException e) {
			fail(e.getMessage());
		}

	}

	public List<ProgramPreset> constructDummyPresetList() throws JAXBException{
		ProgramPreset existing = new ProgramPreset();
		existing.setName(TEST_SETTING_NAME);
		existing.setProgramPresetId(TEST_PROGRAM_PRESET_ID);
		existing.setConfiguration(settingsPresetService.convertPresetSettingToXml(constructCrossSetting(), CrossSetting.class));

		List<ProgramPreset> presetList = new ArrayList<>();
		presetList.add(existing);


		return presetList;
	}

	protected CrossSetting constructCrossSetting() {
		CrossSetting setting = new CrossSetting();
		setting.setName(TEST_SETTING_NAME);

		BreedingMethodSetting methodSetting = new BreedingMethodSetting(TEST_BREEDING_METHOD_ID,
				false);
		setting.setBreedingMethodSetting(methodSetting);

		CrossNameSetting nameSetting = new CrossNameSetting();
		nameSetting.setPrefix(SETTING_PREFIX);
		nameSetting.setSeparator(SETTING_SEPARATOR);
		setting.setCrossNameSetting(nameSetting);
		setting.setAdditionalDetailsSetting(new AdditionalDetailsSetting());

		return setting;
	}

}
