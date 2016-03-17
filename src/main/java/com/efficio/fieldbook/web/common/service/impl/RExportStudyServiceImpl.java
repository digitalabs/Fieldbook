/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;

import com.csvreader.CsvWriter;
import com.efficio.fieldbook.web.common.service.RExportStudyService;
import com.efficio.fieldbook.web.nursery.bean.CSVOziel;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RExportStudyServiceImpl implements RExportStudyService {

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private OntologyService ontologyService;

	@Override
	public String export(final Workbook workbook, final String outputFile, final List<Integer> instances) throws IOException {
		return this.exportToR(workbook, outputFile, null, instances);
	}

	@Override
	public String exportToR(final Workbook workbook, final String outputFile, final Integer selectedTrait, final List<Integer> instances)
			throws IOException {
		final String outFile = this.fieldbookProperties.getUploadDirectory() + File.separator + outputFile;
		final List<MeasurementRow> plotLevelObservations =
				ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), instances);
		final List<MeasurementRow> instanceLevelObservations =
				ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getTrialObservations(), instances);
		final CSVOziel csv = new CSVOziel(workbook, plotLevelObservations, instanceLevelObservations);
		CsvWriter csvOutput = null;
		try {
			csvOutput = new CsvWriter(new FileWriter(outFile, false), ',');
			csvOutput.write("LOC");
			csvOutput.write("REP");
			csvOutput.write("BLK");
			csvOutput.write("ENTRY");
			csvOutput.write("GY");
			csv.defineTraitToEvaluate(this.getLabel(workbook.getVariates(), selectedTrait));
			csv.setSelectedTrait(this.getMeasurementVariable(workbook.getVariates(), csv.getStringTraitToEvaluate()));
			csv.writeTraitsR(csvOutput);
			csvOutput.endRecord();
			csv.writeDATAR(csvOutput, this.ontologyService);

		} finally {
			if (csvOutput != null) {
				csvOutput.close();
			}
		}
		return outFile;
	}

	private String getLabel(final List<MeasurementVariable> variables, final Integer termId) {
		if (variables != null && termId != null) {
			for (final MeasurementVariable variable : variables) {
				if (variable.getTermId() == termId) {
					return variable.getName();
				}
			}
		}
		return null;
	}

	private MeasurementVariable getMeasurementVariable(final List<MeasurementVariable> variables, final String label) {
		if (variables != null && label != null) {
			for (final MeasurementVariable variable : variables) {
				if (variable.getName().equalsIgnoreCase(label)) {
					return variable;
				}
			}
		}
		return null;
	}
}
