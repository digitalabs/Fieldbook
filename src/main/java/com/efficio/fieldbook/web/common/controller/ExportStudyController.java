
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.CsvExportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.common.service.ExportAdvanceListService;
import com.efficio.fieldbook.web.common.service.ExportDataCollectionOrderService;
import com.efficio.fieldbook.web.common.service.KsuCsvExportStudyService;
import com.efficio.fieldbook.web.common.service.KsuExcelExportStudyService;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingRowColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverRangeImpl;
import com.efficio.fieldbook.web.trial.bean.ExportTrialInstanceBean;
import org.generationcp.commons.constant.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import net.sf.jasperreports.engine.JRException;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.reports.service.JasperReportService;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.reports.BuildReportException;
import org.generationcp.middleware.reports.Reporter;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.ReportService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

@Controller
@RequestMapping(ExportStudyController.URL)
public class ExportStudyController extends AbstractBaseFieldbookController {

	protected static final String CONTENT_TYPE = "contentType";
	protected static final String FILENAME = "filename";
	protected static final String OUTPUT_FILENAME = "outputFilename";
	private static final String UTF_8 = "UTF-8";
	private static final String ISO_8859_1 = "iso-8859-1";
	private static final String ERROR_MESSAGE = "errorMessage";
	static final String IS_SUCCESS = "isSuccess";
	private static final Logger LOG = LoggerFactory.getLogger(ExportStudyController.class);
	public static final String URL = "/ExportManager";
	private static final String EXPORT_TRIAL_INSTANCE = "Common/includes/exportTrialInstance";
	private static final String DISPLAY_ADVANCE_GERMPLASM_LIST = "Common/includes/displayListOfAdvanceGermplasmList";

	@Resource
	private UserSelection studySelection;

	@Resource
	private ExcelExportStudyService excelExportStudyService;

	@Resource
	private CsvExportStudyService csvExportStudyService;

	@Resource
	private KsuExcelExportStudyService ksuExcelExportStudyService;

	@Resource
	private KsuCsvExportStudyService ksuCsvExportStudyService;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private ExportOrderingRowColImpl exportOrderingRowColService;

	@Resource
	private ExportOrderingSerpentineOverRangeImpl exportOrderingSerpentineOverRangeService;

	@Resource
	private ExportOrderingSerpentineOverColImpl exportOrderingSerpentineOverColumnService;

	@Resource
	private ExportAdvanceListService exportAdvanceListService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private ReportService reportService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private GermplasmExportService germplasmExportService;

	@Resource
	private JasperReportService jasperReportService;

	@Resource
	private StudyDataManager studyDataManager;
	
	@Override
	public String getContentName() {
		return null;
	}

