
package com.efficio.fieldbook.web.importdesign.constant;

public enum BreedingViewDesignType {

	RANDOMIZED_BLOCK_DESIGN("Randomized Complete Block Design", "randomizedCompleteBlockParams.html", false), INCOMPLETE_BLOCK_DESIGN(
			"Resolvable Incomplete Block Design", "incompleteBlockParams.html", true), ROW_COLUMN_DESIGN("Row-and-Column",
			"rowAndColumnParams.html", true);

	String name;
	// this is an html file that contains the specific fields under design type
	String params;
	boolean withResolvable;

	private BreedingViewDesignType(final String name, final String params, final boolean withResolvable) {
		this.name = name;
		this.params = params;
		this.withResolvable = withResolvable;
	}

	public String getName() {
		return this.name;
	}

	public String getParams() {
		return this.params;
	}

	public boolean withResolvable() {
		return this.withResolvable;
	}

}
