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

package com.efficio.fieldbook.web.inventory.form;

import java.util.List;

import org.generationcp.middleware.domain.inventory.InventoryDetails;

// TODO: Auto-generated Javadoc
/**
 * The Class SeedStoreForm.
 */
public class SeedStoreForm {

	/** The inventory list. */
	private List<InventoryDetails> inventoryList;

	// for pagination
	/** The paginated inventory list. */
	private List<InventoryDetails> paginatedInventoryList;

	/** The current page. */
	private int currentPage;

	/** The total pages. */
	private int totalPages;

	/** The result per page. */
	private int resultPerPage = 100;

	/** The location id. */
	private int inventoryLocationId;

	/** The scale id. */
	private int inventoryScaleId;

	/** The comments. */
	private String inventoryComments;

	/** The gid list. */
	private String gidList;

	private String stockIdsForUpdate;

	/** The list id. */
	private Integer listId;

	/** The total number of germplasms. */
	private int totalNumberOfGermplasms;

	/** The amount. */
	private Double amount;

	/**
	 * Gets the list id.
	 *
	 * @return the list id
	 */
	public Integer getListId() {
		return this.listId;
	}

	/**
	 * Sets the list id.
	 *
	 * @param listId the new list id
	 */
	public void setListId(final Integer listId) {
		this.listId = listId;
	}

	/**
	 * Gets the total number of germplasms.
	 *
	 * @return the total number of germplasms
	 */
	public int getTotalNumberOfGermplasms() {
		if (this.inventoryList != null) {
			return this.inventoryList.size();
		}
		return 0;
	}

	/**
	 * Sets the total number of germplasms.
	 *
	 * @param totalNumberOfGermplasms the new total number of germplasms
	 */
	public void setTotalNumberOfGermplasms(final int totalNumberOfGermplasms) {
		this.totalNumberOfGermplasms = totalNumberOfGermplasms;
	}

	/**
	 * Gets the result per page.
	 *
	 * @return the result per page
	 */
	public int getResultPerPage() {
		return this.resultPerPage;
	}

	/**
	 * Sets the result per page.
	 *
	 * @param resultPerPage the new result per page
	 */
	public void setResultPerPage(final int resultPerPage) {
		this.resultPerPage = resultPerPage;
	}

	/**
	 * Gets the total pages.
	 *
	 * @return the total pages
	 */
	public int getTotalPages() {
		if (this.inventoryList != null && !this.inventoryList.isEmpty()) {
			this.totalPages = (int) Math.ceil(this.inventoryList.size() * 1f / this.getResultPerPage());
		} else {
			this.totalPages = 0;
		}
		return this.totalPages;
	}

	/**
	 * Gets the current page.
	 *
	 * @return the current page
	 */
	public int getCurrentPage() {
		return this.currentPage;
	}

	/**
	 * Sets the current page.
	 *
	 * @param currentPage the new current page
	 */
	public void setCurrentPage(final int currentPage) {

		// assumption is there are nursery list already
		if (this.inventoryList != null && !this.inventoryList.isEmpty()) {
			final int totalItemsPerPage = this.getResultPerPage();
			final int start = (currentPage - 1) * totalItemsPerPage;
			int end = start + totalItemsPerPage;
			if (this.inventoryList.size() < end) {
				end = this.inventoryList.size();
			}
			this.paginatedInventoryList = this.inventoryList.subList(start, end);
			this.currentPage = currentPage;
		} else {
			this.currentPage = 0;
		}

	}

	/**
	 * Sets the total pages.
	 *
	 * @param totalPages the new total pages
	 */
	public void setTotalPages(final int totalPages) {
		this.totalPages = totalPages;
	}

	/**
	 * Gets the inventory list.
	 *
	 * @return the inventory list
	 */
	public List<InventoryDetails> getInventoryList() {
		return this.inventoryList;
	}

	/**
	 * Sets the inventory list.
	 *
	 * @param inventoryList the new inventory list
	 */
	public void setInventoryList(final List<InventoryDetails> inventoryList) {
		this.inventoryList = inventoryList;
	}

	/**
	 * Gets the paginated inventory list.
	 *
	 * @return the paginated inventory list
	 */
	public List<InventoryDetails> getPaginatedInventoryList() {
		return this.paginatedInventoryList;
	}

	/**
	 * Sets the paginated inventory list.
	 *
	 * @param paginatedInventoryList the new paginated inventory list
	 */
	public void setPaginatedInventoryList(final List<InventoryDetails> paginatedInventoryList) {
		this.paginatedInventoryList = paginatedInventoryList;
	}

	/**
	 * Gets the inventory location id.
	 *
	 * @return the inventory location id
	 */
	public int getInventoryLocationId() {
		return this.inventoryLocationId;
	}

	/**
	 * Sets the inventory location id.
	 *
	 * @param inventoryLocationId the new inventory location id
	 */
	public void setInventoryLocationId(final int inventoryLocationId) {
		this.inventoryLocationId = inventoryLocationId;
	}

	/**
	 * Gets the inventory scale id.
	 *
	 * @return the inventory scale id
	 */
	public int getInventoryScaleId() {
		return this.inventoryScaleId;
	}

	/**
	 * Sets the inventory scale id.
	 *
	 * @param inventoryScaleId the new inventory scale id
	 */
	public void setInventoryScaleId(final int inventoryScaleId) {
		this.inventoryScaleId = inventoryScaleId;
	}

	/**
	 * Gets the inventory comments.
	 *
	 * @return the inventory comments
	 */
	public String getInventoryComments() {
		return this.inventoryComments;
	}

	/**
	 * Sets the inventory comments.
	 *
	 * @param inventoryComments the new inventory comments
	 */
	public void setInventoryComments(final String inventoryComments) {
		this.inventoryComments = inventoryComments;
	}

	/**
	 * Gets the gid list.
	 *
	 * @return the gidList
	 */
	public String getGidList() {
		return this.gidList;
	}

	/**
	 * Sets the gid list.
	 *
	 * @param gidList the gidList to set
	 */
	public void setGidList(final String gidList) {
		this.gidList = gidList;
	}

	public String getStockIdsForUpdate() {
		return this.stockIdsForUpdate;
	}

	public void setStockIdsForUpdate(final String stockIdsForUpdate) {
		this.stockIdsForUpdate = stockIdsForUpdate;
	}

	/**
	 * Gets the amount.
	 *
	 * @return the amount
	 */
	public Double getAmount() {
		return this.amount;
	}

	/**
	 * Sets the amount.
	 *
	 * @param amount the new amount
	 */
	public void setAmount(final Double amount) {
		this.amount = amount;
	}

}
