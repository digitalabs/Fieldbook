/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.ontology.form;

import java.util.List;

/**
 * The Class OntologyTraitClassForm.
 */
public class OntologyTraitClassForm implements OntologyModalForm {

	/** The combo manage trait class. */
	private String comboManageTraitClass;

	/** The manage trait class id. */
	private Integer manageTraitClassId;

	/** The manage trait class name. */
	private String manageTraitClassName;

	/** The manage trait class description. */
	private String manageTraitClassDescription;

	/** The combo manage parent trait class. */
	private String comboManageParentTraitClass;

	/** The manage parent trait class id. */
	private Integer manageParentTraitClassId;

	/** The manage parent trait class name. */
	private String manageParentTraitClassName;

	/** The variables linked to trait class. */
	private List<String> variablesLinkedToTraitClass;

	/**
	 * Gets the combo manage trait class.
	 *
	 * @return the combo manage trait class
	 */
	public String getComboManageTraitClass() {
		return this.comboManageTraitClass;
	}

	/**
	 * Sets the combo manage trait class.
	 *
	 * @param comboManageTraitClass the new combo manage trait class
	 */
	public void setComboManageTraitClass(String comboManageTraitClass) {
		this.comboManageTraitClass = comboManageTraitClass;
	}

	/**
	 * Gets the manage trait class id.
	 *
	 * @return the manage trait class id
	 */
	public Integer getManageTraitClassId() {
		return this.manageTraitClassId;
	}

	/**
	 * Sets the manage trait class id.
	 *
	 * @param manageTraitClassId the new manage trait class id
	 */
	public void setManageTraitClassId(Integer manageTraitClassId) {
		this.manageTraitClassId = manageTraitClassId;
	}

	/**
	 * Gets the manage trait class name.
	 *
	 * @return the manage trait class name
	 */
	public String getManageTraitClassName() {
		return this.manageTraitClassName;
	}

	/**
	 * Sets the manage trait class name.
	 *
	 * @param manageTraitClassName the new manage trait class name
	 */
	public void setManageTraitClassName(String manageTraitClassName) {
		this.manageTraitClassName = manageTraitClassName;
	}

	/**
	 * Gets the manage trait class description.
	 *
	 * @return the manage trait class description
	 */
	public String getManageTraitClassDescription() {
		return this.manageTraitClassDescription;
	}

	/**
	 * Sets the manage trait class description.
	 *
	 * @param manageTraitClassDescription the new manage trait class description
	 */
	public void setManageTraitClassDescription(String manageTraitClassDescription) {
		this.manageTraitClassDescription = manageTraitClassDescription;
	}

	/**
	 * Gets the combo manage parent trait class.
	 *
	 * @return the combo manage parent trait class
	 */
	public String getComboManageParentTraitClass() {
		return this.comboManageParentTraitClass;
	}

	/**
	 * Sets the combo manage parent trait class.
	 *
	 * @param comboManageParentTraitClass the new combo manage parent trait class
	 */
	public void setComboManageParentTraitClass(String comboManageParentTraitClass) {
		this.comboManageParentTraitClass = comboManageParentTraitClass;
	}

	/**
	 * Gets the manage parent trait class id.
	 *
	 * @return the manage parent trait class id
	 */
	public Integer getManageParentTraitClassId() {
		return this.manageParentTraitClassId;
	}

	/**
	 * Sets the manage parent trait class id.
	 *
	 * @param manageParentTraitClassId the new manage parent trait class id
	 */
	public void setManageParentTraitClassId(Integer manageParentTraitClassId) {
		this.manageParentTraitClassId = manageParentTraitClassId;
	}

	/**
	 * Gets the manage parent trait class name.
	 *
	 * @return the manage parent trait class name
	 */
	public String getManageParentTraitClassName() {
		return this.manageParentTraitClassName;
	}

	/**
	 * Sets the manage parent trait class name.
	 *
	 * @param manageParentTraitClassName the new manage parent trait class name
	 */
	public void setManageParentTraitClassName(String manageParentTraitClassName) {
		this.manageParentTraitClassName = manageParentTraitClassName;
	}

	/**
	 * Gets the variables linked to trait class.
	 *
	 * @return the variables linked to trait class
	 */
	public List<String> getVariablesLinkedToTraitClass() {
		return this.variablesLinkedToTraitClass;
	}

	/**
	 * Sets the variables linked to trait class.
	 *
	 * @param variablesLinkedToTraitClass the new variables linked to trait class
	 */
	public void setVariablesLinkedToTraitClass(List<String> variablesLinkedToTraitClass) {
		this.variablesLinkedToTraitClass = variablesLinkedToTraitClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#isAddMode()
	 */
	@Override
	public boolean isAddMode() {
		return this.manageTraitClassId == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#getName()
	 */
	@Override
	public String getName() {
		return this.getManageTraitClassName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#getId()
	 */
	@Override
	public Integer getId() {
		return this.getManageTraitClassId();
	}

}
