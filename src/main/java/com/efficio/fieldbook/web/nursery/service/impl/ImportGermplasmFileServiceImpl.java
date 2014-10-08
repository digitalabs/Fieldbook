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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.bean.ImportedCondition;
import com.efficio.fieldbook.web.nursery.bean.ImportedConstant;
import com.efficio.fieldbook.web.nursery.bean.ImportedFactor;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.ImportedVariate;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;

/**
 * The Class ImportGermplasmFileServiceImpl.
 *
 * @author Daniel Jao
 * This should parse the import file from the user.  Can handle basic and advance file format
 */
@SuppressWarnings("unused")
public class ImportGermplasmFileServiceImpl implements ImportGermplasmFileService{
	
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ImportGermplasmFileServiceImpl.class);
    
    /** The file service. */
    @Resource
    private FileService fileService;

	/** The current sheet. */
	private Integer currentSheet;
    
    /** The current row. */
    private Integer currentRow;
    
    /** The current column. */
    private Integer currentColumn;
    
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
    
    /** The file. */
    public File file;

    /** The temp file name. */
    private String tempFileName;
    
    /** The original filename. */
    private String originalFilename;
    
    /** The server filename. */
    private String serverFilename;
    
    /** The Constant FILE_INVALID. */
    public final static String FILE_INVALID = "common.error.invalid.file";
    
    /** The Constant FILE_TYPE_INVALID. */
    public final static String FILE_TYPE_INVALID = "common.error.invalid.file.type";
        
    /** The error messages. */
    private Set<String> errorMessages;
        
    /** The is advance import type. */
    private boolean isAdvanceImportType;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    

	
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService#storeImportGermplasmWorkbook(org.springframework.web.multipart.MultipartFile)
     */
    @Override
    public ImportedGermplasmMainInfo storeImportGermplasmWorkbook(MultipartFile multipartFile) 
            throws IOException {
        ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();

        String filename = getFileService().saveTemporaryFile(multipartFile.getInputStream());

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
        return fileService;
    }
	
	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService#processWorkbook(com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo)
	 */
	@Override
	public ImportedGermplasmMainInfo processWorkbook(ImportedGermplasmMainInfo mainInfo){
		
	    try {
            wb = getFileService().retrieveWorkbook(mainInfo.getServerFilename());
            doProcessNow(wb, mainInfo);

        } catch (FileNotFoundException e) {
            LOG.error("File not found");
        } catch (IOException e) {
            showInvalidFileError(e.getMessage());
        } catch (OfficeXmlFileException e) {
            showInvalidFileError(e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            showInvalidFileError(e.getMessage());
        } finally {
            if (!fileIsValid) {
                mainInfo.setFileIsValid(false);
                mainInfo.setErrorMessages(errorMessages);
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
	public void doProcessNow(Workbook workbook, ImportedGermplasmMainInfo mainInfo) throws Exception{
        wb = workbook;
        currentSheet = 0;
        currentRow = 0;
        currentColumn = 0;

        fileIsValid = true;
        errorMessages = new HashSet<String>();

        readSheet1();
        readSheet2();

        if (!fileIsValid) {
            importedGermplasmList = null;
            mainInfo.setFileIsValid(false);
            mainInfo.setErrorMessages(errorMessages);

        } else {
            mainInfo.setFileIsValid(true);
            mainInfo.setInp(inp);
            mainInfo.setWb(wb);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
            mainInfo.setListDate(listDate);
            mainInfo.setListName(listName);
            mainInfo.setListTitle(listTitle);
            mainInfo.setListType(listType);
            mainInfo.setAdvanceImportType(isAdvanceImportType);
        }
	}
	
	/**
	 * Read sheet1.
	 */
	private void readSheet1(){
        readGermplasmListFileInfo();
        readConditions();
        readFactors();
    }
    
    /**
     * Read sheet2.
     */
    private void readSheet2(){
        currentSheet = 1;
        currentRow = 0;
        currentColumn = 0;

        ImportedGermplasm importedGermplasm;
        Boolean entryColumnIsPresent = false;
        Boolean desigColumnIsPresent = false;
        // for advanced
        Boolean desigGidIsPresent = false;
        Boolean desigCrossIsPresent = false;
        Boolean desigSourcePresent = false;
        Boolean desigEntryCodePresent = false;

        // Check if columns ENTRY and DESIG is present
        if (importedGermplasmList.getImportedFactors() != null)
            for (int col = 0; col < importedGermplasmList.getImportedFactors().size(); col++) {
                if (getCellStringValue(currentSheet, currentRow, col, true)
                        .equalsIgnoreCase(AppConstants.ENTRY.getString())){
                    entryColumnIsPresent = true;
                }else if (getCellStringValue(currentSheet, currentRow, col, true)
                        .equalsIgnoreCase(AppConstants.DESIGNATION.getString())){
                    desigColumnIsPresent = true;
                }else if (getCellStringValue(currentSheet, currentRow, col, true)
                        .equalsIgnoreCase(AppConstants.GID.getString())){
                    desigGidIsPresent = true;
                }else if (getCellStringValue(currentSheet, currentRow, col, true)
                        .equalsIgnoreCase(AppConstants.CROSS.getString())){
                    desigCrossIsPresent = true;
                }else if (getCellStringValue(currentSheet, currentRow, col, true)
                        .equalsIgnoreCase(AppConstants.SOURCE.getString())){
                    desigSourcePresent = true;
                }else if (getCellStringValue(currentSheet, currentRow, col, true)
                        .equalsIgnoreCase(AppConstants.ENTRY_CODE.getString())){
                    desigEntryCodePresent = true;
                }
            }
        if (!entryColumnIsPresent || !desigColumnIsPresent) {
            showInvalidFileError("ENTRY or DESIG column missing from Observation sheet.");
            LOG.debug("Invalid file on missing ENTRY or DESIG on readSheet2");
        }

        if (entryColumnIsPresent && desigColumnIsPresent) {
            isAdvanceImportType = false;
            if (desigGidIsPresent && desigCrossIsPresent 
                    && desigSourcePresent && desigEntryCodePresent) {
                isAdvanceImportType = true;
            } else if (!desigGidIsPresent && !desigCrossIsPresent 
                    && !desigSourcePresent && !desigEntryCodePresent) {

            } else {
                showInvalidFileError("CROSS or SOURCE or GID or ENTRY CODE column missing " 
                            + "from Observation sheet.");
                LOG.debug("Invalid file on missing ENTRY or DESIG on readSheet2");
            }
        }

        // If still valid (after checking headers for ENTRY and DESIG), proceed
        if (fileIsValid) {
            currentRow++;

            while (!rowIsEmpty()) {
                LOG.debug("");
                importedGermplasm = new ImportedGermplasm();
                for (int col = 0; col < importedGermplasmList.getImportedFactors().size(); col++) {
                    if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().equalsIgnoreCase(AppConstants.ENTRY.getString())) {
                        importedGermplasm.setEntryId(
                                Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
                    } else if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().equalsIgnoreCase(AppConstants.DESIGNATION.getString())) {
                        importedGermplasm.setDesig(getCellStringValue(currentSheet, currentRow, col, true));
                    } else if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().equalsIgnoreCase(AppConstants.GID.getString())) {
                        importedGermplasm.setGid(getCellStringValue(currentSheet, currentRow, col, true));
                    } else if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().equalsIgnoreCase(AppConstants.CROSS.getString())) {
                        importedGermplasm.setCross(getCellStringValue(currentSheet, currentRow, col, true));
                    } else if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().equalsIgnoreCase(AppConstants.SOURCE.getString())) {
                        importedGermplasm.setSource(getCellStringValue(currentSheet, currentRow, col, true));
                    } else if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().equalsIgnoreCase(AppConstants.ENTRY_CODE.getString())) {
                        importedGermplasm.setEntryCode(getCellStringValue(currentSheet, currentRow, col, true));
                    }

                    else {
                        LOG.debug("Unhandled Column - "
                                + importedGermplasmList.getImportedFactors().get(col)
                                        .getFactor().toUpperCase() + ":"
                                + getCellStringValue(currentSheet, currentRow, col));
                    }
                }
                importedGermplasmList.addImportedGermplasm(importedGermplasm);
                currentRow++;
            }
        }
    }

    /**
     * Read germplasm list file info.
     */
    private void readGermplasmListFileInfo() {
        try {
            listName = getCellStringValue(0, 0, 1, true);
            listTitle = getCellStringValue(0, 1, 1, true);
            
            String labelIdentifier = getCellStringValue(0, 2, 0, true);
            
            if(AppConstants.LIST_DATE.getString().equalsIgnoreCase(labelIdentifier)){
            	listDate = DateUtil.parseDate(getCellStringValue(0, 2, 1, true));
            	listType = getCellStringValue(0, 3, 1, true);
            }else if(AppConstants.LIST_TYPE.getString().equalsIgnoreCase(labelIdentifier)){            	
            	listType = getCellStringValue(0, 2, 1, true);
            	listDate = DateUtil.parseDate(getCellStringValue(0, 3, 1, true));
            }
            

            importedGermplasmList = new ImportedGermplasmList(
                        originalFilename, listName, listTitle, listType, listDate);
        } catch (ParseException e) {
            LOG.error(e.getMessage(), e);
        }

        // Prepare for next set of data
        while (!rowIsEmpty()) {
            currentRow++;
        }

    }
    
    /**
     * Read conditions.
     */
    private void readConditions(){
        
        currentRow++; //Skip row from file info
        
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true)
                        .equalsIgnoreCase(AppConstants.CONDITION.getString()) 
            || !getCellStringValue(currentSheet,currentRow,1,true)
                        .equalsIgnoreCase(AppConstants.DESCRIPTION.getString())
            || !getCellStringValue(currentSheet,currentRow,2,true)
                        .equalsIgnoreCase(AppConstants.PROPERTY.getString())
            || !getCellStringValue(currentSheet,currentRow,3,true)
                        .equalsIgnoreCase(AppConstants.SCALE.getString())
            || !getCellStringValue(currentSheet,currentRow,4,true)
                        .equalsIgnoreCase(AppConstants.METHOD.getString())
            || !getCellStringValue(currentSheet,currentRow,5,true)
                        .equalsIgnoreCase(AppConstants.DATA_TYPE.getString())
            || !getCellStringValue(currentSheet,currentRow,6,true)
                        .equalsIgnoreCase(AppConstants.VALUE.getString())
            ){
        	//for now we dont flag as an error
        	currentRow++; //Skip row from file info
        	return;
        }
        //If file is still valid (after checking headers), proceed
        if(fileIsValid){
            ImportedCondition importedCondition;
            currentRow++; 
            while(!rowIsEmpty()){
                importedCondition = new ImportedCondition(getCellStringValue(currentSheet,currentRow,0,true)
                    ,getCellStringValue(currentSheet,currentRow,1,true)
                    ,getCellStringValue(currentSheet,currentRow,2,true)
                    ,getCellStringValue(currentSheet,currentRow,3,true)
                    ,getCellStringValue(currentSheet,currentRow,4,true)
                    ,getCellStringValue(currentSheet,currentRow,5,true)
                    ,getCellStringValue(currentSheet,currentRow,6,true)
                    ,""
                    );
                importedGermplasmList.addImportedCondition(importedCondition);
                currentRow++;
            }
        }
        currentRow++;
    }

    /**
     * Read factors.
     */
    private void readFactors(){
        Boolean entryColumnIsPresent = false;
        Boolean desigColumnIsPresent = false;
        
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true)
                        .equalsIgnoreCase(AppConstants.FACTOR.getString()) 
            || !getCellStringValue(currentSheet,currentRow,1,true)
                        .equalsIgnoreCase(AppConstants.DESCRIPTION.getString())
            || !getCellStringValue(currentSheet,currentRow,2,true)
                        .equalsIgnoreCase(AppConstants.PROPERTY.getString())
            || !getCellStringValue(currentSheet,currentRow,3,true)
                        .equalsIgnoreCase(AppConstants.SCALE.getString())
            || !getCellStringValue(currentSheet,currentRow,4,true)
                        .equalsIgnoreCase(AppConstants.METHOD.getString())
            || !getCellStringValue(currentSheet,currentRow,5,true)
                        .equalsIgnoreCase(AppConstants.DATA_TYPE.getString())
            ) {
            showInvalidFileError("Incorrect headers for factors.");
            LOG.debug("Invalid file on readFactors header");
        }
        //If file is still valid (after checking headers), proceed
        if(fileIsValid){
            ImportedFactor importedFactor;            
            currentRow++; //skip header
            while(!rowIsEmpty()){
            	importedFactor = new ImportedFactor(getCellStringValue(currentSheet,currentRow,0,true)
                        ,getCellStringValue(currentSheet,currentRow,1,true)
                        ,getCellStringValue(currentSheet,currentRow,2,true)
                        ,getCellStringValue(currentSheet,currentRow,3,true)
                        ,getCellStringValue(currentSheet,currentRow,4,true)
                        ,getCellStringValue(currentSheet,currentRow,5,true)
                        ,"");
                   importedGermplasmList.addImportedFactor(importedFactor);
                   
                //Check if the current factor is ENTRY or DESIG
                if(importedFactor.getFactor().equalsIgnoreCase(AppConstants.ENTRY.getString())){
                    entryColumnIsPresent = true;
                } else if(importedFactor.getFactor().equalsIgnoreCase(AppConstants.DESIGNATION.getString())){
                    desigColumnIsPresent = true;
                }
                currentRow++;
            }
        }
        currentRow++;

        //If ENTRY or DESIG is not present on Factors, return error
        if(!entryColumnIsPresent || !desigColumnIsPresent){
            showInvalidFileError("There is no ENTRY or DESIG factor.");
            LOG.debug("Invalid file on missing ENTRY or DESIG on readFactors");
        }
    }
    
    /**
     * Read constants.
     */
    private void readConstants(){
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).equalsIgnoreCase(AppConstants.CONSTANT.getString()) 
            || !getCellStringValue(currentSheet,currentRow,1,true).equalsIgnoreCase(AppConstants.DESCRIPTION.getString())
            || !getCellStringValue(currentSheet,currentRow,2,true).equalsIgnoreCase(AppConstants.PROPERTY.getString())
            || !getCellStringValue(currentSheet,currentRow,3,true).equalsIgnoreCase(AppConstants.SCALE.getString())
            || !getCellStringValue(currentSheet,currentRow,4,true).equalsIgnoreCase(AppConstants.METHOD.getString())
            || !getCellStringValue(currentSheet,currentRow,5,true).equalsIgnoreCase(AppConstants.DATA_TYPE.getString())
            || !getCellStringValue(currentSheet,currentRow,6,true).equalsIgnoreCase(AppConstants.VALUE.getString())) {
            showInvalidFileError("Incorrect headers for constants.");
        }
        //If file is still valid (after checking headers), proceed
        if(fileIsValid){
            ImportedConstant importedConstant;
            currentRow++; //skip header
            while(!rowIsEmpty()){
                importedConstant = new ImportedConstant(getCellStringValue(currentSheet,currentRow,0,true)
                    ,getCellStringValue(currentSheet,currentRow,1,true)
                    ,getCellStringValue(currentSheet,currentRow,2,true)
                    ,getCellStringValue(currentSheet,currentRow,3,true)
                    ,getCellStringValue(currentSheet,currentRow,4,true)
                    ,getCellStringValue(currentSheet,currentRow,5,true)
                    ,getCellStringValue(currentSheet,currentRow,6,true));
                importedGermplasmList.addImportedConstant(importedConstant);   
                currentRow++;
            }
        }
        currentRow++;
    }
    
    /**
     * Read variates.
     */
    private void readVariates(){
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).equalsIgnoreCase(AppConstants.VARIATE.getString())
            || !getCellStringValue(currentSheet,currentRow,1,true).equalsIgnoreCase(AppConstants.DESCRIPTION.getString())
            || !getCellStringValue(currentSheet,currentRow,2,true).equalsIgnoreCase(AppConstants.PROPERTY.getString())
            || !getCellStringValue(currentSheet,currentRow,3,true).equalsIgnoreCase(AppConstants.SCALE.getString())
            || !getCellStringValue(currentSheet,currentRow,4,true).equalsIgnoreCase(AppConstants.METHOD.getString())
            || !getCellStringValue(currentSheet,currentRow,5,true).equalsIgnoreCase(AppConstants.DATA_TYPE.getString())) {
            showInvalidFileError("Incorrect headers for variates.");
        }
        //If file is still valid (after checking headers), proceed
        if(fileIsValid){
            ImportedVariate importedVariate;
            currentRow++; //skip header
            while(!rowIsEmpty()){
                importedVariate = new ImportedVariate(getCellStringValue(currentSheet,currentRow,0,true)
                    ,getCellStringValue(currentSheet,currentRow,1,true)
                    ,getCellStringValue(currentSheet,currentRow,2,true)
                    ,getCellStringValue(currentSheet,currentRow,3,true)
                    ,getCellStringValue(currentSheet,currentRow,4,true)
                    ,getCellStringValue(currentSheet,currentRow,5,true));
                importedGermplasmList.addImportedVariate(importedVariate);
                currentRow++;
            }
        }
        currentRow++;
    }

    
    
    /**
     * Row is empty.
     *
     * @return the boolean
     */
    private Boolean rowIsEmpty(){
        return rowIsEmpty(currentRow);
    }
    
    /**
     * Row is empty.
     *
     * @param row the row
     * @return the boolean
     */
    private Boolean rowIsEmpty(Integer row){
        return rowIsEmpty(currentSheet, row);
    }

    /**
     * Row is empty.
     *
     * @param sheet the sheet
     * @param row the row
     * @return the boolean
     */
    private Boolean rowIsEmpty(Integer sheet, Integer row){
        for(int col=0;col<8;col++){
            if(getCellStringValue(sheet, row, col)!=null 
                    && !getCellStringValue(sheet, row, col).equalsIgnoreCase("") ){
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
    private String getCellStringValue(Integer sheetNumber, Integer rowNumber, Integer columnNumber){
        return getCellStringValue(sheetNumber, rowNumber, columnNumber, false);
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
    private String getCellStringValue(Integer sheetNumber, Integer rowNumber
            , Integer columnNumber, Boolean followThisPosition){
        if(followThisPosition){
            currentSheet = sheetNumber;
            currentRow = rowNumber;
            currentColumn = columnNumber;
        }
    
        try {
            Sheet sheet = wb.getSheetAt(sheetNumber);
            Row row = sheet.getRow(rowNumber);
            Cell cell = row.getCell(columnNumber);
            return cell.getStringCellValue();
        } catch(IllegalStateException e) {
            Sheet sheet = wb.getSheetAt(sheetNumber);
            Row row = sheet.getRow(rowNumber);
            Cell cell = row.getCell(columnNumber);
            return String.valueOf(Integer.valueOf((int) cell.getNumericCellValue()));
        } catch(NullPointerException e) {
            return "";
        }
    }
    
    /**
     * Show invalid file error.
     *
     * @param message the message
     */
    private void showInvalidFileError(String message){
        if(fileIsValid){
        	errorMessages.add(FILE_INVALID);
            fileIsValid = false;
        }
    }
    
    /**
     * Show invalid file type error.
     */
    private void showInvalidFileTypeError(){
        if(fileIsValid){
        	errorMessages.add(FILE_TYPE_INVALID);
            fileIsValid = false;
        }
    }

	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService#validataAndAddCheckFactor(java.util.List, com.efficio.fieldbook.web.nursery.bean.UserSelection)
	 */
	@Override
	public void validataAndAddCheckFactor(
			List<ImportedGermplasm> formImportedGermplasmsm, List<ImportedGermplasm> importedGermplasms,
			UserSelection userSelection) throws MiddlewareQueryException {
		long start = System.currentTimeMillis();
		boolean hasCheck = false;
		List<ImportedGermplasm> sessionImportedGermplasmList = importedGermplasms;
		for(int i = 0 ; i < formImportedGermplasmsm.size() ; i++){
			ImportedGermplasm germplasm = formImportedGermplasmsm.get(i);
			String checkVal = "";
			if(germplasm.getCheck() != null && !germplasm.getCheck().equalsIgnoreCase("")){
				checkVal = germplasm.getCheck();
				hasCheck = true;
			}
			sessionImportedGermplasmList.get(i).setCheck(checkVal);
			sessionImportedGermplasmList.get(i).setCheckId(germplasm.getCheckId());
			sessionImportedGermplasmList.get(i).setCheckName(germplasm.getCheckName());
		}
		
		if(hasCheck){
			//we need to add the CHECK factor if its not existing
			List<MeasurementVariable> measurementVariables = userSelection.getWorkbook().getFactors();
			
			Integer checkVariableTermId = TermId.CHECK.getId();
			StandardVariable stdvar = fieldbookMiddlewareService.getStandardVariable(checkVariableTermId);
			MeasurementVariable checkVariable = new MeasurementVariable(
					checkVariableTermId, "CHECK", stdvar.getDescription(), stdvar.getScale().getName(), stdvar.getMethod().getName(),
					stdvar.getProperty().getName(), stdvar.getDataType().getName(), "", AppConstants.ENTRY.getString());
			
			boolean checkFactorExisting = false;
			for(MeasurementVariable var : measurementVariables){
				Integer termId = fieldbookMiddlewareService
				        .getStandardVariableIdByPropertyScaleMethodRole(
				                var.getProperty(), var.getScale(), var.getMethod(), 
				                PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
				if(termId != null && checkVariableTermId != null && termId.intValue() == checkVariableTermId.intValue()){
					checkFactorExisting = true;
					break;
				}
			}
			if(!checkFactorExisting){
				userSelection.getWorkbook().reset();
				userSelection.getWorkbook().setCheckFactorAddedOnly(true);
				checkVariable.setOperation(Operation.ADD);
				userSelection.getWorkbook().getFactors().add(checkVariable);				
			}
		}else{
			//we remove since it was dynamically added only
			if(userSelection.getWorkbook().isCheckFactorAddedOnly() == true){
				//we need to remove it
				userSelection.getWorkbook().reset();
				List<MeasurementVariable> factors = userSelection.getWorkbook().getFactors();
				factors.remove(factors.size() - 1);
				userSelection.getWorkbook().setFactors(factors);
			}
		}
		LOG.info("validataAndAddCheckFactor Time duration: "+ (System.currentTimeMillis() - start));
	}  
    
    
}
