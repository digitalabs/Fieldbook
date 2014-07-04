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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.service.ExcelImportStudyService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Service
public class ExcelImportStudyServiceImpl implements ExcelImportStudyService {

	private static final String TEMPLATE_DESCRIPTION_SHEET_FIRST_VALUE = "STUDY";
	private static final String TEMPLATE_SECTION_CONDITION = "CONDITION";
	private static final String TEMPLATE_SECTION_FACTOR = "FACTOR";
	private static final String TEMPLATE_SECTION_CONSTANT = "CONSTANT";
	private static final String TEMPLATE_SECTION_VARIATE = "VARIATE";
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_DESCRIPTION = 1;
	private static final int COLUMN_PROPERTY = 2;
	private static final int COLUMN_SCALE = 3;
	private static final int COLUMN_METHOD = 4;
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Resource 
	private OntologyService ontologyService;
	
	@Resource
	private ValidationService validationService;
	
	@Resource
	private ResourceBundleMessageSource messageSource;
	
	@Override
	public ImportResult importWorkbook(Workbook workbook, String filename, OntologyService ontologyService, FieldbookService fieldbookMiddlewareService) throws WorkbookParserException {
		
		try {
			org.apache.poi.ss.usermodel.Workbook xlsBook = parseFile(filename);
			
			validateNumberOfSheets(xlsBook);
			Sheet descriptionSheet = xlsBook.getSheetAt(0);
			validateDescriptionSheetFirstCell(descriptionSheet);
			validateSections(descriptionSheet);

			String trialInstanceNumber = (workbook != null && workbook.isNursery()) ? "1" : getTrialInstanceNumber(xlsBook);
			if (trialInstanceNumber == null || "".equals(trialInstanceNumber)) {
				if (!workbook.isNursery()) {
					throw new WorkbookParserException("error.workbook.import.missing.trial.instance");
				}
				else {
					trialInstanceNumber = "1";
				}
			}
			List<MeasurementRow> observations = filterObservationsByTrialInstance(xlsBook, workbook.getObservations(), trialInstanceNumber);
			List<MeasurementRow> trialObservations = filterObservationsByTrialInstance(xlsBook, workbook.getTrialObservations(), trialInstanceNumber);

			validate(xlsBook, workbook, observations);			
			
			List<GermplasmChangeDetail> changeDetailsList = new ArrayList<GermplasmChangeDetail>();
			
			Set<ChangeType> modes = new HashSet<ChangeType>();
			checkForAddedAndDeletedTraits(modes, xlsBook, workbook);
			Map<String, MeasurementRow> rowsMap = createMeasurementRowsMap(workbook.getObservations());
			
			importDataToWorkbook(modes, xlsBook, rowsMap, workbook.getFactors(), trialInstanceNumber, workbook.getObservations(), changeDetailsList);
			SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), true, ontologyService);

			try {
				validationService.validateObservationValues(workbook);
			} catch (MiddlewareQueryException e) {
				WorkbookUtil.resetWorkbookObservations(workbook);
				return new ImportResult(e.getMessage());
			}
			
