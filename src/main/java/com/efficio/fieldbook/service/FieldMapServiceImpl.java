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

    @Override
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
            textLabel.append(NEXT_LINE + label.getGermplasmName());
            fieldTexts.add(textLabel.toString());
        }
        return fieldTexts;
    }

    @Override
    public Plot[][] createFieldMap(int col, int range, int startRange,
            int startCol, boolean isSerpentine, Map deletedPlot,
            List<String> entryNumbersInString) {
        
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
        
        List<String> entryNumbersInString = new ArrayList<>();
        for (int i = 0; i < range*col; i++) {
            entryNumbersInString.add("DummyData-" + i);
        }
        Plot[][] plots = createFieldMap(col, range, startRange, startCol, isSerpentine, deletedPlot, entryNumbersInString);
        return plots;
    }
    
    public boolean isDeleted(int col, int range, Map deletedPlot){
        if(deletedPlot.get(col+"_"+range) != null)
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
                    plots[i][j].setDisplayString(stringToDisplay);
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
    
}
