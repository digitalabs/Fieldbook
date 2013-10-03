package com.efficio.pojos.cropontology;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Name implements Serializable {

	private static final long serialVersionUID = -4754817967345051124L;

	public static final String ENGLISH = "english";
	public static final String CHINESE = "chinese";

	private Map<String, String> names = new LinkedHashMap<String, String>();
	
	@JsonCreator
	public Name(@JsonProperty String name) {
		this.names.put(ENGLISH, name);
	}
	
	@JsonCreator
	public Name(@JsonProperty LinkedHashMap<String, String> names) {
		this.names.putAll(names);
	}

	public Map<String, String> getNames() {
		return names;
	}

	public void setNames(Map<String, String> names) {
		this.names = names;
	}

	@Override
	public String toString() {
		return "Name [names=" + names + "]";
	}

}
