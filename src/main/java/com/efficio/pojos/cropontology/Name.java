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

package com.efficio.pojos.cropontology;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class Name.
 */
public class Name implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4754817967345051124L;

	/** The Constant ENGLISH. */
	public static final String ENGLISH = "english";

	/** The Constant CHINESE. */
	public static final String CHINESE = "chinese";

	/** The names. */
	private Map<String, String> names = new LinkedHashMap<String, String>();

	/**
	 * Instantiates a new name.
	 *
	 * @param name the name
	 */
	@JsonCreator
	public Name(@JsonProperty String name) {
		this.names.put(Name.ENGLISH, name);
	}

	/**
	 * Instantiates a new name.
	 *
	 * @param names the names
	 */
	@JsonCreator
	public Name(@JsonProperty LinkedHashMap<String, String> names) {
		this.names.putAll(names);
	}

	/**
	 * Gets the names.
	 *
	 * @return the names
	 */
	public Map<String, String> getNames() {
		return this.names;
	}

	/**
	 * Sets the names.
	 *
	 * @param names the names
	 */
	public void setNames(Map<String, String> names) {
		this.names = names;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Name [names=" + this.names + "]";
	}

}