	@RequestMapping(value = "/download/file", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> downloadFile(final HttpServletRequest req) throws UnsupportedEncodingException {

		final String outputFilename =
				new String(req.getParameter(ExportStudyController.OUTPUT_FILENAME).getBytes(ExportStudyController.ISO_8859_1),
						ExportStudyController.UTF_8);
		final String filename = new String(req.getParameter(ExportStudyController.FILENAME).getBytes(ExportStudyController.ISO_8859_1),
				ExportStudyController.UTF_8);

		return FieldbookUtil.createResponseEntityForFileDownload(outputFilename, filename);

	}

	@ResponseBody
	@RequestMapping(value = "/export/custom/report", method = RequestMethod.POST)
	public String exportCustomReport(@RequestBody final Map<String, String> data, final HttpServletRequest req,
			final HttpServletResponse response) {
		final String studyId = this.getStudyId(data);
		final String reportCode = data.get("customReportCode");
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "";
		String outputFilename = "";
		final Reporter rep;
		final Map<String, Object> results = new HashMap<>();
		try {

			rep = this.reportService.getStreamReport(reportCode, Integer.parseInt(studyId),
					this.contextUtil.getProjectInContext().getProjectName(), baos);

			fileName = rep.getFileName();
			outputFilename = this.fieldbookProperties.getUploadDirectory() + File.separator + fileName;

			final File reportFile = new File(outputFilename);
			baos.writeTo(new FileOutputStream(reportFile));

			results.put(ExportStudyController.IS_SUCCESS, true);
			results.put(ExportStudyController.OUTPUT_FILENAME, outputFilename);
			results.put(ExportStudyController.FILENAME, SettingsUtil.cleanSheetAndFileName(fileName));
			results.put(ExportStudyController.CONTENT_TYPE, response.getContentType());

		} catch (final NumberFormatException | JRException | IOException | BuildReportException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
			results.put(ExportStudyController.IS_SUCCESS, false);
			results.put(ExportStudyController.ERROR_MESSAGE, this.messageSource.getMessage("export.study.error", null, Locale.ENGLISH));
		}

		return super.convertObjectToJson(results);

	}

	@ResponseBody
	@RequestMapping(value = "/exportStudy/{exportType}/{instances}/{exportWayType}", method = RequestMethod.POST)
	public String exportFileStudy(@RequestBody final Map<String, String> data, @PathVariable final int exportType,
			@PathVariable final String instances, @PathVariable final int exportWayType, final HttpServletRequest req,
			final HttpServletResponse response) throws IOException {
		ExportStudyController.LOG.info("Entering Export Study:exportFileStudy");
		final List<Integer> instancesList = new ArrayList<>();
		final StringTokenizer tokenizer = new StringTokenizer(instances, "-");
		while (tokenizer.hasMoreTokens()) {
			instancesList.add(Integer.valueOf(tokenizer.nextToken()));
		}
		ExportStudyController.LOG.info("Exiting Export Study:exportFileStudy");
		return this.doExport(exportType, response, instancesList, exportWayType, data);

	}


	@ResponseBody
	@RequestMapping(value = "/studyTrial/hasFieldMap", method = RequestMethod.GET)
	public String hasTrialFieldMap(final HttpServletRequest req, final HttpServletResponse response) {
		final UserSelection userSelection = this.getUserSelection();
		userSelection.getWorkbook().getTotalNumberOfInstances();
		final Integer datasetId = userSelection.getWorkbook().getMeasurementDatesetId();
		return datasetId.toString();
	}

	/**
	 * Do export.
	 *
	 * @param exportType the export type
	 * @param response the response
	 * @return the string
	 * @throws IOException
	 */
	private String doExport(final int exportType, final HttpServletResponse response,
		final List<Integer> instances, final int exportWayType, final Map<String, String> data) throws IOException {

		/*
		 * exportWayType 1 - row column 2 - serpentine (range) 3 - serpentine (col)
		 */
		ExportStudyController.LOG.info("Entering Export Study : doExport");
		final ExportDataCollectionOrderService exportDataCollectionService = this.getExportOrderService(exportWayType);

		ExportStudyController.LOG.info("Export Study : doExport : getWorbook : start");

		final UserSelection userSelection = this.getUserSelection();

		final String studyId = this.getStudyId(data);

		final Workbook workbook = this.fieldbookMiddlewareService.getStudyDataSet(Integer.valueOf(studyId));
		this.removeAnalysisAndAnalysisSummaryVariables(workbook);
		userSelection.setWorkbook(workbook);

		// workbook.observations() collection is no longer pre-loaded into user session when trial is opened. Load now as we need it to
		// keep export functionality working.
		final boolean observationsLoaded = this.fieldbookMiddlewareService.loadAllObservations(userSelection.getWorkbook());

		LOG.info("Export Study : doExport : getWorbook : end");
		LOG.info("Export Study : doExport : processWorbook : start");

		final Map<String, Object> results = new HashMap<>();

		try {

			SettingsUtil
				.resetBreedingMethodValueToCode(this.fieldbookMiddlewareService, userSelection.getWorkbook().getObservations(), true,
					this.ontologyService, contextUtil.getCurrentProgramUUID());

			exportDataCollectionService.reorderWorkbook(userSelection.getWorkbook());

			final String studyName = FileUtils.sanitizeFileName(userSelection.getEscapedStudyName());
			FileExportInfo fileExportInfo = new FileExportInfo();
			FieldbookUtil.setColumnOrderingOnWorkbook(userSelection.getWorkbook(), data.get("columnOrders"));
			// By default the content type will be ZIP, unless only 1 instance being exported
			response.setContentType(FileUtils.MIME_ZIP);
			if (AppConstants.EXPORT_STUDY_EXCEL.getInt() == exportType) {
				final List<Integer> visibleColumns = this.getVisibleColumns(data.get("visibleColumns"));
				fileExportInfo = this.excelExportStudyService.export(userSelection.getWorkbook(), studyName, instances, visibleColumns);
				if (instances != null && instances.size() == 1) {
					response.setContentType(FileUtils.MIME_MS_EXCEL);
				}
			} else if (AppConstants.EXPORT_KSU_EXCEL.getInt() == exportType) {
				fileExportInfo = this.ksuExcelExportStudyService.export(userSelection.getWorkbook(), studyName, instances);
				response.setContentType(FileUtils.MIME_ZIP);
			} else if (AppConstants.EXPORT_KSU_CSV.getInt() == exportType) {
				fileExportInfo = this.ksuCsvExportStudyService.export(userSelection.getWorkbook(), studyName, instances);
			} else if (AppConstants.EXPORT_CSV.getInt() == exportType) {
				final List<Integer> visibleColumns = this.getVisibleColumns(data.get("visibleColumns"));
				fileExportInfo = this.csvExportStudyService.export(userSelection.getWorkbook(), studyName, instances, visibleColumns);
				if (instances != null && instances.size() == 1) {
					response.setContentType(FileUtils.MIME_CSV);
				}
			}
			results.put(ExportStudyController.IS_SUCCESS, true);
			results.put(ExportStudyController.OUTPUT_FILENAME, fileExportInfo.getFilePath());
			results.put(ExportStudyController.FILENAME, fileExportInfo.getDownloadFileName());
			results.put(ExportStudyController.CONTENT_TYPE, response.getContentType());

			SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, userSelection.getWorkbook().getObservations(), true,
				this.ontologyService, contextUtil.getCurrentProgramUUID());

			LOG.info("Export Study : doExport : processWorbook : end");

		} catch (final Exception e) {
			// generic exception handling block needs to be added here so that the calling AJAX function receives proper notification that
			// the operation was a failure
			LOG.error("Error exporting study: " + e.getMessage(), e);
			results.put(IS_SUCCESS, false);
			results.put(ERROR_MESSAGE, this.messageSource.getMessage("export.study.error", null, Locale.ENGLISH));
		} finally {
			// Important to clear out the observations collection from user session, once we are done with it to keep heap memory under
			// control. For large trials/nurseries the observations collection can be huge.
			if (observationsLoaded) {
				userSelection.getWorkbook().getObservations().clear();
			}
		}
		LOG.info("Exiting Export Study : doExport");
		return super.convertObjectToJson(results);
	}


