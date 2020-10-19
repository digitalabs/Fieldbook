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

package com.efficio.fieldbook.web.trial.service.impl;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.service.ImportGermplasmFileService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.parsing.pojo.*;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class ImportGermplasmFileServiceImpl.
 * 
 * @author Daniel Jao This should parse the import file from the user. Can handle basic and advance file format
 */
@SuppressWarnings("unused")
public class ImportGermplasmFileServiceImpl implements ImportGermplasmFileService {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(ImportGermplasmFileServiceImpl.class);

	/** The Constant FILE_INVALID. */
	public static final String FILE_INVALID = "common.error.invalid.file";

	/** The Constant FILE_TYPE_INVALID. */
	public static final String FILE_TYPE_INVALID = "common.error.invalid.file.type";

	/** The file service. */
	@Resource
	private FileService fileService;

	/** The current sheet. */
	private Integer currentSheet;

	/** The current row. */
	private Integer currentRow;

	/** The file is valid. */
	private boolean fileIsValid;

	/** The list name. */
	private String listName;

	/** The list title. */
	private String listTitle;

	/** The list type. */
	private String listType;

	/** The list date. */
	private Date listDate;

	/** The inp. */
	private InputStream inp;

	/** The wb. */
	private Workbook wb;

	/** The imported germplasm list. */
	private ImportedGermplasmList importedGermplasmList;

	/** The original filename. */
	private String originalFilename;

	/** The error messages. */
	private Set<String> errorMessages;

	/** The is advance import type. */
	private boolean isAdvanceImportType;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public ImportedGermplasmMainInfo storeImportGermplasmWorkbook(MultipartFile multipartFile) throws IOException {
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();

		String filename = this.getFileService().saveTemporaryFile(multipartFile.getInputStream());

		mainInfo.setServerFilename(filename);
		mainInfo.setOriginalFilename(multipartFile.getOriginalFilename());

		return mainInfo;
	}

	/**
	 * Gets the file service.
	 * 
	 * @return the file service
	 */
	public FileService getFileService() {
		return this.fileService;
	}

	@Override
	public ImportedGermplasmMainInfo processWorkbook(ImportedGermplasmMainInfo mainInfo) {

		try {
			this.wb = this.getFileService().retrieveWorkbook(mainInfo.getServerFilename());
			this.doProcessNow(this.wb, mainInfo);

		} catch (FileNotFoundException e) {
			ImportGermplasmFileServiceImpl.LOG.error("File not found", e);
		} catch (InvalidFormatException | IOException e) {
			ImportGermplasmFileServiceImpl.LOG.error(e.getMessage(), e);
			this.showInvalidFileError(e.getMessage());
		} finally {
			if (!this.fileIsValid) {
				mainInfo.setFileIsValid(false);
				mainInfo.setErrorMessages(this.errorMessages);
			}
		}
		return mainInfo;
	}

	/**
	 * Do process now. This would be used for the junit testing
	 * 
	 * @param workbook the workbook
	 * @param mainInfo the main info
	 * @throws Exception the exception
	 */
	@Override
	public void doProcessNow(Workbook workbook, ImportedGermplasmMainInfo mainInfo) {
		this.wb = workbook;
		this.currentSheet = 0;
		this.currentRow = 0;
		this.fileIsValid = true;
		this.errorMessages = new HashSet<>();

		this.readSheet1();
		this.readSheet2();

		if (!this.fileIsValid) {
			this.importedGermplasmList = null;
			mainInfo.setFileIsValid(false);
			mainInfo.setErrorMessages(this.errorMessages);

		} else {
			mainInfo.setFileIsValid(true);
			mainInfo.setInp(this.inp);
			mainInfo.setWb(this.wb);
			mainInfo.setImportedGermplasmList(this.importedGermplasmList);
			mainInfo.setListDate(this.listDate);
			mainInfo.setListName(this.listName);
			mainInfo.setListTitle(this.listTitle);
			mainInfo.setListType(this.listType);
			mainInfo.setAdvanceImportType(this.isAdvanceImportType);
		}
	}

	/**
	 * Read sheet1.
	 */
	private void readSheet1() {
		this.readGermplasmListFileInfo();
		this.readConditions();
		this.readFactors();
	}

