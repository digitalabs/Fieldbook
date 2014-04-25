package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.service.KsuExceIExportStudyService;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperty;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;

@Service
public class KsuExcelExportStudyServiceImpl implements
		KsuExceIExportStudyService {

	@Override
	public String export(Workbook workbook, String filename, int start, int end) {
		
		String outputFilename = null;
		FileOutputStream fos = null;

        try {
            List<MeasurementRow> observations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getObservations(), start, end);
            List<List<String>> dataTable = KsuFieldbookUtil.convertWorkbookData(observations, workbook.getMeasurementDatasetVariables());

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
			
			String filenamePath = FieldbookProperty.getPathProperty() + File.separator + filename;
			fos = new FileOutputStream(new File(filenamePath));
			xlsBook.write(fos);
			outputFilename = filenamePath;
        	
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
