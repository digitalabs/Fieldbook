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


public class OntologyTraitClassForm implements OntologyModalForm {

    private String comboManageTraitClass;
    private Integer manageTraitClassId;
    private String manageTraitClassName;
    private String manageTraitClassDescription;
    private String comboManageParentTraitClass;
    private Integer manageParentTraitClassId;
    private String manageParentTraitClassName;
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
    
    public String getManageTraitClassName() {
        return manageTraitClassName;
    }
    
    public void setManageTraitClassName(String manageTraitClassName) {
        this.manageTraitClassName = manageTraitClassName;
    }
    
    public String getManageTraitClassDescription() {
        return manageTraitClassDescription;
    }
    
    public void setManageTraitClassDescription(String manageTraitClassDescription) {
        this.manageTraitClassDescription = manageTraitClassDescription;
    }
    
    public String getComboManageParentTraitClass() {
        return comboManageParentTraitClass;
    }

    public void setComboManageParentTraitClass(String comboManageParentTraitClass) {
        this.comboManageParentTraitClass = comboManageParentTraitClass;
    }
    
    public Integer getManageParentTraitClassId() {
        return manageParentTraitClassId;
    }

    public void setManageParentTraitClassId(Integer manageParentTraitClassId) {
        this.manageParentTraitClassId = manageParentTraitClassId;
    }
    
    public String getManageParentTraitClassName() {
        return manageParentTraitClassName;
    }
    
    public void setManageParentTraitClassName(String manageParentTraitClassName) {
        this.manageParentTraitClassName = manageParentTraitClassName;
    }

    public List<String> getVariablesLinkedToTraitClass() {
        return variablesLinkedToTraitClass;
    }
    
    public void setVariablesLinkedToTraitClass(
            List<String> variablesLinkedToTraitClass) {
        this.variablesLinkedToTraitClass = variablesLinkedToTraitClass;
    }

    @Override
    public boolean isAddMode() {
        return manageTraitClassId == null;
    }

    @Override
    public String getName() {
        return getManageTraitClassName();
    }

    @Override
    public Integer getId() {
        return getManageTraitClassId();
    }
    
    
}
