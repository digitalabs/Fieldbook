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

import java.util.List;
import java.util.Map;


import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.util.FieldMapUtilityHelper;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.service.FieldPlotLayoutIterator;

/**
 * The Class FieldMapServiceImpl.
 */
@Service
public class FieldMapServiceImpl implements FieldMapService{
         
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.FieldMapService#createDummyData(int, int, int, int, boolean, java.util.Map)
     */
    @Override
    public Plot[][] createDummyData(int col, int range, int startRange, int startCol, 
    		boolean isSerpentine, Map deletedPlot, List<FieldMapLabel> fieldMapLabels, 
    		FieldPlotLayoutIterator plotLayouIterator) {
        startRange--;
        startCol--;
        /*
        List<FieldMapLabel> labels = new ArrayList<FieldMapLabel>();
        for (int i = 0; i < range*col; i++) {
            FieldMapLabel label = new FieldMapLabel(null, null, "DummyData-" + i, null, null);
            label.setStudyName("Dummy Trial");
            labels.add(label);
        }
        */
        //Plot[][] plots = createFieldMap(col, range, startRange, startCol
        //                  , isSerpentine, deletedPlot, labels, true);
        //for testing only
        Plot[][] plots = plotLayouIterator.createFieldMap(col, range
                , startRange, startCol, isSerpentine, deletedPlot, fieldMapLabels, true);
        //setOtherFieldMapInformation(info, plots, totalColumns, totalRanges, isSerpentine);
        return plots;
    }
    
    
    
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.FieldMapService#generateFieldmap(com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap)
     */
    @Override
    public Plot[][] generateFieldmap(UserFieldmap info, FieldPlotLayoutIterator plotIterator) 
            throws MiddlewareQueryException {
        
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
                        //plot.setUpward(false);
                    }
                    plot.setDisplayString(FieldMapUtilityHelper.getDisplayString(label, info.isTrial()));
                    plot.setNotStarted(false);
                }
                else {
                    throw new MiddlewareQueryException(
                            "The Column/Range of the Field Map exceeded the Total Columns/Ranges");
                }
            }
        }
        
        plotIterator.setOtherFieldMapInformation(info, plots, totalColumns, totalRanges, isSerpentine);
        return plots;
    }
        
    /**
     * Initialize field map array.
     *
     * @param plots the plots
     * @param totalColumns the total columns
     * @param totalRanges the total ranges
     */
    private void initializeFieldMapArray(Plot[][] plots, int totalColumns, int totalRanges) {
        for (int i = 0; i < totalColumns; i++) {
            for (int j = 0; j < totalRanges; j++) {
                
                Plot plot = new Plot(i, j, "");
                plots[i][j] = plot;
                plot.setNotStarted(false);
                //plot.setUpward(true);
            }
        }
    }
    
    
    
    /**
     * Mark deleted coordinates.
     *
     * @param plots the plots
     * @param deletedCoordinates the deleted coordinates
     */
    @SuppressWarnings("unused")
    private void markDeletedCoordinates(Plot[][] plots, List<String> deletedCoordinates) {
        for (String deletedIndex : deletedCoordinates) {
            String[] columnRange = deletedIndex.split("_");
            int column = Integer.parseInt(columnRange[0]);
            int range = Integer.parseInt(columnRange[1]);
            plots[column][range].setPlotDeleted(true);
        }
    }
    
    
}
