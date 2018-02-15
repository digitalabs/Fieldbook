package com.efficio.fieldbook.web.trial.bean;

public class CrossesList extends AdvanceList {

	private String crossesType;

	public CrossesList(final Integer id, final String name, final String crossesType) {
		super(id, name);
		this.crossesType = crossesType;
	}

	public String getCrossesType() {
		return crossesType;
	}

	public void setCrossesType(String crossesType) {
		this.crossesType = crossesType;
	}
}
