
package com.efficio.fieldbook.web.importdesign.constant;

public enum DesignType {

	RANDOMIZED_BLOCK_DESIGN(1, ""), INCOMPLETE_BLOCK_DESIGN(2, ""), ROW_COLUMN_DESIGN(3, ""), E30_2REPS_6BLOCKS_5IND(4,
			"Template1-BW-E30-Rep2-Block6-5Ind.csv"), E30_3REPS_6BLOCKS_5IND(5, "Template2-BW-E30-Rep3-Block6-5Ind.csv"), E50_2REPS_5BLOCKS_10IND(
			6, "Template3-BW-E50-Rep2-Block5-10 Ind.csv");

	private final int id;
	private final String templateFileName;

	DesignType(final int id, final String templateFileName) {
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
