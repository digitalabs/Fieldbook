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
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

@Service
public class FieldMapServiceImpl implements FieldMapService{
    
    private static final Logger LOG = LoggerFactory.getLogger(FieldMapServiceImpl.class);
    
    private static final String NEXT_LINE = "<br/>";

    /*@Override
    public List<String> generateFieldMapLabels(UserFieldmap info) {
        List<FieldMapLabel> labels = info.getFieldMapLabels();
        List<String> fieldTexts = new ArrayList<String>();
        StringBuilder textLabel = null;
        for (FieldMapLabel label : labels) {
            textLabel = new StringBuilder();
            textLabel.append("Entry " + label.getEntryNumber());
            if (info.isTrial()) {
                textLabel.append(NEXT_LINE + "Rep " + label.getRep());
            }
            textLabel.append(NEXT_LINE + info.getSelectedName());
            fieldTexts.add(textLabel.toString());
        }
        return fieldTexts;
    }*/
    
    private String getDisplayString(FieldMapLabel label, boolean isTrial, String selectedName) {
        StringBuilder textLabel = new StringBuilder();
        textLabel.append("Entry " + label.getEntryNumber());
        if (isTrial) {
            textLabel.append(NEXT_LINE + "Rep " + label.getRep());
        }
        textLabel.append(NEXT_LINE + selectedName);
        return textLabel.toString();
    }

    @Override
    public Plot[][] createFieldMap(int col, int range, int startRange,
            int startCol, boolean isSerpentine, Map deletedPlot,
            List<FieldMapLabel> labels, boolean isTrial, String selectedName) {
        
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
                                isTrial, selectedName);
                    }
                }else{
                    for(int j = range - 1 ; j >= 0 ; j--){
                        //for downward planting
                        if(i == startCol && j == startRange){
                            //this will signify that we have started
                            isStartOk = true;
                        }
                        counter = populatePlotData(counter, labels, i, j, plots, isUpward, startCol, startRange, isStartOk, deletedPlot,
                                isTrial, selectedName);

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
            labels.add(new FieldMapLabel(null, null, "DummyData-" + i, null, null));
        }
        Plot[][] plots = createFieldMap(col, range, startRange, startCol, isSerpentine, deletedPlot, labels, true, "Dummy Trial");
        return plots;
    }
    
    public boolean isDeleted(int col, int range, Map deletedPlot){
        if(deletedPlot.get(col+"_"+range) != null)
            return true;
        return false;
    }    
    
    public int populatePlotData(int counter, List<FieldMapLabel> labels, int col, int range, Plot[][] plots,
            boolean isUpward, int startCol, int startRange, boolean isStartOk, Map deletedPlot, boolean isTrial, String selectedName){
        String stringToDisplay = "";
        int i = col;
        int j = range;
        boolean hasAvailableEntries = true;
        if(counter < labels.size()){
            stringToDisplay = getDisplayString(labels.get(counter), isTrial, selectedName);
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
            int column = label.getColumn();
            int range = label.getRange();
            if (column <= totalColumns && range <= totalRanges) {
                Plot plot = plots[column-1][range-1];
                plot.setColumn(column);
                plot.setRange(range);
                if (isSerpentine && column % 2 == 0) {
                    plot.setUpward(false);
                }
                plot.setDisplayString(getDisplayString(label, info.isTrial(), info.getSelectedName()));
                plot.setNotStarted(false);
            }
            else {
                throw new MiddlewareQueryException("The Column/Range of the Field Map exceeded the Total Columns/Ranges");
            }
        }
        
        setOtherFieldMapInformation(info, plots, totalColumns, totalRanges);
        return plots;
    }
        
    private void initializeFieldMapArray(Plot[][] plots, int totalColumns, int totalRanges) {
        for (int i = 0; i < totalColumns; i++) {
            for (int j = 0; j < totalRanges; j++) {
                Plot plot = plots[i][j];
                plot.setNotStarted(true);
                plot.setUpward(true);
            }
        }
    }
    
    private void setOtherFieldMapInformation(UserFieldmap info, Plot[][] plots, int totalColumns, int totalRanges) {
        int startColumn = 0;
        int startRange = 0;
        boolean isStarted = false;
        for (int i = 0; i < totalColumns; i++) {
            for (int j = 0; j < totalRanges; j++) {
                Plot plot = plots[i][j];
                if (!plot.isNotStarted() && startColumn == 0 && startRange == 0) {
                    startColumn = i;
                    startRange = j;
                    isStarted = true;
                }
                else if (isStarted) {
                    plot.setPlotDeleted(true);
                }
            }
        }
        info.setStartingColumn(startColumn);
        info.setStartingRange(startRange);
    }
}
