package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;

import com.efficio.fieldbook.service.api.FieldPlotLayoutIterator;
import com.efficio.fieldbook.util.FieldMapUtilityHelper;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

public class VerticalFieldMapLayoutIterator implements
		FieldPlotLayoutIterator {

	
	/* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.FieldMapService#createFieldMap(int, int, int, int, boolean, java.util.Map, java.util.List, boolean)
     */
    public Plot[][] createFieldMap(int col, int range, int startRange,
            int startCol, boolean isSerpentine, Map deletedPlot,
            List<FieldMapLabel> labels, boolean isTrial) {
        
    	Plot[][] plots = FieldMapUtilityHelper.initializePlots(col, range);
        //this is how we populate data
        int counter = 0;
        //we need to take note of the start range
        boolean isStartOk = false;
        boolean downToUp = true;
        for(int x = 0; x < col ; x++){        		
                if(downToUp){
                    for(int y = 0 ; y < range ; y++){
                        //for upload planting
                        if(x == startCol && y == startRange){
                            //this will signify that we have started
                            isStartOk = true;
                        }
                        counter = FieldMapUtilityHelper.populatePlotData(counter, labels, x, y, plots, false, startCol, startRange, isStartOk, deletedPlot, 
                                isTrial);
                    }
                }else{
                    for(int y = range - 1 ; y >= 0 ; y--){
                        //for downward planting
                        if(x == startCol && y == startRange){
                            //this will signify that we have started
                            isStartOk = true;
                        }
                        counter = FieldMapUtilityHelper.populatePlotData(counter, labels, x, y, plots, false, startCol, startRange, isStartOk, deletedPlot,
                                isTrial);

                    }
                }
                if(isSerpentine){
    	        	downToUp = !downToUp;
    	        }

        }
        return plots;
    }

    /**
     * Sets the other field map information.
     *
     * @param info the info
     * @param plots the plots
     * @param totalColumns the total columns
     * @param totalRanges the total ranges
     * @param isSerpentine the is serpentine
     */
    public void setOtherFieldMapInformation(UserFieldmap info, Plot[][] plots, int totalColumns, int totalRanges, boolean isSerpentine) {
        boolean isStarted = false;
        List<String> possiblyDeletedCoordinates = new ArrayList<String>();
        int[] order = {1};
        boolean downToUp = true;
        for (int x = 0; x < totalColumns; x++) {
            if (downToUp) {
            	for (int y = 0; y < totalRanges; y++) {
                    isStarted = FieldMapUtilityHelper.renderPlotCell(info, plots, x, y, isStarted, possiblyDeletedCoordinates, order);
                }
            }
            else {                
                for (int y = totalRanges - 1; y >= 0; y--) {
                    isStarted = FieldMapUtilityHelper.renderPlotCell(info, plots, x, y, isStarted, possiblyDeletedCoordinates, order);
                }
            }
            if(isSerpentine){
	        	downToUp = !downToUp;
	        }
        }
        info.setSelectedFieldmapList(new SelectedFieldmapList(info.getSelectedFieldMaps(), info.isTrial()));
    }
}
