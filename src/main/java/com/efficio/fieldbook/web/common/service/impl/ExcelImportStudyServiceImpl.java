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
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.operation.parser.WorkbookParser;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.util.PoiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(ExcelImportStudyServiceImpl.class);
    
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
	protected ValidationService validationService;
	
	@Resource
	private ResourceBundleMessageSource messageSource;
	
	private static String STUDY = "STUDY";
	private static String TRIAL = "TRIAL";
	
	@Resource
	private  com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
	
	@Override
	public ImportResult importWorkbook(Workbook workbook, String filename, OntologyService ontologyService, FieldbookService fieldbookMiddlewareService) throws WorkbookParserException {
		
		try {
			org.apache.poi.ss.usermodel.Workbook xlsBook = parseFile(filename);
			
			validate(xlsBook, workbook);

			WorkbookParser parser = new WorkbookParser();
	        Workbook descriptionWorkbook = parser.parseFile(new File(filename), false, false);
	        copyConditionsAndConstants(workbook);

	        Set<ChangeType> modes = new HashSet<ChangeType>();
			checkForAddedAndDeletedTraits(modes, xlsBook, workbook);
			
	        String trialInstanceNumber = getTrialInstanceNumber(workbook,xlsBook);
			List<MeasurementRow> trialObservations = filterObservationsByTrialInstance(workbook.getTrialObservations(), trialInstanceNumber);
			Map<Object, String> originalValueMap = new HashMap<Object, String>();
			importDescriptionSheetToWorkbook(workbook, trialInstanceNumber, descriptionWorkbook, trialObservations, originalValueMap);
			
			Map<String, MeasurementRow> rowsMap = createMeasurementRowsMap(workbook.getObservations(), trialInstanceNumber, workbook.isNursery());
			List<GermplasmChangeDetail> changeDetailsList = new ArrayList<GermplasmChangeDetail>();
			importDataToWorkbook(modes, xlsBook.getSheetAt(1), rowsMap, trialInstanceNumber, changeDetailsList, workbook);
			SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), true, ontologyService);

			try {
				validationService.validateObservationValues(workbook, trialInstanceNumber);
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
				WorkbookUtil.resetWorkbookObservations(workbook);
				return new ImportResult(e.getMessage());
			}
			String conditionsAndConstantsErrorMessage = "";
			try {
				validationService.validateConditionAndConstantValues(workbook, trialInstanceNumber);				
			} catch (MiddlewareQueryException e) {				
				conditionsAndConstantsErrorMessage = e.getMessage();
				WorkbookUtil.revertImportedConditionAndConstantsData(workbook);
				LOG.error(e.getMessage(), e);
			}
			
			ImportResult res = new ImportResult(modes, changeDetailsList);
			res.setConditionsAndConstantsErrorMessage(conditionsAndConstantsErrorMessage);
			return res;
			
			
		} catch (WorkbookParserException e) {
			WorkbookUtil.resetWorkbookObservations(workbook);
			throw e;

		} catch (Exception e) {
			throw new WorkbookParserException(e.getMessage(), e);
		}
	}
	
	private void copyConditionsAndConstants(Workbook workbook){
		
		if(workbook != null){
			List<MeasurementVariable> newVarList = new ArrayList<MeasurementVariable>();
			if(workbook.getConditions() != null){
				List<MeasurementVariable> conditionsCopy = new ArrayList<MeasurementVariable>();
				for(MeasurementVariable var : workbook.getConditions()){
					conditionsCopy.add(var.copy());
				}
				workbook.setImportConditionsCopy(conditionsCopy);
				newVarList.addAll(conditionsCopy);
			}
			if(workbook.getConstants() != null){
				List<MeasurementVariable> constantsCopy = new ArrayList<MeasurementVariable>();
				for(MeasurementVariable var : workbook.getConstants()){
					constantsCopy.add(var.copy());
				}
				workbook.setImportConstantsCopy(constantsCopy);
				newVarList.addAll(constantsCopy);
			}
			if(workbook.getTrialObservations() != null){
				List<MeasurementRow> trialObservationsCopy = new ArrayList<MeasurementRow>();
				for(MeasurementRow row : workbook.getTrialObservations()){
					trialObservationsCopy.add(row.copy(newVarList));
				}
				workbook.setImportTrialObservationsCopy(trialObservationsCopy);
			}
		}
	}
	
	protected org.apache.poi.ss.usermodel.Workbook parseFile(String filename) throws IOException  {
		org.apache.poi.ss.usermodel.Workbook readWorkbook = null;
		try{
			HSSFWorkbook xlsBook = new HSSFWorkbook(new FileInputStream(new File(filename)));
			readWorkbook = xlsBook;
		}catch(OfficeXmlFileException officeException){
			LOG.error(officeException.getMessage(), officeException);
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
	
	private void importDescriptionSheetToWorkbook(Workbook originalWorkbook, String trialInstanceNumber, Workbook descriptionWorkbook, 
			List<MeasurementRow> trialObservations, Map<Object, String> originalValueMap) throws MiddlewareQueryException{
		 Map<String, Object> variableMap = new HashMap<String, Object>();
		if(originalWorkbook != null && descriptionWorkbook != null){
			for(MeasurementVariable var : descriptionWorkbook.getConditions()){
				
				
				if(var.getLabel() != null && var.getLabel().equalsIgnoreCase(STUDY)){
					//study conditions
					//we get from the conditions
					setDataToMatchingMeasurementVariable(originalWorkbook.getConditions(), var, originalValueMap, variableMap);
					
				}else if(var.getLabel() != null && var.getLabel().equalsIgnoreCase(TRIAL)){
					//trial level conditions
					setDataToMatchingMeasurementData(trialObservations, var,  originalWorkbook.isNursery(), originalValueMap, variableMap);
				}
				
			}
			
			for(MeasurementVariable var : descriptionWorkbook.getConstants()){
				
				if(var.getLabel() != null && var.getLabel().equalsIgnoreCase(STUDY)){
					//study conditions
					//we get from the conditions
					setDataToMatchingMeasurementVariable(originalWorkbook.getConstants(), var, originalValueMap, variableMap);
					
				}else if(var.getLabel() != null && var.getLabel().equalsIgnoreCase(TRIAL)){
					//trial level conditions
					//we check if its in constants but not in trial observations
					for(MeasurementVariable constantsVar : originalWorkbook.getConstants()){
						boolean isFound = false;
						if(!trialObservations.isEmpty()){
							for(MeasurementRow temp : trialObservations){
								for(MeasurementData data : temp.getDataList()){
									if(data.getMeasurementVariable().getTermId() == constantsVar.getTermId()){
										isFound = true;
										break;
									}
								}
								if(isFound){
									break;
								}else{
									//we need to add it
									MeasurementData newData = new MeasurementData(constantsVar.getName(), "", false, constantsVar.getDataType(), constantsVar);
									temp.getDataList().add(newData);
								}
							}
						}
					}
					setDataToMatchingMeasurementData(trialObservations, var, originalWorkbook.isNursery(), originalValueMap, variableMap);
				}
			}
			setCorrectBreedingMethodInfo(variableMap);
			//this would set info to location (trial level variable)
			if (originalWorkbook.isNursery() && originalWorkbook.getTrialObservations() != null && !originalWorkbook.getTrialObservations().isEmpty()
  	    			&& originalWorkbook.getTrialConditions() != null && !originalWorkbook.getTrialConditions().isEmpty()) {
  				MeasurementVariable locationNameVar = WorkbookUtil.getMeasurementVariable(originalWorkbook.getTrialConditions(), TermId.TRIAL_LOCATION.getId());
  				if(locationNameVar != null){
  					//we set it to the trial observation level
  					
	    			for (MeasurementRow row : originalWorkbook.getTrialObservations()) {
	    				MeasurementData data = row.getMeasurementData(locationNameVar.getTermId());
	    				if(data != null){
	    					data.setValue(locationNameVar.getValue());
	    				}
	    			}
  					
  				}  				
  			}
			
		}		
	}
	
	private void setCorrectBreedingMethodInfo(Map<String, Object> variableMap){
		//we check for special pair variables here and ensure the name is correct
		try{
			//BM_ID, BM_METHOD_CODE			
			if(variableMap.containsKey(Integer.toString(TermId.BREEDING_METHOD_ID.getId()))){
				//we set the code and name accordingly
				Object tempObj = variableMap.get(Integer.toString(TermId.BREEDING_METHOD_ID.getId()));
				Object tempObjCode = variableMap.get(Integer.toString(TermId.BREEDING_METHOD_CODE.getId()));
				Object tempObjName = variableMap.get(Integer.toString(TermId.BREEDING_METHOD.getId()));
				if(tempObj instanceof MeasurementVariable){
					MeasurementVariable tempVar = (MeasurementVariable) tempObj;
					MeasurementVariable tempVarCode = tempObjCode != null ? (MeasurementVariable) tempObjCode : null;
					MeasurementVariable tempVarName = tempObjName != null ? (MeasurementVariable) tempObjName : null;
					if(tempVar.getValue() != null && !("").equalsIgnoreCase(tempVar.getValue()) && NumberUtils.isNumber(tempVar.getValue())){
						Method method = fieldbookMiddlewareService.getMethodById(Integer.parseInt(tempVar.getValue()));
						if(tempVarCode != null){
							//we set the proper code
							tempVarCode.setValue(method != null ? method.getMcode() : "");
						}
						if(tempVarName != null){
							tempVarName.setValue(method != null ? method.getMname() : "");
						}
					}else{
						//we set the bm code and bm to empty string
						if(tempVarCode != null){
							//we set the proper code
							tempVarCode.setValue("");
						}
						if(tempVarName != null){
							tempVarName.setValue("");
						}
					}
				}
			}else if(variableMap.containsKey(Integer.toString(TermId.BREEDING_METHOD_CODE.getId()))){
				//we just set the name
				Object tempObjCode = variableMap.get(Integer.toString(TermId.BREEDING_METHOD_CODE.getId()));
				Object tempObjName = variableMap.get(Integer.toString(TermId.BREEDING_METHOD.getId()));
				MeasurementVariable tempVarCode = tempObjCode != null ? (MeasurementVariable) tempObjCode : null;
				MeasurementVariable tempVarName = tempObjName != null ? (MeasurementVariable) tempObjName : null;
				if(tempVarCode != null && !("").equalsIgnoreCase(tempVarCode.getValue())){
					Method method = fieldbookMiddlewareService.getMethodByCode(tempVarCode.getValue());
					if(tempVarName != null){
						tempVarName.setValue(method != null ? method.getMname() : "");
					}
				}
			}
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
	}
	
	private void setDataToMatchingMeasurementVariable(List<MeasurementVariable> measurementVarList, MeasurementVariable var, 
			Map<Object, String> originalValueMap, Map<String, Object> variableMap){
		
		for(MeasurementVariable temp : measurementVarList){
			
			if(isMatchingPropertyScaleMethodLabel(var, temp) 
					&& WorkbookUtil.isConditionValidate(temp.getTermId())
					&& (temp != null && temp.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId())){					
			
				variableMap.put(Integer.toString(temp.getTermId()), temp);
				originalValueMap.put(temp, temp.getValue());
				
				try {
					temp.setPossibleValues(fieldbookService.getAllPossibleValues(temp.getTermId(), true));
				} catch (MiddlewareQueryException e) {
					LOG.error(e.getMessage(), e);
				}
				
				String xlsValue = "";
				if (temp != null && temp.getPossibleValues() != null
						&& !temp.getPossibleValues().isEmpty()) {													
					xlsValue = getCategoricalIdCellValue(var, temp);												
				} else {
					xlsValue = var.getValue();
				}
				temp.setValue(xlsValue);
				
				try{
					if(validationService.isValidValue(temp, xlsValue, true)){
						temp.setOperation(Operation.UPDATE);								
					}							
				}catch(Exception e){
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}

	protected boolean isMatchingPropertyScaleMethodLabel(MeasurementVariable var,
			MeasurementVariable temp) {
		return temp.getProperty().equalsIgnoreCase(var.getProperty()) &&
				temp.getScale().equalsIgnoreCase(var.getScale()) &&
				temp.getMethod().equalsIgnoreCase(var.getMethod()) &&
				temp.getLabel().equalsIgnoreCase(var.getLabel());
	}
	
	private void setDataToMatchingMeasurementData(List<MeasurementRow> trialObservations, MeasurementVariable var, boolean isNursery, 
			Map<Object, String> originalValueMap, Map<String, Object> variableMap){
		
		for(MeasurementRow temp : trialObservations){
			
			for(MeasurementData data : temp.getDataList()){
				
				MeasurementVariable origVar = data.getMeasurementVariable();
				
				if(origVar != null && isMatchingPropertyScaleMethodLabel(var, origVar) 
						&& (data != null && origVar != null 
							&& origVar.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId())){
					
					variableMap.put(Integer.toString(origVar.getTermId()), data);
					originalValueMap.put(data, data.getValue());
					
					try {
						origVar.setPossibleValues(fieldbookService.getAllPossibleValues(origVar.getTermId(), true));
					} catch (MiddlewareQueryException e) {
						LOG.error(e.getMessage(),e);
					}
					
					String xlsValue = getXlsValue(var, temp, data, origVar);
					data.setValue(xlsValue);
					if(isNursery){
						origVar.setValue(xlsValue);
					}
					
					try{
						if(validationService.isValidValue(origVar, xlsValue, true)){
							origVar.setOperation(Operation.UPDATE);																						
						}
					} catch (Exception e) {
						LOG.error(e.getMessage(),e);
					}
				}
			}
		}
	}

	protected String getXlsValue(MeasurementVariable var, MeasurementRow temp,
			MeasurementData data, MeasurementVariable origVar) {
		String xlsValue = "";
		if (temp != null && origVar.getPossibleValues() != null
				&& !origVar.getPossibleValues().isEmpty()) {													
			xlsValue = getCategoricalIdCellValue(var, origVar);
			if (origVar.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
				data.setcValueId(xlsValue);
			}
		} else {
			xlsValue = var.getValue();
		}
		return xlsValue;
	}

	protected String getCategoricalIdCellValue(MeasurementVariable var,
			MeasurementVariable origVar) {
		return ExportImportStudyUtil.getCategoricalIdCellValue(var.getValue(), origVar.getPossibleValues(), true);
	}
	
	protected void importDataToWorkbook(Set<ChangeType> modes, Sheet observationSheet, Map<String, MeasurementRow> rowsMap, 
			 String trialInstanceNumber,  List<GermplasmChangeDetail> changeDetailsList, Workbook workbook)
	throws MiddlewareQueryException, WorkbookParserException {
		
		List<MeasurementVariable> variablesFactors = workbook.getFactors();
		List<MeasurementRow> observations = workbook.getObservations();
		
		Map<Integer, MeasurementVariable> factorVariableMap = new HashMap<Integer, MeasurementVariable>();
		for(MeasurementVariable var : variablesFactors){
			factorVariableMap.put(var.getTermId(), var);
		}
		
		
		if (rowsMap != null && !rowsMap.isEmpty()) {
			
			workbook.setHasExistingDataOverwrite(false);
			int lastXlsRowIndex = observationSheet.getLastRowNum();
			String indexes = getColumnIndexesFromXlsSheet(observationSheet, variablesFactors, trialInstanceNumber);
			int desigColumn = findColumn(observationSheet, getColumnLabel(variablesFactors, TermId.DESIG.getId()));
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
						Cell desigCell = xlsRow.getCell(desigColumn);
						if(desigCell == null){
							//throw an error
							throw new WorkbookParserException("error.workbook.import.designation.empty.cell");
						}
						String newDesig = desigCell.getStringCellValue().trim();
						String originalGid = wRow.getMeasurementDataValue(TermId.GID.getId());
						String entryNumber = wRow.getMeasurementDataValue(TermId.ENTRY_NO.getId());
						String plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NO.getId());
						if (plotNumber == null || "".equalsIgnoreCase(plotNumber)) {
							plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NNO.getId());
						}
						
						if (originalDesig != null && !originalDesig.equalsIgnoreCase(newDesig)) {
							List<Integer> newGids = fieldbookMiddlewareService.getGermplasmIdsByName(newDesig);
							if (originalGid != null && newGids.contains(Integer.valueOf(originalGid))) {
								MeasurementData wData = wRow.getMeasurementData(TermId.DESIG.getId());
								wData.setValue(newDesig);
							} else {
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
								importDataCellValues(wData, xlsRow, j, workbook, factorVariableMap);
							}
						}
					}
				}
			}
			if(!rowsMap.isEmpty()){
				//meaning there are items in the original list, so there are items deleted
				modes.add(ChangeType.DELETED_ROWS);								
			}
			
		}
	}
	
	protected boolean hasCellValue(Cell cell){
		if (cell == null){
			return false;
		}else if(PoiUtil.getCellValue(cell) == null){
			return false;
		}else if ("".equals(PoiUtil.getCellValue(cell).toString())){		
			return false;
		}
		return true;
	}
	
	protected void importDataCellValues(MeasurementData wData, Row xlsRow, int columnIndex, 
			Workbook workbook, Map<Integer, MeasurementVariable> factorVariableMap){
		if (wData != null && wData.isEditable()) {
			Cell cell = xlsRow.getCell(columnIndex);
			String xlsValue = "";
			if(cell != null && hasCellValue(cell)){
				if (wData.getMeasurementVariable() != null && wData.getMeasurementVariable().getPossibleValues() != null
						&& !wData.getMeasurementVariable().getPossibleValues().isEmpty()) {
					
					wData.setAccepted(false);
					
					String tempVal = "";
					
					if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						double doubleVal = Double.valueOf(cell.getNumericCellValue());
						double intVal = Double.valueOf(cell.getNumericCellValue()).intValue();
						boolean getDoubleVal = false;
						if((doubleVal - intVal) > 0){
							getDoubleVal = true;
						}
						
						tempVal = String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
						if(getDoubleVal){
							tempVal = String.valueOf(Double.valueOf(cell.getNumericCellValue()));
						}
						xlsValue = ExportImportStudyUtil.getCategoricalIdCellValue(tempVal, 
								wData.getMeasurementVariable().getPossibleValues(), true);
					} else {
						tempVal = cell.getStringCellValue();
						xlsValue = ExportImportStudyUtil.getCategoricalIdCellValue(cell.getStringCellValue(), wData.getMeasurementVariable().getPossibleValues(), true);
					}
					Integer termId = wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : new Integer(0);
					if(!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)){
						workbook.setHasExistingDataOverwrite(true);
					}
					
					if (wData.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() && !xlsValue.equals(tempVal)) {
						wData.setcValueId(xlsValue);
					}else{
						wData.setcValueId(null);
					}
						
				} else {
					
					if(wData.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()){
						wData.setAccepted(false);	
					}
					if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
						xlsValue = getRealNumericValue(cell);																						
					} else {
						xlsValue = cell.getStringCellValue();
					}
					Integer termId = wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : new Integer(0);
					if(!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)){
						workbook.setHasExistingDataOverwrite(true);
					}
				}
				wData.setValue(xlsValue);
			}
		}
	}
	
	private String getRealNumericValue(Cell cell){
		String realValue = "";
		if (cell != null) {
			if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				Double doubleVal = Double.valueOf(cell.getNumericCellValue());
				Integer intVal = Integer.valueOf(doubleVal.intValue());
				if(Double.parseDouble(intVal.toString()) == doubleVal.doubleValue()) {
					realValue = intVal.toString();
				} else {
					realValue = doubleVal.toString();	
				}
			} else {
				realValue = cell.getStringCellValue();
			}
		}
		return realValue;
	}
	
	private void validate(org.apache.poi.ss.usermodel.Workbook xlsBook, Workbook workbook) 
			throws WorkbookParserException, MiddlewareQueryException {
		
		// partially parse the file to parse the description sheet only at first
		validateNumberOfSheets(xlsBook);
		
		Sheet descriptionSheet = xlsBook.getSheetAt(0);
		validateDescriptionSheetFirstCell(descriptionSheet);
		validateSections(descriptionSheet);
		
		Sheet observationSheet = xlsBook.getSheetAt(1);
		validateRequiredObservationColumns(observationSheet, workbook);
		validateVariates(xlsBook, workbook);
	}
	
	protected void validateNumberOfSheets(org.apache.poi.ss.usermodel.Workbook xlsBook) throws WorkbookParserException {
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
	
	protected void validateRequiredObservationColumns(Sheet obsSheet, Workbook workbook) throws WorkbookParserException {
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
                if (cell != null && cell.getStringCellValue() != null
                	 && cell.getStringCellValue().equalsIgnoreCase(cellValue)) {
                	return i;
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
	            } else if (cell.getStringCellValue().equalsIgnoreCase(cellValue)) {
	                return i;
	            }
	        }
        }
        return result;
    }

    protected String findColumns(Sheet sheet, String... cellValue) throws WorkbookParserException{
    	List<String> cellValueList = Arrays.asList(cellValue);
    	String result = StringUtils.join(cellValue, ",");
    	
        if (cellValue != null) {
        	Row row = sheet.getRow(0);
	        int cells = row.getLastCellNum();
	        for (int i = 0; i < cells; i++) {
	            Cell cell = row.getCell(i);
	            if (cell == null){
	                   throw new WorkbookParserException("error.workbook.import.missing.columns.import.file");
	            } else if (cellValueList.contains(cell.getStringCellValue())) {
	            	result = result.replace(cell.getStringCellValue(), String.valueOf(i));
	            }
	        }
        }
        return result;
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
    
	protected String getTrialInstanceNumber(Workbook workbook,
			org.apache.poi.ss.usermodel.Workbook xlsBook)
			throws WorkbookParserException {
		
		String trialInstanceNumber = (workbook != null && workbook.isNursery()) ? "1" : getTrialInstanceNumber(xlsBook);
		if (trialInstanceNumber == null || "".equalsIgnoreCase(trialInstanceNumber)) {
			throw new WorkbookParserException("error.workbook.import.missing.trial.instance");
		}
		
		return trialInstanceNumber;
	}
	
    protected String getTrialInstanceNumber(org.apache.poi.ss.usermodel.Workbook xlsBook) {
    	Sheet descriptionSheet = xlsBook.getSheetAt(0);
		int conditionRow = findRow(descriptionSheet, TEMPLATE_SECTION_CONDITION);
		int factorRow = findRow(descriptionSheet, TEMPLATE_SECTION_FACTOR);
		
		String trialInstance = null;
		
		for(int indexRow = conditionRow + 1 ; indexRow < factorRow ; indexRow++){
			Integer stdVarId = null;
			try{
				Row row = descriptionSheet.getRow(indexRow);
				//we need to check the PSM-R
				Cell propertyCell = row.getCell(2);
				Cell scaleCell = row.getCell(3);
				Cell methodCell = row.getCell(4);
				Cell labelCell = row.getCell(7);
				
				stdVarId = null;
				
				if(isPropertyScaleMethodLabelCellNotNull(propertyCell, scaleCell,methodCell, labelCell) 
						&& isPropertyScaleMethodLabelCellHasStringValue(propertyCell, scaleCell, methodCell, labelCell)){
					//we get the corresponding standard variable id
					stdVarId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(propertyCell.getStringCellValue(), scaleCell.getStringCellValue(), methodCell.getStringCellValue(), PhenotypicType.getPhenotypicTypeForLabel(labelCell.getStringCellValue()));																						
				}
			
				if(stdVarId != null && stdVarId.intValue() == TermId.TRIAL_INSTANCE_FACTOR.getId()){
					Cell cell = row.getCell(6);
					if(cell == null) {
						trialInstance = "1";
					} else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
						Double temp = Double.valueOf(cell.getNumericCellValue());
						
						trialInstance = Integer.toString(temp.intValue());
					} else if(cell.getCellType() == Cell.CELL_TYPE_STRING){
						trialInstance = cell.getStringCellValue();
					}
					break;
				}
			}catch(MiddlewareQueryException e){
				// no matching
				//just itereate the possible rows again
				LOG.error(e.getMessage(), e);
			}catch(Exception e){
				//cell might be null
				//we won't throw error, since we need to check other variable
				LOG.error(e.getMessage(), e);
			}
		}
		
		return trialInstance;
    }

	protected boolean isPropertyScaleMethodLabelCellHasStringValue(Cell propertyCell,
			Cell scaleCell, Cell methodCell, Cell labelCell) {
		return propertyCell.getStringCellValue() != null 
				&& scaleCell.getStringCellValue() != null 
				&& methodCell.getStringCellValue() != null 
				&& labelCell.getStringCellValue() != null;
	}

	protected boolean isPropertyScaleMethodLabelCellNotNull(Cell propertyCell,
			Cell scaleCell, Cell methodCell, Cell labelCell) {
		return propertyCell != null && scaleCell != null && methodCell != null && labelCell != null;
	}       

    protected List<MeasurementRow> filterObservationsByTrialInstance(List<MeasurementRow> observations, String trialInstanceNumber) {
		if (trialInstanceNumber != null) {
			return WorkbookUtil.filterObservationsByTrialInstance(observations, trialInstanceNumber);
		}
		return new ArrayList<MeasurementRow>();
    }
    
    protected Map<String, MeasurementRow> createMeasurementRowsMap(List<MeasurementRow> observations, String instanceNumber, boolean isNursery) {
    	Map<String, MeasurementRow> map = new HashMap<String, MeasurementRow>();
    	List<MeasurementRow> newObservations = new ArrayList<MeasurementRow>();
    	if(!isNursery){
    		if(instanceNumber != null && !"".equalsIgnoreCase(instanceNumber)){
    			newObservations =  WorkbookUtil.filterObservationsByTrialInstance(observations, instanceNumber);
    		}	
    	} else {
    		newObservations = observations;
    	}
    	
    	if (newObservations != null && !newObservations.isEmpty()) {
    		for (MeasurementRow row : newObservations) {
    			map.put(row.getKeyIdentifier(), row);
    		}
    	}
    	return map;
    }
    
    protected String getColumnIndexesFromXlsSheet(Sheet observationSheet, List<MeasurementVariable> variables, String trialInstanceNumber) throws WorkbookParserException{
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
    			if (!NumberUtils.isNumber(index) || "-1".equalsIgnoreCase(index)) {
    				return null;
    			}
    		}
    		return indexes;
    	}
    	return null;
    }
    
	private String getKeyIdentifierFromXlsRow(Row xlsRow, String indexes) throws WorkbookParserException{
    	if (indexes != null) {
	    	String[] indexArray = indexes.split(",");
	    	//plot no
	    	Cell plotCell = xlsRow.getCell(Integer.valueOf(indexArray[1]));
	    	//entry no
	    	Cell entryCell = xlsRow.getCell(Integer.valueOf(indexArray[2])); 
	    	
	    	if(plotCell == null){
	    		throw new WorkbookParserException("error.workbook.import.plot.no.empty.cell");
	    	} else if(entryCell == null){
	    		throw new WorkbookParserException("error.workbook.import.entry.no.empty.cell");
	    	}
	    	
	    	return indexArray[0] 
	    			+ "-" + getRealNumericValue(plotCell) 
	    			+ "-" +getRealNumericValue(entryCell); 
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
                	//new variates
                	if (mvarId == null) {                 		
                		MeasurementVariable mvar = null;
                		try {
                			mvar = getMeasurementVariable(row);
                		} catch(MiddlewareQueryException e) {
                			LOG.error(e.getMessage(), e);
                			throw new WorkbookParserException(messageSource.getMessage("error.import.variate.duplicate.psmr", 
                					new String[] {traitLabel}, LocaleContextHolder.getLocale()));
                		}
                		if (mvar == null) {
                			throw new WorkbookParserException(messageSource.getMessage("error.import.variate.does.not.exist", 
                					new String[] {traitLabel}, LocaleContextHolder.getLocale()));
                		} else if (WorkbookUtil.getMeasurementVariable(workbookVariates, mvar.getTermId()) != null) {
                			throw new WorkbookParserException(messageSource.getMessage("error.import.variate.exists.in.study", 
                					new String[] {traitLabel}, LocaleContextHolder.getLocale()));
                		} else {
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
				if (cell.getStringCellValue() != null && !"".equalsIgnoreCase(cell.getStringCellValue())) {
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
