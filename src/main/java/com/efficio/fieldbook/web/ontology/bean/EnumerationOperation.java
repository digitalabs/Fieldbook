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
package com.efficio.fieldbook.web.ontology.bean;


/**
 * @author Chezka Camille Arevalo
 *
 */
public class EnumerationOperation{
    private Integer id;
    
    private String name;
    
    private String description;
    
    private int operation;
    
    public EnumerationOperation(Integer id, String name, String description, int operation) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.operation = operation;
    }
    
    public EnumerationOperation() {
        
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    public int getOperation() {
        return operation;
    }

}
