package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.csvreader.CsvWriter;
import com.efficio.fieldbook.web.common.service.KsuCsvExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;
import com.efficio.fieldbook.web.util.ZipUtil;

@Service
public class KsuCsvExportStudyServiceImpl implements KsuCsvExportStudyService {

    private static final Logger LOG = LoggerFactory.getLogger(KsuCsvExportStudyServiceImpl.class);
	
    @Resource
    private FieldbookProperties fieldbookProperties;

	@Override
	public String export(Workbook workbook, String filename, int start, int end) {
		
		List<String> filenameList = new ArrayList<String>();

		int fileExtensionIndex = filename.lastIndexOf(".");
		String studyName = filename.substring(0, fileExtensionIndex);

        CsvWriter csvWriter = null;
        try {
			for (int i = start; i <= end; i++) {
				String filenamePath = fieldbookProperties.getUploadDirectory() + File.separator 
						+ studyName 
						+ "-" + String.valueOf(i) + filename.substring(fileExtensionIndex);
		        boolean alreadyExists = new File(filenamePath).exists();
	            List<MeasurementRow> observations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), i, i);
	            List<List<String>> dataTable = KsuFieldbookUtil.convertWorkbookData(observations, workbook.getMeasurementDatasetVariables());
	
	            csvWriter = new CsvWriter(new FileWriter(filenamePath, false), ',');
	            for (List<String> row : dataTable) {
	            	for (String cell : row) {
	            		csvWriter.write(cell);
	            	}
	            	csvWriter.endRecord();
	            }
	            filenameList.add(filenamePath);
			}

			String traitFilenamePath = fieldbookProperties.getUploadDirectory() + File.separator 
					+ studyName + "-Traits"
					+ AppConstants.EXPORT_KSU_TRAITS_SUFFIX.getString();
			KsuFieldbookUtil.writeTraits(workbook.getVariates(), traitFilenamePath);
			filenameList.add(traitFilenamePath);

        } catch (IOException e) {
            LOG.error("ERROR in KSU CSV Export Study", e);
            
        } finally {
        	if (csvWriter != null) {
        		csvWriter.close();
        	}
        }
		
		String outputFilename = fieldbookProperties.getUploadDirectory() 
				+ File.separator 
				+ filename.replaceAll(AppConstants.EXPORT_CSV_SUFFIX.getString(), "") 
				+ AppConstants.ZIP_FILE_SUFFIX.getString();
		ZipUtil.zipIt(outputFilename, filenameList);

		return outputFilename;
	}

}
