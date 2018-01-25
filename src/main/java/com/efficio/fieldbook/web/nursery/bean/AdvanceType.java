package com.efficio.fieldbook.web.nursery.bean;

public enum AdvanceType {
	TRIAL("trial"), //
	SAMPLE("sample"), //
	NURSERY("nursery"), //
	NONE("") //
	;

	private final String lowerCaseName;

	AdvanceType(String lowerCaseName) {
		this.lowerCaseName = lowerCaseName;
	}

	public String getLowerCaseName() {
		return lowerCaseName;
	}

	public static AdvanceType fromLowerCaseName(String lowerCaseName) {
		for (AdvanceType advanceType : AdvanceType.values()) {
			if (advanceType.getLowerCaseName().equals(lowerCaseName)) {
				return advanceType;
			}
		}
		return AdvanceType.NONE;
	}
}
