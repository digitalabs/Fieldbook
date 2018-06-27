package com.efficio.fieldbook.web.common.controller.derived_variables;

public class CalculateVariableRequest {
	private Integer variableId;
	private Integer geoLocationId;

	public Integer getVariableId() {
		return variableId;
	}

	public void setVariableId(final Integer variableId) {
		this.variableId = variableId;
	}

	public Integer getGeoLocationId() {
		return geoLocationId;
	}

	public void setGeoLocationId(final Integer geoLocationId) {
		this.geoLocationId = geoLocationId;
	}

}
