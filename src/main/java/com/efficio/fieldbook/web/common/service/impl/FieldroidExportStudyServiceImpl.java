/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
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
import com.efficio.fieldbook.web.common.service.FieldroidExportStudyService;
import com.efficio.fieldbook.web.nursery.bean.CSVOziel;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperty;

@Service
public class FieldroidExportStudyServiceImpl implements
		FieldroidExportStudyService {
	
    private static final Logger LOG = LoggerFactory.getLogger(FieldroidExportStudyServiceImpl.class);

	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.nursery.service.ExportStudyService#export(org.generationcp.middleware.domain.etl.Workbook, java.lang.String)
	 */
	@Override
	public String export(Workbook workbook, String filename, int start, int end) {
        String outputFile = FieldbookProperty.getPathProperty() + File.separator + filename;
        boolean alreadyExists = new File(outputFile).exists();
        CsvWriter csvOutput = null;
        List<MeasurementRow> observations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getObservations(), start, end);
        List<MeasurementRow> trialObservations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getTrialObservations(), start, end);
        try {
        	CSVOziel csv = new CSVOziel(workbook, observations, trialObservations);
            csvOutput = new CsvWriter(new FileWriter(outputFile, false), ',');
            csvOutput.write("Trial");
            csvOutput.write("Rep");
            csvOutput.write("Block");
            csvOutput.write("Plot");
            csvOutput.write("Entry");
            csvOutput.write("StockID");
            csvOutput.write("Name");
            csvOutput.write("BreedersPedigree1");
            csv.writeColums(csvOutput, 6);
            csvOutput.write("Origin");
            csv.writeColums(csvOutput, 8);
            csvOutput.write("GID");
            csv.writeColums(csvOutput, 2);
            csv.writeTraitsFromObservations(csvOutput);
            csvOutput.endRecord();
            csv.writeRows(csvOutput, 23);
            csv.writeDATA(csvOutput);
            
        } catch (IOException e) {
            LOG.error("ERROR AL CREAR CVS fieldlog", e);
            
        } finally {
        	if (csvOutput != null) {
        		csvOutput.close();
        	}
        }
        
        return outputFile;
	}

}
