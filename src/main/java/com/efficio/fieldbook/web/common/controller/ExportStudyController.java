
package com.efficio.fieldbook.web.common.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRException;

import org.generationcp.commons.constant.ToolEnum;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.reports.service.JasperReportService;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.reports.BuildReportException;
import org.generationcp.middleware.reports.Reporter;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.ReportService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.CsvExportStudyService;
import com.efficio.fieldbook.web.common.service.DataKaptureExportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.common.service.ExportAdvanceListService;
import com.efficio.fieldbook.web.common.service.ExportDataCollectionOrderService;
import com.efficio.fieldbook.web.common.service.FieldroidExportStudyService;
import com.efficio.fieldbook.web.common.service.KsuCsvExportStudyService;
import com.efficio.fieldbook.web.common.service.KsuExcelExportStudyService;
import com.efficio.fieldbook.web.common.service.RExportStudyService;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingRowColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverRangeImpl;
import com.efficio.fieldbook.web.trial.bean.ExportTrialInstanceBean;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Controller
@RequestMapping(ExportStudyController.URL)
public class ExportStudyController extends AbstractBaseFieldbookController {

	private static final String CONTENT_TYPE = "contentType";
	private static final String FILENAME = "filename";
	private static final String OUTPUT_FILENAME = "outputFilename";
	private static final String ERROR_MESSAGE = "errorMessage";
	static final String IS_SUCCESS = "isSuccess";
	private static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
	private static final String CSV_CONTENT_TYPE = "text/csv";
	private static final Logger LOG = LoggerFactory.getLogger(ExportStudyController.class);
	public static final String URL = "/ExportManager";
	private static final int BUFFER_SIZE = 4096 * 4;
	private static String EXPORT_TRIAL_INSTANCE = "Common/includes/exportTrialInstance";
	private static String DISPLAY_ADVANCE_GERMPLASM_LIST = "Common/includes/displayListOfAdvanceGermplasmList";

	@Resource
	private UserSelection studySelection;

	@Resource
	private FieldroidExportStudyService fielddroidExportStudyService;

	@Resource
	private RExportStudyService rExportStudyService;

	@Resource
	private ExcelExportStudyService excelExportStudyService;

	@Resource
	private CsvExportStudyService csvExportStudyService;

	@Resource
	private DataKaptureExportStudyService dataKaptureExportStudyService;

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
	private WorkbenchService workbenchService;

	@Resource
	private ReportService reportService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private GermplasmExportService germplasmExportService;

	@Resource
	private JasperReportService jasperReportService;
	