	/**
	 * Remove variables with variable types 'Analysis' and 'Analysis Summary' in the workbook's conditions, constants, factors and variates
	 */
	protected void removeAnalysisAndAnalysisSummaryVariables(final Workbook workbook) {
		this.removeAnalysisVariables(workbook.getConditions());
		this.removeAnalysisVariables(workbook.getConstants());
		this.removeAnalysisVariables(workbook.getFactors());
		this.removeAnalysisVariables(workbook.getVariates());
	}

	/**
	 * Remove variables with variable types 'Analysis' and 'Analysis Summary' in the list of measurement variables
	 */
	private void removeAnalysisVariables(final List<MeasurementVariable> measurementVariables) {
		final Iterator<MeasurementVariable> measurementVariablesIterator = measurementVariables.iterator();
		while (measurementVariablesIterator.hasNext()) {
			final MeasurementVariable measurementVariable = measurementVariablesIterator.next();
			if (measurementVariable != null && VariableType.getReservedVariableTypes().contains(measurementVariable.getVariableType())) {
				measurementVariablesIterator.remove();
			}
		}
	}

	/***
	 * Return the list of headers's term id, otherwise null
	 *
	 * @param unparsedVisibleColumns
	 * @return
	 */
	protected List<Integer> getVisibleColumns(final String unparsedVisibleColumns) {
		List<Integer> visibleColumns = null;

		if (unparsedVisibleColumns.trim().length() != 0) {
			visibleColumns = new ArrayList<>();

			if (unparsedVisibleColumns.length() > 0) {
				final String[] ids = unparsedVisibleColumns.split(",");
				for (final String id : ids) {
					visibleColumns.add(Integer.valueOf(id));
				}
			}
		}

		return visibleColumns;
	}

