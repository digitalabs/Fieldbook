package com.efficio.fieldbook.web.common.controller.derived_variables;

import java.util.List;

public class CalculateVariableRequest {
	private Integer variableId;
	private List<Integer> geoLocationIds;

	public Integer getVariableId() {
		return this.variableId;
	}

	public void setVariableId(final Integer variableId) {
		this.variableId = variableId;
	}

	public List<Integer> getGeoLocationIds() {
		return this.geoLocationIds;
	}

	public void setGeoLocationIds(final List<Integer> geoLocationIds) {
		this.geoLocationIds = geoLocationIds;
	}

}
