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
package com.efficio.fieldbook.service.api;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.service.FieldPlotLayoutIterator;


/**
 * The Interface FieldMapService.
 */
public interface FieldMapService {
    

    /**
     * Creates the dummy data.
     *
     * @param col the col
     * @param range the range
     * @param startRange the start range
     * @param startCol the start col
     * @param isSerpentine the is serpentine
     * @param deletedPlot the deleted plot
     * @return the plot[][]
     */
    Plot[][] createDummyData(int col, int range, int startRange, int startCol, 
            boolean isSerpentine, Map<String, String> deletedPlot, List<FieldMapLabel> fieldMapLabels, 
            FieldPlotLayoutIterator plotLayouIterator);
    
    /**
     * Generate Fieldmap.
     * 
     * @param info
     * @param plotIterator
     * @param isSavedAlready
     * @return
     * @throws MiddlewareQueryException
     */
    public Plot[][] generateFieldmap(UserFieldmap info, FieldPlotLayoutIterator plotIterator, 
    		boolean isSavedAlready, List<String> deletedPlots) 
            throws MiddlewareQueryException;
    
    /**
     * Generate fieldmap.
     *
     * @param info the info
     * @return the plot[][]
     * @throws MiddlewareQueryException the middleware query exception
     */
    Plot[][] generateFieldmap(UserFieldmap info, FieldPlotLayoutIterator plotIterator, boolean isSavedAlready) throws MiddlewareQueryException;
}
