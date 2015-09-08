
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
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRException;

import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.service.ExportService;
import org.generationcp.commons.service.impl.ExportServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.CustomReportTypeUtil;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.reports.BuildReportException;
import org.generationcp.middleware.reports.Reporter;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.ReportService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

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

@Controller
@RequestMapping(ExportStudyController.URL)
public class ExportStudyController extends AbstractBaseFieldbookController {

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
	private ContextUtil contextUtil;
	@Resource
	private ReportService reportService;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/download/file", method = RequestMethod.GET)
	public String downloadFile(HttpServletRequest req, HttpServletResponse response) throws UnsupportedEncodingException {

		String outputFilename = new String(req.getParameter("outputFilename").getBytes("iso-8859-1"), "UTF-8");
		String filename = new String(req.getParameter("filename").getBytes("iso-8859-1"), "UTF-8");
		String contentType = req.getParameter("contentType");

		// the selected name + current date
		File xls = new File(outputFilename);
		FileInputStream in;

		response.setHeader("Content-disposition",
				"attachment; filename=" + FieldbookUtil.getDownloadFileName(SettingsUtil.cleanSheetAndFileName(filename), req));
		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");
		try {
			in = new FileInputStream(xls);
			OutputStream out = response.getOutputStream();

			// use bigger if you want
			byte[] buffer = new byte[ExportStudyController.BUFFER_SIZE];
			int length = 0;

			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();

		} catch (FileNotFoundException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		} catch (IOException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		}

		return "";

	}

	@ResponseBody
	@RequestMapping(value = "/export/{exportType}/{selectedTraitTermId}/{exportWayType}", method = RequestMethod.POST)
	public String exportRFileForNursery(@RequestBody Map<String, String> data, @PathVariable int exportType,
			@PathVariable int selectedTraitTermId, @PathVariable int exportWayType, HttpServletRequest req, HttpServletResponse response)
			throws MiddlewareException {
		boolean isTrial = false;
		List<Integer> instancesList = new ArrayList<Integer>();
		instancesList.add(1);
		return this.doExport(exportType, selectedTraitTermId, response, isTrial, instancesList, exportWayType, data);

	}

	@ResponseBody
	@RequestMapping(value = "/export/custom/report", method = RequestMethod.POST)
	public String exportCustomReport(@RequestBody Map<String, String> data, HttpServletRequest req, HttpServletResponse response)
			throws MiddlewareQueryException {
		String studyId = this.getStudyId(data);
		String reportCode = data.get("customReportCode");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "";
		String outputFilename = "";
		Reporter rep;
		try {
			rep = this.reportService.getStreamReport(reportCode, Integer.parseInt(studyId), baos);

			fileName = rep.getFileName();
			outputFilename = this.fieldbookProperties.getUploadDirectory() + File.separator + fileName;

			File reportFile = new File(outputFilename);
			baos.writeTo(new FileOutputStream(reportFile));

		} catch (NumberFormatException | MiddlewareException | JRException | IOException | BuildReportException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		}
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("outputFilename", outputFilename);
		results.put("filename", SettingsUtil.cleanSheetAndFileName(fileName));
		results.put("contentType", response.getContentType());
		return super.convertObjectToJson(results);

	}

	@ResponseBody
	@RequestMapping(value = "/export/{exportType}/{exportWayType}", method = RequestMethod.POST)
	public String exportFile(@RequestBody Map<String, String> data, @PathVariable int exportType, @PathVariable int exportWayType,
			HttpServletRequest req, HttpServletResponse response) throws MiddlewareException {
		boolean isTrial = false;
		List<Integer> instancesList = new ArrayList<Integer>();
		instancesList.add(1);
		return this.doExport(exportType, 0, response, isTrial, instancesList, exportWayType, data);
	}

	@ResponseBody
	@RequestMapping(value = "/exportTrial/{exportType}/{selectedTraitTermId}/{instances}/{exportWayType}", method = RequestMethod.POST)
	public String exportRFileForTrial(@RequestBody Map<String, String> data, @PathVariable int exportType,
			@PathVariable int selectedTraitTermId, @PathVariable String instances, @PathVariable int exportWayType, HttpServletRequest req,
			HttpServletResponse response) throws MiddlewareException {
		boolean isTrial = true;
		List<Integer> instancesList = new ArrayList<Integer>();
		StringTokenizer tokenizer = new StringTokenizer(instances, "|");
		while (tokenizer.hasMoreTokens()) {
			instancesList.add(Integer.valueOf(tokenizer.nextToken()));
		}
		return this.doExport(exportType, selectedTraitTermId, response, isTrial, instancesList, exportWayType, data);

	}

