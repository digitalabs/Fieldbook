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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.pojos.svg.Element;
import com.efficio.pojos.svg.Rectangle;

@Service
public class FieldMapServiceImpl implements FieldMapService{
    
    private static final String EMPTY_CELL = "--"; 
    private static final int RANGE_MARGIN_X = 10;
    private static final int RANGE_MARGIN_Y = 10;
    private static final int BLOCK_MARGIN_X = 15;
    private static final int BLOCK_MARGIN_Y = 15;
    private static final int CELL_WIDTH = 70;
    private static final int CELL_HEIGHT = 40;
    private static final int PLOT_MARGIN_Y = 20;
    private static final String NEXT_LINE = "\n";
    private static final String CELL_ID_PREFIX = "cell";

    
    @Override
    public List<Element> createBlankFieldmap(UserFieldmap info, int startX, int startY) {
        int rows = info.getNumberOfRowsInBlock();
        int ranges = info.getNumberOfRangesInBlock();
        int rowsPerPlot = info.getNumberOfRowsPerPlot();
        boolean isSerpentine = info.getPlantingOrder() == 1;
        
        //List<String> data = createFieldmap(info);
        return createFieldmapElements(null, null, rows, ranges, rowsPerPlot, isSerpentine, startX, startY);
    }
    
    @Override
    public List<String> createFieldmap(UserFieldmap info) {
        List<String> names = new ArrayList<String>();
        names.add(info.getSelectedName());
        int reps = info.getNumberOfReps().intValue();

        return generateFieldmapLabels(names, reps);
    }
    
    @Override
    public List<Element> createFieldmap(UserFieldmap info, List<String> markedCells, int startX, int startY) {
        int rows = info.getNumberOfRowsInBlock();
        int ranges = info.getNumberOfRangesInBlock();
        int rowsPerPlot = info.getNumberOfRowsPerPlot();
        boolean isSerpentine = info.getPlantingOrder() == 1;
        
        List<String> data = createFieldmap(info);
        return createFieldmapElements(data, markedCells, rows, ranges, rowsPerPlot, isSerpentine, startX, startY);
    }
    
    private List<Element> createFieldmapElements(List<String> data, List<String> markedCells, int rows, int ranges, 
            int rowsPerPlot, boolean isSerpentine, int startX, int startY) {
        
        List<Element> fieldMapElements = new ArrayList<Element>();
        
        fieldMapElements.add(createBlock(rows, ranges, rowsPerPlot, startX, startY));
        fieldMapElements.addAll(createRanges(rows, ranges, rowsPerPlot, startX, startY));
        fieldMapElements.addAll(createCells(data, markedCells, rows, ranges, rowsPerPlot, isSerpentine, startX, startY));
        
        return fieldMapElements;
    }
    
    @Override
    public List<String> generateFieldmapLabels(List<String> names, int reps) {
        List<String> fieldTexts = new ArrayList<String>();
        //int noOfTrials = names.size();
        int entries = names.size();
        //for (int i = 0; i < noOfTrials; i++) {
            for (int j = 0; j < reps; j++) {
                for (int k = 0; k < entries; k++) {
                    fieldTexts.add("Entry " + (k+1) + NEXT_LINE + "Rep " + (j+1) + NEXT_LINE + names.get(k));
                }
            }
        //}        
        //addPadding(fieldTexts, rows * ranges);
//        if (isSerpentine) {
//            makeSerpentine(fieldTexts, rows, ranges);
//        }
        return fieldTexts;
    }

    @Override
    public List<String> generateFieldMapLabels(UserFieldmap info) {
        List<FieldMapLabel> labels = info.getFieldMapLabels();
        List<String> fieldTexts = new ArrayList<String>();
        StringBuilder textLabel = null;
        for (FieldMapLabel label : labels) {
            textLabel = new StringBuilder();
            textLabel.append("Entry " + label.getEntryNumber());
            if (info.isTrial()) {
                textLabel.append("Rep " + label.getRep());
            }
            textLabel.append(label.getGermplasmName());
            fieldTexts.add(textLabel.toString());
        }
        return fieldTexts;
    }

