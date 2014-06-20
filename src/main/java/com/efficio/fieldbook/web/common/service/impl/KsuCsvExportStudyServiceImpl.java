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
		for (int i = start; i <= end; i++) {
			int fileExtensionIndex = filename.lastIndexOf(".");
			String filenamePath = fieldbookProperties.getUploadDirectory() + File.separator 
					+ filename.substring(0, fileExtensionIndex)
					+ "-" + String.valueOf(i) + filename.substring(fileExtensionIndex);
	        boolean alreadyExists = new File(filenamePath).exists();
	        CsvWriter csvWriter = null;
	        try {
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
	            
	        } catch (IOException e) {
	            LOG.error("ERROR in KSU CSV Export Study", e);
	            
	        } finally {
	        	if (csvWriter != null) {
	        		csvWriter.close();
	        	}
	        }
		}
		
		String outputFilename;
    	if (filenameList.size() == 1) {
    		outputFilename = filenameList.get(0);
    	}
    	else { //multi-trial instances
			outputFilename = fieldbookProperties.getUploadDirectory() 
					+ File.separator 
					+ filename.replaceAll(AppConstants.EXPORT_CSV_SUFFIX.getString(), "") 
					+ AppConstants.ZIP_FILE_SUFFIX.getString();
			ZipUtil.zipIt(outputFilename, filenameList);
    	}

		return outputFilename;
	}

}
