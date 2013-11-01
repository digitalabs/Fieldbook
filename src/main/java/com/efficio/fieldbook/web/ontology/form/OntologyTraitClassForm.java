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


public class OntologyTraitClassForm{

    private String comboManageTraitClass;
    private Integer manageTraitClassId;
    private String manageTraitClassDescription;
    private List<String> variablesLinkedToTraitClass;
    
    public String getComboManageTraitClass() {
        return comboManageTraitClass;
    }
    
    public void setComboManageTraitClass(String comboManageTraitClass) {
        this.comboManageTraitClass = comboManageTraitClass;
    }
    
    public Integer getManageTraitClassId() {
        return manageTraitClassId;
    }
    
    public void setManageTraitClassId(Integer manageTraitClassId) {
        this.manageTraitClassId = manageTraitClassId;
    }
    
    public String getManageTraitClassDescription() {
        return manageTraitClassDescription;
    }
    
    public void setManageTraitClassDescription(String manageTraitClassDescription) {
        this.manageTraitClassDescription = manageTraitClassDescription;
    }
    
    public List<String> getVariablesLinkedToTraitClass() {
        return variablesLinkedToTraitClass;
    }
    
    public void setVariablesLinkedToTraitClass(
            List<String> variablesLinkedToTraitClass) {
        this.variablesLinkedToTraitClass = variablesLinkedToTraitClass;
    }

}
