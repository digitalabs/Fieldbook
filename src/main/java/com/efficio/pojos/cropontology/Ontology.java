package com.efficio.pojos.cropontology;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Ontology implements Serializable {

	private static final long serialVersionUID = 6345785922568675357L;

	private String id;
	private String name;
	private String summary;
	private String username;
	private Integer userId;
	
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
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "Ontology [id=" + id + ", name=" + name + ", summary=" + summary
				+ ", username=" + username + ", userid=" + userId + "]";
	}
	
}
