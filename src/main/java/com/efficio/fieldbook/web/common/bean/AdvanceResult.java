
package com.efficio.fieldbook.web.common.bean;

import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.AdvanceGermplasmChangeDetail;

public class AdvanceResult {

	private List<ImportedGermplasm> advanceList;

	private List<AdvanceGermplasmChangeDetail> changeDetails;

	/**
	 * @return the advanceList
	 */
	public List<ImportedGermplasm> getAdvanceList() {
		return this.advanceList;
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
		return this.changeDetails;
	}

	/**
	 * @param changeDetails the changeDetails to set
	 */
	public void setChangeDetails(List<AdvanceGermplasmChangeDetail> changeDetails) {
		this.changeDetails = changeDetails;
	}

}
