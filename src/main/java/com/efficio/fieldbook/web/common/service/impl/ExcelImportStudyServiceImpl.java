/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
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
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
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
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.service.ExcelImportStudyService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Service
@Transactional
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
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public ImportResult importWorkbook(final Workbook workbook, final String filename, final OntologyService ontologyService,
			final FieldbookService fieldbookMiddlewareService) throws WorkbookParserException {

		try {
			final org.apache.poi.ss.usermodel.Workbook xlsBook = this.parseFile(filename);

			this.validate(xlsBook, workbook);

			final WorkbookParser parser = new WorkbookParser();
			final Workbook descriptionWorkbook = parser.parseFile(new File(filename), false, false);
			this.copyConditionsAndConstants(workbook);

			final Set<ChangeType> modes = new HashSet<ChangeType>();
			this.checkForAddedAndDeletedTraits(modes, xlsBook, workbook);

			final String trialInstanceNumber = this.getTrialInstanceNumber(workbook, xlsBook);
			final List<MeasurementRow> trialObservations =
					this.filterObservationsByTrialInstance(workbook.getTrialObservations(), trialInstanceNumber);
			final Map<Object, String> originalValueMap = new HashMap<Object, String>();
			this.importDescriptionSheetToWorkbook(workbook, trialInstanceNumber, descriptionWorkbook, trialObservations, originalValueMap);

			final Map<String, MeasurementRow> rowsMap =
					this.createMeasurementRowsMap(workbook.getObservations(), trialInstanceNumber, workbook.isNursery());
			final List<GermplasmChangeDetail> changeDetailsList = new ArrayList<GermplasmChangeDetail>();
			this.importDataToWorkbook(modes, xlsBook.getSheetAt(1), rowsMap, trialInstanceNumber, changeDetailsList, workbook);
			SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), true, ontologyService);

			try {
				this.validationService.validateObservationValues(workbook, trialInstanceNumber);
			} catch (final MiddlewareQueryException e) {
				ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
				WorkbookUtil.resetWorkbookObservations(workbook);
				return new ImportResult(e.getMessage());
			}
			String conditionsAndConstantsErrorMessage = "";
			try {
				this.validationService.validateConditionAndConstantValues(workbook, trialInstanceNumber);
			} catch (final MiddlewareQueryException e) {
				conditionsAndConstantsErrorMessage = e.getMessage();
				WorkbookUtil.revertImportedConditionAndConstantsData(workbook);
				ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
			}

			final ImportResult res = new ImportResult(modes, changeDetailsList);
			res.setConditionsAndConstantsErrorMessage(conditionsAndConstantsErrorMessage);
			return res;

		} catch (final WorkbookParserException e) {
			WorkbookUtil.resetWorkbookObservations(workbook);
			throw e;

		} catch (final Exception e) {
			throw new WorkbookParserException(e.getMessage(), e);
		}
	}

	private void copyConditionsAndConstants(final Workbook workbook) {

		if (workbook != null) {
			final List<MeasurementVariable> newVarList = new ArrayList<MeasurementVariable>();
			if (workbook.getConditions() != null) {
				final List<MeasurementVariable> conditionsCopy = new ArrayList<MeasurementVariable>();
				for (final MeasurementVariable var : workbook.getConditions()) {
					conditionsCopy.add(var.copy());
				}
				workbook.setImportConditionsCopy(conditionsCopy);
				newVarList.addAll(conditionsCopy);
			}
			if (workbook.getConstants() != null) {
				final List<MeasurementVariable> constantsCopy = new ArrayList<MeasurementVariable>();
				for (final MeasurementVariable var : workbook.getConstants()) {
					constantsCopy.add(var.copy());
				}
				workbook.setImportConstantsCopy(constantsCopy);
				newVarList.addAll(constantsCopy);
			}
			if (workbook.getTrialObservations() != null) {
				final List<MeasurementRow> trialObservationsCopy = new ArrayList<MeasurementRow>();
				for (final MeasurementRow row : workbook.getTrialObservations()) {
					trialObservationsCopy.add(row.copy(newVarList));
				}
				workbook.setImportTrialObservationsCopy(trialObservationsCopy);
			}
		}
	}

	protected org.apache.poi.ss.usermodel.Workbook parseFile(final String filename) throws IOException {
		org.apache.poi.ss.usermodel.Workbook readWorkbook = null;
		try {
			final HSSFWorkbook xlsBook = new HSSFWorkbook(new FileInputStream(new File(filename)));
			readWorkbook = xlsBook;
		} catch (final OfficeXmlFileException officeException) {
			ExcelImportStudyServiceImpl.LOG.error(officeException.getMessage(), officeException);
			try {
				final XSSFWorkbook xlsxBook = new XSSFWorkbook(new FileInputStream(new File(filename)));
				readWorkbook = xlsxBook;
			} catch (final FileNotFoundException e) {
				throw e;
			} catch (final IOException e) {
				throw e;
			}
		} catch (final FileNotFoundException e) {
			throw e;
		} catch (final IOException e) {
			throw e;
		}
		return readWorkbook;
	}

	private void importDescriptionSheetToWorkbook(final Workbook originalWorkbook, final String trialInstanceNumber,
			final Workbook descriptionWorkbook, final List<MeasurementRow> trialObservations, final Map<Object, String> originalValueMap)
			throws MiddlewareQueryException {
		final Map<String, Object> variableMap = new HashMap<String, Object>();
		if (originalWorkbook != null && descriptionWorkbook != null) {
			for (final MeasurementVariable var : descriptionWorkbook.getConditions()) {

				if (var.getLabel() != null && var.getLabel().equalsIgnoreCase(ExcelImportStudyServiceImpl.STUDY)) {
					// study conditions
					// we get from the conditions
					this.setDataToMatchingMeasurementVariable(originalWorkbook.getConditions(), var, originalValueMap, variableMap);

				} else if (var.getLabel() != null && var.getLabel().equalsIgnoreCase(ExcelImportStudyServiceImpl.TRIAL)) {
					// trial level conditions
					this.setDataToMatchingMeasurementData(trialObservations, var, originalWorkbook.isNursery(), originalValueMap,
							variableMap);
				}

			}

			for (final MeasurementVariable var : descriptionWorkbook.getConstants()) {

				if (var.getLabel() != null && var.getLabel().equalsIgnoreCase(ExcelImportStudyServiceImpl.STUDY)) {
					// study conditions
					// we get from the conditions
					this.setDataToMatchingMeasurementVariable(originalWorkbook.getConstants(), var, originalValueMap, variableMap);

				} else if (var.getLabel() != null && var.getLabel().equalsIgnoreCase(ExcelImportStudyServiceImpl.TRIAL)) {
					// trial level conditions
					// we check if its in constants but not in trial observations
					for (final MeasurementVariable constantsVar : originalWorkbook.getConstants()) {
						boolean isFound = false;
						if (!trialObservations.isEmpty()) {
							for (final MeasurementRow temp : trialObservations) {
								for (final MeasurementData data : temp.getDataList()) {
									if (data.getMeasurementVariable().getTermId() == constantsVar.getTermId()) {
										isFound = true;
										break;
									}
								}
								if (isFound) {
									break;
								} else {
									// we need to add it
									final MeasurementData newData =
											new MeasurementData(constantsVar.getName(), "", false, constantsVar.getDataType(), constantsVar);
									temp.getDataList().add(newData);
								}
							}
						}
					}
					this.setDataToMatchingMeasurementData(trialObservations, var, originalWorkbook.isNursery(), originalValueMap,
							variableMap);
				}
			}
			this.setCorrectBreedingMethodInfo(variableMap);
			// this would set info to location (trial level variable)
			if (originalWorkbook.isNursery() && originalWorkbook.getTrialObservations() != null
					&& !originalWorkbook.getTrialObservations().isEmpty() && originalWorkbook.getTrialConditions() != null
					&& !originalWorkbook.getTrialConditions().isEmpty()) {
				final MeasurementVariable locationNameVar =
						WorkbookUtil.getMeasurementVariable(originalWorkbook.getTrialConditions(), TermId.TRIAL_LOCATION.getId());
				if (locationNameVar != null) {
					// we set it to the trial observation level

					for (final MeasurementRow row : originalWorkbook.getTrialObservations()) {
						final MeasurementData data = row.getMeasurementData(locationNameVar.getTermId());
						if (data != null) {
							data.setValue(locationNameVar.getValue());
						}
					}

				}
			}

		}
	}

	private void setCorrectBreedingMethodInfo(final Map<String, Object> variableMap) {
		// we check for special pair variables here and ensure the name is correct
		try {
			// BM_ID, BM_METHOD_CODE
			if (variableMap.containsKey(Integer.toString(TermId.BREEDING_METHOD_ID.getId()))) {
				// we set the code and name accordingly
				final Object tempObj = variableMap.get(Integer.toString(TermId.BREEDING_METHOD_ID.getId()));
				final Object tempObjCode = variableMap.get(Integer.toString(TermId.BREEDING_METHOD_CODE.getId()));
				final Object tempObjName = variableMap.get(Integer.toString(TermId.BREEDING_METHOD.getId()));
				if (tempObj instanceof MeasurementVariable) {
					final MeasurementVariable tempVar = (MeasurementVariable) tempObj;
					final MeasurementVariable tempVarCode = tempObjCode != null ? (MeasurementVariable) tempObjCode : null;
					final MeasurementVariable tempVarName = tempObjName != null ? (MeasurementVariable) tempObjName : null;
					if (tempVar.getValue() != null && !"".equalsIgnoreCase(tempVar.getValue()) && NumberUtils.isNumber(tempVar.getValue())) {
						final Method method = this.fieldbookMiddlewareService.getMethodById(Integer.parseInt(tempVar.getValue()));
						if (tempVarCode != null) {
							// we set the proper code
							tempVarCode.setValue(method != null ? method.getMcode() : "");
						}
						if (tempVarName != null) {
							tempVarName.setValue(method != null ? method.getMname() : "");
						}
					} else {
						// we set the bm code and bm to empty string
						if (tempVarCode != null) {
							// we set the proper code
							tempVarCode.setValue("");
						}
						if (tempVarName != null) {
							tempVarName.setValue("");
						}
					}
				}
			} else if (variableMap.containsKey(Integer.toString(TermId.BREEDING_METHOD_CODE.getId()))) {
				// we just set the name
				final Object tempObjCode = variableMap.get(Integer.toString(TermId.BREEDING_METHOD_CODE.getId()));
				final Object tempObjName = variableMap.get(Integer.toString(TermId.BREEDING_METHOD.getId()));
				final MeasurementVariable tempVarCode = tempObjCode != null ? (MeasurementVariable) tempObjCode : null;
				final MeasurementVariable tempVarName = tempObjName != null ? (MeasurementVariable) tempObjName : null;
				if (tempVarCode != null && !"".equalsIgnoreCase(tempVarCode.getValue())) {
					final Method method =
							this.fieldbookMiddlewareService.getMethodByCode(tempVarCode.getValue(),
									this.contextUtil.getCurrentProgramUUID());
					if (tempVarName != null) {
						tempVarName.setValue(method != null ? method.getMname() : "");
					}
				}
			}
		} catch (final Exception e) {
			ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
		}
	}

	private void setDataToMatchingMeasurementVariable(final List<MeasurementVariable> measurementVarList, final MeasurementVariable var,
			final Map<Object, String> originalValueMap, final Map<String, Object> variableMap) {

		for (final MeasurementVariable temp : measurementVarList) {

			if (this.isMatchingPropertyScaleMethodLabel(var, temp) && WorkbookUtil.isConditionValidate(temp.getTermId()) && temp != null
					&& temp.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()) {

				variableMap.put(Integer.toString(temp.getTermId()), temp);
				originalValueMap.put(temp, temp.getValue());

				try {
					temp.setPossibleValues(this.fieldbookService.getAllPossibleValues(temp.getTermId(), true));
				} catch (final MiddlewareException e) {
					ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
				}

				String xlsValue = "";
				if (temp != null && temp.getPossibleValues() != null && !temp.getPossibleValues().isEmpty()) {
					xlsValue = this.getCategoricalIdCellValue(var, temp);
				} else {
					xlsValue = var.getValue();
				}
				temp.setValue(xlsValue);

				try {
					if (this.validationService.isValidValue(temp, xlsValue, true)) {
						temp.setOperation(Operation.UPDATE);
					}
				} catch (final Exception e) {
					ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
				}
			}
		}
	}

	protected boolean isMatchingPropertyScaleMethodLabel(final MeasurementVariable var, final MeasurementVariable temp) {
		return temp.getProperty().equalsIgnoreCase(var.getProperty()) && temp.getScale().equalsIgnoreCase(var.getScale())
				&& temp.getMethod().equalsIgnoreCase(var.getMethod()) && temp.getLabel().equalsIgnoreCase(var.getLabel());
	}

	private void setDataToMatchingMeasurementData(final List<MeasurementRow> trialObservations, final MeasurementVariable var,
			final boolean isNursery, final Map<Object, String> originalValueMap, final Map<String, Object> variableMap) {

		for (final MeasurementRow temp : trialObservations) {

			for (final MeasurementData data : temp.getDataList()) {

				final MeasurementVariable origVar = data.getMeasurementVariable();

				if (origVar != null && this.isMatchingPropertyScaleMethodLabel(var, origVar) && data != null && origVar != null
						&& origVar.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()) {

					variableMap.put(Integer.toString(origVar.getTermId()), data);
					originalValueMap.put(data, data.getValue());

					try {
						origVar.setPossibleValues(this.fieldbookService.getAllPossibleValues(origVar.getTermId(), true));
					} catch (final MiddlewareException e) {
						ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
					}

					final String xlsValue = this.getXlsValue(var, temp, data, origVar);
					data.setValue(xlsValue);
					if (isNursery) {
						origVar.setValue(xlsValue);
					}

					try {
						if (this.validationService.isValidValue(origVar, xlsValue, true)) {
							origVar.setOperation(Operation.UPDATE);
						}
					} catch (final Exception e) {
						ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	protected String getXlsValue(final MeasurementVariable var, final MeasurementRow temp, final MeasurementData data,
			final MeasurementVariable origVar) {
		String xlsValue = "";
		if (temp != null && origVar.getPossibleValues() != null && !origVar.getPossibleValues().isEmpty()) {
			xlsValue = this.getCategoricalIdCellValue(var, origVar);
			if (origVar.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
				data.setcValueId(xlsValue);
			}
		} else {
			xlsValue = var.getValue();
		}
		return xlsValue;
	}

	protected String getCategoricalIdCellValue(final MeasurementVariable var, final MeasurementVariable origVar) {
		return ExportImportStudyUtil.getCategoricalIdCellValue(var.getValue(), origVar.getPossibleValues(), true);
	}

	protected void importDataToWorkbook(final Set<ChangeType> modes, final Sheet observationSheet,
			final Map<String, MeasurementRow> rowsMap, final String trialInstanceNumber,
			final List<GermplasmChangeDetail> changeDetailsList, final Workbook workbook) throws MiddlewareQueryException,
			WorkbookParserException {

		final List<MeasurementVariable> variablesFactors = workbook.getFactors();
		final List<MeasurementRow> observations = workbook.getObservations();

		final Map<Integer, MeasurementVariable> factorVariableMap = new HashMap<Integer, MeasurementVariable>();
		for (final MeasurementVariable var : variablesFactors) {
			factorVariableMap.put(var.getTermId(), var);
		}

		if (rowsMap != null && !rowsMap.isEmpty()) {

			workbook.setHasExistingDataOverwrite(false);
			final int lastXlsRowIndex = observationSheet.getLastRowNum();
			final String indexes = this.getColumnIndexesFromXlsSheet(observationSheet, variablesFactors, trialInstanceNumber);
			final int desigColumn = this.findColumn(observationSheet, this.getColumnLabel(variablesFactors, TermId.DESIG.getId()));
			final Row headerRow = observationSheet.getRow(0);
			final int lastXlsColIndex = headerRow.getLastCellNum();

			for (int i = 1; i <= lastXlsRowIndex; i++) {
				final Row xlsRow = observationSheet.getRow(i);
				final String key = this.getKeyIdentifierFromXlsRow(xlsRow, indexes);
				if (key != null) {
					final MeasurementRow wRow = rowsMap.get(key);
					if (wRow == null) {
						modes.add(ChangeType.ADDED_ROWS);
					} else if (wRow != null) {
						rowsMap.remove(key);

						final String originalDesig = wRow.getMeasurementDataValue(TermId.DESIG.getId());
						final Cell desigCell = xlsRow.getCell(desigColumn);
						if (desigCell == null) {
							// throw an error
							throw new WorkbookParserException("error.workbook.import.designation.empty.cell");
						}
						final String newDesig = desigCell.getStringCellValue().trim();
						final String originalGid = wRow.getMeasurementDataValue(TermId.GID.getId());
						final String entryNumber = wRow.getMeasurementDataValue(TermId.ENTRY_NO.getId());
						String plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NO.getId());
						if (plotNumber == null || "".equalsIgnoreCase(plotNumber)) {
							plotNumber = wRow.getMeasurementDataValue(TermId.PLOT_NNO.getId());
						}

						if (originalDesig != null && !originalDesig.equalsIgnoreCase(newDesig)) {
							final List<Integer> newGids = this.fieldbookMiddlewareService.getGermplasmIdsByName(newDesig);
							if (originalGid != null && newGids.contains(Integer.valueOf(originalGid))) {
								final MeasurementData wData = wRow.getMeasurementData(TermId.DESIG.getId());
								wData.setValue(newDesig);
							} else {
								final int index = observations.indexOf(wRow);
								final GermplasmChangeDetail changeDetail =
										new GermplasmChangeDetail(index, originalDesig, originalGid, newDesig, "", trialInstanceNumber,
												entryNumber, plotNumber);
								if (newGids != null && !newGids.isEmpty()) {
									changeDetail.setMatchingGids(newGids);
								}
								changeDetailsList.add(changeDetail);
							}
						}

						for (int j = 0; j <= lastXlsColIndex; j++) {
							final Cell headerCell = headerRow.getCell(j);
							if (headerCell != null) {
								final MeasurementData wData = wRow.getMeasurementData(headerCell.getStringCellValue());
								this.importDataCellValues(wData, xlsRow, j, workbook, factorVariableMap);
							}
						}
					}
				}
			}
			if (!rowsMap.isEmpty()) {
				// meaning there are items in the original list, so there are items deleted
				modes.add(ChangeType.DELETED_ROWS);
			}

		}
	}

	protected boolean hasCellValue(final Cell cell) {
		if (cell == null) {
			return false;
		} else if (PoiUtil.getCellValue(cell) == null) {
			return false;
		} else if ("".equals(PoiUtil.getCellValue(cell).toString())) {
			return false;
		}
		return true;
	}

	protected void importDataCellValues(final MeasurementData wData, final Row xlsRow, final int columnIndex, final Workbook workbook,
			final Map<Integer, MeasurementVariable> factorVariableMap) {
		if (wData != null && wData.isEditable()) {
			final Cell cell = xlsRow.getCell(columnIndex);
			String xlsValue = "";
			if (cell != null && this.hasCellValue(cell)) {
				if (wData.getMeasurementVariable() != null && wData.getMeasurementVariable().getPossibleValues() != null
						&& !wData.getMeasurementVariable().getPossibleValues().isEmpty()) {

					wData.setAccepted(false);

					String tempVal = "";

					if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						final double doubleVal = Double.valueOf(cell.getNumericCellValue());
						final double intVal = Double.valueOf(cell.getNumericCellValue()).intValue();
						boolean getDoubleVal = false;
						if (doubleVal - intVal > 0) {
							getDoubleVal = true;
						}

						tempVal = String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
						if (getDoubleVal) {
							tempVal = String.valueOf(Double.valueOf(cell.getNumericCellValue()));
						}
						xlsValue =
								ExportImportStudyUtil.getCategoricalIdCellValue(tempVal,
										wData.getMeasurementVariable().getPossibleValues(), true);
					} else {
						tempVal = cell.getStringCellValue();
						xlsValue =
								ExportImportStudyUtil.getCategoricalIdCellValue(cell.getStringCellValue(), wData.getMeasurementVariable()
										.getPossibleValues(), true);
					}
					final Integer termId =
							wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : new Integer(0);
					if (!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)) {
						workbook.setHasExistingDataOverwrite(true);
					}

					if (wData.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() && !xlsValue.equals(tempVal)) {
						wData.setcValueId(xlsValue);
					} else {
						wData.setcValueId(null);
					}

				} else {

					if (wData.getMeasurementVariable() != null
							&& wData.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
						wData.setAccepted(false);
					}
					if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						xlsValue = this.getRealNumericValue(cell);
					} else {
						xlsValue = cell.getStringCellValue();
					}
					final Integer termId =
							wData.getMeasurementVariable() != null ? wData.getMeasurementVariable().getTermId() : new Integer(0);
					if (!factorVariableMap.containsKey(termId) && (!"".equalsIgnoreCase(wData.getValue()) || wData.getcValueId() != null)) {
						workbook.setHasExistingDataOverwrite(true);
					}
				}
				wData.setValue(xlsValue);
			}
		}
	}

	private String getRealNumericValue(final Cell cell) {
		String realValue = "";
		if (cell != null) {
			if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				final Double doubleVal = Double.valueOf(cell.getNumericCellValue());
				final Integer intVal = Integer.valueOf(doubleVal.intValue());
				if (Double.parseDouble(intVal.toString()) == doubleVal.doubleValue()) {
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

	private void validate(final org.apache.poi.ss.usermodel.Workbook xlsBook, final Workbook workbook) throws WorkbookParserException,
			MiddlewareQueryException {

		// partially parse the file to parse the description sheet only at first
		this.validateNumberOfSheets(xlsBook);

		final Sheet descriptionSheet = xlsBook.getSheetAt(0);
		this.validateDescriptionSheetFirstCell(descriptionSheet);
		this.validateSections(descriptionSheet);

		final Sheet observationSheet = xlsBook.getSheetAt(1);
		this.validateRequiredObservationColumns(observationSheet, workbook);
		this.validateVariates(xlsBook, workbook);
	}

	protected void validateNumberOfSheets(final org.apache.poi.ss.usermodel.Workbook xlsBook) throws WorkbookParserException {
		if (xlsBook.getNumberOfSheets() != 2) {
			throw new WorkbookParserException("error.workbook.import.invalidNumberOfSheets");
		}
	}

	private void validateDescriptionSheetFirstCell(final Sheet descriptionSheet) throws WorkbookParserException {
		if (!ExcelImportStudyServiceImpl.TEMPLATE_DESCRIPTION_SHEET_FIRST_VALUE.equalsIgnoreCase(descriptionSheet.getRow(0).getCell(0)
				.getStringCellValue())) {
			throw new WorkbookParserException("error.workbook.import.invalidFormatDescriptionSheet");
		}
	}

	private void validateSections(final Sheet descriptionSheet) throws WorkbookParserException {
		final int conditionRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_CONDITION);
		final int factorRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_FACTOR);
		final int constantRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_CONSTANT);
		final int variateRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_VARIATE);
		if (conditionRow <= 0 || factorRow <= conditionRow || constantRow <= conditionRow || variateRow <= conditionRow) {
			throw new WorkbookParserException("error.workbook.import.invalidSections");
		}
	}

	protected void validateRequiredObservationColumns(final Sheet obsSheet, final Workbook workbook) throws WorkbookParserException {
		final int entryCol = this.findColumn(obsSheet, this.getColumnLabel(workbook, TermId.ENTRY_NO.getId()));
		int plotCol = this.findColumn(obsSheet, this.getColumnLabel(workbook, TermId.PLOT_NO.getId()));
		if (plotCol == -1) {
			plotCol = this.findColumn(obsSheet, this.getColumnLabel(workbook, TermId.PLOT_NNO.getId()));
		}
		final int gidCol = this.findColumn(obsSheet, this.getColumnLabel(workbook, TermId.GID.getId()));
		final int desigCol = this.findColumn(obsSheet, this.getColumnLabel(workbook, TermId.DESIG.getId()));
		if (entryCol <= -1 || plotCol <= -1 || gidCol <= -1 || desigCol <= -1) {
			throw new WorkbookParserException("error.workbook.import.requiredColumnsMissing");
		}
	}

	private int findRow(final Sheet sheet, final String cellValue) {
		final int result = 0;
		for (int i = 0; i <= sheet.getLastRowNum(); i++) {
			final Row row = sheet.getRow(i);
			if (row != null) {
				final Cell cell = row.getCell(0);
				if (cell != null && cell.getStringCellValue() != null && cell.getStringCellValue().equalsIgnoreCase(cellValue)) {
					return i;
				}
			}
		}

		return result;
	}

	private int findColumn(final Sheet sheet, final String cellValue) throws WorkbookParserException {
		final int result = -1;
		if (cellValue != null) {
			final Row row = sheet.getRow(0); // Encabezados
			final int cells = row.getLastCellNum();
			for (int i = 0; i < cells; i++) {
				final Cell cell = row.getCell(i);
				if (cell == null) {
					throw new WorkbookParserException("error.workbook.import.missing.columns.import.file");
				} else if (cell.getStringCellValue().equalsIgnoreCase(cellValue)) {
					return i;
				}
			}
		}
		return result;
	}

	protected String findColumns(final Sheet sheet, final String... cellValue) throws WorkbookParserException {
		final List<String> cellValueList = Arrays.asList(cellValue);
		String result = StringUtils.join(cellValue, ",");

		if (cellValue != null) {
			final Row row = sheet.getRow(0);
			final int cells = row.getLastCellNum();
			for (int i = 0; i < cells; i++) {
				final Cell cell = row.getCell(i);
				if (cell == null) {
					throw new WorkbookParserException("error.workbook.import.missing.columns.import.file");
				} else if (cellValueList.contains(cell.getStringCellValue())) {
					result = result.replace(cell.getStringCellValue(), String.valueOf(i));
				}
			}
		}
		return result;
	}

	private String getColumnLabel(final Workbook workbook, final int termId) {
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();
		return this.getColumnLabel(variables, termId);
	}

	private String getColumnLabel(final List<MeasurementVariable> variables, final int termId) {
		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() == termId) {
				return variable.getName();
			}
		}
		return null;
	}

	protected String getTrialInstanceNumber(final Workbook workbook, final org.apache.poi.ss.usermodel.Workbook xlsBook)
			throws WorkbookParserException {

		final String trialInstanceNumber = workbook != null && workbook.isNursery() ? "1" : this.getTrialInstanceNumber(xlsBook);
		if (trialInstanceNumber == null || "".equalsIgnoreCase(trialInstanceNumber)) {
			throw new WorkbookParserException("error.workbook.import.missing.trial.instance");
		}

		return trialInstanceNumber;
	}

	protected String getTrialInstanceNumber(final org.apache.poi.ss.usermodel.Workbook xlsBook) {
		final Sheet descriptionSheet = xlsBook.getSheetAt(0);
		final int conditionRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_CONDITION);
		final int factorRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_FACTOR);

		String trialInstance = null;

		for (int indexRow = conditionRow + 1; indexRow < factorRow; indexRow++) {
			Integer stdVarId = null;
			try {
				final Row row = descriptionSheet.getRow(indexRow);
				// we need to check the PSM-R
				final Cell propertyCell = row.getCell(2);
				final Cell scaleCell = row.getCell(3);
				final Cell methodCell = row.getCell(4);
				final Cell labelCell = row.getCell(7);

				stdVarId = null;

				if (this.isPropertyScaleMethodLabelCellNotNull(propertyCell, scaleCell, methodCell, labelCell)
						&& this.isPropertyScaleMethodLabelCellHasStringValue(propertyCell, scaleCell, methodCell, labelCell)) {
					// we get the corresponding standard variable id
					stdVarId =
							this.fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(
									propertyCell.getStringCellValue(), scaleCell.getStringCellValue(), methodCell.getStringCellValue(),
									PhenotypicType.getPhenotypicTypeForLabel(labelCell.getStringCellValue()));
				}

				if (stdVarId != null && stdVarId.intValue() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					final Cell cell = row.getCell(6);
					if (cell == null) {
						trialInstance = "1";
					} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						final Double temp = Double.valueOf(cell.getNumericCellValue());

						trialInstance = Integer.toString(temp.intValue());
					} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						trialInstance = cell.getStringCellValue();
					}
					break;
				}
			} catch (final MiddlewareQueryException e) {
				// no matching
				// just itereate the possible rows again
				ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
			} catch (final Exception e) {
				// cell might be null
				// we won't throw error, since we need to check other variable
				ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
			}
		}

		return trialInstance;
	}

	protected boolean isPropertyScaleMethodLabelCellHasStringValue(final Cell propertyCell, final Cell scaleCell, final Cell methodCell,
			final Cell labelCell) {
		return propertyCell.getStringCellValue() != null && scaleCell.getStringCellValue() != null
				&& methodCell.getStringCellValue() != null && labelCell.getStringCellValue() != null;
	}

	protected boolean isPropertyScaleMethodLabelCellNotNull(final Cell propertyCell, final Cell scaleCell, final Cell methodCell,
			final Cell labelCell) {
		return propertyCell != null && scaleCell != null && methodCell != null && labelCell != null;
	}

	protected List<MeasurementRow> filterObservationsByTrialInstance(final List<MeasurementRow> observations,
			final String trialInstanceNumber) {
		if (trialInstanceNumber != null) {
			return WorkbookUtil.filterObservationsByTrialInstance(observations, trialInstanceNumber);
		}
		return new ArrayList<MeasurementRow>();
	}

	protected Map<String, MeasurementRow> createMeasurementRowsMap(final List<MeasurementRow> observations, final String instanceNumber,
			final boolean isNursery) {
		final Map<String, MeasurementRow> map = new HashMap<String, MeasurementRow>();
		List<MeasurementRow> newObservations = new ArrayList<MeasurementRow>();
		if (!isNursery) {
			if (instanceNumber != null && !"".equalsIgnoreCase(instanceNumber)) {
				newObservations = WorkbookUtil.filterObservationsByTrialInstance(observations, instanceNumber);
			}
		} else {
			newObservations = observations;
		}

		if (newObservations != null && !newObservations.isEmpty()) {
			for (final MeasurementRow row : newObservations) {
				map.put(row.getKeyIdentifier(), row);
			}
		}
		return map;
	}

	protected String getColumnIndexesFromXlsSheet(final Sheet observationSheet, final List<MeasurementVariable> variables,
			final String trialInstanceNumber) throws WorkbookParserException {
		String plotLabel = null, entryLabel = null;
		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() == TermId.PLOT_NO.getId() || variable.getTermId() == TermId.PLOT_NNO.getId()) {
				plotLabel = variable.getName();
			} else if (variable.getTermId() == TermId.ENTRY_NO.getId()) {
				entryLabel = variable.getName();
			}
		}
		if (plotLabel != null && entryLabel != null) {
			final String indexes = this.findColumns(observationSheet, trialInstanceNumber, plotLabel, entryLabel);
			for (final String index : indexes.split(",")) {
				if (!NumberUtils.isNumber(index) || "-1".equalsIgnoreCase(index)) {
					return null;
				}
			}
			return indexes;
		}
		return null;
	}

	private String getKeyIdentifierFromXlsRow(final Row xlsRow, final String indexes) throws WorkbookParserException {
		if (indexes != null) {
			final String[] indexArray = indexes.split(",");
			// plot no
			final Cell plotCell = xlsRow.getCell(Integer.valueOf(indexArray[1]));
			// entry no
			final Cell entryCell = xlsRow.getCell(Integer.valueOf(indexArray[2]));

			if (plotCell == null) {
				throw new WorkbookParserException("error.workbook.import.plot.no.empty.cell");
			} else if (entryCell == null) {
				throw new WorkbookParserException("error.workbook.import.entry.no.empty.cell");
			}

			return indexArray[0] + "-" + this.getRealNumericValue(plotCell) + "-" + this.getRealNumericValue(entryCell);
		}
		return null;
	}

	private void validateVariates(final org.apache.poi.ss.usermodel.Workbook xlsBook, final Workbook workbook)
			throws WorkbookParserException {
		final Sheet descriptionSheet = xlsBook.getSheetAt(0);
		final int variateRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_VARIATE);
		final List<MeasurementVariable> workbookVariates = workbook.getVariates();
		for (int i = variateRow + 1; i <= descriptionSheet.getLastRowNum(); i++) {
			final Row row = descriptionSheet.getRow(i);
			if (row != null) {
				final Cell cell = row.getCell(0);
				if (cell != null && cell.getStringCellValue() != null) {
					final String traitLabel = cell.getStringCellValue();
					final Integer mvarId = WorkbookUtil.getMeasurementVariableId(workbookVariates, traitLabel);
					// new variates
					if (mvarId == null) {
						MeasurementVariable mvar = null;
						try {
							mvar = this.getMeasurementVariable(row);
						} catch (final MiddlewareException e) {
							ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
							throw new WorkbookParserException(this.messageSource.getMessage("error.import.variate.duplicate.psmr",
									new String[] {traitLabel}, LocaleContextHolder.getLocale()));
						}
						if (mvar == null) {
							throw new WorkbookParserException(this.messageSource.getMessage("error.import.variate.does.not.exist",
									new String[] {traitLabel}, LocaleContextHolder.getLocale()));
						} else if (WorkbookUtil.getMeasurementVariable(workbookVariates, mvar.getTermId()) != null) {
							throw new WorkbookParserException(this.messageSource.getMessage("error.import.variate.exists.in.study",
									new String[] {traitLabel}, LocaleContextHolder.getLocale()));
						} else {
							// valid
							WorkbookUtil.addVariateToObservations(mvar, workbook.getObservations());
						}
					}

				}
			}
		}
	}

	private MeasurementVariable getMeasurementVariable(final Row row) throws MiddlewareException {
		MeasurementVariable mvar = null;
		if(row.getCell(ExcelImportStudyServiceImpl.COLUMN_PROPERTY) != null && row.getCell(ExcelImportStudyServiceImpl.COLUMN_SCALE) != null && row.getCell(ExcelImportStudyServiceImpl.COLUMN_METHOD) != null){
			final String property = row.getCell(ExcelImportStudyServiceImpl.COLUMN_PROPERTY).getStringCellValue();
			final String scale = row.getCell(ExcelImportStudyServiceImpl.COLUMN_SCALE).getStringCellValue();
			final String method = row.getCell(ExcelImportStudyServiceImpl.COLUMN_METHOD).getStringCellValue();
			mvar = this.fieldbookMiddlewareService.getMeasurementVariableByPropertyScaleMethodAndRole(property, scale, method,
							PhenotypicType.VARIATE, this.contextUtil.getCurrentProgramUUID());
			if (mvar != null) {
				mvar.setName(row.getCell(ExcelImportStudyServiceImpl.COLUMN_NAME).getStringCellValue());
				mvar.setDescription(row.getCell(ExcelImportStudyServiceImpl.COLUMN_DESCRIPTION).getStringCellValue());
			}
		}
		return mvar;
	}

	private void checkForAddedAndDeletedTraits(final Set<ChangeType> modes, final org.apache.poi.ss.usermodel.Workbook xlsBook,
			final Workbook workbook) {
		final List<String> xlsVariates = new ArrayList<String>();
		final Sheet descriptionSheet = xlsBook.getSheetAt(0);
		final int variateRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_VARIATE);
		for (int i = variateRow + 1; i <= descriptionSheet.getLastRowNum(); i++) {
			if (descriptionSheet.getRow(i) != null && descriptionSheet.getRow(i).getCell(0) != null) {
				final Cell cell = descriptionSheet.getRow(i).getCell(0);
				if (cell.getStringCellValue() != null && !"".equalsIgnoreCase(cell.getStringCellValue())) {
					xlsVariates.add(cell.getStringCellValue());
				}
			}
		}
		final List<String> wbVariates = new ArrayList<String>();
		for (final MeasurementVariable variate : workbook.getVariates()) {
			wbVariates.add(variate.getName());
		}
		for (int i = 0; i < xlsVariates.size(); i++) {
			final String xlsVariate = xlsVariates.get(i);
			for (final String wbVariate : wbVariates) {
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