	/**
	 * Read sheet2.
	 */
	private void readSheet2() {
		this.currentSheet = 1;
		this.currentRow = 0;
		ImportedGermplasm importedGermplasm;
		Boolean entryColumnIsPresent = false;
		Boolean desigColumnIsPresent = false;
		// for advanced
		Boolean desigGidIsPresent = false;
		Boolean desigCrossIsPresent = false;
		Boolean desigSourcePresent = false;
		Boolean desigEntryCodePresent = false;

		// Check if columns ENTRY and DESIG is present
		if (this.importedGermplasmList.getImportedFactors() != null) {
			for (int col = 0; col < this.importedGermplasmList.getImportedFactors().size(); col++) {
				if (this.getCellStringValue(this.currentSheet, this.currentRow, col, true).equalsIgnoreCase(AppConstants.ENTRY.getString())) {
					entryColumnIsPresent = true;
				} else if (this.getCellStringValue(this.currentSheet, this.currentRow, col, true).equalsIgnoreCase(
						AppConstants.DESIGNATION.getString())) {
					desigColumnIsPresent = true;
				} else if (this.getCellStringValue(this.currentSheet, this.currentRow, col, true).equalsIgnoreCase(
						AppConstants.GID.getString())) {
					desigGidIsPresent = true;
				} else if (this.getCellStringValue(this.currentSheet, this.currentRow, col, true).equalsIgnoreCase(
						AppConstants.CROSS.getString())) {
					desigCrossIsPresent = true;
				} else if (this.getCellStringValue(this.currentSheet, this.currentRow, col, true).equalsIgnoreCase(
						AppConstants.SOURCE.getString())) {
					desigSourcePresent = true;
				} else if (this.getCellStringValue(this.currentSheet, this.currentRow, col, true).equalsIgnoreCase(
						AppConstants.ENTRY_CODE.getString())) {
					desigEntryCodePresent = true;
				}
			}
		}
		if (!entryColumnIsPresent || !desigColumnIsPresent) {
			this.showInvalidFileError("ENTRY or DESIG column missing from Observation sheet.");
			ImportGermplasmFileServiceImpl.LOG.debug("Invalid file on missing ENTRY or DESIG on readSheet2");
		}

		if (entryColumnIsPresent && desigColumnIsPresent) {
			this.isAdvanceImportType = false;
			if (desigGidIsPresent && desigCrossIsPresent && desigSourcePresent && desigEntryCodePresent) {
				this.isAdvanceImportType = true;
			} else if (!desigGidIsPresent && !desigCrossIsPresent && !desigSourcePresent && !desigEntryCodePresent) {
				// do nothing
			} else {
				this.showInvalidFileError("CROSS or SOURCE or GID or ENTRY CODE column missing " + "from Observation sheet.");
				ImportGermplasmFileServiceImpl.LOG.debug("Invalid file on missing ENTRY or DESIG on readSheet2");
			}
		}

		// If still valid (after checking headers for ENTRY and DESIG), proceed
		if (this.fileIsValid) {
			this.currentRow++;

			while (!this.rowIsEmpty()) {
				importedGermplasm = new ImportedGermplasm();
				for (int col = 0; col < this.importedGermplasmList.getImportedFactors().size(); col++) {
					if (this.importedGermplasmList.getImportedFactors().get(col).getFactor()
							.equalsIgnoreCase(AppConstants.ENTRY.getString())) {
						importedGermplasm
								.setEntryNumber(Integer.valueOf(this.getCellStringValue(this.currentSheet, this.currentRow, col, true)));
					} else if (this.importedGermplasmList.getImportedFactors().get(col).getFactor()
							.equalsIgnoreCase(AppConstants.DESIGNATION.getString())) {
						importedGermplasm.setDesig(this.getCellStringValue(this.currentSheet, this.currentRow, col, true));
					} else if (this.importedGermplasmList.getImportedFactors().get(col).getFactor()
							.equalsIgnoreCase(AppConstants.GID.getString())) {
						importedGermplasm.setGid(this.getCellStringValue(this.currentSheet, this.currentRow, col, true));
					} else if (this.importedGermplasmList.getImportedFactors().get(col).getFactor()
							.equalsIgnoreCase(AppConstants.CROSS.getString())) {
						importedGermplasm.setCross(this.getCellStringValue(this.currentSheet, this.currentRow, col, true));
					} else if (this.importedGermplasmList.getImportedFactors().get(col).getFactor()
							.equalsIgnoreCase(AppConstants.SOURCE.getString())) {
						importedGermplasm.setSource(this.getCellStringValue(this.currentSheet, this.currentRow, col, true));
					} else if (this.importedGermplasmList.getImportedFactors().get(col).getFactor()
							.equalsIgnoreCase(AppConstants.ENTRY_CODE.getString())) {
						importedGermplasm.setEntryCode(this.getCellStringValue(this.currentSheet, this.currentRow, col, true));
					} else {
						ImportGermplasmFileServiceImpl.LOG.debug("Unhandled Column - "
								+ this.importedGermplasmList.getImportedFactors().get(col).getFactor().toUpperCase() + ":"
								+ this.getCellStringValue(this.currentSheet, this.currentRow, col));
					}
				}
				this.importedGermplasmList.addImportedGermplasm(importedGermplasm);
				this.currentRow++;
			}
		}
	}

