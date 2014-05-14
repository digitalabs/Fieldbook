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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.service.ExcelImportStudyService;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Service
public class ExcelImportStudyServiceImpl implements ExcelImportStudyService {

	private static final String TEMPLATE_DESCRIPTION_SHEET_FIRST_VALUE = "STUDY";
	private static final String TEMPLATE_SECTION_CONDITION = "CONDITION";
	private static final String TEMPLATE_SECTION_FACTOR = "FACTOR";
	private static final String TEMPLATE_SECTION_CONSTANT = "CONSTANT";
	private static final String TEMPLATE_SECTION_VARIATE = "VARIATE";
	
	@Override
	public void importWorkbook(Workbook workbook, String filename) throws WorkbookParserException {
		try {
			HSSFWorkbook xlsBook = new HSSFWorkbook(new FileInputStream(new File(filename))); //WorkbookFactory.create(new FileInputStream(new File(filename)));
			
			List<MeasurementRow> observations = filterObservationsByTrialInstance(xlsBook, workbook.getObservations());
			List<MeasurementRow> trialObservations = filterObservationsByTrialInstance(xlsBook, workbook.getTrialObservations());

			validate(xlsBook, workbook, observations);
				
			importDataToWorkbook(xlsBook, observations);
			importTrialToWorkbook(xlsBook, trialObservations);

		} catch (WorkbookParserException e) {
			throw e;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new WorkbookParserException(e.getMessage());
		}
	}
	