	@ResponseBody
	@RequestMapping(value = "/exportTrial/{exportType}/{instances}/{exportWayType}", method = RequestMethod.POST)
	public String exportFileTrial(@RequestBody Map<String, String> data, @PathVariable int exportType, @PathVariable String instances,
			@PathVariable int exportWayType, HttpServletRequest req, HttpServletResponse response) throws MiddlewareException {
		boolean isTrial = true;
		List<Integer> instancesList = new ArrayList<Integer>();
		StringTokenizer tokenizer = new StringTokenizer(instances, "|");
		while (tokenizer.hasMoreTokens()) {
			instancesList.add(Integer.valueOf(tokenizer.nextToken()));
		}
		return this.doExport(exportType, 0, response, isTrial, instancesList, exportWayType, data);

	}

	@ResponseBody
	@RequestMapping(value = "/study/hasFieldMap", method = RequestMethod.GET)
	public String hasFieldMap(HttpServletRequest req, HttpServletResponse response) {
		String studyId = req.getParameter("studyId");
		UserSelection userSelection = this.getUserSelection();
		boolean hasFieldMap = false;
		try {
			Workbook workbook = null;
			if ("0".equalsIgnoreCase(studyId)) {

				workbook = userSelection.getWorkbook();
				studyId = workbook.getStudyDetails().getId().toString();
			} else {
				// meaning for the session
				workbook = this.getPaginationListSelection().getReviewWorkbook(studyId);
			}
			hasFieldMap = this.fieldbookMiddlewareService.checkIfStudyHasFieldmap(Integer.valueOf(studyId));
		} catch (MiddlewareQueryException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		}
		return hasFieldMap ? "1" : "0";
	}

