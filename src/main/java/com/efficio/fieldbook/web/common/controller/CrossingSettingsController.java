
package com.efficio.fieldbook.web.common.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.service.CrossNameService;
import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
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
import com.efficio.fieldbook.util.FieldbookUtil;
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
	public static final int YEAR_INTERVAL = 10;
	public static final String ID = "id";
	public static final String TEXT = "text";
	public static final String SUCCESS_KEY = "success";

	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsController.class);
	private static final String IS_SUCCESS = "isSuccess";
	private static final String HAS_PLOT_DUPLICATE = "hasPlotDuplicate";
	public static final String CHOOSING_LIST_OWNER_NEEDED = "isChoosingListOwnerNeeded";
	public static final String ERROR = "error";

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
	private CrossingTemplateExcelExporter crossingTemplateExcelExporter;

	@Resource
	private MessageSource messageSource;

	@Resource
	private CrossesListUtil crossesListUtil;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private UserDataManager userDataManager;

	@Resource
	private GermplasmDataManager germplasmDataManager;

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
				importSettings.setProgramPresetId(preset.getProgramPresetId());
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
	@RequestMapping(value = "/deleteSetting/{programPresetId}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteSetting(@PathVariable final Integer programPresetId) {
		final Map<String, Object> returnVal = new HashMap<>();

		this.deleteCrossSetting(programPresetId);

		returnVal.put(CrossingSettingsController.SUCCESS_KEY, "1");
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
		} catch (final MiddlewareException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
			returnVal.put(CrossingSettingsController.SUCCESS_KEY, "0");
			String errorMessage = (e instanceof MiddlewareQueryException)
					? this.messageSource.getMessage("error.no.next.name.in.sequence", new Object[] {}, LocaleContextHolder.getLocale())
					: e.getMessage();
			returnVal.put(ERROR, errorMessage);
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

		int currentYear = cal.get(Calendar.YEAR);
		
		//the years should include + 10 years, current year and - 10 years
		for (int year = currentYear + YEAR_INTERVAL; year >= currentYear - YEAR_INTERVAL; year--) {
			years.add(Integer.toString(year));
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

			final Integer currentUserId = this.workbenchService.getCurrentIbdbUserId(Long.valueOf(this.getCurrentProjectId()),
					this.contextUtil.getCurrentWorkbenchUserId());

			final File result = this.crossingTemplateExcelExporter.export(studyId, this.studySelection.getWorkbook().getStudyName(), currentUserId);

			out.put(CrossingSettingsController.IS_SUCCESS, Boolean.TRUE);
			out.put("outputFilename", result.getAbsolutePath());

		} catch (CrossingTemplateExportException | NullPointerException e) {
			CrossingSettingsController.LOG.debug(e.getMessage(), e);

			out.put(CrossingSettingsController.IS_SUCCESS, Boolean.FALSE);
			out.put("errorMessage", this.messageSource
					.getMessage(e.getMessage(), new String[] {}, "cannot export a crossing template", LocaleContextHolder.getLocale()));
		}

		return out;
	}

	@ResponseBody
	@RequestMapping(value = "/download/file", method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<FileSystemResource> download(final HttpServletRequest req) throws UnsupportedEncodingException {
		final String outputFilename = new String(req.getParameter("outputFilename").getBytes("iso-8859-1"), "UTF-8");

		return FieldbookUtil.createResponseEntityForFileDownload(new File(outputFilename));
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
			DuplicatesUtil.processDuplicatesAndReciprocals(parseResults);
			// 3. Store the crosses to study selection if all validated

		    this.setParentsInformation(parseResults.getImportedCrosses());

		    parseResults.setType(GermplasmListType.F1IMP.toString());

			this.studySelection.setImportedCrossesList(parseResults);

			resultsMap.put(CrossingSettingsController.IS_SUCCESS, 1);
			resultsMap.put(CrossingSettingsController.HAS_PLOT_DUPLICATE, parseResults.hasPlotDuplicate());

			if (!parseResults.getWarningMessages().isEmpty()) {
				resultsMap.put("warnings", parseResults.getWarningMessages());
			}

			// if no User is set we need to ask the User for the input via chooseUser modal dialog
			if (parseResults.getUserId() == null) {
				resultsMap.put(CrossingSettingsController.CHOOSING_LIST_OWNER_NEEDED, 1);
			} else {
				resultsMap.put(CrossingSettingsController.CHOOSING_LIST_OWNER_NEEDED, 0);
			}

		} catch (final FileParsingException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
			resultsMap.put(CrossingSettingsController.IS_SUCCESS, 0);
			resultsMap.put(ERROR, new String[] {e.getMessage()});
		}
		return super.convertObjectToJson(resultsMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getImportedCrossesList", method = RequestMethod.GET)
	public Map<String, Object> getImportedCrossesList() {

		final Map<String, Object> responseMap = new HashMap<>();
		ImportedCrossesList importedCrossesList = this.studySelection.getImportedCrossesList();

		if (null == importedCrossesList) {
			return responseMap;
		}

		this.crossingService.processCrossBreedingMethod(this.studySelection.getCrossSettings(), importedCrossesList);

		// TODO decouple save and apply settings and then replace this for for the apply settings method
		for (final ImportedCrosses importedCross : importedCrossesList.getImportedCrosses()) {
			this.crossingService.populateSeedSource(importedCross, this.userSelection.getWorkbook());
		}

		final List<Map<String, Object>> masterList = new ArrayList<>();
		final List<String> tableHeaderList = this.crossesListUtil.getTableHeaders();

		for (final ImportedCrosses cross : importedCrossesList.getImportedCrosses()) {
			masterList.add(this.crossesListUtil.generateCrossesTableWithDuplicationNotes(tableHeaderList, cross));
		}

		responseMap.put(CrossesListUtil.TABLE_HEADER_LIST, tableHeaderList);
		responseMap.put(CrossesListUtil.LIST_DATA_TABLE, masterList);
		responseMap.put(CrossingSettingsController.IS_SUCCESS, 1);

		responseMap.put(CrossesListUtil.IS_IMPORT, true);
		return responseMap;
	}

	@ResponseBody
	@RequestMapping(value = "/getCurrentProgramMembers", method = RequestMethod.GET, produces = "application/json")
	public Map<String, Person> getCurrentProgramMembers() {
		// we need to convert Integer to String because angular doest work with numbers as options for select
		final Map<String, Person> currentProgramMembers = new HashMap<>();
		final Long projectId = this.workbenchDataManager.getProjectByUuid(this.getCurrentProgramID()).getProjectId();
		final Map<Integer, Person> programMembers = this.workbenchDataManager.getPersonsByProjectId(projectId);
		for (final Map.Entry<Integer, Person> member : programMembers.entrySet()) {
			currentProgramMembers.put(String.valueOf(member.getKey()), member.getValue());
		}
		return currentProgramMembers;
	}

	@ResponseBody
	@RequestMapping(value = "/getCurrentUser", method = RequestMethod.GET)
	public String getCurrentWorkbenchUser() {
		return String.valueOf(this.contextUtil.getCurrentWorkbenchUserId());
	}

	@ResponseBody
	@RequestMapping(value = "/submitListOwner", method = RequestMethod.POST)
	public Map<String, Object> submitListOwner(@RequestBody final String workbenchUserId) {
		final Map<String, Object> returnVal = new HashMap<>();
		final int workbenchUID;
		try {
			workbenchUID = Integer.parseInt(workbenchUserId);
		} catch (final Exception e){
			CrossingSettingsController.LOG.error(e.getMessage(), e);
			final Map<String, Object> resultsMap = new HashMap<>();
			resultsMap.put(CrossingSettingsController.IS_SUCCESS, 0);
			final String localisedErrorMessage = this.messageSource.getMessage("error.submit.list.owner.wrong.format", new String[] {},
					"Could not associate User id with the list", LocaleContextHolder.getLocale());
			resultsMap.put(ERROR, new String[] {localisedErrorMessage});
			return resultsMap;
		}

		final Integer userId = this.workbenchService.getCurrentIbdbUserId(Long.valueOf(this.getCurrentProjectId()), workbenchUID);
		this.studySelection.getImportedCrossesList().setUserId(userId);
		returnVal.put(CrossingSettingsController.IS_SUCCESS, 1);
		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteCrossList/{createdCrossesListId}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteCrossList(@PathVariable final Integer createdCrossesListId) {
		Map<String, Object> responseMap = new HashMap<>();

		this.germplasmListManager.deleteGermplasmListByListIdPhysically(createdCrossesListId);

		responseMap.put(CrossingSettingsController.IS_SUCCESS, 1);
		return responseMap;
	}

	@ResponseBody
	@RequestMapping(value = "/getImportedCrossesList/{createdCrossesListId}", method = RequestMethod.GET)
	public Map<String, Object> getImportedCrossesList(@PathVariable final String createdCrossesListId) {

		final Map<String, Object> responseMap = new HashMap<>();
		final List<Map<String, Object>> masterList = new ArrayList<>();
		final Integer crossesListId = Integer.parseInt(createdCrossesListId);

		final List<GermplasmListData> germplasmListDataList = this.germplasmListManager.retrieveListDataWithParents(crossesListId);
		final GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(crossesListId);

		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final List<ImportedCrosses> importedCrosses = new ArrayList<>();
		final Map<Integer, ImportedCrosses> importedCrossesMap = new HashMap<>();

		final String studyName = this.studySelection.getWorkbook().getStudyDetails().getStudyName();
		final List<String> tableHeaderList = this.crossesListUtil.getTableHeaders();
		for (final GermplasmListData listData : germplasmListDataList) {
			masterList.add(this.crossesListUtil.generateCrossesTableWithDuplicationNotes(tableHeaderList, listData));
			final ImportedCrosses importedCross = this.crossesListUtil.convertGermplasmListData2ImportedCrosses(listData);
			if (importedCross.getGid() == null) {
				responseMap.put(CrossingSettingsController.IS_SUCCESS, 0);
				final String localisedErrorMessage = this.messageSource.getMessage("error.germplasm.record.already.exists", new String[] {},
						"Cross germplasm record must already exist in database when using crossing manager to create crosses in Nurseries",
						LocaleContextHolder.getLocale());
				responseMap.put(ERROR, new String[] {localisedErrorMessage});
				return responseMap;
			}
			// When crossing using crossing manager (as opposed to crossing spreadsheet import),
			// both female and male nursery is the current nursery.
			importedCross.setMaleStudyName(studyName);
			importedCross.setFemaleStudyName(studyName);
			importedCrosses.add(importedCross);
			importedCrossesMap.put(importedCross.getEntryId(), importedCross);
		}
		importedCrossesList.setImportedGermplasms(importedCrosses);
		importedCrossesList.setType(germplasmList.getType());
		importedCrossesList.setUserId(germplasmList.getUserId());
		this.userSelection.setImportedCrossesList(importedCrossesList);

		this.crossingService.processCrossBreedingMethod(this.studySelection.getCrossSettings(), importedCrossesList);

		for (Map<String, Object> map : masterList){
			Integer entryId = (Integer) map.get(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX));
			String breedingMethodIndex = tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX);
			String seedSourceIndex = tableHeaderList.get(CrossesListUtil.SOURCE_INDEX);

			map.put(breedingMethodIndex, importedCrossesMap.get(entryId).getBreedingMethodName());
			map.put(seedSourceIndex, importedCrossesMap.get(entryId).getSource());

		}

		responseMap.put(CrossesListUtil.TABLE_HEADER_LIST, tableHeaderList);
		responseMap.put(CrossesListUtil.LIST_DATA_TABLE, masterList);
		responseMap.put(CrossingSettingsController.IS_SUCCESS, 1);
		responseMap.put(CrossesListUtil.IS_IMPORT, false);
		return responseMap;
	}

	protected void deleteCrossSetting(int programPresetId)  {
		this.presetDataManager.deleteProgramPreset(programPresetId);
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

	private void setParentsInformation(List<ImportedCrosses> importedCrossesList) {

		Collection<Integer> maleGidList = Collections2.transform(importedCrossesList, new Function<ImportedCrosses, Integer>() {

			@Override public Integer apply(ImportedCrosses input) {
				return Integer.parseInt(input.getMaleGid());
			}
		});
		Collection<Integer> femaleGidList = Collections2.transform(importedCrossesList, new Function<ImportedCrosses, Integer>() {

			@Override public Integer apply(ImportedCrosses input) {
				return Integer.parseInt(input.getFemaleGid());
			}
		});

		List<Integer> gidList = new ArrayList<>();
		gidList.addAll(maleGidList);
		gidList.addAll(femaleGidList);

		ImmutableList<Integer> listWithNoDuplicates = ImmutableSet.copyOf(gidList).asList();

		Map<Integer, String[]> pedigreeMap = germplasmDataManager.getParentsInfoByGIDList(listWithNoDuplicates);

		for (ImportedCrosses importedCrosses : importedCrossesList) {
			importedCrosses.setFemalePedigree(pedigreeMap.get(Integer.parseInt(importedCrosses.getFemaleGid()))[0]);
			importedCrosses.setMalePedigree(pedigreeMap.get(Integer.parseInt(importedCrosses.getMaleGid()))[0]);
			importedCrosses.setFemaleCross(pedigreeMap.get(Integer.parseInt(importedCrosses.getFemaleGid()))[1]);
			importedCrosses.setMaleCross(pedigreeMap.get(Integer.parseInt(importedCrosses.getMaleGid()))[1]);
		}

	}


}
