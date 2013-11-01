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


public class OntologyMethodForm{

    private String comboManageMethod;
    private Integer manageMethodId;
    private String manageMethodName;
    private String manageMethodDescription;
    private List<String> variablesLinkedToMethod;
    
    
    public String getManageMethodName() {
        return manageMethodName;
    }
    
    public void setManageMethodName(String manageMethodName) {
        this.manageMethodName = manageMethodName;
    }

    public String getComboManageMethod() {
        return comboManageMethod;
    }
    
    public void setComboManageMethod(String comboManageMethod) {
        this.comboManageMethod = comboManageMethod;
    }
    
    public Integer getManageMethodId() {
        return manageMethodId;
    }
    
    public void setManageMethodId(Integer manageMethodId) {
        this.manageMethodId = manageMethodId;
    }
    
    public String getManageMethodDescription() {
        return manageMethodDescription;
    }
    
    public void setManageMethodDescription(String manageMethodDescription) {
        this.manageMethodDescription = manageMethodDescription;
    }
    
    public List<String> getVariablesLinkedToMethod() {
        return variablesLinkedToMethod;
    }
    
    public void setVariablesLinkedToMethod(List<String> variablesLinkedToMethod) {
        this.variablesLinkedToMethod = variablesLinkedToMethod;
    }
}