    private void makeSerpentine(List<Element> data, int rows, int ranges) {
        for (int y = 0; y < rows && y < Math.ceil((double) data.size() / ranges); y++) {
            int start = y * ranges;
            int end = y * ranges + ranges - 1;
            if ((y % 2) == 1) {
                reverse(data, start, end);
            }
        }
    }
    
    private void reverse(List<Element> data, int start, int end) {
//        int iterations = (end - start + 1) / 2;
//        int pad = end - data.size() + 1;
//        if (pad > 0) {
//            for (int i = 0; i < pad; i++) {
//                data.add(EMPTY_CELL);
//            }
//        }
//        for (int i = 0; i < iterations; i++) {
//            Collections.swap(data, start + i, end - i);
//        }
        while (start < end) {
            while (data.get(start).getTitle().isEmpty() && start < end) {
                start++;
            }
            while (data.get(end).getTitle().isEmpty() && start < end) {
                end--;
            }
            if (start < end) {
                Collections.swap(data, start, end);
                swapCoordinates(data, start, end);
            }
            start++;
            end--;
        }
    }
    
    private void swapCoordinates(List<Element> data, int start, int end) {
        Rectangle startElement = ((Rectangle) data.get(start));
        Rectangle endElement = ((Rectangle) data.get(end));
        int tempX = startElement.getX();
        int tempY = endElement.getY();
        startElement.setX(endElement.getX());
        startElement.setY(endElement.getY());
        endElement.setX(tempX);
        endElement.setY(tempY);
    }
    
    private Element createBlock(int rows, int ranges, int rowsPerPlot, int startX, int startY) {
        Rectangle block = new Rectangle();
        block.setX(startX);
        block.setY(startY);
        int width = BLOCK_MARGIN_X * (ranges + 1) + (RANGE_MARGIN_X * 2 + CELL_WIDTH) * ranges;
        int noOfPlotsInRange = rows / rowsPerPlot;
        int height = BLOCK_MARGIN_Y * 2 + RANGE_MARGIN_Y * (rows + noOfPlotsInRange) + PLOT_MARGIN_Y * (noOfPlotsInRange -1) + CELL_HEIGHT * rows;
        block.setWidth(width);
        block.setHeight(height);
        block.setFill("white");
        block.setStroke("red");
        return block;
    }
    
    private List<Element> createRanges(int rows, int ranges, int rowsPerPlot, int startX, int startY) {
        List<Element> rangeElements = new ArrayList<Element>();
        startX += BLOCK_MARGIN_X;
        int noOfPlotsInRange = rows / rowsPerPlot;
        int width = RANGE_MARGIN_X * 2 + CELL_WIDTH;
        int height = RANGE_MARGIN_Y * (rows + noOfPlotsInRange) + PLOT_MARGIN_Y * (noOfPlotsInRange - 1) + CELL_HEIGHT * rows;
        int y = startY + BLOCK_MARGIN_Y;
        for (int i = 0; i < ranges; i++) {
            Rectangle range = new Rectangle();
            range.setX(startX + (BLOCK_MARGIN_X + width) * i);
            range.setY(y);
            range.setWidth(width);
            range.setHeight(height);
            range.setFill("white");
            range.setStroke("blue");
            rangeElements.add(range);
        }
        return rangeElements;
    }
    
