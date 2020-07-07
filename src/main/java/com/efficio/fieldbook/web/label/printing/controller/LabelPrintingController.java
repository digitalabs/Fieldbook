/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.label.printing.controller;

import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.util.labelprinting.LabelPrintingUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.constant.LabelPrintingFileTypes;
import com.efficio.fieldbook.web.label.printing.form.LabelPrintingForm;
import com.efficio.fieldbook.web.label.printing.xml.BarcodeLabelPrintingSetting;
import com.efficio.fieldbook.web.label.printing.xml.CSVExcelLabelPrintingSetting;
import com.efficio.fieldbook.web.label.printing.xml.LabelPrintingSetting;
import com.efficio.fieldbook.web.label.printing.xml.PDFLabelPrintingSetting;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.CustomReportTypeUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.reports.BuildReportException;
import org.generationcp.middleware.reports.Reporter;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.ReportService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The Class LabelPrintingController.
 * <p>
 * This class would handle the label printing for the pdf and excel generation.
 */
@Controller
@RequestMapping({LabelPrintingController.URL})
public class LabelPrintingController extends AbstractBaseFieldbookController {

	protected static final String FILE_NAME = "fileName";
	/**
	 * The Constant URL.
	 */
	public static final String URL = "/LabelPrinting/specifyLabelDetails";
	static final String IS_SUCCESS = "isSuccess";
	private static final String AVAILABLE_FIELDS = "availableFields";
	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingController.class);

	/**
	 * The user label printing.
	 */
	@Resource
	private UserLabelPrinting userLabelPrinting;
	/**
	 * The fieldbook middleware service.
	 */
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	/**
	 * The label printing service.
	 */
	@Resource
	private LabelPrintingService labelPrintingService;
	/**
	 * The user fieldmap.
	 */
	@Resource
	private UserFieldmap userFieldmap;
	/**
	 * The message source.
	 */
	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private UserSelection userSelection;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private ReportService reportService;

	@Resource
	private GermplasmListManager germplasmListManager;

	@Resource
	private InventoryDataManager inventoryDataManager;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private LabelPrintingUtil labelPrintingUtil;

	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	/**
	 * Show trial label details.
	 *
	 * @param form    the form
	 * @param model   the model
	 * @param session the session
	 * @param id      the id
	 * @param locale  the locale
	 * @return the string
	 */
	@RequestMapping(value = "/trial/{id}", method = RequestMethod.GET)
	public String showTrialLabelDetails(@ModelAttribute("labelPrintingForm") final LabelPrintingForm form, final Model model,
		final HttpServletRequest req, final HttpSession session, @PathVariable final int id, final Locale locale) {

		SessionUtility.clearSessionData(session, new String[] {
			SessionUtility.FIELDMAP_SESSION_NAME,
			SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
		Study study = null;
		final List<FieldMapInfo> fieldMapInfoList;
		FieldMapInfo fieldMapInfo = null;
		boolean hasFieldMap = false;
		try {
			study = this.fieldbookMiddlewareService.getStudy(id);
			final List<Integer> ids = new ArrayList<>();
			ids.add(id);
			fieldMapInfoList = this.fieldbookMiddlewareService.getFieldMapInfoOfTrial(ids, this.crossExpansionProperties);

			for (final FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
				fieldMapInfo = fieldMapInfoDetail;
				hasFieldMap = this.labelPrintingService.checkAndSetFieldmapProperties(this.userLabelPrinting, fieldMapInfoDetail);
			}
		} catch (final MiddlewareException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}
		this.userLabelPrinting.setStudyId(id);
		this.userLabelPrinting.setStudy(study);
		this.userLabelPrinting.setFieldMapInfo(fieldMapInfo);
		this.userLabelPrinting.setBarcodeNeeded("0");
		this.userLabelPrinting.setBarcodeGeneratedAutomatically("1");
		this.userLabelPrinting.setIncludeColumnHeadinginNonPdf("1");
		this.userLabelPrinting.setNumberOfLabelPerRow("3");
		this.userLabelPrinting.setTitle(study != null ? study.getDescription() : null);
		this.userLabelPrinting.setName(study != null ? study.getName() : null);

		this.userLabelPrinting.setFilename(this.generateDefaultFilename(this.userLabelPrinting));
		form.setUserLabelPrinting(this.userLabelPrinting);

		model.addAttribute(LabelPrintingController.AVAILABLE_FIELDS,
			this.labelPrintingService.getAvailableLabelFieldsForStudy(hasFieldMap, locale, id));

		return super.show(model);
	}

	/**
	 * Show fieldmap label details.
	 *
	 * @param form    the form
	 * @param model   the model
	 * @param session the session
	 * @param locale  the locale
	 * @return the string
	 */
	@RequestMapping(value = "/fieldmap", method = RequestMethod.GET)
	public String showFieldmapLabelDetails(@ModelAttribute("labelPrintingForm") final LabelPrintingForm form, final Model model,
		final HttpSession session, final Locale locale) {
		final List<FieldMapInfo> fieldMapInfoList = this.userFieldmap.getSelectedFieldMaps();

		// sets the initial fieldMapInfo from fieldMapInfoList
		// this will be used later for the generation of labels in label
		// printing
		final FieldMapInfo fieldMapInfo = fieldMapInfoList.get(0);
		this.userLabelPrinting.setStudyId(null);
		this.userLabelPrinting.setTitle(StringUtils.EMPTY);
		this.userLabelPrinting.setName(StringUtils.EMPTY);
		this.userLabelPrinting.setFieldMapInfo(fieldMapInfo);
		this.userLabelPrinting.setFieldMapInfoList(fieldMapInfoList);
		this.userLabelPrinting.setBarcodeNeeded("0");
		this.userLabelPrinting.setBarcodeGeneratedAutomatically("1");
		this.userLabelPrinting.setIncludeColumnHeadinginNonPdf("1");
		this.userLabelPrinting.setNumberOfLabelPerRow("3");

		this.userLabelPrinting.setFirstBarcodeField(StringUtils.EMPTY);
		this.userLabelPrinting.setSecondBarcodeField(StringUtils.EMPTY);
		this.userLabelPrinting.setThirdBarcodeField(StringUtils.EMPTY);
		this.userLabelPrinting.setFieldMapsExisting(true);

		this.userLabelPrinting.setSettingsName(StringUtils.EMPTY);

		this.userLabelPrinting.setFilename(this.generateDefaultFilename(this.userLabelPrinting));
		form.setUserLabelPrinting(this.userLabelPrinting);

		model.addAttribute(LabelPrintingController.AVAILABLE_FIELDS,
			this.labelPrintingService.getAvailableLabelFieldsForFieldMap(true, locale));

		return super.show(model);
	}

	@RequestMapping(value = "/stock/{id}", method = RequestMethod.GET)
	public String showStockListLabelDetails(@ModelAttribute("labelPrintingForm") final LabelPrintingForm form, final Model model,
		final HttpSession session, @PathVariable final int id, final Locale locale) {

		SessionUtility.clearSessionData(session, new String[] {
			SessionUtility.FIELDMAP_SESSION_NAME,
			SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});

		// retrieve the stock list
		final GermplasmList stockList = this.germplasmListManager.getGermplasmListById(id);

		final Study study = this.fieldbookMiddlewareService.getStudy(stockList.getProjectId());
		final List<Integer> ids = new ArrayList<>();
		ids.add(stockList.getProjectId());

		final List<FieldMapInfo> fieldMapInfoList;

		fieldMapInfoList = this.fieldbookMiddlewareService.getFieldMapInfoOfTrial(ids, this.crossExpansionProperties);

		for (final FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
			this.userLabelPrinting.setFieldMapInfo(fieldMapInfoDetail);
			this.labelPrintingService.checkAndSetFieldmapProperties(this.userLabelPrinting, fieldMapInfoDetail);
		}

		this.userLabelPrinting.setStudy(study);
		this.userLabelPrinting.setBarcodeNeeded("0");
		this.userLabelPrinting.setBarcodeGeneratedAutomatically("1");
		this.userLabelPrinting.setIncludeColumnHeadinginNonPdf("1");
		this.userLabelPrinting.setNumberOfLabelPerRow("3");
		this.userLabelPrinting.setFilename(this.generateDefaultFilename(this.userLabelPrinting));
		form.setUserLabelPrinting(this.userLabelPrinting);
		model.addAttribute(LabelPrintingController.AVAILABLE_FIELDS, this.labelPrintingService.getAvailableLabelFieldsForStockList(
			this.labelPrintingService.getStockListType(stockList.getType()), locale, stockList.getProjectId()));

		return super.show(model);
	}

	/**
	 * Generate default filename.
	 *
	 * @param userLabelPrinting the user label printing
	 * @return the string
	 */
	protected String generateDefaultFilename(final UserLabelPrinting userLabelPrinting) {
		final String currentDate = DateUtil.getCurrentDateAsStringValue();
		String fileName = "Labels-for-" + userLabelPrinting.getName();

		if (userLabelPrinting.getFieldMapInfoList() != null) {
			fileName = "Study-Field-Map-Labels-" + currentDate;
		} else {
			// changed selected name to block name for now
			if (!StringUtils.isEmpty(userLabelPrinting.getNumberOfInstances())) {
				fileName += "-" + userLabelPrinting.getNumberOfInstances();
			}
			fileName += "-" + currentDate;
		}
		fileName = SettingsUtil.cleanSheetAndFileName(fileName);
		return fileName;
	}

	/**
	 * Export File
	 *
	 * @param req
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> exportFile(final HttpServletRequest req) throws UnsupportedEncodingException {

		final String filename = this.userLabelPrinting.getFilenameWithExtension();
		final String absoluteLocation = this.userLabelPrinting.getFilenameDLLocation();

		return FieldbookUtil.createResponseEntityForFileDownload(absoluteLocation, filename);
	}

	/* Submits the details.
	 *
	 * @param form the form
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, Object> submitDetails(@ModelAttribute("labelPrintingForm") final LabelPrintingForm form) {
		try {
			final String generateAutomatically =
				form.getUserLabelPrinting().getBarcodeGeneratedAutomatically() == null || form.getUserLabelPrinting()
					.getBarcodeGeneratedAutomatically().equals("0") ? "0" : "1";
			this.userLabelPrinting.setBarcodeNeeded(form.getUserLabelPrinting().getBarcodeNeeded());
			this.userLabelPrinting.setBarcodeGeneratedAutomatically(generateAutomatically);
			this.userLabelPrinting.setSizeOfLabelSheet(form.getUserLabelPrinting().getSizeOfLabelSheet());
			this.userLabelPrinting.setNumberOfLabelPerRow(form.getUserLabelPrinting().getNumberOfLabelPerRow());
			this.userLabelPrinting.setNumberOfRowsPerPageOfLabel(form.getUserLabelPrinting().getNumberOfRowsPerPageOfLabel());
			this.userLabelPrinting.setLeftSelectedLabelFields(form.getUserLabelPrinting().getLeftSelectedLabelFields());
			this.userLabelPrinting.setRightSelectedLabelFields(form.getUserLabelPrinting().getRightSelectedLabelFields());
			this.userLabelPrinting.setMainSelectedLabelFields(form.getUserLabelPrinting().getMainSelectedLabelFields());
			this.userLabelPrinting.setIncludeColumnHeadinginNonPdf(form.getUserLabelPrinting().getIncludeColumnHeadinginNonPdf());
			this.userLabelPrinting.setSettingsName(form.getUserLabelPrinting().getSettingsName());
			this.userLabelPrinting.setFirstBarcodeField(form.getUserLabelPrinting().getFirstBarcodeField());
			this.userLabelPrinting.setSecondBarcodeField(form.getUserLabelPrinting().getSecondBarcodeField());
			this.userLabelPrinting.setThirdBarcodeField(form.getUserLabelPrinting().getThirdBarcodeField());
			this.userLabelPrinting.setFilename(form.getUserLabelPrinting().getFilename());
			this.userLabelPrinting.setGenerateType(form.getUserLabelPrinting().getGenerateType());

			// add validation for the file name
			if (!FileUtils.isFilenameValid(this.userLabelPrinting.getFilename())) {
				final Map<String, Object> results = new HashMap<>();
				results.put(LabelPrintingController.IS_SUCCESS, 0);
				results.put(AppConstants.MESSAGE.getString(),
					this.messageSource.getMessage("common.error.invalid.filename.windows", new Object[] {}, Locale.getDefault()));

				return results;
			}

			final Workbook workbook = this.userSelection.getWorkbook();
			// workbook.observations() collection is no longer pre-loaded into user session when trial is opened. Load now as we need it to
			// keep label printing functionality working as before (all plots assumed loaded).
			this.fieldbookMiddlewareService.loadAllObservations(workbook);

			if (workbook != null) {
				final String selectedLabelFields = this.getSelectedLabelFields(this.userLabelPrinting);
				this.labelPrintingService
					.populateUserSpecifiedLabelFields(this.userLabelPrinting.getFieldMapInfo().getDatasets().get(0).getTrialInstances(),
						workbook, selectedLabelFields, this.userLabelPrinting);
			}

			final List<FieldMapInfo> fieldMapInfoList = this.userLabelPrinting.getFieldMapInfoList();

			final List<StudyTrialInstanceInfo> trialInstances;

			if (fieldMapInfoList != null) {
				trialInstances = this.generateTrialInstancesFromSelectedFieldMaps(fieldMapInfoList, form);
			} else {
				// initial implementation of BMS-186 will be for single studies
				// only, not for cases where multiple studies participating in a
				// single fieldmap
				trialInstances = this.generateTrialInstancesFromFieldMap();

				for (final StudyTrialInstanceInfo trialInstance : trialInstances) {
					final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();
					fieldMapTrialInstanceInfo.setLocationName(fieldMapTrialInstanceInfo.getSiteName());
				}
			}

			return this.generateLabels(trialInstances, form.isCustomReport());
		} finally {
			// TODO See BMS-4454
			// Important to clear out the observations collection from user session, once we are done with it to keep heap memory under
			// control. For large trials/nurseries the observations collection can be huge.
			this.userSelection.getWorkbook().getObservations().clear();
		}
	}

	String getSelectedLabelFields(final UserLabelPrinting userLabelPrinting) {
		final String selectedLabelFields;
		if (userLabelPrinting.getGenerateType().equalsIgnoreCase(AppConstants.LABEL_PRINTING_PDF.getString())) {
			selectedLabelFields = userLabelPrinting.getLeftSelectedLabelFields() + "," + userLabelPrinting.getRightSelectedLabelFields();
		} else {
			selectedLabelFields = userLabelPrinting.getMainSelectedLabelFields();
		}
		return selectedLabelFields;
	}

	Map<String, Object> generateLabels(final List<StudyTrialInstanceInfo> trialInstances, final boolean isCustomReport) {
		final Map<String, Object> results = new HashMap<>();

		try {
			if (isCustomReport) {
				this.generateLabelForCustomReports(results);
			} else {
				this.generateLabelForLabelTypes(trialInstances, results);
			}

		} catch (IOException | MiddlewareException | JRException | BuildReportException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
			results.put(LabelPrintingController.IS_SUCCESS, 0);
			results.put(AppConstants.MESSAGE.getString(), e.getMessage());
		} catch (final LabelPrintingException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
			results.put(LabelPrintingController.IS_SUCCESS, 0);
			final Locale locale = LocaleContextHolder.getLocale();

			if (e.getErrorCode() != null) {
				results.put(AppConstants.MESSAGE.getString(),
					this.messageSource.getMessage(e.getErrorCode(), new String[] {e.getLabelError()}, locale));
			} else if (e.getCause() != null) {
				results.put(AppConstants.MESSAGE.getString(), e.getCause().getMessage());
			}

		}
		return results;
	}

	void generateLabelForLabelTypes(final List<StudyTrialInstanceInfo> trialInstances, final Map<String, Object> results)
		throws LabelPrintingException, IOException {
		final String fileName;
		final LabelPrintingFileTypes selectedLabelPrintingType =
			LabelPrintingFileTypes.getFileTypeByIndex(this.userLabelPrinting.getGenerateType());

		if (selectedLabelPrintingType.isValid()) {
			this.getFileNameAndSetFileLocations(selectedLabelPrintingType.getExtension());

			fileName = this.labelPrintingService.generateLabels(selectedLabelPrintingType.getFormIndex(), trialInstances,
				this.userLabelPrinting);

			results.put(LabelPrintingController.IS_SUCCESS, 1);
			results.put(FILE_NAME, fileName);

		} else {
			final String errorMsg = this.messageSource.getMessage("label.printing.cannot.generate.invalid.type", new String[] {},
				LocaleContextHolder.getLocale());

			LabelPrintingController.LOG.error(errorMsg);
			results.put(LabelPrintingController.IS_SUCCESS, 0);
			results.put(AppConstants.MESSAGE.getString(), errorMsg);
		}

	}

	void generateLabelForCustomReports(final Map<String, Object> results) throws JRException, IOException, BuildReportException {
		final Integer studyId = this.userLabelPrinting.getStudyId();
		final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

		final Reporter rep = this.reportService.getStreamReport(this.userLabelPrinting.getGenerateType(), studyId,
			this.contextUtil.getProjectInContext().getProjectName(), byteStream);

		// additionally creates the file in 'target' folder, for human
		// validation ;)
		final String fileName = rep.getFileName();

		this.getFileNameAndSetFileLocations("." + rep.getFileExtension());

		Files.write(Paths.get(this.userLabelPrinting.getFilenameDLLocation()), byteStream.toByteArray());

		this.userLabelPrinting.setFilename(fileName);

		results.put(LabelPrintingController.IS_SUCCESS, 1);
		results.put(FILE_NAME, fileName);

	}

	@ResponseBody
	@RequestMapping(value = "/presets/list", method = RequestMethod.GET)
	public List<LabelPrintingPresets> getLabelPrintingPresets(final HttpServletRequest request) {
		final ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

		try {
			return this.labelPrintingService.getAllLabelPrintingPresets(contextInfo.getSelectedProjectId().intValue());
		} catch (final LabelPrintingException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
	}

	@ResponseBody
	@RequestMapping(value = "/custom/reports", method = RequestMethod.GET)
	public List<CustomReportType> getLabelPrintingCustomReports() {
		final List<CustomReportType> customReportTypes = new ArrayList<>();
		try {

			final int fieldbookToolId = this.workbenchDataManager.getToolWithName(ToolName.FIELDBOOK_WEB.getName()).getToolId().intValue();

			if (this.userLabelPrinting.getStudyId() != null) {

				final List<StandardPreset> standardPresetList =
					this.workbenchDataManager
						.getStandardPresetFromCropAndTool(this.contextUtil.getProjectInContext().getCropType().getCropName().toLowerCase(),
							fieldbookToolId, ToolSection.FB_LBL_PRINT_CUSTOM_REPORT.name());

				// we need to convert the standard preset for custom report type
				// to custom report type pojo
				for (int index = 0; index < standardPresetList.size(); index++) {
					customReportTypes.addAll(CustomReportTypeUtil.readReportConfiguration(standardPresetList.get(index),
						this.crossExpansionProperties.getProfile()));
				}
			}
		} catch (final MiddlewareQueryException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}

		return customReportTypes;
	}

	@ResponseBody
	@RequestMapping(value = "/presets/{presetType}/{presetId}", method = RequestMethod.GET, produces = "application/json")
	public LabelPrintingSetting getLabelPrintingSetting(@PathVariable final int presetType, @PathVariable final int presetId,
		final HttpServletRequest request) {
		try {
			return this.getLabelPrintingSetting(presetType, presetId);
		} catch (final JAXBException e) {
			LabelPrintingController.LOG.error(this.messageSource.getMessage("label.printing.error.parsing.preset.xml", new String[] {},
				LocaleContextHolder.getLocale()), e);

		} catch (final LabelPrintingException e) {
			final String labelError = this.messageSource.getMessage(e.getLabelError(), new String[] {}, LocaleContextHolder.getLocale());

			LabelPrintingController.LOG
				.error(this.messageSource.getMessage(e.getErrorCode(), new String[] {labelError}, LocaleContextHolder.getLocale()), e);
		}

		return new LabelPrintingSetting();
	}

	private LabelPrintingSetting getLabelPrintingSetting(final int presetType, final int presetId)
		throws JAXBException, LabelPrintingException {
		final Unmarshaller parseXML = JAXBContext.newInstance(LabelPrintingSetting.class).createUnmarshaller();

		// retrieve appropriate setting
		final String xmlToRead = this.labelPrintingService.getLabelPrintingPresetConfig(presetId, presetType);

		return (LabelPrintingSetting) parseXML.unmarshal(new StringReader(xmlToRead));
	}

	/**
	 * Search program-preset,
	 *
	 * @param presetName
	 * @param request
	 * @return list of presets that matches presetName
	 */
	@ResponseBody
	@RequestMapping(value = "/presets/searchLabelPrintingPresetByName", method = RequestMethod.GET)
	public List<LabelPrintingPresets> searchLabelPrintingPresetByName(@RequestParam("name") final String presetName,
		final HttpServletRequest request) {
		final ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

		try {
			return this.labelPrintingService.getAllLabelPrintingPresetsByName(presetName, contextInfo.getSelectedProjectId().intValue());
		} catch (final MiddlewareQueryException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
			return new ArrayList<>();
		}

	}

	/**
	 * Delete's program preset
	 *
	 * @param programPresetId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/presets/delete", method = RequestMethod.GET)
	public Boolean deleteLabelPrintingPreset(@RequestParam("programPresetId") final Integer programPresetId) {

		try {
			this.labelPrintingService.deleteProgramPreset(programPresetId);

			return true;

		} catch (final MiddlewareQueryException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}

		return false;
	}

	/**
	 * Saves the label printing setting. Note that the fields should be pre-validated before calling this service
	 *
	 * @param labelPrintingPresetSetting
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/presets/save", method = RequestMethod.POST)
	public Boolean saveLabelPrintingSetting(@ModelAttribute("labelPrintingForm") final LabelPrintingForm labelPrintingPresetSetting,
		final HttpServletRequest request) {
		final UserLabelPrinting rawSettings = labelPrintingPresetSetting.getUserLabelPrinting();

		// save or update
		try {
			final ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

			this.labelPrintingService.saveOrUpdateLabelPrintingPresetConfig(rawSettings.getSettingsName(),
				this.transformLabelPrintingSettingsToXML(rawSettings), contextInfo.getSelectedProjectId().intValue());

		} catch (final MiddlewareQueryException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	@ResponseBody
	@RequestMapping(value = "/presets/isModified/{presetType}/{presetId}", method = RequestMethod.POST)
	public Boolean isLabelPrintingIsModified(@ModelAttribute("labelPrintingForm") final LabelPrintingForm labelPrintingPresetSetting,
		@PathVariable final Integer presetType, @PathVariable final Integer presetId, final HttpServletRequest request) {
		final LabelPrintingSetting lbSetting;
		try {
			lbSetting = this.getLabelPrintingSetting(presetType, presetId);
		} catch (JAXBException | LabelPrintingException e) {
			return true;
		}
		final LabelPrintingSetting modifiedSetting;
		final Unmarshaller parseXML;
		try {

			parseXML = JAXBContext.newInstance(LabelPrintingSetting.class).createUnmarshaller();

			// retrieve appropriate setting
			final String xmlToRead = this.transformLabelPrintingSettingsToXML(labelPrintingPresetSetting.getUserLabelPrinting());

			modifiedSetting = (LabelPrintingSetting) parseXML.unmarshal(new StringReader(xmlToRead));

			return !modifiedSetting.equals(lbSetting);

		} catch (final JAXBException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}

		return false;
	}

	/**
	 * @param rawSettings
	 * @return
	 */
	private String transformLabelPrintingSettingsToXML(final UserLabelPrinting rawSettings) {
		// Preparation, convert the form into appropriate pojos for easy access
		CSVExcelLabelPrintingSetting nonPDFSettings = null;
		PDFLabelPrintingSetting pdfSettings = null;
		final String barcodeGeneratedAutomatically = rawSettings.getBarcodeGeneratedAutomatically();
		final boolean isPlotCodePrefix = barcodeGeneratedAutomatically == null || barcodeGeneratedAutomatically.equals("0") ? false : true;
		final BarcodeLabelPrintingSetting barcodeSettings = new BarcodeLabelPrintingSetting(
			"1".equals(rawSettings.getBarcodeNeeded()), "Barcode", StringUtil.stringify(new String[] {
			rawSettings.getFirstBarcodeField(), rawSettings.getSecondBarcodeField(), rawSettings.getThirdBarcodeField()}, ","),
			isPlotCodePrefix);

		if (AppConstants.LABEL_PRINTING_PDF.getString().equals(rawSettings.getGenerateType())) {
			pdfSettings = new PDFLabelPrintingSetting(rawSettings.getSizeOfLabelSheet(),
				Integer.parseInt(rawSettings.getNumberOfRowsPerPageOfLabel(), 10), rawSettings.getLeftSelectedLabelFields(),
				rawSettings.getRightSelectedLabelFields());
		} else {
			nonPDFSettings = new CSVExcelLabelPrintingSetting("1".equals(rawSettings.getIncludeColumnHeadinginNonPdf()),
				rawSettings.getMainSelectedLabelFields());
		}

		// get the xml value
		String xmlConfig = StringUtils.EMPTY;
		try {
			xmlConfig = this.generateXMLFromLabelPrintingSettings(rawSettings.getSettingsName(),
				LabelPrintingFileTypes.getFileTypeByIndex(rawSettings.getGenerateType()).getType(), nonPDFSettings, pdfSettings,
				barcodeSettings, rawSettings.getSorting(), rawSettings.getNumberOfCopies());
		} catch (final JAXBException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}

		return xmlConfig;
	}

	private String generateXMLFromLabelPrintingSettings(final String name, final String outputType,
		final CSVExcelLabelPrintingSetting csvSettings, final PDFLabelPrintingSetting pdfSettings,
		final BarcodeLabelPrintingSetting barcodeSettings, final String sorting, final String numberOfCopies) throws JAXBException {
		final LabelPrintingSetting labelPrintingSetting =
			new LabelPrintingSetting(name, outputType, csvSettings, pdfSettings, barcodeSettings, sorting, numberOfCopies);

		final JAXBContext context = JAXBContext.newInstance(LabelPrintingSetting.class);
		final Marshaller marshaller = context.createMarshaller();
		final StringWriter writer = new StringWriter();
		marshaller.marshal(labelPrintingSetting, writer);

		return writer.toString();
	}

	private String getFileNameAndSetFileLocations(final String extension) throws IOException {
		String filenameWithoutExtension = this.userLabelPrinting.getFilename().replaceAll(" ", "-");
		filenameWithoutExtension = FileUtils.sanitizeFileName(filenameWithoutExtension);
		final String fileNameLocation =
			this.installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(filenameWithoutExtension, extension,
				this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);

		final String filenameWithExtension = filenameWithoutExtension + extension;
		this.userLabelPrinting.setFilenameWithExtension(filenameWithExtension);
		this.userLabelPrinting.setFilenameDLLocation(fileNameLocation);
		return filenameWithExtension;
	}

	/**
	 * Generate trial instances from field map.
	 *
	 * @return the list
	 */
	private List<StudyTrialInstanceInfo> generateTrialInstancesFromFieldMap() {
		final List<FieldMapTrialInstanceInfo> trialInstances =
			this.userLabelPrinting.getFieldMapInfo().getDatasets().get(0).getTrialInstances();
		final List<StudyTrialInstanceInfo> studyTrial = new ArrayList<>();

		for (final FieldMapTrialInstanceInfo trialInstance : trialInstances) {
			final StudyTrialInstanceInfo studyTrialInstance =
				new StudyTrialInstanceInfo(trialInstance, this.userLabelPrinting.getFieldMapInfo().getFieldbookName());
			studyTrial.add(studyTrialInstance);
		}
		return studyTrial;
	}

	/**
	 * Generate trial instances from selected field maps.
	 *
	 * @param fieldMapInfoList the field map info list
	 * @param form             the form
	 * @return the list
	 */
	private List<StudyTrialInstanceInfo> generateTrialInstancesFromSelectedFieldMaps(final List<FieldMapInfo> fieldMapInfoList,
		final LabelPrintingForm form) {
		final List<StudyTrialInstanceInfo> trialInstances = new ArrayList<>();
		final String[] fieldMapOrder = form.getUserLabelPrinting().getOrder().split(",");
		for (final String fieldmap : fieldMapOrder) {
			final String[] fieldMapGroup = fieldmap.split("\\|");
			final int order = Integer.parseInt(fieldMapGroup[0]);
			final int studyId = Integer.parseInt(fieldMapGroup[1]);
			final int datasetId = Integer.parseInt(fieldMapGroup[2]);
			final int instanceId = Integer.parseInt(fieldMapGroup[3]);

			for (final FieldMapInfo fieldMapInfo : fieldMapInfoList) {
				if (fieldMapInfo.getFieldbookId().equals(studyId)) {
					fieldMapInfo.getDataSet(datasetId).getTrialInstance(instanceId).setOrder(order);
					final StudyTrialInstanceInfo trialInstance = new StudyTrialInstanceInfo(
						fieldMapInfo.getDataSet(datasetId).getTrialInstance(instanceId), fieldMapInfo.getFieldbookName());
					if (this.userFieldmap.getBlockName() != null && this.userFieldmap.getLocationName() != null) {
						trialInstance.getTrialInstance().setBlockName(this.userFieldmap.getBlockName());
						trialInstance.getTrialInstance().setFieldName(this.userFieldmap.getFieldName());
						trialInstance.getTrialInstance().setLocationName(this.userFieldmap.getLocationName());
					}
					trialInstances.add(trialInstance);
					break;
				}
			}
		}

		Collections.sort(trialInstances, new Comparator<StudyTrialInstanceInfo>() {

			@Override
			public int compare(final StudyTrialInstanceInfo o1, final StudyTrialInstanceInfo o2) {
				return o1.getTrialInstance().getOrder().compareTo(o2.getTrialInstance().getOrder());
			}
		});

		return trialInstances;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName ()
	 */
	@Override
	public String getContentName() {
		return "LabelPrinting/specifyLabelDetails";
	}

	/**
	 * Sets the user label printing.
	 *
	 * @param userLabelPrinting the new user label printing
	 */
	public void setUserLabelPrinting(final UserLabelPrinting userLabelPrinting) {
		this.userLabelPrinting = userLabelPrinting;
	}

	@Override
	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public CrossExpansionProperties getCrossExpansionProperties() {
		return this.crossExpansionProperties;
	}

	public void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

	/**
	 * Enable setting of reportService so we can inject dependency in tests runtime
	 *
	 * @param reportService
	 */
	void setReportService(final ReportService reportService) {
		this.reportService = reportService;
	}

	void setUserFieldMap(final UserFieldmap userFieldmap) {
		this.userFieldmap = userFieldmap;
	}

	void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	UserLabelPrinting getUserLabelPrinting() {
		return this.userLabelPrinting;
	}
}
