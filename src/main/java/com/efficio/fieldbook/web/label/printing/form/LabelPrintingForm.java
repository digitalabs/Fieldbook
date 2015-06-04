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

package com.efficio.fieldbook.web.label.printing.form;

import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;

/**
 * The Class LabelPrintingForm.
 */
public class LabelPrintingForm {

	private boolean isCustomReport;
	/** The is trial. */
	private boolean isTrial;

	/** The user label printing. */
	private UserLabelPrinting userLabelPrinting;

	/** Determine if it is a stock list */
	private boolean isStockList;

	/**
	 * Gets the user label printing.
	 *
	 * @return the user label printing
	 */
	public UserLabelPrinting getUserLabelPrinting() {
		return this.userLabelPrinting;
	}

	/**
	 * Sets the user label printing.
	 *
	 * @param userLabelPrinting the new user label printing
	 */
	public void setUserLabelPrinting(UserLabelPrinting userLabelPrinting) {
		this.userLabelPrinting = userLabelPrinting;
	}

	/**
	 * Gets the checks if is trial.
	 *
	 * @return the checks if is trial
	 */
	public boolean getIsTrial() {
		return this.isTrial;
	}

	/**
	 * Sets the checks if is trial.
	 *
	 * @param isTrial the new checks if is trial
	 */
	public void setIsTrial(boolean isTrial) {
		this.isTrial = isTrial;
	}

	/**
	 * Get the checks if it is a stock list
	 * 
	 * @return isStockList
	 */
	public boolean getIsStockList() {
		return this.isStockList;
	}

	/**
	 * Sets if it is a stock list
	 * 
	 * @param isStockList
	 */
	public void setIsStockList(boolean isStockList) {
		this.isStockList = isStockList;
	}

	public boolean isCustomReport() {
		return this.isCustomReport;
	}

	public void setCustomReport(boolean isCustomReport) {
		this.isCustomReport = isCustomReport;
	}

}
