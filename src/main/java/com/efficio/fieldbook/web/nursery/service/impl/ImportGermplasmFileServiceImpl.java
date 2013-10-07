package com.efficio.fieldbook.web.nursery.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.nursery.bean.ImportedCondition;
import com.efficio.fieldbook.web.nursery.bean.ImportedConstant;
import com.efficio.fieldbook.web.nursery.bean.ImportedFactor;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.ImportedVariate;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.ui.Window.Notification;
/**
 * Author: Daniel Jao
 * This should parse the import file from the user
 * 
 */
public class ImportGermplasmFileServiceImpl implements ImportGermplasmFileService{
	@Resource
    private FileService fileService;

	private Integer currentSheet;
    private Integer currentRow;
    private Integer currentColumn;
    private boolean fileIsValid;
    private String listName;
    private String listTitle;
    private String listType;
    private Date listDate;
    
    private InputStream inp;
    private Workbook wb;
    
    private ImportedGermplasmList importedGermplasmList;
    
	public File file;

    private String tempFileName;
    
    private String originalFilename;
    private String serverFilename;
    
    public final static String FILE_INVALID = "error.invalid.file";
    public final static String FILE_TYPE_INVALID = "error.invalid.file.type";
    
    private Set<String> errorMessages;
        
	
	@Override
    public ImportedGermplasmMainInfo storeImportGermplasmWorkbook(MultipartFile multipartFile) throws IOException {
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		
		String filename = getFileService().saveTemporaryFile(multipartFile.getInputStream());
		
		mainInfo.setServerFilename(filename);
		mainInfo.setOriginalFilename(multipartFile.getOriginalFilename());
			
		return mainInfo;
        //return ;
    }
	
	public FileService getFileService() {
        return fileService;
    }
	
