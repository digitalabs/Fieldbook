package com.efficio.fieldbook.web.common.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.pojos.workbench.ToolName;

import com.efficio.fieldbook.web.common.service.ExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;

public abstract class BaseExportStudyServiceImpl implements ExportStudyService {
	
	@Resource
	protected org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
	@Resource
	protected ContextUtil contextUtil;
	
	abstract void writeOutputFile(final Workbook workbook, final List<Integer> visibleColumns,
			final MeasurementRow instanceLevelObservation, final List<MeasurementRow> plotLevelObservations, final String fileNamePath)
			throws IOException;

	abstract String getFileExtension();
	
	@Override
	public FileExportInfo export(final Workbook workbook, final String studyName, final List<Integer> instances) throws IOException {
		return this.export(workbook, studyName, instances, null);
	}
	
	@Override
	public FileExportInfo export(final Workbook workbook, final String studyName, final List<Integer> instances,
			final List<Integer> visibleColumns) throws IOException {
		final List<String> filenameList = new ArrayList<>();
		String outputFilename = null;
		String downloadFilename = null;

		for (final Integer trialInstanceNo : instances) {
			final List<Integer> indexes = new ArrayList<>();
			indexes.add(trialInstanceNo);

			final List<MeasurementRow> plotLevelObservations =
					ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), indexes);

			final MeasurementRow instanceLevelObservation = workbook.getTrialObservationByTrialInstanceNo(trialInstanceNo);
			
			final FileExportInfo exportInfo = ExportImportStudyUtil.getFileNamePath(trialInstanceNo, instanceLevelObservation, instances,
					studyName + this.getFileExtension(), this.fieldbookMiddlewareService, this.contextUtil);
			outputFilename = exportInfo.getFilePath();
			downloadFilename = exportInfo.getDownloadFileName();
			this.writeOutputFile(workbook, visibleColumns, instanceLevelObservation, plotLevelObservations, outputFilename);

			filenameList.add(outputFilename);

		}

		// multiple instances
		if (instances != null && instances.size() > 1) {
			downloadFilename = studyName + AppConstants.ZIP_FILE_SUFFIX.getString();
			outputFilename = createZipFile(studyName, filenameList);
		}

		return new FileExportInfo(outputFilename, downloadFilename);
	}

	protected String createZipFile(final String studyName, final List<String> filenameList) throws IOException {
		String outputFilename;
		final ZipUtil zipUtil = new ZipUtil();
		outputFilename =
				zipUtil.zipIt(studyName, filenameList, this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		return outputFilename;
	}

	
	public void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

}
