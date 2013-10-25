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

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TraitReference;


// TODO: Auto-generated Javadoc
/**
 * The Class OntologyBrowserForm.
 *
 * @author Efficio.Daniel
 */
public class OntologyBrowserForm{
    
    /** The has error. */
    private String hasError;
    
    /** The add successful. */
    private String addSuccessful;
    
    /** The trait reference list. */
    private List<TraitReference> traitReferenceList;
    
    /** The tree data. */
    private String treeData;
    //convert to json 1 level for the property and standard variable
    /** The search tree data. */
    private String searchTreeData;
    
    /** The variable name. */
    private String variableName;
    
    /** The variable description. */
    private String variableDescription;
    
    /** The data types. */
    private String dataType;  
    
    /** The roles. */
    private String role;
    
    /** The crop ontology id. */
    private String cropOntologyId;
    
    /** The trait class. */
    private String traitClass;
    
    /** The property. */
    private String property;
    
    /** The method. */
    private String method;
    
    /** The scale. */
    private String scale;
    
    /** The trait class description. */
    private String traitClassDescription;
    
    /** The property description. */
    private String propertyDescription;
    
    /** The method description. */
    private String methodDescription;
    
    /** The scale description. */
    private String scaleDescription;
       
    /**
     * Gets the checks for error.
     *
     * @return the checks for error
     */
    public String getHasError() {
                return hasError;
        }

    /**
     * Sets the checks for error.
     *
     * @param hasError the new checks for error
     */
    public void setHasError(String hasError) {
            this.hasError = hasError;
    }
    
    
    /**
     * Gets the adds the successful.
     *
     * @return the adds the successful
     */
    public String getAddSuccessful() {
        return addSuccessful;
    }
    
    /**
     * Sets the adds the successful.
     *
     * @param addSuccessful the new adds the successful
     */
    public void setAddSuccessful(String addSuccessful) {
        this.addSuccessful = addSuccessful;
    }

    
    /**
     * Gets the tree data.
     *
     * @return the tree data
     */
    public String getTreeData() {
        return treeData;
    }


    
    /**
     * Sets the tree data.
     *
     * @param treeData the new tree data
     */
    public void setTreeData(String treeData) {
        this.treeData = treeData;
    }


    /**
     * Gets the trait reference list.
     *
     * @return the trait reference list
     */
    public List<TraitReference> getTraitReferenceList() {
        return traitReferenceList;
    }
    
    /**
     * Sets the trait reference list.
     *
     * @param traitReferenceList the new trait reference list
     */
    public void setTraitReferenceList(List<TraitReference> traitReferenceList) {
        this.traitReferenceList = traitReferenceList;
    }
    
    /**
     * Gets the search tree data.
     *
     * @return the search tree data
     */
    public String getSearchTreeData() {
        return searchTreeData;
    }

    /**
     * Sets the search tree data.
     *
     * @param searchTreeData the new search tree data
     */
    public void setSearchTreeData(String searchTreeData) {
        this.searchTreeData = searchTreeData;
    }
    
    /**
     * Gets the variable name.
     *
     * @return the variable name
     */
    public String getVariableName() {
        return variableName;
    }
    
    /**
     * Sets the variable name.
     *
     * @param variableName the new variable name
     */
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
    
    /**
     * Gets the variable description.
     *
     * @return the variable description
     */
    public String getVariableDescription() {
        return variableDescription;
    }
    
    /**
     * Sets the variable description.
     *
     * @param variableDescription the new variable description
     */
    public void setVariableDescription(String variableDescription) {
        this.variableDescription = variableDescription;
    }
    
    /**
     * Gets the data types.
     *
     * @return the data types
     */
    public String getDataType() {
        return dataType;
    }
    
    /**
     * Sets the data types.
     *
     * @param dataType the new data type
     */
    public void setDataType(String dataType){
        this.dataType = dataType;
    }
    
    /**
     * Gets the roles.
     *
     * @return the roles
     */
    public String getRole() {
        return role;
    }
    
    /**
     * Sets the roles.
     *
     * @param role the new role
     */
    public void setRole(String role) {
        this.role = role;
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
     * Gets the trait class.
     *
     * @return the trait class
     */
    public String getTraitClass() {
        return traitClass;
    }
    
    /**
     * Sets the trait class.
     *
     * @param traitClass the new trait class
     */
    public void setTraitClass(String traitClass) {
        this.traitClass = traitClass;
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
    public void setProperty(String property) {
        this.property = property;
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
    public void setMethod(String method) {
        this.method = method;
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
    public void setScale(String scale) {
        this.scale = scale;
    }
    
    /**
     * Gets the trait class description.
     *
     * @return the trait class description
     */
    public String getTraitClassDescription() {
        return traitClassDescription;
    }
    
    /**
     * Sets the trait class description.
     *
     * @param traitClassDescription the new trait class description
     */
    public void setTraitClassDescription(String traitClassDescription) {
        this.traitClassDescription = traitClassDescription;
    }
    
    /**
     * Gets the property description.
     *
     * @return the property description
     */
    public String getPropertyDescription() {
        return propertyDescription;
    }
    
    /**
     * Sets the property description.
     *
     * @param propertyDescription the new property description
     */
    public void setPropertyDescription(String propertyDescription) {
        this.propertyDescription = propertyDescription;
    }
    
    /**
     * Gets the method description.
     *
     * @return the method description
     */
    public String getMethodDescription() {
        return methodDescription;
    }
    
    /**
     * Sets the method description.
     *
     * @param methodDescription the new method description
     */
    public void setMethodDescription(String methodDescription) {
        this.methodDescription = methodDescription;
    }
    
    /**
     * Gets the scale description.
     *
     * @return the scale description
     */
    public String getScaleDescription() {
        return scaleDescription;
    }
    
    /**
     * Sets the scale description.
     *
     * @param scaleDescription the new scale description
     */
    public void setScaleDescription(String scaleDescription) {
        this.scaleDescription = scaleDescription;
    }
    
 }