	@Override
	public ImportedGermplasmMainInfo processWorkbook(ImportedGermplasmMainInfo mainInfo){
		currentSheet = 0;
		currentRow = 0;
		currentColumn = 0;
		
		fileIsValid = true;
		errorMessages = new HashSet();
        
        try {
        	//inp = new FileInputStream(mainInfo.getServerFilename());
        	wb = getFileService().retrieveWorkbook(mainInfo.getServerFilename());
        	//wb = new HSSFWorkbook(inp);
            
            readSheet1();
            readSheet2();

            if(fileIsValid==false){
                importedGermplasmList = null;
                mainInfo.setFileIsValid(false);
                mainInfo.setErrorMessages(errorMessages);
            }else{
            	mainInfo.setFileIsValid(true);
            	mainInfo.setInp(inp);
            	mainInfo.setWb(wb);
            	mainInfo.setImportedGermplasmList(importedGermplasmList);
            	mainInfo.setListDate(listDate);
            	mainInfo.setListName(listName);
            	mainInfo.setListTitle(listTitle);
            	mainInfo.setListType(listType);
            	
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            showInvalidFileError(e.getMessage());
        } catch (ReadOnlyException e) {
            showInvalidFileTypeError();
        } catch (ConversionException e) {
            showInvalidFileTypeError();
        } catch (OfficeXmlFileException e){
            showInvalidFileError(e.getMessage());
        } catch (Exception e) {
        	showInvalidFileError(e.getMessage());
		} finally{
			if(fileIsValid==false){
				mainInfo.setFileIsValid(false);
	            mainInfo.setErrorMessages(errorMessages);
			}
		}
		return mainInfo;
	}
	
	private void readSheet1(){
        readGermplasmListFileInfo();
        readConditions();
        readFactors();
        readConstants();
        readVariates();
    }
    
    private void readSheet2(){
        currentSheet = 1;
        currentRow = 0;
        currentColumn = 0;
                
        ImportedGermplasm importedGermplasm;
        Boolean entryColumnIsPresent = false;
        Boolean desigColumnIsPresent = false;        
    
        //Check if columns ENTRY and DESIG is present
        if(importedGermplasmList.getImportedFactors()!=null)
        for(int col=0;col<importedGermplasmList.getImportedFactors().size();col++){
            if(getCellStringValue(currentSheet, currentRow, col, true).toUpperCase().equals("ENTRY"))
                entryColumnIsPresent = true;
            else if(getCellStringValue(currentSheet, currentRow, col, true).toUpperCase().equals("DESIG"))
                desigColumnIsPresent = true;
        }
        if(entryColumnIsPresent==false || desigColumnIsPresent==false){
            showInvalidFileError("ENTRY or DESIG column missing from Observation sheet.");
            System.out.println("DEBUG | Invalid file on missing ENTRY or DESIG on readSheet2");
        }
        
        //If still valid (after checking headers for ENTRY and DESIG), proceed
        if(fileIsValid){
            currentRow++;
        
            while(!rowIsEmpty()){
                System.out.println("");
                importedGermplasm = new ImportedGermplasm();
                for(int col=0;col<importedGermplasmList.getImportedFactors().size();col++){
                    if(importedGermplasmList.getImportedFactors().get(col).getFactor().toUpperCase().equals("ENTRY")){
                        importedGermplasm.setEntryId(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
                        System.out.println("DEBUG | ENTRY:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(importedGermplasmList.getImportedFactors().get(col).getFactor().toUpperCase().equals("DESIG")){
                        importedGermplasm.setDesig(getCellStringValue(currentSheet, currentRow, col, true));
                        System.out.println("DEBUG | DESIG:"+getCellStringValue(currentSheet, currentRow, col));
                    } else {
                        System.out.println("DEBUG | Unhandled Column - "+importedGermplasmList.getImportedFactors().get(col).getFactor().toUpperCase()+":"+getCellStringValue(currentSheet, currentRow, col));
                    }
                }
                importedGermplasmList.addImportedGermplasm(importedGermplasm);
                currentRow++;
            }
        }
    }

    private void readGermplasmListFileInfo(){
        try {
            listName = getCellStringValue(0,0,1,true);
            listTitle = getCellStringValue(0,1,1,true);
            listType = getCellStringValue(0,2,1,true);
            listDate = new SimpleDateFormat("yyyyMMdd").parse(getCellStringValue(0,3,1,true));
            
            importedGermplasmList = new ImportedGermplasmList(originalFilename, listName, listTitle, listType, listDate); 
            
            System.out.println("DEBUG | Original Filename:" + originalFilename);
            System.out.println("DEBUG | List Name:" + listName);
            System.out.println("DEBUG | List Title:" + listTitle);
            System.out.println("DEBUG | List Type:" + listType);
            System.out.println("DEBUG | List Date:" + listDate);
            
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        //Prepare for next set of data
        while(!rowIsEmpty()){
            currentRow++;
        }
        
    }
    
    private void readConditions(){
        
        currentRow++; //Skip row from file info
        
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("CONDITION") 
            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
            || !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("VALUE")
            || !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")){
            showInvalidFileError("Incorrect headers for conditions.");
            System.out.println("DEBUG | Invalid file on readConditions header");
            System.out.println(getCellStringValue(currentSheet,currentRow,0,true).toUpperCase());
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
                    ,getCellStringValue(currentSheet,currentRow,7,true));
                importedGermplasmList.addImportedCondition(importedCondition);
                currentRow++;
            }
        }
        currentRow++;
    }

    private void readFactors(){
        Boolean entryColumnIsPresent = false;
        Boolean desigColumnIsPresent = false;

        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("FACTOR") 
            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
            || !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")) {
            showInvalidFileError("Incorrect headers for factors.");
            System.out.println("DEBUG | Invalid file on readFactors header");
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
                    ,getCellStringValue(currentSheet,currentRow,7,true));
                
                   importedGermplasmList.addImportedFactor(importedFactor);
                
                System.out.println("");
                System.out.println("DEBUG | Factor:"+getCellStringValue(currentSheet,currentRow,0));
                System.out.println("DEBUG | Description:"+getCellStringValue(currentSheet,currentRow,1));
                System.out.println("DEBUG | Property:"+getCellStringValue(currentSheet,currentRow,2));
                System.out.println("DEBUG | Scale:"+getCellStringValue(currentSheet,currentRow,3));
                System.out.println("DEBUG | Method:"+getCellStringValue(currentSheet,currentRow,4));
                System.out.println("DEBUG | Data Type:"+getCellStringValue(currentSheet,currentRow,5));
                System.out.println("DEBUG | Value:"+getCellStringValue(currentSheet,currentRow,6));
                System.out.println("DEBUG | Label:"+getCellStringValue(currentSheet,currentRow,7));
                
                //Check if the current factor is ENTRY or DESIG
                if(importedFactor.getFactor().toUpperCase().equals("ENTRY")){
                    entryColumnIsPresent = true;
                } else if(importedFactor.getFactor().toUpperCase().equals("DESIG")){
                    desigColumnIsPresent = true;
                }
                currentRow++;
            }
        }
        currentRow++;

        //If ENTRY or DESIG is not present on Factors, return error
        if(entryColumnIsPresent == false || desigColumnIsPresent == false){
            showInvalidFileError("There is no ENTRY or DESIG factor.");
            System.out.println("DEBUG | Invalid file on missing ENTRY or DESIG on readFactors");
        }
    }
    
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
            System.out.println("DEBUG | Invalid file on readConstants header");
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
    
    private void readVariates(){
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("VARIATE")
            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")) {
            showInvalidFileError("Incorrect headers for variates.");
            System.out.println("DEBUG | Invalid file on readVariates header");
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

    
    
    private Boolean rowIsEmpty(){
        return rowIsEmpty(currentRow);
    }
    
    private Boolean rowIsEmpty(Integer row){
        return rowIsEmpty(currentSheet, row);
    }

    private Boolean rowIsEmpty(Integer sheet, Integer row){
        for(int col=0;col<8;col++){
            if(getCellStringValue(sheet, row, col)!="" && getCellStringValue(sheet, row, col)!=null)
                return false;
        }
        return true;        
    }    
    
    private String getCellStringValue(Integer sheetNumber, Integer rowNumber, Integer columnNumber){
        return getCellStringValue(sheetNumber, rowNumber, columnNumber, false);
    }
        
    private String getCellStringValue(Integer sheetNumber, Integer rowNumber, Integer columnNumber, Boolean followThisPosition){
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
    
    private void showInvalidFileError(String message){
        if(fileIsValid){
            //source.getAccordion().getApplication().getMainWindow().showNotification("Invalid Import File: " + message, Notification.TYPE_ERROR_MESSAGE);
        	errorMessages.add(FILE_INVALID);
            fileIsValid = false;
        }
    }
    
    private void showInvalidFileTypeError(){
        if(fileIsValid){
            //source.getAccordion().getApplication().getMainWindow().showNotification("Invalid Import File Type, you need to upload an XLS file", Notification.TYPE_ERROR_MESSAGE);
        	errorMessages.add(FILE_TYPE_INVALID);
            fileIsValid = false;
        }
    }    
}
