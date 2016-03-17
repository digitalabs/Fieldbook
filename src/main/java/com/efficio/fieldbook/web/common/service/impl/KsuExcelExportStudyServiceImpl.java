
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import com.efficio.fieldbook.web.common.service.KsuExcelExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;
import com.efficio.fieldbook.web.util.ZipUtil;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class KsuExcelExportStudyServiceImpl implements KsuExcelExportStudyService {

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;

	@Override
	public String export(final Workbook workbook, final String filename, final List<Integer> instances) throws IOException {

		String outputFilename = null;
		FileOutputStream fos = null;

		final int fileExtensionIndex = filename.lastIndexOf(".");
		final String studyName = filename.substring(0, fileExtensionIndex);

		try {
			final List<String> filenameList = new ArrayList<String>();
			final int fileCount = instances.size();
			for (final Integer index : instances) {
				final List<Integer> indexes = new ArrayList<Integer>();
				indexes.add(index);
				final List<MeasurementRow> plotLevelObservations =
						ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), indexes);
				final List<List<String>> dataTable =
						KsuFieldbookUtil.convertWorkbookData(plotLevelObservations, workbook.getMeasurementDatasetVariables());

				final HSSFWorkbook xlsBook = new HSSFWorkbook();

				if (dataTable != null && !dataTable.isEmpty()) {
					final HSSFSheet xlsSheet = xlsBook.createSheet(filename.substring(0, filename.lastIndexOf(".")));
					for (int rowIndex = 0; rowIndex < dataTable.size(); rowIndex++) {
						final HSSFRow xlsRow = xlsSheet.createRow(rowIndex);

						for (int colIndex = 0; colIndex < dataTable.get(rowIndex).size(); colIndex++) {
							final HSSFCell cell = xlsRow.createCell(colIndex);
							cell.setCellValue(dataTable.get(rowIndex).get(colIndex));
						}
					}
				}

				final String filenamePath =
						this.fieldbookProperties.getUploadDirectory() + File.separator + studyName
								+ (fileCount > 1 ? "-" + String.valueOf(index) : "") + filename.substring(fileExtensionIndex);
				fos = new FileOutputStream(new File(filenamePath));
				xlsBook.write(fos);
				filenameList.add(filenamePath);

			}

			final String traitFilenamePath =
					this.fieldbookProperties.getUploadDirectory() + File.separator + studyName + "-Traits"
							+ AppConstants.EXPORT_KSU_TRAITS_SUFFIX.getString();
			KsuFieldbookUtil.writeTraits(workbook.getVariates(), traitFilenamePath, this.fieldbookMiddlewareService, this.ontologyService);
			filenameList.add(traitFilenamePath);

			outputFilename =
					this.fieldbookProperties.getUploadDirectory() + File.separator
							+ filename.replaceAll(AppConstants.EXPORT_XLS_SUFFIX.getString(), "")
							+ AppConstants.ZIP_FILE_SUFFIX.getString();
			ZipUtil.zipIt(outputFilename, filenameList);

		} finally {
			if (fos != null) {
				fos.close();
			}
		}

		return outputFilename;
	}

}