	/**
	 * Read germplasm list file info.
	 */
	private void readGermplasmListFileInfo() {
		try {
			this.listName = this.getCellStringValue(0, 0, 1, true);
			this.listTitle = this.getCellStringValue(0, 1, 1, true);

			String labelIdentifier = this.getCellStringValue(0, 2, 0, true);

			if (AppConstants.LIST_DATE.getString().equalsIgnoreCase(labelIdentifier)) {
				this.listDate = DateUtil.parseDate(this.getCellStringValue(0, 2, 1, true), DateUtil.DATE_AS_NUMBER_FORMAT);
				this.listType = this.getCellStringValue(0, 3, 1, true);
			} else if (AppConstants.LIST_TYPE.getString().equalsIgnoreCase(labelIdentifier)) {
				this.listType = this.getCellStringValue(0, 2, 1, true);
				this.listDate = DateUtil.parseDate(this.getCellStringValue(0, 3, 1, true), DateUtil.DATE_AS_NUMBER_FORMAT);
			}

			this.importedGermplasmList =
					new ImportedGermplasmList(this.originalFilename, this.listName, this.listTitle, this.listType, this.listDate);
		} catch (ParseException e) {
			ImportGermplasmFileServiceImpl.LOG.error(e.getMessage(), e);
		}

		// Prepare for next set of data
		while (!this.rowIsEmpty()) {
			this.currentRow++;
		}

	}

	/**
	 * Read conditions.
	 */
	private void readConditions() {

		// Skip row from file info
		this.currentRow++;

		// Check if headers are correct
		if (!this.getCellStringValue(this.currentSheet, this.currentRow, 0, true).equalsIgnoreCase(AppConstants.CONDITION.getString())
				|| !this.getCellStringValue(this.currentSheet, this.currentRow, 1, true).equalsIgnoreCase(
						AppConstants.DESCRIPTION.getString())
				|| !this.getCellStringValue(this.currentSheet, this.currentRow, 6, true).equalsIgnoreCase(AppConstants.VALUE.getString())) {
			// for now we dont flag as an error
			// Skip row from file info
			this.currentRow++;
			return;
		}
		if (!this.getCellStringValue(this.currentSheet, this.currentRow, 2, true).equalsIgnoreCase(AppConstants.PROPERTY.getString())
				|| !this.getCellStringValue(this.currentSheet, this.currentRow, 3, true).equalsIgnoreCase(AppConstants.SCALE.getString())
				|| !this.getCellStringValue(this.currentSheet, this.currentRow, 4, true).equalsIgnoreCase(AppConstants.METHOD.getString())
				|| !this.getCellStringValue(this.currentSheet, this.currentRow, 5, true).equalsIgnoreCase(
						AppConstants.DATA_TYPE.getString())) {
			// for now we dont flag as an error
			// Skip row from file info
			this.currentRow++;
			return;
		}
		// If file is still valid (after checking headers), proceed
		if (this.fileIsValid) {
			ImportedCondition importedCondition;
			this.currentRow++;
			while (!this.rowIsEmpty()) {
				importedCondition =
						new ImportedCondition(this.getCellStringValue(this.currentSheet, this.currentRow, 0, true),
								this.getCellStringValue(this.currentSheet, this.currentRow, 1, true), this.getCellStringValue(
										this.currentSheet, this.currentRow, 2, true), this.getCellStringValue(this.currentSheet,
										this.currentRow, 3, true), this.getCellStringValue(this.currentSheet, this.currentRow, 4, true),
								this.getCellStringValue(this.currentSheet, this.currentRow, 5, true), this.getCellStringValue(
										this.currentSheet, this.currentRow, 6, true), "");
				this.importedGermplasmList.addImportedCondition(importedCondition);
				this.currentRow++;
			}
		}
		this.currentRow++;
	}