	protected String getStudyId(final Map<String, String> data) {
		return data.get("studyExportId");
	}

	protected UserSelection getUserSelection() {
		return this.studySelection;
	}

	protected ExportDataCollectionOrderService getExportOrderService(final int exportWayType) {
		if (exportWayType == 1) {
			return this.exportOrderingRowColService;
		} else if (exportWayType == 2) {
			return this.exportOrderingSerpentineOverRangeService;
		} else if (exportWayType == 3) {
			return this.exportOrderingSerpentineOverColumnService;
		}
		return this.exportOrderingRowColService;
	}

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@RequestMapping(value = "/trial/instances/{studyId}", method = RequestMethod.GET)
	public String saveList(@PathVariable final int studyId, final Model model, final HttpSession session) {

		final List<ExportTrialInstanceBean> trialInstances = new ArrayList<>();
		final List<Integer> trialIds = new ArrayList<>();
		trialIds.add(studyId);
		final List<FieldMapInfo> fieldMapInfoList;

		fieldMapInfoList = this.fieldbookMiddlewareService.getFieldMapInfoOfTrial(trialIds, this.crossExpansionProperties);

		if (fieldMapInfoList != null && fieldMapInfoList.get(0).getDatasets() != null
				&& fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances() != null) {
			for (int i = 0; i < fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances().size(); i++) {
				final FieldMapTrialInstanceInfo info = fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances().get(i);
				trialInstances.add(new ExportTrialInstanceBean(info.getTrialInstanceNo(), info.getLocationName(), info.getEnvironmentId()));
			}
		}
		model.addAttribute("trialInstances", trialInstances);
		return super.showAjaxPage(model, ExportStudyController.EXPORT_TRIAL_INSTANCE);
	}

