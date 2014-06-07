package com.efficio.fieldbook.web.common.bean;

import java.util.List;
import java.util.Set;

public class ImportResult {

	private Set<ChangeType> modes;
	private List<GermplasmChangeDetail> changeDetails;
	private String errorMessage;
	
	public ImportResult(Set<ChangeType> modes, List<GermplasmChangeDetail> changeDetails){
		this.modes = modes;
		this.changeDetails = changeDetails;
	}
	
	public ImportResult(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	/**
	 * @return the modes
	 */
	public Set<ChangeType> getModes() {
		return modes;
	}

	/**
	 * @param modes the modes to set
	 */
	public void setModes(Set<ChangeType> modes) {
		this.modes = modes;
	}

	/**
	 * @return the changeDetails
	 */
	public List<GermplasmChangeDetail> getChangeDetails() {
		return changeDetails;
	}
	/**
	 * @param changeDetails the changeDetails to set
	 */
	public void setChangeDetails(List<GermplasmChangeDetail> changeDetails) {
		this.changeDetails = changeDetails;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
}
