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
package com.efficio.fieldbook.web.label.printing.bean;

import java.io.Serializable;

/**
 * The Class LabelFields.
 *
 * @author Efficio.Daniel
 */
public class LabelFields implements Serializable{
    
    private static final long serialVersionUID = 1L;

    /** The name. */
    private String name;
    
    /** The id. */
    private int id;
    
    /**
     * Instantiates a new label fields.
     *
     * @param name the name
     * @param id the id
     */
    public LabelFields(String name, int id){
        this.name = name;
        this.id = id;
    }
    
    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(int id) {
        this.id = id;
    }
    
}
