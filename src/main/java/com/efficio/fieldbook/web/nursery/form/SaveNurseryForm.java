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
package com.efficio.fieldbook.web.nursery.form;

/**
 * Form for the Nursery Wizard - Step 4.
 * 
 */
public class SaveNurseryForm {
	
	private String title;
	
	private String objective;
	
	private String nurseryBookName;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getObjective() {
		return objective;
	}

	public void setObjective(String objective) {
		this.objective = objective;
	}

	public String getNurseryBookName() {
		return nurseryBookName;
	}

	public void setNurseryBookName(String nurseryBookName) {
		this.nurseryBookName = nurseryBookName;
	}

}
