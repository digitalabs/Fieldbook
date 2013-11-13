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

import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.FieldMapService;
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
        int reps = Integer.valueOf(info.getNumberOfReps());
        int entries = Integer.valueOf(info.getNumberOfEntries());
        int rows = info.getNumberOfRowsInBlock();
        int ranges = info.getNumberOfRangesInBlock();
        int rowsPerPlot = info.getNumberOfRowsPerPlot();
        boolean isSerpentine = info.getPlantingOrder() == 1;

        return generateFieldmapLabels(names, reps, entries, rowsPerPlot, rows, ranges, isSerpentine);
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
    
    private List<String> generateFieldmapLabels(List<String> names, int reps, int entries, int rowsPerPlot, int rows, int ranges, boolean isSerpentine) {
        List<String> fieldTexts = new ArrayList<String>();
        int noOfTrials = names.size();
        for (int i = 0; i < noOfTrials; i++) {
            for (int j = 0; j < reps; j++) {
                for (int k = 0; k < entries; k++) {
                    fieldTexts.add("Entry " + (k+1) + NEXT_LINE + "Rep " + (j+1) + NEXT_LINE + names.get(i));
                }
            }
        }        
        addPadding(fieldTexts, rows * ranges);
//        if (isSerpentine) {
//            makeSerpentine(fieldTexts, rows, ranges);
//        }
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
}
