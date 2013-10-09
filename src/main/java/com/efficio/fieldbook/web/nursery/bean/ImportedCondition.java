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
 * The Class ImportedCondition.
 */
public class ImportedCondition implements Serializable{
    
    private static final long serialVersionUID = 1L;

    /** The condition. */
    private String condition;
    
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
    
    /** The value. */
    private String value;
    
    /** The label. */
    private String label;

    /**
     * Instantiates a new imported condition.
     */
    public ImportedCondition(){
        
    }
    
    /**
     * Instantiates a new imported condition.
     *
     * @param condition the condition
     * @param description the description
     * @param property the property
     * @param scale the scale
     * @param method the method
     * @param dataType the data type
     * @param value the value
     * @param label the label
     */
    public ImportedCondition(String condition, String description, String property, String scale, String method
            , String dataType, String value, String label) {
        this.condition = condition;
        this.description = description;
        this.property = property;
        this.scale = scale;
        this.method = method;
        this.dataType = dataType;
        this.value = value;
        this.label = label;
    }
    
    /**
     * Gets the condition.
     *
     * @return the condition
     */
    public String getCondition() {
        return condition;
    }
    
    /**
     * Sets the condition.
     *
     * @param condition the new condition
     */
    public void setCondition(String condition){
        this.condition = condition;
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
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value){
        this.value = value;
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