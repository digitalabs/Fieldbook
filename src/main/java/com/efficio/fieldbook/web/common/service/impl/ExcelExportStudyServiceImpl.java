/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.ZipUtil;

@Service
public class ExcelExportStudyServiceImpl implements ExcelExportStudyService {
	
	private static final int PIXEL_SIZE = 250;
	
	private static final String OCC_8170_LABEL = "8170_LABEL";
	
	@Resource
	private MessageSource messageSource;
	
	@Resource
    private FieldbookProperties fieldbookProperties;
	
	@Resource
	private OntologyService ontologyService;
	
	private static final List<Integer> STUDY_DETAILS_IDS = Arrays.asList(TermId.STUDY_NAME.getId(), TermId.STUDY_TITLE.getId(), 
			TermId.PM_KEY.getId(), TermId.STUDY_OBJECTIVE.getId(), TermId.START_DATE.getId(), TermId.END_DATE.getId(), 
			TermId.STUDY_TYPE.getId(), TermId.STUDY_UID.getId(), TermId.STUDY_STATUS.getId());
	
	@Override
	public String export(Workbook workbook, String filename,  List<Integer> instances) {
		FileOutputStream fos = null;
		List<String> filenameList = new ArrayList<String>();
		String outputFilename = null;
		
			for (Integer index : instances) {
	    		List<Integer> indexes = new ArrayList<Integer>();
	    		indexes.add(index);
        		
	            List<MeasurementRow> observations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), indexes);
	            List<MeasurementRow> trialObservations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getTrialObservations(), indexes);
				try {
					MeasurementRow trialObservation = trialObservations.get(0);
					
					HSSFWorkbook xlsBook = new HSSFWorkbook();
					
					writeDescriptionSheet(xlsBook, workbook, trialObservation);
					writeObservationSheet(xlsBook, workbook, observations);
					
					String filenamePath = fieldbookProperties.getUploadDirectory() + File.separator + filename;
					if (instances != null && instances.size() > 1) {
						int fileExtensionIndex = filenamePath.lastIndexOf(".");
						filenamePath = filenamePath.substring(0, fileExtensionIndex) +  "-" + index + filenamePath.substring(fileExtensionIndex);
					}
					fos = new FileOutputStream(new File(filenamePath));
					xlsBook.write(fos);
					outputFilename = filenamePath;
					filenameList.add(filenamePath);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			if (instances != null && instances.size() > 1) {
				outputFilename = fieldbookProperties.getUploadDirectory() 
						+ File.separator 
						+ filename.replaceAll(AppConstants.EXPORT_XLS_SUFFIX.getString(), "") 
						+ AppConstants.ZIP_FILE_SUFFIX.getString();
				ZipUtil.zipIt(outputFilename, filenameList);
			}
			
		return outputFilename;
	}
	
	
	private void writeDescriptionSheet(HSSFWorkbook xlsBook, Workbook workbook, MeasurementRow trialObservation) {
		Locale locale = LocaleContextHolder.getLocale();
		HSSFSheet xlsSheet = xlsBook.createSheet(messageSource.getMessage("export.study.sheet.description", null, locale));
		int currentRowNum = 0;
		
		currentRowNum = writeStudyDetails(currentRowNum, xlsBook, xlsSheet, workbook.getStudyDetails());
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = writeConditions(currentRowNum, xlsBook, xlsSheet, workbook.getConditions(), trialObservation);
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = writeFactors(currentRowNum, xlsBook, xlsSheet, workbook.getNonTrialFactors());
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = writeConstants(currentRowNum, xlsBook, xlsSheet, workbook.getConstants(), trialObservation);
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = writeVariates(currentRowNum, xlsBook, xlsSheet, workbook.getVariates());
		
//		for(int i = 0 ; i < 8 ; i++){
//			xlsSheet.autoSizeColumn(i);
//		}
		xlsSheet.setColumnWidth(0, 20 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(1, 24 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(2, 30 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(3, 18 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(4, 18 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(5, 15 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(6, 20 * PIXEL_SIZE);
		xlsSheet.setColumnWidth(7, 20 * PIXEL_SIZE);
	}

	private void writeObservationSheet(HSSFWorkbook xlsBook, Workbook workbook, List<MeasurementRow> observations) {
		Locale locale = LocaleContextHolder.getLocale();
		HSSFSheet xlsSheet = xlsBook.createSheet(messageSource.getMessage("export.study.sheet.observation", null, locale));
		int currentRowNum = 0;
		
		writeObservationHeader(currentRowNum++, xlsBook, xlsSheet, workbook.getMeasurementDatasetVariables());
		
		String propertyName = "";
		try {
		    propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getTerm().getName();
		} catch (MiddlewareQueryException e) {
		    e.printStackTrace();
		}
		
		for (MeasurementRow dataRow : observations) {
			writeObservationRow(currentRowNum++, xlsSheet, dataRow, workbook.getMeasurementDatasetVariables(), xlsBook, propertyName);
		}
	}
	
	private int writeStudyDetails(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, StudyDetails studyDetails) {
		writeStudyDetailRow(xlsBook, xlsSheet, currentRowNum++, "export.study.description.details.study", studyDetails.getStudyName());
		writeStudyDetailRow(xlsBook, xlsSheet, currentRowNum++, "export.study.description.details.title", studyDetails.getTitle());
//		writeStudyDetailRow(xlsBook, xlsSheet, currentRowNum++, "export.study.description.details.pmkey", studyDetails.getPmKey());
		writeStudyDetailRow(xlsBook, xlsSheet, currentRowNum++, "export.study.description.details.objective", studyDetails.getObjective());
		String startDate = studyDetails.getStartDate();
		String endDate = studyDetails.getEndDate();
		
		if(startDate != null){
			startDate = startDate.replace("-", "");
		}
		
		if(endDate != null){
			endDate = endDate.replace("-", "");
		}
		
		writeStudyDetailRow(xlsBook, xlsSheet, currentRowNum++, "export.study.description.details.startdate", startDate);
		writeStudyDetailRow(xlsBook, xlsSheet, currentRowNum++, "export.study.description.details.enddate", endDate);
		writeStudyDetailRow(xlsBook, xlsSheet, currentRowNum++, "export.study.description.details.studytype", studyDetails.getStudyType().name());
		
		return currentRowNum;
	}
	
	private int writeConditions(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> conditions,
			MeasurementRow trialObservation) {
		List<MeasurementVariable> arrangedConditions = new ArrayList<MeasurementVariable>();
		List<MeasurementVariable> filteredConditions = new ArrayList<MeasurementVariable>();
		if(conditions != null){
			arrangedConditions.addAll(conditions);
			Collections.sort(arrangedConditions, new Comparator<MeasurementVariable>(){
				   @Override
				   public int compare(MeasurementVariable var1, MeasurementVariable  var2) {
					   return var1.getName().compareToIgnoreCase(var2.getName());
				     }
				 });
			
			for (MeasurementVariable variable : arrangedConditions) {
				if (!STUDY_DETAILS_IDS.contains(variable.getTermId())) {
					filteredConditions.add(variable);
					if (PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().contains(variable.getLabel())) {
						variable.setValue(trialObservation.getMeasurementDataValue(variable.getName()));
					}
				}
			}
		}
		return writeSection(currentRowNum, xlsBook, xlsSheet, filteredConditions, "export.study.description.column.condition", 51, 153, 102);
	}
	

	private int writeFactors(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> factors) {
		List<MeasurementVariable> filteredFactors = new ArrayList<MeasurementVariable>();
		for (MeasurementVariable factor : factors) {
			if (factor.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				filteredFactors.add(factor);
			}
		}
		return writeSection(currentRowNum, xlsBook, xlsSheet, filteredFactors, "export.study.description.column.factor", 51, 153, 102);
	}
	
	private int writeConstants(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> constants, 
			MeasurementRow trialObservation) {
		
		List<MeasurementVariable> filteredConditions = new ArrayList<MeasurementVariable>();
		for (MeasurementVariable variable : constants) {
			filteredConditions.add(variable);
			if (PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().contains(variable.getLabel())) {
				variable.setValue(trialObservation.getMeasurementDataValue(variable.getName()));
			}
		}
		return writeSection(currentRowNum, xlsBook, xlsSheet, constants, "export.study.description.column.constant", 51, 51, 153);
	}
	
	private int writeVariates(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> variates) {
		return writeSection(currentRowNum, xlsBook, xlsSheet, variates, "export.study.description.column.variate", 51, 51, 153);
	}
	
	private CellStyle getHeaderStyle(HSSFWorkbook xlsBook, int c1, int c2, int c3) {
		HSSFPalette palette = xlsBook.getCustomPalette();
		HSSFColor color = palette.findSimilarColor(c1, c2, c3);
		short colorIndex = color.getIndex();
		
		HSSFFont whiteFont = xlsBook.createFont();
		whiteFont.setColor(new HSSFColor.WHITE().getIndex());
		
		CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setFillForegroundColor(colorIndex);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setFont(whiteFont);
		
		return cellStyle;
	}
	
	private void writeStudyDetailRow(HSSFWorkbook xlsBook, HSSFSheet xlsSheet, int currentRowNum, String label, String value) {
		Locale locale = LocaleContextHolder.getLocale();
		HSSFRow row = xlsSheet.createRow(currentRowNum);
		HSSFCell cell = row.createCell(0, HSSFCell.CELL_TYPE_STRING);
		cell.setCellStyle(getHeaderStyle(xlsBook, 153, 51, 0));
		cell.setCellValue(messageSource.getMessage(label, null, locale));
		cell = row.createCell(1, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(value);
	}
	
	private int writeSection(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> variables, String sectionLabel,
			int c1, int c2, int c3) {
		
		writeSectionHeader(xlsBook, xlsSheet, currentRowNum++, sectionLabel, c1, c2, c3);
		if (variables != null && !variables.isEmpty()) {
			for (MeasurementVariable variable : variables) {
				writeSectionRow(currentRowNum++, xlsSheet, variable);
			}
		}
		return currentRowNum;
	}
	
	private void writeSectionHeader(HSSFWorkbook xlsBook, HSSFSheet xlsSheet, int currentRowNum, String typeLabel, int c1, int c2, int c3) {
		Locale locale = LocaleContextHolder.getLocale();
		HSSFRow row = xlsSheet.createRow(currentRowNum);
		
		HSSFCell cell = row.createCell(0, HSSFCell.CELL_TYPE_STRING);
		cell.setCellStyle(getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(messageSource.getMessage(typeLabel, null, locale));

		cell = row.createCell(1, HSSFCell.CELL_TYPE_STRING);
		cell.setCellStyle(getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(messageSource.getMessage("export.study.description.column.description", null, locale));
		
		cell = row.createCell(2, HSSFCell.CELL_TYPE_STRING);
		cell.setCellStyle(getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(messageSource.getMessage("export.study.description.column.property", null, locale));
		
		cell = row.createCell(3, HSSFCell.CELL_TYPE_STRING);
		cell.setCellStyle(getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(messageSource.getMessage("export.study.description.column.scale", null, locale));
		
		cell = row.createCell(4, HSSFCell.CELL_TYPE_STRING);
		cell.setCellStyle(getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(messageSource.getMessage("export.study.description.column.method", null, locale));
		
		cell = row.createCell(5, HSSFCell.CELL_TYPE_STRING);
		cell.setCellStyle(getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(messageSource.getMessage("export.study.description.column.datatype", null, locale));
		
		cell = row.createCell(6, HSSFCell.CELL_TYPE_STRING);
		cell.setCellStyle(getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(messageSource.getMessage("export.study.description.column.value", null, locale));
		
		cell = row.createCell(7, HSSFCell.CELL_TYPE_STRING);
		cell.setCellStyle(getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(messageSource.getMessage("export.study.description.column.label", null, locale));
	}
	
	private void writeSectionRow(int currentRowNum, HSSFSheet xlsSheet, MeasurementVariable variable) {
		HSSFRow row = xlsSheet.createRow(currentRowNum);

		HSSFCell cell = row.createCell(0, HSSFCell.CELL_TYPE_STRING);
		String occName = variable.getName();
		String appConstant8170 = AppConstants.getString(OCC_8170_LABEL);
		if(appConstant8170 != null && appConstant8170.equalsIgnoreCase(occName))
			occName = AppConstants.OCC.getString();
		cell.setCellValue(occName);

		cell = row.createCell(1, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getDescription());

		cell = row.createCell(2, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getProperty());

		cell = row.createCell(3, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getScale());

		cell = row.createCell(4, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getMethod());

		cell = row.createCell(5, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getDataTypeDisplay());

		cell = row.createCell(6, HSSFCell.CELL_TYPE_STRING);
		cleanupValue(variable);
		if(variable.getDataTypeId() != null && variable.getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())){
			if(variable.getValue() != null && !"".equalsIgnoreCase(variable.getValue())){
				cell.setCellType(Cell.CELL_TYPE_BLANK);
				cell.setCellType(Cell.CELL_TYPE_NUMERIC);		
				cell.setCellValue(Double.valueOf(variable.getValue()));				
			}else{
				cell.setCellValue(variable.getValue());	
			}		
		}
		else{
			cell.setCellValue(variable.getValue());
		}

		cell = row.createCell(7, HSSFCell.CELL_TYPE_STRING);
		if (variable.getTreatmentLabel() != null && !"".equals(variable.getTreatmentLabel())) {
			cell.setCellValue(variable.getTreatmentLabel());
		}
		else {
			cell.setCellValue(variable.getLabel());
		}
	}
	
	private void writeObservationHeader(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> variables) {
		if (variables != null && !variables.isEmpty()) {
			int currentColNum = 0;
			HSSFRow row = xlsSheet.createRow(currentRowNum++);
			for (MeasurementVariable variable : variables) {
				HSSFCell cell = row.createCell(currentColNum++);
				if (variable.isFactor()) {
					cell.setCellStyle(getHeaderStyle(xlsBook, 51, 153, 102));
				}
				else {
					cell.setCellStyle(getHeaderStyle(xlsBook, 51, 51, 153));
				}
				cell.setCellValue(variable.getName());
			}
			
		}
	}
	
	private void writeObservationRow(int currentRowNum, HSSFSheet xlsSheet, MeasurementRow dataRow, List<MeasurementVariable> variables, 
			HSSFWorkbook xlsBook, String propertyName) {
		
		HSSFRow row = xlsSheet.createRow(currentRowNum);
		int currentColNum = 0;
		CellStyle style =  xlsBook.createCellStyle();
		DataFormat format = xlsBook.createDataFormat();
		style.setDataFormat(format.getFormat("0.#"));

		for (MeasurementVariable variable : variables) {
			MeasurementData dataCell = dataRow.getMeasurementData(variable.getTermId());
			if (dataCell != null) {
				if (dataCell.getMeasurementVariable() != null && dataCell.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					continue;
				}
				HSSFCell cell = row.createCell(currentColNum++);
										
				if (dataCell.getMeasurementVariable() != null && dataCell.getMeasurementVariable().getPossibleValues() != null
						&& !dataCell.getMeasurementVariable().getPossibleValues().isEmpty() 
						&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE.getId()
						&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE_CODE.getId()
						&& !dataCell.getMeasurementVariable().getProperty().equals(propertyName)) {
	
					cell.setCellValue(ExportImportStudyUtil.getCategoricalCellValue(dataCell.getValue(), dataCell.getMeasurementVariable().getPossibleValues()));
				}
				else {
					
					if(AppConstants.NUMERIC_DATA_TYPE.getString().equalsIgnoreCase(dataCell.getDataType())){					
						if(dataCell.getValue() != null && !"".equalsIgnoreCase(dataCell.getValue())){
							cell.setCellType(Cell.CELL_TYPE_BLANK);
							cell.setCellType(Cell.CELL_TYPE_NUMERIC);		
							cell.setCellValue(Double.valueOf(dataCell.getValue()));
							
						}
					}else{
						cell.setCellValue(dataCell.getValue());	
					}
				}
			}			
		}
	}
	
	private void cleanupValue(MeasurementVariable variable) {
		if (variable.getValue() != null) {
			variable.setValue(variable.getValue().trim());
			List<Integer> specialDropdowns = getSpecialDropdownIds();
			if (specialDropdowns.contains(variable.getTermId()) && "0".equals(variable.getValue())) {
				variable.setValue("");
			}
			else if (variable.getDataTypeId().equals(TermId.DATE_VARIABLE.getId()) && "0".equals(variable.getValue())) {
				variable.setValue("");
			}
		}
	}
	
	private List<Integer> getSpecialDropdownIds() {
		List<Integer> ids = new ArrayList<Integer>();
		
		String idNameCombo = AppConstants.ID_NAME_COMBINATION.getString();
		String[] idNames = idNameCombo.split(",");
		for (String idName : idNames) {
			ids.add(Integer.valueOf(idName.substring(0, idName.indexOf("|"))));
		}
		
		return ids;
	}
	
}
