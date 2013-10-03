package com.efficio.pojos.cropontology;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class CropTerm implements Serializable {

	private static final long serialVersionUID = 2060716251936951785L;

	private String id;
	private Name name;
	private Name ontologyName;
	
	@JsonCreator
	public CropTerm(@JsonProperty("id") String id, @JsonProperty("name") Name name, @JsonProperty("ontology_name") Name ontologyName) {
		this.id = id;
		this.name = name;
		this.ontologyName = ontologyName;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public Name getOntologyName() {
		return ontologyName;
	}

	public void setOntologyName(Name ontologyName) {
		this.ontologyName = ontologyName;
	}

	@Override
	public String toString() {
		return "CropTerm [id=" + id + ", name=" + name + ", ontologyName="
				+ ontologyName + "]";
	}
	
}