	/**
	 * Read factors.
	 */
	private void readFactors() {
		Boolean entryColumnIsPresent = false;
		Boolean desigColumnIsPresent = false;

		// Check if headers are correct
		if (!this.getCellStringValue(this.currentSheet, this.currentRow, 0, true).equalsIgnoreCase(AppConstants.FACTOR.getString())
				|| !this.getCellStringValue(this.currentSheet, this.currentRow, 1, true).equalsIgnoreCase(
						AppConstants.DESCRIPTION.getString())) {
			this.showInvalidFileError("Incorrect headers for factors.");
			ImportGermplasmFileServiceImpl.LOG.debug("Invalid file on readFactors header");
		}
		if (!this.getCellStringValue(this.currentSheet, this.currentRow, 2, true).equalsIgnoreCase(AppConstants.PROPERTY.getString())
				|| !this.getCellStringValue(this.currentSheet, this.currentRow, 3, true).equalsIgnoreCase(AppConstants.SCALE.getString())
				|| !this.getCellStringValue(this.currentSheet, this.currentRow, 4, true).equalsIgnoreCase(AppConstants.METHOD.getString())
				|| !this.getCellStringValue(this.currentSheet, this.currentRow, 5, true).equalsIgnoreCase(
						AppConstants.DATA_TYPE.getString())) {
			this.showInvalidFileError("Incorrect headers for factors.");
			ImportGermplasmFileServiceImpl.LOG.debug("Invalid file on readFactors header");
		}
		// If file is still valid (after checking headers), proceed
		if (this.fileIsValid) {
			ImportedFactor importedFactor;
			// skip header
			this.currentRow++;
			while (!this.rowIsEmpty()) {
				importedFactor =
						new ImportedFactor(this.getCellStringValue(this.currentSheet, this.currentRow, 0, true), this.getCellStringValue(
								this.currentSheet, this.currentRow, 1, true), this.getCellStringValue(this.currentSheet, this.currentRow,
								2, true), this.getCellStringValue(this.currentSheet, this.currentRow, 3, true), this.getCellStringValue(
								this.currentSheet, this.currentRow, 4, true), this.getCellStringValue(this.currentSheet, this.currentRow,
								5, true), "");
				this.importedGermplasmList.addImportedFactor(importedFactor);

				// Check if the current factor is ENTRY or DESIG
				if (importedFactor.getFactor().equalsIgnoreCase(AppConstants.ENTRY.getString())) {
					entryColumnIsPresent = true;
				} else if (importedFactor.getFactor().equalsIgnoreCase(AppConstants.DESIGNATION.getString())) {
					desigColumnIsPresent = true;
				}
				this.currentRow++;
			}
		}
		this.currentRow++;

		// If ENTRY or DESIG is not present on Factors, return error
		if (!entryColumnIsPresent || !desigColumnIsPresent) {
			this.showInvalidFileError("There is no ENTRY or DESIG factor.");
			ImportGermplasmFileServiceImpl.LOG.debug("Invalid file on missing ENTRY or DESIG on readFactors");
		}
	}

	/**
	 * Row is empty.
	 * 
	 * @return the boolean
	 */
	private Boolean rowIsEmpty() {
		return this.rowIsEmpty(this.currentRow);
	}

	/**
	 * Row is empty.
	 * 
	 * @param row the row
	 * @return the boolean
	 */
	private Boolean rowIsEmpty(Integer row) {
		return this.rowIsEmpty(this.currentSheet, row);
	}

