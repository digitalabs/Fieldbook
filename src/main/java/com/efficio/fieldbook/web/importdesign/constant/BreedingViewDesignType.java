
package com.efficio.fieldbook.web.importdesign.constant;

public enum BreedingViewDesignType {

	RANDOMIZED_BLOCK_DESIGN("Randomized Complete Block Design", "randomizedCompleteBlockParams.html"), //
	INCOMPLETE_BLOCK_DESIGN("Resolvable Incomplete Block Design", "incompleteBlockParams.html"), //
	ROW_COLUMN_DESIGN("Row-and-Column", "rowAndColumnParams.html");

	String name;
	// this is an html file that contains the specific fields under design type
	String params;

	private BreedingViewDesignType(final String name, final String params) {
		this.name = name;
		this.params = params;
	}

	public String getName() {
		return this.name;
	}

	public String getParams() {
		return this.params;
	}
}
