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
package com.efficio.fieldbook.web.nursery.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.generationcp.middleware.domain.etl.Workbook;

import com.csvreader.CsvWriter;
import com.efficio.fieldbook.web.nursery.bean.CSVOziel;
import com.efficio.fieldbook.web.nursery.service.FieldroidExportStudyService;

public class FieldroidExportStudyServiceImpl implements
		FieldroidExportStudyService {

	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.nursery.service.ExportStudyService#export(org.generationcp.middleware.domain.etl.Workbook, java.lang.String)
	 */
	@Override
	public void export(Workbook workbook, String filename) {
        String outputFile = filename + ".csv";
        boolean alreadyExists = new File(outputFile).exists();
        CsvWriter csvOutput = null;
        try {
        	CSVOziel csv = new CSVOziel(workbook);
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
            System.out.println("ERROR AL CREAR CVS fieldlog");
            
        } finally {
        	if (csvOutput != null) {
        		csvOutput.close();
        	}
        }
	}

}