	/**
	 * Row is empty.
	 * 
	 * @param sheet the sheet
	 * @param row the row
	 * @return the boolean
	 */
	private Boolean rowIsEmpty(Integer sheet, Integer row) {
		for (int col = 0; col < 8; col++) {
			if (this.getCellStringValue(sheet, row, col) != null && !"".equalsIgnoreCase(this.getCellStringValue(sheet, row, col))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the cell string value.
	 * 
	 * @param sheetNumber the sheet number
	 * @param rowNumber the row number
	 * @param columnNumber the column number
	 * @return the cell string value
	 */
	private String getCellStringValue(Integer sheetNumber, Integer rowNumber, Integer columnNumber) {
		return this.getCellStringValue(sheetNumber, rowNumber, columnNumber, false);
	}

	/**
	 * Gets the cell string value.
	 * 
	 * @param sheetNumber the sheet number
	 * @param rowNumber the row number
	 * @param columnNumber the column number
	 * @param followThisPosition the follow this position
	 * @return the cell string value
	 */
	private String getCellStringValue(final Integer sheetNumber, final Integer rowNumber, final Integer columnNumber,
			final Boolean followThisPosition) {
		if (followThisPosition) {
			this.currentSheet = sheetNumber;
			this.currentRow = rowNumber;
		}

		try {
			final Sheet sheet = this.wb.getSheetAt(sheetNumber);
			final Row row = sheet.getRow(rowNumber);
			final Cell cell = row.getCell(columnNumber);
			return cell.getStringCellValue();
		} catch (final IllegalStateException e) {
			final Sheet sheet = this.wb.getSheetAt(sheetNumber);
			final Row row = sheet.getRow(rowNumber);
			final Cell cell = row.getCell(columnNumber);
			ImportGermplasmFileServiceImpl.LOG.error(e.getMessage(), e);
			return String.valueOf(Integer.valueOf((int) cell.getNumericCellValue()));
		} catch (final NullPointerException e) {
			ImportGermplasmFileServiceImpl.LOG.error(e.getMessage(), e);
			return "";
		}
	}

	/**
	 * Show invalid file error.
	 * 
	 * @param message the message
	 */
	private void showInvalidFileError(String message) {
		if (this.fileIsValid) {
			this.errorMessages.add(ImportGermplasmFileServiceImpl.FILE_INVALID);
			this.fileIsValid = false;
		}
	}

	@Override
	public void validataAndAddCheckFactor(List<ImportedGermplasm> formImportedGermplasmsm, List<ImportedGermplasm> importedGermplasms,
			UserSelection userSelection) throws MiddlewareException {
		long start = System.currentTimeMillis();
		boolean hasCheck = false;
		List<ImportedGermplasm> sessionImportedGermplasmList = importedGermplasms;
		for (int i = 0; i < formImportedGermplasmsm.size(); i++) {
			ImportedGermplasm germplasm = formImportedGermplasmsm.get(i);
			String checkVal = "";
			if (germplasm.getEntryTypeValue() != null && !"".equalsIgnoreCase(germplasm.getEntryTypeValue())) {
				checkVal = germplasm.getEntryTypeValue();
				hasCheck = true;
			}
			sessionImportedGermplasmList.get(i).setEntryTypeValue(checkVal);
			sessionImportedGermplasmList.get(i).setEntryTypeCategoricalID(germplasm.getEntryTypeCategoricalID());
			sessionImportedGermplasmList.get(i).setEntryTypeName(germplasm.getEntryTypeName());
		}

		if (hasCheck) {
			// we need to add the CHECK factor if its not existing
			List<MeasurementVariable> measurementVariables = userSelection.getWorkbook().getFactors();

			Integer checkVariableTermId = TermId.CHECK.getId();
			StandardVariable stdvar =
					this.fieldbookMiddlewareService.getStandardVariable(checkVariableTermId, this.contextUtil.getCurrentProgramUUID());
			MeasurementVariable checkVariable =
					new MeasurementVariable(checkVariableTermId, "CHECK", stdvar.getDescription(), stdvar.getScale().getName(), stdvar
							.getMethod().getName(), stdvar.getProperty().getName(), stdvar.getDataType().getName(), "",
							AppConstants.ENTRY.getString());
			checkVariable.setRole(PhenotypicType.GERMPLASM);
			boolean checkFactorExisting = false;
			for (MeasurementVariable var : measurementVariables) {
				Integer termId =
						this.fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(var.getProperty(), var.getScale(),
								var.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
				if (termId != null && checkVariableTermId != null && termId.intValue() == checkVariableTermId.intValue()) {
					checkFactorExisting = true;
					break;
				}
			}
			if (!checkFactorExisting) {
				userSelection.getWorkbook().reset();
				userSelection.getWorkbook().setCheckFactorAddedOnly(true);
				checkVariable.setOperation(Operation.ADD);
				userSelection.getWorkbook().getFactors().add(checkVariable);
			}
		} else {
			// we remove since it was dynamically added only
			if (userSelection.getWorkbook().isCheckFactorAddedOnly()) {
				// we need to remove it
				userSelection.getWorkbook().reset();
				List<MeasurementVariable> factors = userSelection.getWorkbook().getFactors();
				factors.remove(factors.size() - 1);
				userSelection.getWorkbook().setFactors(factors);
			}
		}
		ImportGermplasmFileServiceImpl.LOG.info("validataAndAddCheckFactor Time duration: " + (System.currentTimeMillis() - start));
	}

	public void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

}
