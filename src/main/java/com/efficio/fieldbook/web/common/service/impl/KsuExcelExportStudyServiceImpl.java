
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.service.KsuExcelExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;

@Service
@Transactional
public class KsuExcelExportStudyServiceImpl extends BaseKsuExportStudyServiceImpl implements KsuExcelExportStudyService {
	
	@Override
	void writeOutputFile(final String studyName, final List<List<String>> dataTable, final String fileNamePath) throws IOException {
		FileOutputStream fos = null;
		try {
			final HSSFWorkbook xlsBook = new HSSFWorkbook();
			if (dataTable != null && !dataTable.isEmpty()) {
				final HSSFSheet xlsSheet = xlsBook.createSheet(studyName);
				for (int rowIndex = 0; rowIndex < dataTable.size(); rowIndex++) {
					final HSSFRow xlsRow = xlsSheet.createRow(rowIndex);
		
					for (int colIndex = 0; colIndex < dataTable.get(rowIndex).size(); colIndex++) {
						final HSSFCell cell = xlsRow.createCell(colIndex);
						cell.setCellValue(dataTable.get(rowIndex).get(colIndex));
					}
				}
			}
			fos = new FileOutputStream(new File(fileNamePath));
			xlsBook.write(fos);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	@Override
	String getFileExtension() {
		return AppConstants.EXPORT_XLS_SUFFIX.getString();
	}

	

}
