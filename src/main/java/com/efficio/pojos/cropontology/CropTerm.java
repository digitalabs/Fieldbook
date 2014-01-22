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
package com.efficio.pojos.cropontology;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class CropTerm.
 */
public class CropTerm implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2060716251936951785L;

	/** The id. */
	private String id;
	
	/** The name. */
	private Name name;
	
	/** The ontology name. */
	private Name ontologyName;
	
	/**
	 * Instantiates a new crop term.
	 *
	 * @param id the id
	 * @param name the name
	 * @param ontologyName the ontology name
	 */
	@JsonCreator
	public CropTerm(@JsonProperty("id") String id, @JsonProperty("name") Name name
	        , @JsonProperty("ontology_name") Name ontologyName) {
		this.id = id;
		this.name = name;
		this.ontologyName = ontologyName;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
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
	public Name getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(Name name) {
		this.name = name;
	}

	/**
	 * Gets the ontology name.
	 *
	 * @return the ontology name
	 */
	public Name getOntologyName() {
		return ontologyName;
	}

	/**
	 * Sets the ontology name.
	 *
	 * @param ontologyName the new ontology name
	 */
	public void setOntologyName(Name ontologyName) {
		this.ontologyName = ontologyName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CropTerm [id=" + id + ", name=" + name + ", ontologyName="
				+ ontologyName + "]";
	}
	
}
