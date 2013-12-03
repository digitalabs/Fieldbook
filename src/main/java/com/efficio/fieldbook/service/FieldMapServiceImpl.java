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
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

@Service
public class FieldMapServiceImpl implements FieldMapService{
    
    private static final Logger LOG = LoggerFactory.getLogger(FieldMapServiceImpl.class);
    
    private static final String NEXT_LINE = "<br/>";

    private String getDisplayString(FieldMapLabel label, boolean isTrial) {
        StringBuilder textLabel = new StringBuilder();
        textLabel.append(label.getStudyName());
        textLabel.append(NEXT_LINE + "Entry " + label.getEntryNumber());
        if (isTrial) {
            textLabel.append(NEXT_LINE + "Rep " + label.getRep());
        }
        return textLabel.toString();
    }

    @Override
    public Plot[][] createFieldMap(int col, int range, int startRange,
            int startCol, boolean isSerpentine, Map deletedPlot,
            List<FieldMapLabel> labels, boolean isTrial) {
        
        Plot[][] plots = new Plot[col][range];
        //this creates the initial data
        for(int j = range -1 ; j >= 0 ; j--){
            for(int i = 0 ; i < col ; i++){
                plots[i][j] = new Plot(i, j, "");
                //System.out.print("[ " + plots[i][j].getDisplayString() + " ]");
            }
            //System.out.println("");
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
                        counter = populatePlotData(counter, labels, i, j, plots, isUpward, startCol, startRange, isStartOk, deletedPlot, 
                                isTrial);
                    }
                }else{
                    for(int j = range - 1 ; j >= 0 ; j--){
                        //for downward planting
                        if(i == startCol && j == startRange){
                            //this will signify that we have started
                            isStartOk = true;
                        }
                        counter = populatePlotData(counter, labels, i, j, plots, isUpward, startCol, startRange, isStartOk, deletedPlot,
                                isTrial);

                    }
                }


        }
        //for displaying the data
