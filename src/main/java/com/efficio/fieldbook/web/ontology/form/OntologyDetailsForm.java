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

import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;

import java.util.HashSet;
import java.util.Set;

/**
 * The Class OntologyBrowserForm.
 *
 * @author Efficio.Daniel
 */
public class OntologyDetailsForm {

	/** The standard variable. */
	private Variable variable;

	private Long projectCount = 0L;

	private Long observationCount = 0L;

	private VariableType currentVariableType;

	public OntologyDetailsForm() {
	}

	public OntologyDetailsForm(Variable variable) {
		this.variable = variable;
	}

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}

	public Long getProjectCount() {
		return projectCount;
	}

	public void setProjectCount(Long projectCount) {
		this.projectCount = projectCount;
	}

	public Long getObservationCount() {
		return observationCount;
	}

	public void setObservationCount(Long observationCount) {
		this.observationCount = observationCount;
	}

	public Set<String> getVariableTypeNames() {
		Set<String> variableTypes = new HashSet<>();
		for (VariableType variableType : variable.getVariableTypes()) {
			variableTypes.add(variableType.getName());
		}
		return variableTypes;
	}

	public VariableType getCurrentVariableType() {
		return currentVariableType;
	}

	public void setCurrentVariableType(VariableType currentVariableType) {
		this.currentVariableType = currentVariableType;
	}
}
