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
package com.efficio.fieldbook.web.ontology.form;

import java.util.List;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;

/**
 * The Class OntologyBrowserForm.
 *
 * @author Efficio.Daniel
 */
public class OntologyDetailsForm{
    
    /** The standard variable. */
    StandardVariable variable;
    
    /** The project count. */
    String projectCount;
    
    /** The observation count. */
    String observationCount;
    
    /** The trait class name. */
    private String traitClassName;
    
    /** The property name. */
    private String propertyName;
    
    /** The role name. */
    private String roleName;
    
    /** The method name. */
    private String methodName;
    
    /** The scale name. */
    private String scaleName;
    
    /** The data type name. */
    private String dataTypeName;
    
    /** The crop ontology id. */
    private String cropOntologyId;    
    
    /** The constraints that contains the min and max values for numeric variables, if any. */
    private VariableConstraints constraints;

    /** The enumerated values. */
    private List<Enumeration> categoricalValues;
    
    private String possiblePairsJson;
   
	/**
     * Gets the trait class name.
     *
     * @return the trait class name
     */
    public String getTraitClassName() {
        return traitClassName;
    }

    /**
     * Sets the trait class name.
     *
     * @param traitClassName the new trait class name
     */
    public void setTraitClassName(String traitClassName) {
        this.traitClassName = traitClassName;
    }

    /**
     * Gets the property name.
     *
     * @return the property name
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the property name.
     *
     * @param propertyName the new property name
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Gets the role name.
     *
     * @return the role name
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Sets the role name.
     *
     * @param roleName the new role name
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    /**
     * Gets the method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }
    
    /**
     * Sets the method name.
     *
     * @param methodName the new method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    /**
     * Gets the scale name.
     *
     * @return the scale name
     */
    public String getScaleName() {
        return scaleName;
    }
    
    /**
     * Sets the scale name.
     *
     * @param scaleName the new scale name
     */
    public void setScaleName(String scaleName) {
        this.scaleName = scaleName;
    }
    
    /**
     * Gets the data type name.
     *
     * @return the data type name
     */
    public String getDataTypeName() {
        return dataTypeName;
    }
    
    /**
     * Sets the data type name.
     *
     * @param dataTypeName the new data type name
     */
    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    /**
     * Gets the project count.
     *
     * @return the project count
     */
    public String getProjectCount() {
        return projectCount;
    }
    
    /**
     * Sets the project count.
     *
     * @param projectCount the new project count
     */
    public void setProjectCount(String projectCount) {
        this.projectCount = projectCount;
    }
    
    /**
     * Gets the observation count.
     *
     * @return the observation count
     */
    public String getObservationCount() {
        return observationCount;
    }
    
    /**
     * Sets the observation count.
     *
     * @param observationCount the new observation count
     */
    public void setObservationCount(String observationCount) {
        this.observationCount = observationCount;
    }

    /**
     * Gets the variable.
     *
     * @return the variable
     */
    public StandardVariable getVariable() {
        return variable;
    }

    /**
     * Gets the crop ontology id.
     *
     * @return the crop ontology id
     */
    public String getCropOntologyId() {
        return cropOntologyId;
    }

    /**
     * Sets the crop ontology id.
     *
     * @param cropOntologyId the new crop ontology id
     */
    public void setCropOntologyId(String cropOntologyId) {
        this.cropOntologyId = cropOntologyId;
    }

    /**
     * Gets the enumerated values.
     *
     * @return the enumerated values
     */
    public List<Enumeration> getCategoricalValues() {
        return categoricalValues;
    }

    /**
     * Sets the enumerated values.
     *
     * @param categoricalValues the new categorical values
     */
    public void setCategoricalValues(List<Enumeration> categoricalValues) {
        this.categoricalValues = categoricalValues;
    }
    

    
    /**
     * Gets the constraints.
     *
     * @return the constraints
     */
    public VariableConstraints getConstraints() {
        return constraints;
    }

    
    /**
     * Sets the constraints.
     *
     * @param constraints the new constraints
     */
    public void setConstraints(VariableConstraints constraints) {
        this.constraints = constraints;
    }
    
    
    /**
     * Gets the minimum value from the constraints.
     *
     * @return the min value
     */
    public Double getMinValue() {
        if (constraints != null){
            return constraints.getMinValue();
        }
        return null;
    }
    
    /**
     * Gets the max value.
     *
     * @return the max value
     */
    public Double getMaxValue() {
        if (constraints != null){
            return constraints.getMaxValue();
        }
        return null;
    }

    /**
     * Sets the variable.
     *
     * @param variable the new variable
     */
    public void setVariable(StandardVariable variable) {
        this.variable = variable;
        if(variable != null){
            setTraitClassName(variable.getIsA().getName());
            setPropertyName(variable.getProperty().getName());
            setRoleName(variable.getStoredIn().getName());
            setMethodName(variable.getMethod().getName());
            setScaleName(variable.getScale().getName());
            setDataTypeName(variable.getDataType().getName());
            setCropOntologyId(variable.getCropOntologyId());
            setConstraints(variable.getConstraints());
            setCategoricalValues(variable.getEnumerations());
        }
    }

	public String getPossiblePairsJson() {
		return possiblePairsJson;
	}

	public void setPossiblePairsJson(String possiblePairsJson) {
		this.possiblePairsJson = possiblePairsJson;
	}


 }
