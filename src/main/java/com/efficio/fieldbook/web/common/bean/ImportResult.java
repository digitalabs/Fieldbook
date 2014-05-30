package com.efficio.fieldbook.web.common.bean;

import java.util.List;

public class ImportResult {

	private int mode;
	private List<GermplasmChangeDetail> changeDetails;
	private String errorMessage;
	
	public ImportResult(int mode, List<GermplasmChangeDetail> changeDetails){
		this.mode = mode;
		this.changeDetails = changeDetails;
	}
	
	public ImportResult(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	/**
	 * @return the mode
	 */
	public int getMode() {
		return mode;
	}
	/**
	 * @param mode the mode to set
	 */
	public void setMode(int mode) {
		this.mode = mode;
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
