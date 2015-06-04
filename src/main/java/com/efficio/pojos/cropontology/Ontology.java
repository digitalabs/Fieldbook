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

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class Ontology.
 */
public class Ontology implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6345785922568675357L;

	/** The id. */
	private String id;

	/** The name. */
	private String name;

	/** The summary. */
	private String summary;

	/** The username. */
	private String username;

	/** The user id. */
	private Integer userId;

	/**
	 * Instantiates a new ontology.
	 *
	 * @param id the id
	 * @param name the name
	 * @param summary the summary
	 * @param username the username
	 * @param userId the user id
	 */
	@JsonCreator
	public Ontology(@JsonProperty("ontology_id") String id, @JsonProperty("ontology_name") String name,
			@JsonProperty("ontology_summary") String summary, @JsonProperty("username") String username,
			@JsonProperty("userid") Integer userId) {
		this.id = id;
		this.name = name;
		this.summary = summary;
		this.username = username;
		this.userId = userId;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
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
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the summary.
	 *
	 * @return the summary
	 */
	public String getSummary() {
		return this.summary;
	}

	/**
	 * Sets the summary.
	 *
	 * @param summary the new summary
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username the new username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public Integer getUserId() {
		return this.userId;
	}

	/**
	 * Sets the user id.
	 *
	 * @param userId the new user id
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Ontology [id=" + this.id + ", name=" + this.name + ", summary=" + this.summary + ", username=" + this.username
				+ ", userid=" + this.userId + "]";
	}

}
