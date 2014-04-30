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
package com.efficio.fieldbook.web.inventory.bean;

import java.io.Serializable;
import java.util.List;

import org.generationcp.middleware.domain.inventory.InventoryDetails;

/**
 * The Class SeedSelection.
 */
public class SeedSelection implements Serializable {
	
	/** The inventory list. */
	private List<InventoryDetails> inventoryList;

	/**
	 * Gets the inventory list.
	 *
	 * @return the inventory list
	 */
	public List<InventoryDetails> getInventoryList() {
		return inventoryList;
	}

	/**
	 * Sets the inventory list.
	 *
	 * @param inventoryList the new inventory list
	 */
	public void setInventoryList(List<InventoryDetails> inventoryList) {
		this.inventoryList = inventoryList;
	}
	
	
}
