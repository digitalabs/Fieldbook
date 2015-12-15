
package com.efficio.fieldbook.web.importdesign.constant;

public enum PresetDesignType {

	E30_2REPS_6BLOCKS_5IND(4, "E30-Rep2-Block6-5Ind", 2, 30), E30_3REPS_6BLOCKS_5IND(5, "E30-Rep3-Block6-5Ind", 3, 30), E50_2REPS_5BLOCKS_10IND(
			6, "E50-Rep2-Block5-10Ind", 2, 50);

	private static final String TEMPLATE_FILE_EXT = ".csv";
	private final int id;
	private final String name;
	private final int numberOfReps;
	private final int totalNoOfEntries;

	PresetDesignType(final int id, final String name, final int numberOfReps, final int totalNoOfEntries) {
		this.id = id;
		this.name = name;
		this.numberOfReps = numberOfReps;
		this.totalNoOfEntries = totalNoOfEntries;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getTemplateName() {
		return this.name.concat(TEMPLATE_FILE_EXT);
	}

	public int getNumberOfReps() {
		return this.numberOfReps;
	}

	public int getTotalNoOfEntries() {
		return this.totalNoOfEntries;
	}

	public static PresetDesignType getPresetDesignTypeById(final int id) {
		for (final PresetDesignType presetDesignType : PresetDesignType.values()) {
			if (presetDesignType.getId() == id) {
				return presetDesignType;
			}
		}
		return null;
	}

	public static PresetDesignType getPresetDesignTypeByName(final String name) {
		for (final PresetDesignType presetDesignType : PresetDesignType.values()) {
			if (name.equalsIgnoreCase(presetDesignType.getName())) {
				return presetDesignType;
			}
		}
		return null;
	}

}