//        LOG.debug("Here Data:");
//        for(int j = range -1 ; j >= 0 ; j--){
//            //we only show this once
//            if(j == range - 1){
//                for(int i = 0 ; i < col ; i++){
//                    if(plots[i][j].isUpward())
//                        System.out.print("[  UP  ]");
//                    else
//                        System.out.print("[   DOWN   ]");
//                }
//                System.out.println("");
//            }
//
//            for(int i = 0 ; i < col ; i++){
//                //s[i][j] = "Col-"+i+ " Range-"+j;
//                System.out.print("[ "+plots[i][j].getDisplayString() + "]");
//            }
//            System.out.println("");
//        }
        
        return plots;
    }
    
    @Override
    public Plot[][] createDummyData(int col, int range, int startRange, int startCol, boolean isSerpentine, Map deletedPlot) {
        startRange--;
        startCol--;
        
        List<FieldMapLabel> labels = new ArrayList<FieldMapLabel>();
        for (int i = 0; i < range*col; i++) {
            FieldMapLabel label = new FieldMapLabel(null, null, "DummyData-" + i, null, null);
            label.setStudyName("Dummy Trial");
            labels.add(label);
        }
        Plot[][] plots = createFieldMap(col, range, startRange, startCol, isSerpentine, deletedPlot, labels, true);
        return plots;
    }
    
    public boolean isDeleted(int col, int range, Map deletedPlot){
        if(deletedPlot.get(col+"_"+range) != null)
            return true;
        return false;
    }    
    
    public int populatePlotData(int counter, List<FieldMapLabel> labels, int col, int range, Plot[][] plots,
            boolean isUpward, int startCol, int startRange, boolean isStartOk, Map deletedPlot, boolean isTrial){
        String stringToDisplay = "";
        int i = col;
        int j = range;
        boolean hasAvailableEntries = true;
        if(counter < labels.size()){
            stringToDisplay = getDisplayString(labels.get(counter), isTrial);
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
                    plots[i][j].setDisplayString(stringToDisplay);
                    //plots[i][j].setExperimentId(labels.get(counter).getExperimentId());
                    labels.get(counter).setColumn(i+1);
                    labels.get(counter).setRange(j+1);
                    plots[i][j].setNoMoreEntries(false);
                    counter++;
                }else{
                    //there are space but no more entries to plant
                    plots[i][j].setNoMoreEntries(true);
                }
            }
            else{
                //meaing this plot is deleted
                plots[i][j].setPlotDeleted(true);
            }
        }else{
            //meaning we haven't started
            plots[i][j].setNotStarted(true);
        }
        return counter;
    }
    
    @Override
    public Plot[][] generateFieldmap(UserFieldmap info) throws MiddlewareQueryException {
        
        int totalColumns = info.getNumberOfColumnsInBlock();
        int totalRanges = info.getNumberOfRangesInBlock();
        boolean isSerpentine = (info.getPlantingOrder() == 2);
        Plot[][] plots = new Plot[totalColumns][totalRanges];
        List<FieldMapLabel> labels = info.getFieldMapLabels();
        initializeFieldMapArray(plots, totalColumns, totalRanges);
        for (FieldMapLabel label : labels) {
            if (label.getColumn() != null && label.getRange() != null) {
                int column = label.getColumn();
                int range = label.getRange();
                if (column <= totalColumns && range <= totalRanges) {
                    Plot plot = plots[column-1][range-1];
                    plot.setColumn(column);
                    plot.setRange(range);
                    plot.setDatasetId(label.getDatasetId());
                    plot.setGeolocationId(label.getGeolocationId());
                    if (isSerpentine && column % 2 == 0) {
                        plot.setUpward(false);
                    }
                    plot.setDisplayString(getDisplayString(label, info.isTrial()));
                    plot.setNotStarted(false);
                }
                else {
                    throw new MiddlewareQueryException("The Column/Range of the Field Map exceeded the Total Columns/Ranges");
                }
            }
        }
        
        setOtherFieldMapInformation(info, plots, totalColumns, totalRanges, isSerpentine);
        return plots;
    }
        
    private void initializeFieldMapArray(Plot[][] plots, int totalColumns, int totalRanges) {
        for (int i = 0; i < totalColumns; i++) {
            for (int j = 0; j < totalRanges; j++) {
                
                Plot plot = new Plot(i, j, "");
                plots[i][j] = plot;
                plot.setNotStarted(false);
                plot.setUpward(true);
            }
        }
    }
    
    private void setOtherFieldMapInformation(UserFieldmap info, Plot[][] plots, int totalColumns, int totalRanges, boolean isSerpentine) {
        boolean isStarted = false;
        List<String> possiblyDeletedCoordinates = new ArrayList<String>();
        int[] order = {1};
        for (int i = 0; i < totalColumns; i++) {
            if (isSerpentine && i % 2 == 1) {
                for (int j = totalRanges - 1; j >= 0; j--) {
                    isStarted = renderPlotCell(info, plots, i, j, isStarted, possiblyDeletedCoordinates, order);
                }
            }
            else {
                for (int j = 0; j < totalRanges; j++) {
                    isStarted = renderPlotCell(info, plots, i, j, isStarted, possiblyDeletedCoordinates, order);
                }
            }
        }
        info.setSelectedFieldmapList(new SelectedFieldmapList(info.getSelectedFieldMaps()));
    }
    
    private void markDeletedCoordinates(Plot[][] plots, List<String> deletedCoordinates) {
        for (String deletedIndex : deletedCoordinates) {
            String[] columnRange = deletedIndex.split("_");
            int column = Integer.parseInt(columnRange[0]);
            int range = Integer.parseInt(columnRange[1]);
            plots[column][range].setPlotDeleted(true);
        }
    }
    
    private boolean renderPlotCell(UserFieldmap info, Plot[][] plots, int i, int j, boolean isStarted, 
            List<String> possiblyDeletedCoordinates, int[] order) {
        
        Plot plot = plots[i][j];
        if (plot.getDisplayString() != null && !plot.getDisplayString().isEmpty()) {
            if (!isStarted) {
                info.setStartingColumn(i + 1);
                info.setStartingRange(j + 1);
                isStarted = true;
            }
            if (!possiblyDeletedCoordinates.isEmpty()) {
                markDeletedCoordinates(plots, possiblyDeletedCoordinates);
                possiblyDeletedCoordinates.clear();
            }
            FieldMapTrialInstanceInfo trial = info.getSelectedTrialInstanceByDatasetIdAndGeolocationId(
                                                    plot.getDatasetId(), plot.getGeolocationId());
            if (trial != null && trial.getOrder() == null) {
                trial.setOrder(order[0]);
                order[0] += 1;
            }
        }
        else {
            if (isStarted) {
                possiblyDeletedCoordinates.add(i + "_" + j);
            }
            else {
                plot.setNotStarted(true);
            }
        }

        return isStarted;
    }
}
