
package com.efficio.etl.web.bean;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class IndexValueDTO {

	private final int index;
	private final String value;

	public IndexValueDTO(int index, String value) {
		this.index = index;
		this.value = value;
	}

	public int getIndex() {
		return this.index;
	}

	public String getValue() {
		return this.value;
	}
}
