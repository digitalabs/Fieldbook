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

import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;


public interface FieldMapService {
    
    List<String> generateFieldMapLabels(UserFieldmap info);
    
    //added by Daniel
    Plot[][] createFieldMap(int col, int range, int startRange, int startCol, boolean isSerpentine, Map deletedPlot, List<String> entryNumbersInString);
}
