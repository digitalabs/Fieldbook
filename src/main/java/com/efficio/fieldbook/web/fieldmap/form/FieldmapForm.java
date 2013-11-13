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

import org.generationcp.middleware.pojos.Location;

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
    
    /** The fieldmap labels. */
    private List<String> fieldmapLabels;
    
    /** The fieldmap shapes. */
    private List<Element> fieldmapShapes;
    
    /** The marked cells. */
    private String markedCells;
    

    /**
     * Gets the fieldmap labels.
     *
     * @return the fieldmap labels
     */
    public List<String> getFieldmapLabels() {
        return fieldmapLabels;
    }


    
    /**
     * Sets the fieldmap labels.
     *
     * @param fieldmapLabels the new fieldmap labels
     */
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



    
    /**
     * Gets the fieldmap shapes.
     *
     * @return the fieldmap shapes
     */
    public List<Element> getFieldmapShapes() {
        return fieldmapShapes;
    }



    
    /**
     * Sets the fieldmap shapes.
     *
     * @param fieldmapShapes the new fieldmap shapes
     */
    public void setFieldmapShapes(List<Element> fieldmapShapes) {
        this.fieldmapShapes = fieldmapShapes;
    }
    
    
    
    /**
     * Gets the marked cells.
     *
     * @return the marked cells
     */
    public String getMarkedCells() {
        return markedCells;
    }
    
    /**
     * Sets the marked cells.
     *
     * @param markedCells the new marked cells
     */
    public void setMarkedCells(String markedCells) {
        this.markedCells = markedCells;
    }

    
    
}
