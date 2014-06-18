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
import com.efficio.fieldbook.web.common.service.DataKaptureExportStudyService;
import com.efficio.fieldbook.web.nursery.bean.CSVOziel;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.ZipUtil;

@Service
public class DataKaptureExportStudyServiceImpl implements
		DataKaptureExportStudyService {

    private static final Logger LOG = LoggerFactory.getLogger(DataKaptureExportStudyServiceImpl.class);
    
	@Resource
    private FieldbookProperties fieldbookProperties;

    @Override
	public String export(Workbook workbook, String filename, int start, int end) {
        List<MeasurementRow> observations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), start, end);
        List<MeasurementRow> trialObservations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getTrialObservations(), start, end);
    	CSVOziel csv = new CSVOziel(workbook, observations, trialObservations, true);

    	List<String> filenameList = new ArrayList<String>();
    	filenameList.add(exportObservations(filename, csv));
    	filenameList.add(exportTraits(filename, csv));
    	
		String outputFilename = fieldbookProperties.getUploadDirectory()
				+ File.separator 
				+ filename.replaceAll(AppConstants.EXPORT_XLS_SUFFIX.getString(), "") 
				+ AppConstants.ZIP_FILE_SUFFIX.getString();
		ZipUtil.zipIt(outputFilename, filenameList);
		
		return outputFilename;
	}
	
	private String exportObservations(String filename, CSVOziel csv) {
        String outputFile = fieldbookProperties.getUploadDirectory() + File.separator + filename + AppConstants.EXPORT_CSV_SUFFIX.getString();
    	try {
            new File(outputFile).exists();
        	CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, false), ',');
            csvOutput.write("Site");
            csvOutput.write("Type");
            csvOutput.write("Year");
            csvOutput.write("TrialNumber");
            csvOutput.write("Row");
            csvOutput.write("Column");
            csvOutput.write("PlotBarCode");
            csvOutput.write("GID");
            csvOutput.write("Genotype");
            csvOutput.write("Pedigree");
            csvOutput.write("Rep");
            csv.writeTraitsFromObservations(csvOutput);
            csvOutput.endRecord();
            csv.writeDataDataKapture(csvOutput);
            csvOutput.close();
        } catch (IOException e) {
            LOG.error("ERROR AL CREAR CVS fieldlog");
        }
        return outputFile;
	}
	
	private String exportTraits(String filename, CSVOziel csv) {
        String outputFile = fieldbookProperties.getUploadDirectory() + File.separator 
        		+ filename + AppConstants.DATAKAPTURE_TRAITS_SUFFIX.getString() 
        		+ AppConstants.EXPORT_CSV_SUFFIX.getString();
        try {
            new File(outputFile).exists();
            CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, false), ',');
            csvOutput.write("TraitName");
            csvOutput.write("TraitValRule");
            csvOutput.write("DataType");
            csvOutput.write("AutoProgressFieldLength");
            csvOutput.write("IsDaysTrait");
            csvOutput.write("DateStamp");
            csvOutput.write("TraitUnits");
            csvOutput.write("Connection");
            csvOutput.endRecord();
            csv.writeTraitsDataKapture(csvOutput);
            csvOutput.close();
        } catch (IOException e) {
            LOG.error("ERROR AL CREAR CVS fieldlog", e);
        }
        return outputFile;
	}

}
