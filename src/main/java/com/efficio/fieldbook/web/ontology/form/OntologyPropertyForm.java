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


public class OntologyPropertyForm{
    
    private String comboManageProperty;
    private Integer managePropertyId;
    private String managePropertyName;
    private String managePropertyDescription;
    private List<String> variablesLinkedToProperty;
    
    
    public String getManagePropertyName() {
        return managePropertyName;
    }
    
    public void setManagePropertyName(String managePropertyName) {
        this.managePropertyName = managePropertyName;
    }

    public String getComboManageProperty() {
        return comboManageProperty;
    }
    
    public void setComboManageProperty(String comboManageProperty) {
        this.comboManageProperty = comboManageProperty;
    }
    
    public Integer getManagePropertyId() {
        return managePropertyId;
    }
    
    public void setManagePropertyId(Integer managePropertyId) {
        this.managePropertyId = managePropertyId;
    }
    
    public String getManagePropertyDescription() {
        return managePropertyDescription;
    }
    
    public void setManagePropertyDescription(String managePropertyDescription) {
        this.managePropertyDescription = managePropertyDescription;
    }
    
    public List<String> getVariablesLinkedToProperty() {
        return variablesLinkedToProperty;
    }
    
    public void setVariablesLinkedToProperty(List<String> variablesLinkedToProperty) {
        this.variablesLinkedToProperty = variablesLinkedToProperty;
    }
}
