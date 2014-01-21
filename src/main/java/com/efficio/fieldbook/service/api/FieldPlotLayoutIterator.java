package com.efficio.fieldbook.service.api;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;

import com.efficio.fieldbook.web.fieldmap.bean.Plot;

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
    
    
    
}