	@Resource
	private ContextUtil contextUtil;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/download/file", method = RequestMethod.GET)
	public String downloadFile(final HttpServletRequest req, final HttpServletResponse response) throws UnsupportedEncodingException {

		final String outputFilename = new String(req.getParameter(OUTPUT_FILENAME).getBytes("iso-8859-1"), "UTF-8");
		final String filename = new String(req.getParameter(FILENAME).getBytes("iso-8859-1"), "UTF-8");

		// the selected name + current date
		final File xls = new File(outputFilename);
		FileInputStream in;

		FieldbookUtil.resolveContentDisposition(filename, response, req.getHeader("User-Agent"));

		response.setContentType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename));
		response.setCharacterEncoding("UTF-8");

		try {
			in = new FileInputStream(xls);
			final OutputStream out = response.getOutputStream();

			// use bigger if you want
			final byte[] buffer = new byte[ExportStudyController.BUFFER_SIZE];
			int length = 0;

			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();

		} catch (final FileNotFoundException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		} catch (final IOException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		}

		return "";

	}

	@ResponseBody
	@RequestMapping(value = "/export/{exportType}/{selectedTraitTermId}/{exportWayType}", method = RequestMethod.POST)
	public String exportRFileForNursery(@RequestBody final Map<String, String> data, @PathVariable final int exportType,
			@PathVariable final int selectedTraitTermId, @PathVariable final int exportWayType, final HttpServletRequest req,
			final HttpServletResponse response) throws IOException {
		final boolean isTrial = false;
		final List<Integer> instancesList = new ArrayList<Integer>();
		instancesList.add(1);
		return this.doExport(exportType, selectedTraitTermId, response, isTrial, instancesList, exportWayType, data);

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
		Reporter rep;
		final Map<String, Object> results = new HashMap<String, Object>();
		try {

			rep =
					this.reportService.getStreamReport(reportCode, Integer.parseInt(studyId), this.contextUtil.getProjectInContext()
							.getProjectName(), baos);

			fileName = rep.getFileName();
			outputFilename = this.fieldbookProperties.getUploadDirectory() + File.separator + fileName;

			final File reportFile = new File(outputFilename);
			baos.writeTo(new FileOutputStream(reportFile));

			results.put(IS_SUCCESS, true);
			results.put(OUTPUT_FILENAME, outputFilename);
			results.put(FILENAME, SettingsUtil.cleanSheetAndFileName(fileName));
			results.put(CONTENT_TYPE, response.getContentType());

		} catch (NumberFormatException | JRException | IOException | BuildReportException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
			results.put(IS_SUCCESS, false);
			results.put(ERROR_MESSAGE, this.messageSource.getMessage("export.study.error", null, Locale.ENGLISH));
		}

		return super.convertObjectToJson(results);

	}

	@ResponseBody
	@RequestMapping(value = "/export/{exportType}/{exportWayType}", method = RequestMethod.POST)
	public String exportFile(@RequestBody final Map<String, String> data, @PathVariable final int exportType,
			@PathVariable final int exportWayType, final HttpServletRequest req, final HttpServletResponse response) throws IOException {
		
		String result = "";
		final Monitor monitor = MonitorFactory.start("ExportStudy:com.efficio.fieldbook.web.common.controller.ExportStudyController.exportFile");
		try{
		final boolean isTrial = false;
		final List<Integer> instancesList = new ArrayList<Integer>();
		instancesList.add(1);		
		result = this.doExport(exportType, 0, response, isTrial, instancesList, exportWayType, data);
		} finally {
		  monitor.stop();
		}
		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/exportTrial/{exportType}/{selectedTraitTermId}/{instances}/{exportWayType}", method = RequestMethod.POST)
	public String exportRFileForTrial(@RequestBody final Map<String, String> data, @PathVariable final int exportType,
			@PathVariable final int selectedTraitTermId, @PathVariable final String instances, @PathVariable final int exportWayType,
			final HttpServletRequest req, final HttpServletResponse response) throws IOException {
		final boolean isTrial = true;
		final List<Integer> instancesList = new ArrayList<Integer>();
		final StringTokenizer tokenizer = new StringTokenizer(instances, "|");
		while (tokenizer.hasMoreTokens()) {
			instancesList.add(Integer.valueOf(tokenizer.nextToken()));
		}
		return this.doExport(exportType, selectedTraitTermId, response, isTrial, instancesList, exportWayType, data);
	}

	@ResponseBody
	@RequestMapping(value = "/exportTrial/{exportType}/{instances}/{exportWayType}", method = RequestMethod.POST)
	public String exportFileTrial(@RequestBody final Map<String, String> data, @PathVariable final int exportType,
			@PathVariable final String instances, @PathVariable final int exportWayType, final HttpServletRequest req,
			final HttpServletResponse response) throws IOException {
		String result = "";
		Monitor monitor = MonitorFactory.start("ExportStudy:com.efficio.fieldbook.web.common.controller.ExportStudyController.exportFileTrial");
		try{
  		final boolean isTrial = true;
  		final List<Integer> instancesList = new ArrayList<Integer>();
  		final StringTokenizer tokenizer = new StringTokenizer(instances, "|");
  		while (tokenizer.hasMoreTokens()) {
  			instancesList.add(Integer.valueOf(tokenizer.nextToken()));
  		}
  		result = this.doExport(exportType, 0, response, isTrial, instancesList, exportWayType, data);
		}finally {
			monitor.stop();		  
		}
		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/study/hasFieldMap", method = RequestMethod.GET)
	public String hasFieldMap(final HttpServletRequest req, final HttpServletResponse response) {
		String studyId = req.getParameter("studyId");
		final UserSelection userSelection = this.getUserSelection();
		boolean hasFieldMap = false;

		Workbook workbook = null;
		if ("0".equalsIgnoreCase(studyId)) {

			workbook = userSelection.getWorkbook();
			studyId = workbook.getStudyDetails().getId().toString();
		} else {
			// meaning for the session
			workbook = this.getPaginationListSelection().getReviewWorkbook(studyId);
		}
		hasFieldMap = this.fieldbookMiddlewareService.checkIfStudyHasFieldmap(Integer.valueOf(studyId));

		return hasFieldMap ? "1" : "0";
	}

	@ResponseBody
	@RequestMapping(value = "/studyTrial/hasFieldMap", method = RequestMethod.GET)
	public String hasTrialFieldMap(final HttpServletRequest req, final HttpServletResponse response) {
		final UserSelection userSelection = this.getUserSelection();
		userSelection.getWorkbook().getTotalNumberOfInstances();
		final Integer datasetId = userSelection.getWorkbook().getMeasurementDatesetId();
		return datasetId.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/study/traits", method = RequestMethod.GET)
	public String getStudyTraits(final HttpServletRequest req, final HttpServletResponse response) {
		final String studyId = req.getParameter("studyId");

		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
		try {
			List<MeasurementVariable> tempVariates = new ArrayList<MeasurementVariable>();
			if ("0".equalsIgnoreCase(studyId)) {

				tempVariates = userSelection.getWorkbook().getMeasurementDatasetVariables();

			} else {
				// meaning for the session
				final Workbook workbook = this.getPaginationListSelection().getReviewWorkbook(studyId);
				tempVariates = workbook.getVariates();
			}

			for (final MeasurementVariable var : tempVariates) {
				if (var.isFactor() == false) {
					variates.add(var);
				}
			}

		} catch (final Exception e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		}
		return super.convertObjectToJson(variates);
	}

	/**
	 * Do export.
	 * 
	 * @param exportType the export type
	 * @param selectedTraitTermId the selected trait term id
	 * @param response the response
	 * @return the string
	 * @throws IOException
	 */
	private String doExport(final int exportType, final int selectedTraitTermId, final HttpServletResponse response, final boolean isTrial,
			final List<Integer> instances, final int exportWayType, final Map<String, String> data) throws IOException {

		/*
		 * exportWayType 1 - row column 2 - serpentine (range) 3 - serpentine (col)
		 */

		Monitor monitor = MonitorFactory.start("ExportStudy: getWorkbook : com.efficio.fieldbook.web.common.controller.ExportStudyController.exportFileTrial");
		
		final ExportDataCollectionOrderService exportDataCollectionService = this.getExportOrderService(exportWayType);

		final UserSelection userSelection = this.getUserSelection();
		try {
			final String studyId = this.getStudyId(data);
			if (!"0".equalsIgnoreCase(studyId)) {
				// we need to get the workbook and set it in the userSelectionObject
				Workbook workbookSession = null;

				if (this.getPaginationListSelection().getReviewFullWorkbook(studyId) == null) {
					if (isTrial) {
						workbookSession = this.fieldbookMiddlewareService.getTrialDataSet(Integer.valueOf(studyId));
					} else {
						workbookSession = this.fieldbookMiddlewareService.getNurseryDataSet(Integer.valueOf(studyId));
					}
					SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbookSession.getObservations(), false,
							this.ontologyService, contextUtil.getCurrentProgramUUID());

					this.getPaginationListSelection().addReviewFullWorkbook(studyId, workbookSession);
				} else {
					workbookSession = this.getPaginationListSelection().getReviewFullWorkbook(studyId);
				}

				userSelection.setWorkbook(workbookSession);
			}
		} catch (final NumberFormatException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		} finally {
			monitor.stop();
		}

		monitor = MonitorFactory.start("ExportStudy: processWorkbook : com.efficio.fieldbook.web.common.controller.ExportStudyController.exportFileTrial");

		final Map<String, Object> results = new HashMap<>();
		try {
			
			final String breedingMethodPropertyName = this.ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getTerm().getName();		
			
			excelExportStudyService.setBreeedingMethodPropertyName(breedingMethodPropertyName);
			
			final Workbook workbook = userSelection.getWorkbook();

			SettingsUtil.resetBreedingMethodValueToCode(this.fieldbookMiddlewareService, workbook.getObservations(), true,
					this.ontologyService, contextUtil.getCurrentProgramUUID());

			exportDataCollectionService.reorderWorkbook(workbook);

			String filename = FileUtils.sanitizeFileName(userSelection.getEscapedStudyName());
			String outputFilename = null;
			FieldbookUtil.setColumnOrderingOnWorkbook(workbook, data.get("columnOrders"));
			if (AppConstants.EXPORT_NURSERY_FIELDLOG_FIELDROID.getInt() == exportType) {
				filename = filename + AppConstants.EXPORT_FIELDLOG_SUFFIX.getString();
				outputFilename = this.fielddroidExportStudyService.export(userSelection.getWorkbook(), filename, instances);
				response.setContentType(ExportStudyController.CSV_CONTENT_TYPE);
			} else if (AppConstants.EXPORT_NURSERY_R.getInt() == exportType) {
				filename = filename + AppConstants.EXPORT_R_SUFFIX.getString();
				outputFilename = this.rExportStudyService.exportToR(userSelection.getWorkbook(), filename, selectedTraitTermId, instances);
				response.setContentType(ExportStudyController.CSV_CONTENT_TYPE);
			} else if (AppConstants.EXPORT_NURSERY_EXCEL.getInt() == exportType) {
				final List<Integer> visibleColumns = this.getVisibleColumns(data.get("visibleColumns"));
				filename = filename + AppConstants.EXPORT_XLS_SUFFIX.getString();
				outputFilename = this.excelExportStudyService.export(userSelection.getWorkbook(), filename, instances, visibleColumns);
				if (instances != null && instances.size() > 1) {
					final int extensionIndex = filename.lastIndexOf(".");
					filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
					response.setContentType("application/zip");
				} else {
					filename = this.getOutputFileName(workbook.isNursery(), outputFilename, filename);
					response.setContentType(ExportStudyController.APPLICATION_VND_MS_EXCEL);
				}
			} else if (AppConstants.EXPORT_DATAKAPTURE.getInt() == exportType) {
				outputFilename = this.dataKaptureExportStudyService.export(userSelection.getWorkbook(), filename, instances);
				response.setContentType("application/zip");
				filename = filename + AppConstants.ZIP_FILE_SUFFIX.getString();
			} else if (AppConstants.EXPORT_KSU_EXCEL.getInt() == exportType) {
				filename = filename + AppConstants.EXPORT_XLS_SUFFIX.getString();
				outputFilename = this.ksuExcelExportStudyService.export(userSelection.getWorkbook(), filename, instances);
				final int extensionIndex = filename.lastIndexOf(".");
				filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
				response.setContentType("application/zip");
			} else if (AppConstants.EXPORT_KSU_CSV.getInt() == exportType) {
				filename = filename + AppConstants.EXPORT_CSV_SUFFIX.getString();
				outputFilename = this.ksuCsvExportStudyService.export(userSelection.getWorkbook(), filename, instances);
				final int extensionIndex = filename.lastIndexOf(".");
				filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
				response.setContentType("application/zip");
			} else if (AppConstants.EXPORT_CSV.getInt() == exportType) {
				final List<Integer> visibleColumns = this.getVisibleColumns(data.get("visibleColumns"));
				filename = filename + AppConstants.EXPORT_CSV_SUFFIX.getString();
				outputFilename = this.csvExportStudyService.export(userSelection.getWorkbook(), filename, instances, visibleColumns);
				if (instances != null && instances.size() > 1) {
					final int extensionIndex = filename.lastIndexOf(".");
					filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
					response.setContentType("application/zip");
				} else {
					filename = this.getOutputFileName(workbook.isNursery(), outputFilename, filename);
					response.setContentType(ExportStudyController.CSV_CONTENT_TYPE);
				}
			}

			results.put(IS_SUCCESS, true);
			results.put(OUTPUT_FILENAME, outputFilename);
			results.put(FILENAME, filename);
			results.put(CONTENT_TYPE, response.getContentType());

			SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), true,
					this.ontologyService, contextUtil.getCurrentProgramUUID());
			
		} catch (final Exception e) {
			// generic exception handling block needs to be added here so that the calling AJAX function receives proper notification that
			// the operation was a failure
			results.put(IS_SUCCESS, false);
			results.put(ERROR_MESSAGE, this.messageSource.getMessage("export.study.error", null, Locale.ENGLISH));
		} finally {
			monitor.stop();
		}

		return super.convertObjectToJson(results);
	}

	/***
	 * Return the list of headers's term id, otherwise null
	 * 
	 * @param data
	 * @return
	 */
	protected List<Integer> getVisibleColumns(final String unparsedVisibleColumns) {
		List<Integer> visibleColumns = null;

		if (unparsedVisibleColumns.trim().length() != 0) {
			visibleColumns = new ArrayList<Integer>();

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

	protected String getOutputFileName(final boolean isNursery, final String outputFilename, final String filename) {
		if (!isNursery) {
			return outputFilename;
		}
		return filename;
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

		final List<ExportTrialInstanceBean> trialInstances = new ArrayList<ExportTrialInstanceBean>();

		final List<Integer> trialIds = new ArrayList<Integer>();
		trialIds.add(studyId);
		List<FieldMapInfo> fieldMapInfoList = new ArrayList<FieldMapInfo>();

		fieldMapInfoList = this.fieldbookMiddlewareService.getFieldMapInfoOfTrial(trialIds, this.crossExpansionProperties);

		if (fieldMapInfoList != null && fieldMapInfoList.get(0).getDatasets() != null
				&& fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances() != null) {
			for (int i = 0; i < fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances().size(); i++) {
				final FieldMapTrialInstanceInfo info = fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances().get(i);
				trialInstances.add(new ExportTrialInstanceBean(info.getTrialInstanceNo(), info.getHasFieldMap()));
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

		List<GermplasmList> germplasmList = new ArrayList<GermplasmList>();
		germplasmList = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(studyId), GermplasmListType.ADVANCED);
		model.addAttribute("advancedList", germplasmList);
		return super.showAjaxPage(model, ExportStudyController.DISPLAY_ADVANCE_GERMPLASM_LIST);
	}

	/**
	 * Do export.
	 * 
	 * @param exportType the export type
	 * @param selectedTraitTermId the selected trait term id
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

		String outputFilename = null;
		final File file = this.exportAdvanceListItems(exportType, advancedListIds, studyDetails);

		outputFilename = file.getAbsolutePath();
		final int extensionIndex = outputFilename.lastIndexOf(".");
		final String extensionName = outputFilename.substring(extensionIndex, outputFilename.length());
		String contentType = "";
		if (extensionName.indexOf(AppConstants.ZIP_FILE_SUFFIX.getString()) != -1) {
			contentType = "application/zip";
		} else if (extensionName.indexOf(AppConstants.EXPORT_CSV_SUFFIX.getString()) != -1) {
			contentType = "text/csv";
		} else if (extensionName.indexOf(AppConstants.EXPORT_XLS_SUFFIX.getString()) != -1) {
			contentType = ExportStudyController.APPLICATION_VND_MS_EXCEL;
		}
		response.setContentType(contentType);

		final Map<String, Object> results = new HashMap<String, Object>();
		results.put(OUTPUT_FILENAME, outputFilename);
		results.put(FILENAME, SettingsUtil.cleanSheetAndFileName(file.getName()));
		results.put(CONTENT_TYPE, contentType);

		return super.convertObjectToJson(results);
	}

	@ResponseBody
	@RequestMapping(value = "/custom/nursery/reports", method = RequestMethod.GET)
	public List<CustomReportType> getCustomNurseryReports() {
		return this.getCustomReportTypes(ToolSection.FB_NURSE_MGR_CUSTOM_REPORT.name());
	}

	@ResponseBody
	@RequestMapping(value = "/custom/trial/reports", method = RequestMethod.GET)
	public List<CustomReportType> getCustomTrialReports() {
		return this.getCustomReportTypes(ToolSection.FB_TRIAL_MGR_CUSTOM_REPORT.name());
	}

	public List<CustomReportType> getCustomReportTypes(final String name) {
		return this.jasperReportService.getCustomReportTypes(name, ToolEnum.FIELDBOOK_WEB.getToolName());
	}

	protected File exportAdvanceListItems(final String exportType, final String advancedListIds, final StudyDetails studyDetails) {
		if (AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString().equalsIgnoreCase(exportType)
				|| AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString().equalsIgnoreCase(exportType)) {
			return this.exportAdvanceListService.exportAdvanceGermplasmList(advancedListIds, studyDetails.getStudyName(),
					this.germplasmExportService, exportType);
		}
		return null;
	}

	/**
	 * Do export.
	 * 
	 * @param exportType the export type
	 * @param selectedTraitTermId the selected trait term id
	 * @param response the response
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/export/stock/lists", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	public String doExportStockList(final HttpServletResponse response, final HttpServletRequest req) {

		final String stockIds = req.getParameter("exportStockListId");

		String outputFilename = null;

		final File file = this.exportAdvanceListService.exportStockList(Integer.valueOf(stockIds), this.germplasmExportService);

		outputFilename = file.getAbsolutePath();
		final String contentType = ExportStudyController.APPLICATION_VND_MS_EXCEL;
		response.setContentType(contentType);
		final Map<String, Object> results = new HashMap<String, Object>();
		results.put(OUTPUT_FILENAME, outputFilename);
		results.put(FILENAME, SettingsUtil.cleanSheetAndFileName(file.getName()));
		results.put(CONTENT_TYPE, contentType);

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

	public void setWorkbenchService(final WorkbenchService workbenchService) {
		this.workbenchService = workbenchService;
	}

	public void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

	@Override
	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

}
