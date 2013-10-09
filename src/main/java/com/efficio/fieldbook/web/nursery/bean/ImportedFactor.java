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
package com.efficio.fieldbook.web.nursery.bean;

import java.io.Serializable;

/**
 * The Class ImportedFactor.
 */
public class ImportedFactor  implements Serializable{
    
    private static final long serialVersionUID = 1L;

    /** The factor. */
    private String factor;
    
    /** The description. */
    private String description;
    
    /** The property. */
    private String property;
    
    /** The scale. */
    private String scale;
    
    /** The method. */
    private String method;
    
    /** The data type. */
    private String dataType;
    
    /** The nested in. */
    private String nestedIn;
    
    /** The label. */
    private String label;

    /**
     * Instantiates a new imported factor.
     */
    public ImportedFactor(){
    }
    
    /**
     * Instantiates a new imported factor.
     *
     * @param factor the factor
     * @param description the description
     * @param property the property
     * @param scale the scale
     * @param method the method
     * @param dataType the data type
     * @param label the label
     */
    public ImportedFactor(String factor, String description, String property, String scale, String method
            , String dataType, String label) {
        this.factor = factor;
        this.description = description;
        this.property = property;
        this.scale = scale;
        this.method = method;
        this.dataType = dataType;
        this.label = label;
    }
    
    /**
     * Instantiates a new imported factor.
     *
     * @param factor the factor
     * @param description the description
     * @param property the property
     * @param scale the scale
     * @param method the method
     * @param dataType the data type
     * @param nestedIn the nested in
     * @param label the label
     */
    public ImportedFactor(String factor, String description, String property, String scale, String method
            , String dataType, String nestedIn, String label) {
        this.factor = factor;
        this.description = description;
        this.property = property;
        this.scale = scale;
        this.method = method;
        this.dataType = dataType;
        this.nestedIn = nestedIn;
        this.label = label;
    }    
    
    /**
     * Gets the factor.
     *
     * @return the factor
     */
    public String getFactor() {
        return factor;
    }
    
    /**
     * Sets the factor.
     *
     * @param factor the new factor
     */
    public void setFactor(String factor){
        this.factor = factor;
    }
    
    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description){
        this.description = description;
    }
    
    /**
     * Gets the property.
     *
     * @return the property
     */
    public String getProperty() {
        return property;
    }
    
    /**
     * Sets the property.
     *
     * @param property the new property
     */
    public void setProperty(String property){
        this.property = property;
    }
    
    /**
     * Gets the scale.
     *
     * @return the scale
     */
    public String getScale() {
        return scale;
    }
    
    /**
     * Sets the scale.
     *
     * @param scale the new scale
     */
    public void setScale(String scale){
        this.scale = scale;
    }
    
    /**
     * Gets the method.
     *
     * @return the method
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * Sets the method.
     *
     * @param method the new method
     */
    public void setMethod(String method){
        this.method = method;
    }
    
    /**
     * Gets the data type.
     *
     * @return the data type
     */
    public String getDataType() {
        return dataType;
    }
    
    /**
     * Sets the data type.
     *
     * @param dataType the new data type
     */
    public void setDataType(String dataType){
        this.dataType = dataType;
    }
    
    /**
     * Gets the nested in.
     *
     * @return the nested in
     */
    public String getNestedIn() {
        return nestedIn;
    }
    
    /**
     * Sets the nested in.
     *
     * @param nestedIn the new nested in
     */
    public void setNestedIn(String nestedIn){
        this.nestedIn = nestedIn;
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
    public void setLabel(String label){
        this.label = label;
    }
    
}