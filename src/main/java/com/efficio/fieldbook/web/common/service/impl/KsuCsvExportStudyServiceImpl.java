package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.csvreader.CsvWriter;
import com.efficio.fieldbook.web.common.service.KsuCsvExportStudyService;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperty;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;

@Service
public class KsuCsvExportStudyServiceImpl implements KsuCsvExportStudyService {

    private static final Logger LOG = LoggerFactory.getLogger(KsuCsvExportStudyServiceImpl.class);
	
	@Override
	public String export(Workbook workbook, String filename, int start, int end) {
        String outputFile = FieldbookProperty.getPathProperty() + File.separator + filename;
        boolean alreadyExists = new File(outputFile).exists();
        CsvWriter csvWriter = null;
        try {
            List<MeasurementRow> observations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getObservations(), start, end);
            List<List<String>> dataTable = KsuFieldbookUtil.convertWorkbookData(observations, workbook.getMeasurementDatasetVariables());

            csvWriter = new CsvWriter(new FileWriter(outputFile, false), ',');
            for (List<String> row : dataTable) {
            	for (String cell : row) {
            		csvWriter.write(cell);
            	}
            	csvWriter.endRecord();
            }
            
        } catch (IOException e) {
            LOG.error("ERROR in KSU CSV Export Study", e);
            
        } finally {
        	if (csvWriter != null) {
        		csvWriter.close();
        	}
        }
        
        return outputFile;
	}

}
