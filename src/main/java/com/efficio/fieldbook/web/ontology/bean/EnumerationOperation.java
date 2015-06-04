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

package com.efficio.fieldbook.web.ontology.bean;

/**
 * The Class EnumerationOperation.
 *
 * @author Chezka Camille Arevalo
 */
public class EnumerationOperation {

	/** The id. */
	private Integer id;

	/** The name. */
	private String name;

	/** The description. */
	private String description;

	/** The operation. */
	private int operation;

	/**
	 * Instantiates a new enumeration operation.
	 *
	 * @param id the id
	 * @param name the name
	 * @param description the description
	 * @param operation the operation
	 */
	public EnumerationOperation(Integer id, String name, String description, int operation) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.operation = operation;
	}

	/**
	 * Instantiates a new enumeration operation.
	 */
	public EnumerationOperation() {

	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Gets the operation.
	 *
	 * @return the operation
	 */
	public int getOperation() {
		return this.operation;
	}

}
