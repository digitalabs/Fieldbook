package com.efficio.fieldbook.web.ontology.form;

import java.util.List;


public class OntologyScaleForm{

    private String comboManageScale;
    private Integer manageScaleId;
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
    
}
