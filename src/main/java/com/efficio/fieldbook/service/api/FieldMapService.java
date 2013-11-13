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

import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.pojos.svg.Element;


public interface FieldMapService {
    
    List<Element> createBlankFieldmap(UserFieldmap info, int startX, int startY);
    List<String> createFieldmap(UserFieldmap info);
    List<Element> createFieldmap(UserFieldmap info, List<String> markedCells, int startX, int startY);
}
