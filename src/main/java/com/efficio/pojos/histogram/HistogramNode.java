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
package com.efficio.pojos.histogram;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * This class holds the data needed for rendering a tree view using dynatree jquery.
 */
public class HistogramNode {
    
    /** The label. */
    private String label;
    
    /** The val. */
    private int val;
    
    
    /**
     * Instantiates a new histogram node.
     *
     * @param label the label
     * @param val the val
     */
    public HistogramNode(String label, int val){
        this.label = label;
        this.val = val;
    }
    
    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Sets the label.
     *
     * @param label the new label
     */
    public void setLabel(String label) {
        this.label = label;
    }
    
    /**
     * Gets the val.
     *
     * @return the val
     */
    public int getVal() {
        return val;
    }
    
    /**
     * Sets the val.
     *
     * @param val the new val
     */
    public void setVal(int val) {
        this.val = val;
    }
    
    
}