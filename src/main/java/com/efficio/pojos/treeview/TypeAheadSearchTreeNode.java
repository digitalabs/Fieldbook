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

package com.efficio.pojos.treeview;

import java.util.List;

/**
 * This class holds the data needed for rendering a tree view using dynatree jquery.
 */
public class TypeAheadSearchTreeNode {

	/** The value. */
	private String value;

	/** The tokens. */
	private List<String> tokens;

	/** The key. */
	private String key;

	/** The parent title. */
	private String parentTitle;

	/** The type. */
	private String type;

	/**
	 * Instantiates a new type ahead search tree node.
	 */
	public TypeAheadSearchTreeNode() {
	}

	/**
	 * Instantiates a new type ahead search tree node.
	 *
	 * @param key the key
	 * @param tokens the tokens
	 * @param value the value
	 * @param parentTitle the parent title
	 * @param type the type
	 */
	public TypeAheadSearchTreeNode(String key, List<String> tokens, String value, String parentTitle, String type) {
		this.key = key;
		this.tokens = tokens;
		this.value = value;
		this.parentTitle = parentTitle;
		this.type = type;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Gets the tokens.
	 *
	 * @return the tokens
	 */
	public List<String> getTokens() {
		return this.tokens;
	}

	/**
	 * Sets the tokens.
	 *
	 * @param tokens the new tokens
	 */
	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key the new key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Gets the parent title.
	 *
	 * @return the parent title
	 */
	public String getParentTitle() {
		return this.parentTitle;
	}

	/**
	 * Sets the parent title.
	 *
	 * @param parentTitle the new parent title
	 */
	public void setParentTitle(String parentTitle) {
		this.parentTitle = parentTitle;
	}

}
