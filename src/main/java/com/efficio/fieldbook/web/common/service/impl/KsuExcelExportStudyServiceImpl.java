
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.service.KsuExcelExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;
import com.efficio.fieldbook.web.util.ZipUtil;

@Service
public class KsuExcelExportStudyServiceImpl implements KsuExcelExportStudyService {

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;

	@Override
	public String export(Workbook workbook, String filename, List<Integer> instances) {

		String outputFilename = null;
		FileOutputStream fos = null;

		int fileExtensionIndex = filename.lastIndexOf(".");
		String studyName = filename.substring(0, fileExtensionIndex);

		try {
			List<String> filenameList = new ArrayList<String>();
			int fileCount = instances.size();
			for (Integer index : instances) {
				List<Integer> indexes = new ArrayList<Integer>();
				indexes.add(index);
				List<MeasurementRow> observations =
						ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), indexes);
				List<List<String>> dataTable =
						KsuFieldbookUtil.convertWorkbookData(observations, workbook.getMeasurementDatasetVariables());

				HSSFWorkbook xlsBook = new HSSFWorkbook();

				if (dataTable != null && !dataTable.isEmpty()) {
					HSSFSheet xlsSheet = xlsBook.createSheet(filename.substring(0, filename.lastIndexOf(".")));
					for (int rowIndex = 0; rowIndex < dataTable.size(); rowIndex++) {
						HSSFRow xlsRow = xlsSheet.createRow(rowIndex);

						for (int colIndex = 0; colIndex < dataTable.get(rowIndex).size(); colIndex++) {
							HSSFCell cell = xlsRow.createCell(colIndex);
							cell.setCellValue(dataTable.get(rowIndex).get(colIndex));
						}
					}
				}

				String filenamePath =
						this.fieldbookProperties.getUploadDirectory() + File.separator + studyName
								+ (fileCount > 1 ? "-" + String.valueOf(index) : "") + filename.substring(fileExtensionIndex);
				fos = new FileOutputStream(new File(filenamePath));
				xlsBook.write(fos);
				filenameList.add(filenamePath);

			}

			String traitFilenamePath =
					this.fieldbookProperties.getUploadDirectory() + File.separator + studyName + "-Traits"
							+ AppConstants.EXPORT_KSU_TRAITS_SUFFIX.getString();
			KsuFieldbookUtil.writeTraits(workbook.getVariates(), traitFilenamePath, this.fieldbookMiddlewareService, this.ontologyService);
			filenameList.add(traitFilenamePath);

			outputFilename =
					this.fieldbookProperties.getUploadDirectory() + File.separator
							+ filename.replaceAll(AppConstants.EXPORT_XLS_SUFFIX.getString(), "")
							+ AppConstants.ZIP_FILE_SUFFIX.getString();
			ZipUtil.zipIt(outputFilename, filenameList);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return outputFilename;
	}

}