	private void importDataToWorkbook(HSSFWorkbook xlsBook, List<MeasurementRow> observations) {
		if (observations != null) {
			HSSFSheet observationSheet = xlsBook.getSheetAt(1);
			int xlsRowIndex = 1; //row 0 is the header row
			for (MeasurementRow wRow : observations) {
				HSSFRow xlsRow = observationSheet.getRow(xlsRowIndex);
				for (MeasurementData wData : wRow.getDataList()) {
					if (wData.isEditable()) {
						String label = wData.getLabel();
						int xlsColIndex = findColumn(observationSheet, label);
						Cell cell = xlsRow.getCell(xlsColIndex);
						String xlsValue = "";
						
						if(cell != null){
							if (wData.getMeasurementVariable() != null && wData.getMeasurementVariable().getPossibleValues() != null
									&& !wData.getMeasurementVariable().getPossibleValues().isEmpty()) {
								
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									xlsValue = ExportImportStudyUtil.getCategoricalIdCellValue(String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue()), 
											wData.getMeasurementVariable().getPossibleValues());
								}
								else {
									xlsValue = ExportImportStudyUtil.getCategoricalIdCellValue(cell.getStringCellValue(), wData.getMeasurementVariable().getPossibleValues());
								}
							} 
							else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
								Double doubleVal = Double.valueOf(cell.getNumericCellValue());
								Integer intVal = Integer.valueOf(doubleVal.intValue());
								if(Double.parseDouble(intVal.toString()) == doubleVal.doubleValue()){
									xlsValue = intVal.toString();
								}else{
									xlsValue = doubleVal.toString();	
								}
								
								
							}
							else
								xlsValue = cell.getStringCellValue();
						}
						wData.setValue(xlsValue);
					}
				}
				xlsRowIndex++;
			}
		}
	}

	private void importTrialToWorkbook(HSSFWorkbook xlsBook, List<MeasurementRow> observations) {
		if (observations != null) {
			for (MeasurementRow wRow : observations) {
				for (MeasurementData wData : wRow.getDataList()) {
					String label = wData.getLabel();
					String value = findValueFromDescriptionSheet(xlsBook, label);
					wData.setValue(value);
				}
			}
		}
	}
	
	private void validate(HSSFWorkbook xlsBook, Workbook workbook, List<MeasurementRow> observations) throws WorkbookParserException {
		HSSFSheet descriptionSheet = xlsBook.getSheetAt(0);
		HSSFSheet observationSheet = xlsBook.getSheetAt(1);
		
		validateNumberOfSheets(xlsBook);
		validateDescriptionSheetFirstCell(descriptionSheet);
		validateSections(descriptionSheet);
		validateRequiredObservationColumns(observationSheet, workbook);
		validateNumberOfRows(observationSheet, observations);
		validateRowIdentifiers(observationSheet, workbook, observations);
		validateObservationColumns(getAllVariates(descriptionSheet), workbook);
	}
	
	private void validateNumberOfSheets(HSSFWorkbook xlsBook) throws WorkbookParserException {
		if (xlsBook.getNumberOfSheets() != 2) {
			throw new WorkbookParserException("error.workbook.import.invalidNumberOfSheets");
		}
	}
	
	private void validateDescriptionSheetFirstCell(HSSFSheet descriptionSheet) throws WorkbookParserException {
		if (!TEMPLATE_DESCRIPTION_SHEET_FIRST_VALUE.equalsIgnoreCase(descriptionSheet.getRow(0).getCell(0).getStringCellValue())) {
			throw new WorkbookParserException("error.workbook.import.invalidFormatDescriptionSheet");
		}
	}
	
	private void validateSections(HSSFSheet descriptionSheet) throws WorkbookParserException {
		int conditionRow = findRow(descriptionSheet, TEMPLATE_SECTION_CONDITION);
		int factorRow = findRow(descriptionSheet, TEMPLATE_SECTION_FACTOR);
		int constantRow = findRow(descriptionSheet, TEMPLATE_SECTION_CONSTANT);
		int variateRow = findRow(descriptionSheet, TEMPLATE_SECTION_VARIATE);
		if (conditionRow <= 0 || factorRow <= conditionRow 
				|| constantRow <= conditionRow || variateRow <= conditionRow) {
			throw new WorkbookParserException("error.workbook.import.invalidSections");
		}			
	}
	
	private void validateRequiredObservationColumns(HSSFSheet obsSheet, Workbook workbook) throws WorkbookParserException {
		int entryCol = findColumn(obsSheet, getColumnLabel(workbook, TermId.ENTRY_NO.getId()));
		int plotCol = findColumn(obsSheet, getColumnLabel(workbook, TermId.PLOT_NO.getId()));
		if (plotCol == -1) {
			plotCol = findColumn(obsSheet, getColumnLabel(workbook, TermId.PLOT_NNO.getId()));
		}
		int gidCol = findColumn(obsSheet, getColumnLabel(workbook, TermId.GID.getId()));
		int desigCol = findColumn(obsSheet, getColumnLabel(workbook, TermId.DESIG.getId()));
		if (entryCol <= -1 || plotCol <= -1 || gidCol <= -1 || desigCol <= -1) {
			throw new WorkbookParserException("error.workbook.import.requiredColumnsMissing");
		}
	}
	
	private void validateNumberOfRows(HSSFSheet observationSheet, List<MeasurementRow> observations) throws WorkbookParserException {
		if (observations != null && observationSheet.getLastRowNum() != observations.size()) {
			throw new WorkbookParserException("error.workbook.import.observationRowCountMismatch");
		}
	}
	
	private void validateRowIdentifiers(HSSFSheet observationSheet, Workbook workbook, List<MeasurementRow> observations) throws WorkbookParserException {
		if (observations != null) {
			String gidLabel = getColumnLabel(workbook, TermId.GID.getId());
			String desigLabel = getColumnLabel(workbook, TermId.DESIG.getId());
			String entryLabel = getColumnLabel(workbook, TermId.ENTRY_NO.getId());
			int gidCol = findColumn(observationSheet, gidLabel);
			int desigCol = findColumn(observationSheet, desigLabel);
			int entryCol = findColumn(observationSheet, entryLabel);
			int rowIndex = 1;
			for (MeasurementRow wRow : observations) {
				HSSFRow row = observationSheet.getRow(rowIndex++);

				Integer gid = getMeasurementDataValueInt(wRow, gidLabel);
				String desig = getMeasurementDataValue(wRow, desigLabel);
				Integer entry = getMeasurementDataValueInt(wRow, entryLabel);
				Integer xlsGid = getExcelValueInt(row, gidCol);
				String xlsDesig = row.getCell(desigCol).getStringCellValue();
				Integer xlsEntry = getExcelValueInt(row, entryCol);

				if (gid == null || desig == null || entry == null || xlsDesig == null
						|| !gid.equals(xlsGid) || !desig.trim().equalsIgnoreCase(xlsDesig.trim())
						|| !entry.equals(xlsEntry)) {
					
					throw new WorkbookParserException("error.workbook.import.observationRowMismatch");
				}
			}
		}
	}
	
	private void validateObservationColumns(List<String> allXlsVariates, Workbook workbook) throws WorkbookParserException {
		if (workbook.getMeasurementDatasetVariables() != null) {
			for (MeasurementVariable mvar : workbook.getVariates()) {
				if (!allXlsVariates.contains(mvar.getName().toUpperCase())) {
					throw new WorkbookParserException("error.workbook.import.columnsMismatch");
				}
			}
		}
	}
	
	private List<String> getAllVariates(HSSFSheet descriptionSheet) {
		List<String> variates = new ArrayList<String>();
		
		int startRowIndex = findRow(descriptionSheet, TEMPLATE_SECTION_VARIATE) + 1;
		int endRowIndex = descriptionSheet.getLastRowNum();
		
		if (startRowIndex <= endRowIndex) {
			for (int rowIndex = startRowIndex; rowIndex <= endRowIndex; rowIndex++) {
				if (descriptionSheet.getRow(rowIndex) == null || descriptionSheet.getRow(rowIndex).getCell(0) == null 
						|| descriptionSheet.getRow(rowIndex).getCell(0).getStringCellValue() == null) {
					break;
				}
				variates.add(descriptionSheet.getRow(rowIndex).getCell(0).getStringCellValue().toUpperCase());
			}
		}
		
		return variates;
	}
	
    private int findRow(HSSFSheet sheet, String cellValue) {
        int result = 0;
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            HSSFRow row = sheet.getRow(i);
            if (row != null) {
                HSSFCell cell = row.getCell(0);
                if (cell != null && cell.getStringCellValue() != null) {
                    if (cell.getStringCellValue().equals(cellValue)) {
                        return i;
                    }
                }
            }
        }

        return result;
    }

    private int findColumn(HSSFSheet sheet, String cellValue) {
        int result = -1;
        if (cellValue != null) {
	        HSSFRow row = sheet.getRow(0); //Encabezados
	        int cells = row.getLastCellNum();
	        for (int i = 0; i < cells; i++) {
	            HSSFCell cell = row.getCell(i);
	            if (cell.getStringCellValue().equals(cellValue)) {
	                return i;
	            }
	        }
        }
        return result;
    }

    private String findValueFromDescriptionSheet(HSSFWorkbook workbook, String cellValue) {
    	HSSFSheet sheet = workbook.getSheetAt(0);
        if (cellValue != null) {
	        int lastRow = sheet.getLastRowNum();
	        for (int i = 0; i < lastRow; i++) {
	        	HSSFRow row = sheet.getRow(i);
	        	if (row != null) {
		        	HSSFCell cell = row.getCell(0);
		        	if (cell != null && row.getCell(6) != null) {
		        		String value = "";
		        		if (row.getCell(6).getCellType() == Cell.CELL_TYPE_NUMERIC) {
		        			value = String.valueOf(Double.valueOf(row.getCell(6).getNumericCellValue()).intValue());
		        		}
		        		else {
		        			value = row.getCell(6).getStringCellValue();
		        		}
			        	if (cell.getStringCellValue().equalsIgnoreCase(cellValue)) {
			        		return value;
			        	}
		        	}
	        	}
	        }
        }
        return "";
    }

    private String getColumnLabel(Workbook workbook, int termId) {
    	List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();
    	for (MeasurementVariable variable : variables) {
    		if (variable.getTermId() == termId) {
    			return variable.getName();
    		}
    	}
    	return null;
    }
    
    private MeasurementData getMeasurementData(MeasurementRow row, String label) {
    	for (MeasurementData data : row.getDataList()) {
    		if (data.getLabel().equals(label)) {
    			return data;
    		}
    	}
    	return null;
    }
    
    private String getMeasurementDataValue(MeasurementRow row, String label) {
    	MeasurementData data = getMeasurementData(row, label);
    	if (data != null) {
    		return data.getValue();
    	}
    	return null;
    }
    
    private Integer getMeasurementDataValueInt(MeasurementRow row, String label) {
    	MeasurementData data = getMeasurementData(row, label);
    	if (data != null && NumberUtils.isNumber(data.getValue())) {
			return Integer.valueOf(data.getValue());
    	}
    	return null;
    }
    
    private Integer getExcelValueInt(HSSFRow row, int columnIndex) {
    	Cell cell = row.getCell(columnIndex);
    	String xlsStr = "";
    	if(cell.getCellType() == Cell.CELL_TYPE_STRING)
    		xlsStr = cell.getStringCellValue();
    	else			
    		xlsStr = String.valueOf((int)cell.getNumericCellValue());
    	if (NumberUtils.isNumber(xlsStr)) {
    		return Integer.valueOf(xlsStr);
    	}
    	return null;
    }
    
