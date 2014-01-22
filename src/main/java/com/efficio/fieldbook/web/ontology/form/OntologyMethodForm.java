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
 * The Class OntologyMethodForm.
 */
public class OntologyMethodForm implements OntologyModalForm {

    /** The combo manage method. */
    private String comboManageMethod;
    
    /** The manage method id. */
    private Integer manageMethodId;
    
    /** The manage method name. */
    private String manageMethodName;
    
    /** The manage method description. */
    private String manageMethodDescription;
    
    /** The variables linked to method. */
    private List<String> variablesLinkedToMethod;
    
    
    /**
     * Gets the manage method name.
     *
     * @return the manage method name
     */
    public String getManageMethodName() {
        return manageMethodName;
    }
    
    /**
     * Sets the manage method name.
     *
     * @param manageMethodName the new manage method name
     */
    public void setManageMethodName(String manageMethodName) {
        this.manageMethodName = manageMethodName;
    }

    /**
     * Gets the combo manage method.
     *
     * @return the combo manage method
     */
    public String getComboManageMethod() {
        return comboManageMethod;
    }
    
    /**
     * Sets the combo manage method.
     *
     * @param comboManageMethod the new combo manage method
     */
    public void setComboManageMethod(String comboManageMethod) {
        this.comboManageMethod = comboManageMethod;
    }
    
    /**
     * Gets the manage method id.
     *
     * @return the manage method id
     */
    public Integer getManageMethodId() {
        return manageMethodId;
    }
    
    /**
     * Sets the manage method id.
     *
     * @param manageMethodId the new manage method id
     */
    public void setManageMethodId(Integer manageMethodId) {
        this.manageMethodId = manageMethodId;
    }
    
    /**
     * Gets the manage method description.
     *
     * @return the manage method description
     */
    public String getManageMethodDescription() {
        return manageMethodDescription;
    }
    
    /**
     * Sets the manage method description.
     *
     * @param manageMethodDescription the new manage method description
     */
    public void setManageMethodDescription(String manageMethodDescription) {
        this.manageMethodDescription = manageMethodDescription;
    }
    
    /**
     * Gets the variables linked to method.
     *
     * @return the variables linked to method
     */
    public List<String> getVariablesLinkedToMethod() {
        return variablesLinkedToMethod;
    }
    
    /**
     * Sets the variables linked to method.
     *
     * @param variablesLinkedToMethod the new variables linked to method
     */
    public void setVariablesLinkedToMethod(List<String> variablesLinkedToMethod) {
        this.variablesLinkedToMethod = variablesLinkedToMethod;
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#isAddMode()
     */
    @Override
    public boolean isAddMode() {
        return manageMethodId == null;
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#getName()
     */
    @Override
    public String getName() {
        return getManageMethodName();
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#getId()
     */
    @Override
    public Integer getId() {
        return getManageMethodId();
    }
}
