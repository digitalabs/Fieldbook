package com.efficio.fieldbook.web.common.controller;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.service.api.SampleListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import org.generationcp.commons.service.CsvExportSampleListService;
import com.google.common.collect.Lists;

@Controller
@RequestMapping(ExportSampleListController.URL)
@Transactional
public class ExportSampleListController  extends AbstractBaseFieldbookController {

	protected static final String CONTENT_TYPE = "contentType";
	protected static final String FILENAME = "filename";
	protected static final String OUTPUT_FILENAME = "outputFilename";
	private static final String UTF_8 = "UTF-8";
	private static final String ISO_8859_1 = "iso-8859-1";
	private static final String ERROR_MESSAGE = "errorMessage";
	static final String IS_SUCCESS = "isSuccess";
	private static final Logger LOG = LoggerFactory.getLogger(ExportSampleListController.class);
	public static final String URL = "/ExportManager";

	@Resource
	private MessageSource messageSource;

	@Resource
	private SampleListService sampleListService;

	@Resource
	private CsvExportSampleListService csvExportSampleListService;

	@Override
	public String getContentName() {
		return null;
	}

	@RequestMapping(value = "/download/SampleListFile", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> downloadFile(final HttpServletRequest req) throws UnsupportedEncodingException {

		final String outputFilename =
			new String(req.getParameter(ExportSampleListController.OUTPUT_FILENAME).getBytes(ExportSampleListController.ISO_8859_1),
				ExportSampleListController.UTF_8);
		final String filename = new String(req.getParameter(ExportSampleListController.FILENAME).getBytes(ExportSampleListController.ISO_8859_1),
			ExportSampleListController.UTF_8);

		return FieldbookUtil.createResponseEntityForFileDownload(outputFilename, filename);

	}

	@ResponseBody
	@RequestMapping(value = "/exportSampleList/{exportType}", method = RequestMethod.POST)
	public String exportSampleList(@RequestBody final Map<String, String> data, @PathVariable final int exportType,
		final HttpServletRequest req, final HttpServletResponse response) {

		ExportSampleListController.LOG.debug("Entering Export exportSampleList");
		final String export = this.doExport(response, data);
		ExportSampleListController.LOG.debug("Exiting Export exportSampleList");
		return export;
	}

	private String doExport(final HttpServletResponse response, final Map<String, String> data) {
		ExportSampleListController.LOG.debug("Entering Export doExport");

		final Map<String, Object> results = new HashMap<>();

		try{
			String filename = FileUtils.sanitizeFileName(data.get("studyname") + "-" + data.get("listname"));
			final Integer sampleListId = Integer.valueOf(data.get("listId"));
			final List<String> visibleColumns = Lists.newArrayList(data.get("visibleColumns").split(","));
	
			final List<SampleDetailsDTO> sampleDetailsDTOs = sampleListService.getSampleDetailsDTOs(sampleListId);
			final String enumeratorVariableName = sampleListService.getObservationVariableName(sampleListId);
			final FileExportInfo exportInfo = this.csvExportSampleListService.export(sampleDetailsDTOs, filename, visibleColumns, enumeratorVariableName);
			response.setContentType(FileUtils.MIME_CSV);

			results.put(ExportSampleListController.IS_SUCCESS, true);
			results.put(ExportSampleListController.OUTPUT_FILENAME, exportInfo.getFilePath());
			results.put(ExportSampleListController.FILENAME, exportInfo.getDownloadFileName());
			results.put(ExportSampleListController.CONTENT_TYPE, response.getContentType());


		} catch (final Exception e) {
			// generic exception handling block needs to be added here so that the calling AJAX function receives proper notification that
			// the operation was a failure
			LOG.error("Error exporting sampleList: " + e.getMessage(), e);
			results.put(IS_SUCCESS, false);
			results.put(ERROR_MESSAGE, this.messageSource.getMessage("export.study.error", null, Locale.ENGLISH));
		}
		LOG.debug("Exiting Export doExport");
		return super.convertObjectToJson(results);
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public SampleListService getSampleListService() {
		return sampleListService;
	}

	public void setSampleListService(SampleListService sampleListService) {
		this.sampleListService = sampleListService;
	}

	public CsvExportSampleListService getCsvExportSampleListService() {
		return csvExportSampleListService;
	}

	public void setCsvExportSampleListService(CsvExportSampleListService csvExportSampleListService) {
		this.csvExportSampleListService = csvExportSampleListService;
	}
}