			importTrialToWorkbook(xlsBook, trialObservations);
			return new ImportResult(modes, changeDetailsList);
			
			
		} catch (WorkbookParserException e) {
			WorkbookUtil.resetWorkbookObservations(workbook);
			throw e;

		} catch (Exception e) {
			throw new WorkbookParserException(e.getMessage(), e);
		}
	}
	
	private org.apache.poi.ss.usermodel.Workbook parseFile(String filename) throws Exception {
		org.apache.poi.ss.usermodel.Workbook readWorkbook = null;
		try{
			HSSFWorkbook xlsBook = new HSSFWorkbook(new FileInputStream(new File(filename))); //WorkbookFactory.create(new FileInputStream(new File(filename)));
			readWorkbook = xlsBook;
		}catch(OfficeXmlFileException officeException){
			try {
				XSSFWorkbook xlsxBook = new XSSFWorkbook(new FileInputStream(new File(filename)));
				readWorkbook = xlsxBook;
			} catch (FileNotFoundException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			} 
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		return readWorkbook;
	}
	
	private void importDataToWorkbook(Set<ChangeType> modes, org.apache.poi.ss.usermodel.Workbook xlsBook, Map<String, MeasurementRow> rowsMap, 
			List<MeasurementVariable> variables, String trialInstanceNumber, List<MeasurementRow> observations, List<GermplasmChangeDetail> changeDetailsList)
	throws MiddlewareQueryException, WorkbookParserException {
		
		if (rowsMap != null && !rowsMap.isEmpty()) {
			Sheet observationSheet = xlsBook.getSheetAt(1);
			int lastXlsRowIndex = observationSheet.getLastRowNum();
			String indexes = getColumnIndexesFromXlsSheet(observationSheet, variables, trialInstanceNumber);
			int desigColumn = findColumn(observationSheet, getColumnLabel(variables, TermId.DESIG.getId()));
			Row headerRow = observationSheet.getRow(0);
			int lastXlsColIndex = headerRow.getLastCellNum();
			for (int i = 1; i <= lastXlsRowIndex; i++) {
				Row xlsRow = observationSheet.getRow(i);
				String key = getKeyIdentifierFromXlsRow(xlsRow, indexes);
				if (key != null) {
					MeasurementRow wRow = rowsMap.get(key);
					if (wRow == null) {
						modes.add(ChangeType.ADDED_ROWS);
					} else if (wRow != null) {
						rowsMap.remove(key);
						
						String originalDesig = wRow.getMeasurementDataValue(TermId.DESIG.getId());
						String newDesig = xlsRow.getCell(desigColumn).getStringCellValue().trim();
						String originalGid = wRow.getMeasurementDataValue(TermId.GID.getId());
						String entryNumber = wRow.getMeasurementDataValue(TermId.ENTRY_NO.getId());
						String plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NO.getId());
						if (plotNumber == null || "".equals(plotNumber)) {
							plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NNO.getId());
						}
						
						if (originalDesig != null && !originalDesig.equalsIgnoreCase(newDesig)) {
							List<Integer> newGids = fieldbookMiddlewareService.getGermplasmIdsByName(newDesig);
							if (originalGid != null && newGids.contains(Integer.valueOf(originalGid))) {
								MeasurementData wData = wRow.getMeasurementData(TermId.DESIG.getId());
								wData.setValue(newDesig);
							} 
							else {
								int index = observations.indexOf(wRow);
								GermplasmChangeDetail changeDetail = new GermplasmChangeDetail(index, originalDesig, originalGid, newDesig, "", 
										trialInstanceNumber, entryNumber, plotNumber);
								if (newGids != null && !newGids.isEmpty()) {
									changeDetail.setMatchingGids(newGids);
								}
								changeDetailsList.add(changeDetail);
							}
						}
						
						for (int j = 0; j <= lastXlsColIndex; j++) {
							Cell headerCell = headerRow.getCell(j);
							if (headerCell != null) {
	
								MeasurementData wData = wRow.getMeasurementData(headerCell.getStringCellValue());
								if (wData != null && wData.isEditable()) {
									Cell cell = xlsRow.getCell(j);
									String xlsValue = "";
									if(cell != null){
										if (wData.getMeasurementVariable() != null && wData.getMeasurementVariable().getPossibleValues() != null
												&& !wData.getMeasurementVariable().getPossibleValues().isEmpty()) {
											
											if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
												xlsValue = ExportImportStudyUtil.getCategoricalIdCellValue(String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue()), 
														wData.getMeasurementVariable().getPossibleValues(), true);
											}
											else {
												xlsValue = ExportImportStudyUtil.getCategoricalIdCellValue(cell.getStringCellValue(), wData.getMeasurementVariable().getPossibleValues(), true);
											}
											
											if (wData.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
												wData.setcValueId(xlsValue);
											}
										} 
										else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
											xlsValue = getRealNumericValue(cell);																						
										}
										else {
											xlsValue = cell.getStringCellValue();
										}
										wData.setValue(xlsValue);
									}
									else {
										wData.setcValueId(null);
										wData.setValue(null);
									}
								}
							}
						}
					}
				}
			}
			if(rowsMap.size() != 0){
				//meaning there are items in the original list, so there are items deleted
				modes.add(ChangeType.DELETED_ROWS);								
			}
		}
	}
	
	private String getRealNumericValue(Cell cell){
		String realValue = "";
		if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
			Double doubleVal = Double.valueOf(cell.getNumericCellValue());
			Integer intVal = Integer.valueOf(doubleVal.intValue());
			if(Double.parseDouble(intVal.toString()) == doubleVal.doubleValue()){
				realValue = intVal.toString();
			}else{
				realValue = doubleVal.toString();	
			}
		}else{
			realValue = cell.getStringCellValue();
		}
		return realValue;
	}

	private void importTrialToWorkbook(org.apache.poi.ss.usermodel.Workbook xlsBook, List<MeasurementRow> observations) {
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
	
	private void validate(org.apache.poi.ss.usermodel.Workbook xlsBook, Workbook workbook, List<MeasurementRow> observations) 
			throws WorkbookParserException, MiddlewareQueryException {
		
		Sheet observationSheet = xlsBook.getSheetAt(1);
		
		validateRequiredObservationColumns(observationSheet, workbook);
		validateVariates(xlsBook, workbook);
	}
	
	private void validateNumberOfSheets(org.apache.poi.ss.usermodel.Workbook xlsBook) throws WorkbookParserException {
		if (xlsBook.getNumberOfSheets() != 2) {
			throw new WorkbookParserException("error.workbook.import.invalidNumberOfSheets");
		}
	}
	
	private void validateDescriptionSheetFirstCell(Sheet descriptionSheet) throws WorkbookParserException {
		if (!TEMPLATE_DESCRIPTION_SHEET_FIRST_VALUE.equalsIgnoreCase(descriptionSheet.getRow(0).getCell(0).getStringCellValue())) {
			throw new WorkbookParserException("error.workbook.import.invalidFormatDescriptionSheet");
		}
	}
	
	private void validateSections(Sheet descriptionSheet) throws WorkbookParserException {
		int conditionRow = findRow(descriptionSheet, TEMPLATE_SECTION_CONDITION);
		int factorRow = findRow(descriptionSheet, TEMPLATE_SECTION_FACTOR);
		int constantRow = findRow(descriptionSheet, TEMPLATE_SECTION_CONSTANT);
		int variateRow = findRow(descriptionSheet, TEMPLATE_SECTION_VARIATE);
		if (conditionRow <= 0 || factorRow <= conditionRow 
				|| constantRow <= conditionRow || variateRow <= conditionRow) {
			throw new WorkbookParserException("error.workbook.import.invalidSections");
		}			
	}
	
	private void validateRequiredObservationColumns(Sheet obsSheet, Workbook workbook) throws WorkbookParserException {
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

    private int findRow(Sheet sheet, String cellValue) {
        int result = 0;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(0);
                if (cell != null && cell.getStringCellValue() != null) {
                    if (cell.getStringCellValue().equals(cellValue)) {
                        return i;
                    }
                }
            }
        }

        return result;
    }

    private int findColumn(Sheet sheet, String cellValue) throws WorkbookParserException{
        int result = -1;
        if (cellValue != null) {
	        Row row = sheet.getRow(0); //Encabezados
	        int cells = row.getLastCellNum();
	        for (int i = 0; i < cells; i++) {
	            Cell cell = row.getCell(i);
	            if (cell == null){
                   throw new WorkbookParserException("error.workbook.import.missing.columns.import.file");
	            } else if (cell.getStringCellValue().equals(cellValue)) {
	                return i;
	            }
	        }
        }
        return result;
    }

    private String findColumns(Sheet sheet, String... cellValue) throws WorkbookParserException{
    	List<String> cellValueList = Arrays.asList(cellValue);
    	String result = StringUtils.join(cellValue, ",");
    	
        if (cellValue != null) {
	        Row row = sheet.getRow(0); //Encabezados
	        int cells = row.getLastCellNum();
	        for (int i = 0; i < cells; i++) {
	            Cell cell = row.getCell(i);
	            if (cell == null){
	                   throw new WorkbookParserException("error.workbook.import.missing.columns.import.file");
	            }
	            else if (cellValueList.contains(cell.getStringCellValue())) {
	            	result = result.replace(cell.getStringCellValue(), String.valueOf(i));
	            }
	        }
        }
        return result;
    }

    private String findValueFromDescriptionSheet(org.apache.poi.ss.usermodel.Workbook workbook, String cellValue) {
    	Sheet sheet = workbook.getSheetAt(0);
        if (cellValue != null) {
	        int lastRow = sheet.getLastRowNum();
	        for (int i = 0; i < lastRow; i++) {
	        	Row row = sheet.getRow(i);
	        	if (row != null) {
		        	Cell cell = row.getCell(0);
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
    	return getColumnLabel(variables, termId);
    }
    
    private String getColumnLabel(List<MeasurementVariable> variables, int termId) {
    	for (MeasurementVariable variable : variables) {
    		if (variable.getTermId() == termId) {
    			return variable.getName();
    		}
    	}
    	return null;
    }
    
    private String getTrialInstanceNumber(org.apache.poi.ss.usermodel.Workbook xlsBook) {
    	Sheet descriptionSheet = xlsBook.getSheetAt(0);
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
			Row row = descriptionSheet.getRow(trialRow);
			Cell cell = row.getCell(6);
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
		return trialInstance;
    }

    private List<MeasurementRow> filterObservationsByTrialInstance(org.apache.poi.ss.usermodel.Workbook xlsBook, List<MeasurementRow> observations, String trialInstanceNumber) {
		if (trialInstanceNumber != null) {
			return WorkbookUtil.filterObservationsByTrialInstance(observations, trialInstanceNumber);
		}
		return null;
    }
    
    private Map<String, MeasurementRow> createMeasurementRowsMap(List<MeasurementRow> observations) {
    	Map<String, MeasurementRow> map = new HashMap<String, MeasurementRow>();
    	if (observations != null && !observations.isEmpty()) {
    		for (MeasurementRow row : observations) {
    			map.put(row.getKeyIdentifier(), row);
    		}
    	}
    	return map;
    }
    
    private String getColumnIndexesFromXlsSheet(Sheet observationSheet, List<MeasurementVariable> variables, String trialInstanceNumber) throws WorkbookParserException{
    	String plotLabel = null, entryLabel = null;
    	for (MeasurementVariable variable : variables) {
    		if (variable.getTermId() == TermId.PLOT_NO.getId() || variable.getTermId() == TermId.PLOT_NNO.getId()) {
    			plotLabel = variable.getName();
    		} else if (variable.getTermId() == TermId.ENTRY_NO.getId()) {
    			entryLabel = variable.getName();
    		}
    	}
    	if (plotLabel != null && entryLabel != null) {
    		String indexes = findColumns(observationSheet, trialInstanceNumber, plotLabel, entryLabel);
    		for (String index : indexes.split(",")) {
    			if (!NumberUtils.isNumber(index) || "-1".equals(index)) {
    				return null;
    			}
    		}
    		return indexes;
    	}
    	return null;
    }
    
    private String getKeyIdentifierFromXlsRow(Row xlsRow, String indexes) {
    	if (indexes != null) {
	    	String[] indexArray = indexes.split(",");
	    	
	    	return indexArray[0] 
	    			+ "-" + getRealNumericValue(xlsRow.getCell(Integer.valueOf(indexArray[1])))
	    			+ "-" +getRealNumericValue(xlsRow.getCell(Integer.valueOf(indexArray[2])));
    	} 
    	return null;
    }
    
   
    private void validateVariates(org.apache.poi.ss.usermodel.Workbook xlsBook, Workbook workbook) throws WorkbookParserException {
    	Sheet descriptionSheet = xlsBook.getSheetAt(0);
		int variateRow = findRow(descriptionSheet, TEMPLATE_SECTION_VARIATE);
		List<MeasurementVariable> workbookVariates = workbook.getVariates();
		for (int i = variateRow+1; i <= descriptionSheet.getLastRowNum(); i++) {
            Row row = descriptionSheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(0);
                if (cell != null && cell.getStringCellValue() != null) {
                	String traitLabel = cell.getStringCellValue();
                	Integer mvarId = WorkbookUtil.getMeasurementVariableId(workbookVariates, traitLabel);
                	if (mvarId == null) { //new variates
                		MeasurementVariable mvar = null;
                		try {
                			mvar = getMeasurementVariable(row);
                		} catch(MiddlewareQueryException e) {
                			throw new WorkbookParserException(messageSource.getMessage("error.import.variate.duplicate.psmr", 
                					new String[] {traitLabel}, LocaleContextHolder.getLocale()));
                		}
                		if (mvar == null) {
                			throw new WorkbookParserException(messageSource.getMessage("error.import.variate.does.not.exist", 
                					new String[] {traitLabel}, LocaleContextHolder.getLocale()));
                		}
                		else if (WorkbookUtil.getMeasurementVariable(workbookVariates, mvar.getTermId()) != null) {
                			throw new WorkbookParserException(messageSource.getMessage("error.import.variate.exists.in.study", 
                					new String[] {traitLabel}, LocaleContextHolder.getLocale()));
                		}
                		else {
                			//valid
                			WorkbookUtil.addVariateToObservations(mvar, workbook.getObservations());
                		}
                	}
                	
                }
            }
		}
    }
    
    private MeasurementVariable getMeasurementVariable(Row row) throws MiddlewareQueryException {
    	String property = row.getCell(COLUMN_PROPERTY).getStringCellValue();
    	String scale = row.getCell(COLUMN_SCALE).getStringCellValue();
    	String method = row.getCell(COLUMN_METHOD).getStringCellValue();
    	MeasurementVariable mvar = fieldbookMiddlewareService.getMeasurementVariableByPropertyScaleMethodAndRole(property, scale, method, PhenotypicType.VARIATE);
    	if (mvar != null) {
	    	mvar.setName(row.getCell(COLUMN_NAME).getStringCellValue());
	    	mvar.setDescription(row.getCell(COLUMN_DESCRIPTION).getStringCellValue());
    	}
    	return mvar;
    }
    
    private void checkForAddedAndDeletedTraits(Set<ChangeType> modes, org.apache.poi.ss.usermodel.Workbook xlsBook, Workbook workbook) {
    	List<String> xlsVariates = new ArrayList<String>();
    	Sheet descriptionSheet = xlsBook.getSheetAt(0);
		int variateRow = findRow(descriptionSheet, TEMPLATE_SECTION_VARIATE);
		for (int i = variateRow + 1; i <= descriptionSheet.getLastRowNum(); i++) {
			if (descriptionSheet.getRow(i) != null && descriptionSheet.getRow(i).getCell(0) != null) {
				Cell cell = descriptionSheet.getRow(i).getCell(0);
				if (cell.getStringCellValue() != null && !"".equals(cell.getStringCellValue())) {
					xlsVariates.add(cell.getStringCellValue());
				}
			}
		}
		List<String> wbVariates = new ArrayList<String>();
		for (MeasurementVariable variate : workbook.getVariates()) {
			wbVariates.add(variate.getName());
		}
		for (int i = 0; i < xlsVariates.size(); i++) {
			String xlsVariate = xlsVariates.get(i);
			for (String wbVariate : wbVariates) {
				if (xlsVariate.equalsIgnoreCase(wbVariate)) {
					xlsVariates.remove(xlsVariate);
					wbVariates.remove(wbVariate);
					i--;
					break;
				}
			}
		}
		if (!xlsVariates.isEmpty()) {
			modes.add(ChangeType.ADDED_TRAITS);
		}
		if (!wbVariates.isEmpty()) {
			modes.add(ChangeType.DELETED_TRAITS);
		}
    }
}
