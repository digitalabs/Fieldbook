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
    
    private List<Term> dataTypes;  
    private List<Term> roles;
    private List<TraitReference> traitClasses;
    private List<Term> properties;
    private List<Term> methods;
    private List<Term> scales;            
    
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
    
    
    public List<Term> getDataTypes() {
        return dataTypes;
    }
    
    public void setDataTypes(List<Term> dataTypes){
        this.dataTypes = dataTypes;
    }
    
    public List<Term> getRoles() {
        return roles;
    }
    
    public void setRoles(List<Term> roles) {
        this.roles = roles;
    }
    
    public List<TraitReference> getTraitClasses() {
        return traitClasses;
    }
    
    public void setTraitClasses(List<TraitReference> traitClasses) {
        this.traitClasses = traitClasses;
    }
    
    public List<Term> getProperties() {
        return properties;
    }
    
    public void setProperties(List<Term> properties) {
        this.properties = properties;
    }
    
    public List<Term> getMethods() {
        return methods;
    }
    
    public void setMethods(List<Term> methods) {
        this.methods = methods;
    }
    
    public List<Term> getScales() {
        return scales;
    }
    
    public void setScales(List<Term> scales) {
        this.scales = scales;
    }
}
