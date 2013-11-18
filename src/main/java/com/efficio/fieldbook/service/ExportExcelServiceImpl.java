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
package com.efficio.fieldbook.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.efficio.fieldbook.service.api.ExportExcelService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

/**
 * The Class ExcelExportServiceImpl.
 */
@SuppressWarnings("deprecation")
public class ExportExcelServiceImpl implements ExportExcelService{
    
    private static final Logger LOG = LoggerFactory.getLogger(ExportExcelServiceImpl.class);
    
    @Resource
    private ResourceBundleMessageSource messageSource;
    
    public FileOutputStream exportFieldMapToExcel(String fileName, UserFieldmap userFieldMap) throws FieldbookException{
        Locale locale = LocaleContextHolder.getLocale();

        boolean isTrial = userFieldMap.isTrial();
        
        // Summary of Trial/Nursery, Field and Planting Details
        String summaryOfFieldbookFieldPlantingDetailsLabel = messageSource.getMessage("fieldmap.header.summary.for.trial", null, locale); //SUMMARY OF TRIAL, FIELD AND PLANTING DETAILS
        String selectedFieldbookLabel =  messageSource.getMessage("fieldmap.trial.selected.trial", null, locale); //Selected Trial:
        if (!isTrial){ 
            summaryOfFieldbookFieldPlantingDetailsLabel = messageSource.getMessage("fieldmap.header.summary.for.nursery", null, locale); //SUMMARY OF NURSERY, FIELD AND PLANTING DETAILS
            selectedFieldbookLabel =  messageSource.getMessage("fieldmap.nursery.selected.nursery", null, locale); //Selected Nursery:
        }
        String selectedFieldbookValue = userFieldMap.getSelectedName();
        String numberOfEntriesLabel = messageSource.getMessage("fieldmap.trial.number.of.entries", null, locale); // Number of Entries:  -- used if Trial
        String numberOfEntriesAndPlotLabel = messageSource.getMessage("fieldmap.trial.number.of.entries.and.plots", null, locale); //Number of Entries and Plot: -- used if Nursery
        long numberOfEntriesValue = userFieldMap.getNumberOfEntries();
        String numberOfRepsLabel = messageSource.getMessage("fieldmap.trial.number.of.reps", null, locale); // Number of Reps:
        long numberOfRepsValue = userFieldMap.getNumberOfReps();
        String numberOfPlotsLabel = messageSource.getMessage("fieldmap.trial.total.number.of.plots", null, locale); // Total Number of Plots:
        long numberOfPlotsValue = userFieldMap.getTotalNumberOfPlots();

        
        //  Field And Block Details
        String fieldAndBlockDetailsLabel = messageSource.getMessage("fieldmap.trial.field.and.block.details", null, locale); //FIELD AND BLOCK DETAILS
        String fieldLocationLabel =  messageSource.getMessage("fieldmap.label.field.location", null, locale); //Field Location
        String fieldLocationValue = userFieldMap.getLocationName();
        String fieldNameLabel =   messageSource.getMessage("fieldmap.label.field.name", null, locale); //Field Name
        String fieldNameValue = userFieldMap.getFieldName();
        String blockNameLabel =   messageSource.getMessage("fieldmap.label.block.name", null, locale); //Block Name
        String blockNameValue = userFieldMap.getBlockName();
        
        // Row, Range & Plot Details
        String rowRangePlotDetailsLabel = messageSource.getMessage("fieldmap.trial.row.and.range.and.plot.details", null, locale); //ROW, RANGE AND PLOT DETAILS
        String blockCapacityLabel = messageSource.getMessage("fieldmap.label.block.capacity", null, locale); //Block Capacity
        String blockCapacityValue = userFieldMap.getBlockCapacityString(messageSource);   //e.g. "10 Columns, 10 Ranges"
        String rowsPerPlotLabel =  messageSource.getMessage("fieldmap.label.rows.per.plot", null, locale); //Rows per Plot
        int rowsPerPlotValue = userFieldMap.getNumberOfRowsPerPlot(); 
        String columnsLabel =  messageSource.getMessage("fieldmap.label.columns", null, locale); //Columns     
        Integer columnsValue = userFieldMap.getNumberOfColumnsInBlock();     // 10
        
        //Planting Details
        String plantingDetailsLabel = messageSource.getMessage("fieldmap.header.planting.details", null, locale); //PLANTING DETAILS
        String startingCoordinatesLabel =  messageSource.getMessage("fieldmap.label.starting.coordinates", null, locale); //Starting Coordinates     
        String startingCoordinatesValue = userFieldMap.getStartingCoordinateString(messageSource); // Column 1, Range 1
        String plantingOrderLabel =  messageSource.getMessage("fieldmap.label.planting.order", null, locale); //Planting Order     
        String plantingOrderValue = userFieldMap.getPlantingOrderString(messageSource);  //"Row/Column" or "Serpentine"
        
        // FieldMap
        String fieldMapLabel = messageSource.getMessage("fieldmap.header.fieldmap", null, locale); //FIELD MAP
        String rowsLabel = messageSource.getMessage("fieldmap.label.rows", null, locale); //Rows
        String columnLabel = messageSource.getMessage("fieldmap.label.capitalized.column", null, locale); //Column
        String rangeLabel = messageSource.getMessage("fieldmap.label.capitalized.range", null, locale); //Range
         
        try {
	        //Create workbook
	        Workbook workbook = new HSSFWorkbook();
	        Sheet fieldMapSheet = workbook.createSheet(fieldMapLabel);
	        
	        int rowIndex = 0;
	        int columnIndex = 0;
	        
	        // Create Header Information
	        
	        // Row 1: SUMMARY OF TRIAL, FIELD AND PLANTING DETAILS 
	        Row row = fieldMapSheet.createRow(rowIndex++);
	        row.createCell(columnIndex).setCellValue(summaryOfFieldbookFieldPlantingDetailsLabel);
	        
	        // Row 2: Space
	        row = fieldMapSheet.createRow(rowIndex++);
            
	        // Row 3: Fieldbook Name, Entries, Reps, Plots
	        row = fieldMapSheet.createRow(rowIndex++); 
	        
	        // Selected Trial : [Fieldbook Name]
            row.createCell(columnIndex++).setCellValue(selectedFieldbookLabel);
            row.createCell(columnIndex++).setCellValue(selectedFieldbookValue);
	        
            if (isTrial){ 
                // Number of Entries : 25
                row.createCell(columnIndex++).setCellValue(numberOfEntriesLabel);
                row.createCell(columnIndex++).setCellValue(numberOfEntriesValue);

                // Number of Reps : 3
                row.createCell(columnIndex++).setCellValue(numberOfRepsLabel);
                row.createCell(columnIndex++).setCellValue(numberOfRepsValue);
                
                // Total Number of Plots : 75
                row.createCell(columnIndex++).setCellValue(numberOfPlotsLabel);
                row.createCell(columnIndex++).setCellValue(numberOfPlotsValue);
            } else { // Nursery
                // Number of Entries and Plots: 25
                row.createCell(columnIndex++).setCellValue(numberOfEntriesAndPlotLabel);
                row.createCell(columnIndex++).setCellValue(numberOfEntriesValue);
                
            }

            // Row 4: Space
            row = fieldMapSheet.createRow(rowIndex++);
            
            // Row 5: Header - Details Heading
            row = fieldMapSheet.createRow(rowIndex++);
            columnIndex = 0;
            row.createCell(columnIndex++).setCellValue(fieldAndBlockDetailsLabel);
            row.createCell(columnIndex++);
            row.createCell(columnIndex++).setCellValue(rowRangePlotDetailsLabel);
            row.createCell(columnIndex++);
            row.createCell(columnIndex++).setCellValue(plantingDetailsLabel);
            row.createCell(columnIndex++);
            
            //Row 6: Field Location, Block Capacity, Starting Coordinates
            row = fieldMapSheet.createRow(rowIndex++);
            columnIndex = 0;
            row.createCell(columnIndex++).setCellValue(fieldLocationLabel);
            row.createCell(columnIndex++).setCellValue(fieldLocationValue);
            row.createCell(columnIndex++).setCellValue(blockCapacityLabel);
            row.createCell(columnIndex++).setCellValue(blockCapacityValue);
            row.createCell(columnIndex++).setCellValue(startingCoordinatesLabel);
            row.createCell(columnIndex++).setCellValue(startingCoordinatesValue);
            
            // Row 7: Field Name, Rows Per Plot, Planting Order
            row = fieldMapSheet.createRow(rowIndex++);
            columnIndex = 0;
            row.createCell(columnIndex++).setCellValue(fieldNameLabel);
            row.createCell(columnIndex++).setCellValue(fieldNameValue);
            row.createCell(columnIndex++).setCellValue(rowsPerPlotLabel);
            row.createCell(columnIndex++).setCellValue(rowsPerPlotValue);
            row.createCell(columnIndex++).setCellValue(plantingOrderLabel);
            row.createCell(columnIndex++).setCellValue(plantingOrderValue);
            
            // Row 8: Block Name, Columns
            row = fieldMapSheet.createRow(rowIndex++);
            columnIndex = 0;
            row.createCell(columnIndex++).setCellValue(blockNameLabel);
            row.createCell(columnIndex++).setCellValue(blockNameValue);
            row.createCell(columnIndex++).setCellValue(columnsLabel);
            row.createCell(columnIndex++).setCellValue(columnsValue);
            
            // Row 9: Space
            row = fieldMapSheet.createRow(rowIndex++);
            
            // Row 10: FIELD MAP
            row = fieldMapSheet.createRow(rowIndex++);
            columnIndex = 0;
            row.createCell(columnIndex++).setCellValue(fieldMapLabel);

            // Row 11: Space
            row = fieldMapSheet.createRow(rowIndex++);
            
            // Get FieldMap data
            Plot[][] plots = userFieldMap.getFieldmap();
            int range = userFieldMap.getNumberOfRangesInBlock();
            int col = userFieldMap.getNumberOfColumnsInBlock();

            for(int j = range - 1 ; j >= 0 ; j--){

                if(j == range - 1){ // TOP TABLE LABELS
                    
                    // Row 12: Rows Header
                    rowIndex = printRowHeader(fieldMapSheet, userFieldMap.getNumberOfRowsInBlock(), rowIndex, rowsLabel);

                    // Row 13: UP, DOWN Direction
                    rowIndex = printDirectionHeader(fieldMapSheet, plots, j, col, rowIndex);

                    // Row 14: Column labels
                    rowIndex = printColumnHeader(fieldMapSheet, col, rowIndex, columnLabel);
                }
                
                // Rows 15 onwards: Ranges and Row Data
                row = fieldMapSheet.createRow(rowIndex);
                columnIndex = 0;
                int rangeValue = j + 1;
                row.createCell(columnIndex++).setCellValue(rangeLabel + " " + rangeValue);
                for(int i = 0 ; i < col ; i++){
                    String displayString = plots[i][j].getDisplayString().replace("<br/>", "\n");
                    if (plots[i][j].isPlotDeleted()){
                        displayString = "  X  ";
                    }
                    row.createCell(columnIndex++).setCellValue(displayString);
                    row.createCell(columnIndex).setCellValue("");
                    fieldMapSheet.addMergedRegion(new CellRangeAddress(
                            rowIndex, //first row (0-based)
                            rowIndex, //last row  (0-based)
                            columnIndex - 1, //first column (0-based)
                            columnIndex //last column  (0-based)
                            ));
                    columnIndex++;
                }
                rowIndex++;
                
                if(j == 0){
                    // BOTTOM TABLE LABELS
                    rowIndex = printColumnHeader(fieldMapSheet, col, rowIndex, columnLabel);
                    rowIndex = printDirectionHeader(fieldMapSheet, plots, j, col, rowIndex);
                    rowIndex = printRowHeader(fieldMapSheet, userFieldMap.getNumberOfRowsInBlock(), rowIndex, rowsLabel);
                }
                
            }
            
            //Write the excel file
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            return fileOutputStream;

            
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new FieldbookException("Error writing to file: " + fileName, e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new FieldbookException("Error writing to file: " + fileName, e);
        }
		
		
	}
	
	private int printRowHeader(Sheet fieldMapSheet,  int numOfRows, int rowIndex, String rowsLabel){
        Row row = fieldMapSheet.createRow(rowIndex++);
        int columnIndex = 0;
        row.createCell(columnIndex++).setCellValue(rowsLabel);
        for (int i = 0; i < numOfRows; i++){
            row.createCell(columnIndex++).setCellValue(i+1);
        }
        return rowIndex;

	}
	

	private int printColumnHeader(Sheet fieldMapSheet,  int numberOfColumns, int rowIndex, String columnLabel){
        Row row = fieldMapSheet.createRow(rowIndex);
        int columnIndex = 0;
        row.createCell(columnIndex++).setCellValue("");
        for(int i = 0 ; i < numberOfColumns ; i++){
            int columnValue = i + 1;
            row.createCell(columnIndex++).setCellValue(columnLabel + " " + columnValue);
            row.createCell(columnIndex).setCellValue("");
            fieldMapSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, columnIndex - 1, columnIndex));
            columnIndex++;
        }
        rowIndex++;
        return rowIndex;

	}

    private int printDirectionHeader(Sheet fieldMapSheet, Plot[][] plots, int range, int numberOfColumns, int rowIndex){

        Row row = fieldMapSheet.createRow(rowIndex);
        int columnIndex = 0;
        row.createCell(columnIndex++).setCellValue("");
        for(int i = 0 ; i < numberOfColumns ; i++){
            if(plots[i][range].isUpward()){
                row.createCell(columnIndex++).setCellValue(" UP ");
            } else {
                row.createCell(columnIndex++).setCellValue(" DOWN ");
            }
            row.createCell(columnIndex).setCellValue("");
            fieldMapSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, columnIndex - 1, columnIndex ));
            columnIndex++;
        }
        rowIndex++;
        return rowIndex;
    }



}
