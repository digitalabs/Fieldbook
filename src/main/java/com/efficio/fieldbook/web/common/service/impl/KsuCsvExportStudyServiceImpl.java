
package com.efficio.fieldbook.web.common.service.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csvreader.CsvWriter;
import com.efficio.fieldbook.web.common.service.KsuCsvExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;

@Service
@Transactional
public class KsuCsvExportStudyServiceImpl extends BaseKsuExportStudyServiceImpl implements KsuCsvExportStudyService {

	@Override
	void writeOutputFile(String studyName, List<List<String>> dataTable, String fileNamePath) throws IOException {
		CsvWriter csvWriter = null;
		try{
			csvWriter = new CsvWriter(new FileWriter(fileNamePath, false), ',');
			for (final List<String> row : dataTable) {
				for (final String cell : row) {
					csvWriter.write(cell);
				}
				csvWriter.endRecord();
			}

		} finally {
			if (csvWriter != null) {
				csvWriter.close();
			}
		}
	}

	@Override
	String getFileExtension() {
		return AppConstants.EXPORT_CSV_SUFFIX.getString();
	}


	


}
