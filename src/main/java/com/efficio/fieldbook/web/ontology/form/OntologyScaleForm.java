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


public class OntologyScaleForm{

    private String comboManageScale;
    private Integer manageScaleId;
    private String manageScaleName;
    private String manageScaleDescription;
    private List<String> variablesLinkedToScale;
    
    public String getComboManageScale() {
        return comboManageScale;
    }
    
    public void setComboManageScale(String comboManageScale) {
        this.comboManageScale = comboManageScale;
    }
    
    public Integer getManageScaleId() {
        return manageScaleId;
    }
    
    public void setManageScaleId(Integer manageScaleId) {
        this.manageScaleId = manageScaleId;
    }
    
    public String getManageScaleDescription() {
        return manageScaleDescription;
    }
    
    public void setManageScaleDescription(String manageScaleDescription) {
        this.manageScaleDescription = manageScaleDescription;
    }
    
    public List<String> getVariablesLinkedToScale() {
        return variablesLinkedToScale;
    }
    
    public void setVariablesLinkedToScale(List<String> variablesLinkedToScale) {
        this.variablesLinkedToScale = variablesLinkedToScale;
    }

    public String getManageScaleName() {
        return manageScaleName;
    }

    public void setManageScaleName(String manageScaleName) {
        this.manageScaleName = manageScaleName;
    }
    
}
