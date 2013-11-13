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
package com.efficio.fieldbook.web.fieldmap.form;

import java.util.List;

import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.pojos.svg.Element;


// TODO: Auto-generated Javadoc
/**
 * The Class FieldmapForm.
 *
 * @author Efficio.Daniel
 */
public class FieldmapForm{
    
    /** The user fieldmap. */
    private UserFieldmap userFieldmap;
    
    private List<String> fieldmapLabels;
    
    private List<Element> fieldmapShapes;
    
    private String markedCells;
    

    public List<String> getFieldmapLabels() {
        return fieldmapLabels;
    }


    
    public void setFieldmapLabels(List<String> fieldmapLabels) {
        this.fieldmapLabels = fieldmapLabels;
    }


    /**
     * Gets the user fieldmap.
     *
     * @return the user fieldmap
     */
    public UserFieldmap getUserFieldmap() {
        return userFieldmap;
    }

    
    /**
     * Sets the user fieldmap.
     *
     * @param userFieldmap the new user fieldmap
     */
    public void setUserFieldmap(UserFieldmap userFieldmap) {
        this.userFieldmap = userFieldmap;
    }



    
    public List<Element> getFieldmapShapes() {
        return fieldmapShapes;
    }



    
    public void setFieldmapShapes(List<Element> fieldmapShapes) {
        this.fieldmapShapes = fieldmapShapes;
    }
    
    
    
    public String getMarkedCells() {
        return markedCells;
    }
    
    public void setMarkedCells(String markedCells) {
        this.markedCells = markedCells;
    }
    
}
