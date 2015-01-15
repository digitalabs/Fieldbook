package com.efficio.fieldbook.web.common.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ReviewOutOfBoundsChanges {

	private List<Datum> data = new ArrayList<Datum>();
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * 
	 * @return The data
	 */
	public List<Datum> getData() {
		return data;
	}

	/**
	 * 
	 * @param data
	 *            The data
	 */
	public void setData(List<Datum> data) {
		this.data = data;
	}

	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}