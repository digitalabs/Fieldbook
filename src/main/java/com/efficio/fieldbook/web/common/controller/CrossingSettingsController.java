
package com.efficio.fieldbook.web.common.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.service.CrossNameService;
import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.CrossImportSettings;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;
import com.efficio.fieldbook.web.common.form.ImportCrossesForm;
import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.common.service.impl.CrossingTemplateExcelExporter;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.util.CrossesListUtil;
import com.efficio.fieldbook.web.util.DuplicatesUtil;

@Controller
@RequestMapping(CrossingSettingsController.URL)
public class CrossingSettingsController extends SettingsController {

	public static final String URL = "/crosses";
	public static final int YEAR_INTERVAL = 30;
	public static final String ID = "id";
	public static final String TEXT = "text";
	public static final String SUCCESS_KEY = "success";

	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsController.class);
	private static final String IS_SUCCESS = "isSuccess";
	private static final String HAS_PLOT_DUPLICATE = "hasPlotDuplicate";

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

	@Resource
	private CrossesListUtil crossesListUtil;

	/**
	 * The germplasm list manager.
	 */
	@Resource
	private GermplasmListManager germplasmListManager;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveSettings", method = RequestMethod.GET, produces = "application/json")
	public List<CrossImportSettings> getAvailableCrossImportSettings() {
		final List<CrossImportSettings> settings = new ArrayList<>();

		try {
			final List<ProgramPreset> presets =
					this.presetDataManager.getProgramPresetFromProgramAndTool(this.getCurrentProgramID(), this.getFieldbookToolID(),
							ToolSection.FBK_CROSS_IMPORT.name());

			for (final ProgramPreset preset : presets) {
				final CrossSetting crossSetting =
						(CrossSetting) this.settingsPresetService.convertPresetFromXmlString(preset.getConfiguration(), CrossSetting.class);
				final CrossImportSettings importSettings = new CrossImportSettings();
				importSettings.populate(crossSetting);
				settings.add(importSettings);
			}

		} catch (MiddlewareQueryException | JAXBException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
		}

		return settings;
	}

	@ResponseBody
	@RequestMapping(value = "/submitAndSaveSetting", method = RequestMethod.POST, consumes = "application/json",
			produces = "application/json")
	public Map<String, Object> submitAndSaveCrossSettings(@RequestBody final CrossSetting settings) {
		final Map<String, Object> returnVal = new HashMap<>();
		try {
			this.saveCrossSetting(settings, this.getCurrentProgramID());
			return this.submitCrossSettings(settings);
		} catch (MiddlewareQueryException | JAXBException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
		}

		returnVal.put(CrossingSettingsController.SUCCESS_KEY, "0");
		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/generateSequenceValue", method = RequestMethod.POST, consumes = "application/json",
			produces = "application/json")
	public Map<String, String> generateSequenceValue(@RequestBody final CrossSetting setting, final HttpServletRequest request) {
		final Map<String, String> returnVal = new HashMap<>();

		try {
			final String sequenceValue = this.crossNameService.getNextNameInSequence(setting.getCrossNameSetting());
			returnVal.put(CrossingSettingsController.SUCCESS_KEY, "1");
			returnVal.put("sequenceValue", sequenceValue);
		} catch (final MiddlewareQueryException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
			returnVal.put(CrossingSettingsController.SUCCESS_KEY, "0");
		}

		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/submit", method = RequestMethod.POST)
	public Map<String, Object> submitCrossSettings(@RequestBody final CrossSetting settings) {
		final Map<String, Object> returnVal = new HashMap<>();

		this.studySelection.setCrossSettings(settings);
		returnVal.put(CrossingSettingsController.SUCCESS_KEY, 1);
		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/getHarvestYears", method = RequestMethod.GET)
	public List<String> getHarvestYears() {
		final List<String> years = new ArrayList<>();

		final Calendar cal = DateUtil.getCalendarInstance();

		for (int i = 0; i < CrossingSettingsController.YEAR_INTERVAL; i++) {
			years.add(Integer.toString(cal.get(Calendar.YEAR)));
			cal.roll(Calendar.YEAR, false);
		}

		return years;
	}

	@ResponseBody
	@RequestMapping(value = "/getHarvestMonths", method = RequestMethod.GET)
	public List<Map<String, String>> getHarvestMonths() {
		final List<Map<String, String>> monthList = new ArrayList<>();

		final String[] monthLabels = DateFormatSymbols.getInstance().getMonths();
		int i = 1;
		for (final String monthLabel : monthLabels) {
			if (monthLabel.isEmpty()) {
				continue;
			}

			String textValue = Integer.toString(i++);
			if (textValue.length() == 1) {
				textValue = "0" + textValue;
			}

			final Map<String, String> monthMap = new HashMap<>();
			monthMap.put(CrossingSettingsController.ID, textValue);
			monthMap.put(CrossingSettingsController.TEXT, monthLabel);

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
		final Map<String, Object> out = new HashMap<>();
		try {
			Integer studyId = this.studySelection.getWorkbook().getStudyDetails().getId();
			if (studyId == null && this.studySelection.getWorkbook().getStudyDetails() != null) {
				studyId = this.studySelection.getWorkbook().getStudyDetails().getId();
			}
			final File result = this.crossingTemplateExcelExporter.export(studyId, this.studySelection.getWorkbook().getStudyName());

			out.put(CrossingSettingsController.IS_SUCCESS, Boolean.TRUE);
			out.put("outputFilename", result.getAbsolutePath());

		} catch (CrossingTemplateExportException | NullPointerException e) {
			CrossingSettingsController.LOG.debug(e.getMessage(), e);

			out.put(CrossingSettingsController.IS_SUCCESS, Boolean.FALSE);
			out.put("errorMessage",
					this.messageSource.getMessage(e.getMessage(), new String[] {}, "cannot export a crossing template",
							LocaleContextHolder.getLocale()));
		}

		return out;
	}

	@ResponseBody
	@RequestMapping(value = "/download/file", method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<FileSystemResource> download(final HttpServletRequest req) throws UnsupportedEncodingException {
		final String outputFilename = new String(req.getParameter("outputFilename").getBytes("iso-8859-1"), "UTF-8");

		try {
			final File resource = new File(outputFilename);
			final FileSystemResource fileSystemResource = new FileSystemResource(resource);

			final HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			respHeaders.setContentLength(fileSystemResource.contentLength());
			String encodedFilename = FileUtils.encodeFilenameForDownload(resource.getName());

			final String userAgent = req.getHeader("User-Agent");

			if (userAgent.indexOf("MSIE") != -1 || userAgent.indexOf("Trident") != -1) {
				// Internet Explorer has problems reading the Content-disposition header if it contains "filename*"
				respHeaders.set("Content-Disposition", "form-data;filename=\"" + encodedFilename + "\";");
			} else {
				// Those user agents (browser) that do not support the RFC 5987 encoding ignore filename when it occurs after filename.
				respHeaders.set("Content-Disposition", "form-data;filename=\"" + encodedFilename + "\"; filename*=\"UTF-8''"
						+ encodedFilename + "\";");

			}

			return new ResponseEntity<>(fileSystemResource, respHeaders, HttpStatus.OK);

		} catch (final IOException e) {
			CrossingSettingsController.LOG.error("Cannot download file " + outputFilename, e);

			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/germplasm", method = RequestMethod.POST)
	public String importFile(final Model model, @ModelAttribute("importCrossesForm") final ImportCrossesForm form) {

		final Map<String, Object> resultsMap = new HashMap<>();

		// 1. PARSE the file into an ImportCrosses List REF: deprecated:
		// CrossingManagerUploader.java
		try {
			final ImportedCrossesList parseResults = this.crossingService.parseFile(form.getFile());
			// 2. Process duplicates and set to ImportedCrossesList
			DuplicatesUtil.processDuplicates(parseResults);
			// 3. Store the crosses to study selection if all validated

			this.studySelection.setImportedCrossesList(parseResults);

			resultsMap.put(CrossingSettingsController.IS_SUCCESS, 1);
			resultsMap.put(CrossingSettingsController.HAS_PLOT_DUPLICATE, parseResults.hasPlotDuplicate());

		} catch (final FileParsingException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
			resultsMap.put(CrossingSettingsController.IS_SUCCESS, 0);
			resultsMap.put("error", new String[] {e.getMessage()});
		}
		return super.convertObjectToJson(resultsMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getImportedCrossesList", method = RequestMethod.GET)
	public List<Map<String, Object>> getImportedCrossesList(final HttpSession session) {
		// session.removeAttribute("createdCrossesListId");
		final List<Map<String, Object>> masterList = new ArrayList<>();

		if (null == this.studySelection.getImportedCrossesList()) {
			return masterList;
		}

		for (final ImportedCrosses cross : this.studySelection.getImportedCrossesList().getImportedCrosses()) {
			masterList.add(this.crossesListUtil.generateDatatableDataMapWithDups(cross));
		}

		return masterList;
	}

	@ResponseBody
	@RequestMapping(value = "/getImportedCrossesList/{createdCrossesListId}", method = RequestMethod.GET)
	public List<Map<String, Object>> getImportedCrossesList(@PathVariable final String createdCrossesListId) {

		final List<Map<String, Object>> masterList = new ArrayList<>();
		final Integer crossesListId = Integer.parseInt(createdCrossesListId);

		final List<GermplasmListData> germplasmListDataList = this.germplasmListManager.retrieveListDataWithParents(crossesListId);
		final GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(crossesListId);

		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final List<ImportedCrosses> importedCrosses = new ArrayList<>();

		for (final GermplasmListData listData : germplasmListDataList) {
			masterList.add(this.crossesListUtil.generateDatatableDataMapWithDups(listData));
			importedCrosses.add(this.crossesListUtil.convertGermplasmListData2ImportedCrosses(listData));
		}
		importedCrossesList.setImportedGermplasms(importedCrosses);
		importedCrossesList.setType(germplasmList.getType());
		this.userSelection.setImportedCrossesList(importedCrossesList);
		return masterList;
	}

	protected void saveCrossSetting(final CrossSetting setting, final String programUUID) throws MiddlewareQueryException, JAXBException {

		final List<ProgramPreset> presets =
				this.presetDataManager.getProgramPresetFromProgramAndTool(programUUID, this.getFieldbookToolID(),
						ToolSection.FBK_CROSS_IMPORT.name());

		boolean found = false;
		ProgramPreset forSaving = null;
		for (final ProgramPreset preset : presets) {
			if (preset.getName().equals(setting.getName())) {
				preset.setConfiguration(this.settingsPresetService.convertPresetSettingToXml(setting, CrossSetting.class));
				found = true;
				forSaving = preset;
				break;
			}
		}

		if (!found) {
			forSaving = new ProgramPreset();
			forSaving.setName(setting.getName());
			forSaving.setToolId(this.getFieldbookToolID());
			forSaving.setProgramUuid(programUUID);
			forSaving.setToolSection(ToolSection.FBK_CROSS_IMPORT.name());
			forSaving.setConfiguration(this.settingsPresetService.convertPresetSettingToXml(setting, CrossSetting.class));
		}

		this.presetDataManager.saveOrUpdateProgramPreset(forSaving);
	}

	protected Integer getFieldbookToolID() throws MiddlewareQueryException {
		return this.workbenchService.getFieldbookWebTool().getToolId().intValue();
	}

	protected String getCurrentProgramID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	public void setCrossesListUtil(final CrossesListUtil crossesListUtil) {
		this.crossesListUtil = crossesListUtil;
	}
}
