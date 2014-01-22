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

/**
 * The Class OntologyPropertyForm.
 */
public class OntologyPropertyForm implements OntologyModalForm {
    
    /** The combo manage property. */
    private String comboManageProperty;
    
    /** The manage property id. */
    private Integer managePropertyId;
    
    /** The manage property name. */
    private String managePropertyName;
    
    /** The manage property description. */
    private String managePropertyDescription;
    
    /** The combo manage prop trait class. */
    private String comboManagePropTraitClass;
    
    /** The manage prop trait class id. */
    private Integer managePropTraitClassId;
    
    /** The manage prop trait class name. */
    private String managePropTraitClassName;
    
    /** The variables linked to property. */
    private List<String> variablesLinkedToProperty;
    
    /** The crop ontology id. */
    private String cropOntologyId;
    
    /**
     * Gets the combo manage property.
     *
     * @return the combo manage property
     */
    public String getComboManageProperty() {
        return comboManageProperty;
    }
    
    /**
     * Sets the combo manage property.
     *
     * @param comboManageProperty the new combo manage property
     */
    public void setComboManageProperty(String comboManageProperty) {
        this.comboManageProperty = comboManageProperty;
    }
    
    /**
     * Gets the manage property id.
     *
     * @return the manage property id
     */
    public Integer getManagePropertyId() {
        return managePropertyId;
    }
    
    /**
     * Sets the manage property id.
     *
     * @param managePropertyId the new manage property id
     */
    public void setManagePropertyId(Integer managePropertyId) {
        this.managePropertyId = managePropertyId;
    }
    
    /**
     * Gets the manage property name.
     *
     * @return the manage property name
     */
    public String getManagePropertyName() {
        return managePropertyName;
    }
    
    /**
     * Sets the manage property name.
     *
     * @param managePropertyName the new manage property name
     */
    public void setManagePropertyName(String managePropertyName) {
        this.managePropertyName = managePropertyName;
    }
    
    /**
     * Gets the manage property description.
     *
     * @return the manage property description
     */
    public String getManagePropertyDescription() {
        return managePropertyDescription;
    }
    
    /**
     * Sets the manage property description.
     *
     * @param managePropertyDescription the new manage property description
     */
    public void setManagePropertyDescription(String managePropertyDescription) {
        this.managePropertyDescription = managePropertyDescription;
    }
    
    /**
     * Gets the combo manage prop trait class.
     *
     * @return the combo manage prop trait class
     */
    public String getComboManagePropTraitClass() {
        return comboManagePropTraitClass;
    }
    
    /**
     * Sets the combo manage prop trait class.
     *
     * @param comboManagePropTraitClass the new combo manage prop trait class
     */
    public void setComboManagePropTraitClass(String comboManagePropTraitClass) {
        this.comboManagePropTraitClass = comboManagePropTraitClass;
    }
    
    /**
     * Gets the manage prop trait class id.
     *
     * @return the manage prop trait class id
     */
    public Integer getManagePropTraitClassId() {
        return managePropTraitClassId;
    }
    
    /**
     * Sets the manage prop trait class id.
     *
     * @param managePropTraitClassId the new manage prop trait class id
     */
    public void setManagePropTraitClassId(Integer managePropTraitClassId) {
        this.managePropTraitClassId = managePropTraitClassId;
    }
    
    /**
     * Gets the manage prop trait class name.
     *
     * @return the manage prop trait class name
     */
    public String getManagePropTraitClassName() {
        return managePropTraitClassName;
    }
    
    /**
     * Sets the manage prop trait class name.
     *
     * @param managePropTraitClassName the new manage prop trait class name
     */
    public void setManagePropTraitClassName(String managePropTraitClassName) {
        this.managePropTraitClassName = managePropTraitClassName;
    }
    
    /**
     * Gets the variables linked to property.
     *
     * @return the variables linked to property
     */
    public List<String> getVariablesLinkedToProperty() {
        return variablesLinkedToProperty;
    }
    
    /**
     * Sets the variables linked to property.
     *
     * @param variablesLinkedToProperty the new variables linked to property
     */
    public void setVariablesLinkedToProperty(List<String> variablesLinkedToProperty) {
        this.variablesLinkedToProperty = variablesLinkedToProperty;
    }

    /**
     * Gets the crop ontology id.
     *
     * @return the cropOntologyId
     */
    public String getCropOntologyId() {
        return cropOntologyId;
    }

    /**
     * Sets the crop ontology id.
     *
     * @param cropOntologyId the cropOntologyId to set
     */
    public void setCropOntologyId(String cropOntologyId) {
        this.cropOntologyId = cropOntologyId;
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#isAddMode()
     */
    @Override
    public boolean isAddMode() {
        return managePropertyId == null;
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#getName()
     */
    @Override
    public String getName() {
        return getManagePropertyName();
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#getId()
     */
    @Override
    public Integer getId() {
        return getManagePropertyId();
    }
    
}
