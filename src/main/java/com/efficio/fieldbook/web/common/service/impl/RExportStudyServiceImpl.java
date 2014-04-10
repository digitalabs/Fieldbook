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
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.springframework.stereotype.Service;

import com.csvreader.CsvWriter;
import com.efficio.fieldbook.web.common.service.RExportStudyService;
import com.efficio.fieldbook.web.nursery.bean.CSVOziel;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperty;
@Service
public class RExportStudyServiceImpl implements RExportStudyService {

	@Override
	public String export(Workbook workbook, String outputFile, int start, int end) {
		return exportToR(workbook, outputFile, null, start, end);
	}
	
	@Override
	public String exportToR(Workbook workbook, String outputFile, Integer selectedTrait, int start, int end) {
		String outFile = FieldbookProperty.getPathProperty() + File.separator + outputFile;
        boolean alreadyExists = new File(outFile).exists();
        List<MeasurementRow> observations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getObservations(), start, end);
        List<MeasurementRow> trialObservations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getTrialObservations(), start, end);
        CSVOziel csv = new CSVOziel(workbook, observations, trialObservations);
        CsvWriter csvOutput = null;
        try {
            csvOutput = new CsvWriter(new FileWriter(outFile, false), ',');
            csvOutput.write("LOC");
            csvOutput.write("REP");
            csvOutput.write("BLK");
            csvOutput.write("ENTRY");
            csvOutput.write("GY");
            csv.defineTraitToEvaluate(getLabel(workbook.getVariates(), selectedTrait));
            csv.setSelectedTrait(getMeasurementVariable(workbook.getVariates(), csv.getStringTraitToEvaluate()));
            csv.writeTraitsR(csvOutput);
            csvOutput.endRecord();
            csv.writeDATAR(csvOutput);
            
        } catch (IOException e) {
        	e.printStackTrace();
        	
        } finally {
        	if (csvOutput != null) {
        		csvOutput.close();
        	}
        }
        return outFile;
	}

	private String getLabel(List<MeasurementVariable> variables, Integer termId) {
		if (variables != null && termId != null) {
			for (MeasurementVariable variable : variables) {
				if (variable.getTermId() == termId) {
					return variable.getName();
				}
			}
		}
		return null;
	}
	
	private MeasurementVariable getMeasurementVariable(List<MeasurementVariable> variables, String label) {
		if (variables != null && label != null) {
			for (MeasurementVariable variable : variables) {
				if (variable.getName().equalsIgnoreCase(label)) {
					return variable;
				}
			}
		}
		return null;
	}
}
