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
 * The Class OntologyScaleForm.
 */
public class OntologyScaleForm implements OntologyModalForm {

	/** The combo manage scale. */
	private String comboManageScale;

	/** The manage scale id. */
	private Integer manageScaleId;

	/** The manage scale name. */
	private String manageScaleName;

	/** The manage scale description. */
	private String manageScaleDescription;

	/** The variables linked to scale. */
	private List<String> variablesLinkedToScale;

	/**
	 * Gets the combo manage scale.
	 *
	 * @return the combo manage scale
	 */
	public String getComboManageScale() {
		return this.comboManageScale;
	}

	/**
	 * Sets the combo manage scale.
	 *
	 * @param comboManageScale the new combo manage scale
	 */
	public void setComboManageScale(String comboManageScale) {
		this.comboManageScale = comboManageScale;
	}

	/**
	 * Gets the manage scale id.
	 *
	 * @return the manage scale id
	 */
	public Integer getManageScaleId() {
		return this.manageScaleId;
	}

	/**
	 * Sets the manage scale id.
	 *
	 * @param manageScaleId the new manage scale id
	 */
	public void setManageScaleId(Integer manageScaleId) {
		this.manageScaleId = manageScaleId;
	}

	/**
	 * Gets the manage scale description.
	 *
	 * @return the manage scale description
	 */
	public String getManageScaleDescription() {
		return this.manageScaleDescription;
	}

	/**
	 * Sets the manage scale description.
	 *
	 * @param manageScaleDescription the new manage scale description
	 */
	public void setManageScaleDescription(String manageScaleDescription) {
		this.manageScaleDescription = manageScaleDescription;
	}

	/**
	 * Gets the variables linked to scale.
	 *
	 * @return the variables linked to scale
	 */
	public List<String> getVariablesLinkedToScale() {
		return this.variablesLinkedToScale;
	}

	/**
	 * Sets the variables linked to scale.
	 *
	 * @param variablesLinkedToScale the new variables linked to scale
	 */
	public void setVariablesLinkedToScale(List<String> variablesLinkedToScale) {
		this.variablesLinkedToScale = variablesLinkedToScale;
	}

	/**
	 * Gets the manage scale name.
	 *
	 * @return the manage scale name
	 */
	public String getManageScaleName() {
		return this.manageScaleName;
	}

	/**
	 * Sets the manage scale name.
	 *
	 * @param manageScaleName the new manage scale name
	 */
	public void setManageScaleName(String manageScaleName) {
		this.manageScaleName = manageScaleName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#isAddMode()
	 */
	@Override
	public boolean isAddMode() {
		return this.manageScaleId == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#getName()
	 */
	@Override
	public String getName() {
		return this.getManageScaleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.ontology.form.OntologyModalForm#getId()
	 */
	@Override
	public Integer getId() {
		return this.getManageScaleId();
	}

}
