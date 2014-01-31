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
import java.text.SimpleDateFormat;
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
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.nursery.bean.ImportedCondition;
import com.efficio.fieldbook.web.nursery.bean.ImportedConstant;
import com.efficio.fieldbook.web.nursery.bean.ImportedFactor;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.ImportedVariate;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;

// TODO: Auto-generated Javadoc
/**
 * The Class ImportGermplasmFileServiceImpl.
 *
 * @author Daniel Jao
 * This should parse the import file from the user.  Can handle basic and advance file format
 */
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
        
    /** The Constant CONDITION_CONDITION. */
    private final static String CONDITION_CONDITION = "CONDITION";
    
    /** The Constant CONDITION_DESCRIPTION. */
    private final static String CONDITION_DESCRIPTION = "DESCRIPTION";
    
    /** The Constant CONDITION_PROPERTY. */
    private final static String CONDITION_PROPERTY = "PROPERTY";
    
    /** The Constant CONDITION_SCALE. */
    private final static String CONDITION_SCALE = "SCALE";
    
    /** The Constant CONDITION_METHOD. */
    private final static String CONDITION_METHOD = "METHOD";
    
    /** The Constant CONDITION_DATA_TYPE. */
    private final static String CONDITION_DATA_TYPE = "DATA TYPE";
    
    /** The Constant CONDITION_VALUE. */
    private final static String CONDITION_VALUE = "VALUE";    
    	
    /** The Constant FACTOR_HEADER_FACTOR. */
    private final static String FACTOR_HEADER_FACTOR = "FACTOR";
    
    /** The Constant FACTOR_HEADER_DESCRIPTION. */
    private final static String FACTOR_HEADER_DESCRIPTION = "DESCRIPTION";
    
    /** The Constant FACTOR_HEADER_PROPERTY. */
    private final static String FACTOR_HEADER_PROPERTY = "PROPERTY";
    
    /** The Constant FACTOR_HEADER_SCALE. */
    private final static String FACTOR_HEADER_SCALE = "SCALE";
    
    /** The Constant FACTOR_HEADER_METHOD. */
    private final static String FACTOR_HEADER_METHOD = "METHOD";
    
    /** The Constant FACTOR_HEADER_DATA_TYPE. */
    private final static String FACTOR_HEADER_DATA_TYPE = "DATA TYPE";
    
    /** The Constant FACTOR_ENTRY. */
    public final static String FACTOR_ENTRY = "ENTRY";
    
    /** The Constant FACTOR_DESIGNATION. */
    public final static String FACTOR_DESIGNATION = "DESIGNATION";
    
    /** The Constant FACTOR_DESIG. */
    public final static String FACTOR_DESIG = "DESIG";
    
    /** The Constant FACTOR_GID. */
    public final static String FACTOR_GID = "GID";
    
    /** The Constant FACTOR_CROSS. */
    public final static String FACTOR_CROSS = "CROSS";
    
    /** The Constant FACTOR_SOURCE. */
    public final static String FACTOR_SOURCE = "SOURCE";
    
    /** The Constant FACTOR_ENTRY_CODE. */
    public final static String FACTOR_ENTRY_CODE = "ENTRY CODE";
    
    /** The Constant FACTOR_PLOT. */
    public final static String FACTOR_PLOT = "PLOT";
    
    /** The Constant FACTOR_CHECK. */
    public final static String FACTOR_CHECK = "CHECK";
    
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
            // inp = new FileInputStream(mainInfo.getServerFilename());
            wb = getFileService().retrieveWorkbook(mainInfo.getServerFilename());
            // wb = new HSSFWorkbook(inp);
            doProcessNow(wb, mainInfo);

        } catch (FileNotFoundException e) {
            LOG.error("File not found");
        } catch (IOException e) {
            showInvalidFileError(e.getMessage());
        } catch (ReadOnlyException e) {
            showInvalidFileTypeError();
        } catch (ConversionException e) {
            showInvalidFileTypeError();
        } catch (OfficeXmlFileException e) {
            showInvalidFileError(e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            showInvalidFileError(e.getMessage());
        } finally {
            if (fileIsValid == false) {
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

        if (fileIsValid == false) {
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
        //readConstants();
        //readVariates();
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
                        .toUpperCase().equals(FACTOR_ENTRY)){
                    entryColumnIsPresent = true;
                }else if (getCellStringValue(currentSheet, currentRow, col, true)
                        .toUpperCase().equals(FACTOR_DESIGNATION)){
                    desigColumnIsPresent = true;
                }else if (getCellStringValue(currentSheet, currentRow, col, true)
                        .toUpperCase().equals(FACTOR_GID)){
                    desigGidIsPresent = true;
                }else if (getCellStringValue(currentSheet, currentRow, col, true)
                        .toUpperCase().equals(FACTOR_CROSS)){
                    desigCrossIsPresent = true;
                }else if (getCellStringValue(currentSheet, currentRow, col, true)
                        .toUpperCase().equals(FACTOR_SOURCE)){
                    desigSourcePresent = true;
                }else if (getCellStringValue(currentSheet, currentRow, col, true)
                        .toUpperCase().equals(FACTOR_ENTRY_CODE)){
                    desigEntryCodePresent = true;
                }
            }
        if (entryColumnIsPresent == false || desigColumnIsPresent == false) {
            showInvalidFileError("ENTRY or DESIG column missing from Observation sheet.");
            LOG.debug("Invalid file on missing ENTRY or DESIG on readSheet2");
        }

        if (entryColumnIsPresent == true && desigColumnIsPresent == true) {
            isAdvanceImportType = false;
            if (desigGidIsPresent == true && desigCrossIsPresent == true 
                    && desigSourcePresent == true && desigEntryCodePresent == true) {
                isAdvanceImportType = true;
            } else if (desigGidIsPresent == false && desigCrossIsPresent == false 
                    && desigSourcePresent == false && desigEntryCodePresent == false) {
                ;
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
                            .getFactor().toUpperCase().equals(FACTOR_ENTRY)) {
                        importedGermplasm.setEntryId(
                                Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
                        // LOG.debug("ENTRY:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().toUpperCase() .equals(FACTOR_DESIGNATION)) {
                        importedGermplasm.setDesig(getCellStringValue(currentSheet, currentRow, col, true));
                        // LOG.debug("DESIG:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().toUpperCase().equals(FACTOR_GID)) {
                        importedGermplasm.setGid(getCellStringValue(currentSheet, currentRow, col, true));
                        // LOG.debug("DESIG:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().toUpperCase().equals(FACTOR_CROSS)) {
                        importedGermplasm.setCross(getCellStringValue(currentSheet, currentRow, col, true));
                        // LOG.debug("DESIG:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().toUpperCase().equals(FACTOR_SOURCE)) {
                        importedGermplasm.setSource(getCellStringValue(currentSheet, currentRow, col, true));
                        // LOG.debug("DESIG:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if (importedGermplasmList.getImportedFactors().get(col)
                            .getFactor().toUpperCase().equals(FACTOR_ENTRY_CODE)) {
                        importedGermplasm.setEntryCode(getCellStringValue(currentSheet, currentRow, col, true));
                        // LOG.debug("DESIG:"+getCellStringValue(currentSheet, currentRow, col));
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
            listDate = new SimpleDateFormat("yyyyMMdd").parse(getCellStringValue(0, 2, 1, true));
            listType = getCellStringValue(0, 3, 1, true);

            importedGermplasmList = new ImportedGermplasmList(
                        originalFilename, listName, listTitle, listType, listDate);
            /*
             * LOG.debug("Original Filename:" + originalFilename); 
             * LOG.debug("List Name:" + listName); 
             * LOG.debug("List Title:" + listTitle);
             * LOG.debug("List Type:" + listType);
             * LOG.debug("List Date:" + listDate);
             */
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
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals(CONDITION_CONDITION) 
            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals(CONDITION_DESCRIPTION)
            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals(CONDITION_PROPERTY)
            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals(CONDITION_SCALE)
            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals(CONDITION_METHOD)
            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals(CONDITION_DATA_TYPE)
            || !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals(CONDITION_VALUE)
            //|| !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")
            ){
        	/*
            showInvalidFileError("Incorrect headers for conditions.");
            LOG.debug("Invalid file on readConditions header");
            LOG.debug("getCellStringValue(currentSheet,currentRow,0,true).toUpperCase());
            */
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
                    //,getCellStringValue(currentSheet,currentRow,7,true)
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
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals(FACTOR_HEADER_FACTOR) 
            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals(FACTOR_HEADER_DESCRIPTION)
            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals(FACTOR_HEADER_PROPERTY)
            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals(FACTOR_HEADER_SCALE)
            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals(FACTOR_HEADER_METHOD)
            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals(FACTOR_HEADER_DATA_TYPE)
            //|| !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("NESTED IN")
            ) {
            showInvalidFileError("Incorrect headers for factors.");
            LOG.debug("Invalid file on readFactors header");
        }
        //If file is still valid (after checking headers), proceed
        if(fileIsValid){
            ImportedFactor importedFactor;            
            currentRow++; //skip header
            while(!rowIsEmpty()){
               /* importedFactor = new ImportedFactor(getCellStringValue(currentSheet,currentRow,0,true)
                    ,getCellStringValue(currentSheet,currentRow,1,true)
                    ,getCellStringValue(currentSheet,currentRow,2,true)
                    ,getCellStringValue(currentSheet,currentRow,3,true)
                    ,getCellStringValue(currentSheet,currentRow,4,true)
                    ,getCellStringValue(currentSheet,currentRow,5,true)
                    ,getCellStringValue(currentSheet,currentRow,7,true));
                */
            	importedFactor = new ImportedFactor(getCellStringValue(currentSheet,currentRow,0,true)
                        ,getCellStringValue(currentSheet,currentRow,1,true)
                        ,getCellStringValue(currentSheet,currentRow,2,true)
                        ,getCellStringValue(currentSheet,currentRow,3,true)
                        ,getCellStringValue(currentSheet,currentRow,4,true)
                        ,getCellStringValue(currentSheet,currentRow,5,true)
                        ,"");
                   importedGermplasmList.addImportedFactor(importedFactor);
                /*
                LOG.debug("");
                LOG.debug("Factor:"+getCellStringValue(currentSheet,currentRow,0));
                LOG.debug("Description:"+getCellStringValue(currentSheet,currentRow,1));
                LOG.debug("Property:"+getCellStringValue(currentSheet,currentRow,2));
                LOG.debug("Scale:"+getCellStringValue(currentSheet,currentRow,3));
                LOG.debug("Method:"+getCellStringValue(currentSheet,currentRow,4));
                LOG.debug("Data Type:"+getCellStringValue(currentSheet,currentRow,5));
                */
                //LOG.debug("Value:"+getCellStringValue(currentSheet,currentRow,6));
                //LOG.debug("Label:"+getCellStringValue(currentSheet,currentRow,7));
                //
                //Check if the current factor is ENTRY or DESIG
                if(importedFactor.getFactor().toUpperCase().equals(FACTOR_ENTRY)){
                    entryColumnIsPresent = true;
                } else if(importedFactor.getFactor().toUpperCase().equals(FACTOR_DESIGNATION)){
                    desigColumnIsPresent = true;
                }
                currentRow++;
            }
        }
        currentRow++;

        //If ENTRY or DESIG is not present on Factors, return error
        if(entryColumnIsPresent == false || desigColumnIsPresent == false){
            showInvalidFileError("There is no ENTRY or DESIG factor.");
            LOG.debug("Invalid file on missing ENTRY or DESIG on readFactors");
        }
    }
    
    /**
     * Read constants.
     */
    private void readConstants(){
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("CONSTANT") 
            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
            || !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("VALUE")) {
            showInvalidFileError("Incorrect headers for constants.");
            //LOG.debug("Invalid file on readConstants header");
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
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("VARIATE")
            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")) {
            showInvalidFileError("Incorrect headers for variates.");
            //LOG.debug("Invalid file on readVariates header");
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
            //source.getAccordion().getApplication().getMainWindow().showNotification("Invalid Import File: " + message, Notification.TYPE_ERROR_MESSAGE);
        	errorMessages.add(FILE_INVALID);
            fileIsValid = false;
        }
    }
    
    /**
     * Show invalid file type error.
     */
    private void showInvalidFileTypeError(){
        if(fileIsValid){
            //source.getAccordion().getApplication().getMainWindow().showNotification("Invalid Import File Type, you need to upload an XLS file", Notification.TYPE_ERROR_MESSAGE);
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

		boolean hasCheck = false;
		List<ImportedGermplasm> sessionImportedGermplasmList = importedGermplasms;
		for(int i = 0 ; i < formImportedGermplasmsm.size() ; i++){
			ImportedGermplasm germplasm = formImportedGermplasmsm.get(i);
			String checkVal = "";
			if(germplasm.getCheck() != null){
				checkVal = germplasm.getCheck();
				hasCheck = true;
			}
			sessionImportedGermplasmList.get(i).setCheck(checkVal);
		}
		
		if(hasCheck){
			//we need to add the CHECK factor if its not existing
			List<MeasurementVariable> measurementVariables = userSelection.getWorkbook().getFactors();
			MeasurementVariable checkVariable = new MeasurementVariable("CHECK", "TYPE OF ENTRY", "CODE", "ASSIGNED", "CHECK", "C", "", "ENTRY");
			Integer checkVariableTermId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(checkVariable.getProperty(), checkVariable.getScale(), checkVariable.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(checkVariable.getLabel()));
			boolean checkFactorExisting = false;
			for(MeasurementVariable var : measurementVariables){
				Integer termId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(var.getProperty(), var.getScale(), var.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
				if(termId != null && checkVariableTermId != null && termId.intValue() == checkVariableTermId.intValue()){
					checkFactorExisting = true;
					break;
				}
			}
			if(checkFactorExisting == false){
				userSelection.getWorkbook().reset();
				userSelection.getWorkbook().setCheckFactorAddedOnly(true);
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
	}  
    
    
}
