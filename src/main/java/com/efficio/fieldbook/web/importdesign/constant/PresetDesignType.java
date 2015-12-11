
package com.efficio.fieldbook.web.importdesign.constant;

public enum PresetDesignType {

	E30_2REPS_6BLOCKS_5IND(4, "Template1-BW-E30-Rep2-Block6-5Ind.csv", 2), E30_3REPS_6BLOCKS_5IND(5,
			"Template2-BW-E30-Rep3-Block6-5Ind.csv", 3), E50_2REPS_5BLOCKS_10IND(6, "Template3-BW-E50-Rep2-Block5-10 Ind.csv", 2);

	private final int id;
	private final String templateFileName;
	private final int numberOfReps;

	PresetDesignType(final int id, final String templateFileName, int numberOfReps) {
		this.id = id;
		this.templateFileName = templateFileName;
		this.numberOfReps = numberOfReps;
	}

	public int getId() {
		return this.id;
	}

	public String getTemplateName() {
		return this.templateFileName;
	}

	public String getTemplateFileName() {
		return templateFileName;
	}

	public int getNumberOfReps() {
		return numberOfReps;
	}
	
	public static PresetDesignType getPresetDesignTypeById(int id) {
		for (PresetDesignType presetDesignType : PresetDesignType.values()) {
			if(presetDesignType.getId() == id) {
				return presetDesignType;
			}
		}
		return null;
	}

}