	/*
	 * Returns the advances list using the study id
	 */
	@RequestMapping(value = "/retrieve/advanced/lists/{studyId}", method = RequestMethod.GET)
	public String getAdvanceListsOfStudy(@PathVariable final int studyId, final Model model, final HttpSession session) {

		final List<GermplasmList> germplasmList = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.ADVANCED);
		model.addAttribute("advancedList", germplasmList);
		return super.showAjaxPage(model, ExportStudyController.DISPLAY_ADVANCE_GERMPLASM_LIST);
	}

	/**
	 * Do export.
	 *
	 * @param response the response
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/export/advanced/lists", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	public String doAdvanceExport(final HttpServletResponse response, final HttpServletRequest req) {

		final String advancedListIds = req.getParameter("exportAdvanceListGermplasmIds");
		final String exportType = req.getParameter("exportAdvanceListGermplasmType");

		final UserSelection userSelection = this.getUserSelection();
		final StudyDetails studyDetails = userSelection.getWorkbook().getStudyDetails();

		final FileExportInfo exportInfo = this.exportAdvanceListItems(exportType, advancedListIds, studyDetails);
		final String outputFilename = exportInfo.getFilePath();
		final int extensionIndex = outputFilename.lastIndexOf('.');
		final String extensionName = outputFilename.substring(extensionIndex, outputFilename.length());
		String contentType = "";
		if (extensionName.indexOf(AppConstants.ZIP_FILE_SUFFIX.getString()) != -1) {
			contentType = FileUtils.MIME_ZIP;
		} else if (extensionName.indexOf(AppConstants.EXPORT_CSV_SUFFIX.getString()) != -1) {
			contentType = FileUtils.MIME_CSV;
		} else if (extensionName.indexOf(AppConstants.EXPORT_XLS_SUFFIX.getString()) != -1) {
			contentType = FileUtils.MIME_MS_EXCEL;
		}
		response.setContentType(contentType);

		final Map<String, Object> results = new HashMap<>();
		results.put(ExportStudyController.OUTPUT_FILENAME, outputFilename);
		results.put(ExportStudyController.FILENAME, SettingsUtil.cleanSheetAndFileName(exportInfo.getDownloadFileName()));
		results.put(ExportStudyController.CONTENT_TYPE, contentType);

		return super.convertObjectToJson(results);
	}

	@ResponseBody
	@RequestMapping(value = "/custom/{studyId}/reports", method = RequestMethod.GET)
	public List<CustomReportType> getCustomReports(@PathVariable final int studyId) {
		final StudyDetails studyDetails = studyDataManager.getStudyDetails(studyId);
		// DO NOT remove this condition. Reports are organized based on the study type
		// It needs to be discussed with IBP whenever they want to bring custom reports back
		if (StudyTypeDto.NURSERY_NAME.equalsIgnoreCase(studyDetails.getStudyType().getName())) {
			return this.getCustomReportTypes(ToolSection.FB_NURSE_MGR_CUSTOM_REPORT.name());
		} else if (StudyTypeDto.TRIAL_NAME.equalsIgnoreCase(studyDetails.getStudyType().getName())) {
			return this.getCustomReportTypes(ToolSection.FB_TRIAL_MGR_CUSTOM_REPORT.name());
		}

		return new ArrayList<>();
	}

	public List<CustomReportType> getCustomReportTypes(final String name) {
		return this.jasperReportService.getCustomReportTypes(name, ToolName.FIELDBOOK_WEB.getName());
	}

	protected FileExportInfo exportAdvanceListItems(final String exportType, final String advancedListIds, final StudyDetails studyDetails) {
		if (AppConstants.EXPORT_ADVANCE_STUDY_EXCEL.getString().equalsIgnoreCase(exportType)
				|| AppConstants.EXPORT_ADVANCE_STUDY_CSV.getString().equalsIgnoreCase(exportType)) {
			return this.exportAdvanceListService.exportAdvanceGermplasmList(advancedListIds, studyDetails.getStudyName(),
					this.germplasmExportService, exportType);
		}
		return new FileExportInfo();
	}

	/**
	 * Do export.
	 *
	 * @param response the response
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/export/stock/lists", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	public String doExportStockList(final HttpServletResponse response, final HttpServletRequest req) {

		final String stockIds = req.getParameter("exportStockListId");

		final FileExportInfo exportInfo = this.exportAdvanceListService.exportStockList(Integer.valueOf(stockIds), this.germplasmExportService);
		final String outputFilename = exportInfo.getFilePath();
		final String contentType = FileUtils.MIME_MS_EXCEL;
		response.setContentType(contentType);
		final Map<String, Object> results = new HashMap<>();
		results.put(ExportStudyController.OUTPUT_FILENAME, outputFilename);
		results.put(ExportStudyController.FILENAME, SettingsUtil.cleanSheetAndFileName(exportInfo.getDownloadFileName()));
		results.put(ExportStudyController.CONTENT_TYPE, contentType);

		return super.convertObjectToJson(results);
	}

	protected void setExportAdvanceListService(final ExportAdvanceListService exportAdvanceListService) {
		this.exportAdvanceListService = exportAdvanceListService;
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.studySelection = userSelection;
	}

	public void setExportOrderingRowColService(final ExportOrderingRowColImpl exportOrderingRowColService) {
		this.exportOrderingRowColService = exportOrderingRowColService;
	}

	public void setExportOrderingSerpentineOverRangeService(
			final ExportOrderingSerpentineOverRangeImpl exportOrderingSerpentineOverRangeService) {
		this.exportOrderingSerpentineOverRangeService = exportOrderingSerpentineOverRangeService;
	}

	public void setExportOrderingSerpentineOverColumnService(
			final ExportOrderingSerpentineOverColImpl exportOrderingSerpentineOverColumnService) {
		this.exportOrderingSerpentineOverColumnService = exportOrderingSerpentineOverColumnService;
	}

	protected void setCsvExportStudyService(final CsvExportStudyService csvExportStudyService) {
		this.csvExportStudyService = csvExportStudyService;
	}

	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected void setOntologyService(final OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}


	public void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}
}
