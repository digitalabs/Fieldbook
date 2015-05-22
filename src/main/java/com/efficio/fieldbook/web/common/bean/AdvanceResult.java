package com.efficio.fieldbook.web.common.bean;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;

import java.util.List;

public class AdvanceResult {

	private List<ImportedGermplasm> advanceList;
	
	private List<AdvanceGermplasmChangeDetail> changeDetails;

	/**
	 * @return the advanceList
	 */
	public List<ImportedGermplasm> getAdvanceList() {
		return advanceList;
	}

	/**
	 * @param advanceList the advanceList to set
	 */
	public void setAdvanceList(List<ImportedGermplasm> advanceList) {
		this.advanceList = advanceList;
	}

	/**
	 * @return the changeDetails
	 */
	public List<AdvanceGermplasmChangeDetail> getChangeDetails() {
		return changeDetails;
	}

	/**
	 * @param changeDetails the changeDetails to set
	 */
	public void setChangeDetails(List<AdvanceGermplasmChangeDetail> changeDetails) {
		this.changeDetails = changeDetails;
	}
	
	
}
