package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.CrossImportSettings;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.impl.CrossingTemplateExcelExporter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.service.CrossNameService;
import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 1/21/2015
 * Time: 1:49 PM
 */

@Controller
@RequestMapping(CrossingSettingsController.URL)
public class CrossingSettingsController extends AbstractBaseFieldbookController {
	public static final String URL = "/import/crosses";
	public static final int YEAR_INTERVAL = 30;
	public static final String ID = "id";
	public static final String TEXT = "text";
	public static final String SUCCESS_KEY = "success";
	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsController.class);
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
	private CrossingTemplateExcelExporter crossingTemplateExcelExporter;

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

		} catch (MiddlewareQueryException | JAXBException e) {
			LOG.error(e.getMessage(), e);
		}

		return settings;
	}

	@ResponseBody
	@RequestMapping(value = "/submitAndSaveSetting", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public Map<String, Object> submitAndSaveCrossSettings(@RequestBody CrossSetting settings,
			HttpServletRequest request) {
		Map<String, Object> returnVal = new HashMap<>();
		try {
			Integer programID = getCurrentProgramID(request);
			saveCrossSetting(settings, programID);
			return submitCrossSettings(settings);
		} catch (MiddlewareQueryException | JAXBException e) {
			LOG.error(e.getMessage(), e);
		}

		returnVal.put(SUCCESS_KEY, "0");
		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/generateSequenceValue", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public Map<String, String> generateSequenceValue(@RequestBody CrossSetting setting,
			HttpServletRequest request) {
		Map<String, String> returnVal = new HashMap<>();

		try {
			String sequenceValue = crossNameService
					.getNextNameInSequence(setting.getCrossNameSetting());
			returnVal.put(SUCCESS_KEY, "1");
			returnVal.put("sequenceValue", sequenceValue);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			returnVal.put(SUCCESS_KEY, "0");
		}

		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/submit", method = RequestMethod.POST)
	public Map<String, Object> submitCrossSettings(@RequestBody CrossSetting settings) {
		Map<String, Object> returnVal = new HashMap<>();

		studySelection.setCrossSettings(settings);
		returnVal.put(SUCCESS_KEY, new Integer(1));
		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/getHarvestYears", method = RequestMethod.GET)
	public List<String> getHarvestYears() {
		List<String> years = new ArrayList<>();

		Calendar cal = Calendar.getInstance();

		for (int i = 0; i < YEAR_INTERVAL; i++) {
			years.add(Integer.toString(cal.get(Calendar.YEAR)));
			cal.roll(Calendar.YEAR, false);
		}

		return years;
	}

	@ResponseBody
	@RequestMapping(value = "/getHarvestMonths", method = RequestMethod.GET)
	public List<Map<String, String>> getHarvestMonths() {
		List<Map<String, String>> monthList = new ArrayList<>();

		String[] monthLabels = DateFormatSymbols.getInstance().getMonths();
		int i = 1;
		for (String monthLabel : monthLabels) {
			if (monthLabel.isEmpty()) {
				continue;
			}

			String textValue = Integer.toString(i++);
			if (textValue.length() == 1) {
				textValue = "0" + textValue;
			}

			Map<String, String> monthMap = new HashMap<>();
			monthMap.put(ID, textValue);
			monthMap.put(TEXT, monthLabel);

			monthList.add(monthMap);
		}

		return monthList;

	}

	@ResponseBody
	@RequestMapping(value = "/doCrossingExport", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<FileSystemResource> doExport() {
		try {

			File result = crossingTemplateExcelExporter
					.export(studySelection.getWorkbook().getStudyId(),
							studySelection.getWorkbook().getStudyName());
			FileSystemResource resultResource = new FileSystemResource(result);

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			respHeaders.setContentLength(resultResource.contentLength());
			respHeaders.setContentDispositionFormData("attachment", resultResource.getFilename());

			return new ResponseEntity<>(resultResource, respHeaders, HttpStatus.OK);

		} catch (IOException | InvalidFormatException | MiddlewareQueryException e) {
			LOG.error("failed exporting of crossing template", e);

			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	protected void saveCrossSetting(CrossSetting setting, Integer programID)
			throws MiddlewareQueryException,
			JAXBException {

		List<ProgramPreset> presets = presetDataManager
				.getProgramPresetFromProgramAndTool(programID,
						getFieldbookToolID(),
						ToolSection.FBK_CROSS_IMPORT.name());

		boolean found = false;
		ProgramPreset forSaving = null;
		for (ProgramPreset preset : presets) {
			if (preset.getName().equals(setting.getName())) {
				preset.setConfiguration(settingsPresetService
						.convertPresetSettingToXml(setting, CrossSetting.class));
				found = true;
				forSaving = preset;
				break;
			}
		}

		if (!found) {
			forSaving = new ProgramPreset();
			forSaving.setName(setting.getName());
			forSaving.setToolId(getFieldbookToolID());
			forSaving.setProgramUuid(programID);
			forSaving.setToolSection(ToolSection.FBK_CROSS_IMPORT.name());
			forSaving.setConfiguration(
					settingsPresetService.convertPresetSettingToXml(setting, CrossSetting.class));
		}

		presetDataManager.saveOrUpdateProgramPreset(forSaving);
	}

	protected Integer getFieldbookToolID() throws MiddlewareQueryException {
		return workbenchService.getFieldbookWebTool().getToolId().intValue();
	}

	protected Integer getCurrentProgramID(HttpServletRequest request) {
		final ContextInfo contextInfo = (ContextInfo) WebUtils
				.getSessionAttribute(request,
						ContextConstants.SESSION_ATTR_CONTEXT_INFO);
		if (contextInfo != null) {
			return contextInfo.getSelectedProjectId().intValue();
		} else {
			return 3;
		}
	}

	public SettingsPresetService getSettingsPresetService() {
		return settingsPresetService;
	}

	public void setSettingsPresetService(SettingsPresetService settingsPresetService) {
		this.settingsPresetService = settingsPresetService;
	}

}