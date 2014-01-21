package com.efficio.fieldbook.service.api;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;

import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

// TODO: Auto-generated Javadoc
/**
 * The Interface FieldPlotLayoutIterator.
 */
public interface FieldPlotLayoutIterator {
	
	
    
    /**
     * Creates the field map.
     *
     * @param col the col
     * @param range the range
     * @param startRange the start range
     * @param startCol the start col
     * @param isSerpentine the is serpentine
     * @param deletedPlot the deleted plot
     * @param labels the labels
     * @param isTrial the is trial
     * @return the plot[][]
     */
    Plot[][] createFieldMap(int col, int range, int startRange, int startCol, boolean isSerpentine, Map deletedPlot, List<FieldMapLabel> labels, boolean isTrial);
    
    /**
     * Sets the other field map information.
     * 
     * This would be use when generating a field map from a saved information in the DB
     *
     * @param info the info
     * @param plots the plots
     * @param totalColumns the total columns
     * @param totalRanges the total ranges
     * @param isSerpentine the is serpentine
     */
    void setOtherFieldMapInformation(UserFieldmap info, Plot[][] plots, int totalColumns, int totalRanges, boolean isSerpentine);
    
}
