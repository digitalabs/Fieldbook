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

import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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
        String selectedFieldbookValue = userFieldMap.getBlockName();
        /*String numberOfEntriesLabel = messageSource.getMessage("fieldmap.trial.number.of.entries", null, locale); // Number of Entries:  -- used if Trial
        String numberOfEntriesAndPlotLabel = messageSource.getMessage("fieldmap.trial.number.of.entries.and.plots", null, locale); //Number of Entries and Plot: -- used if Nursery
        long numberOfEntriesValue = userFieldMap.getNumberOfEntries();
        String numberOfRepsLabel = messageSource.getMessage("fieldmap.trial.number.of.reps", null, locale); // Number of Reps:
        long numberOfRepsValue = userFieldMap.getNumberOfReps();
        String numberOfPlotsLabel = messageSource.getMessage("fieldmap.trial.total.number.of.plots", null, locale); // Total Number of Plots:
        long numberOfPlotsValue = userFieldMap.getTotalNumberOfPlots();
*/
        
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
            HSSFWorkbook workbook = new HSSFWorkbook();
	        Sheet fieldMapSheet = workbook.createSheet(fieldMapLabel);
	    
	        CellStyle labelStyle = workbook.createCellStyle();
	        HSSFFont font = workbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	        labelStyle.setFont(font);
	        
	        
	        CellStyle wrapStyle = workbook.createCellStyle();
	        wrapStyle.setWrapText(true);
	        wrapStyle.setAlignment(CellStyle.ALIGN_CENTER);
	        
	        CellStyle mainHeaderStyle = workbook.createCellStyle();
	        
	        HSSFPalette palette = workbook.getCustomPalette();
	        // get the color which most closely matches the color you want to use
	        HSSFColor myColor = palette.findSimilarColor(179,165, 165);
	        // get the palette index of that color 
	        short palIndex = myColor.getIndex();
	        // code to get the style for the cell goes here
	        mainHeaderStyle.setFillForegroundColor(palIndex);	        
	        mainHeaderStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
	        
	        CellStyle mainSubHeaderStyle = workbook.createCellStyle();
            
            HSSFPalette paletteSubHeader = workbook.getCustomPalette();
            // get the color which most closely matches the color you want to use
            HSSFColor myColorSubHeader = paletteSubHeader.findSimilarColor(190,190, 186);
            // get the palette index of that color 
            short palIndexSubHeader = myColorSubHeader.getIndex();
            // code to get the style for the cell goes here
            mainSubHeaderStyle.setFillForegroundColor(palIndexSubHeader);           
            mainSubHeaderStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            mainSubHeaderStyle.setAlignment(CellStyle.ALIGN_CENTER);
	        
	        int rowIndex = 0;
	        int columnIndex = 0;
	        
	        // Create Header Information
	        
	        // Row 1: SUMMARY OF TRIAL, FIELD AND PLANTING DETAILS 
	        Row row = fieldMapSheet.createRow(rowIndex++);
	        Cell summaryCell = row.createCell(columnIndex);
	        summaryCell.setCellValue(summaryOfFieldbookFieldPlantingDetailsLabel);
	        summaryCell.setCellStyle(labelStyle);
	        
	        // Row 2: Space
	        row = fieldMapSheet.createRow(rowIndex++);
            
	        // Row 3: Fieldbook Name, Entries, Reps, Plots
	        row = fieldMapSheet.createRow(rowIndex++); 
	        
	        // Selected Trial : [Fieldbook Name]
            Cell labelCell = row.createCell(columnIndex++);
            
            