    private List<Element> createCells(List<String> data, List<String> markedCells, int rows, int ranges, int rowsPerPlot, boolean isSerpentine, int startX, int startY) {
        List<Element> cells = new ArrayList<Element>();
        startX += BLOCK_MARGIN_X + RANGE_MARGIN_X;
        startY += BLOCK_MARGIN_Y + RANGE_MARGIN_Y;
        int cellDistanceX = RANGE_MARGIN_X * 2 + BLOCK_MARGIN_X;
        int cellDistanceY = RANGE_MARGIN_Y;
        int plotDistanceY = RANGE_MARGIN_Y + PLOT_MARGIN_Y;
        int index = 0;
        String cellLabel = null;
        int cols = rows / rowsPerPlot;
        
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < ranges; r++) {
                cellLabel = getCellLabel(data, markedCells, rowsPerPlot * c, r, index);
                if (cellLabel != null && !cellLabel.isEmpty()) {
                    index++;
                }
                for (int p = 0; p < rowsPerPlot; p++) {
                    int currentRowIndex = rowsPerPlot * c + p;
                    int x = startX + (cellDistanceX + CELL_WIDTH) * r;
                    int y = startY + (cellDistanceY + CELL_HEIGHT) * currentRowIndex + plotDistanceY * c;
                    
                    Rectangle rect = new Rectangle();
                    rect.setUsageType("cell");
                    rect.setTitle(cellLabel);
                    rect.setId(CELL_ID_PREFIX + currentRowIndex + "_" + r);
                    rect.setStroke("black");
                    rect.setWidth(CELL_WIDTH);
                    rect.setHeight(CELL_HEIGHT);
                    rect.setX(x);
                    rect.setY(y);
                    cells.add(rect);
                }
            }
        }
        
        /*
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < ranges; j++) {
                if (data == null || index < data.size()) {
                    int x = startX + (cellDistanceX + CELL_WIDTH) * j;
                    int y = startY + (cellDistanceY + CELL_HEIGHT) * i + plotDistanceY * (i / rowsPerPlot);
                    
                    Rectangle rect = new Rectangle();
                    if (data != null) {
                        if (markedCells.contains(getCoordinatesKey(i, j))) {
                            rect.setTitle("");
                            rect.setFill("#000000");
                        }
                        else {
                            cellTitle = data.get(index++);
                            rect.setTitle(cellTitle);
                        }
                    }
                    rect.setId(CELL_ID_PREFIX + i + "_" + j);
                    rect.setStroke("black");
                    rect.setWidth(CELL_WIDTH);
                    rect.setHeight(CELL_HEIGHT);
                    rect.setX(x);
                    rect.setY(y);
                    cells.add(rect);
                    
//                    Text text = new Text();
//                    text.setX(x);
//                    text.setY(y);
//                    text.setText(data.get(i * ranges + j));
//                    cells.add(text);
                }
            }
        }
        */
        //if (isSerpentine && data != null) {
        //    makeSerpentine(cells, rowsPerPlot, ranges);
        //}
        return cells;
    }
    
    private String getCellLabel(List<String> data, List<String> markedCells, int rowIndex, int rangeIndex, int dataIndex) {
        if (data != null) {
            if (markedCells != null && markedCells.contains(getCoordinatesKey(rowIndex, rangeIndex))) {
                return "";
            }
            else {
                return data.get(dataIndex);
            }
        }
        return null;
    }
    
    private String getCoordinatesKey(int x, int y) {
        return x + "_" + y;
    }
    
    private void addPadding(List<String> data, int expectedSize) {
        int dataSize = data.size();
        if (dataSize < expectedSize) {
            int numberOfPads = expectedSize - dataSize;
            for (int i = 0; i < numberOfPads; i++) {
                data.add(EMPTY_CELL);
            }
        }
    }

    
    @Override
    public Plot[][] createFieldMap(int col, int range, int startRange,
            int startCol, boolean isSerpentine, Map deletedPlot,
            List<String> entryNumbersInString) {
        
        Plot[][] plots = new Plot[col][range];
        //this creates the initial data
        for(int j = range -1 ; j >= 0 ; j--){
            for(int i = 0 ; i < col ; i++){
                //plots[i][j] = new Plot(i,j, "Col-"+i+ " Range-"+j);
                plots[i][j] = new Plot(i, j, "");
                System.out.print("[ " + plots[i][j].getDisplayString() + " ]");
            }
            System.out.println("");
        }

        //this is how we populate data
        int counter = 0;
        //we need to take note of the start range
        boolean isStartOk = false;
        for(int i = 0; i < col ; i++){

                boolean isUpward = true;
                if(isSerpentine){
                    if(i % 2 == 0){
                        isUpward = true;
                    }else{
                        isUpward = false;
                    }
                }else{
                    //row/column
                    isUpward = true;
                }

                if(isUpward){
                    for(int j = 0 ; j < range ; j++){
                        //for upload planting
                        if(i == startCol && j == startRange){
                            //this will signify that we have started
                            isStartOk = true;
                        }
                        counter = populatePlotData(counter, entryNumbersInString, i, j, plots, isUpward, startCol, startRange, isStartOk, deletedPlot);
                    }
                }else{
                    for(int j = range - 1 ; j >= 0 ; j--){
                        //for downward planting
                        if(i == startCol && j == startRange){
                            //this will signify that we have started
                            isStartOk = true;
                        }
                        counter = populatePlotData(counter, entryNumbersInString, i, j, plots, isUpward, startCol, startRange, isStartOk, deletedPlot);

                    }
                }


        }
        //for displaying the data
        System.out.println("Here Data:");
        for(int j = range -1 ; j >= 0 ; j--){
            //we only show this once
            if(j == range - 1){
                for(int i = 0 ; i < col ; i++){
                    if(plots[i][j].isUpward())
                        System.out.print("[  UP  ]");
                    else
                        System.out.print("[   DOWN   ]");
                }
                System.out.println("");
            }

            for(int i = 0 ; i < col ; i++){
                //s[i][j] = "Col-"+i+ " Range-"+j;
                System.out.print("[ "+plots[i][j].getDisplayString() + "]");
            }
            System.out.println("");
        }
        
        return plots;
    }
    
    public boolean isDeleted(int col, int range, Map deletedPlot){
        if(deletedPlot.get(col+"-"+range) != null)
            return true;
        return false;
    }    
    
    public int populatePlotData(int counter, List<String> entryNumbersInString, int col, int range, Plot[][] plots,
            boolean isUpward, int startCol, int startRange, boolean isStartOk, Map deletedPlot){
        String stringToDisplay = "";
        int i = col;
        int j = range;
        boolean hasAvailableEntries = true;
        if(counter < entryNumbersInString.size()){
            stringToDisplay = entryNumbersInString.get(counter);
        }else{
            hasAvailableEntries = false;
        }
        plots[i][j].setUpward(isUpward);
    
        if(isStartOk){
            plots[i][j].setNotStarted(false);
            if(isDeleted(i,j, deletedPlot) == false){
                plots[i][j].setPlotDeleted(false);
                if(hasAvailableEntries){
                    //meaning we can plant already and move to the next plant
                    //plots[i][j].setDisplayString(plots[i][j].getDisplayString() + ": " + stringToDisplay);
                    plots[i][j].setDisplayString(stringToDisplay);
                    plots[i][j].setNoMoreEntries(false);
                    counter++;
                }else{
                    //there are space but no more entries to plant
                    //plots[i][j].setDisplayString(plots[i][j].getDisplayString() + ": No More Entries");
                    plots[i][j].setNoMoreEntries(true);
                }
            }
            else{
                //meaing this plot is deleted
                //plots[i][j].setDisplayString(plots[i][j].getDisplayString() + ": " + "DELETED");
                plots[i][j].setPlotDeleted(true);
            }
        }else{
            //meaning we haven't started
            //plots[i][j].setDisplayString(plots[i][j].getDisplayString() + ": " + "NOT STARTED");
            plots[i][j].setNotStarted(true);
        }
        return counter;
    }
    
}
