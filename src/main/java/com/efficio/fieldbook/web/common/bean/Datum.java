
package com.efficio.fieldbook.web.common.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Datum {

	private Integer termId;
	private List<Value> values = new ArrayList<Value>();
	private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 *
	 * @return The termId
	 */
	public Integer getTermId() {
		return this.termId;
	}

	/**
	 *
	 * @param termId The termId
	 */
	public void setTermId(Integer termId) {
		this.termId = termId;
	}

	/**
	 *
	 * @return The values
	 */
	public List<Value> getValues() {
		return this.values;
	}

	/**
	 *
	 * @param values The values
	 */
	public void setValues(List<Value> values) {
		this.values = values;
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