	@ResponseBody
	@RequestMapping(value = "/studyTrial/hasFieldMap", method = RequestMethod.GET)
	public String hasTrialFieldMap(HttpServletRequest req, HttpServletResponse response) {
		UserSelection userSelection = this.getUserSelection();
		userSelection.getWorkbook().getTotalNumberOfInstances();
		Integer datasetId = userSelection.getWorkbook().getMeasurementDatesetId();
		return datasetId.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/study/traits", method = RequestMethod.GET)
	public String getStudyTraits(HttpServletRequest req, HttpServletResponse response) {
		String studyId = req.getParameter("studyId");

		UserSelection userSelection = this.getUserSelection();
		List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
		try {
			List<MeasurementVariable> tempVariates = new ArrayList<MeasurementVariable>();
			if ("0".equalsIgnoreCase(studyId)) {

				tempVariates = userSelection.getWorkbook().getMeasurementDatasetVariables();

			} else {
				// meaning for the session
				Workbook workbook = this.getPaginationListSelection().getReviewWorkbook(studyId);
				tempVariates = workbook.getVariates();
			}

			for (MeasurementVariable var : tempVariates) {
				if (var.isFactor() == false) {
					variates.add(var);
				}
			}

		} catch (Exception e) {
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
	 */
	private String doExport(int exportType, int selectedTraitTermId, HttpServletResponse response, boolean isTrial,
			List<Integer> instances, int exportWayType, Map<String, String> data) throws MiddlewareException {

		/*
		 * exportWayType 1 - row column 2 - serpentine (range) 3 - serpentine (col)
		 */
		ExportDataCollectionOrderService exportDataCollectionService = this.getExportOrderService(exportWayType);

		UserSelection userSelection = this.getUserSelection();
		try {
			String studyId = this.getStudyId(data);
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
							this.ontologyService);

					this.getPaginationListSelection().addReviewFullWorkbook(studyId, workbookSession);
				} else {
					workbookSession = this.getPaginationListSelection().getReviewFullWorkbook(studyId);
				}

				userSelection.setWorkbook(workbookSession);
			}
		} catch (NumberFormatException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		} catch (MiddlewareQueryException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		}

		Workbook workbook = userSelection.getWorkbook();

		SettingsUtil
				.resetBreedingMethodValueToCode(this.fieldbookMiddlewareService, workbook.getObservations(), true, this.ontologyService);

		exportDataCollectionService.reorderWorkbook(workbook);

		String filename = this.getFileName(userSelection);
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
			List<Integer> visibleColumns = this.getVisibleColumns(data.get("visibleColumns"));
			filename = filename + AppConstants.EXPORT_XLS_SUFFIX.getString();
			outputFilename = this.excelExportStudyService.export(userSelection.getWorkbook(), filename, instances, visibleColumns);
			if (instances != null && instances.size() > 1) {
				int extensionIndex = filename.lastIndexOf(".");
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
			int extensionIndex = filename.lastIndexOf(".");
			filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
			response.setContentType("application/zip");
		} else if (AppConstants.EXPORT_KSU_CSV.getInt() == exportType) {
			filename = filename + AppConstants.EXPORT_CSV_SUFFIX.getString();
			outputFilename = this.ksuCsvExportStudyService.export(userSelection.getWorkbook(), filename, instances);
			int extensionIndex = filename.lastIndexOf(".");
			filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
			response.setContentType("application/zip");
		} else if (AppConstants.EXPORT_CSV.getInt() == exportType) {
			List<Integer> visibleColumns = this.getVisibleColumns(data.get("visibleColumns"));
			filename = filename + AppConstants.EXPORT_CSV_SUFFIX.getString();
			outputFilename = this.csvExportStudyService.export(userSelection.getWorkbook(), filename, instances, visibleColumns);
			if (instances != null && instances.size() > 1) {
				int extensionIndex = filename.lastIndexOf(".");
				filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
				response.setContentType("application/zip");
			} else {
				filename = this.getOutputFileName(workbook.isNursery(), outputFilename, filename);
				response.setContentType(ExportStudyController.CSV_CONTENT_TYPE);
			}
		}
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("outputFilename", outputFilename);
		results.put("filename", SettingsUtil.cleanSheetAndFileName(filename));
		results.put("contentType", response.getContentType());

		SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), true, this.ontologyService);

		return super.convertObjectToJson(results);
	}

	protected String getFileName(UserSelection userSelection) {
		return HtmlUtils.htmlUnescape(userSelection.getWorkbook().getStudyDetails().getStudyName());
	}

	/***
	 * Return the list of headers's term id, otherwise null
	 * 
	 * @param data
	 * @return
	 */
	protected List<Integer> getVisibleColumns(String unparsedVisibleColumns) {
		List<Integer> visibleColumns = null;

		if (unparsedVisibleColumns.trim().length() != 0) {
			visibleColumns = new ArrayList<Integer>();

			if (unparsedVisibleColumns.length() > 0) {
				String[] ids = unparsedVisibleColumns.split(",");
				for (String id : ids) {
					visibleColumns.add(Integer.valueOf(id));
				}
			}
		}

		return visibleColumns;
	}

	protected String getStudyId(Map<String, String> data) {
		return data.get("studyExportId");
	}

	protected String getOutputFileName(boolean isNursery, String outputFilename, String filename) {
		if (!isNursery) {
			return outputFilename;
		}
		return filename;
	}

	protected UserSelection getUserSelection() {
		return this.studySelection;
	}

	protected ExportDataCollectionOrderService getExportOrderService(int exportWayType) {
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
	public String saveList(@PathVariable int studyId, Model model, HttpSession session) {

		List<ExportTrialInstanceBean> trialInstances = new ArrayList<ExportTrialInstanceBean>();

		List<Integer> trialIds = new ArrayList<Integer>();
		trialIds.add(studyId);
		List<FieldMapInfo> fieldMapInfoList = new ArrayList<FieldMapInfo>();

		try {
			fieldMapInfoList = this.fieldbookMiddlewareService.getFieldMapInfoOfTrial(trialIds, this.crossExpansionProperties);
		} catch (MiddlewareQueryException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		}
		if (fieldMapInfoList != null && fieldMapInfoList.get(0).getDatasets() != null
				&& fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances() != null) {
			for (int i = 0; i < fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances().size(); i++) {
				FieldMapTrialInstanceInfo info = fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances().get(i);
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
	public String getAdvanceListsOfStudy(@PathVariable int studyId, Model model, HttpSession session) {

		List<GermplasmList> germplasmList = new ArrayList<GermplasmList>();
		;
		try {
			germplasmList =
					this.fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(studyId), GermplasmListType.ADVANCED);
		} catch (MiddlewareQueryException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		}
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
	public String doAdvanceExport(HttpServletResponse response, HttpServletRequest req) throws MiddlewareQueryException {

		String advancedListIds = req.getParameter("exportAdvanceListGermplasmIds");
		String exportType = req.getParameter("exportAdvanceListGermplasmType");

		UserSelection userSelection = this.getUserSelection();
		StudyDetails studyDetails = userSelection.getWorkbook().getStudyDetails();

		String outputFilename = null;
		File file = this.exportAdvanceListItems(exportType, advancedListIds, studyDetails);

		outputFilename = file.getAbsolutePath();
		int extensionIndex = outputFilename.lastIndexOf(".");
		String extensionName = outputFilename.substring(extensionIndex, outputFilename.length());
		String contentType = "";
		if (extensionName.indexOf(AppConstants.ZIP_FILE_SUFFIX.getString()) != -1) {
			contentType = "application/zip";
		} else if (extensionName.indexOf(AppConstants.EXPORT_CSV_SUFFIX.getString()) != -1) {
			contentType = "text/csv";
		} else if (extensionName.indexOf(AppConstants.EXPORT_XLS_SUFFIX.getString()) != -1) {
			contentType = ExportStudyController.APPLICATION_VND_MS_EXCEL;
		}
		response.setContentType(contentType);
		;
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("outputFilename", outputFilename);
		results.put("filename", SettingsUtil.cleanSheetAndFileName(file.getName()));
		results.put("contentType", contentType);

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

	protected List<CustomReportType> getCustomReportTypes(String toolSection) {
		List<CustomReportType> customReportTypes = new ArrayList<CustomReportType>();
		try {
			List<StandardPreset> standardPresetList =
					this.workbenchService.getStandardPresetByCrop(this.workbenchService.getFieldbookWebTool().getToolId().intValue(),
							this.contextUtil.getProjectInContext().getCropType().getCropName().toLowerCase(), toolSection);
			// we need to convert the standard preset for custom report type to custom report type pojo
			for (int index = 0; index < standardPresetList.size(); index++) {
				customReportTypes.addAll(CustomReportTypeUtil.readReportConfiguration(standardPresetList.get(index),
						this.crossExpansionProperties.getProfile()));
			}
		} catch (MiddlewareQueryException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
		}

		return customReportTypes;
	}

	protected File exportAdvanceListItems(String exportType, String advancedListIds, StudyDetails studyDetails) {
		if (AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString().equalsIgnoreCase(exportType)
				|| AppConstants.EXPORT_ADVANCE_NURSERY_CSV.getString().equalsIgnoreCase(exportType)) {
			return this.exportAdvanceListService.exportAdvanceGermplasmList(advancedListIds, studyDetails.getStudyName(),
					this.getExportServiceImpl(), exportType);
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
	public String doExportStockList(HttpServletResponse response, HttpServletRequest req) throws MiddlewareQueryException {

		String stockIds = req.getParameter("exportStockListId");

		String outputFilename = null;

		File file = this.exportAdvanceListService.exportStockList(Integer.valueOf(stockIds), this.getExportServiceImpl());

		outputFilename = file.getAbsolutePath();
		String contentType = ExportStudyController.APPLICATION_VND_MS_EXCEL;
		response.setContentType(contentType);
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("outputFilename", outputFilename);
		results.put("filename", SettingsUtil.cleanSheetAndFileName(file.getName()));
		results.put("contentType", contentType);

		return super.convertObjectToJson(results);
	}

	protected void setExportAdvanceListService(ExportAdvanceListService exportAdvanceListService) {
		this.exportAdvanceListService = exportAdvanceListService;
	}

	protected ExportService getExportServiceImpl() {
		return new ExportServiceImpl();
	}

	protected void setUserSelection(UserSelection userSelection) {
		this.studySelection = userSelection;
	}

	public void setExportOrderingRowColService(ExportOrderingRowColImpl exportOrderingRowColService) {
		this.exportOrderingRowColService = exportOrderingRowColService;
	}

	public void setExportOrderingSerpentineOverRangeService(ExportOrderingSerpentineOverRangeImpl exportOrderingSerpentineOverRangeService) {
		this.exportOrderingSerpentineOverRangeService = exportOrderingSerpentineOverRangeService;
	}

	public void setExportOrderingSerpentineOverColumnService(ExportOrderingSerpentineOverColImpl exportOrderingSerpentineOverColumnService) {
		this.exportOrderingSerpentineOverColumnService = exportOrderingSerpentineOverColumnService;
	}

	protected void setCsvExportStudyService(CsvExportStudyService csvExportStudyService) {
		this.csvExportStudyService = csvExportStudyService;
	}

	protected void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected void setOntologyService(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	public void setWorkbenchService(WorkbenchService workbenchService) {
		this.workbenchService = workbenchService;
	}

	public void setCrossExpansionProperties(CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

	public void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

}
