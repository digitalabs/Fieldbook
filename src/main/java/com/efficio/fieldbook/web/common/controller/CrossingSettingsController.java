package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.CrossImportSettings;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.service.CrossNameService;
import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ProgramUUIDFactory;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 1/21/2015
 * Time: 1:49 PM
 */

@Controller
@RequestMapping(CrossingSettingsController.URL)
public class CrossingSettingsController extends AbstractBaseFieldbookController {
	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsController.class);
	public static final String URL = "/import/crosses";

	@Resource
	private WorkbenchService workbenchService;

	@Resource
	private PresetDataManager presetDataManager;

	@Resource
	private SettingsPresetService settingsPresetService;

	@Resource
	private UserSelection studySelection;

	@Resource
	private CrossNameService crossNameService;
	
	@Resource
	private ProgramUUIDFactory programUUIDFactory;

	public static final String SUCCESS_KEY = "success";


	@Override public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveSettings", method = RequestMethod.GET, produces = "application/json")
	public List<CrossImportSettings> getAvailableCrossImportSettings(HttpServletRequest request) {
		List<CrossImportSettings> settings = new ArrayList<>();

		try {
			List<ProgramPreset> presets = presetDataManager
					.getProgramPresetFromProgramAndTool(getCurrentProgramID(request),
							getFieldbookToolID(),
							ToolSection.FBK_CROSS_IMPORT.name());

			for (ProgramPreset preset : presets) {
				CrossSetting crossSetting = (CrossSetting) settingsPresetService
						.convertPresetFromXmlString(preset.getConfiguration(),
								CrossSetting.class);
				CrossImportSettings importSettings = new CrossImportSettings();
				importSettings.populate(crossSetting);
				settings.add(importSettings);
			}


		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage());
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

		return settings;
	}

	@ResponseBody
	@RequestMapping(value="/submitAndSaveSetting", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public Map<String, Object> submitAndSaveCrossSettings(@RequestBody CrossSetting settings, HttpServletRequest request) {
		Map<String, Object> returnVal = new HashMap<>();
		try {
			saveCrossSetting(settings, getCurrentProgramID(request));
			return submitCrossSettings(settings);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage());
		} catch (JAXBException e) {
			LOG.error(e.getMessage());
		}

		returnVal.put(SUCCESS_KEY, "0");
		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value="/generateSequenceValue", method = RequestMethod.POST,consumes = "application/json", produces = "application/json")
	public Map<String, String> generateSequenceValue(@RequestBody CrossSetting setting, HttpServletRequest request) {
		Map<String, String> returnVal = new HashMap<>();

		try {
			String sequenceValue = crossNameService.getNextNameInSequence(setting.getCrossNameSetting());
			returnVal.put(SUCCESS_KEY, "1");
			returnVal.put("sequenceValue", sequenceValue);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage());
			returnVal.put(SUCCESS_KEY, "0");
		}

		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value="/submit", method = RequestMethod.POST)
	public Map<String, Object> submitCrossSettings(@RequestBody CrossSetting settings) {
		Map<String, Object> returnVal = new HashMap<>();

		studySelection.setCrossSettings(settings);
		returnVal.put(SUCCESS_KEY, new Integer(1));
		return returnVal;
	}

	protected void saveCrossSetting(CrossSetting setting, String programUUID) throws MiddlewareQueryException,
			JAXBException{

		List<ProgramPreset> presets = presetDataManager
				.getProgramPresetFromProgramAndTool(programUUID,
						getFieldbookToolID(),
						ToolSection.FBK_CROSS_IMPORT.name());

		boolean found = false;
		ProgramPreset forSaving = null;
		for (ProgramPreset preset : presets) {
			if (preset.getName().equals(setting.getName())) {
				preset.setConfiguration(settingsPresetService.convertPresetSettingToXml(setting, CrossSetting.class));
				found = true;
				forSaving = preset;
				break;
			}
		}

		if (!found) {
			forSaving = new ProgramPreset();
			forSaving.setName(setting.getName());
			forSaving.setToolId(getFieldbookToolID());
			forSaving.setProgramUuid(programUUID);
			forSaving.setToolSection(ToolSection.FBK_CROSS_IMPORT.name());
			forSaving.setConfiguration(settingsPresetService.convertPresetSettingToXml(setting, CrossSetting.class));
		}


		presetDataManager.saveOrUpdateProgramPreset(forSaving);
	}

	protected Integer getFieldbookToolID() throws MiddlewareQueryException{
		return workbenchService.getFieldbookWebTool().getToolId().intValue();
	}

	protected String getCurrentProgramID(HttpServletRequest request) {
		return programUUIDFactory.getCurrentProgramUUID();
	}

	public SettingsPresetService getSettingsPresetService() {
		return settingsPresetService;
	}

	public void setSettingsPresetService(SettingsPresetService settingsPresetService) {
		this.settingsPresetService = settingsPresetService;
	}
}