//    private String getCategoricalIdCellValue(String description, List<ValueReference> possibleValues) {
//    	for (ValueReference possibleValue : possibleValues) {
//    		if (description.equalsIgnoreCase(possibleValue.getDescription())) {
//    			return possibleValue.getId().toString();
//    		}
//    	}
//    	return "";
//    }
    
    private MeasurementRow findMeasurementRow(List<MeasurementRow> rows, int trial, int plot) {
    	if (rows != null) {
    		List<MeasurementVariable> variables = rows.get(0).getMeasurementVariables();
    		for (MeasurementRow row : rows) {
    			String trialInRow = WorkbookUtil.getValueByIdInRow(variables, TermId.TRIAL_INSTANCE_FACTOR.getId(), row);
    			String plotInRow = WorkbookUtil.getValueByIdInRow(variables, TermId.PLOT_NO.getId(), row);
    			if (plotInRow == null || "".equals(plotInRow)) {
    				plotInRow = WorkbookUtil.getValueByIdInRow(variables, TermId.PLOT_NNO.getId(), row);
    			}
    			if (trialInRow != null && trialInRow.equals(trial)
    					&& plotInRow != null && plotInRow.equals(plot)) {
    				
    				return row;
    			}
    		}
    	}
    	return null;
    }
    
    private List<MeasurementRow> filterObservationsByTrialInstance(HSSFWorkbook xlsBook, List<MeasurementRow> observations) {
    	HSSFSheet descriptionSheet = xlsBook.getSheetAt(0);
		int conditionRow = findRow(descriptionSheet, TEMPLATE_SECTION_CONDITION);
		int factorRow = findRow(descriptionSheet, TEMPLATE_SECTION_FACTOR);
		int trialRow = -1;
		String trialInstance = null;
		for (String label : PhenotypicType.TRIAL_ENVIRONMENT.getLabelList()) {
			trialRow = findRow(descriptionSheet, label);
			if (trialRow > 0 && trialRow > conditionRow && trialRow < factorRow) {
				break;
			}
		}
		if (trialRow > 0) {
			HSSFRow row = descriptionSheet.getRow(trialRow);
			HSSFCell cell = row.getCell(6);
			//trialInstance = cell.getStringCellValue();
			if(cell == null)
				trialInstance = "1";
			else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
				Double temp = Double.valueOf(cell.getNumericCellValue());
				
				trialInstance = Integer.toString(temp.intValue());
			}
			else if(cell.getCellType() == Cell.CELL_TYPE_STRING){
				trialInstance = cell.getStringCellValue();
			}
		}
		
		if (trialInstance != null) {
			return WorkbookUtil.filterObservationsByTrialInstance(observations, trialInstance);
		}
		return null;
    }
}
