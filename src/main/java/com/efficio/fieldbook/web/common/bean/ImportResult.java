
package com.efficio.fieldbook.web.common.bean;

import java.util.List;
import java.util.Set;

public class ImportResult {

	private Set<ChangeType> modes;
	private List<GermplasmChangeDetail> changeDetails;
	private List<String> variablesAdded;
	private List<String> variablesRemoved;
	private String errorMessage;
	private String conditionsAndConstantsErrorMessage;

	public ImportResult(final Set<ChangeType> modes, final List<GermplasmChangeDetail> changeDetails) {
		this.modes = modes;
		this.changeDetails = changeDetails;
	}

	public ImportResult(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the modes
	 */
	public Set<ChangeType> getModes() {
		return this.modes;
	}

	/**
	 * @param modes the modes to set
	 */
	public void setModes(final Set<ChangeType> modes) {
		this.modes = modes;
	}

	/**
	 * @return the changeDetails
	 */
	public List<GermplasmChangeDetail> getChangeDetails() {
		return this.changeDetails;
	}

	/**
	 * @param changeDetails the changeDetails to set
	 */
	public void setChangeDetails(final List<GermplasmChangeDetail> changeDetails) {
		this.changeDetails = changeDetails;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getConditionsAndConstantsErrorMessage() {
		return this.conditionsAndConstantsErrorMessage;
	}

	public void setConditionsAndConstantsErrorMessage(final String conditionsAndConstantsErrorMessage) {
		this.conditionsAndConstantsErrorMessage = conditionsAndConstantsErrorMessage;
	}

	public List<String> getVariablesAdded() {
		return variablesAdded;
	}

	public void setVariablesAdded(final List<String> variablesAdded) {
		this.variablesAdded = variablesAdded;
	}

	public List<String> getVariablesRemoved() {
		return variablesRemoved;
	}

	public void setVariablesRemoved(final List<String> variablesRemoved) {
		this.variablesRemoved = variablesRemoved;
	}

}
