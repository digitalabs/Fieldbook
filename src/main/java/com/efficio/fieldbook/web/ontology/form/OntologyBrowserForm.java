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
import org.generationcp.middleware.domain.oms.TraitReference;


/**
 * @author Efficio.Daniel
 *
 */
public class OntologyBrowserForm{
    private List<TraitReference> traitReferenceList;
    private String treeData;
    //convert to json 1 level for the property and standard variable
    private String searchTreeData;
            
    
    public String getTreeData() {
        return treeData;
    }


    
    public void setTreeData(String treeData) {
        this.treeData = treeData;
    }


    public List<TraitReference> getTraitReferenceList() {
        return traitReferenceList;
    }

    
    public void setTraitReferenceList(List<TraitReference> traitReferenceList) {
        this.traitReferenceList = traitReferenceList;
    }



    
    public String getSearchTreeData() {
        return searchTreeData;
    }



    
    public void setSearchTreeData(String searchTreeData) {
        this.searchTreeData = searchTreeData;
    }
    
    

}
