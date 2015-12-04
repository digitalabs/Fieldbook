
package com.efficio.fieldbook.web.importdesign.constant;

public enum DesignType {

	RANDOMIZED_BLOCK_DESIGN(1, ""), INCOMPLETE_BLOCK_DESIGN(2, ""), ROW_COLUMN_DESIGN(3, ""), E30_2REPS_6BLOCKS_5IND(4, ""), E30_3REPS_6BLOCKS_5IND(
			5, ""), E50_2REPS_5BLOCKS_10IND(6, "");

	private final int id;
	private final String templateFileName;

	DesignType(int id, String templateFileName) {
		this.id = id;
		this.templateFileName = templateFileName;
	}

	public int getId() {
		return this.id;
	}

	public String getTemplateName() {
		return this.templateFileName;
	}

}
