package com.efficio.fieldbook.service;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;

import com.efficio.fieldbook.service.api.FieldPlotLayoutIterator;
import com.efficio.fieldbook.util.FieldMapUtilityHelper;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;

public class VerticalFieldMapLayoutIterator implements
		FieldPlotLayoutIterator {

	
	/* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.FieldMapService#createFieldMap(int, int, int, int, boolean, java.util.Map, java.util.List, boolean)
     */
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
                        counter = FieldMapUtilityHelper.populatePlotData(counter, labels, i, j, plots, isUpward, startCol, startRange, isStartOk, deletedPlot, 
                                isTrial);
                    }
                }else{
                    for(int j = range - 1 ; j >= 0 ; j--){
                        //for downward planting
                        if(i == startCol && j == startRange){
                            //this will signify that we have started
                            isStartOk = true;
                        }
                        counter = FieldMapUtilityHelper.populatePlotData(counter, labels, i, j, plots, isUpward, startCol, startRange, isStartOk, deletedPlot,
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

}
