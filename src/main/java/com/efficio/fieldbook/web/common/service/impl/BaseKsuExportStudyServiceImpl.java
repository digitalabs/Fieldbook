package com.efficio.fieldbook.web.common.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;

import com.efficio.fieldbook.web.common.service.ExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;

public abstract class BaseKsuExportStudyServiceImpl implements ExportStudyService{
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;
	
	@Resource
	private ContextUtil contextUtil;
	
	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();
	
	abstract void writeOutputFile(final String studyName, final List<List<String>> dataTable, final String fileNamePath) throws IOException;
	
	abstract String getFileExtension();
	
	@Override
	public FileExportInfo export(final Workbook workbook, final String studyName, final List<Integer> instances) throws IOException {

		String outputFilename = null;
		String downloadFilename = null;
		FileOutputStream fos = null;

		try {
			final List<String> filenameList = new ArrayList<>();
			final int fileCount = instances.size();
			for (final Integer index : instances) {
				final List<Integer> indexes = new ArrayList<>();
				indexes.add(index);
				final List<MeasurementRow> plotLevelObservations =
						ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), indexes);
				final List<List<String>> dataTable =
						KsuFieldbookUtil.convertWorkbookData(plotLevelObservations, workbook.getMeasurementDatasetVariables());

				final String dataFilename = studyName + (fileCount > 1 ? "-" + index : "") + this.getFileExtension();
				final String dataFilenamePath = this.installationDirectoryUtil.getFileInTemporaryDirectoryForProjectAndTool(dataFilename,
						this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
				this.writeOutputFile(studyName, dataTable, dataFilenamePath);
				
				filenameList.add(dataFilenamePath);
			}

			final String traitFilename = studyName + "-Traits" + AppConstants.EXPORT_KSU_TRAITS_SUFFIX.getString();
			final String traitFilenamePath = this.installationDirectoryUtil.getFileInTemporaryDirectoryForProjectAndTool(traitFilename,
					this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
			this.writeTraitsFile(workbook, traitFilenamePath);
			filenameList.add(traitFilenamePath);

			downloadFilename = studyName + AppConstants.ZIP_FILE_SUFFIX.getString();
			outputFilename = this.createZipFile(studyName, filenameList);

		} finally {
			if (fos != null) {
				fos.close();
			}
		}

		return new FileExportInfo(outputFilename, downloadFilename);
	}

	protected void writeTraitsFile(final Workbook workbook, final String traitFilenamePath) {
		KsuFieldbookUtil.writeTraits(workbook.getVariates(), traitFilenamePath, this.fieldbookMiddlewareService, this.ontologyService);
	}

	protected String createZipFile(final String studyName, final List<String> filenameList) throws IOException {
		String outputFilename;
		final ZipUtil zipUtil = new ZipUtil();
		outputFilename =
				zipUtil.zipIt(studyName, filenameList, this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		return outputFilename;
	}

	@Override
	public FileExportInfo export(Workbook workbook, String filename, List<Integer> instances, List<Integer> visibleColumns)
			throws IOException {
		return this.export(workbook, filename, instances);
	}
	
	public void setOntologyService(final OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	
	public void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

}