/*            labelCell.setCellValue(selectedFieldbookLabel);
            labelCell.setCellStyle(labelStyle);
            row.createCell(columnIndex++).setCellValue(selectedFieldbookValue);
            
            if (isTrial){ 
                // Number of Entries : 25
                labelCell = row.createCell(columnIndex++);
                labelCell.setCellValue(numberOfEntriesLabel);
                labelCell.setCellStyle(labelStyle);
                
                row.createCell(columnIndex++).setCellValue(numberOfEntriesValue);

                // Number of Reps : 3
                labelCell = row.createCell(columnIndex++);
                labelCell.setCellValue(numberOfRepsLabel);
                labelCell.setCellStyle(labelStyle);
                
                row.createCell(columnIndex++).setCellValue(numberOfRepsValue);
                
                // Total Number of Plots : 75
                labelCell = row.createCell(columnIndex++);
                labelCell.setCellValue(numberOfPlotsLabel);
                labelCell.setCellStyle(labelStyle);
                
                row.createCell(columnIndex++).setCellValue(numberOfPlotsValue);
            } else { // Nursery
                // Number of Entries and Plots: 25
                labelCell = row.createCell(columnIndex++);
                labelCell.setCellValue(numberOfEntriesAndPlotLabel);
                labelCell.setCellStyle(labelStyle);
                
                row.createCell(columnIndex++).setCellValue(numberOfEntriesValue);
                
            }
*/
            // Row 4: Space
            row = fieldMapSheet.createRow(rowIndex++);
            
            // Row 5: Header - Details Heading
            row = fieldMapSheet.createRow(rowIndex++);
            columnIndex = 0;
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(fieldAndBlockDetailsLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++);
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(rowRangePlotDetailsLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++);
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(plantingDetailsLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++);
            
            //Row 6: Field Location, Block Capacity, Starting Coordinates
            row = fieldMapSheet.createRow(rowIndex++);
            columnIndex = 0;
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(fieldLocationLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++).setCellValue(fieldLocationValue);
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(blockCapacityLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++).setCellValue(blockCapacityValue);
            
            
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(startingCoordinatesLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++).setCellValue(startingCoordinatesValue);
            
            // Row 7: Field Name, Rows Per Plot, Planting Order
            row = fieldMapSheet.createRow(rowIndex++);
            columnIndex = 0;
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(fieldNameLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++).setCellValue(fieldNameValue);
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(rowsPerPlotLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++).setCellValue(rowsPerPlotValue);
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(plantingOrderLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++).setCellValue(plantingOrderValue);
            
            // Row 8: Block Name, Columns
            row = fieldMapSheet.createRow(rowIndex++);
            columnIndex = 0;
            labelCell= row.createCell(columnIndex++);
            labelCell.setCellValue(blockNameLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++).setCellValue(blockNameValue);
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(columnsLabel);
            labelCell.setCellStyle(labelStyle);
            
            row.createCell(columnIndex++).setCellValue(columnsValue);
            
            // Row 9: Space
            row = fieldMapSheet.createRow(rowIndex++);
            
            // Row 10: FIELD MAP
            row = fieldMapSheet.createRow(rowIndex++);
            columnIndex = 0;
            labelCell = row.createCell(columnIndex++);
            labelCell.setCellValue(fieldMapLabel);
            labelCell.setCellStyle(labelStyle);

            // Row 11: Space
            row = fieldMapSheet.createRow(rowIndex++);
            
            // Get FieldMap data
            Plot[][] plots = userFieldMap.getFieldmap();
            int range = userFieldMap.getNumberOfRangesInBlock();
            int col = userFieldMap.getNumberOfColumnsInBlock();
            int rowsPerPlot = userFieldMap.getNumberOfRowsPerPlot();
            int machineRowCapacity = userFieldMap.getMachineRowCapacity();
            int rows = userFieldMap.getNumberOfRowsInBlock();
            boolean isSerpentine = userFieldMap.getPlantingOrder() == 2; 

            for(int j = range - 1 ; j >= 0 ; j--){

                if(j == range - 1){ // TOP TABLE LABELS
                    
                    // Row 12: Rows Header
                    rowIndex = printRowHeader(fieldMapSheet, userFieldMap.getNumberOfRowsInBlock(), rowIndex, rowsLabel, mainHeaderStyle, mainSubHeaderStyle);

                    // Row 13: UP, DOWN Direction
                    rowIndex = printDirectionHeader(fieldMapSheet, plots, j, rows, rowIndex, machineRowCapacity, mainHeaderStyle, mainSubHeaderStyle, isSerpentine);

                    // Row 14: Column labels
                    rowIndex = printColumnHeader(fieldMapSheet, col, rowIndex, columnLabel, rowsPerPlot, mainHeaderStyle, mainSubHeaderStyle);
                }
                
                // Rows 15 onwards: Ranges and Row Data
                row = fieldMapSheet.createRow(rowIndex);
                row.setHeightInPoints(45);
                columnIndex = 0;
                int rangeValue = j + 1;
                Cell cellRange = row.createCell(columnIndex++);
                cellRange.setCellValue(rangeLabel + " " + rangeValue);
                cellRange.setCellStyle(mainSubHeaderStyle);
                for(int i = 0 ; i < col ; i++){
                    String displayString = plots[i][j].getDisplayString().replace("<br/>", "\n");
                    if (plots[i][j].isPlotDeleted()){
                        displayString = "  X  ";
                    }
                    Cell dataCell = row.createCell(columnIndex++);
                    //dataCell.setCellValue(displayString);
                    dataCell.setCellValue(
                            new HSSFRichTextString(displayString)
                         ); 
                    dataCell.setCellStyle(wrapStyle);
                    //row.createCell(columnIndex).setCellValue("");
                    
                    for(int k = 0 ; k < rowsPerPlot -1 ; k++){
                        row.createCell(columnIndex++).setCellValue("");
                    }
                                        
                    fieldMapSheet.addMergedRegion(new CellRangeAddress(
                            rowIndex, //first row (0-based)
                            rowIndex, //last row  (0-based)
                            columnIndex - rowsPerPlot, //first column (0-based)
                            columnIndex -1 //last column  (0-based)
                            ));
                    //columnIndex++;
                }
                rowIndex++;
                
                if(j == 0){
                    // BOTTOM TABLE LABELS
                    rowIndex = printColumnHeader(fieldMapSheet, col, rowIndex, columnLabel, rowsPerPlot, mainHeaderStyle, mainSubHeaderStyle);
                    rowIndex = printDirectionHeader(fieldMapSheet, plots, j, rows, rowIndex, machineRowCapacity, mainHeaderStyle, mainSubHeaderStyle, isSerpentine);
                    rowIndex = printRowHeader(fieldMapSheet, userFieldMap.getNumberOfRowsInBlock(), rowIndex, rowsLabel, mainHeaderStyle, mainSubHeaderStyle);
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
	
	private int printRowHeader(Sheet fieldMapSheet,  int numOfRows, int rowIndex, String rowsLabel,CellStyle mainHeader, CellStyle subHeaderStyle){
        Row row = fieldMapSheet.createRow(rowIndex++);
        int columnIndex = 0;
        Cell cell = row.createCell(columnIndex++);
        cell.setCellValue(rowsLabel);
        cell.setCellStyle(mainHeader);
        for (int i = 0; i < numOfRows; i++){
            Cell tableCell = row.createCell(columnIndex++);
            tableCell.setCellValue(i+1);
            tableCell.setCellStyle(subHeaderStyle);
        }
        return rowIndex;

	}
	

	private int printColumnHeader(Sheet fieldMapSheet,  int numberOfColumns, int rowIndex, String columnLabel, int rowsPerPlot,CellStyle mainHeader, CellStyle subHeaderStyle){
        Row row = fieldMapSheet.createRow(rowIndex);
        int columnIndex = 0;
        Cell mainCell = row.createCell(columnIndex++);
        mainCell.setCellValue("");
        mainCell.setCellStyle(mainHeader);
        for(int i = 0 ; i < numberOfColumns ; i++){
            int columnValue = i + 1;
            Cell cell = row.createCell(columnIndex++);
            cell.setCellValue(columnLabel + " " + columnValue);
            cell.setCellStyle(subHeaderStyle);
            for(int j = 0 ; j < rowsPerPlot -1 ; j++){
                Cell cell1 = row.createCell(columnIndex++);
                cell1.setCellValue("");
                cell.setCellStyle(subHeaderStyle);
            }
            
            fieldMapSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, columnIndex - rowsPerPlot, columnIndex-1));
            //columnIndex++;
        }
        rowIndex++;
        return rowIndex;

	}

    private int printDirectionHeader(Sheet fieldMapSheet, Plot[][] plots, int range, int numberOfRows, int rowIndex, 
            int machineRowCapacity, CellStyle mainHeader, CellStyle subHeaderStyle, boolean isSerpentine){

        Row row = fieldMapSheet.createRow(rowIndex);
        int columnIndex = 0;
        Cell cell1 = row.createCell(columnIndex++);
        cell1.setCellValue("");
        cell1.setCellStyle(mainHeader);
        
        int numberOfDirections = numberOfRows / machineRowCapacity;
        int remainingRows = numberOfRows % machineRowCapacity;
        if (remainingRows > 0) {
            numberOfDirections++;
        }
        
        for(int i = 0 ; i < numberOfDirections; i++){
            int startCol = machineRowCapacity * i + 1;
            if (isSerpentine) {
                if (i % 2 == 1) {
                    Cell cell = row.createCell(startCol);
                    cell.setCellValue(" DOWN ");
                    cell.setCellStyle(subHeaderStyle);
                }
                else {
                    Cell cell = row.createCell(startCol);
                    cell.setCellValue(" UP ");
                    cell.setCellStyle(subHeaderStyle);
                }
            }
            else {
                Cell cell = row.createCell(startCol);
                cell.setCellValue(" UP ");
                cell.setCellStyle(subHeaderStyle);
            }
            if (i == numberOfDirections - 1 && remainingRows > 0) { //last item
                fieldMapSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 
                        startCol, machineRowCapacity * i + remainingRows));
            }
            else {
                fieldMapSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 
                        startCol, machineRowCapacity * (i+1)));
            }
        }
        rowIndex++;
        return rowIndex;
    }



}
