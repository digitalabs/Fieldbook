package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.CrossImportSettings;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;
import com.efficio.fieldbook.web.common.form.ImportCrossesForm;
import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.common.service.impl.CrossingTemplateExcelExporter;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.service.CrossNameService;
import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
	public static final String URL = "/crosses";
	public static final int YEAR_INTERVAL = 30;
	public static final String ID = "id";
	public static final String TEXT = "text";
	public static final String SUCCESS_KEY = "success";

	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsController.class);
	private static final String IS_SUCCESS = "isSuccess";

	@Resource
	private WorkbenchService workbenchService;

	@Resource
	private PresetDataManager presetDataManager;

	@Resource
	private SettingsPresetService settingsPresetService;

	@Resource
	private UserSelection studySelection;

	@Resource
	private CrossingService crossingService;

	@Resource
	private CrossNameService crossNameService;
	
	@Resource
	private ContextUtil contextUtil;

	@Resource
	private CrossingTemplateExcelExporter crossingTemplateExcelExporter;

	@Resource
	private MessageSource messageSource;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveSettings", method = RequestMethod.GET, produces = "application/json")
	public List<CrossImportSettings> getAvailableCrossImportSettings() {
		List<CrossImportSettings> settings = new ArrayList<>();

		try {
			List<ProgramPreset> presets = presetDataManager
					.getProgramPresetFromProgramAndTool(getCurrentProgramID(),
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
	public Map<String, Object> submitAndSaveCrossSettings(@RequestBody CrossSetting settings) {
		Map<String, Object> returnVal = new HashMap<>();
		try {
			saveCrossSetting(settings, getCurrentProgramID());
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
		returnVal.put(SUCCESS_KEY, 1);
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
	
	/**
	 * Validates if current study can perform an export
	 *
	 * @return a JSON result object
	 */
	@ResponseBody
	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public Map<String, Object> doCrossingExport() {
		Map<String, Object> out = new HashMap<>();
		try {
			File result = crossingTemplateExcelExporter
					.export(studySelection.getWorkbook().getStudyId(),
							studySelection.getWorkbook().getStudyName());

			out.put(IS_SUCCESS, Boolean.TRUE);
			out.put("outputFilename", result.getAbsolutePath());

		} catch (CrossingTemplateExportException | NullPointerException e) {
			LOG.debug(e.getMessage(), e);

			out.put(IS_SUCCESS, Boolean.FALSE);
			out.put("errorMessage", messageSource.getMessage(e.getMessage(), new String[] { },
					"cannot export a crossing template", LocaleContextHolder.getLocale()));
		}

		return out;
	}

	@ResponseBody
	@RequestMapping(value = "/download/file", method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<FileSystemResource> download(HttpServletRequest req) throws UnsupportedEncodingException {
		String outputFilename = new String(req.getParameter("outputFilename").getBytes("iso-8859-1"), "UTF-8");

		try {
			File resource = new File(outputFilename);
			FileSystemResource fileSystemResource = new FileSystemResource(resource);

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			respHeaders.setContentLength(fileSystemResource.contentLength());
			respHeaders
					.setContentDispositionFormData("attachment", FieldbookUtil.getDownloadFileName(fileSystemResource.getFilename(), req));

			return new ResponseEntity<>(fileSystemResource, respHeaders, HttpStatus.OK);

		} catch (IOException e) {
			LOG.error("Cannot download file " + outputFilename, e);

			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/germplasm", method = RequestMethod.POST, produces = "application/json")
	public Map<String, Object> importFile(Model model,
			@ModelAttribute("importCrossesForm") ImportCrossesForm form) {

		Map<String, Object> resultsMap = new HashMap<>();

		// 1. PARSE the file into an ImportCrosses List REF: deprecated: CrossingManagerUploader.java
		try {
			ImportedCrossesList parseResults = crossingService.parseFile(form.getFile());

			// 2. Store the crosses to study selection if all validated

			studySelection.setimportedCrossesList(parseResults);

			resultsMap.put(IS_SUCCESS, 1);

		} catch (FileParsingException e) {
			resultsMap.put(IS_SUCCESS, 0);
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/getImportedCrossesList", method = RequestMethod.GET)
	public List<Map<String, Object>> getImportedCrossesList() {

		List<Map<String, Object>> masterList = new ArrayList<>();

		if (null == studySelection.getImportedCrossesList()) {
			return masterList;
		}

		for (ImportedCrosses cross : studySelection.getImportedCrossesList().getImportedCrosses()) {
			masterList.add(generateDatatableDataMap(cross));
		}

		return masterList;
	}

	protected Map<String, Object> generateDatatableDataMap(ImportedCrosses importedCrosses) {

		Map<String, Object> dataMap = new HashMap<>();

		dataMap.put("ENTRY", importedCrosses.getEntryId());
		dataMap.put("PARENTAGE", importedCrosses.getCross());
		dataMap.put("ENTRY CODE", importedCrosses.getEntryCode());
		dataMap.put("FEMALE PARENT", importedCrosses.getFemaleDesig());
		dataMap.put("FGID", importedCrosses.getFemaleGid());
		dataMap.put("MALE PARENT", importedCrosses.getMaleDesig());
		dataMap.put("MGID", importedCrosses.getMaleGid());
		dataMap.put("SOURCE", importedCrosses.getSource());

		return dataMap;

	}

	protected void saveCrossSetting(CrossSetting setting, String programUUID)
			throws MiddlewareQueryException,
			JAXBException {

		List<ProgramPreset> presets = presetDataManager
				.getProgramPresetFromProgramAndTool(programUUID,
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
			forSaving.setProgramUuid(programUUID);
			forSaving.setToolSection(ToolSection.FBK_CROSS_IMPORT.name());
			forSaving.setConfiguration(
					settingsPresetService.convertPresetSettingToXml(setting, CrossSetting.class));
		}

		presetDataManager.saveOrUpdateProgramPreset(forSaving);
	}

	protected Integer getFieldbookToolID() throws MiddlewareQueryException {
		return workbenchService.getFieldbookWebTool().getToolId().intValue();
	}

	protected String getCurrentProgramID() {
		return contextUtil.getCurrentProgramUUID();
	}

}
