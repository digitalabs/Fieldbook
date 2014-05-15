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
package com.efficio.fieldbook.web.common.form;

// TODO: Auto-generated Javadoc
/**
 * The Class SaveListForm.
 */
public class SaveListForm {
	
	/** The list name. */
	private String listName;
	
	/** The list description. */
	private String listDescription;
	
	/** The list type. */
	private String listType;
	
	/** The list date. */
	private String listDate;
	
	/** The list notes. */
	private String listNotes;
	
	private String listIdentifier; //to be use for dynamic of advance list		
	
	public String getListIdentifier() {
		return listIdentifier;
	}

	public void setListIdentifier(String listIdentifier) {
		this.listIdentifier = listIdentifier;
	}

	/**
	 * Gets the list name.
	 *
	 * @return the list name
	 */
	public String getListName() {
		return listName;
	}
	
	/**
	 * Sets the list name.
	 *
	 * @param listName the new list name
	 */
	public void setListName(String listName) {
		this.listName = listName;
	}
	
	/**
	 * Gets the list description.
	 *
	 * @return the list description
	 */
	public String getListDescription() {
		return listDescription;
	}
	
	/**
	 * Sets the list description.
	 *
	 * @param listDescription the new list description
	 */
	public void setListDescription(String listDescription) {
		this.listDescription = listDescription;
	}
	
	/**
	 * Gets the list type.
	 *
	 * @return the list type
	 */
	public String getListType() {
		return listType;
	}
	
	/**
	 * Sets the list type.
	 *
	 * @param listType the new list type
	 */
	public void setListType(String listType) {
		this.listType = listType;
	}
	
	/**
	 * Gets the list date.
	 *
	 * @return the list date
	 */
	public String getListDate() {
		return listDate;
	}
	
	/**
	 * Sets the list date.
	 *
	 * @param listDate the new list date
	 */
	public void setListDate(String listDate) {
		this.listDate = listDate;
	}
	
	/**
	 * Gets the list notes.
	 *
	 * @return the list notes
	 */
	public String getListNotes() {
		return listNotes;
	}
	
	/**
	 * Sets the list notes.
	 *
	 * @param listNotes the new list notes
	 */
	public void setListNotes(String listNotes) {
		this.listNotes = listNotes;
	}
	
}
