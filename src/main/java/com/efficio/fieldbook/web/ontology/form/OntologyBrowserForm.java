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
    
    /** The trait reference list. */
    private List<TraitReference> traitReferenceList;
    
    /** The tree data. */
    private String treeData;
    //convert to json 1 level for the property and standard variable
    /** The search tree data. */
    private String searchTreeData;
    
    /** The data types. */
    private List<Term> dataTypes;  
    
    /** The roles. */
    private List<Term> roles;
    
    private String traitClass;
    
    private String property;
    
    private String method;
    
    private String scale;
    
    private String traitClassDescription;
    
    private String propertyDescription;
    
    private String methodDescription;
    
    private String scaleDescription;
            
    
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
     * Gets the data types.
     *
     * @return the data types
     */
    public List<Term> getDataTypes() {
        return dataTypes;
    }
    
    /**
     * Sets the data types.
     *
     * @param dataTypes the new data types
     */
    public void setDataTypes(List<Term> dataTypes){
        this.dataTypes = dataTypes;
    }
    
    /**
     * Gets the roles.
     *
     * @return the roles
     */
    public List<Term> getRoles() {
        return roles;
    }
    
    /**
     * Sets the roles.
     *
     * @param roles the new roles
     */
    public void setRoles(List<Term> roles) {
        this.roles = roles;
    }
    
    public String getTraitClass() {
        return traitClass;
    }
    
    public void setTraitClass(String traitClass) {
        this.traitClass = traitClass;
    }
    
    public String getProperty() {
        return property;
    }
    
    public void setProperty(String property) {
        this.property = property;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getScale() {
        return scale;
    }
    
    public void setScale(String scale) {
        this.scale = scale;
    }
    
    public String getTraitClassDescription() {
        return traitClassDescription;
    }
    
    public void setTraitClassDescription(String traitClassDescription) {
        this.traitClassDescription = traitClassDescription;
    }
    
    public String getPropertyDescription() {
        return propertyDescription;
    }
    
    public void setPropertyDescription(String propertyDescription) {
        this.propertyDescription = propertyDescription;
    }
    
    public String getMethodDescription() {
        return methodDescription;
    }
    
    public void setMethodDescription(String methodDescription) {
        this.methodDescription = methodDescription;
    }
    
    public String getScaleDescription() {
        return scaleDescription;
    }
    
    public void setScaleDescription(String scaleDescription) {
        this.scaleDescription = scaleDescription;
    }
    
 }
